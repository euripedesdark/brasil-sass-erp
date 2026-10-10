-- Completa o schema de faturamento parcial ja referenciado pela entidade ItemPedidoVenda.
ALTER TABLE brasil_saas.bc_ven_pedido_item ADD COLUMN IF NOT EXISTS quantidade_faturada NUMERIC(15,3) NOT NULL DEFAULT 0;
