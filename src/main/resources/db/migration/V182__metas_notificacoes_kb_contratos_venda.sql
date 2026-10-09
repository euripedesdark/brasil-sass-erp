-- Metas comerciais
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ven_meta (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    ano INT NOT NULL,
    mes INT NOT NULL,
    vendedor_id BIGINT,
    canal VARCHAR(40),
    valor_meta NUMERIC(18,2) NOT NULL DEFAULT 0,
    valor_realizado NUMERIC(18,2) NOT NULL DEFAULT 0,
    observacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_ven_meta_emp ON brasil_saas.bc_ven_meta(empresa_id, ano, mes) WHERE deleted_at IS NULL;

-- Notificações internas
CREATE TABLE IF NOT EXISTS brasil_saas.bc_core_notificacao (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT,
    titulo VARCHAR(200) NOT NULL,
    mensagem TEXT,
    tipo VARCHAR(40) NOT NULL DEFAULT 'INFO',
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    link VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_notif_emp_user ON brasil_saas.bc_core_notificacao(empresa_id, usuario_id, lida) WHERE deleted_at IS NULL;

-- Base de conhecimento
CREATE TABLE IF NOT EXISTS brasil_saas.bc_kb_artigo (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    categoria VARCHAR(80),
    conteudo TEXT,
    tags VARCHAR(200),
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_kb_emp ON brasil_saas.bc_kb_artigo(empresa_id) WHERE deleted_at IS NULL;

-- Contratos comerciais (venda)
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ven_contrato (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    numero VARCHAR(40) NOT NULL,
    cliente_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO',
    valor NUMERIC(18,2) NOT NULL DEFAULT 0,
    inicio DATE,
    fim DATE,
    renovacao_auto BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_ven_ctr_emp ON brasil_saas.bc_ven_contrato(empresa_id, status) WHERE deleted_at IS NULL;
