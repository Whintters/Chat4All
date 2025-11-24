package com.chat4all.frontend.controller;

import com.chat4all.api.ChatRequest;
import com.chat4all.api.ChatResponse;
import com.chat4all.api.ChatServiceGrpc;
import com.chat4all.frontend.service.MessageProducerService;
import com.chat4all.shared.event.MessageEvent;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.UUID;

@GrpcService // Anotação mágica que expõe isso na porta 9090 (padrão gRPC)
public class GrpcChatController extends ChatServiceGrpc.ChatServiceImplBase {

    private final MessageProducerService producerService;

    public GrpcChatController(MessageProducerService producerService) {
        this.producerService = producerService;
    }

    @Override
    public void sendMessage(ChatRequest request, StreamObserver<ChatResponse> responseObserver) {
        // 1. Gera ID e Timestamp
        String messageId = UUID.randomUUID().toString();
        
        // 2. Cria o evento (Reutilizando seu DTO compartilhado)
        MessageEvent event = new MessageEvent(
                messageId,
                request.getConversationId(),
                request.getSenderId(),
                request.getContent(),
                Instant.now().toString()
        );

        // 3. Envia para o Kafka (Assíncrono)
        producerService.sendMessage(event).subscribe();

        // 4. Responde para o cliente gRPC imediatamente (Ack)
        ChatResponse response = ChatResponse.newBuilder()
                .setMessageId(messageId)
                .setStatus("ACCEPTED")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}