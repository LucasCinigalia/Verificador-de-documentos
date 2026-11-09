# ADR 0004: Entrada temporária da análise e armazenamento local

- **Status:** aceita (provisória: substituída nas Etapas 4 e 5)
- **Data:** 2026-11-09

## Contexto

Na arquitetura final, o resultado da extração chega pelo tópico `documento.analisado` (Kafka)
e os arquivos ficam no MinIO. Na Etapa 1 não existem Kafka, MinIO nem agente, mas o fluxo
completo (enviar → analisar → decidir → consultar) precisa ser demonstrável pelo Swagger.

## Opções consideradas

1. **Só enviar e consultar.** Nenhuma decisão é tomada; as regras ficam testadas, mas o fluxo
   não roda de ponta a ponta.
2. **Decidir com dados falsos ao enviar.** Mistura responsabilidades e esconde o fato de que a
   análise é assíncrona.
3. **Endpoint `POST /api/v1/documentos/{id}/analise` que recebe o mesmo payload que o agente
   vai publicar.** O serviço chamado (`DecisaoService.registrarAnalise`) é o mesmo que o
   consumidor Kafka vai chamar.

Para os arquivos: gravar em disco local atrás da interface `ArmazenamentoArquivos`, ou subir o
MinIO já agora.

## Decisão

Opção 3, marcada como temporária no Swagger (tag "Simulação do agente (temporário)"), e
armazenamento em disco local atrás de `ArmazenamentoArquivos`. O arquivo é salvo com o id do
documento como nome (nunca o nome enviado pelo cliente) e o SHA-256 é calculado durante a
gravação.

## Consequências

- O contrato do payload de análise (`DadosExtraidos`, com valor e confiança por campo) já fica
  definido e será o schema Pydantic do agente na Etapa 5.
- Na Etapa 4 o endpoint é removido (ou restrito a `ADMIN`) e entra o consumidor Kafka.
- Trocar o disco pelo MinIO é uma nova implementação de `ArmazenamentoArquivos`; o resto da
  API não muda.
- Se o banco falhar depois de gravar o arquivo, sobra um arquivo órfão. Aceitável agora; o
  outbox da Etapa 4 e uma limpeza periódica resolvem.
