CREATE TABLE IF NOT EXISTS brasil_saas.bc_ctb_pendencia (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  empresa_id bigint NOT NULL,
  evento varchar(60) NOT NULL,
  origem_tipo varchar(60) NOT NULL,
  origem_id bigint NOT NULL,
  motivo varchar(500) NOT NULL,
  valor numeric(18,2),
  status varchar(20) NOT NULL DEFAULT 'ABERTA',
  created_at timestamp NOT NULL DEFAULT now(),
  resolvido_em timestamp,
  UNIQUE (empresa_id, origem_tipo, origem_id, evento)
);
CREATE INDEX IF NOT EXISTS ix_ctb_pendencia_empresa_status
  ON brasil_saas.bc_ctb_pendencia (empresa_id, status);
CREATE INDEX IF NOT EXISTS ix_ctb_lancamento_origem
  ON brasil_saas.bc_ctb_lancamento (empresa_id, origem_tipo, origem_id)
  WHERE deleted_at IS NULL;
