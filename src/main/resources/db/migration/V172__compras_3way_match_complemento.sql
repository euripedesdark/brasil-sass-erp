-- Complemento do 3-way match de compras.
-- V171 cria a tabela base. Este migration completa as colunas usadas pela entidade
-- e pelo comparador item a item, mantendo a migration segura em bases já criadas.

ALTER TABLE brasil_saas.bc_com_conferencia_fatura_item
    ADD COLUMN IF NOT EXISTS pedido_item_id bigint REFERENCES brasil_saas.bc_com_pedido_item(id),
    ADD COLUMN IF NOT EXISTS recebimento_item_id bigint REFERENCES brasil_saas.bc_com_recebimento_item(id),
    ADD COLUMN IF NOT EXISTS nfe_item_id bigint REFERENCES brasil_saas.bc_fis_nfe_item(id),
    ADD COLUMN IF NOT EXISTS numero_item integer,
    ADD COLUMN IF NOT EXISTS descricao varchar(300),
    ADD COLUMN IF NOT EXISTS valor_total_pedido numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS valor_total_recebido numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS valor_total_faturado numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS conforme boolean NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS tipo_divergencia varchar(40);

CREATE INDEX IF NOT EXISTS ix_com_conf_item_pedido
    ON brasil_saas.bc_com_conferencia_fatura_item(empresa_id, pedido_item_id);

CREATE INDEX IF NOT EXISTS ix_com_conf_item_nfe
    ON brasil_saas.bc_com_conferencia_fatura_item(empresa_id, nfe_item_id);
