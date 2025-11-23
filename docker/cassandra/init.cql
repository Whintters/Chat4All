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