package com.chat4all.worker.repository;

import com.chat4all.worker.model.MessageEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface WorkerMessageRepository extends ReactiveCassandraRepository<MessageEntity, String> {
    // O Spring Data usa o índice que criamos acima para fazer essa busca funcionar
    Mono<MessageEntity> findByMessageId(String messageId);
}