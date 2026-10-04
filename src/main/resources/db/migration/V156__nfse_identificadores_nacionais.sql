ALTER TABLE brasil_saas.bc_fis_nfse
    ADD COLUMN IF NOT EXISTS id_dps varchar(50),
    ADD COLUMN IF NOT EXISTS protocolo_nacional varchar(100);