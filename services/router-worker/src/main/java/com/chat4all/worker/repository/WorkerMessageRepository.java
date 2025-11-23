package com.chat4all.worker.repository;

import com.chat4all.worker.model.MessageEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerMessageRepository extends ReactiveCassandraRepository<MessageEntity, String> {
    // Para este worker básico, apenas o método .save() padrão (herdado) é necessário.
}