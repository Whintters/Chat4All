package com.chat4all.frontend.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDto {
    private String conversationId;
    private String senderId;
    private String content;
}