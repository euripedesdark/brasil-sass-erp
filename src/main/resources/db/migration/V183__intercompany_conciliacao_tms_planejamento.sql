-- GAP #9: conciliação bilateral intercompany
ALTER TABLE brasil_saas.bc_fin_intercompany ADD COLUMN IF NOT EXISTS contrapartida_id BIGINT;
ALTER TABLE brasil_saas.bc_fin_intercompany ADD COLUMN IF NOT EXISTS diferenca NUMERIC(18,2);
ALTER TABLE brasil_saas.bc_fin_intercompany ADD COLUMN IF NOT EXISTS reconciliado_em TIMESTAMP;

-- GAP #10: paradas vinculadas ao documento de origem (pedido/NF) para o planejamento de carga
ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS referencia_tipo VARCHAR(40);
ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS referencia_id BIGINT;
ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS peso NUMERIC(15,3);
ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS volume NUMERIC(15,3);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tms_parada_ref ON brasil_saas.bc_tms_parada(empresa_id, referencia_tipo, referencia_id)
    WHERE referencia_id IS NOT NULL AND status <> 'CANCELADA';
