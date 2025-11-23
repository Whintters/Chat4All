package com.chat4all.frontend.service;

import com.chat4all.shared.event.MessageEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessageProducerService {

    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;

    public MessageProducerService(KafkaTemplate<String, MessageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> sendMessage(MessageEvent event) {
        // Kafka Topic: messages. Key: conversationId (garante ordem na partição) [cite: 13, 47]
        return Mono.fromFuture(kafkaTemplate.send("messages", event.getConversationId(), event))
                .then();
    }
}