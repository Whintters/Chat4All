package com.chat4all.frontend.repository;

import com.chat4all.frontend.model.FileMetadataEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends ReactiveCassandraRepository<FileMetadataEntity, String> {
}