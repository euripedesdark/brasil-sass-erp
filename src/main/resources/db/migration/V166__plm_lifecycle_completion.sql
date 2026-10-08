-- PLM lifecycle completion: effect execution, auditability and production master-data integration.
ALTER TABLE brasil_saas.bc_plm_efeito_mudanca
    ADD COLUMN IF NOT EXISTS ordem_execucao integer NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS obrigatorio boolean NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS aplicado_em timestamp,
    ADD COLUMN IF NOT EXISTS aplicado_por bigint,
    ADD COLUMN IF NOT EXISTS erro_implementacao varchar(2000);

CREATE INDEX IF NOT EXISTS ix_plm_efeito_execucao
    ON brasil_saas.bc_plm_efeito_mudanca(empresa_id,mudanca_id,ordem_execucao);

ALTER TABLE brasil_saas.bc_plm_aprovacao
    ADD COLUMN IF NOT EXISTS obrigatoria boolean NOT NULL DEFAULT true;

CREATE INDEX IF NOT EXISTS ix_plm_aprovacao_workflow
    ON brasil_saas.bc_plm_aprovacao(empresa_id,mudanca_id,etapa,decisao);
