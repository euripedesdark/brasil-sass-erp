-- Compatibiliza instalacoes V139 com as colunas presentes no bootstrap B184.
-- Mantem registros antigos sem pedido correspondente, sem inventar cliente.
ALTER TABLE brasil_saas.bc_ven_devolucao
    ADD COLUMN IF NOT EXISTS cliente_id bigint,
    ADD COLUMN IF NOT EXISTS numero varchar(30);

UPDATE brasil_saas.bc_ven_devolucao d
SET cliente_id = p.cliente_id
FROM brasil_saas.bc_ven_pedido p
WHERE d.pedido_id = p.id AND d.empresa_id = p.empresa_id
  AND d.cliente_id IS NULL AND p.cliente_id IS NOT NULL;

UPDATE brasil_saas.bc_ven_devolucao
SET numero = 'DV-LEG-' || id::text
WHERE numero IS NULL;
