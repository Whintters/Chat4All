package com.chat4all.frontend.controller;

import com.chat4all.frontend.model.MessageEntity;
import com.chat4all.frontend.repository.MessageRepository;
import com.chat4all.frontend.service.MessageProducerService;
import com.chat4all.shared.event.MessageEvent;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageProducerService producerService;
    private final MessageRepository messageRepository;
    
    // Token estático para autenticação simples (conforme PDF 1)
    private static final String STATIC_TOKEN = "Bearer chat4all-secret-key";

    public MessageController(MessageProducerService producerService, MessageRepository messageRepository) {
        this.producerService = producerService;
        this.messageRepository = messageRepository;
    }

    // Endpoint 1: Enviar Mensagem (Texto ou Arquivo)
    @PostMapping("/messages")
    public Mono<ResponseEntity<Void>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MessageRequestDto request) {

        // Validação de Autenticação
        if (authHeader == null || !authHeader.equals(STATIC_TOKEN)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        // Lógica para definir o tipo da mensagem (default: "text")
        // Isso atende ao requisito de suportar payloads com type: "file"
        String msgType = (request.getType() != null) ? request.getType() : "text";

        // Criação do Evento (DTO Compartilhado)
        // A ordem dos parâmetros deve bater com o construtor do MessageEvent (Shared Library)
        MessageEvent event = new MessageEvent(
                UUID.randomUUID().toString(),   // messageId
                request.getConversationId(),    // conversationId
                request.getSenderId(),          // senderId
                request.getRecipientId(),        // recipientId
                request.getContent(),           // payload (texto ou descrição)
                Instant.now().toString(),       // timestamp (String para evitar erro de serialização)
                msgType,                        // type (Novo campo)
                request.getFileId()             // fileId (Novo campo - pode ser null)
        );

        // Envio Assíncrono para o Kafka
        return producerService.sendMessage(event)
                .map(v -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    // Endpoint 2: Listar Histórico de Mensagens
    @GetMapping("/conversations/{conversationId}/messages")
    public Flux<MessageEntity> getMessages(
            @PathVariable String conversationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Nota: Para leitura, o PDF não exigia auth estrita, mas é bom manter o padrão.
        // Aqui deixamos passar direto para facilitar seus testes no navegador.
        
        // Busca direta no Cassandra (Alta performance de leitura)
        return messageRepository.findByConversationId(conversationId);
    }
}