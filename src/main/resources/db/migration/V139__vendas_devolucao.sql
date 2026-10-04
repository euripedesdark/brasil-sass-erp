-- Troca/devolucao de venda: reverte estoque e registra motivo/aprovacao.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ven_devolucao (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    motivo varchar(500) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'SOLICITADA',
    decidida_por bigint,
    decidida_em timestamp,
    recebida_em timestamp,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ven_devolucao_item (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    devolucao_id bigint NOT NULL REFERENCES brasil_saas.bc_ven_devolucao(id),
    produto_id bigint NOT NULL,
    quantidade numeric(15,3) NOT NULL,
    qtd_recebida numeric(15,3) NOT NULL DEFAULT 0,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_ven_dev_pedido ON brasil_saas.bc_ven_devolucao_item (devolucao_id) WHERE deleted_at IS NULL;
