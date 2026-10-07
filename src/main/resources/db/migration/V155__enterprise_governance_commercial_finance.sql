-- Enterprise block 01: contratos, fornecedores, metas, fechamento, orçamento,
-- tesouraria projetada e cenários tributários.
-- As tabelas são tenant-scoped e complementam os módulos transacionais existentes.

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ent_contrato (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    tipo varchar(40) NOT NULL,
    numero varchar(80) NOT NULL,
    parceiro_tipo varchar(30),
    parceiro_id bigint,
    descricao varchar(500) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'RASCUNHO',
    inicio date,
    fim date,
    valor_total numeric(18,2) NOT NULL DEFAULT 0,
    renovacao_automatica boolean NOT NULL DEFAULT false,
    indice_reajuste varchar(40),
    observacao text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uq_ent_contrato_empresa_numero UNIQUE (empresa_id, numero)
);
CREATE INDEX IF NOT EXISTS ix_ent_contrato_empresa_status ON brasil_saas.bc_ent_contrato (empresa_id,status) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ent_fornecedor_qualificacao (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    fornecedor_id bigint NOT NULL,
    status varchar(25) NOT NULL DEFAULT 'PENDENTE',
    score numeric(6,2),
    validade date,
    categoria varchar(80),
    observacao text,
    aprovado_em timestamp,
    aprovado_por bigint,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uq_ent_fornecedor_qualificacao UNIQUE (empresa_id, fornecedor_id)
);
CREATE INDEX IF NOT EXISTS ix_ent_fornecedor_qualificacao_status ON brasil_saas.bc_ent_fornecedor_qualificacao (empresa_id,status) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ent_meta_comercial (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    vendedor_id bigint,
    periodo_inicio date NOT NULL,
    periodo_fim date NOT NULL,
    meta_valor numeric(18,2) NOT NULL DEFAULT 0,
    meta_quantidade numeric(18,3) NOT NULL DEFAULT 0,
    realizado_valor numeric(18,2) NOT NULL DEFAULT 0,
    realizado_quantidade numeric(18,3) NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ABERTA',
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_ent_meta_comercial_periodo ON brasil_saas.bc_ent_meta_comercial (empresa_id,periodo_inicio,periodo_fim) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ent_periodo_contabil (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    competencia date NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'ABERTO',
    fechamento_financeiro boolean NOT NULL DEFAULT false,
    fechamento_fiscal boolean NOT NULL DEFAULT false,
    fechamento_contabil boolean NOT NULL DEFAULT false,
    observacao varchar(500),
    fechado_em timestamp,
    fechado_por bigint,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT uq_ent_periodo_contabil UNIQUE (empresa_id,competencia)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ent_orcamento (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    competencia date NOT NULL,
    centro_custo_id bigint,
    conta_contabil_id bigint,
    versao integer NOT NULL DEFAULT 1,
    valor_orcado numeric(18,2) NOT NULL DEFAULT 0,
    valor_revisado numeric(18,2) NOT NULL DEFAULT 0,
    valor_realizado numeric(18,2) NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'RASCUNHO',
    observacao varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_ent_orcamento_competencia ON brasil_saas.bc_ent_orcamento (empresa_id,competencia) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ent_tesouraria_previsao (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    data_prevista date NOT NULL,
    tipo varchar(20) NOT NULL,
    origem varchar(40),
    referencia_id bigint,
    descricao varchar(500) NOT NULL,
    valor numeric(18,2) NOT NULL,
    probabilidade numeric(5,2) NOT NULL DEFAULT 100,
    status varchar(20) NOT NULL DEFAULT 'PROJETADA',
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_ent_tesouraria_data ON brasil_saas.bc_ent_tesouraria_previsao (empresa_id,data_prevista) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ent_cenario_tributario (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    nome varchar(120) NOT NULL,
    vigencia_inicio date NOT NULL,
    vigencia_fim date,
    regime varchar(30),
    uf_origem char(2),
    uf_destino char(2),
    cst varchar(10),
    cfop varchar(10),
    aliquota_icms numeric(8,4),
    aliquota_ibs numeric(8,4),
    aliquota_cbs numeric(8,4),
    reducao numeric(8,4) NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'RASCUNHO',
    observacao text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_ent_cenario_tributario_vigencia ON brasil_saas.bc_ent_cenario_tributario (empresa_id,vigencia_inicio) WHERE deleted_at IS NULL;

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_modulo (uuid,chave,nome,descricao,icone,rota,ordem,exige_superuser,ativo)
SELECT gen_random_uuid(),'enterprise','Gestão Empresarial','Contratos, fornecedores, metas, orçamento, fechamento, tesouraria e cenários tributários','pi pi-building','/gestao-empresarial',46,false,true
WHERE NOT EXISTS (SELECT 1 FROM bc_core_modulo WHERE chave='enterprise');

INSERT INTO bc_core_permissao (codigo,recurso,acao,descricao) VALUES
 ('enterprise:leitura','enterprise','leitura','Consultar gestão empresarial'),
 ('enterprise:escrita','enterprise','escrita','Alterar gestão empresarial')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO bc_core_perfil_permissao (perfil_id,permissao_id)
SELECT p.id,pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER','DIRETORIA','GERENTE','GESTOR')
AND pe.codigo IN ('enterprise:leitura','enterprise:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);
