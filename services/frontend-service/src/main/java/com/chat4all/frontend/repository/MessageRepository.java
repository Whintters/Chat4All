package com.chat4all.frontend.repository;

import com.chat4all.frontend.model.MessageEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends ReactiveCassandraRepository<MessageEntity, String> {
    
    // O Spring Data cria automaticamente a query baseada no nome do método.
    // Ele busca todos os registros onde a partition key "conversation_id" é igual ao parâmetro.
    Flux<MessageEntity> findByConversationId(String conversationId);
}