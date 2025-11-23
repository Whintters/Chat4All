# Chat4All Platform - Semana 3-4

Este projeto implementa a arquitetura base do Chat4All, focando em microsserviços, mensageria assíncrona com **Apache Kafka** e persistência distribuída com **Apache Cassandra**.

## 📋 Pré-requisitos

* **Java 17** (Obrigatório ser versão LTS 17 ou 21).
* **Maven 3.8+**.
* **Docker & Docker Compose** (Com WSL 2 atualizado no Windows).

---

## 🚀 Como Executar o Projeto

### 1. Subir a Infraestrutura

Na raiz do projeto, inicie os containers do Kafka, Zookeeper e Cassandra:

```bash
docker-compose up -d
```

Aguarde: O Cassandra demora cerca de 60 a 90 segundos para estar pronto para conexões. Verifique com docker ps se o status está (healthy).

### 2. Configuração Inicial do Banco de Dados (Cassandra)

Como o Cassandra inicia vazio, é necessário criar o Keyspace e a Tabela manualmente na primeira execução.

#### 2.1 Acesse o terminal do container:

```bash
docker exec -it chat4all-platform-cassandra-1 cqlsh
```

#### 2.2 Cole os comandos abaixo no prompt cqlsh>:

-- Criar o Keyspace:

```bash
CREATE KEYSPACE chat4all WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
```

-- Usar o Keyspace:

```bash
USE chat4all;
```

-- Criar a Tabela:

```bash
CREATE TABLE messages_by_conversation (
    conversation_id text,
    sequence_number bigint,
    message_id text,
    payload text,
    status text,
    created_at timestamp,
    PRIMARY KEY ((conversation_id), sequence_number)
) WITH CLUSTERING ORDER BY (sequence_number ASC);

-- Sair
exit
```

### 3. Compilar o Projeto

Compile os microsserviços e a biblioteca compartilhada:

```bash
mvn clean install
```

### 4. Iniciar os Microsserviços

Serão necessários dois terminais abertos simultaneamente.

#### 4.1 Terminal 1 - Frontend Service (API):

```bash
java -jar services/frontend-service/target/frontend-service-0.0.1-SNAPSHOT.jar
```

#### 4.2 Terminal 2 - Router Worker (Consumidor):

```bash
java -jar services/router-worker/target/router-worker-0.0.1-SNAPSHOT.jar
```

### 6. 📡 Como Testar

#### 6.1 Enviar Mensagem (Produtor):

Utilize o Thunder Client, Postman ou Curl:

- Método: POST

- URL: http://localhost:8080/v1/messages

- Header: Authorization: Bearer chat4all-secret-key

- Body (JSON):

  ```JSOn
  {
    "conversationId": "conversa-demo",
    "senderId": "usuario-01",
    "content": "Teste de envio via Kafka e Cassandra!"
  }
  ```

- Retorno Esperado: 202 Accepted ou 200 OK.

#### 6.2 Verificar Recebimento (Consumidor)

- Verifique o log do Terminal 2 (Worker). Deve aparecer: Audit: Mensagem ... persistida com status DELIVERED

- Consulte a API de leitura:

  Método: GET

  URL: http://localhost:8080/v1/conversations/conversa-demo/messages

  Header: Authorization: Bearer chat4all-secret-key

  Retorno: JSON contendo a mensagem salva com status DELIVERED.

### 7. 🛠 Tecnologias Utilizadas

Spring Boot 3.1.5 (WebFlux)

Apache Kafka (Mensageria)

Apache Cassandra (NoSQL Database)

Maven Multi-Module
