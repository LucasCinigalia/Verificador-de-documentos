# ADR 0001: Monorepo e registro de decisões

- **Status:** aceita
- **Data:** 2026-11-09

## Contexto

O VeriDoc tem pelo menos dois serviços (API em Java e agente em Python), infraestrutura
(Compose, Helm, Argo CD) e documentação. Precisamos decidir como organizar o código e como
registrar as escolhas de arquitetura ao longo das 9 etapas.

## Opções consideradas

1. **Um repositório por serviço (polyrepo).** Pipelines independentes, mas mudanças que
   atravessam serviços (por exemplo, o formato do evento `documento.analisado`) viram vários
   PRs coordenados.
2. **Monorepo.** Um único repositório com `services/`, `infra/` e `docs/`. Uma mudança de
   contrato fica num único commit; o CI filtra por caminho (`paths:`) para não rodar tudo
   sempre.

## Decisão

Monorepo, usando este próprio repositório como raiz. Cada decisão relevante vira um arquivo
em `docs/adr/` no formato curto *contexto → opções → decisão → consequências*, numerado em
sequência. ADRs não são editados depois de aceitos; uma decisão nova substitui a antiga.

## Consequências

- Quem avalia o projeto encontra código, infraestrutura e o raciocínio no mesmo lugar.
- Os workflows do GitHub Actions precisarão de filtros por caminho (Etapa 2).
- O histórico de decisões mostra a evolução do projeto, não só o resultado final.
