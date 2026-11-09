# VeriDoc: contexto do projeto

Este arquivo dá o contexto completo para desenvolver o VeriDoc. Leia tudo antes de começar e siga a seção "Como trabalhar comigo".

## Quem sou e por que este projeto existe

Sou Lucas Cinigalia Francisco, desenvolvedor backend em Florianópolis (SC).

- **Trabalho atual:** Assistente de TI na Stratos Consultoria desde jan/2026. Faço integrações entre os sistemas da empresa e os ERPs contábeis dos clientes e escrevo consultas SQL que cruzam vários sistemas, mas muito é vibe coding, então quero aprender mais técnico.
- **Formação:** Análise e Desenvolvimento de Sistemas no Senac, com conclusão prevista para jun/2027.
- **Stack que já uso:** Java/Spring (a mais forte), Python, React e SQL.
- **GitHub:** github.com/LucasCinigalia. O projeto mais completo é o LimpAção (github.com/Projeto-LimpAcao), feito com Java 21, Spring Boot, Spring Security com JWT, JPA, MySQL e WebSocket/STOMP.
- **IA:** conheço sobre LangChain, LangGraph, RAG, chunking, bancos vetoriais e evals, mas não com projetos reais. Tenho projetos menores com LLM, como o auto-fiscal-ai, que faz extração estruturada de PDFs.

**Objetivo:** me posicionar como desenvolvedor backend que integra soluções de IA a sistemas reais. Meu currículo ainda tem pouco tempo de experiência, então preciso de projetos públicos que provem, na prática, o que as vagas pedem.

## As vagas que motivaram o projeto

### VAAS: Desenvolvedor(a) Back-end Java (vaga principal)

A VAAS fica em Florianópolis, trabalha em modelo híbrido e usa agentes de IA para automatizar compliance e gestão de risco.

- **Dia a dia:** APIs em Java 21 e Spring Boot 3; modelagem de dados SQL e NoSQL com migrations, índices, concorrência e integridade; integração via REST/JSON e mensageria com contratos OpenAPI, resiliência, idempotência e observabilidade desde o primeiro dia.
- **Também pede:** testes unitários, de integração e de contrato no CI; profiling e tuning de JVM e SQL; retry e circuit breaker; SLOs e runbooks; segurança com OAuth2/OIDC e OWASP; CI/CD com GitHub Actions; GitOps com Argo CD; Kubernetes (GKE).
- **Stack:** Spring Boot 3 (Web, Data, Validation, Actuator, Security), PostgreSQL, MySQL, MongoDB, Redis, Kafka ou Pub/Sub, GCP.
- **Obrigatórios:** 3 anos de backend, sendo 2 com Java e Spring em produção. Eu ainda não tenho esse tempo, e o projeto existe para compensar com prova prática.
- **Diferenciais:** WebFlux/Reactor, Kafka avançado, Kubernetes no dia a dia, Event Sourcing/CQRS, DDD e tuning de JVM e SQL.

### Fundação CERTI: Desenvolvedor de IA Júnior (vaga secundária)

- **Dia a dia:** agentes em LangGraph ou CrewAI com tratamento de exceções e estado persistido; ferramentas em FastAPI com Pydantic; evals de alucinação e relevância (Ragas ou DeepEval) no CI; leitura de traces no Langfuse ou LangSmith; benchmarks de RAG com chunking e re-ranking.
- **Requisitos:** Python, NumPy e scikit-learn, Git, SQL e ETL básico.
- **Diferenciais:** PyTorch ou TensorFlow, APIs REST, Docker e MLflow.

### O que se repete no mercado

Analisei 8 vagas de backend Java publicadas entre ago e nov/2026 (VAAS, Sylision, DB1, BRQ, SoftExpert, Grupo Data e banco BV):

| Requisito | Vagas que pedem (de 8) | Situação atual |
| --- | --- | --- |
| Java + Spring Boot | 8 | Tenho |
| SQL relacional | 8 | Tenho MySQL; falta PostgreSQL e migrations |
| Mensageria (Kafka) | 7 | Falta |
| Kubernetes | 6 | Falta. Quero aprender |
| Testes automatizados | 5 | JUnit básico; falta integração e contrato |
| Docker | 5 | Básico |
| CI/CD | 5 | Falta |
| Cloud (AWS ou GCP) | 5 | Falta |
| Observabilidade | 4 | Falta |
| OAuth2/OIDC | 4 | Tenho JWT; falta OAuth2/OIDC |
| Redis ou NoSQL | 4 | Falta |
| IA (RAG, agentes) | 3 | É meu diferencial |

## O projeto: VeriDoc

O VeriDoc é uma plataforma de verificação de documentos. Uma empresa envia os documentos de um cliente, um agente de IA extrai os dados e a API decide entre `APROVADO`, `REVISAO_MANUAL` ou `REPROVADO`, guardando o motivo de cada decisão.

**Regra fixa:** usar apenas documentos fictícios gerados por nós. Nunca usar dados reais de pessoas.

### Arquitetura

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
                                  (baixa o arquivo do MinIO, extrai com Gemini)
```

- A API nunca chama o agente diretamente. Tudo passa pelo Kafka.
- A publicação de eventos usa o padrão outbox transacional.
- Ao receber `documento.analisado`, a API aplica as regras de risco, grava a decisão e avisa a empresa por webhook com retry.
- Observabilidade em todos os serviços: OpenTelemetry com trace do Java ao Python, Prometheus, Grafana e Jaeger.
- Plataforma: Docker Compose para desenvolvimento local, depois Kubernetes com Helm e Argo CD.

### Stack

| Parte | Tecnologia |
| --- | --- |
| API | Java 21, Spring Boot 3 (Web, Data JPA, Validation, Actuator, Security), springdoc-openapi |
| Dados | PostgreSQL + Flyway, Redis, MinIO |
| Mensageria | Kafka (modo KRaft), Spring Kafka, tópico de erro (DLT) |
| Segurança | Keycloak (OAuth2/OIDC), Spring Security como resource server, ProblemDetail (RFC 9457) |
| Resiliência | Resilience4j (retry, circuit breaker) |
| Testes | JUnit 5, Mockito, Testcontainers, Spring Cloud Contract |
| Agente de IA | Python, FastAPI, Pydantic, Gemini multimodal, LangSmith |
| Observabilidade | Micrometer, Prometheus, Grafana, OpenTelemetry, Jaeger |
| Entrega | Docker multi-stage, GitHub Actions, GHCR, kind, Helm, Argo CD; GKE opcional |

### Estrutura do repositório (monorepo)

O monorepo é este próprio repositório (`Verificador-de-documentos`); a raiz dele faz o papel da pasta `veridoc/`.

```
CLAUDE.md
README.md
docs/adr/            decisões de arquitetura (uma por arquivo)
docs/runbooks/       runbooks de incidente e SLOs
services/api/        Spring Boot, pacotes por domínio (documento, analise, decisao, auditoria)
services/agent/      Python, FastAPI
infra/compose/       docker-compose.yml para desenvolvimento local
infra/helm/          chart Helm
infra/argocd/        manifests do Argo CD
samples/             documentos fictícios para teste
.github/workflows/   pipelines de CI
```

### Etapas (fazer em ordem, uma de cada vez)

Cada etapa termina com algo que roda, com testes passando e um commit no GitHub.

1. **API base.** Endpoints para enviar documento, consultar status e consultar decisão. PostgreSQL com Flyway, contrato OpenAPI, Bean Validation, erros com ProblemDetail, lock otimista com `@Version` na troca de status. Testes unitários das regras de risco.
   - *Pronto quando:* `docker compose up` sobe API e banco, e o Swagger mostra o contrato.
2. **Testes de integração e CI.** Testcontainers com PostgreSQL real, Dockerfile multi-stage e GitHub Actions que testa e publica a imagem no GHCR.
   - *Pronto quando:* o badge do pipeline estiver verde no README.
3. **Segurança.** Keycloak no Compose, API como resource server, papéis `ANALISTA` e `ADMIN`, validação de tipo e tamanho de arquivo, segredos fora do código.
   - *Pronto quando:* existirem testes para acesso negado e para cada papel.
4. **Mensageria.** Kafka no Compose, outbox transacional, consumidor com consumer group, retry e DLT, consumo idempotente.
   - *Pronto quando:* um teste com Testcontainers provar que uma mensagem duplicada não gera decisão duplicada.
5. **Agente de IA.** Serviço FastAPI que consome `documento.enviado`, baixa o arquivo, extrai os campos com Gemini para um schema Pydantic e publica o resultado com confiança por campo. Traces no LangSmith e um conjunto pequeno de documentos fictícios com o resultado esperado.
   - *Pronto quando:* existir um script que mede o acerto campo a campo.
6. **Redis e resiliência.** Cache de status, header `Idempotency-Key` no envio, webhook com Resilience4j (retry e circuit breaker).
7. **Observabilidade.** Actuator e Micrometer com Prometheus e Grafana, logs em JSON e um trace OpenTelemetry atravessando API, Kafka e agente. Definir 2 SLOs e escrever 1 runbook.
8. **Kubernetes.** Cluster local com kind. Deployments, Services, ConfigMaps e Secrets; probes de liveness e readiness usando o Actuator; requests e limits; HPA. Empacotar tudo num chart Helm.
9. **GitOps e nuvem.** Argo CD sincronizando a partir do repositório e testes de contrato no pipeline. Opcional: deploy no GKE com os créditos gratuitos.

Extras opcionais, depois das 9 etapas: Spring WebFlux, CQRS para a tela de consulta e DDD mais explícito.

### Como apresentar

- **README:** frase do que o sistema faz, diagrama, GIF do fluxo, como rodar com um comando, prints do Grafana e do Jaeger, link para os ADRs e badge do CI.
- **Currículo:** o projeto entra na seção Projetos assim que as etapas 1 a 4 estiverem prontas.

## Como trabalhar comigo

- **Quero aprender, não só ter o código pronto.** Antes de cada etapa, explique em poucas linhas os conceitos novos, como Kafka, outbox, OAuth2 e Kubernetes.
- **Faça em passos pequenos.** Uma etapa por vez, com commits pequenos e mensagens claras. Não pule etapas sem eu pedir.
- **Explique as decisões.** Quando houver escolha de arquitetura, apresente as opções e registre a decisão em `docs/adr/`.
- **Testes fazem parte da etapa.** Escreva testes junto com o código e rode a suíte antes de dizer que terminou.
- **Convenções:** Java 21 com records onde fizer sentido, pacotes por domínio, injeção por construtor, nada de segredo no código. Use português no domínio e nos documentos e inglês no código.
- **Commits só na minha conta:** autor e committer `Lucas Cinigalia <150202188+LucasCinigalia@users.noreply.github.com>`. Nunca adicionar `Co-Authored-By`, `Claude-Session` ou qualquer outra linha de atribuição ao Claude em commits ou PRs.
- **Ao fim de cada etapa:** resuma o que foi feito, o que aprendi e o que vem a seguir, e marque a etapa como concluída neste arquivo.

## Comandos úteis

```bash
# testes da API
cd services/api && ./mvnw test

# subir API + PostgreSQL
docker compose -f infra/compose/docker-compose.yml up --build
# Swagger: http://localhost:8080/swagger-ui.html
```

## Projetos futuros (não fazer agora)

1. **Agente RAG com evals no CI (mira a vaga da CERTI).** LangGraph com checkpointer e fallback; ferramentas em FastAPI e Pydantic; benchmark de chunking com e sem re-ranking (recall@k e MRR com NumPy); Ragas ou DeepEval no GitHub Actions; traces no LangSmith ou Langfuse; MLflow; Docker Compose.
2. **ML clássico contra LLM.** Classificar textos de um dataset público com scikit-learn e comparar com um LLM em acurácia, custo e latência, registrando no MLflow.

## Status

- [x] Etapa 1: API base (concluída em 2026-11-09)
- [ ] Etapa 2: Testes de integração e CI
- [ ] Etapa 3: Segurança
- [ ] Etapa 4: Mensageria
- [ ] Etapa 5: Agente de IA
- [ ] Etapa 6: Redis e resiliência
- [ ] Etapa 7: Observabilidade
- [ ] Etapa 8: Kubernetes
- [ ] Etapa 9: GitOps e nuvem

**Próximo passo:** Etapa 2, testes de integração com Testcontainers e CI no GitHub Actions.
Pendências herdadas da Etapa 1 que entram na Etapa 2:
- teste de integração que prove o conflito de concorrência no PostgreSQL real (ADR 0003);
- teste que valide o schema (Flyway + `ddl-auto: validate`) e o mapeamento `jsonb`;
- revisar o Dockerfile multi-stage (já existe, criado para o `docker compose up` da Etapa 1).
