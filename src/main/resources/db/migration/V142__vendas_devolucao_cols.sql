ALTER TABLE brasil_saas.bc_ven_devolucao_item
    ADD COLUMN IF NOT EXISTS qtd_recebida numeric(15,3) NOT NULL DEFAULT 0;
