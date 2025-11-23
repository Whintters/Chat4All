package com.chat4all.frontend.controller;

import com.chat4all.frontend.service.MessageProducerService;
import com.chat4all.shared.event.MessageEvent;
import com.chat4all.frontend.repository.MessageRepository; // Interface ReactiveCassandraRepository
import com.chat4all.frontend.model.MessageEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class MessageController {

    private final MessageProducerService producerService;
    private final MessageRepository messageRepository;
    private static final String STATIC_TOKEN = "Bearer chat4all-secret-key"; // Autenticação Simples 

    public MessageController(MessageProducerService producerService, MessageRepository messageRepository) {
        this.producerService = producerService;
        this.messageRepository = messageRepository;
    }

    // Endpoint 1: Envio de mensagem (POST /v1/messages) [cite: 8]
    @PostMapping("/messages")
    public Mono<ResponseEntity<Void>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MessageRequestDto request) {

        // Autenticação Simples
        if (authHeader == null || !authHeader.equals(STATIC_TOKEN)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        // Criação do Evento
        MessageEvent event = new MessageEvent(
                UUID.randomUUID().toString(),
                request.getConversationId(),
                request.getSenderId(),
                request.getContent(),
                Instant.now().toString()
        );

        // Envio Assíncrono para o Kafka (Particionado por conversation_id) [cite: 13]
        return producerService.sendMessage(event)
                .map(v -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    // Endpoint 2: Listar mensagens (GET /v1/conversations/{id}/messages) [cite: 9]
    @GetMapping("/conversations/{conversationId}/messages")
    public Flux<MessageEntity> getMessages(@PathVariable String conversationId) {
        // Leitura direta do Cassandra (Eventual Consistency) [cite: 89]
        return messageRepository.findByConversationId(conversationId);
    }
}