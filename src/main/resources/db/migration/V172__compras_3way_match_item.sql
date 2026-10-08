-- 3-way match item a item: pedido x recebimento x NF-e.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_conferencia_fatura_item (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    conferencia_id bigint NOT NULL REFERENCES brasil_saas.bc_com_conferencia_fatura(id),
    pedido_item_id bigint REFERENCES brasil_saas.bc_com_pedido_item(id),
    recebimento_item_id bigint REFERENCES brasil_saas.bc_com_recebimento_item(id),
    nfe_item_id bigint REFERENCES brasil_saas.bc_fis_nfe_item(id),
    produto_id bigint,
    quantidade_pedida numeric(18,4) NOT NULL DEFAULT 0,
    quantidade_recebida numeric(18,4) NOT NULL DEFAULT 0,
    quantidade_faturada numeric(18,4) NOT NULL DEFAULT 0,
    valor_unitario_pedido numeric(18,4) NOT NULL DEFAULT 0,
    valor_unitario_recebido numeric(18,4) NOT NULL DEFAULT 0,
    valor_unitario_faturado numeric(18,4) NOT NULL DEFAULT 0,
    tolerancia numeric(18,4) NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'DIVERGENTE',
    divergencia text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_com_conf_item_tenant
    ON brasil_saas.bc_com_conferencia_fatura_item(empresa_id, conferencia_id);
CREATE INDEX IF NOT EXISTS ix_com_conf_item_produto
    ON brasil_saas.bc_com_conferencia_fatura_item(empresa_id, produto_id);

-- V171 cria a tabela com as colunas da conferencia detalhada.
-- CREATE TABLE IF NOT EXISTS nao acrescenta colunas a uma tabela existente.
ALTER TABLE brasil_saas.bc_com_conferencia_fatura_item
    ADD COLUMN IF NOT EXISTS pedido_item_id bigint REFERENCES brasil_saas.bc_com_pedido_item(id),
    ADD COLUMN IF NOT EXISTS recebimento_item_id bigint REFERENCES brasil_saas.bc_com_recebimento_item(id),
    ADD COLUMN IF NOT EXISTS nfe_item_id bigint REFERENCES brasil_saas.bc_fis_nfe_item(id),
    ADD COLUMN IF NOT EXISTS tolerancia numeric(18,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status varchar(20) NOT NULL DEFAULT 'DIVERGENTE';
