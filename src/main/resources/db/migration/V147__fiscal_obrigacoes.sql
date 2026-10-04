-- Agenda de obrigacoes acessorias: catalogo por empresa + entregas por competencia.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_fis_obrigacao (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    nome varchar(200) NOT NULL,
    orgao varchar(60),
    periodicidade varchar(20) NOT NULL DEFAULT 'MENSAL',
    dia_vencimento integer NOT NULL DEFAULT 20,
    descricao text,
    ativa boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_fis_obrigacao_entrega (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    obrigacao_id bigint NOT NULL REFERENCES brasil_saas.bc_fis_obrigacao(id),
    competencia char(7) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'PENDENTE',
    entregue_em timestamp,
    protocolo varchar(200),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_obrigacao_entrega UNIQUE (empresa_id, obrigacao_id, competencia)
);
CREATE INDEX IF NOT EXISTS ix_obrigacao_entrega_comp ON brasil_saas.bc_fis_obrigacao_entrega (empresa_id, competencia) WHERE deleted_at IS NULL;
