-- Portais de autoatendimento (cliente, fornecedor, funcionario) via token.
-- Leitura escopada a propria pessoa; sem login, sem JWT, com expiracao.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ptl_acesso (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    tipo varchar(20) NOT NULL,
    pessoa_id bigint NOT NULL,
    token varchar(64) NOT NULL,
    expira_em timestamp NOT NULL,
    ativo boolean NOT NULL DEFAULT true,
    ultimo_uso_em timestamp,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_ptl_acesso_token UNIQUE (token)
);
CREATE INDEX IF NOT EXISTS ix_ptl_acesso_pessoa ON brasil_saas.bc_ptl_acesso (empresa_id, pessoa_id) WHERE deleted_at IS NULL;
