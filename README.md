# Chat4All Platform 🚀

**Chat4All** é uma plataforma de mensageria distribuída de alta performance, projetada com arquitetura de microsserviços orientada a eventos (Event-Driven). O sistema suporta envio de mensagens de texto e arquivos, roteamento inteligente para múltiplos canais (simulados), persistência poliglota e monitoramento em tempo real.

---

## 🛠️ Tecnologias e Arquitetura

### Core
* **Linguagem:** Java 17 (LTS)
* **Framework:** Spring Boot 3 (WebFlux & MVC)
* **Build Tool:** Maven (Multi-Module)

### Infraestrutura & Dados
* **Message Broker:** Apache Kafka (Desacoplamento e buffer de mensagens)
* **NoSQL Database:** Apache Cassandra (Persistência de alto throughput)
* **Object Storage:** MinIO (Armazenamento de arquivos compatível com S3)
* **Containerização:** Docker & Docker Compose

### Comunicação
* **API REST:** Endpoints HTTP padrão.
* **gRPC:** Protocolo de alta performance com Protobuf.

### Observabilidade
* **Métricas:** Spring Actuator & Micrometer.
* **Coleta:** Prometheus.
* **Visualização:** Grafana.

---

## 📋 Pré-requisitos

Para rodar este projeto, você precisa de:
1.  **Java 17** instalado e configurado no PATH.
2.  **Maven** instalado.
3.  **Docker Desktop** rodando (com WSL 2 no Windows).
4.  **cURL** ou **Postman** (para testes).
5.  **k6** (opcional, para testes de carga).

---

## 🚀 Guia de Inicialização (Zero to Hero)

Siga estes passos na ordem exata para levantar o ambiente completo.

### 1. Subir a Infraestrutura

Na raiz do projeto, inicie os containers:

```bash
docker-compose up -d
```

⏳ Aguarde: O Cassandra e o Kafka levam cerca de 60 a 90 segundos para estarem totalmente operacionais. Verifique com docker ps se o status do Cassandra é (healthy).

### 2. Configurar o Banco de Dados (Cassandra)

Execute o script abaixo para criar o Keyspace, Tabelas e Índices:

```bash
docker-compose exec cassandra cqlsh -e "
CREATE KEYSPACE IF NOT EXISTS chat4all WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
USE chat4all;

-- Tabela de Mensagens
CREATE TABLE IF NOT EXISTS messages_by_conversation (
    conversation_id text,
    sequence_number bigint,
    message_id text,
    payload text,
    status text,
    created_at timestamp,
    status_history list<text>,
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
    conversation_id text,
    created_at timestamp
);

-- Índice para busca por ID (necessário para atualização de status)
CREATE INDEX IF NOT EXISTS ON messages_by_conversation (message_id);"
```

### 3. Configurar o Object Storage (MinIO)

Crie o bucket para uploads:
1.	Acesse http://localhost:9001 (Login: minioadmin / minioadmin).
2.	Vá em Buckets > Create Bucket.
3.	Nome do bucket: chat-files.
4.	Clique em Create Bucket.

### 4. Compilar o Projeto
Gera os artefatos .jar de todos os módulos:

```bash
mvn clean install
```

### 5. Iniciar os Microsserviços

Abra 4 terminais separados e execute um comando em cada (utilizando -Dserver.address=0.0.0.0 para evitar bloqueios de firewall no Windows):

Terminal 1: API Gateway (Frontend)

```bash
java "-Dserver.address=0.0.0.0" -jar services/frontend-service/target/frontend-service-0.0.1-SNAPSHOT.jar
```

Terminal 2: Router Worker (Processador)

```bash
java "-Dserver.address=0.0.0.0" -jar services/router-worker/target/router-worker-0.0.1-SNAPSHOT.jar
```

Terminal 3: WhatsApp Mock

```bash
java "-Dserver.address=0.0.0.0" -jar services/connector-whatsapp/target/connector-whatsapp-0.0.1-SNAPSHOT.jar
```
Terminal 4: Instagram Mock

```bash
java "-Dserver.address=0.0.0.0" -jar services/connector-instagram/target/connector-instagram-0.0.1-SNAPSHOT.jar
```

---

## 📡 Interfaces e Acessos

- Serviço: Swagger UI (API Docs) - URL: http://localhost:8080/webjars/swagger-ui/index.html - Credenciais/Notas: Token: chat4all-secret-key
- Serviços: Grafana (Dashboards) - URL: http://localhost:3000 - Credenciais/Notas: User: admin / Pass: admin
- Serviços: Prometheus (Métricas) - URL: http://localhost:9090 - Credenciais/Notas: -
- Serviços: MinIO Console - URL: http://localhost:9001 - Credenciais/Notas: minioadmin / minioadmin
- Serviços: gRPC Server - URL: localhost:9091 - Credenciais/Notas: Plaintext (sem TLS)

---

## 🧪 Cenários de Teste

### 1. Upload de Arquivo (REST)

Envie um arquivo e receba o link de download e ID:

```bash
curl.exe -X POST http://localhost:8080/v1/files `
  -F "file=@teste.txt" `
  -F "uploaderId=user-01" `
  -F "conversationId=chat-demo"
```
  
### 2. Envio de Mensagem com Anexo (REST)

Use o fileId retornado no passo anterior. Se o senderId começar com wa_, será roteado para o WhatsApp Mock:

POST /v1/messages
JSON
{
  "conversationId": "chat-demo",
  "senderId": "wa_usuario_teste",
  "content": "Segue o documento anexo",
  "type": "file",
  "fileId": "COLE_O_FILE_ID_AQUI"
}

### 3. Envio via gRPC

Utilize o Postman (gRPC Request):
•	URL: localhost:9091 (Modo Plaintext).
•	Proto: Importe api/src/main/proto/message.proto.
•	Método: SendMessage.

---

## 📈 Testes de Carga e Escalabilidade

Para validar a performance, utilize o script k6 incluído na raiz:

### 1.	Subir Workers Extras: Abra novos terminais e rode o router-worker em portas alternativas:

```bash
java "-Dserver.port=8084" "-Dserver.address=0.0.0.0" -jar services/router-worker/target/router-worker-0.0.1-SNAPSHOT.jar
java "-Dserver.port=8085" "-Dserver.address=0.0.0.0" -jar services/router-worker/target/router-worker-0.0.1-SNAPSHOT.jar
```

### 2.	Rodar o Script de Carga:

```bash
k6 run load-test.js
```

### 3.	Monitorar: 

Acompanhe o throughput e o consumo de CPU de cada worker no Dashboard do Grafana.

---

## 🧹 Limpeza do Ambiente

Para encerrar todos os processos e liberar memória:

PowerShell
### 1. Matar processos Java:

```bash
taskkill /F /IM java.exe
```

### 2. Derrubar containers (Mantendo dados):

```bash
docker-compose down
```

### OU 3. Derrubar e apagar dados (Reset total):

```bash
docker-compose down -v
```

