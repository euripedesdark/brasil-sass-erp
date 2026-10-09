
-- Ações de cobrança (dunning) e promessas de pagamento — paridade SAP FSCM-lite
CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_cobranca_acao (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL,
    titulo_id       BIGINT NOT NULL,
    pessoa_id       BIGINT,
    nivel           INT NOT NULL DEFAULT 1,
    tipo            VARCHAR(30) NOT NULL,
    observacao      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    CONSTRAINT ck_cob_acao_nivel CHECK (nivel BETWEEN 1 AND 5),
    CONSTRAINT ck_cob_acao_tipo CHECK (tipo IN ('LEMBRETE','AVISO','NEGATIVACAO','LIGACAO','EMAIL','WHATSAPP','OUTRO'))
);
CREATE INDEX IF NOT EXISTS idx_cob_acao_empresa_titulo ON brasil_saas.bc_fin_cobranca_acao(empresa_id, titulo_id);
CREATE INDEX IF NOT EXISTS idx_cob_acao_created ON brasil_saas.bc_fin_cobranca_acao(empresa_id, created_at DESC);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_promessa_pagamento (
    id                BIGSERIAL PRIMARY KEY,
    empresa_id        BIGINT NOT NULL,
    titulo_id         BIGINT NOT NULL,
    pessoa_id         BIGINT,
    valor_prometido   NUMERIC(15,2) NOT NULL,
    data_prometida    DATE NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    observacao        VARCHAR(500),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        BIGINT,
    updated_at        TIMESTAMP,
    CONSTRAINT ck_promessa_status CHECK (status IN ('ABERTA','CUMPRIDA','QUEBRADA','CANCELADA')),
    CONSTRAINT ck_promessa_valor CHECK (valor_prometido > 0)
);
CREATE INDEX IF NOT EXISTS idx_promessa_empresa ON brasil_saas.bc_fin_promessa_pagamento(empresa_id, status);
CREATE INDEX IF NOT EXISTS idx_promessa_titulo ON brasil_saas.bc_fin_promessa_pagamento(empresa_id, titulo_id);
