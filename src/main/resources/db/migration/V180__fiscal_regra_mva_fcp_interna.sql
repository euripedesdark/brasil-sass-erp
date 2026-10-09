-- Amplia regra tributária para ST/DIFAL (MVA, FCP, alíquota interna destino)
ALTER TABLE brasil_saas.bc_fis_regra_tributaria
    ADD COLUMN IF NOT EXISTS mva NUMERIC(7,4),
    ADD COLUMN IF NOT EXISTS aliquota_fcp NUMERIC(7,4),
    ADD COLUMN IF NOT EXISTS aliquota_interna NUMERIC(7,4),
    ADD COLUMN IF NOT EXISTS reducao_base_pct NUMERIC(7,4);

CREATE INDEX IF NOT EXISTS idx_fis_regra_ncm ON brasil_saas.bc_fis_regra_tributaria(empresa_id, ncm)
    WHERE deleted_at IS NULL AND ativa = true;
