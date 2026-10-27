# Notification Service

Microserviço de notificações da plataforma Wallet. O serviço consome eventos do Kafka e envia e-mails HTML usando Spring Boot, Thymeleaf e SMTP.

> Estado atual: primeira versão funcional, sem endpoints HTTP de negócio. A entrada principal do serviço são os tópicos Kafka configurados em `application.yml`.

## Funcionalidades

- Consome eventos de criação de usuário e envia um e-mail de boas-vindas para o usuário.
- Consome eventos de criação de transação e envia um e-mail de confirmação para o destinatário padrão.
- Renderiza os e-mails com templates Thymeleaf:
  - `user-created.html`
  - `transaction-created.html`
- Aceita mensagens JSON sem cabeçalhos Kafka de tipo, usando conversão baseada no tipo esperado pelo listener.
- Possui tratamento de erro com duas novas tentativas, intervalo fixo de 1 segundo e registro do erro quando o processamento não é recuperado.

## Tecnologias

- Java 21
- Spring Boot 3.5.5
- Spring Kafka
- Spring Boot Mail / Jakarta Mail
- Thymeleaf
- Maven Wrapper
- Docker com Eclipse Temurin 21

## Fluxo de processamento

```text
Kafka
  ├── user-created-topic        ──> e-mail de boas-vindas ──> userMail do evento
  └── transaction-created-topic ──> e-mail de transação   ──> destinatário padrão
```

O serviço utiliza o grupo de consumidores `notification-group` por padrão.

## Tópicos e payloads

### `user-created-topic`

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "userName": "Maria Silva",
  "userMail": "maria@example.com",
  "createdAt": "2026-10-26T10:00:00"
}
```

### `transaction-created-topic`

```json
{
  "transactionId": "123e4567-e89b-12d3-a456-426614174000",
  "assetSymbol": "PETR4",
  "type": "BUY",
  "quantity": 100.0,
  "unitPrice": 37.50,
  "totalValue": 3750.00,
  "createdAt": "2026-10-26T10:30:00"
}
```

Os valores aceitos para `type` são `BUY` e `SELL`.

## Pré-requisitos

- JDK 21
- Kafka acessível pela aplicação
- Um servidor SMTP acessível pela aplicação

Para desenvolvimento local, a configuração padrão espera Kafka em `localhost:9092` e SMTP em `localhost:1025`.

## Configuração

As propriedades podem ser sobrescritas por variáveis de ambiente:

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `PORT` | `8083` | Porta da aplicação |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Endereço dos brokers Kafka |
| `USER_CREATED_TOPIC` | `user-created-topic` | Tópico de criação de usuário |
| `TRANSACTION_CREATED_TOPIC` | `transaction-created-topic` | Tópico de criação de transação |
| `MAIL_HOST` | `localhost` | Host SMTP |
| `MAIL_PORT` | `1025` | Porta SMTP |
| `MAIL_USERNAME` | vazio | Usuário SMTP, quando necessário |
| `MAIL_PASSWORD` | vazio | Senha SMTP, quando necessário |
| `MAIL_FROM` | `no-reply@wallet.com` | Remetente dos e-mails |
| `NOTIFICATION_DEFAULT_RECIPIENT` | `notifications@wallet.com` | Destinatário padrão das notificações de transação |

Exemplo:

```powershell
$env:KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
$env:MAIL_HOST = "localhost"
$env:MAIL_PORT = "1025"
$env:MAIL_FROM = "no-reply@wallet.com"
```

## Executando localmente

Na raiz do projeto:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

A aplicação fica disponível na porta `8083`. Ela não possui, no estado atual, endpoints REST para envio manual de notificações; os e-mails são disparados a partir dos eventos consumidos do Kafka.

## Executando com Docker

Construir a imagem:

```powershell
docker build -t notification-service .
```

Executar, informando os serviços externos necessários:

```powershell
docker run --rm -p 8083:8083 `
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 `
  -e MAIL_HOST=host.docker.internal `
  -e MAIL_PORT=1025 `
  notification-service
```

O `Dockerfile` compila o projeto com Maven, gera o JAR e o executa em uma imagem JRE baseada no Eclipse Temurin 21.

## Testes

Os testes cobrem:

- carregamento do contexto Spring;
- delegação dos eventos para as estratégias de e-mail;
- renderização e envio dos e-mails de boas-vindas e de transação;
- consumo de mensagens JSON usando Kafka embarcado.

Para executar:

```powershell
.\mvnw.cmd test
```

## Estrutura principal

```text
src/main/java/com/joaocastro/notification_wallet/
├── config/       # Conversão de mensagens e tratamento de erros do Kafka
├── dto/          # Eventos e tipos de transação
├── service/      # Consumidor dos tópicos Kafka
└── strategy/     # Estratégias de envio e composição dos e-mails

src/main/resources/
├── application.yml
└── templates/    # Templates HTML utilizados pelo serviço
```

O build e a aplicação executável estão configurados na raiz do repositório. O diretório `notification-service/` atualmente contém templates auxiliares, mas eles não são os templates carregados pela configuração efetiva da aplicação.

## Limitações conhecidas

- Não há API REST para disparo ou consulta de notificações.
- Não há persistência própria de notificações ou histórico de envios.
- Não há configuração de Dead Letter Topic; após as tentativas configuradas, o erro é apenas registrado.
- O serviço depende de Kafka e SMTP externos para funcionar em ambiente de execução.
