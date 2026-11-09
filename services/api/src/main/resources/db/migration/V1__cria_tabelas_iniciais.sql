-- Documento enviado por uma empresa cliente. A coluna "versao" sustenta o lock otimista
-- (@Version) usado na troca de status.
create table documento (
    id              uuid         primary key,
    tipo            varchar(30)  not null,
    nome_arquivo    varchar(255) not null,
    content_type    varchar(100) not null,
    tamanho_bytes   bigint       not null,
    sha256          char(64)     not null,
    caminho_arquivo varchar(500) not null,
    cpf_declarado   char(11)     not null,
    nome_declarado  varchar(200) not null,
    status          varchar(20)  not null,
    versao          bigint       not null,
    criado_em       timestamptz  not null,
    atualizado_em   timestamptz  not null,
    constraint ck_documento_tipo   check (tipo in ('RG', 'CNH')),
    constraint ck_documento_status check (status in ('RECEBIDO', 'EM_ANALISE', 'CONCLUIDO')),
    constraint ck_documento_tamanho check (tamanho_bytes > 0)
);

-- Consultas operacionais por status (ex.: documentos parados em EM_ANALISE).
create index ix_documento_status on documento (status, criado_em);

-- Resultado bruto da extração (campos + confiança), guardado como JSON para auditoria.
create table analise (
    id            uuid        primary key,
    documento_id  uuid        not null references documento (id),
    campos        jsonb       not null,
    registrada_em timestamptz not null,
    constraint uq_analise_documento unique (documento_id)
);

-- Decisão de risco. A unique em documento_id é a última barreira contra decisão duplicada.
create table decisao (
    id           uuid        primary key,
    documento_id uuid        not null references documento (id),
    resultado    varchar(20) not null,
    motivos      jsonb       not null,
    decidida_em  timestamptz not null,
    constraint uq_decisao_documento unique (documento_id),
    constraint ck_decisao_resultado check (resultado in ('APROVADO', 'REVISAO_MANUAL', 'REPROVADO'))
);

-- Trilha de auditoria: o que aconteceu com cada documento e quando.
create table evento_auditoria (
    id           uuid        primary key,
    documento_id uuid        not null references documento (id),
    tipo         varchar(40) not null,
    detalhe      text        not null,
    ocorrido_em  timestamptz not null
);

create index ix_evento_auditoria_documento on evento_auditoria (documento_id, ocorrido_em);
