# ADR 0002: Motor de regras de risco

- **Status:** aceita
- **Data:** 2026-11-09

## Contexto

Depois que o agente extrai os campos de um documento, a API precisa decidir entre
`APROVADO`, `REVISAO_MANUAL` e `REPROVADO` e explicar o porquê. As regras vão mudar com
frequência (novos tipos de documento, limites diferentes) e precisam ser fáceis de testar.

## Opções consideradas

1. **Um método grande com `if/else`.** Rápido de escrever, difícil de testar isoladamente;
   a primeira regra que falha "esconde" as outras.
2. **Motor de regras externo (Drools, Easy Rules).** Poderoso, mas adiciona uma linguagem e
   uma dependência pesada para meia dúzia de regras.
3. **Interface `RegraRisco` + um motor que combina os resultados.** Cada regra é uma classe
   pequena, sem estado, testada sozinha. O Spring injeta todas as implementações numa
   `List<RegraRisco>`.

## Decisão

Opção 3. Regras do Etapa 1:

| Código | Resultado | Quando |
| --- | --- | --- |
| `CAMPO_AUSENTE` | REVISAO_MANUAL | campo obrigatório do tipo não foi extraído |
| `BAIXA_CONFIANCA` | REVISAO_MANUAL | campo obrigatório com confiança < `veridoc.regras.confianca-minima` (0,80) |
| `NOME_DIVERGENTE` | REVISAO_MANUAL | nome extraído ≠ declarado (ignorando acento, caixa e espaços) |
| `DATA_INVALIDA` | REVISAO_MANUAL | data ilegível ou nascimento no futuro |
| `CPF_INVALIDO` | REPROVADO | dígitos verificadores errados |
| `CPF_DIVERGENTE` | REPROVADO | CPF extraído ≠ declarado |
| `DOCUMENTO_VENCIDO` | REPROVADO | validade anterior a hoje |
| `MENOR_DE_IDADE` | REPROVADO | idade < `veridoc.regras.idade-minima` (18) |

Combinação: **vence o resultado mais grave** (REPROVADO > REVISAO_MANUAL > APROVADO), mas
**todos os motivos são guardados**. Sem nenhum motivo, a decisão é `APROVADO` com o motivo
`SEM_RESSALVAS`, para que toda decisão tenha uma explicação registrada.

A data de "hoje" entra no `ContextoAvaliacao` (vinda de um `Clock`), e não por
`LocalDate.now()` dentro da regra, para os testes serem determinísticos.

## Consequências

- Nova regra = nova classe com `@Component` + teste; o motor não muda.
- Os códigos de motivo viram parte do contrato com as empresas clientes: renomear um código
  é uma mudança incompatível.
- A ordem de execução das regras não importa, porque o resultado só depende do conjunto de
  motivos.
