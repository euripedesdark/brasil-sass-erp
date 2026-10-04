ALTER TABLE brasil_saas.bc_ven_devolucao
    ADD COLUMN IF NOT EXISTS decidida_por bigint,
    ADD COLUMN IF NOT EXISTS decidida_em timestamp,
    ADD COLUMN IF NOT EXISTS recebida_em timestamp;
