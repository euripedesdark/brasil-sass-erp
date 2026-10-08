
CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_rescisao (
    id                BIGSERIAL PRIMARY KEY,
    empresa_id        BIGINT NOT NULL,
    funcionario_id    BIGINT NOT NULL,
    data_desligamento DATE NOT NULL,
    motivo            VARCHAR(40) NOT NULL,
    meses_trabalhados INT,
    saldo_salario     NUMERIC(15,2) NOT NULL DEFAULT 0,
    decimo_terceiro   NUMERIC(15,2) NOT NULL DEFAULT 0,
    ferias            NUMERIC(15,2) NOT NULL DEFAULT 0,
    terco_ferias      NUMERIC(15,2) NOT NULL DEFAULT 0,
    aviso_previo      NUMERIC(15,2) NOT NULL DEFAULT 0,
    multa_40          NUMERIC(15,2) NOT NULL DEFAULT 0,
    total             NUMERIC(15,2) NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'EFETIVADA',
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        BIGINT,
    updated_at        TIMESTAMP,
    updated_by        BIGINT,
    deleted_at        TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rh_rescisao_empresa ON brasil_saas.bc_rh_rescisao(empresa_id);
CREATE INDEX IF NOT EXISTS idx_rh_rescisao_func ON brasil_saas.bc_rh_rescisao(empresa_id, funcionario_id);
