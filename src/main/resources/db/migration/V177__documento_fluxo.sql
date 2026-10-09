
-- Fluxo de documentos (paridade SAP Document Flow)
CREATE TABLE IF NOT EXISTS brasil_saas.bc_core_documento_fluxo (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL,
    origem_tipo     VARCHAR(40) NOT NULL,
    origem_id       BIGINT NOT NULL,
    origem_numero   VARCHAR(60),
    destino_tipo    VARCHAR(40) NOT NULL,
    destino_id      BIGINT NOT NULL,
    destino_numero  VARCHAR(60),
    relacao         VARCHAR(40) NOT NULL DEFAULT 'GERA',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_doc_fluxo_origem ON brasil_saas.bc_core_documento_fluxo(empresa_id, origem_tipo, origem_id);
CREATE INDEX IF NOT EXISTS idx_doc_fluxo_destino ON brasil_saas.bc_core_documento_fluxo(empresa_id, destino_tipo, destino_id);
