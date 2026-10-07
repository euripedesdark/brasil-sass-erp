-- TMS: associação de rota e dados de execução da ordem.
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS rota_id bigint;
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS veiculo varchar(120);
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS motorista varchar(160);
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS data_saida timestamp;
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS data_entrega timestamp;
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS distancia_real_km numeric(12,2);
CREATE INDEX IF NOT EXISTS ix_tms_ordem_rota ON brasil_saas.bc_tms_ordem(empresa_id,rota_id);
