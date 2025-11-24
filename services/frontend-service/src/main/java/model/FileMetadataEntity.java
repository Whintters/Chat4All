package com.chat4all.frontend.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.time.Instant;

@Data
@Table("file_metadata")
public class FileMetadataEntity {
    @PrimaryKey("file_id")
    private String fileId;

    @Column("filename")
    private String filename;

    @Column("content_type")
    private String contentType;

    @Column("size")
    private Long size;

    @Column("download_url")
    private String downloadUrl;

    // --- CAMPOS NOVOS (Requisitos do PDF) ---
    @Column("uploader_id")
    private String uploaderId;

    @Column("conversation_id")
    private String conversationId;
    // ----------------------------------------

    @Column("created_at")
    private Instant createdAt;
}