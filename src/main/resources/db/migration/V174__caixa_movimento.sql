-- Movimentos de caixa: SANGRIA (saída) e SUPRIMENTO (entrada)
CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_caixa_movimento (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL,
    caixa_id        BIGINT NOT NULL REFERENCES brasil_saas.bc_fin_caixa(id),
    tipo            VARCHAR(20) NOT NULL,
    valor           NUMERIC(15,2) NOT NULL,
    saldo_anterior  NUMERIC(15,2) NOT NULL,
    saldo_posterior NUMERIC(15,2) NOT NULL,
    observacao      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    CONSTRAINT ck_caixa_mov_tipo CHECK (tipo IN ('SANGRIA', 'SUPRIMENTO')),
    CONSTRAINT ck_caixa_mov_valor CHECK (valor > 0)
);
CREATE INDEX IF NOT EXISTS idx_caixa_mov_empresa_caixa ON brasil_saas.bc_fin_caixa_movimento(empresa_id, caixa_id);
CREATE INDEX IF NOT EXISTS idx_caixa_mov_created ON brasil_saas.bc_fin_caixa_movimento(empresa_id, created_at DESC);
