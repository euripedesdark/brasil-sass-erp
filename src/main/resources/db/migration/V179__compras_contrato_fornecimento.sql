-- Contrato de fornecimento (outline agreement) + vínculo em pedido de compra
CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_contrato (
    id                    BIGSERIAL PRIMARY KEY,
    uuid                  UUID NOT NULL UNIQUE,
    empresa_id            BIGINT NOT NULL,
    fornecedor_id         BIGINT NOT NULL,
    numero                VARCHAR(30) NOT NULL,
    tipo                  VARCHAR(20) NOT NULL DEFAULT 'QUANTIDADE',
    status                VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    vigencia_inicio       DATE NOT NULL,
    vigencia_fim          DATE NOT NULL,
    condicao_pagamento_id BIGINT,
    valor_limite          NUMERIC(15,2),
    valor_liberado        NUMERIC(15,2) NOT NULL DEFAULT 0,
    observacao            TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP,
    deleted_at            TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_com_contrato_empresa ON brasil_saas.bc_com_contrato(empresa_id);
CREATE INDEX IF NOT EXISTS idx_com_contrato_status ON brasil_saas.bc_com_contrato(empresa_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_com_contrato_numero
    ON brasil_saas.bc_com_contrato(empresa_id, numero) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_contrato_item (
    id                      BIGSERIAL PRIMARY KEY,
    uuid                    UUID NOT NULL UNIQUE,
    empresa_id              BIGINT NOT NULL,
    contrato_id             BIGINT NOT NULL REFERENCES brasil_saas.bc_com_contrato(id),
    numero_item             INTEGER NOT NULL,
    produto_id              BIGINT,
    descricao               VARCHAR(300),
    unidade                 VARCHAR(10),
    quantidade_contratada   NUMERIC(15,3) NOT NULL,
    quantidade_liberada     NUMERIC(15,3) NOT NULL DEFAULT 0,
    valor_unitario          NUMERIC(15,4) NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP,
    deleted_at              TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_com_contrato_item_contrato ON brasil_saas.bc_com_contrato_item(contrato_id);

ALTER TABLE brasil_saas.bc_com_pedido
    ADD COLUMN IF NOT EXISTS contrato_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_com_pedido_contrato
    ON brasil_saas.bc_com_pedido(contrato_id) WHERE contrato_id IS NOT NULL;
