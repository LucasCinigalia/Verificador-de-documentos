# ADR 0003: Status do documento e lock otimista

- **Status:** aceita
- **Data:** 2026-11-09

## Contexto

O status de um documento (`RECEBIDO → EM_ANALISE → CONCLUIDO`) vai ser alterado por
processos diferentes: a requisição HTTP, o publicador do outbox e o consumidor Kafka
(que pode receber a mesma mensagem duas vezes). Duas escritas simultâneas não podem gerar
duas decisões nem fazer o status "voltar".

## Opções consideradas

1. **Sem controle.** A última escrita vence (*lost update*). Inaceitável para auditoria.
2. **Lock pessimista (`SELECT ... FOR UPDATE`).** A segunda transação espera a primeira
   terminar. Seguro, mas segura conexões e linhas bloqueadas; com Kafka, um consumidor lento
   trava os outros.
3. **Lock otimista (`@Version`).** Ninguém espera: cada `UPDATE` leva
   `WHERE id = ? AND versao = ?`. Se outra transação já incrementou a versão, nenhuma linha é
   atualizada e o Hibernate lança `OptimisticLockException`. Bom quando conflitos são raros.

## Decisão

Lock otimista com `@Version` na entidade `Documento`, mais duas defesas:

- **Máquina de estados no domínio:** `StatusDocumento.podeIrPara` recusa transições
  inválidas (por exemplo, `CONCLUIDO → CONCLUIDO`) antes de qualquer escrita.
- **Unique no banco:** `decisao.documento_id` e `analise.documento_id` são únicos, então
  mesmo um bug no código não grava duas decisões.

A API traduz o conflito em `409 Conflict` com ProblemDetail
(`urn:veridoc:problema:conflito-de-concorrencia`). Enquanto não há Kafka,
`RECEBIDO → CONCLUIDO` direto é permitido (ver ADR 0004).

## Consequências

- Quem recebe 409 deve consultar o status antes de tentar de novo; não há retry automático.
- O teste que prova o conflito real (duas transações no mesmo PostgreSQL) depende de banco de
  verdade e entra na Etapa 2 com Testcontainers. Na Etapa 1 há testes da máquina de estados e
  do mapeamento do erro para HTTP.
- Na Etapa 4, a idempotência do consumidor Kafka se apoia nessas mesmas três camadas.
