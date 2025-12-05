package com.chat4all.shared.event;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageEvent {
    private String messageId;
    private String conversationId; // Partition Key para o Kafka [cite: 47]
    private String senderId;
    private String recipientId;
    private String payload;
    private String timestamp;
    private String type;   // "text" ou "file"
    private String fileId; // Opcional
}