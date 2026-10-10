-- ATP: local_id NULL nao participa de UNIQUE em Postgres (varias linhas "iguais" permitidas).
-- Usa sentinela 0 = calculo da empresa toda (todos os depositos).
UPDATE brasil_saas.bc_sc_atp SET local_id = 0 WHERE local_id IS NULL;
ALTER TABLE brasil_saas.bc_sc_atp ALTER COLUMN local_id SET DEFAULT 0;
ALTER TABLE brasil_saas.bc_sc_atp ALTER COLUMN local_id SET NOT NULL;
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'bc_sc_atp_empresa_id_produto_id_local_id_data_key'
      AND conrelid = 'brasil_saas.bc_sc_atp'::regclass
  ) THEN
    ALTER TABLE brasil_saas.bc_sc_atp
      ADD CONSTRAINT bc_sc_atp_empresa_id_produto_id_local_id_data_key
      UNIQUE (empresa_id, produto_id, local_id, data);
  END IF;
END $$;
