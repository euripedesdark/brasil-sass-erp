-- Enterprise block 03: consolidação/intercompany e governança de riscos/controles.

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_intercompany (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, empresa_parceira_id bigint NOT NULL, numero varchar(80) NOT NULL,
 tipo varchar(30) NOT NULL DEFAULT 'LANCAMENTO', data_documento date NOT NULL,
 competencia date, descricao varchar(500) NOT NULL, valor numeric(18,2) NOT NULL DEFAULT 0,
 moeda char(3) NOT NULL DEFAULT 'BRL', status varchar(25) NOT NULL DEFAULT 'ABERTO',
 reconciliado boolean NOT NULL DEFAULT false, documento_origem_tipo varchar(50), documento_origem_id bigint,
 observacao text, created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 created_by bigint, updated_by bigint, deleted_at timestamp,
 CONSTRAINT uq_fin_intercompany UNIQUE(empresa_id,numero)
);
CREATE INDEX IF NOT EXISTS ix_fin_intercompany_partner ON brasil_saas.bc_fin_intercompany(empresa_id,empresa_parceira_id,competencia) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_consolidacao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, competencia date NOT NULL, grupo varchar(100) NOT NULL DEFAULT 'PADRAO',
 status varchar(25) NOT NULL DEFAULT 'RASCUNHO', eliminacoes numeric(18,2) NOT NULL DEFAULT 0,
 ajustes numeric(18,2) NOT NULL DEFAULT 0, cambio_medio numeric(18,8), cambio_fechamento numeric(18,8),
 valor_consolidado numeric(18,2) NOT NULL DEFAULT 0, consolidado_em timestamp, consolidado_por bigint,
 observacao text, created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, created_by bigint, updated_by bigint,
 CONSTRAINT uq_fin_consolidacao UNIQUE(empresa_id,competencia,grupo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_gov_risco (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, codigo varchar(80) NOT NULL, categoria varchar(60) NOT NULL,
 titulo varchar(200) NOT NULL, descricao text, probabilidade numeric(6,2) NOT NULL DEFAULT 0,
 impacto numeric(6,2) NOT NULL DEFAULT 0, score numeric(10,2) NOT NULL DEFAULT 0,
 tratamento varchar(40) DEFAULT 'MITIGAR', responsavel_id bigint, status varchar(25) NOT NULL DEFAULT 'ABERTO',
 prazo date, risco_residual numeric(10,2), created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 created_by bigint, updated_by bigint, deleted_at timestamp,
 CONSTRAINT uq_gov_risco UNIQUE(empresa_id,codigo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_gov_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, codigo varchar(80) NOT NULL, processo varchar(100) NOT NULL,
 titulo varchar(200) NOT NULL, objetivo text, frequencia varchar(30) NOT NULL DEFAULT 'MENSAL',
 tipo varchar(30) NOT NULL DEFAULT 'PREVENTIVO', responsavel_id bigint,
 status varchar(25) NOT NULL DEFAULT 'ATIVO', ultima_execucao date, proxima_execucao date,
 evidencias text, resultado varchar(30), created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 created_by bigint, updated_by bigint, deleted_at timestamp,
 CONSTRAINT uq_gov_controle UNIQUE(empresa_id,codigo)
);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao(codigo,recurso,acao,descricao) VALUES
 ('corporativo:leitura','corporativo','leitura','Consultar consolidação, intercompany e governança'),
 ('corporativo:escrita','corporativo','escrita','Alterar consolidação, intercompany e governança')
ON CONFLICT(codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao(perfil_id,permissao_id)
SELECT p.id,pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER','DIRETORIA','GERENTE')
AND pe.codigo IN ('corporativo:leitura','corporativo:escrita')
AND NOT EXISTS(SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);
