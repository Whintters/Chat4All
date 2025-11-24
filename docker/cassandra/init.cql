-- Criação do Keyspace
CREATE KEYSPACE IF NOT EXISTS chat4all 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE chat4all;

-- Criação da Tabela baseada na Tabela 2 do PDF de Arquitetura
-- Chave de Partição: conversation_id (para distribuição de carga)
-- Chave de Agrupamento: sequence_number (para ordenação causal)
CREATE TABLE IF NOT EXISTS messages_by_conversation (
    conversation_id text,
    sequence_number bigint,
    message_id text,
    payload text,
    status text,
    created_at timestamp,
    PRIMARY KEY ((conversation_id), sequence_number)
) WITH CLUSTERING ORDER BY (sequence_number ASC);

-- Tabela de Metadados de Arquivos
CREATE TABLE IF NOT EXISTS file_metadata (
    file_id text PRIMARY KEY,
    filename text,
    content_type text,
    size bigint,
    uploader_id text,
    download_url text,
    created_at timestamp
);

-- Tabela para controlar status da mensagem (Sent -> Delivered -> Read)
ALTER TABLE messages_by_conversation ADD status_history list<text>;