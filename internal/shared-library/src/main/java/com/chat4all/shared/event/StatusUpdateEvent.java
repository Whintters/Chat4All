package com.chat4all.shared.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateEvent {
    private String conversationId;
    private String messageId;
    private String status; // DELIVERED, READ
    private String timestamp;
}