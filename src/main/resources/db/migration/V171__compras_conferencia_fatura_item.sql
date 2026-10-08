-- Conferencia 3-way de compra.
--
-- A tabela da conferencia nunca teve migration: a entidade existia, o SQL nao.
-- Com ddl-auto: validate isso derruba o boot. Aqui entram as duas tabelas.
-- Uma linha por item conferido: pedido x recebimento x NF-e de entrada.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_conferencia_fatura (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    recebimento_id bigint,
    titulo_id bigint,
    nfe_id bigint,
    valor_pedido numeric(15,2) NOT NULL DEFAULT 0,
    valor_recebido numeric(15,2) NOT NULL DEFAULT 0,
    valor_fatura numeric(15,2) NOT NULL DEFAULT 0,
    tolerancia numeric(7,4) NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'PENDENTE',
    divergencia text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_com_conferencia_pedido
    ON brasil_saas.bc_com_conferencia_fatura (empresa_id, pedido_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_conferencia_fatura_item (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    conferencia_id bigint NOT NULL,
    produto_id bigint,
    numero_item integer,
    descricao varchar(300),
    quantidade_pedida numeric(15,4) NOT NULL DEFAULT 0,
    quantidade_recebida numeric(15,4) NOT NULL DEFAULT 0,
    quantidade_faturada numeric(15,4) NOT NULL DEFAULT 0,
    valor_unitario_pedido numeric(15,4) NOT NULL DEFAULT 0,
    valor_unitario_recebido numeric(15,4) NOT NULL DEFAULT 0,
    valor_unitario_faturado numeric(15,4) NOT NULL DEFAULT 0,
    valor_total_pedido numeric(15,2) NOT NULL DEFAULT 0,
    valor_total_recebido numeric(15,2) NOT NULL DEFAULT 0,
    valor_total_faturado numeric(15,2) NOT NULL DEFAULT 0,
    conforme boolean NOT NULL DEFAULT true,
    tipo_divergencia varchar(40),
    divergencia varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_com_conferencia_item
    ON brasil_saas.bc_com_conferencia_fatura_item (empresa_id, conferencia_id) WHERE deleted_at IS NULL;
