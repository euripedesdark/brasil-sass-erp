-- Tabelas do PCP criadas sem as colunas de auditoria exigidas pela entidade.
ALTER TABLE brasil_saas.bc_prod_centro_trabalho
    ADD COLUMN IF NOT EXISTS uuid uuid NOT NULL DEFAULT gen_random_uuid(),
    ADD COLUMN IF NOT EXISTS created_by bigint,
    ADD COLUMN IF NOT EXISTS updated_by bigint;
ALTER TABLE brasil_saas.bc_prod_roteiro
    ADD COLUMN IF NOT EXISTS uuid uuid NOT NULL DEFAULT gen_random_uuid(),
    ADD COLUMN IF NOT EXISTS created_by bigint,
    ADD COLUMN IF NOT EXISTS updated_by bigint;
ALTER TABLE brasil_saas.bc_prod_roteiro_operacao
    ADD COLUMN IF NOT EXISTS uuid uuid NOT NULL DEFAULT gen_random_uuid(),
    ADD COLUMN IF NOT EXISTS created_by bigint,
    ADD COLUMN IF NOT EXISTS updated_by bigint;
ALTER TABLE brasil_saas.bc_prod_centro_trabalho ADD CONSTRAINT bc_prod_centro_trabalho_uuid_key UNIQUE (uuid);
ALTER TABLE brasil_saas.bc_prod_roteiro ADD CONSTRAINT bc_prod_roteiro_uuid_key UNIQUE (uuid);
ALTER TABLE brasil_saas.bc_prod_roteiro_operacao ADD CONSTRAINT bc_prod_roteiro_operacao_uuid_key UNIQUE (uuid);
