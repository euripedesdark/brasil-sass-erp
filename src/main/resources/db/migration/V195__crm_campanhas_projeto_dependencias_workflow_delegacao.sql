-- Extensao aditiva: CRM, cronograma e workflow sem alterar historico de migrations.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_crm_campanha (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),empresa_id BIGINT NOT NULL,
 nome VARCHAR(200) NOT NULL,descricao TEXT,canal VARCHAR(30) NOT NULL DEFAULT 'OUTRO',
 status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',data_inicio DATE,data_fim DATE,
 orcamento NUMERIC(15,2) NOT NULL DEFAULT 0,custo_realizado NUMERIC(15,2) NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT now(),updated_at TIMESTAMP,created_by BIGINT,updated_by BIGINT,deleted_at TIMESTAMP);
CREATE INDEX IF NOT EXISTS ix_crm_campanha_tenant ON brasil_saas.bc_crm_campanha(empresa_id,status) WHERE deleted_at IS NULL;
CREATE TABLE IF NOT EXISTS brasil_saas.bc_crm_campanha_contato (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),empresa_id BIGINT NOT NULL,
 campanha_id BIGINT NOT NULL REFERENCES brasil_saas.bc_crm_campanha(id),
 lead_id BIGINT NOT NULL REFERENCES brasil_saas.bc_crm_lead(id),
 status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',data_ultimo_contato TIMESTAMP,observacao TEXT,
 created_at TIMESTAMP NOT NULL DEFAULT now(),updated_at TIMESTAMP,created_by BIGINT,updated_by BIGINT,deleted_at TIMESTAMP);
CREATE UNIQUE INDEX IF NOT EXISTS ux_crm_campanha_lead_ativo ON brasil_saas.bc_crm_campanha_contato(empresa_id,campanha_id,lead_id) WHERE deleted_at IS NULL;
CREATE TABLE IF NOT EXISTS brasil_saas.bc_prj_dependencia (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),empresa_id BIGINT NOT NULL,
 projeto_id BIGINT NOT NULL REFERENCES brasil_saas.bc_prj_projeto(id),
 antecessora_id BIGINT NOT NULL REFERENCES brasil_saas.bc_prj_etapa(id),
 sucessora_id BIGINT NOT NULL REFERENCES brasil_saas.bc_prj_etapa(id),
 defasagem_dias INTEGER NOT NULL DEFAULT 0 CHECK(defasagem_dias BETWEEN 0 AND 3650),
 created_at TIMESTAMP NOT NULL DEFAULT now(),updated_at TIMESTAMP,created_by BIGINT,updated_by BIGINT,deleted_at TIMESTAMP,
 CONSTRAINT ck_prj_dep_sem_auto CHECK(antecessora_id <> sucessora_id));
CREATE UNIQUE INDEX IF NOT EXISTS ux_prj_dependencia_ativa ON brasil_saas.bc_prj_dependencia(empresa_id,projeto_id,antecessora_id,sucessora_id) WHERE deleted_at IS NULL;
ALTER TABLE brasil_saas.bc_prj_etapa ADD COLUMN IF NOT EXISTS concluida_em TIMESTAMP;
ALTER TABLE brasil_saas.bc_wkf_task ADD COLUMN IF NOT EXISTS responsavel_anterior VARCHAR(200),
 ADD COLUMN IF NOT EXISTS delegado_por BIGINT, ADD COLUMN IF NOT EXISTS delegado_em TIMESTAMP;
INSERT INTO brasil_saas.bc_core_permissao(codigo,recurso,acao,descricao)
SELECT 'workflow:delegar','workflow','delegar','Delegacao de tarefas pendentes'
WHERE NOT EXISTS(SELECT 1 FROM brasil_saas.bc_core_permissao WHERE codigo='workflow:delegar' AND deleted_at IS NULL);
INSERT INTO brasil_saas.bc_core_perfil_permissao(perfil_id,permissao_id)
SELECT p.id,perm.id FROM brasil_saas.bc_core_perfil p
JOIN brasil_saas.bc_core_permissao perm ON perm.codigo='workflow:delegar' AND perm.deleted_at IS NULL
WHERE p.deleted_at IS NULL AND UPPER(p.nome) IN('SUPERUSER','ADMIN')
AND NOT EXISTS(SELECT 1 FROM brasil_saas.bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=perm.id);
