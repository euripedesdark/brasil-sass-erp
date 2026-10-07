-- RH enterprise: ciclo de vida, contratos, beneficios, dependentes, afastamentos,
-- banco de horas, eventos de folha, fechamento, medicina/seguranca e treinamento.
CREATE SCHEMA IF NOT EXISTS brasil_saas;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_contrato (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, funcionario_id bigint NOT NULL, tipo varchar(30) NOT NULL DEFAULT 'CLT',
 numero varchar(80), inicio date NOT NULL, fim date, salario numeric(15,2) NOT NULL DEFAULT 0,
 jornada_semanal numeric(6,2) NOT NULL DEFAULT 44, centro_custo_id bigint, cargo_id bigint,
 sindicato varchar(200), regime varchar(40) DEFAULT 'MENSAL', status varchar(20) NOT NULL DEFAULT 'ATIVO',
 motivo_fim varchar(300), created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_rh_contrato_func ON brasil_saas.bc_rh_contrato(empresa_id, funcionario_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_dependente (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, funcionario_id bigint NOT NULL, nome varchar(200) NOT NULL,
 cpf varchar(14), nascimento date, parentesco varchar(50), dependente_ir boolean NOT NULL DEFAULT false,
 dependente_salario_familia boolean NOT NULL DEFAULT false, status varchar(20) NOT NULL DEFAULT 'ATIVO',
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_beneficio (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, codigo varchar(50) NOT NULL, descricao varchar(200) NOT NULL,
 tipo varchar(40) NOT NULL, valor_empresa numeric(15,2) NOT NULL DEFAULT 0,
 valor_funcionario numeric(15,2) NOT NULL DEFAULT 0, periodicidade varchar(20) DEFAULT 'MENSAL',
 fornecedor varchar(200), ativo boolean NOT NULL DEFAULT true, created_at timestamp NOT NULL DEFAULT now(),
 updated_at timestamp, deleted_at timestamp, UNIQUE(empresa_id,codigo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_funcionario_beneficio (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, funcionario_id bigint NOT NULL, beneficio_id bigint NOT NULL,
 inicio date NOT NULL, fim date, quantidade numeric(12,3) NOT NULL DEFAULT 1,
 valor_desconto numeric(15,2) NOT NULL DEFAULT 0, status varchar(20) NOT NULL DEFAULT 'ATIVO',
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_afastamento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, funcionario_id bigint NOT NULL, tipo varchar(50) NOT NULL,
 inicio date NOT NULL, fim date, dias integer NOT NULL DEFAULT 0, cid varchar(20),
 motivo varchar(500), remunerado boolean NOT NULL DEFAULT false, status varchar(20) DEFAULT 'ABERTO',
 esocial_evento_id bigint, created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_banco_horas (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, funcionario_id bigint NOT NULL, competencia varchar(7) NOT NULL,
 saldo_anterior numeric(8,2) NOT NULL DEFAULT 0, creditos numeric(8,2) NOT NULL DEFAULT 0,
 debitos numeric(8,2) NOT NULL DEFAULT 0, saldo_final numeric(8,2) NOT NULL DEFAULT 0,
 status varchar(20) NOT NULL DEFAULT 'ABERTO', observacao varchar(500),
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp,
 UNIQUE(empresa_id,funcionario_id,competencia)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_evento_folha (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, funcionario_id bigint, folha_id bigint, codigo varchar(30) NOT NULL,
 descricao varchar(200) NOT NULL, tipo varchar(20) NOT NULL,
 referencia numeric(15,4) NOT NULL DEFAULT 0, valor numeric(15,2) NOT NULL DEFAULT 0,
 incidencias jsonb NOT NULL DEFAULT '{}'::jsonb, origem varchar(40) DEFAULT 'MANUAL',
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_folha_item (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, folha_id bigint NOT NULL, funcionario_id bigint NOT NULL,
 bruto numeric(15,2) NOT NULL DEFAULT 0, descontos numeric(15,2) NOT NULL DEFAULT 0,
 inss numeric(15,2) NOT NULL DEFAULT 0, irrf numeric(15,2) NOT NULL DEFAULT 0,
 fgts numeric(15,2) NOT NULL DEFAULT 0, beneficios numeric(15,2) NOT NULL DEFAULT 0,
 liquido numeric(15,2) NOT NULL DEFAULT 0, encargos numeric(15,2) NOT NULL DEFAULT 0,
 status varchar(20) NOT NULL DEFAULT 'CALCULADO', created_at timestamp NOT NULL DEFAULT now(),
 updated_at timestamp, UNIQUE(empresa_id,folha_id,funcionario_id)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_fechamento_folha (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, competencia varchar(7) NOT NULL, folha_id bigint,
 etapa varchar(40) NOT NULL, status varchar(20) NOT NULL DEFAULT 'ABERTO',
 total_bruto numeric(18,2) DEFAULT 0, total_descontos numeric(18,2) DEFAULT 0,
 total_liquido numeric(18,2) DEFAULT 0, total_encargos numeric(18,2) DEFAULT 0,
 fechado_em timestamp, fechado_por bigint, observacao varchar(1000),
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp,
 UNIQUE(empresa_id,competencia,etapa)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_saude_seguranca (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, funcionario_id bigint, tipo varchar(40) NOT NULL,
 data_evento date NOT NULL, vencimento date, documento varchar(100), resultado varchar(100),
 risco varchar(100), descricao varchar(1000), status varchar(20) DEFAULT 'ATIVO',
 esocial_evento_id bigint, created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_treinamento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, codigo varchar(50), titulo varchar(200) NOT NULL,
 tipo varchar(50), carga_horaria numeric(8,2) DEFAULT 0, validade_meses integer,
 fornecedor varchar(200), custo numeric(15,2) DEFAULT 0, status varchar(20) DEFAULT 'ATIVO',
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_treinamento_participante (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, treinamento_id bigint NOT NULL,
 funcionario_id bigint NOT NULL, inicio date, fim date, nota numeric(8,2), certificado varchar(200),
 status varchar(20) DEFAULT 'CONCLUIDO', created_at timestamp NOT NULL DEFAULT now(),
 UNIQUE(empresa_id,treinamento_id,funcionario_id)
);

CREATE INDEX IF NOT EXISTS ix_rh_afast_func ON brasil_saas.bc_rh_afastamento(empresa_id,funcionario_id,inicio);
CREATE INDEX IF NOT EXISTS ix_rh_evento_folha ON brasil_saas.bc_rh_evento_folha(empresa_id,folha_id,funcionario_id);
CREATE INDEX IF NOT EXISTS ix_rh_ss_venc ON brasil_saas.bc_rh_saude_seguranca(empresa_id,vencimento);
