# Chat4All Platform - Versão Completa (Fase 3)

Plataforma de mensageria escalável com suporte a microsserviços, gRPC, upload de arquivos e integração mock com redes sociais.

## 🛠 Tecnologias

* **Java 17** (Spring Boot 3 + WebFlux)
* **Apache Kafka** (Event Backbone)
* **Apache Cassandra** (Banco Distribuído)
* **MinIO** (Object Storage S3-Compatible)
* **gRPC** (Protocol Buffers)
* **Swagger/OpenAPI** (Documentação)

## 🚀 Como Executar

### 1. Infraestrutura (Docker)

Inicie todos os serviços de base (Kafka, Cassandra, MinIO):

```bash
docker-compose up -d
```

Aguarde o Cassandra ficar "healthy" e o MinIO subir.

### 2. Configuração Inicial (Primeira Execução)

Se for a primeira vez, crie o esquema do banco:

```bash
# Execute dentro do container do Cassandra
docker exec -i chat4all-platform-cassandra-1 cqlsh -e "
CREATE KEYSPACE IF NOT EXISTS chat4all WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
USE chat4all;
CREATE TABLE IF NOT EXISTS messages_by_conversation (
    conversation_id text, sequence_number bigint, message_id text, payload text, status text, 
    created_at timestamp, status_history list<text>,
    PRIMARY KEY ((conversation_id), sequence_number)
) WITH CLUSTERING ORDER BY (sequence_number ASC);
CREATE TABLE IF NOT EXISTS file_metadata (
    file_id text PRIMARY KEY, filename text, content_type text, size bigint, 
    uploader_id text, download_url text, conversation_id text, created_at timestamp
);"
```

Certifique-se também de que o bucket chat-files existe no MinIO (http://localhost:9001). Caso não exista, crie com os comandos:

```bash
docker exec -it chat4all-platform-minio-1 mc alias set myminio http://localhost:9000 minioadmin minioadmin
>> docker exec -it chat4all-platform-minio-1 mc mb myminio/chat-files
```

### 3. Compilar e Rodar

Compile todo o projeto:

  ```bash
  mvn clean install
  ```

Abra 4 terminais e inicie os serviços:

  - API Gateway (REST/gRPC):

    ```bash
    java -jar services/frontend-service/target/frontend-service-0.0.1-SNAPSHOT.jar
    ```

  - Router Worker:

    ```bash
    java -jar services/router-worker/target/router-worker-0.0.1-SNAPSHOT.jar
    ```
    
  - WhatsApp Mock:

    ```bash
    java -jar services/connector-whatsapp/target/connector-whatsapp-0.0.1-SNAPSHOT.jar
    ```
    
  - Instagram Mock:

    ```bash
    java -jar services/connector-instagram/target/connector-instagram-0.0.1-SNAPSHOT.jar
    ```

## 📚 Documentação da API (Swagger)

Acesse a interface visual para testes: 👉 http://localhost:8080/webjars/swagger-ui/index.html 👉 clique em Authorize e use o token: chat4all-secret-key

## 🧪 Cenários de Teste

### 1. Upload de Arquivo

- POST /v1/files (Multipart)

- Retorna fileId e URL de download.

### 2. Enviar Mensagem (Com ou sem arquivo)

- POST /v1/messages

Body:

  ```JSON
  {
    "conversationId": "chat-01",
    "senderId": "wa_usuario", 
    "content": "Teste com arquivo",
    "type": "file",
    "fileId": "<ID_DO_UPLOAD>"
  }
  ```

- Obs: Se senderId começar com wa_, vai para o WhatsApp Mock. Se ig_, vai para o Instagram.

### 3. Fluxo de Status

O sistema simula automaticamente: SENT (API) -> DELIVERED (Mock recebeu) -> READ (Mock leu). Verifique o status final consultando o GET /v1/conversations/{id}/messages.
