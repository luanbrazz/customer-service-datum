# Desafio Técnico — Desenvolvedor Java Pleno

Microsserviço de **gerenciamento de clientes**, com integração HTTP a um serviço
externo de **score** e notificação assíncrona via **RabbitMQ**, desenvolvido
para o desafio técnico da vaga de Desenvolvedor(a) Java Pleno (Datum).

O repositório contém três projetos Maven independentes:

| Projeto | Descrição | Porta |
|---|---|---|
| `customer-service` | Microsserviço principal (o entregável do desafio) | `8080` |
| `score-mock-service` | Simulador do serviço externo de score (`GET /scores/{cpf}`) | `8081` |
| `notification-service` | Consome eventos de cliente criado via RabbitMQ (bônus) | — (sem API REST) |

---

## 1. Requisitos para execução

- Java 17+ (JDK)
- Maven 3.8+ (ou use o `mvnw`/`mvnw.cmd` incluso em cada projeto)
- Docker e Docker Compose (recomendado — sobe tudo com um comando)

---

## 2. Como iniciar a aplicação

### Opção A — Docker Compose (recomendado, sobe tudo já conectado)

Na raiz do repositório:

```bash
docker compose up --build
```

Isso sobe:
- **PostgreSQL** na porta `5432`
- **RabbitMQ** nas portas `5672` (AMQP) e `15672` (painel web, login `guest`/`guest`)
- **score-mock-service** na porta `8081`
- **notification-service** (consumidor de fila, sem porta HTTP exposta)
- **customer-service** na porta `8080`, já conectado a todos os anteriores

Para derrubar tudo (incluindo dados do banco):
```bash
docker compose down -v
```

### Opção B — Maven local (quatro terminais, banco H2 em memória)

O RabbitMQ precisa estar de pé mesmo no modo local — sobe só ele via Docker:
```bash
docker compose up -d rabbitmq
```

```bash
# Terminal 1 — simulador do servico de score (porta 8081)
cd score-mock-service && mvnw spring-boot:run

# Terminal 2 — consumidor de notificacoes
cd notification-service && mvnw spring-boot:run

# Terminal 3 — microsservico de clientes (porta 8080), usa H2 por padrao
cd customer-service && mvnw spring-boot:run
```

Nesse modo, o `customer-service` sobe com o profile `test` ativo por padrão
(banco **H2 em memória**, zero configuração extra). O console do H2 fica em
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:customerdb`, usuário
`sa`, senha em branco).

Para rodar contra Postgres localmente (fora do Docker), ative o profile `prod`:
```bash
SPRING_PROFILES_ACTIVE=prod mvnw spring-boot:run
```

---

## 3. Como executar os testes

```bash
cd customer-service
mvnw clean test
```

A suíte cobre:
- **Regras de negócio** (`CustomerServiceTest`, unitário com Mockito): criação,
  CPF duplicado, cliente inexistente, roteamento entre JPA e JdbcTemplate.
- **Integração com o serviço externo** (`ScoreClientTest`): sucesso, erro 500
  (indisponibilidade) e timeout, simulados com Mockito — sem depender do
  `score-mock-service` estar no ar.
- **Ponta a ponta das APIs** (`CustomerControllerIntegrationTest`, com
  `MockMvc` + banco H2 isolado): fluxo CRUD completo, autenticação e
  autorização (401/403), validação (400), CPF duplicado (409), consulta de
  score.

---

## 4. Endpoints disponíveis

Todas as rotas exigem **Basic Authentication**. Perfis:

| Usuário | Senha | Perfil | Pode fazer |
|---|---|---|---|
| `user` | `user123` | `USER` | Apenas consultas (GET) |
| `admin` | `admin123` | `ADMIN` | Consultas + criar/alterar/excluir |

| Método | Endpoint | Perfil | Descrição |
|---|---|---|---|
| `POST` | `/customers` | ADMIN | Cria um cliente (publica evento `customer.created` no RabbitMQ) |
| `PUT` | `/customers/{id}` | ADMIN | Atualiza um cliente |
| `DELETE` | `/customers/{id}` | ADMIN | Exclui um cliente |
| `GET` | `/customers/{id}` | USER, ADMIN | Consulta cliente por id |
| `GET` | `/customers` | USER, ADMIN | Lista todos os clientes |
| `GET` | `/customers?status=ACTIVE` | USER, ADMIN | Lista clientes por status (via `JdbcTemplate`) |
| `GET` | `/customers/search?name=joao` | USER, ADMIN | Busca por nome, parcial e case-insensitive (via **Native Query**) |
| `GET` | `/customers/{id}/score` | USER, ADMIN | Consulta o score do cliente no serviço externo |

### Respostas de erro por situação

| Situação | Status HTTP |
|---|---|
| Dado obrigatório ausente/inválido (ex: CPF com dígito verificador errado) | `400 Bad Request` |
| Não autenticado | `401 Unauthorized` |
| Autenticado, mas sem permissão | `403 Forbidden` |
| Cliente inexistente | `404 Not Found` |
| CPF já cadastrado | `409 Conflict` |
| Resposta inesperada do serviço de score | `502 Bad Gateway` |
| Serviço de score indisponível / circuit breaker aberto | `503 Service Unavailable` |
| Timeout na chamada ao serviço de score | `504 Gateway Timeout` |

---

## 5. Configurações necessárias

O `customer-service` usa **Spring Profiles** para alternar de banco sem
alterar código:

- **`test`** (padrão): H2 em memória — zero configuração, é o que roda se
  nenhuma variável for definida.
- **`prod`**: PostgreSQL — ativado via `SPRING_PROFILES_ACTIVE=prod`, usado
  automaticamente pelo `docker-compose.yml`.

Configuração externalizada via `application.yaml` + variáveis de ambiente:

```yaml
app:
  security:
    users:
      - username: user
        password: ${APP_USER_PASSWORD:user123}
        role: USER
      - username: admin
        password: ${APP_ADMIN_PASSWORD:admin123}
        role: ADMIN
  score-service:
    base-url: ${SCORE_SERVICE_BASE_URL:http://localhost:8081}
    connect-timeout-ms: ${SCORE_SERVICE_CONNECT_TIMEOUT_MS:2000}
    read-timeout-ms: ${SCORE_SERVICE_READ_TIMEOUT_MS:3000}

spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

Credenciais do Postgres (perfil `prod`) e do RabbitMQ também são
externalizadas por variável de ambiente — nunca fixas no código.

O comportamento de resiliência (circuit breaker) na chamada ao serviço de
score é configurável na seção `resilience4j` do mesmo arquivo.

---

## 6. Como executar ou simular o serviço externo de score

O serviço externo é simulado pelo módulo `score-mock-service`, expondo
`GET /scores/{cpf}` — a mesma assinatura do serviço real descrito no desafio.

```bash
cd score-mock-service
mvnw spring-boot:run
```

Comportamento determinístico baseado no CPF consultado, para facilitar testar
os tratamentos de erro exigidos no desafio:

| CPF de teste (válido) | Comportamento simulado |
|---|---|
| `60385977000` (termina em `000`) | Erro `500` → customer-service devolve `503 Service Unavailable` |
| `13650249111` (termina em `111`) | Demora de 6s → customer-service devolve `504 Gateway Timeout` (timeout configurado em 3s) |
| `91547887222` (termina em `222`) | Corpo de resposta incompleto → customer-service devolve `502 Bad Gateway` |
| Qualquer outro CPF válido | Score calculado deterministicamente, `200 OK` |

---

## 7. Exemplos de utilização da API

```bash
# Criar cliente (ADMIN) - dispara notificacao assincrona via RabbitMQ
curl -u admin:admin123 -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"Joao da Silva","cpf":"11144477735","email":"joao@email.com"}'

# Consultar cliente por id (USER)
curl -u user:user123 http://localhost:8080/customers/1

# Listar clientes ativos (usa JdbcTemplate)
curl -u user:user123 "http://localhost:8080/customers?status=ACTIVE"

# Buscar por nome (usa Native Query)
curl -u user:user123 "http://localhost:8080/customers/search?name=joao"

# Consultar score do cliente
curl -u user:user123 http://localhost:8080/customers/1/score

# Atualizar cliente (ADMIN)
curl -u admin:admin123 -X PUT http://localhost:8080/customers/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Joao Atualizado","cpf":"11144477735","email":"joao.novo@email.com","status":"ACTIVE"}'

# Excluir cliente (ADMIN)
curl -u admin:admin123 -X DELETE http://localhost:8080/customers/1

# Sem autenticacao -> 401
curl -i http://localhost:8080/customers
```

Também pode ser testado via Postman/Insomnia, usando Basic Auth com as
credenciais da seção 4. Após criar um cliente, o log do `notification-service`
mostra a notificação processada, e a fila `customer.created.queue` pode ser
inspecionada visualmente em `http://localhost:15672`.

---

## 8. Decisões técnicas

- **Java 17, Spring Boot 3.3, Maven** conforme solicitado.
- **Persistência**: H2 em memória (profile `test`, padrão) ou PostgreSQL
  (profile `prod`, via Docker) + Spring Data JPA para o CRUD; uma **native
  query** para busca por nome; consulta com **`JdbcTemplate`** para filtro
  por status.
- **Segurança**: Spring Security com **Basic Authentication**, perfis `USER`
  (consulta) e `ADMIN` (CRUD completo).
- **Validação**: Bean Validation (`@NotBlank`, `@Email`, `@CPF` com dígito
  verificador real) no DTO de entrada.
- **Integração com o serviço externo**: `RestTemplate` com timeouts
  configuráveis + **circuit breaker (Resilience4j)**, evitando que uma falha
  do serviço de score afete a disponibilidade do `customer-service`.
- **Tratamento de erros**: centralizado em `GlobalExceptionHandler`
  (`@RestControllerAdvice`), com status HTTP e formato JSON consistentes.
- **Mensageria assíncrona**: ao criar um cliente, o `customer-service` publica
  um evento `customer.created` num