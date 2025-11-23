package com.chat4all.worker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Data
@NoArgsConstructor
@Table("messages_by_conversation")
public class MessageEntity {

    @PrimaryKeyColumn(name = "conversation_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String conversationId;

    @PrimaryKeyColumn(name = "sequence_number", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Long sequenceNumber;

    @Column("message_id")
    private String messageId;

    @Column("payload")
    private String payload;

    @Column("status")
    private String status;

    @Column("created_at")
    private Instant createdAt;
}