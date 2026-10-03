CREATE TABLE brasil_saas.bc_qual_plano_inspecao (
    id BIGSERIAL PRIMARY KEY, uuid UUID NOT NULL UNIQUE, empresa_id BIGINT NOT NULL,
    codigo VARCHAR(50) NOT NULL, descricao VARCHAR(255) NOT NULL, tipo VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP, deleted_at TIMESTAMP, CONSTRAINT uq_qual_plano_empresa_codigo UNIQUE (empresa_id, codigo)
);
CREATE TABLE brasil_saas.bc_qual_inspecao (
    id BIGSERIAL PRIMARY KEY, uuid UUID NOT NULL UNIQUE, empresa_id BIGINT NOT NULL,
    plano_id BIGINT, referencia_tipo VARCHAR(30) NOT NULL, referencia_id BIGINT,
    numero VARCHAR(50) NOT NULL, data_inspecao DATE NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'ABERTA',
    resultado VARCHAR(30), observacao VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP, deleted_at TIMESTAMP, CONSTRAINT uq_qual_inspecao_empresa_numero UNIQUE (empresa_id, numero)
);
CREATE TABLE brasil_saas.bc_qual_nao_conformidade (
    id BIGSERIAL PRIMARY KEY, uuid UUID NOT NULL UNIQUE, empresa_id BIGINT NOT NULL, inspecao_id BIGINT,
    numero VARCHAR(50) NOT NULL, severidade VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTA', descricao VARCHAR(2000) NOT NULL,
    causa_raiz VARCHAR(2000), acao_corretiva VARCHAR(2000), acao_preventiva VARCHAR(2000),
    responsavel_id BIGINT, prazo DATE, encerrada_em TIMESTAMP, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP, deleted_at TIMESTAMP, CONSTRAINT uq_qual_nc_empresa_numero UNIQUE (empresa_id, numero)
);
CREATE TABLE brasil_saas.bc_ativo_imobilizado (
    id BIGSERIAL PRIMARY KEY, uuid UUID NOT NULL UNIQUE, empresa_id BIGINT NOT NULL,
    codigo VARCHAR(50) NOT NULL, descricao VARCHAR(255) NOT NULL, classe VARCHAR(100),
    numero_serie VARCHAR(100), localizacao VARCHAR(255), responsavel_id BIGINT, data_aquisicao DATE,
    valor_aquisicao NUMERIC(15,2) NOT NULL DEFAULT 0, valor_residual NUMERIC(15,2) NOT NULL DEFAULT 0,
    valor_depreciado NUMERIC(15,2) NOT NULL DEFAULT 0, status VARCHAR(30) NOT NULL DEFAULT 'ATIVO',
    vida_util_meses INTEGER, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP,
    deleted_at TIMESTAMP, CONSTRAINT uq_ativo_empresa_codigo UNIQUE (empresa_id, codigo)
);
CREATE TABLE brasil_saas.bc_ativo_manutencao (
    id BIGSERIAL PRIMARY KEY, uuid UUID NOT NULL UNIQUE, empresa_id BIGINT NOT NULL,
    ativo_id BIGINT NOT NULL, numero VARCHAR(50) NOT NULL, tipo VARCHAR(30) NOT NULL DEFAULT 'CORRETIVA',
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTA', prioridade VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    descricao VARCHAR(2000) NOT NULL, data_programada DATE, data_conclusao DATE,
    custo NUMERIC(15,2) NOT NULL DEFAULT 0, responsavel_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP, deleted_at TIMESTAMP,
    CONSTRAINT uq_manut_empresa_numero UNIQUE (empresa_id, numero)
);
CREATE INDEX idx_qual_inspecao_empresa_status ON brasil_saas.bc_qual_inspecao (empresa_id, status);
CREATE INDEX idx_qual_nc_empresa_status ON brasil_saas.bc_qual_nao_conformidade (empresa_id, status);
CREATE INDEX idx_ativo_empresa_status ON brasil_saas.bc_ativo_imobilizado (empresa_id, status);
CREATE INDEX idx_manut_empresa_status ON brasil_saas.bc_ativo_manutencao (empresa_id, status);
