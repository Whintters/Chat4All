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

@GrpcService // Expõe o serviço na porta gRPC (9090 por padrão)
public class GrpcChatController extends ChatServiceGrpc.ChatServiceImplBase {

    private final MessageProducerService producerService;

    public GrpcChatController(MessageProducerService producerService) {
        this.producerService = producerService;
    }

    @Override
    public void sendMessage(ChatRequest request, StreamObserver<ChatResponse> responseObserver) {
        String messageId = UUID.randomUUID().toString();
        
        // Lógica para definir default "text" se o campo type não vier preenchido
        // O método .hasType() é gerado automaticamente pelo 'optional' no .proto
        String msgType = request.hasType() ? request.getType() : "text";
        
        // Pega o fileId apenas se existir
        String fileId = request.hasFileId() ? request.getFileId() : null;

        // Cria o evento usando o construtor completo (com type e fileId)
        // Isso garante que o Worker receba os dados do arquivo mesmo vindo via gRPC
        MessageEvent event = new MessageEvent(
                messageId,
                request.getConversationId(),
                request.getSenderId(),
                request.getRecipientId(),
                request.getContent(),
                Instant.now().toString(),
                msgType,  // Passando o tipo ("file" ou "text")
                fileId    // Passando o ID do arquivo do MinIO
        );

        // Reutiliza o MESMO serviço do REST para enviar ao Kafka
        // Isso mantém a lógica centralizada e evita duplicação
        producerService.sendMessage(event).subscribe();

        // Responde imediatamente ao cliente (Ack)
        ChatResponse response = ChatResponse.newBuilder()
                .setMessageId(messageId)
                .setStatus("ACCEPTED")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}