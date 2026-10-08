-- Enterprise block 02: Supply Chain, Transportation, Product Engineering e EHS.

CREATE TABLE IF NOT EXISTS brasil_saas.bc_scm_politica_reposicao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, produto_id bigint NOT NULL, deposito_id bigint, metodo varchar(30) NOT NULL DEFAULT 'PONTO_PEDIDO',
 estoque_minimo numeric(18,3) NOT NULL DEFAULT 0, estoque_maximo numeric(18,3) NOT NULL DEFAULT 0,
 estoque_seguranca numeric(18,3) NOT NULL DEFAULT 0, ponto_pedido numeric(18,3) NOT NULL DEFAULT 0,
 lote_economico numeric(18,3) NOT NULL DEFAULT 0, lead_time_dias integer NOT NULL DEFAULT 0,
 fornecedor_preferencial_id bigint, ativo boolean NOT NULL DEFAULT true,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, created_by bigint, updated_by bigint, deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_scm_reposicao_produto ON brasil_saas.bc_scm_politica_reposicao(empresa_id,produto_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_scm_ordem_transporte (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, tipo varchar(30) NOT NULL DEFAULT 'ENTREGA',
 status varchar(25) NOT NULL DEFAULT 'PLANEJADA', origem varchar(200), destino varchar(200),
 transportadora_id bigint, veiculo varchar(100), motorista varchar(160), data_prevista date,
 data_saida date, data_entrega date, valor_frete numeric(18,2) NOT NULL DEFAULT 0,
 peso numeric(18,3), volume numeric(18,3), observacao text,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, created_by bigint, updated_by bigint, deleted_at timestamp,
 CONSTRAINT uq_scm_ordem_transporte UNIQUE(empresa_id,numero)
);
CREATE INDEX IF NOT EXISTS ix_scm_transporte_status ON brasil_saas.bc_scm_ordem_transporte(empresa_id,status) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_produto_revisao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, produto_id bigint NOT NULL, revisao varchar(30) NOT NULL,
 descricao varchar(500), status varchar(25) NOT NULL DEFAULT 'EM_DESENVOLVIMENTO',
 vigente_desde date, vigente_ate date, motivo varchar(500), documento_id varchar(120),
 criado_por bigint, aprovado_por bigint, aprovado_em timestamp,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp,
 CONSTRAINT uq_plm_revisao UNIQUE(empresa_id,produto_id,revisao)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_mudanca (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, tipo varchar(30) NOT NULL DEFAULT 'ENGENHARIA',
 titulo varchar(200) NOT NULL, descricao text, prioridade varchar(20) NOT NULL DEFAULT 'NORMAL',
 status varchar(25) NOT NULL DEFAULT 'ABERTA', solicitante_id bigint, aprovador_id bigint,
 aprovado_em timestamp, implementado_em timestamp, observacao text,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, created_by bigint, updated_by bigint, deleted_at timestamp,
 CONSTRAINT uq_plm_mudanca UNIQUE(empresa_id,numero)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_ocorrencia (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, tipo varchar(40) NOT NULL,
 severidade varchar(20) NOT NULL DEFAULT 'MEDIA', data_ocorrencia timestamp NOT NULL DEFAULT now(),
 local_ocorrencia varchar(200), funcionario_id bigint, ativo_id bigint, descricao text NOT NULL,
 causa_raiz text, acao_corretiva text, status varchar(25) NOT NULL DEFAULT 'ABERTA',
 prazo date, encerrado_em timestamp, encerrado_por bigint,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, created_by bigint, updated_by bigint, deleted_at timestamp,
 CONSTRAINT uq_ehs_ocorrencia UNIQUE(empresa_id,numero)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_srv_contrato (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, cliente_id bigint, descricao varchar(500) NOT NULL,
 inicio date NOT NULL, fim date, tipo varchar(30) NOT NULL DEFAULT 'SUPORTE',
 sla_horas numeric(10,2), valor_mensal numeric(18,2) NOT NULL DEFAULT 0,
 franquia_horas numeric(10,2), horas_consumidas numeric(10,2) NOT NULL DEFAULT 0,
 status varchar(25) NOT NULL DEFAULT 'ATIVO', renovacao_automatica boolean NOT NULL DEFAULT false,
 observacao text, created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 created_by bigint, updated_by bigint, deleted_at timestamp,
 CONSTRAINT uq_srv_contrato UNIQUE(empresa_id,numero)
);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao(codigo,recurso,acao,descricao) VALUES
 ('supplychain:leitura','supplychain','leitura','Consultar planejamento e transporte'),
 ('supplychain:escrita','supplychain','escrita','Alterar planejamento e transporte'),
 ('plm:leitura','plm','leitura','Consultar engenharia e revisões'),
 ('plm:escrita','plm','escrita','Alterar engenharia e mudanças'),
 ('ehs:leitura','ehs','leitura','Consultar ocorrências EHS'),
 ('ehs:escrita','ehs','escrita','Registrar e tratar ocorrências EHS'),
 ('servico:contrato:leitura','servico_contrato','leitura','Consultar contratos de serviço'),
 ('servico:contrato:escrita','servico_contrato','escrita','Alterar contratos de serviço')
ON CONFLICT(codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao(perfil_id,permissao_id)
SELECT p.id,pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER','DIRETORIA','GERENTE','GESTOR')
AND pe.codigo IN ('supplychain:leitura','supplychain:escrita','plm:leitura','plm:escrita','ehs:leitura','ehs:escrita','servico:contrato:leitura','servico:contrato:escrita')
AND NOT EXISTS(SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);
