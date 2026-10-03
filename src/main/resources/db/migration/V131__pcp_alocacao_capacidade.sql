-- Tabela que faltou no PR de capacidade (entidade existe, migration nao).
CREATE TABLE IF NOT EXISTS brasil_saas.bc_prod_alocacao_capacidade (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    centro_trabalho_id bigint NOT NULL,
    data date NOT NULL,
    horas numeric(12,4) NOT NULL,
    ordem_producao_id bigint,
    operacao_roteiro_id bigint,
    origem varchar(20) NOT NULL DEFAULT 'OP',
    observacao varchar(255),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_prod_aloc_cap ON brasil_saas.bc_prod_alocacao_capacidade (empresa_id, centro_trabalho_id, data) WHERE deleted_at IS NULL;
