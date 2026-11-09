# ADR 0005: Organização em pacotes por domínio

- **Status:** aceita
- **Data:** 2026-11-09

## Contexto

A API precisa de uma organização de pacotes que continue clara conforme as etapas adicionam
Kafka, segurança, cache e webhook. Na primeira versão os pacotes já eram por domínio, mas uma
revisão dos imports mostrou três acoplamentos que anulavam a vantagem:

1. `analise` e `decisao` dependiam um do outro (ciclo).
2. `decisao` usava `DocumentoRepository` e alterava a entidade `Documento` diretamente.
3. `compartilhado` (o `TratadorDeErros`) importava exceções de `documento`.

## Opções consideradas

1. **Pacotes por camada** (`controller/`, `service/`, `repository/`, `model/`). Familiar e
   comum em tutoriais. Uma mudança de negócio se espalha por todas as pastas, e todas as
   classes precisam ser `public` para se enxergarem.
2. **Pacotes por domínio, só como convenção.** As pastas organizam, mas nada impede que um
   domínio use o repositório de outro ou que surjam ciclos. Foi o que aconteceu.
3. **Pacotes por domínio com fronteiras verificadas.** Cada domínio expõe um serviço público
   e esconde o resto com visibilidade package-private, e um teste de arquitetura falha o build
   se alguém quebrar as regras.

## Decisão

Opção 3.

- **Módulos:** `documento`, `analise`, `decisao` (com `decisao.regras`), `auditoria` e
  `compartilhado`.
- **Sentido das dependências** (sem ciclos):

  ```
  decisao ──> analise ──> documento ──> auditoria
     │                       │
     └──────────> compartilhado <──────┘
  ```

  `decisao` também depende de `documento` e `auditoria`, e `analise` também registra
  auditoria; nenhum módulo depende de `decisao`. `analise` só usa `TipoDocumento.Campo` de
  `documento`.
- **Porta de entrada de cada domínio** é o seu `*Service`. Entre domínios trafegam records
  imutáveis (`DadosDeclarados`, `DadosExtraidos`), não entidades de outro domínio.
- **Repositórios são package-private**, assim como os construtores dos serviços que os
  recebem. O compilador impede o uso fora do domínio.
- **Exceções de negócio** estendem `compartilhado.ErroDeNegocioException`, que carrega uma
  `CategoriaErro` (sem conceitos HTTP). Só o `TratadorDeErros` converte categoria em status.
- **O resultado da análise entra por `decisao`** (`ResultadoAnaliseController`, e na Etapa 4
  o consumidor de `documento.analisado`), porque é a decisão que reage a ele; `analise` só
  guarda o resultado bruto.
- **`ArquiteturaTest` (ArchUnit)** verifica: módulos sem ciclos; `compartilhado` sem
  dependência de domínios; repositórios não públicos; `decisao.regras` sem Spring Web, JPA
  nem Spring Data.

## Consequências

- Uma mudança de negócio tende a ficar dentro de um ou dois pacotes.
- Quebrar uma fronteira falha o build, não depende de alguém notar no code review.
- Testes de serviço ficam no mesmo pacote do serviço, porque só ali o repositório é visível.
- Efeito colateral positivo: como `DocumentoService.concluirAnalise` grava o status com
  `saveAndFlush` antes de qualquer outra escrita, análises simultâneas agora são barradas pelo
  lock otimista ou pela máquina de estados (ADR 0003), e as uniques do banco ficam como
  última defesa, sem serem acionadas no teste de concorrência.
- Se um domínio crescer muito, ele pode ganhar subpacotes internos (por exemplo `api`,
  `dominio`, `infra`, no estilo hexagonal) sem mudar as regras entre módulos.
