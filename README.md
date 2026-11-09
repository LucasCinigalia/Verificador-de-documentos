# VeriDoc

Plataforma de verificação de documentos: uma empresa envia o documento de um cliente, um
agente de IA extrai os dados e a API decide entre `APROVADO`, `REVISAO_MANUAL` ou
`REPROVADO`, guardando o motivo de cada decisão.

> Projeto de portfólio. Usa **apenas documentos e dados fictícios**.

## Arquitetura (alvo)

```
Empresa cliente --(token Keycloak)--> API de verificação (Java)
                                        |-- PostgreSQL (análises, decisões, auditoria)
                                        |-- Redis (cache de status, idempotência)
                                        |-- MinIO (arquivos, compatível com S3)
                                        |
                              publica documento.enviado
                                        v
                                      Kafka  <-- publica documento.analisado
                                        |                       ^
                                        v                       |
                                  Agente de IA (Python) --------+
```

Hoje (Etapa 1) existem a API e o PostgreSQL; o resultado do agente é simulado por um
endpoint temporário ([ADR 0004](docs/adr/0004-entrada-temporaria-da-analise-e-armazenamento-local.md)).

## Como rodar

Pré-requisito: Docker com Compose.

```bash
cp infra/compose/.env.example infra/compose/.env   # defina DB_PASSWORD
docker compose -f infra/compose/docker-compose.yml up --build
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Contrato OpenAPI: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

### Fluxo pelo terminal

```bash
# 1. envia um documento (fictício)
curl -F arquivo=@samples/cnh-ficticia.txt -F tipo=CNH \
     -F cpfDeclarado=52998224725 -F "nomeDeclarado=Maria Ficticia da Silva" \
     http://localhost:8080/api/v1/documentos
# → 202 Accepted, status RECEBIDO, header Location com o id

# 2. simula o agente de IA (temporário, até a Etapa 4)
curl -H 'Content-Type: application/json' -d @samples/analise-cnh-ficticia.json \
     http://localhost:8080/api/v1/documentos/{id}/analise

# 3. consulta status e decisão
curl http://localhost:8080/api/v1/documentos/{id}
curl http://localhost:8080/api/v1/documentos/{id}/decisao
```

Erros seguem a [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`):

```json
{
  "type": "urn:veridoc:problema:transicao-de-status-invalida",
  "title": "Transição de status inválida",
  "status": 409,
  "detail": "Documento em CONCLUIDO não pode ir para CONCLUIDO"
}
```

## Testes

```bash
cd services/api && ./mvnw test
```

## Decisões de arquitetura

| ADR | Decisão |
| --- | --- |
| [0001](docs/adr/0001-monorepo-e-registro-de-decisoes.md) | Monorepo e registro de decisões |
| [0002](docs/adr/0002-motor-de-regras-de-risco.md) | Motor de regras de risco |
| [0003](docs/adr/0003-status-do-documento-e-lock-otimista.md) | Status do documento e lock otimista |
| [0004](docs/adr/0004-entrada-temporaria-da-analise-e-armazenamento-local.md) | Entrada temporária da análise e armazenamento local |

## Roadmap

- [x] Etapa 1: API base (Spring Boot 3, PostgreSQL + Flyway, OpenAPI, ProblemDetail, lock otimista)
- [ ] Etapa 2: Testes de integração (Testcontainers) e CI (GitHub Actions + GHCR)
- [ ] Etapa 3: Segurança (Keycloak, OAuth2/OIDC)
- [ ] Etapa 4: Mensageria (Kafka, outbox, DLT, consumo idempotente)
- [ ] Etapa 5: Agente de IA (FastAPI, Pydantic, Gemini)
- [ ] Etapa 6: Redis e resiliência
- [ ] Etapa 7: Observabilidade
- [ ] Etapa 8: Kubernetes
- [ ] Etapa 9: GitOps e nuvem
