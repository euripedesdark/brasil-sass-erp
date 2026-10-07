-- Supply Chain Enterprise: planejamento, disponibilidade, TMS, tracking e frete.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_sc_demanda (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, produto_id bigint NOT NULL,
 local_id bigint, periodo date NOT NULL, tipo varchar(30) NOT NULL DEFAULT 'PREVISAO',
 quantidade numeric(18,4) NOT NULL DEFAULT 0, confianca numeric(7,4), origem varchar(50),
 status varchar(20) NOT NULL DEFAULT 'ABERTA', created_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_sc_planejamento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, codigo varchar(80) NOT NULL,
 periodo_inicio date NOT NULL, periodo_fim date NOT NULL, tipo varchar(30) NOT NULL,
 status varchar(25) NOT NULL DEFAULT 'RASCUNHO', parametros jsonb DEFAULT '{}'::jsonb,
 resultado jsonb DEFAULT '{}'::jsonb, executado_em timestamp, created_at timestamp NOT NULL DEFAULT now(),
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_sc_atp (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, produto_id bigint NOT NULL,
 local_id bigint, data date NOT NULL, estoque_disponivel numeric(18,4) DEFAULT 0,
 entradas_confirmadas numeric(18,4) DEFAULT 0, reservas numeric(18,4) DEFAULT 0,
 demanda_aberta numeric(18,4) DEFAULT 0, quantidade_atp numeric(18,4) DEFAULT 0,
 quantidade_ctp numeric(18,4) DEFAULT 0, calculado_em timestamp NOT NULL DEFAULT now(),
 UNIQUE(empresa_id,produto_id,local_id,data)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_sc_rota (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, codigo varchar(80) NOT NULL,
 origem varchar(200) NOT NULL, destino varchar(200) NOT NULL, distancia_km numeric(12,2),
 tempo_minutos integer, custo_base numeric(18,2) DEFAULT 0, custo_km numeric(18,4) DEFAULT 0,
 pedagio numeric(18,2) DEFAULT 0, restricoes jsonb DEFAULT '{}'::jsonb, ativo boolean DEFAULT true,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_sc_carga (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, codigo varchar(80) NOT NULL,
 transportadora_id bigint, veiculo varchar(100), origem varchar(200), destino varchar(200),
 peso numeric(18,3) DEFAULT 0, volume numeric(18,3) DEFAULT 0, valor_mercadoria numeric(18,2) DEFAULT 0,
 rota_id bigint, frete_estimado numeric(18,2) DEFAULT 0, frete_real numeric(18,2) DEFAULT 0,
 status varchar(30) NOT NULL DEFAULT 'PLANEJADA', saida timestamp, entrega_prevista timestamp, entrega timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_sc_tracking (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, carga_id bigint NOT NULL,
 evento varchar(50) NOT NULL, data_evento timestamp NOT NULL DEFAULT now(), localizacao varchar(200),
 latitude numeric(10,7), longitude numeric(10,7), ocorrencia varchar(500), comprovante varchar(500)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_sc_frete (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, carga_id bigint NOT NULL,
 transportadora_id bigint, documento varchar(100), frete numeric(18,2) NOT NULL DEFAULT 0,
 pedagio numeric(18,2) DEFAULT 0, adicionais numeric(18,2) DEFAULT 0, total numeric(18,2) NOT NULL DEFAULT 0,
 status varchar(25) NOT NULL DEFAULT 'PENDENTE', conferido_em timestamp, pago_em timestamp,
 UNIQUE(empresa_id,carga_id)
);
CREATE INDEX IF NOT EXISTS ix_sc_demanda_periodo ON brasil_saas.bc_sc_demanda(empresa_id,produto_id,periodo);
CREATE INDEX IF NOT EXISTS ix_sc_tracking_carga ON brasil_saas.bc_sc_tracking(empresa_id,carga_id,data_evento);
