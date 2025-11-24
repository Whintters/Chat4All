# Chat4All Platform - API Básica

Este projeto implementa a primeira versão funcional do sistema Chat4All, focando em uma arquitetura orientada a eventos com alta escalabilidade. A solução utiliza **Spring Boot**, **Apache Kafka** para mensageria assíncrona e **Cassandra** para persistência de alto throughput.

## 🚀 Como Executar

### Pré-requisitos
* Java 17+
* Maven 3.8+
* Docker e Docker Compose

### Passo 1: Inicializar Infraestrutura
Utilize o script de inicialização automática para subir o Kafka, Zookeeper e Cassandra (já com as tabelas criadas).

```bash
docker-compose up -d