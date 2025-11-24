package com.chat4all.connector.whatsapp;

import com.chat4all.shared.event.MessageEvent;
import com.chat4all.shared.event.StatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class WhatsappConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappConsumer.class);
    
    // Template para enviar o callback de volta ao sistema
    private final KafkaTemplate<String, StatusUpdateEvent> kafkaTemplate;

    // Injeção de dependência via construtor
    public WhatsappConsumer(KafkaTemplate<String, StatusUpdateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "whatsapp-outbound", groupId = "whatsapp-mock-group")
    public void consume(MessageEvent event) {
        logger.info("[WhatsApp Mock] 📩 Recebido: {} | Destino: {}", event.getPayload(), event.getSenderId());

        try {
            // 1. Simula latência de rede (1 segundo)
            Thread.sleep(1000);
            sendStatus(event, "DELIVERED");
            
            // 2. Simula tempo de leitura do usuário (mais 2 segundos)
            Thread.sleep(2000);
            sendStatus(event, "READ");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Erro na simulação de delay", e);
        }
    }

    // Método auxiliar para montar e enviar o evento de status
    private void sendStatus(MessageEvent originalEvent, String status) {
        StatusUpdateEvent update = new StatusUpdateEvent(
                originalEvent.getConversationId(),
                originalEvent.getMessageId(),
                status,
                Instant.now().toString()
        );

        // Envia para o tópico 'status-updates' que o Router Worker vai escutar
        kafkaTemplate.send("status-updates", update);
        
        logger.info("[WhatsApp Mock] 🔄 Callback enviado: Status atualizado para {}", status);
    }
}