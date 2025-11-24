package com.chat4all.worker.consumer;

import com.chat4all.shared.event.MessageEvent;
import com.chat4all.worker.model.MessageEntity;
import com.chat4all.worker.repository.WorkerMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate; // Import necessário
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    
    private final WorkerMessageRepository repository;
    // Variável final que precisa ser inicializada no construtor
    private final KafkaTemplate<String, MessageEvent> kafkaTemplate; 

    // --- CONSTRUTOR ATUALIZADO (AQUI ESTAVA O ERRO) ---
    public MessageConsumer(WorkerMessageRepository repository, KafkaTemplate<String, MessageEvent> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate; // Inicialização obrigatória
    }

    @KafkaListener(topics = "messages", groupId = "router-workers-group")
    public void consume(MessageEvent event) {
        logger.info("Audit: Processando mensagem {} da conversa {}", event.getMessageId(), event.getConversationId());

        MessageEntity entity = new MessageEntity();
        entity.setConversationId(event.getConversationId());
        entity.setSequenceNumber(Instant.now().toEpochMilli());
        entity.setMessageId(event.getMessageId());
        entity.setPayload(event.getPayload());
        
        try {
            // Tenta converter a String de data de volta para Instant
            entity.setCreatedAt(Instant.parse(event.getTimestamp()));
        } catch (Exception e) {
            entity.setCreatedAt(Instant.now());
        }

        entity.setStatus("DELIVERED");

        // Salva no banco
        repository.save(entity).subscribe();
        logger.info("Audit: Mensagem {} persistida com status DELIVERED", event.getMessageId());

        // --- ROTEAMENTO PARA OS MOCKS ---
        if (event.getSenderId() != null) {
            if (event.getSenderId().startsWith("wa_")) {
                kafkaTemplate.send("whatsapp-outbound", event);
                logger.info("🔀 Roteado para WhatsApp Mock");
            } else if (event.getSenderId().startsWith("ig_")) {
                kafkaTemplate.send("instagram-outbound", event);
                logger.info("🔀 Roteado para Instagram Mock");
            }
        }
    }

    @KafkaListener(topics = "status-updates", groupId = "router-status-group")
    public void consumeStatus(com.chat4all.shared.event.StatusUpdateEvent event) {
        logger.info("🔄 Processando atualização de status: ID={} -> {}", event.getMessageId(), event.getStatus());

        repository.findByMessageId(event.getMessageId())
                .flatMap(msg -> {
                    // Atualiza o status na memória
                    msg.setStatus(event.getStatus());
                    // Salva novamente no banco (Update)
                    return repository.save(msg);
                })
                .doOnSuccess(updatedMsg -> 
                    logger.info("✅ DB ATUALIZADO REAL: Mensagem {} agora está {}", updatedMsg.getMessageId(), updatedMsg.getStatus())
                )
                .doOnError(e -> 
                    logger.error("❌ Erro ao atualizar status no banco: ", e)
                )
                .subscribe();
    }
}