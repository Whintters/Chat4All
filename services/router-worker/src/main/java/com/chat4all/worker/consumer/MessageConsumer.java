package com.chat4all.worker.consumer;

import com.chat4all.shared.event.MessageEvent;
import com.chat4all.worker.model.MessageEntity;
import com.chat4all.worker.repository.WorkerMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private final WorkerMessageRepository repository;

    public MessageConsumer(WorkerMessageRepository repository) {
        this.repository = repository;
    }

    // Worker consome mensagens e salva no banco [cite: 14, 20]
    @KafkaListener(topics = "messages", groupId = "router-workers-group")
    public void consume(MessageEvent event) {
        logger.info("Audit: Processando mensagem {} da conversa {}", event.getMessageId(), event.getConversationId()); // [cite: 21]

        MessageEntity entity = new MessageEntity();
        entity.setConversationId(event.getConversationId());
        // Na arquitetura completa, sequence_number vem do MetadataDB. Aqui simulamos com timestamp para o "Basic API"
        entity.setSequenceNumber(Instant.now().toEpochMilli()); 
        entity.setMessageId(event.getMessageId());
        entity.setPayload(event.getPayload());
        entity.setCreatedAt(Instant.parse(event.getTimestamp()));
        
        // Simulando envio e atualizando status para DELIVERED 
        entity.setStatus("DELIVERED");

        repository.save(entity).subscribe(); // Salva de forma reativa no Cassandra
        
        logger.info("Audit: Mensagem {} persistida com status DELIVERED", event.getMessageId());
    }
}