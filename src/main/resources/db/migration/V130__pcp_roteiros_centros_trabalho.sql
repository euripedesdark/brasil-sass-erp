CREATE TABLE IF NOT EXISTS brasil_saas.bc_prod_centro_trabalho (
    id BIGSERIAL PRIMARY KEY, empresa_id BIGINT NOT NULL, codigo VARCHAR(60) NOT NULL,
    nome VARCHAR(160) NOT NULL, capacidade_horas_dia NUMERIC(10,2) NOT NULL DEFAULT 8,
    ativo BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at TIMESTAMP NULL,
    CONSTRAINT uk_prod_centro_trabalho_empresa_codigo UNIQUE (empresa_id, codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_prod_roteiro (
    id BIGSERIAL PRIMARY KEY, empresa_id BIGINT NOT NULL, produto_id BIGINT NOT NULL,
    codigo VARCHAR(60) NOT NULL, nome VARCHAR(160) NOT NULL, versao INTEGER NOT NULL DEFAULT 1,
    vigencia_inicio DATE NULL, vigencia_fim DATE NULL, ativo BOOLEAN NOT NULL DEFAULT TRUE,
    observacao TEXT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at TIMESTAMP NULL,
    CONSTRAINT uk_prod_roteiro_empresa_produto_codigo_versao UNIQUE (empresa_id, produto_id, codigo, versao)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_prod_roteiro_operacao (
    id BIGSERIAL PRIMARY KEY, empresa_id BIGINT NOT NULL, roteiro_id BIGINT NOT NULL REFERENCES brasil_saas.bc_prod_roteiro(id),
    sequencia INTEGER NOT NULL, codigo VARCHAR(60) NOT NULL, nome VARCHAR(160) NOT NULL,
    centro_trabalho_id BIGINT NULL REFERENCES brasil_saas.bc_prod_centro_trabalho(id),
    setup_minutos NUMERIC(12,3) NOT NULL DEFAULT 0, maquina_minutos NUMERIC(12,3) NOT NULL DEFAULT 0,
    homem_minutos NUMERIC(12,3) NOT NULL DEFAULT 0, instrucoes TEXT NULL, ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);
CREATE INDEX IF NOT EXISTS idx_prod_centro_trabalho_empresa_ativo ON brasil_saas.bc_prod_centro_trabalho (empresa_id, ativo);
CREATE INDEX IF NOT EXISTS idx_prod_roteiro_empresa_produto ON brasil_saas.bc_prod_roteiro (empresa_id, produto_id, ativo);
CREATE INDEX IF NOT EXISTS idx_prod_roteiro_operacao_roteiro ON brasil_saas.bc_prod_roteiro_operacao (empresa_id, roteiro_id, sequencia);
