package com.chat4all.connector.instagram;

import com.chat4all.shared.event.MessageEvent;
import com.chat4all.shared.event.StatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InstagramConsumer {

    private static final Logger logger = LoggerFactory.getLogger(InstagramConsumer.class);
    
    private final KafkaTemplate<String, StatusUpdateEvent> kafkaTemplate;

    public InstagramConsumer(KafkaTemplate<String, StatusUpdateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "instagram-outbound", groupId = "instagram-mock-group")
    public void consume(MessageEvent event) {
        logger.info("[Instagram Mock] 📸 Direct Message Recebida: {} | User: {}", event.getPayload(), event.getSenderId());

        try {
            // Simula latência mais rápida (800ms)
            Thread.sleep(800);
            sendStatus(event, "DELIVERED");
            
            // Simula o "Visto" (1.5 segundos depois)
            Thread.sleep(1500);
            // Mapeamos o "SEEN" do Instagram para "READ" do nosso sistema interno
            sendStatus(event, "READ"); 

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Erro na simulação de delay", e);
        }
    }

    private void sendStatus(MessageEvent originalEvent, String status) {
        StatusUpdateEvent update = new StatusUpdateEvent(
                originalEvent.getConversationId(),
                originalEvent.getMessageId(),
                status,
                Instant.now().toString()
        );

        kafkaTemplate.send("status-updates", update);
        
        logger.info("[Instagram Mock] 👁️ Callback enviado: DM marcada como {}", status);
    }
}