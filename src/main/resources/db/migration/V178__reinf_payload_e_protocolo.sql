-- EFD-Reinf: enriquece bc_fis_reinf para guardar payload e protocolo do evento.
ALTER TABLE brasil_saas.bc_fis_reinf
    ADD COLUMN IF NOT EXISTS payload TEXT,
    ADD COLUMN IF NOT EXISTS protocolo VARCHAR(60),
    ADD COLUMN IF NOT EXISTS gerado_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS total_docs INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS valor_total NUMERIC(15,2) DEFAULT 0;

COMMENT ON COLUMN brasil_saas.bc_fis_reinf.payload IS 'JSON do evento R-xxxx gerado (sem assinatura/transmissão)';
COMMENT ON COLUMN brasil_saas.bc_fis_reinf.protocolo IS 'Protocolo local ou da RFB quando houver transmissão';
