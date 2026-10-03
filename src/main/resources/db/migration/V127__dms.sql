-- DMS: documentos com versionamento, aprovacao e retencao.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_dms_documento (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    codigo varchar(60) NOT NULL,
    titulo varchar(300) NOT NULL,
    categoria varchar(100),
    entidade_tipo varchar(60),
    entidade_id bigint,
    status varchar(20) NOT NULL DEFAULT 'RASCUNHO',
    versao_atual integer NOT NULL DEFAULT 1,
    reter_ate date,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_dms_documento UNIQUE (empresa_id, codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_dms_versao (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    documento_id bigint NOT NULL REFERENCES brasil_saas.bc_dms_documento(id),
    versao integer NOT NULL,
    arquivo_nome varchar(300),
    content_type varchar(120),
    tamanho bigint,
    hash varchar(128),
    conteudo_oid oid,
    comentario text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_dms_versao UNIQUE (documento_id, versao)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_dms_aprovacao (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    documento_id bigint NOT NULL REFERENCES brasil_saas.bc_dms_documento(id),
    versao integer NOT NULL,
    aprovador varchar(200),
    status varchar(20) NOT NULL DEFAULT 'PENDENTE',
    decidido_em timestamp,
    decidido_por bigint,
    comentario text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_dms_doc_categoria ON brasil_saas.bc_dms_documento (empresa_id, categoria) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_dms_doc_entidade ON brasil_saas.bc_dms_documento (empresa_id, entidade_tipo, entidade_id) WHERE deleted_at IS NULL;
