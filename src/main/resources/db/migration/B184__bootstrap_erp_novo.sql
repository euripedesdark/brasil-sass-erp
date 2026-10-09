-- B184: bootstrap exclusivo de instalacoes novas e vazias.
-- Esquema sem dados do dump sanitizado, consolidado com V159-V184.
-- Inclui somente sementes de permissoes/modulos das migrations anteriores.
-- Nao aplica baseline/repair em bancos existentes; migrations V* permanecem intactas.

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

CREATE SCHEMA IF NOT EXISTS brasil_saas;

COMMENT ON SCHEMA brasil_saas IS 'Schema principal do Brasil SaaS ERP';

CREATE SCHEMA brasil_saas_dl;

COMMENT ON SCHEMA brasil_saas_dl IS 'Datalake do BI - Brasil SaaS ERP';

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA brasil_saas;

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA brasil_saas;

COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA brasil_saas;

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';

CREATE DOMAIN public.dmemo AS character varying(500);

CREATE DOMAIN public.dmkqt AS numeric(15,2);

CREATE DOMAIN public.docod AS integer;

CREATE DOMAIN public.dodata AS date;

CREATE DOMAIN public.dohoras AS time without time zone;

CREATE DOMAIN public.dom1 AS character(1);

CREATE DOMAIN public.dom10 AS character varying(10);

CREATE DOMAIN public.dom100 AS character varying(100);

CREATE DOMAIN public.dom15 AS character varying(15);

CREATE DOMAIN public.dom150 AS character varying(150);

CREATE DOMAIN public.dom2 AS character(2);

CREATE DOMAIN public.dom20 AS character varying(20);

CREATE DOMAIN public.dom200 AS character varying(200);

CREATE DOMAIN public.dom30 AS character varying(30);

CREATE DOMAIN public.dom40 AS character varying(40);

CREATE DOMAIN public.dom45 AS character varying(45);

CREATE DOMAIN public.dom5 AS character varying(5);

CREATE DOMAIN public.dom50 AS character varying(50);

CREATE DOMAIN public.dom60 AS character varying(60);

CREATE DOMAIN public.dom80 AS character varying(80);

CREATE DOMAIN public.dompeso AS numeric(9,3);

CREATE DOMAIN public.domtraco AS numeric(15,5);

CREATE DOMAIN public.domvalor4casas AS numeric(15,4);

CREATE DOMAIN public.dovalor AS numeric(15,2);

CREATE DOMAIN public.dovalort AS numeric(15,4);

CREATE FUNCTION brasil_saas.fiscal_normaliza_codigo(p_txt text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $$
    SELECT ltrim(regexp_replace(p_txt, '[^0-9]', '', 'g'), '0');
$$;

COMMENT ON FUNCTION brasil_saas.fiscal_normaliza_codigo(p_txt text) IS 'Código sem pontuação e sem zeros à esquerda, para casar o que o usuário digita com o que está cadastrado. Só usa dígito, então independe de collation.';

CREATE FUNCTION brasil_saas.fn_repetir_papel_proibido() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_usuario   text;
    v_empresa_j text;
    v_ja_existe boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM brasil_saas.bc_core_usuario_empresa ue
         WHERE ue.usuario_id  = NEW.usuario_id
           AND ue.perfil_nome = NEW.perfil_nome
           AND ue.id <> COALESCE(NEW.id, -1)
    ) INTO v_ja_existe;

    IF NOT v_ja_existe THEN
        RETURN NEW;
    END IF;

    SELECT u.username INTO v_usuario
      FROM brasil_saas.bc_core_usuario u WHERE u.id = NEW.usuario_id;

    SELECT e.razao_social INTO v_empresa_j
      FROM brasil_saas.bc_core_usuario_empresa ue
      JOIN brasil_saas.bc_core_empresa e ON e.id = ue.empresa_id
     WHERE ue.usuario_id  = NEW.usuario_id
       AND ue.perfil_nome = NEW.perfil_nome
       AND ue.id <> COALESCE(NEW.id, -1)
     LIMIT 1;

    RAISE EXCEPTION
        'Não é possível dar o papel % a %: esta pessoa já é % na empresa "%". '
        'Uma pessoa pode ter papéis diferentes em empresas diferentes, mas não '
        'o mesmo papel duas vezes. Remova o vínculo existente antes de criar este.',
        NEW.perfil_nome, v_usuario, NEW.perfil_nome,
        coalesce(v_empresa_j, 'empresa desconhecida')
        USING ERRCODE = 'unique_violation';
END;
$$;

CREATE FUNCTION brasil_saas.set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION brasil_saas.set_updated_at() IS 'Atualiza updated_at em triggers de UPDATE';

SET default_tablespace = '';

SET default_table_access_method = heap;

CREATE TABLE brasil_saas.bc_ativo_imobilizado (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    descricao character varying(255) NOT NULL,
    classe character varying(100),
    numero_serie character varying(100),
    localizacao character varying(255),
    responsavel_id bigint,
    data_aquisicao date,
    valor_aquisicao numeric(15,2) DEFAULT 0 NOT NULL,
    valor_residual numeric(15,2) DEFAULT 0 NOT NULL,
    valor_depreciado numeric(15,2) DEFAULT 0 NOT NULL,
    status character varying(30) DEFAULT 'ATIVO'::character varying NOT NULL,
    vida_util_meses integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ativo_imobilizado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ativo_imobilizado_id_seq OWNED BY brasil_saas.bc_ativo_imobilizado.id;

CREATE TABLE brasil_saas.bc_ativo_manutencao (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    ativo_id bigint NOT NULL,
    numero character varying(50) NOT NULL,
    tipo character varying(30) DEFAULT 'CORRETIVA'::character varying NOT NULL,
    status character varying(30) DEFAULT 'ABERTA'::character varying NOT NULL,
    prioridade character varying(20) DEFAULT 'MEDIA'::character varying NOT NULL,
    descricao character varying(2000) NOT NULL,
    data_programada date,
    data_conclusao date,
    custo numeric(15,2) DEFAULT 0 NOT NULL,
    responsavel_id bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ativo_manutencao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ativo_manutencao_id_seq OWNED BY brasil_saas.bc_ativo_manutencao.id;

CREATE TABLE brasil_saas.bc_bi_dashboard (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    dashboard_type character varying(50) DEFAULT 'CUSTOM'::character varying,
    layout_config jsonb,
    is_public boolean DEFAULT false,
    is_default boolean DEFAULT false,
    refresh_interval_minutes integer DEFAULT 30,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    nome character varying(100),
    tipo character varying(50),
    layout text,
    filtros text,
    ativo boolean DEFAULT true,
    publico boolean DEFAULT false,
    data_criacao date DEFAULT CURRENT_DATE,
    data_atualizacao date,
    criado_por bigint,
    descricao text
);

COMMENT ON TABLE brasil_saas.bc_bi_dashboard IS 'Business Intelligence dashboards';

CREATE SEQUENCE brasil_saas.bc_bi_dashboard_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_dashboard_id_seq OWNED BY brasil_saas.bc_bi_dashboard.id;

CREATE TABLE brasil_saas.bc_bi_dashboard_widget (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    dashboard_id bigint NOT NULL,
    title character varying(100) NOT NULL,
    widget_type character varying(50) NOT NULL,
    data_source character varying(200),
    chart_type character varying(50),
    config jsonb,
    position_x integer DEFAULT 0,
    position_y integer DEFAULT 0,
    width integer DEFAULT 4,
    height integer DEFAULT 3,
    refresh_enabled boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_bi_dashboard_widget IS 'Widgets that compose dashboards';

CREATE SEQUENCE brasil_saas.bc_bi_dashboard_widget_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_dashboard_widget_id_seq OWNED BY brasil_saas.bc_bi_dashboard_widget.id;

CREATE TABLE brasil_saas.bc_bi_indicador (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    categoria character varying(50),
    formula text,
    valor_atual numeric(15,2),
    valor_anterior numeric(15,2),
    valor_meta numeric(15,2),
    unidade_medida character varying(20) DEFAULT 'R$'::character varying,
    cor_valor_baixo character varying(20) DEFAULT '#dc3545'::character varying,
    cor_valor_medio character varying(20) DEFAULT '#ffc107'::character varying,
    cor_valor_alto character varying(20) DEFAULT '#28a745'::character varying,
    ativo boolean DEFAULT true,
    data_calculo date,
    frequencia_atualizacao character varying(20),
    visivel_dashboard boolean DEFAULT true,
    ordem_exibicao integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_bi_indicador_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_indicador_id_seq OWNED BY brasil_saas.bc_bi_indicador.id;

CREATE TABLE brasil_saas.bc_bi_kpi (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    kpi_type character varying(50),
    query_formula text,
    target_value double precision,
    current_value double precision,
    unit character varying(20) DEFAULT 'R$'::character varying,
    format character varying(20) DEFAULT 'NUMBER'::character varying,
    color_good character varying(20) DEFAULT '#10b981'::character varying,
    color_warning character varying(20) DEFAULT '#f59e0b'::character varying,
    color_bad character varying(20) DEFAULT '#ef4444'::character varying,
    threshold_good double precision,
    threshold_warning double precision,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_bi_kpi IS 'Key Performance Indicators';

CREATE SEQUENCE brasil_saas.bc_bi_kpi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_kpi_id_seq OWNED BY brasil_saas.bc_bi_kpi.id;

CREATE TABLE brasil_saas.bc_bi_relatorio (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    tipo character varying(50),
    categoria character varying(50),
    sql_query text,
    parametros text,
    ativo boolean DEFAULT true,
    agendado boolean DEFAULT false,
    frequencia character varying(20),
    ultima_execucao timestamp without time zone,
    proxima_execucao timestamp without time zone,
    email_destinatarios text,
    formato_exportacao character varying(20) DEFAULT 'PDF'::character varying,
    criado_por bigint,
    data_criacao timestamp without time zone DEFAULT now(),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE TABLE brasil_saas.bc_bi_relatorio_agendado (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    relatorio_id bigint,
    nome character varying(100) NOT NULL,
    descricao text,
    frequencia character varying(20),
    intervalo_dias integer,
    proxima_execucao timestamp without time zone,
    ultima_execucao timestamp without time zone,
    ativo boolean DEFAULT true,
    email_destinatarios text,
    formato character varying(20) DEFAULT 'PDF'::character varying,
    parametros text,
    criado_por bigint,
    data_criacao timestamp without time zone DEFAULT now(),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_bi_relatorio_agendado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_relatorio_agendado_id_seq OWNED BY brasil_saas.bc_bi_relatorio_agendado.id;

CREATE SEQUENCE brasil_saas.bc_bi_relatorio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_relatorio_id_seq OWNED BY brasil_saas.bc_bi_relatorio.id;

CREATE TABLE brasil_saas.bc_bi_report (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    report_type character varying(50),
    query_sql text,
    template_path character varying(500),
    output_format character varying(50),
    is_scheduled boolean DEFAULT false,
    schedule_cron character varying(100),
    schedule_email character varying(200),
    category character varying(50),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_bi_report IS 'Business Intelligence reports';

CREATE SEQUENCE brasil_saas.bc_bi_report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_report_id_seq OWNED BY brasil_saas.bc_bi_report.id;

CREATE TABLE brasil_saas.bc_bi_report_parameter (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    report_id bigint NOT NULL,
    name character varying(50) NOT NULL,
    label character varying(100),
    parameter_type character varying(20),
    default_value character varying(200),
    is_required boolean DEFAULT false,
    sort_order integer DEFAULT 0,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_bi_report_parameter IS 'Parameters for reports';

CREATE SEQUENCE brasil_saas.bc_bi_report_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_bi_report_parameter_id_seq OWNED BY brasil_saas.bc_bi_report_parameter.id;

CREATE TABLE brasil_saas.bc_cad_base_cep (
    id bigint NOT NULL,
    cep character varying(8) NOT NULL,
    logradouro character varying(200),
    bairro character varying(100),
    municipio_id bigint,
    uf character(2),
    tipo character varying(30),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_base_cep IS 'Base de CEPs (global)';

CREATE SEQUENCE brasil_saas.bc_cad_base_cep_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_base_cep_id_seq OWNED BY brasil_saas.bc_cad_base_cep.id;

CREATE TABLE brasil_saas.bc_cad_categoria (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao character varying(255),
    categoria_pai_id bigint,
    tipo character varying(20) DEFAULT 'PRODUTO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_categoria IS 'Categorias de produtos/serviços (hierárquica)';

CREATE SEQUENCE brasil_saas.bc_cad_categoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_categoria_id_seq OWNED BY brasil_saas.bc_cad_categoria.id;

CREATE TABLE brasil_saas.bc_cad_cliente (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    codigo character varying(30),
    limite_credito numeric(15,2) DEFAULT 0 NOT NULL,
    classificacao character varying(10),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    logo_url text,
    logo_tipo_conteudo character varying(50),
    logo_tamanho bigint
);

COMMENT ON TABLE brasil_saas.bc_cad_cliente IS 'Clientes';

COMMENT ON COLUMN brasil_saas.bc_cad_cliente.logo_url IS 'URL do logo do cliente para relatórios e documentos';

COMMENT ON COLUMN brasil_saas.bc_cad_cliente.logo_tipo_conteudo IS 'Tipo MIME do logo do cliente';

COMMENT ON COLUMN brasil_saas.bc_cad_cliente.logo_tamanho IS 'Tamanho em bytes do logo do cliente';

CREATE SEQUENCE brasil_saas.bc_cad_cliente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_cliente_id_seq OWNED BY brasil_saas.bc_cad_cliente.id;

CREATE TABLE brasil_saas.bc_cad_contato (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    tipo character varying(30) DEFAULT 'COMERCIAL'::character varying NOT NULL,
    nome character varying(150),
    email character varying(150),
    telefone character varying(20),
    observacao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_contato IS 'Contatos das pessoas';

CREATE SEQUENCE brasil_saas.bc_cad_contato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_contato_id_seq OWNED BY brasil_saas.bc_cad_contato.id;

CREATE TABLE brasil_saas.bc_cad_documento_fiscal (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint,
    modelo character varying(10) NOT NULL,
    serie character varying(10),
    numero character varying(20) NOT NULL,
    chave_acesso character varying(50),
    emissao_at timestamp without time zone,
    arquivo_url character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_documento_fiscal IS 'Documentos fiscais recebidos/emitidos vinculados à pessoa';

CREATE SEQUENCE brasil_saas.bc_cad_documento_fiscal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_documento_fiscal_id_seq OWNED BY brasil_saas.bc_cad_documento_fiscal.id;

CREATE TABLE brasil_saas.bc_cad_endereco (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    tipo character varying(20) DEFAULT 'PRINCIPAL'::character varying NOT NULL,
    logradouro character varying(200),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cep character varying(8),
    municipio_id bigint,
    uf character(2),
    latitude numeric(10,7),
    longitude numeric(10,7),
    principal boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_endereco IS 'Endereços das pessoas';

CREATE SEQUENCE brasil_saas.bc_cad_endereco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_endereco_id_seq OWNED BY brasil_saas.bc_cad_endereco.id;

CREATE TABLE brasil_saas.bc_cad_fornecedor (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    codigo character varying(30),
    prazo_medio_dias integer DEFAULT 0 NOT NULL,
    avaliacao numeric(4,2),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    logo_url text,
    logo_tipo_conteudo character varying(50),
    logo_tamanho bigint
);

COMMENT ON TABLE brasil_saas.bc_cad_fornecedor IS 'Fornecedores';

COMMENT ON COLUMN brasil_saas.bc_cad_fornecedor.logo_url IS 'URL do logo do fornecedor';

COMMENT ON COLUMN brasil_saas.bc_cad_fornecedor.logo_tipo_conteudo IS 'Tipo MIME do logo do fornecedor';

COMMENT ON COLUMN brasil_saas.bc_cad_fornecedor.logo_tamanho IS 'Tamanho em bytes do logo do fornecedor';

CREATE SEQUENCE brasil_saas.bc_cad_fornecedor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_fornecedor_id_seq OWNED BY brasil_saas.bc_cad_fornecedor.id;

CREATE TABLE brasil_saas.bc_cad_marca (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_marca IS 'Marcas de produtos';

CREATE SEQUENCE brasil_saas.bc_cad_marca_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_marca_id_seq OWNED BY brasil_saas.bc_cad_marca.id;

CREATE TABLE brasil_saas.bc_cad_municipio (
    id bigint NOT NULL,
    codigo_ibge character varying(7) NOT NULL,
    nome character varying(100) NOT NULL,
    uf character(2) NOT NULL,
    codigo_siafi character varying(10),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_municipio IS 'Municípios IBGE (tabela oficial, global)';

CREATE SEQUENCE brasil_saas.bc_cad_municipio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_municipio_id_seq OWNED BY brasil_saas.bc_cad_municipio.id;

CREATE TABLE brasil_saas.bc_cad_papel (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(50) NOT NULL,
    descricao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_papel IS 'Papéis de relacionamento (CLIENTE, FORNECEDOR, TRANSPORTADORA...)';

CREATE SEQUENCE brasil_saas.bc_cad_papel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_papel_id_seq OWNED BY brasil_saas.bc_cad_papel.id;

CREATE TABLE brasil_saas.bc_cad_pessoa (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    tipo character varying(10) NOT NULL,
    nome character varying(200) NOT NULL,
    documento character varying(14),
    email character varying(150),
    telefone character varying(20),
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_pessoa IS 'Pessoas físicas e jurídicas (cadastro único)';

COMMENT ON COLUMN brasil_saas.bc_cad_pessoa.tipo IS 'FISICA ou JURIDICA';

CREATE TABLE brasil_saas.bc_cad_pessoa_fisica (
    id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    cpf character varying(11),
    rg character varying(20),
    orgao_expedidor character varying(20),
    data_nascimento date,
    sexo character(1),
    estado_civil character varying(20),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_pessoa_fisica IS 'Complemento de pessoa física';

CREATE SEQUENCE brasil_saas.bc_cad_pessoa_fisica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_pessoa_fisica_id_seq OWNED BY brasil_saas.bc_cad_pessoa_fisica.id;

CREATE SEQUENCE brasil_saas.bc_cad_pessoa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_pessoa_id_seq OWNED BY brasil_saas.bc_cad_pessoa.id;

CREATE TABLE brasil_saas.bc_cad_pessoa_juridica (
    id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    cnpj character varying(14),
    inscricao_estadual character varying(30),
    inscricao_municipal character varying(30),
    data_abertura date,
    porte character varying(20),
    natureza_juridica character varying(100),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_pessoa_juridica IS 'Complemento de pessoa jurídica';

CREATE SEQUENCE brasil_saas.bc_cad_pessoa_juridica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_pessoa_juridica_id_seq OWNED BY brasil_saas.bc_cad_pessoa_juridica.id;

CREATE TABLE brasil_saas.bc_cad_produto (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(30) NOT NULL,
    nome character varying(200) NOT NULL,
    descricao text,
    categoria_id bigint,
    marca_id bigint,
    unidade_medida_id bigint,
    ncm character varying(8),
    cfop_padrao character varying(4),
    codigo_barras character varying(30),
    preco_custo numeric(15,4) DEFAULT 0 NOT NULL,
    preco_venda numeric(15,2) DEFAULT 0 NOT NULL,
    estoque_minimo numeric(15,3) DEFAULT 0 NOT NULL,
    estoque_maximo numeric(15,3) DEFAULT 0 NOT NULL,
    peso numeric(10,3),
    tipo character varying(20) DEFAULT 'PRODUTO'::character varying NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    url_produto character varying(500),
    cest character varying(7)
);

COMMENT ON TABLE brasil_saas.bc_cad_produto IS 'Produtos';

COMMENT ON COLUMN brasil_saas.bc_cad_produto.cest IS 'CEST do produto, especificador da substituicao tributaria (7 digitos).';

CREATE TABLE brasil_saas.bc_cad_produto_ecommerce (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    publicado boolean DEFAULT false NOT NULL,
    marketplace character varying(50),
    url_externa character varying(500),
    preco_marketplace numeric(15,2),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    uuid uuid NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_cad_produto_ecommerce IS 'Publicação de produtos em e-commerce/marketplace';

CREATE SEQUENCE brasil_saas.bc_cad_produto_ecommerce_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_produto_ecommerce_id_seq OWNED BY brasil_saas.bc_cad_produto_ecommerce.id;

CREATE SEQUENCE brasil_saas.bc_cad_produto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_produto_id_seq OWNED BY brasil_saas.bc_cad_produto.id;

CREATE TABLE brasil_saas.bc_cad_produto_imagem (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    url character varying(500) NOT NULL,
    ordem integer DEFAULT 0 NOT NULL,
    principal boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_produto_imagem IS 'Imagens de produtos';

CREATE SEQUENCE brasil_saas.bc_cad_produto_imagem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_produto_imagem_id_seq OWNED BY brasil_saas.bc_cad_produto_imagem.id;

CREATE TABLE brasil_saas.bc_cad_produto_kit (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    kit_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantidade numeric(15,3) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint
);

COMMENT ON TABLE brasil_saas.bc_cad_produto_kit IS 'Composição de kits';

CREATE SEQUENCE brasil_saas.bc_cad_produto_kit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_produto_kit_id_seq OWNED BY brasil_saas.bc_cad_produto_kit.id;

CREATE TABLE brasil_saas.bc_cad_produto_variacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    sku character varying(50),
    codigo_barras character varying(30),
    preco numeric(15,2),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_produto_variacao IS 'Variações de produto (cor, tamanho...)';

CREATE SEQUENCE brasil_saas.bc_cad_produto_variacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_produto_variacao_id_seq OWNED BY brasil_saas.bc_cad_produto_variacao.id;

CREATE TABLE brasil_saas.bc_cad_servico (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(30) NOT NULL,
    nome character varying(200) NOT NULL,
    descricao text,
    lc116_codigo character varying(10),
    nbs character varying(12),
    aliquota_iss numeric(7,4) DEFAULT 0 NOT NULL,
    preco numeric(15,2) DEFAULT 0 NOT NULL,
    unidade_medida_id bigint,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    codigo_tributacao_municipal character varying(10),
    codigo_tributacao_nacional character varying(6)
);

COMMENT ON TABLE brasil_saas.bc_cad_servico IS 'Serviços ( NFS-e )';

COMMENT ON COLUMN brasil_saas.bc_cad_servico.codigo_tributacao_municipal IS 'Codigo de servico da Tabela de Servicos da Prefeitura de Sao Paulo (4 digitos). Ex.: 2919 = suporte tecnico em informatica (LC 116 01.07). Usado na emissao de NFS-e.';

CREATE SEQUENCE brasil_saas.bc_cad_servico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_servico_id_seq OWNED BY brasil_saas.bc_cad_servico.id;

CREATE TABLE brasil_saas.bc_cad_transportadora (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    codigo character varying(30),
    registro_antt character varying(30),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_transportadora IS 'Transportadoras';

CREATE SEQUENCE brasil_saas.bc_cad_transportadora_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_transportadora_id_seq OWNED BY brasil_saas.bc_cad_transportadora.id;

CREATE TABLE brasil_saas.bc_cad_unidade_medida (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    sigla character varying(10) NOT NULL,
    nome character varying(50) NOT NULL,
    tipo character varying(20) DEFAULT 'UNIDADE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_cad_unidade_medida IS 'Unidades de medida';

CREATE SEQUENCE brasil_saas.bc_cad_unidade_medida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_cad_unidade_medida_id_seq OWNED BY brasil_saas.bc_cad_unidade_medida.id;

CREATE TABLE brasil_saas.bc_cmp_devolucao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    motivo character varying(500) NOT NULL,
    status character varying(20) DEFAULT 'SOLICITADA'::character varying NOT NULL,
    devolvida_em timestamp without time zone,
    observacao character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_cmp_devolucao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cmp_devolucao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_cmp_devolucao_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    devolucao_id bigint NOT NULL,
    produto_id bigint,
    quantidade numeric(15,3) DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_cmp_devolucao_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cmp_devolucao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_com_conferencia_fatura (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    recebimento_id bigint,
    titulo_id bigint,
    valor_pedido numeric(15,2) DEFAULT 0 NOT NULL,
    valor_recebido numeric(15,2) DEFAULT 0 NOT NULL,
    valor_fatura numeric(15,2) DEFAULT 0 NOT NULL,
    tolerancia numeric(7,4) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    divergencia text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    nfe_id bigint
);

COMMENT ON TABLE brasil_saas.bc_com_conferencia_fatura IS 'Conferencia tripla pedido, recebimento e titulo';

COMMENT ON COLUMN brasil_saas.bc_com_conferencia_fatura.nfe_id IS 'NF-e de entrada vinculada à conferência 3-way';

CREATE SEQUENCE brasil_saas.bc_com_conferencia_fatura_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_conferencia_fatura_id_seq OWNED BY brasil_saas.bc_com_conferencia_fatura.id;

CREATE TABLE brasil_saas.bc_com_contrato (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    fornecedor_id bigint NOT NULL,
    numero character varying(30) NOT NULL,
    descricao character varying(255),
    data_inicio date NOT NULL,
    data_fim date,
    valor_limite numeric(15,2),
    valor_utilizado numeric(15,2) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    renovacao_automatica boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_com_contrato IS 'Contratos de fornecimento';

CREATE SEQUENCE brasil_saas.bc_com_contrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_contrato_id_seq OWNED BY brasil_saas.bc_com_contrato.id;

CREATE TABLE brasil_saas.bc_com_cotacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    solicitacao_id bigint,
    numero character varying(20) NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    data_abertura date DEFAULT CURRENT_DATE NOT NULL,
    data_limite date,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_com_cotacao IS 'Cotacoes de compra';

CREATE TABLE brasil_saas.bc_com_cotacao_fornecedor (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    cotacao_id bigint NOT NULL,
    fornecedor_id bigint NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    prazo_entrega integer,
    condicao_pagamento_id bigint,
    frete numeric(15,2) DEFAULT 0,
    desconto numeric(15,2) DEFAULT 0,
    valor_total numeric(15,2) DEFAULT 0,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_com_cotacao_fornecedor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_cotacao_fornecedor_id_seq OWNED BY brasil_saas.bc_com_cotacao_fornecedor.id;

CREATE SEQUENCE brasil_saas.bc_com_cotacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_cotacao_id_seq OWNED BY brasil_saas.bc_com_cotacao.id;

CREATE TABLE brasil_saas.bc_com_cotacao_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    cotacao_fornecedor_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,4) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_com_cotacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_cotacao_item_id_seq OWNED BY brasil_saas.bc_com_cotacao_item.id;

CREATE TABLE brasil_saas.bc_com_pedido (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    fornecedor_id bigint NOT NULL,
    numero character varying(20),
    status character varying(255) DEFAULT 'ABERTO'::character varying NOT NULL,
    condicao_pagamento_id bigint,
    data_emissao date NOT NULL,
    data_previsao_entrega date,
    valor_produtos numeric(15,2) DEFAULT 0 NOT NULL,
    valor_desconto numeric(15,2) DEFAULT 0 NOT NULL,
    valor_frete numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    titulo_id bigint,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_com_pedido IS 'Pedidos de compra';

COMMENT ON COLUMN brasil_saas.bc_com_pedido.status IS 'ABERTO, RECEBIDO ou CANCELADO';

COMMENT ON COLUMN brasil_saas.bc_com_pedido.titulo_id IS 'Título a pagar gerado no financeiro';

CREATE SEQUENCE brasil_saas.bc_com_pedido_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_pedido_id_seq OWNED BY brasil_saas.bc_com_pedido.id;

CREATE TABLE brasil_saas.bc_com_pedido_item (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    numero_item integer NOT NULL,
    produto_id bigint,
    descricao character varying(300),
    quantidade numeric(15,3) NOT NULL,
    unidade character varying(10),
    valor_unitario numeric(15,4) NOT NULL,
    valor_desconto numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    criado_estoque boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    quantidade_recebida numeric(15,3) DEFAULT 0 NOT NULL
);

COMMENT ON COLUMN brasil_saas.bc_com_pedido_item.quantidade_recebida IS 'Quantidade fisicamente recebida e conferida do item do pedido';

CREATE SEQUENCE brasil_saas.bc_com_pedido_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_pedido_item_id_seq OWNED BY brasil_saas.bc_com_pedido_item.id;

CREATE TABLE brasil_saas.bc_com_recebimento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    pedido_id bigint,
    numero character varying(30) NOT NULL,
    data_recebimento date DEFAULT CURRENT_DATE NOT NULL,
    status character varying(20) DEFAULT 'CONFERENCIA'::character varying NOT NULL,
    documento_fornecedor character varying(50),
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_com_recebimento IS 'Recebimentos/conferencia de compras';

COMMENT ON COLUMN brasil_saas.bc_com_recebimento.documento_fornecedor IS 'Numero do documento fiscal ou documento de entrada informado pelo fornecedor';

CREATE SEQUENCE brasil_saas.bc_com_recebimento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_recebimento_id_seq OWNED BY brasil_saas.bc_com_recebimento.id;

CREATE TABLE brasil_saas.bc_com_recebimento_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    recebimento_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade_pedida numeric(15,4) DEFAULT 0 NOT NULL,
    quantidade_recebida numeric(15,4) DEFAULT 0 NOT NULL,
    valor_unitario numeric(15,4) DEFAULT 0 NOT NULL,
    lote_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_com_recebimento_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_recebimento_item_id_seq OWNED BY brasil_saas.bc_com_recebimento_item.id;

CREATE TABLE brasil_saas.bc_com_solicitacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    solicitante_id bigint,
    numero character varying(20) NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    data_solicitacao date DEFAULT CURRENT_DATE NOT NULL,
    data_necessidade date,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_com_solicitacao IS 'Solicitacoes internas de compra';

CREATE SEQUENCE brasil_saas.bc_com_solicitacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_solicitacao_id_seq OWNED BY brasil_saas.bc_com_solicitacao.id;

CREATE TABLE brasil_saas.bc_com_solicitacao_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    solicitacao_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    observacao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_com_solicitacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_com_solicitacao_item_id_seq OWNED BY brasil_saas.bc_com_solicitacao_item.id;

CREATE TABLE brasil_saas.bc_core_auditoria (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    usuario_id bigint,
    tabela character varying(80) NOT NULL,
    registro_id bigint,
    operacao character varying(10) NOT NULL,
    antes jsonb,
    depois jsonb,
    ip character varying(45),
    user_agent character varying(300),
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_core_auditoria IS 'Trilha de auditoria de operações sensíveis';

CREATE SEQUENCE brasil_saas.bc_core_auditoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_auditoria_id_seq OWNED BY brasil_saas.bc_core_auditoria.id;

CREATE TABLE brasil_saas.bc_core_auth_source (
    username character varying(256) NOT NULL,
    source character varying(16) NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT bc_core_auth_source_source_check CHECK (((source)::text = ANY ((ARRAY['AD'::character varying, 'POSTGRES'::character varying, 'LINUX'::character varying])::text[])))
);

CREATE TABLE brasil_saas.bc_core_banco (
    compe character varying(10) NOT NULL,
    ispb character varying(20),
    cnpj character varying(20),
    nome character varying(200) NOT NULL,
    nome_curto character varying(100),
    tipo character varying(60),
    aceita_pix boolean DEFAULT false NOT NULL,
    ativo boolean DEFAULT true NOT NULL
);

CREATE TABLE brasil_saas.bc_core_configuracao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    chave character varying(100) NOT NULL,
    valor text,
    descricao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_core_configuracao IS 'Configurações por empresa (chave/valor)';

CREATE SEQUENCE brasil_saas.bc_core_configuracao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_configuracao_id_seq OWNED BY brasil_saas.bc_core_configuracao.id;

CREATE TABLE brasil_saas.bc_core_empresa (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    razao_social character varying(200) NOT NULL,
    nome_fantasia character varying(200),
    cnpj character varying(14) NOT NULL,
    inscricao_estadual character varying(30),
    inscricao_municipal character varying(30),
    regime_tributario character varying(20) DEFAULT 'SIMPLES_NACIONAL'::character varying NOT NULL,
    codigo_ibge character varying(7),
    endereco character varying(200),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cep character varying(8),
    uf character(2),
    telefone character varying(20),
    email character varying(150),
    logo_url text,
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    logo_tipo_conteudo character varying(50),
    logo_tamanho bigint,
    matriz boolean,
    grupo_cnpj_cpf character varying(20),
    matriz_id bigint,
    tipo_pessoa character varying(20),
    stripe_secret_key_encrypted text,
    stripe_webhook_secret_encrypted text,
    stripe_habilitada boolean DEFAULT false NOT NULL,
    stripe_account_id character varying(64),
    CONSTRAINT ck_matriz_nao_aponta_para_matriz CHECK ((NOT ((matriz IS TRUE) AND (matriz_id IS NOT NULL))))
);

COMMENT ON TABLE brasil_saas.bc_core_empresa IS 'Empresas (raiz do multi-tenant)';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.logo_url IS 'URL do logo da empresa para relatórios, NF-e e OS';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.logo_tipo_conteudo IS 'Tipo MIME do logo da empresa';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.logo_tamanho IS 'Tamanho em bytes do logo da empresa';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.matriz IS 'true quando esta empresa e'' a matriz do grupo. Uma por grupo, garantido pelo indice parcial unico em grupo_cnpj_cpf.';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.grupo_cnpj_cpf IS 'A chave do grupo empresarial: CPF para produtor rural, CNPJ para empresa comum. E'' a coluna que separa uma empresa que a outra.';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.matriz_id IS 'A matriz do grupo desta filial. NULL na propria matriz. A constraint abaixo garante que a empresa apontada e'' de verdade a matriz do mesmo grupo.';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.tipo_pessoa IS 'PF para produtor rural, PJ para empresa. Determina se a chave do grupo e'' CPF ou CNPJ.';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.stripe_secret_key_encrypted IS 'Chave secreta Stripe da empresa, cifrada com APP_SECURITY_SECRET_ENCRYPTION_KEY';

COMMENT ON COLUMN brasil_saas.bc_core_empresa.stripe_webhook_secret_encrypted IS 'Signing secret do webhook Stripe da empresa, cifrado';

CREATE SEQUENCE brasil_saas.bc_core_empresa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_empresa_id_seq OWNED BY brasil_saas.bc_core_empresa.id;

CREATE TABLE brasil_saas.bc_core_empresa_vinculo (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    empresa_vinculada_id bigint NOT NULL,
    tipo character varying(30) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_core_empresa_vinculo IS 'Vínculos entre empresas (matriz/filial, contador, parceira)';

CREATE SEQUENCE brasil_saas.bc_core_empresa_vinculo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_empresa_vinculo_id_seq OWNED BY brasil_saas.bc_core_empresa_vinculo.id;

CREATE TABLE brasil_saas.bc_core_integration_event (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    event_type character varying(80) NOT NULL,
    source_system character varying(40) DEFAULT 'erp'::character varying NOT NULL,
    aggregate_type character varying(80),
    aggregate_id bigint,
    payload text DEFAULT '{}'::text NOT NULL,
    status character varying(16) DEFAULT 'PENDING'::character varying NOT NULL,
    processed_at timestamp without time zone,
    correlation_id uuid,
    CONSTRAINT bc_core_integration_event_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PUBLISHED'::character varying, 'FAILED'::character varying, 'CONSUMED'::character varying])::text[])))
);

CREATE SEQUENCE brasil_saas.bc_core_integration_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_integration_event_id_seq OWNED BY brasil_saas.bc_core_integration_event.id;

CREATE TABLE brasil_saas.bc_core_log_acesso (
    id bigint NOT NULL,
    empresa_id bigint,
    usuario_id bigint,
    username character varying(50),
    ip character varying(45),
    user_agent character varying(300),
    sucesso boolean NOT NULL,
    motivo character varying(100),
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_core_log_acesso IS 'Log de tentativas de login/acesso';

CREATE SEQUENCE brasil_saas.bc_core_log_acesso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_log_acesso_id_seq OWNED BY brasil_saas.bc_core_log_acesso.id;

CREATE TABLE brasil_saas.bc_core_modulo (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    chave character varying(30) NOT NULL,
    nome character varying(100) NOT NULL,
    descricao character varying(255),
    icone character varying(50),
    rota character varying(100),
    ordem integer DEFAULT 100 NOT NULL,
    exige_superuser boolean DEFAULT false NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_core_modulo IS 'Modulos do ERP. A coluna exige_superuser marca os quelidam documento/certificado e exigem SUPERUSER.';

CREATE SEQUENCE brasil_saas.bc_core_modulo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_modulo_id_seq OWNED BY brasil_saas.bc_core_modulo.id;

CREATE TABLE brasil_saas.bc_core_notificacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    titulo character varying(150) NOT NULL,
    mensagem text NOT NULL,
    tipo character varying(30) DEFAULT 'INFO'::character varying NOT NULL,
    lida boolean DEFAULT false NOT NULL,
    lida_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_core_notificacao IS 'Notificações para usuários';

CREATE SEQUENCE brasil_saas.bc_core_notificacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_notificacao_id_seq OWNED BY brasil_saas.bc_core_notificacao.id;

CREATE TABLE brasil_saas.bc_core_perfil (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(50) NOT NULL,
    descricao character varying(255),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    hierarquia_nivel integer,
    perfil_pai_id bigint,
    CONSTRAINT ck_bc_core_perfil_pai_nao_self CHECK (((perfil_pai_id IS NULL) OR (perfil_pai_id <> id)))
);

COMMENT ON TABLE brasil_saas.bc_core_perfil IS 'Perfil de acesso. SUPERUSER opera o gerenciador SQL; enquanto a separacao nao for avaliada, SUPERUSER e ADMIN tem as mesmas permissoes.';

COMMENT ON COLUMN brasil_saas.bc_core_perfil.hierarquia_nivel IS 'Nível hierárquico do perfil (usado para ordenação e permissões em cascata)';

COMMENT ON COLUMN brasil_saas.bc_core_perfil.perfil_pai_id IS 'Perfil pai do qual este perfil herda permissões efetivas';

CREATE SEQUENCE brasil_saas.bc_core_perfil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_perfil_id_seq OWNED BY brasil_saas.bc_core_perfil.id;

CREATE TABLE brasil_saas.bc_core_perfil_permissao (
    id bigint NOT NULL,
    perfil_id bigint NOT NULL,
    permissao_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint
);

COMMENT ON TABLE brasil_saas.bc_core_perfil_permissao IS 'Associação perfil ↔ permissão';

CREATE SEQUENCE brasil_saas.bc_core_perfil_permissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_perfil_permissao_id_seq OWNED BY brasil_saas.bc_core_perfil_permissao.id;

CREATE TABLE brasil_saas.bc_core_permissao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    codigo character varying(80) NOT NULL,
    recurso character varying(50) NOT NULL,
    acao character varying(20) NOT NULL,
    descricao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_core_permissao IS 'Permissões por recurso/ação (ex.: CADASTRO_CLIENTE_CRIAR)';

CREATE SEQUENCE brasil_saas.bc_core_permissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_permissao_id_seq OWNED BY brasil_saas.bc_core_permissao.id;

CREATE TABLE brasil_saas.bc_core_sessao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    usuario_id bigint NOT NULL,
    token_hash character varying(255) NOT NULL,
    refresh_token_hash character varying(255),
    ip character varying(45),
    user_agent character varying(300),
    expira_at timestamp without time zone NOT NULL,
    revogada_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_core_sessao IS 'Sessões JWT ativas/revogadas';

CREATE SEQUENCE brasil_saas.bc_core_sessao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_sessao_id_seq OWNED BY brasil_saas.bc_core_sessao.id;

CREATE TABLE brasil_saas.bc_core_usuario (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint,
    nome character varying(150) NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(150) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    cpf character varying(11),
    telefone character varying(20),
    ativo boolean DEFAULT true NOT NULL,
    mfa_habilitado boolean DEFAULT false NOT NULL,
    mfa_secret character varying(100),
    ultimo_login_at timestamp without time zone,
    tentativas_login integer DEFAULT 0 NOT NULL,
    bloqueado_ate timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    foto_url text,
    foto_tipo_conteudo character varying(50),
    foto_tamanho bigint
);

COMMENT ON TABLE brasil_saas.bc_core_usuario IS 'Usuários do sistema';

COMMENT ON COLUMN brasil_saas.bc_core_usuario.empresa_id IS 'Empresa do usuario. NULL significa que ele ainda nao cadastrou a sua: o sistema so libera o resto depois do cadastro da empresa.';

COMMENT ON COLUMN brasil_saas.bc_core_usuario.senha_hash IS 'BCrypt hash — nunca armazenar senha em texto';

COMMENT ON COLUMN brasil_saas.bc_core_usuario.foto_url IS 'URL da foto do usuário para perfil';

COMMENT ON COLUMN brasil_saas.bc_core_usuario.foto_tipo_conteudo IS 'Tipo MIME da foto do usuário';

COMMENT ON COLUMN brasil_saas.bc_core_usuario.foto_tamanho IS 'Tamanho em bytes da foto do usuário';

CREATE TABLE brasil_saas.bc_core_usuario_empresa (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    perfil_id bigint NOT NULL,
    perfil_nome character varying(50) NOT NULL,
    criado_em timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em timestamp with time zone
);

COMMENT ON TABLE brasil_saas.bc_core_usuario_empresa IS '
    Vinculo usuario x empresa x papel. UNIQUE(usuario_id, perfil_nome) impede a
    mesma pessoa de repetir o mesmo papel em duas empresas. Permite papeis
    diferentes em empresas diferentes. perfil_nome e'' desnormalizado de proposito:
    a constraint precisa do nome, e nao do id do perfil, porque cada empresa tem
    a sua propria linha em bc_core_perfil com o mesmo nome.
';

CREATE TABLE brasil_saas.bc_core_usuario_empresa_backup_20260928 (
    id bigint,
    usuario_id bigint,
    empresa_id bigint,
    perfil_id bigint,
    perfil_nome character varying(50),
    criado_em timestamp with time zone,
    atualizado_em timestamp with time zone
);

CREATE SEQUENCE brasil_saas.bc_core_usuario_empresa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_usuario_empresa_id_seq OWNED BY brasil_saas.bc_core_usuario_empresa.id;

CREATE SEQUENCE brasil_saas.bc_core_usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_usuario_id_seq OWNED BY brasil_saas.bc_core_usuario.id;

CREATE TABLE brasil_saas.bc_core_usuario_modulo (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    modulo_id bigint NOT NULL,
    somente_leitura boolean DEFAULT false NOT NULL,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    criado_por bigint
);

COMMENT ON TABLE brasil_saas.bc_core_usuario_modulo IS 'Modulos liberados por usuario. many-to-many: permite Financeiro + Estoque sem criar perfil novo.';

CREATE SEQUENCE brasil_saas.bc_core_usuario_modulo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_usuario_modulo_id_seq OWNED BY brasil_saas.bc_core_usuario_modulo.id;

CREATE TABLE brasil_saas.bc_core_usuario_perfil (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    perfil_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint
);

COMMENT ON TABLE brasil_saas.bc_core_usuario_perfil IS 'Associação usuário ↔ perfil';

CREATE SEQUENCE brasil_saas.bc_core_usuario_perfil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_core_usuario_perfil_id_seq OWNED BY brasil_saas.bc_core_usuario_perfil.id;

CREATE TABLE brasil_saas.bc_crm_atividade (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    oportunidade_id bigint,
    cliente_id bigint,
    vendedor_id bigint,
    tipo character varying(30) NOT NULL,
    assunto character varying(150) NOT NULL,
    data_agendada timestamp without time zone,
    data_conclusao timestamp without time zone,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    resultado text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_crm_atividade IS 'Atividades comerciais e follow-up';

CREATE SEQUENCE brasil_saas.bc_crm_atividade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_crm_atividade_id_seq OWNED BY brasil_saas.bc_crm_atividade.id;

CREATE TABLE brasil_saas.bc_crm_lead (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(200) NOT NULL,
    empresa_nome character varying(200),
    email character varying(150),
    telefone character varying(20),
    origem character varying(60),
    etapa character varying(30) DEFAULT 'PROSPECCAO'::character varying NOT NULL,
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    valor_estimado numeric(15,2) DEFAULT 0 NOT NULL,
    probabilidade integer DEFAULT 10 NOT NULL,
    responsavel character varying(200),
    data_prev_fechamento date,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_crm_lead ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_crm_lead_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_crm_oportunidade (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    cliente_id bigint,
    vendedor_id bigint,
    titulo character varying(150) NOT NULL,
    etapa character varying(30) DEFAULT 'PROSPECCAO'::character varying NOT NULL,
    origem character varying(50),
    valor_estimado numeric(15,2),
    probabilidade numeric(5,2),
    data_prevista_fechamento date,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_crm_oportunidade IS 'Pipeline comercial/CRM';

CREATE SEQUENCE brasil_saas.bc_crm_oportunidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_crm_oportunidade_id_seq OWNED BY brasil_saas.bc_crm_oportunidade.id;

CREATE TABLE brasil_saas.bc_crm_tarefa (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    lead_id bigint,
    tipo character varying(20) DEFAULT 'TAREFA'::character varying NOT NULL,
    assunto character varying(200) NOT NULL,
    descricao text,
    data_agendada timestamp without time zone,
    concluida boolean DEFAULT false NOT NULL,
    concluida_em timestamp without time zone,
    responsavel character varying(200),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_crm_tarefa ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_crm_tarefa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_ctb_fechamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    periodo character varying(7) NOT NULL,
    status character varying(20) DEFAULT 'FECHADO'::character varying NOT NULL,
    fechado_por bigint,
    fechado_em timestamp without time zone DEFAULT now() NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_ctb_fechamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ctb_fechamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_ctb_lancamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    data date NOT NULL,
    periodo character varying(7) NOT NULL,
    historico character varying(500) NOT NULL,
    origem_tipo character varying(60),
    origem_id bigint,
    status character varying(20) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_ctb_lancamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ctb_lancamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_ctb_partida (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    lancamento_id bigint NOT NULL,
    conta_id bigint NOT NULL,
    centro_custo_id bigint,
    debito numeric(15,2) DEFAULT 0 NOT NULL,
    credito numeric(15,2) DEFAULT 0 NOT NULL,
    historico character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    CONSTRAINT ck_partida_um_lado CHECK ((((debito > (0)::numeric) AND (credito = (0)::numeric)) OR ((credito > (0)::numeric) AND (debito = (0)::numeric))))
);

ALTER TABLE brasil_saas.bc_ctb_partida ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ctb_partida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_dms_aprovacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    documento_id bigint NOT NULL,
    versao integer NOT NULL,
    aprovador character varying(200),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    decidido_em timestamp without time zone,
    decidido_por bigint,
    comentario text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_dms_aprovacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_dms_aprovacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_dms_documento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    titulo character varying(300) NOT NULL,
    categoria character varying(100),
    entidade_tipo character varying(60),
    entidade_id bigint,
    status character varying(20) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    versao_atual integer DEFAULT 1 NOT NULL,
    reter_ate date,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_dms_documento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_dms_documento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_dms_versao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    documento_id bigint NOT NULL,
    versao integer NOT NULL,
    arquivo_nome character varying(300),
    content_type character varying(120),
    tamanho bigint,
    hash character varying(128),
    conteudo_oid oid,
    comentario text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_dms_versao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_dms_versao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_esocial_evento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    funcionario_id bigint,
    payload text,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    recibo character varying(200),
    protocolo character varying(200),
    erro text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_esocial_evento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_esocial_evento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_est_deposito (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    codigo character varying(30) NOT NULL,
    nome character varying(100) NOT NULL,
    tipo character varying(30) DEFAULT 'PADRAO'::character varying NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_est_deposito IS 'Depositos/almoxarifados por empresa';

CREATE SEQUENCE brasil_saas.bc_est_deposito_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_deposito_id_seq OWNED BY brasil_saas.bc_est_deposito.id;

CREATE TABLE brasil_saas.bc_est_endereco (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    deposito_id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    descricao character varying(150),
    tipo character varying(30) DEFAULT 'PULMAO'::character varying NOT NULL,
    capacidade numeric(15,4),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_est_endereco IS 'Enderecamento fisico de estoque';

CREATE SEQUENCE brasil_saas.bc_est_endereco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_endereco_id_seq OWNED BY brasil_saas.bc_est_endereco.id;

CREATE TABLE brasil_saas.bc_est_expedicao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    pedido_venda_id bigint NOT NULL,
    deposito_id bigint NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    transportadora_id bigint,
    codigo_rastreio character varying(120),
    data_abertura timestamp without time zone DEFAULT now() NOT NULL,
    data_separacao timestamp without time zone,
    data_embalagem timestamp without time zone,
    data_expedicao timestamp without time zone,
    observacoes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_est_expedicao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_expedicao_id_seq OWNED BY brasil_saas.bc_est_expedicao.id;

CREATE TABLE brasil_saas.bc_est_expedicao_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    expedicao_id bigint NOT NULL,
    reserva_id bigint,
    produto_id bigint NOT NULL,
    quantidade numeric(15,3) NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    lote_id bigint,
    endereco_id bigint,
    CONSTRAINT bc_est_expedicao_item_quantidade_check CHECK ((quantidade > (0)::numeric))
);

CREATE SEQUENCE brasil_saas.bc_est_expedicao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_expedicao_item_id_seq OWNED BY brasil_saas.bc_est_expedicao_item.id;

CREATE TABLE brasil_saas.bc_est_inventario (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    deposito_id bigint NOT NULL,
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    data_contagem timestamp without time zone DEFAULT now() NOT NULL,
    observacoes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_est_inventario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_inventario_id_seq OWNED BY brasil_saas.bc_est_inventario.id;

CREATE TABLE brasil_saas.bc_est_inventario_item (
    id bigint NOT NULL,
    inventario_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    lote_id bigint,
    endereco_id bigint,
    quantidade_sistema numeric(15,3) DEFAULT 0 NOT NULL,
    quantidade_contada numeric(15,3) DEFAULT 0 NOT NULL,
    diferenca numeric(15,3) DEFAULT 0 NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_est_inventario_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_inventario_item_id_seq OWNED BY brasil_saas.bc_est_inventario_item.id;

CREATE TABLE brasil_saas.bc_est_lote (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    codigo character varying(80) NOT NULL,
    data_fabricacao date,
    data_validade date,
    quantidade numeric(15,4) DEFAULT 0 NOT NULL,
    deposito_id bigint,
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    CONSTRAINT ck_est_lote_quantidade_nao_negativa CHECK ((quantidade >= (0)::numeric))
);

COMMENT ON TABLE brasil_saas.bc_est_lote IS 'Lotes com validade e rastreabilidade';

CREATE SEQUENCE brasil_saas.bc_est_lote_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_lote_id_seq OWNED BY brasil_saas.bc_est_lote.id;

CREATE TABLE brasil_saas.bc_est_movimentacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    tipo character varying(255) NOT NULL,
    origem character varying(255),
    origem_id bigint,
    quantidade numeric(15,3) NOT NULL,
    saldo_apos numeric(15,3),
    data_movimento timestamp without time zone DEFAULT now() NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    deposito_id bigint,
    endereco_id bigint,
    lote_id bigint
);

COMMENT ON TABLE brasil_saas.bc_est_movimentacao IS 'Movimentações de estoque';

COMMENT ON COLUMN brasil_saas.bc_est_movimentacao.tipo IS 'ENTRADA, SAIDA ou AJUSTE';

COMMENT ON COLUMN brasil_saas.bc_est_movimentacao.origem IS 'VENDA, COMPRA ou MANUAL';

CREATE SEQUENCE brasil_saas.bc_est_movimentacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_movimentacao_id_seq OWNED BY brasil_saas.bc_est_movimentacao.id;

CREATE TABLE brasil_saas.bc_est_reserva (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    deposito_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    pedido_venda_id bigint,
    quantidade numeric(15,4) NOT NULL,
    status character varying(20) DEFAULT 'RESERVADA'::character varying NOT NULL,
    data_reserva timestamp without time zone DEFAULT now() NOT NULL,
    data_expiracao timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    lote_id bigint,
    endereco_id bigint
);

COMMENT ON TABLE brasil_saas.bc_est_reserva IS 'Reservas de estoque para operacoes comerciais';

COMMENT ON COLUMN brasil_saas.bc_est_reserva.lote_id IS 'Lote fisico reservado; permite rastreabilidade e separacao por FEFO';

CREATE SEQUENCE brasil_saas.bc_est_reserva_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_reserva_id_seq OWNED BY brasil_saas.bc_est_reserva.id;

CREATE TABLE brasil_saas.bc_est_saldo (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade numeric(15,3) DEFAULT 0 NOT NULL,
    atualizado_em timestamp without time zone DEFAULT now() NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    deleted_at timestamp without time zone,
    deposito_id bigint NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_est_saldo IS 'Saldo de estoque por produto e empresa';

CREATE SEQUENCE brasil_saas.bc_est_saldo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_saldo_id_seq OWNED BY brasil_saas.bc_est_saldo.id;

CREATE TABLE brasil_saas.bc_est_serie (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    numero_serie character varying(100) NOT NULL,
    lote_id bigint,
    deposito_id bigint,
    status character varying(20) DEFAULT 'DISPONIVEL'::character varying NOT NULL,
    data_entrada date,
    data_saida date,
    origem character varying(30),
    origem_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_est_serie IS 'Numeros de serie e rastreabilidade unitaria';

CREATE SEQUENCE brasil_saas.bc_est_serie_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_serie_id_seq OWNED BY brasil_saas.bc_est_serie.id;

CREATE TABLE brasil_saas.bc_est_transferencia (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    deposito_origem_id bigint NOT NULL,
    deposito_destino_id bigint NOT NULL,
    status character varying(20) DEFAULT 'CONCLUIDA'::character varying NOT NULL,
    data_transferencia timestamp without time zone DEFAULT now() NOT NULL,
    observacoes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    tipo character varying(30) DEFAULT 'DEPOSITO'::character varying NOT NULL,
    CONSTRAINT ck_est_transferencia_depositos CHECK ((deposito_origem_id <> deposito_destino_id))
);

CREATE SEQUENCE brasil_saas.bc_est_transferencia_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_transferencia_id_seq OWNED BY brasil_saas.bc_est_transferencia.id;

CREATE TABLE brasil_saas.bc_est_transferencia_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    transferencia_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade numeric(15,3) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    lote_id bigint,
    endereco_origem_id bigint,
    endereco_destino_id bigint,
    CONSTRAINT bc_est_transferencia_item_quantidade_check CHECK ((quantidade > (0)::numeric))
);

CREATE SEQUENCE brasil_saas.bc_est_transferencia_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_est_transferencia_item_id_seq OWNED BY brasil_saas.bc_est_transferencia_item.id;

CREATE TABLE brasil_saas.bc_fin_analise_rentabilidade (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    centro_custo_id bigint,
    periodo character varying(20) NOT NULL,
    receita numeric(15,2) DEFAULT 0.00,
    despesa numeric(15,2) DEFAULT 0.00,
    margem_lucro numeric(15,4) DEFAULT 0.00,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_analise_rentabilidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_analise_rentabilidade_id_seq OWNED BY brasil_saas.bc_fin_analise_rentabilidade.id;

CREATE TABLE brasil_saas.bc_fin_aplicacao_financeira (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    conta_bancaria_id bigint,
    descricao character varying(100) NOT NULL,
    valor_aplicado numeric(15,2) NOT NULL,
    taxa_juros numeric(10,4),
    data_aplicacao date NOT NULL,
    data_resgate date,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_aplicacao_financeira_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_aplicacao_financeira_id_seq OWNED BY brasil_saas.bc_fin_aplicacao_financeira.id;

CREATE TABLE brasil_saas.bc_fin_aprovacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    titulo_id bigint NOT NULL,
    fluxo_aprovacao_id bigint,
    usuario_aprovador_id bigint,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    data_aprovacao timestamp without time zone,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    tipo_documento character varying(30) DEFAULT 'TITULO'::character varying NOT NULL,
    documento_id bigint,
    nivel integer DEFAULT 1 NOT NULL,
    usuario_solicitante_id bigint,
    data_solicitacao timestamp without time zone,
    data_rejeicao timestamp without time zone,
    numero_documento character varying(60)
);

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.status IS 'PENDENTE | APROVADO | REJEITADO | CANCELADO';

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.tipo_documento IS 'Domínio do documento aprovado: TITULO, COMPRA, VENDA etc.';

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.documento_id IS 'Id genérico do documento no domínio (para workflow transversal).';

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.nivel IS 'Nível sequencial dentro do fluxo (aprovação multinível).';

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.numero_documento IS 'Número do documento aprovado (snapshot no momento da solicitação).';

CREATE SEQUENCE brasil_saas.bc_fin_aprovacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_aprovacao_id_seq OWNED BY brasil_saas.bc_fin_aprovacao.id;

CREATE TABLE brasil_saas.bc_fin_baixa (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    titulo_id bigint NOT NULL,
    parcela_id bigint,
    conta_bancaria_id bigint,
    tipo_pagamento_id bigint,
    data_baixa date NOT NULL,
    valor_baixa numeric(15,2) NOT NULL,
    valor_desconto numeric(15,2) DEFAULT 0.00,
    valor_juro numeric(15,2) DEFAULT 0.00,
    valor_multa numeric(15,2) DEFAULT 0.00,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_baixa IS 'Baixas financeiras (pagamentos e recebimentos)';

CREATE SEQUENCE brasil_saas.bc_fin_baixa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_baixa_id_seq OWNED BY brasil_saas.bc_fin_baixa.id;

CREATE TABLE brasil_saas.bc_fin_boleto (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    titulo_id bigint,
    nosso_numero character varying(50),
    numero_documento character varying(50),
    banco character varying(10) NOT NULL,
    agencia character varying(10),
    conta character varying(20),
    digito_agencia character varying(2),
    digito_conta character varying(2),
    valor numeric(15,2) NOT NULL,
    vencimento date NOT NULL,
    nosso_numero_base character varying(50),
    especie_documento character varying(4),
    especie_moeda character varying(4),
    aceite character varying(2),
    codigo_moeda character varying(4),
    uso_boleto character varying(2),
    especie_vencimento character varying(2),
    documento_id character varying(64),
    documento_hash character varying(64),
    status character varying(20) DEFAULT 'EMITIDO'::character varying NOT NULL,
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    atualizado_em timestamp without time zone,
    nosso_numero_chave character varying(50),
    CONSTRAINT ck_fin_boleto_status CHECK (((status)::text = ANY (ARRAY[('EMITIDO'::character varying)::text, ('PAGO'::character varying)::text, ('CANCELADO'::character varying)::text, ('VENCIDO'::character varying)::text]))),
    CONSTRAINT ck_fin_boleto_valor CHECK ((valor > (0)::numeric))
);

COMMENT ON TABLE brasil_saas.bc_fin_boleto IS 'Boleto emitido via boleto_cnab_api. documento_id aponta para a colecao "documentos" do Mongo.';

COMMENT ON COLUMN brasil_saas.bc_fin_boleto.documento_id IS 'Id do documento no Mongo (colecao "documentos"); nao e caminho de arquivo.';

CREATE SEQUENCE brasil_saas.bc_fin_boleto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_boleto_id_seq OWNED BY brasil_saas.bc_fin_boleto.id;

CREATE TABLE brasil_saas.bc_fin_caixa (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    saldo numeric(15,2) DEFAULT 0.00 NOT NULL,
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_caixa IS 'Caixas do modulo financeiro. Tela /financeiro/caixa.';

COMMENT ON COLUMN brasil_saas.bc_fin_caixa.saldo IS 'Saldo atual. Mantido aqui para consulta rapida; a origem do movimento e o extrato.';

CREATE SEQUENCE brasil_saas.bc_fin_caixa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_caixa_id_seq OWNED BY brasil_saas.bc_fin_caixa.id;

CREATE TABLE brasil_saas.bc_fin_centro_custo (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    descricao character varying(255) NOT NULL,
    centro_custo_pai_id bigint,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_centro_custo IS 'Centros de custo para rateio e análise';

CREATE SEQUENCE brasil_saas.bc_fin_centro_custo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_centro_custo_id_seq OWNED BY brasil_saas.bc_fin_centro_custo.id;

CREATE TABLE brasil_saas.bc_fin_comissao (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    deleted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    data_pagamento timestamp(6) without time zone,
    funcionario_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    percentual_comissao numeric(5,2) NOT NULL,
    status character varying(20) NOT NULL,
    valor_comissao numeric(15,2) NOT NULL,
    valor_venda numeric(15,2) NOT NULL
);

ALTER TABLE brasil_saas.bc_fin_comissao ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fin_comissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_fin_conciliacao_bancaria (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    conta_bancaria_id bigint NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    status character varying(20) DEFAULT 'EM_ABERTO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_conciliacao_bancaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_conciliacao_bancaria_id_seq OWNED BY brasil_saas.bc_fin_conciliacao_bancaria.id;

CREATE TABLE brasil_saas.bc_fin_conciliacao_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    conciliacao_id bigint NOT NULL,
    extrato_id bigint NOT NULL,
    baixa_id bigint,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_conciliacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_conciliacao_item_id_seq OWNED BY brasil_saas.bc_fin_conciliacao_item.id;

CREATE TABLE brasil_saas.bc_fin_condicao_pagamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    descricao character varying(100) NOT NULL,
    dias text,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_condicao_pagamento IS 'Condições de pagamento (prazos)';

CREATE SEQUENCE brasil_saas.bc_fin_condicao_pagamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_condicao_pagamento_id_seq OWNED BY brasil_saas.bc_fin_condicao_pagamento.id;

CREATE TABLE brasil_saas.bc_fin_conta_bancaria (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    banco character varying(100) NOT NULL,
    agencia character varying(20) NOT NULL,
    conta character varying(50) NOT NULL,
    digito character varying(5),
    tipo character varying(50) NOT NULL,
    saldo_inicial numeric(15,2) DEFAULT 0.00 NOT NULL,
    ativa boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_conta_bancaria IS 'Contas bancárias da empresa';

CREATE SEQUENCE brasil_saas.bc_fin_conta_bancaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_conta_bancaria_id_seq OWNED BY brasil_saas.bc_fin_conta_bancaria.id;

CREATE TABLE brasil_saas.bc_fin_emprestimo (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    instituicao character varying(100) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    taxa_juros numeric(10,4),
    data_contratacao date NOT NULL,
    data_quitacao date,
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_emprestimo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_emprestimo_id_seq OWNED BY brasil_saas.bc_fin_emprestimo.id;

CREATE TABLE brasil_saas.bc_fin_extrato (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    conta_bancaria_id bigint NOT NULL,
    data_movimento date NOT NULL,
    descricao character varying(255) NOT NULL,
    valor numeric(15,2) NOT NULL,
    tipo character(1) NOT NULL,
    saldo_anterior numeric(15,2),
    saldo_atual numeric(15,2),
    conciliado boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    fitid character varying(100)
);

COMMENT ON TABLE brasil_saas.bc_fin_extrato IS 'Extrato bancário importado ou manual';

CREATE SEQUENCE brasil_saas.bc_fin_extrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_extrato_id_seq OWNED BY brasil_saas.bc_fin_extrato.id;

CREATE TABLE brasil_saas.bc_fin_fluxo_aprovacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    descricao character varying(100) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_fluxo_aprovacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_fluxo_aprovacao_id_seq OWNED BY brasil_saas.bc_fin_fluxo_aprovacao.id;

CREATE TABLE brasil_saas.bc_fin_integracao_bancaria (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    conta_bancaria_id bigint NOT NULL,
    provedor character varying(50) NOT NULL,
    configuracao_json text,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_integracao_bancaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_integracao_bancaria_id_seq OWNED BY brasil_saas.bc_fin_integracao_bancaria.id;

CREATE TABLE brasil_saas.bc_fin_lancamento_contabil (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    data_lancamento date NOT NULL,
    periodo_contabil_id bigint,
    descricao_historico character varying(255) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    origem character varying(50),
    id_origem bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_lancamento_contabil IS 'Lançamentos contábeis (cabeçalho)';

CREATE SEQUENCE brasil_saas.bc_fin_lancamento_contabil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_lancamento_contabil_id_seq OWNED BY brasil_saas.bc_fin_lancamento_contabil.id;

CREATE TABLE brasil_saas.bc_fin_lancamento_partida (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    lancamento_id bigint NOT NULL,
    plano_contas_id bigint NOT NULL,
    centro_custo_id bigint,
    tipo character(1) NOT NULL,
    valor numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_lancamento_partida IS 'Partidas dobradas (débito e crédito)';

CREATE SEQUENCE brasil_saas.bc_fin_lancamento_partida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_lancamento_partida_id_seq OWNED BY brasil_saas.bc_fin_lancamento_partida.id;

CREATE TABLE brasil_saas.bc_fin_orcamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    ano integer NOT NULL,
    centro_custo_id bigint,
    plano_contas_id bigint,
    valor_orcado numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_orcamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_orcamento_id_seq OWNED BY brasil_saas.bc_fin_orcamento.id;

CREATE TABLE brasil_saas.bc_fin_orcamento_realizado (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    orcamento_id bigint NOT NULL,
    mes integer NOT NULL,
    valor_realizado numeric(15,2) DEFAULT 0.00 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_orcamento_realizado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_orcamento_realizado_id_seq OWNED BY brasil_saas.bc_fin_orcamento_realizado.id;

CREATE TABLE brasil_saas.bc_fin_periodo_contabil (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_periodo_contabil IS 'Períodos contábeis para fechamento';

CREATE SEQUENCE brasil_saas.bc_fin_periodo_contabil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_periodo_contabil_id_seq OWNED BY brasil_saas.bc_fin_periodo_contabil.id;

CREATE TABLE brasil_saas.bc_fin_plano_contas (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    descricao character varying(255) NOT NULL,
    tipo character(1) NOT NULL,
    natureza character(1) NOT NULL,
    conta_pai_id bigint,
    nivel integer DEFAULT 1 NOT NULL,
    ativa boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_plano_contas IS 'Plano de contas contábil';

CREATE SEQUENCE brasil_saas.bc_fin_plano_contas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_plano_contas_id_seq OWNED BY brasil_saas.bc_fin_plano_contas.id;

CREATE TABLE brasil_saas.bc_fin_projecao_fluxo_caixa (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    data_projecao date NOT NULL,
    valor_previsto_entrada numeric(15,2) DEFAULT 0.00,
    valor_previsto_saida numeric(15,2) DEFAULT 0.00,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq OWNED BY brasil_saas.bc_fin_projecao_fluxo_caixa.id;

CREATE TABLE brasil_saas.bc_fin_provisao_pdd (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    data_provisao date NOT NULL,
    valor_provisao numeric(15,2) NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_provisao_pdd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_provisao_pdd_id_seq OWNED BY brasil_saas.bc_fin_provisao_pdd.id;

CREATE TABLE brasil_saas.bc_fin_remessa (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    conta_bancaria_id bigint,
    banco character varying(10) NOT NULL,
    tipo character varying(10) DEFAULT 'cnab240'::character varying NOT NULL,
    numero_sequencial character varying(20),
    data_geracao date DEFAULT CURRENT_DATE NOT NULL,
    data_credito date,
    qtde_titulos integer DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'GERADA'::character varying NOT NULL,
    documento_id character varying(64),
    documento_hash character varying(64),
    nome_arquivo character varying(255),
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    atualizado_em timestamp without time zone,
    CONSTRAINT ck_fin_remessa_status CHECK (((status)::text = ANY (ARRAY[('GERADA'::character varying)::text, ('ENVIADA'::character varying)::text, ('PROCESSADA'::character varying)::text, ('REJEITADA'::character varying)::text]))),
    CONSTRAINT ck_fin_remessa_tipo CHECK (((tipo)::text = ANY (ARRAY[('cnab240'::character varying)::text, ('cnab400'::character varying)::text])))
);

COMMENT ON TABLE brasil_saas.bc_fin_remessa IS 'Remessa CNAB gerada pelo boleto_cnab_api e enviada ao banco.';

CREATE SEQUENCE brasil_saas.bc_fin_remessa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_remessa_id_seq OWNED BY brasil_saas.bc_fin_remessa.id;

CREATE TABLE brasil_saas.bc_fin_remessa_item (
    id bigint NOT NULL,
    remessa_id bigint NOT NULL,
    titulo_id bigint,
    boleto_id bigint,
    nosso_numero character varying(50),
    valor numeric(15,2) NOT NULL,
    vencimento date,
    codigo_ocorrencia character varying(4),
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    documento_sacado character varying(20),
    nome_sacado character varying(200),
    endereco_sacado character varying(200),
    bairro_sacado character varying(120),
    cep_sacado character varying(10),
    cidade_sacado character varying(120),
    uf_sacado character varying(2)
);

CREATE SEQUENCE brasil_saas.bc_fin_remessa_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_remessa_item_id_seq OWNED BY brasil_saas.bc_fin_remessa_item.id;

CREATE TABLE brasil_saas.bc_fin_renegociacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    titulo_original_id bigint NOT NULL,
    novo_titulo_id bigint,
    data_renegociacao date NOT NULL,
    valor_acrescimo numeric(15,2) DEFAULT 0.00,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_renegociacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_renegociacao_id_seq OWNED BY brasil_saas.bc_fin_renegociacao.id;

CREATE TABLE brasil_saas.bc_fin_retorno_bancario (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    conta_bancaria_id bigint,
    banco character varying(10) NOT NULL,
    tipo character varying(10) DEFAULT 'cnab240'::character varying NOT NULL,
    nome_arquivo character varying(255),
    data_arquivo date,
    qtde_registros integer DEFAULT 0 NOT NULL,
    qtde_baixados integer DEFAULT 0 NOT NULL,
    qtde_divergentes integer DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'PROCESSADO'::character varying NOT NULL,
    documento_id character varying(64),
    resumo_json text,
    erro text,
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT ck_fin_retorno_status CHECK (((status)::text = ANY (ARRAY[('PROCESSADO'::character varying)::text, ('PARCIAL'::character varying)::text, ('ERRO'::character varying)::text]))),
    CONSTRAINT ck_fin_retorno_tipo CHECK (((tipo)::text = ANY (ARRAY[('cnab240'::character varying)::text, ('cnab400'::character varying)::text])))
);

CREATE SEQUENCE brasil_saas.bc_fin_retorno_bancario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_retorno_bancario_id_seq OWNED BY brasil_saas.bc_fin_retorno_bancario.id;

CREATE TABLE brasil_saas.bc_fin_retorno_item (
    id bigint NOT NULL,
    retorno_id bigint NOT NULL,
    titulo_id bigint,
    codigo_registro character varying(4),
    codigo_ocorrencia character varying(4),
    nosso_numero character varying(50),
    valor_titulo numeric(15,2),
    valor_recebido numeric(15,2),
    juros numeric(15,2),
    multa numeric(15,2),
    desconto numeric(15,2),
    data_credito date,
    data_ocorrencia date,
    aplicado boolean DEFAULT false NOT NULL,
    observacao text,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    boleto_id bigint
);

CREATE SEQUENCE brasil_saas.bc_fin_retorno_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_retorno_item_id_seq OWNED BY brasil_saas.bc_fin_retorno_item.id;

CREATE TABLE brasil_saas.bc_fin_stripe_customer (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    stripe_customer_id character varying(64) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_stripe_customer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_stripe_customer_id_seq OWNED BY brasil_saas.bc_fin_stripe_customer.id;

CREATE TABLE brasil_saas.bc_fin_stripe_payment (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    titulo_id bigint NOT NULL,
    stripe_customer_id character varying(64),
    checkout_session_id character varying(128),
    payment_intent_id character varying(128),
    invoice_id character varying(128),
    amount bigint NOT NULL,
    currency character varying(3) NOT NULL,
    status character varying(40) NOT NULL,
    checkout_url text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_stripe_payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_stripe_payment_id_seq OWNED BY brasil_saas.bc_fin_stripe_payment.id;

CREATE TABLE brasil_saas.bc_fin_stripe_webhook_event (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    stripe_event_id character varying(128) NOT NULL,
    event_type character varying(128) NOT NULL,
    processed_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_fin_stripe_webhook_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_stripe_webhook_event_id_seq OWNED BY brasil_saas.bc_fin_stripe_webhook_event.id;

CREATE TABLE brasil_saas.bc_fin_tipo_pagamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    descricao character varying(100) NOT NULL,
    codigo character varying(20),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_tipo_pagamento IS 'Tipos/Meios de pagamento';

CREATE SEQUENCE brasil_saas.bc_fin_tipo_pagamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_tipo_pagamento_id_seq OWNED BY brasil_saas.bc_fin_tipo_pagamento.id;

CREATE TABLE brasil_saas.bc_fin_titulo (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    tipo character(1) NOT NULL,
    numero_documento character varying(100),
    descricao character varying(255) NOT NULL,
    pessoa_id bigint,
    valor_original numeric(15,2) NOT NULL,
    valor_saldo numeric(15,2) NOT NULL,
    data_emissao date NOT NULL,
    data_vencimento date NOT NULL,
    status character varying(255) DEFAULT 'ABERTO'::character varying NOT NULL,
    centro_custo_id bigint,
    plano_contas_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_titulo IS 'Títulos a pagar e a receber';

COMMENT ON COLUMN brasil_saas.bc_fin_titulo.status IS 'ABERTO | PENDENTE_APROVACAO | BAIXADO | CANCELADO (demais estados por regra de negócio)';

CREATE SEQUENCE brasil_saas.bc_fin_titulo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_titulo_id_seq OWNED BY brasil_saas.bc_fin_titulo.id;

CREATE TABLE brasil_saas.bc_fin_titulo_parcela (
    id bigint NOT NULL,
    uuid uuid DEFAULT brasil_saas.uuid_generate_v4(),
    empresa_id bigint NOT NULL,
    titulo_id bigint NOT NULL,
    numero_parcela integer NOT NULL,
    valor_parcela numeric(15,2) NOT NULL,
    valor_saldo numeric(15,2) NOT NULL,
    data_vencimento date NOT NULL,
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fin_titulo_parcela IS 'Parcelas dos títulos';

CREATE SEQUENCE brasil_saas.bc_fin_titulo_parcela_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fin_titulo_parcela_id_seq OWNED BY brasil_saas.bc_fin_titulo_parcela.id;

CREATE TABLE brasil_saas.bc_fis_apuracao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    imposto_id bigint NOT NULL,
    competencia character varying(7) NOT NULL,
    base_calculo numeric(15,2) DEFAULT 0 NOT NULL,
    valor_devido numeric(15,2) DEFAULT 0 NOT NULL,
    valor_credito numeric(15,2) DEFAULT 0 NOT NULL,
    valor_pagar numeric(15,2) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    apurada_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    data_encerramento date,
    imposto character varying(10) NOT NULL,
    valor_base_calculo numeric(15,2),
    valor_imposto numeric(15,2),
    valor_debito numeric(15,2),
    valor_recolher numeric(15,2)
);

COMMENT ON TABLE brasil_saas.bc_fis_apuracao IS 'Apurações mensais de impostos';

CREATE SEQUENCE brasil_saas.bc_fis_apuracao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_apuracao_id_seq OWNED BY brasil_saas.bc_fis_apuracao.id;

CREATE TABLE brasil_saas.bc_fis_certificado_digital (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    tipo character varying(10) DEFAULT 'A1'::character varying NOT NULL,
    arquivo_url character varying(500),
    cnpj_titular character varying(14),
    emissao_at timestamp without time zone,
    validade_at timestamp without time zone NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    arquivo character varying(200),
    cnpj character varying(14),
    razao_social character varying(200),
    senha character varying(255)
);

COMMENT ON TABLE brasil_saas.bc_fis_certificado_digital IS 'Certificados digitais (A1/A3)';

COMMENT ON COLUMN brasil_saas.bc_fis_certificado_digital.arquivo_url IS 'Referência MinIO — nunca versionar o arquivo em si';

CREATE SEQUENCE brasil_saas.bc_fis_certificado_digital_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_certificado_digital_id_seq OWNED BY brasil_saas.bc_fis_certificado_digital.id;

CREATE TABLE brasil_saas.bc_fis_cest (
    id bigint NOT NULL,
    codigo character varying(7) NOT NULL,
    ncm character varying(8),
    descricao text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_cest IS 'CEST — Código Especificador da Substituição Tributária (oficial)';

CREATE SEQUENCE brasil_saas.bc_fis_cest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_cest_id_seq OWNED BY brasil_saas.bc_fis_cest.id;

CREATE TABLE brasil_saas.bc_fis_cfop (
    id bigint NOT NULL,
    codigo character varying(4) NOT NULL,
    descricao text NOT NULL,
    tipo character varying(20),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_cfop IS 'CFOP — Código Fiscal de Operações e Prestações (oficial)';

CREATE SEQUENCE brasil_saas.bc_fis_cfop_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_cfop_id_seq OWNED BY brasil_saas.bc_fis_cfop.id;

CREATE TABLE brasil_saas.bc_fis_cnae_servico (
    id bigint NOT NULL,
    codigo character varying(10) NOT NULL,
    descricao text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    lc116_codigo character varying(12)
);

COMMENT ON TABLE brasil_saas.bc_fis_cnae_servico IS 'CNAE de serviços (oficial IBGE)';

CREATE SEQUENCE brasil_saas.bc_fis_cnae_servico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_cnae_servico_id_seq OWNED BY brasil_saas.bc_fis_cnae_servico.id;

CREATE TABLE brasil_saas.bc_fis_cte (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint,
    numero bigint,
    serie character varying(10),
    chave_acesso character varying(50),
    data_emissao timestamp without time zone,
    status character varying(20) DEFAULT 'DIGITADA'::character varying NOT NULL,
    valor_carga numeric(15,2) DEFAULT 0 NOT NULL,
    valor_frete numeric(15,2) DEFAULT 0 NOT NULL,
    xml text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    tipo_operacao character(1) DEFAULT 'S'::bpchar NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_fis_cte IS 'Conhecimentos de transporte eletrônicos (modelo 57)';

COMMENT ON COLUMN brasil_saas.bc_fis_cte.tipo_operacao IS 'E=entrada, S=saída';

CREATE SEQUENCE brasil_saas.bc_fis_cte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_cte_id_seq OWNED BY brasil_saas.bc_fis_cte.id;

CREATE TABLE brasil_saas.bc_fis_cte_item (
    id bigint NOT NULL,
    cte_id bigint NOT NULL,
    descricao text,
    quantidade numeric(15,4),
    valor numeric(15,2),
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_fis_cte_item IS 'Cargas/itens do CT-e';

CREATE SEQUENCE brasil_saas.bc_fis_cte_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_cte_item_id_seq OWNED BY brasil_saas.bc_fis_cte_item.id;

CREATE TABLE brasil_saas.bc_fis_ecd (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    exercicio character varying(4) NOT NULL,
    arquivo_url character varying(500),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_ecd IS 'ECD — Escrituração Contábil Digital';

CREATE SEQUENCE brasil_saas.bc_fis_ecd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_ecd_id_seq OWNED BY brasil_saas.bc_fis_ecd.id;

CREATE TABLE brasil_saas.bc_fis_ecf (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    exercicio character varying(4) NOT NULL,
    arquivo_url character varying(500),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_ecf IS 'ECF — Escrituração Contábil Fiscal';

CREATE SEQUENCE brasil_saas.bc_fis_ecf_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_ecf_id_seq OWNED BY brasil_saas.bc_fis_ecf.id;

CREATE TABLE brasil_saas.bc_fis_esocial (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia character varying(7) NOT NULL,
    evento character varying(20),
    arquivo_url character varying(500),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_esocial IS 'Eventos de eSocial';

CREATE SEQUENCE brasil_saas.bc_fis_esocial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_esocial_id_seq OWNED BY brasil_saas.bc_fis_esocial.id;

CREATE TABLE brasil_saas.bc_fis_imposto (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    sigla character varying(10) NOT NULL,
    nome character varying(100) NOT NULL,
    tipo character varying(30) NOT NULL,
    aliquota_padrao numeric(7,4),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_imposto IS 'Impostos configurados por empresa';

CREATE SEQUENCE brasil_saas.bc_fis_imposto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_imposto_id_seq OWNED BY brasil_saas.bc_fis_imposto.id;

CREATE TABLE brasil_saas.bc_fis_issqn (
    id bigint NOT NULL,
    codigo character varying(20) NOT NULL,
    descricao text NOT NULL,
    aliquota numeric(7,4),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    cod_ibge character varying(7),
    uf character(2),
    municipio character varying(150),
    codigo_municipal character varying(20),
    vigencia date
);

COMMENT ON TABLE brasil_saas.bc_fis_issqn IS 'Códigos de tributação municipal de ISSQN';

CREATE SEQUENCE brasil_saas.bc_fis_issqn_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_issqn_id_seq OWNED BY brasil_saas.bc_fis_issqn.id;

CREATE TABLE brasil_saas.bc_fis_manifestacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    chave_acesso character varying(50) NOT NULL,
    tipo character varying(30) NOT NULL,
    justificativa character varying(255),
    protocolo character varying(50),
    data_evento timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_manifestacao IS 'Manifestação do destinatário';

CREATE SEQUENCE brasil_saas.bc_fis_manifestacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_manifestacao_id_seq OWNED BY brasil_saas.bc_fis_manifestacao.id;

CREATE TABLE brasil_saas.bc_fis_mdfe (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero bigint,
    serie character varying(10),
    chave_acesso character varying(50),
    data_emissao timestamp without time zone,
    status character varying(20) DEFAULT 'DIGITADA'::character varying NOT NULL,
    uf_inicio character(2),
    uf_fim character(2),
    xml text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_mdfe IS 'Manifestos eletrônicos de documentos fiscais (modelo 58)';

CREATE SEQUENCE brasil_saas.bc_fis_mdfe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_mdfe_id_seq OWNED BY brasil_saas.bc_fis_mdfe.id;

CREATE TABLE brasil_saas.bc_fis_nbs (
    id bigint NOT NULL,
    codigo character varying(12) NOT NULL,
    descricao text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_nbs IS 'NBS — Nomenclatura Brasileira de Serviços (oficial)';

CREATE SEQUENCE brasil_saas.bc_fis_nbs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nbs_id_seq OWNED BY brasil_saas.bc_fis_nbs.id;

CREATE TABLE brasil_saas.bc_fis_ncm (
    id bigint NOT NULL,
    codigo character varying(8) NOT NULL,
    descricao text NOT NULL,
    aliquota_ipi numeric(7,4),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_ncm IS 'NCM — Nomenclatura Comum do Mercosul (oficial)';

CREATE SEQUENCE brasil_saas.bc_fis_ncm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_ncm_id_seq OWNED BY brasil_saas.bc_fis_ncm.id;

CREATE TABLE brasil_saas.bc_fis_nfce (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint,
    numero bigint,
    serie character varying(10),
    chave_acesso character varying(50),
    data_emissao timestamp without time zone,
    status character varying(20) DEFAULT 'DIGITADA'::character varying NOT NULL,
    protocolo character varying(50),
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    xml text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_nfce IS 'Notas fiscais de consumidor eletrônicas (modelo 65)';

CREATE SEQUENCE brasil_saas.bc_fis_nfce_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfce_id_seq OWNED BY brasil_saas.bc_fis_nfce.id;

CREATE TABLE brasil_saas.bc_fis_nfce_item (
    id bigint NOT NULL,
    nfce_id bigint NOT NULL,
    produto_id bigint,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,4) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_fis_nfce_item IS 'Itens da NFC-e';

CREATE SEQUENCE brasil_saas.bc_fis_nfce_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfce_item_id_seq OWNED BY brasil_saas.bc_fis_nfce_item.id;

CREATE TABLE brasil_saas.bc_fis_nfe (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    cliente_id bigint,
    pessoa_id bigint,
    numero bigint,
    serie character varying(10),
    chave_acesso character varying(50),
    natureza_operacao character varying(100),
    cfop character varying(4),
    data_emissao timestamp without time zone,
    data_saida timestamp without time zone,
    status character varying(20) DEFAULT 'DIGITADA'::character varying NOT NULL,
    protocolo character varying(50),
    xml text,
    danfe_url character varying(500),
    valor_produtos numeric(15,2) DEFAULT 0 NOT NULL,
    valor_frete numeric(15,2) DEFAULT 0 NOT NULL,
    valor_desconto numeric(15,2) DEFAULT 0 NOT NULL,
    valor_icms numeric(15,2) DEFAULT 0 NOT NULL,
    valor_ipi numeric(15,2) DEFAULT 0 NOT NULL,
    valor_pis numeric(15,2) DEFAULT 0 NOT NULL,
    valor_cofins numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    tipo_operacao character(1) DEFAULT 'S'::bpchar NOT NULL,
    pedido_venda_id bigint,
    pedido_compra_id bigint,
    documento_origem_tipo character varying(30),
    documento_origem_id bigint
);

COMMENT ON TABLE brasil_saas.bc_fis_nfe IS 'Notas fiscais eletrônicas (modelo 55)';

COMMENT ON COLUMN brasil_saas.bc_fis_nfe.tipo_operacao IS 'E=entrada, S=saída';

COMMENT ON COLUMN brasil_saas.bc_fis_nfe.documento_origem_tipo IS 'VENDA, COMPRA, SERVICO ou OUTRO';

CREATE TABLE brasil_saas.bc_fis_nfe_evento (
    id bigint NOT NULL,
    nfe_id bigint NOT NULL,
    tipo character varying(30) NOT NULL,
    sequencia integer DEFAULT 1 NOT NULL,
    protocolo character varying(50),
    data_evento timestamp without time zone,
    xml text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint
);

COMMENT ON TABLE brasil_saas.bc_fis_nfe_evento IS 'Eventos de NF-e (cancelamento, carta de correção...)';

CREATE SEQUENCE brasil_saas.bc_fis_nfe_evento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfe_evento_id_seq OWNED BY brasil_saas.bc_fis_nfe_evento.id;

CREATE SEQUENCE brasil_saas.bc_fis_nfe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfe_id_seq OWNED BY brasil_saas.bc_fis_nfe.id;

CREATE TABLE brasil_saas.bc_fis_nfe_item (
    id bigint NOT NULL,
    nfe_id bigint NOT NULL,
    produto_id bigint,
    numero_item integer NOT NULL,
    ncm character varying(8),
    cfop character varying(4),
    cest character varying(7),
    quantidade numeric(15,4) NOT NULL,
    unidade character varying(10),
    valor_unitario numeric(15,4) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    aliquota_icms numeric(7,4),
    valor_icms numeric(15,2),
    aliquota_ipi numeric(7,4),
    valor_ipi numeric(15,2),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    codigo_produto character varying(60),
    codigo_barras character varying(20),
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_nfe_item IS 'Itens da NF-e';

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.codigo_produto IS 'cProd: codigo do produto no fornecedor. Nao e o codigo interno do ERP.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.codigo_barras IS 'cEAN do item, so quando o emissor escreveu digitos. Texto como "SEM GTIN" vira NULL.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.uuid IS 'Chave publica do item. NULL so em linha criada antes da V103.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.deleted_at IS 'Item corrigido nao se apaga: a nota e documento fiscal e o historico precisa ficar.';

CREATE SEQUENCE brasil_saas.bc_fis_nfe_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfe_item_id_seq OWNED BY brasil_saas.bc_fis_nfe_item.id;

CREATE TABLE brasil_saas.bc_fis_nfse (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    cliente_id bigint,
    pessoa_id bigint,
    servico_id bigint,
    numero bigint,
    codigo_verificacao character varying(50),
    lc116_codigo character varying(10),
    codigo_tributacao_municipal character varying(20),
    data_emissao timestamp without time zone,
    status character varying(20) DEFAULT 'DIGITADA'::character varying NOT NULL,
    xml text,
    pdf_url character varying(500),
    base_calculo numeric(15,2) DEFAULT 0 NOT NULL,
    aliquota_iss numeric(7,4) DEFAULT 0 NOT NULL,
    valor_iss numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    tipo_operacao character(1) DEFAULT 'S'::bpchar NOT NULL,
    xml_documento_id character varying(50),
    pdf_documento_id character varying(50),
    chave_nota_nacional character varying(50),
    serie_rps character varying(10),
    numero_rps character varying(20),
    id_dps character varying(50),
    protocolo_nacional character varying(100)
);

COMMENT ON TABLE brasil_saas.bc_fis_nfse IS 'Notas fiscais de serviço eletrônicas';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.tipo_operacao IS 'E=entrada, S=saída';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.xml_documento_id IS 'Id do documento no MongoDB (tipoEntidade nfse_xml). Guarda 5 anos.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.pdf_documento_id IS 'Id do documento no MongoDB (tipoEntidade nfse_pdf). Expira em 60 dias.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.chave_nota_nacional IS 'Chave da nota nacional devolvida pela prefeitura (44 digitos).';

CREATE SEQUENCE brasil_saas.bc_fis_nfse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfse_id_seq OWNED BY brasil_saas.bc_fis_nfse.id;

CREATE TABLE brasil_saas.bc_fis_nfse_item (
    id bigint NOT NULL,
    nfse_id bigint NOT NULL,
    servico_id bigint,
    descricao text,
    quantidade numeric(15,4) DEFAULT 1 NOT NULL,
    valor_unitario numeric(15,4) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_fis_nfse_item IS 'Itens da NFS-e';

CREATE SEQUENCE brasil_saas.bc_fis_nfse_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfse_item_id_seq OWNED BY brasil_saas.bc_fis_nfse_item.id;

CREATE TABLE brasil_saas.bc_fis_nfse_retorno (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nfse_id bigint,
    operacao character varying(20) NOT NULL,
    sucesso boolean,
    http_status integer,
    duracao_ms bigint,
    mensagem text,
    codigo character varying(20),
    alertas text,
    documento_id character varying(50),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);

COMMENT ON TABLE brasil_saas.bc_fis_nfse_retorno IS 'Uma linha por chamada a prefeitura: emissao, cancelamento, consulta. O JSON bruto da resposta fica no Mongo (documento_id); o que da para consultar fica aqui. E o registro que sobrevive ao fechamento da tela de erro.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.sucesso IS 'true aceitou; false recusou (com mensagem); NULL nao deu para saber — unico caso em que reemitir e perigoso.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.mensagem IS 'Texto da prefeitura, literal. Sobrevive ao fechamento da tela.';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.codigo IS 'Codigo de erro da prefeitura, extraido da mensagem (ex.: 1001).';

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.documento_id IS 'Id do documento no MongoDB (tipoEntidade nfse_retorno). Guarda o JSON bruto da resposta.';

CREATE SEQUENCE brasil_saas.bc_fis_nfse_retorno_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_nfse_retorno_id_seq OWNED BY brasil_saas.bc_fis_nfse_retorno.id;

CREATE TABLE brasil_saas.bc_fis_obrigacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(200) NOT NULL,
    orgao character varying(60),
    periodicidade character varying(20) DEFAULT 'MENSAL'::character varying NOT NULL,
    dia_vencimento integer DEFAULT 20 NOT NULL,
    descricao text,
    ativa boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE TABLE brasil_saas.bc_fis_obrigacao_entrega (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    obrigacao_id bigint NOT NULL,
    competencia character varying(7) NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    entregue_em timestamp without time zone,
    protocolo character varying(200),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_fis_obrigacao_entrega ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fis_obrigacao_entrega_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

ALTER TABLE brasil_saas.bc_fis_obrigacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fis_obrigacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_fis_palavra_chave (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    tabela character varying(16) NOT NULL,
    codigo character varying(16) NOT NULL,
    palavra character varying(80) NOT NULL,
    peso smallint DEFAULT 5 NOT NULL,
    origem character varying(64),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    CONSTRAINT ck_palavra_tabela CHECK (((tabela)::text = ANY ((ARRAY['ncm'::character varying, 'issqn'::character varying, 'cfop'::character varying])::text[])))
);

COMMENT ON TABLE brasil_saas.bc_fis_palavra_chave IS 'Índice de busca do cadastro fiscal. Nível 1: token da descrição oficial. Nível 2: sinônimo curado, com origem declarada.';

CREATE SEQUENCE brasil_saas.bc_fis_palavra_chave_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_palavra_chave_id_seq OWNED BY brasil_saas.bc_fis_palavra_chave.id;

CREATE TABLE brasil_saas.bc_fis_regra_tributaria (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    ncm character varying(8),
    cfop character varying(4),
    uf_origem character(2),
    uf_destino character(2),
    cst_icms character varying(5),
    aliquota_icms numeric(7,4),
    cst_ipi character varying(5),
    aliquota_ipi numeric(7,4),
    cst_pis character varying(5),
    aliquota_pis numeric(7,4),
    cst_cofins character varying(5),
    aliquota_cofins numeric(7,4),
    aliquota_st numeric(7,4),
    ativa boolean DEFAULT true NOT NULL,
    prioridade integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    origem character varying(64),
    origem_referencia character varying(180)
);

COMMENT ON TABLE brasil_saas.bc_fis_regra_tributaria IS 'Regras de tributação por NCM/CFOP/UF';

COMMENT ON COLUMN brasil_saas.bc_fis_regra_tributaria.origem IS 'Procedência da regra: identificador da fonte. NUNCA confiar linhas sem este valor.';

COMMENT ON COLUMN brasil_saas.bc_fis_regra_tributaria.origem_referencia IS 'Arquivo e tag de origem, para auditar a carga.';

CREATE SEQUENCE brasil_saas.bc_fis_regra_tributaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_regra_tributaria_id_seq OWNED BY brasil_saas.bc_fis_regra_tributaria.id;

CREATE TABLE brasil_saas.bc_fis_reinf (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia character varying(7) NOT NULL,
    evento character varying(20),
    arquivo_url character varying(500),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_reinf IS 'EFD-REINF';

CREATE SEQUENCE brasil_saas.bc_fis_reinf_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_reinf_id_seq OWNED BY brasil_saas.bc_fis_reinf.id;

CREATE TABLE brasil_saas.bc_fis_servico_lc116 (
    id bigint NOT NULL,
    codigo character varying(10) NOT NULL,
    descricao text NOT NULL,
    aliquota_min numeric(7,4),
    aliquota_max numeric(7,4),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_servico_lc116 IS 'Itens da Lista de Serviços da LC 116/2003 (oficial)';

CREATE SEQUENCE brasil_saas.bc_fis_servico_lc116_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_servico_lc116_id_seq OWNED BY brasil_saas.bc_fis_servico_lc116.id;

CREATE TABLE brasil_saas.bc_fis_sped_contribuicoes (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia character varying(7) NOT NULL,
    arquivo_url character varying(500),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    gerado_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_sped_contribuicoes IS 'Gerações de SPED Contribuições (PIS/COFINS)';

CREATE SEQUENCE brasil_saas.bc_fis_sped_contribuicoes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_sped_contribuicoes_id_seq OWNED BY brasil_saas.bc_fis_sped_contribuicoes.id;

CREATE TABLE brasil_saas.bc_fis_sped_fiscal (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia character varying(7) NOT NULL,
    arquivo_url character varying(500),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    gerado_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_fis_sped_fiscal IS 'Gerações de SPED Fiscal (ICMS/IPI)';

CREATE SEQUENCE brasil_saas.bc_fis_sped_fiscal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_fis_sped_fiscal_id_seq OWNED BY brasil_saas.bc_fis_sped_fiscal.id;

CREATE TABLE brasil_saas.bc_ia_analise_preditiva (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    tipo character varying(50),
    entidade character varying(50),
    entidade_id bigint,
    periodo character varying(20),
    dias_previsao integer DEFAULT 30,
    valor_atual numeric(15,2),
    valor_previsto numeric(15,2),
    valor_minimo numeric(15,2),
    valor_maximo numeric(15,2),
    confianca numeric(5,2),
    status character varying(20),
    data_analise date,
    data_proxima_analise date,
    criado_por bigint,
    data_criacao date DEFAULT CURRENT_DATE,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ia_analise_preditiva_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_analise_preditiva_id_seq OWNED BY brasil_saas.bc_ia_analise_preditiva.id;

CREATE TABLE brasil_saas.bc_ia_assistente_auditoria (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    usuario_id bigint,
    pergunta text NOT NULL,
    contexto jsonb,
    fontes character varying(200),
    modelo character varying(120),
    resposta_modelo text,
    resposta text NOT NULL,
    status character varying(32) DEFAULT 'ok'::character varying NOT NULL,
    erro text,
    duracao_ms bigint,
    tokens_entrada integer,
    tokens_saida integer,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    usuario_nome character varying(256),
    identity_id character varying(128)
);

COMMENT ON TABLE brasil_saas.bc_ia_assistente_auditoria IS 'Auditoria do Assistente ERP: pergunta, contexto, modelo e tempo. Sem ela não há como conferir, depois, se a resposta veio do ERP ou do modelo.';

COMMENT ON COLUMN brasil_saas.bc_ia_assistente_auditoria.usuario_nome IS 'Quem perguntou, pelo nome. É o campo confiável: vale para AD, Postgres e Linux, e não depende de JOIN com bc_core_usuario.';

COMMENT ON COLUMN brasil_saas.bc_ia_assistente_auditoria.identity_id IS 'identityId do contrato oficial do Auth Service. Âncora estavel da identidade. NULL enquanto o IdentityClient não existir; username é rótulo, não chave.';

CREATE SEQUENCE brasil_saas.bc_ia_assistente_auditoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_assistente_auditoria_id_seq OWNED BY brasil_saas.bc_ia_assistente_auditoria.id;

CREATE TABLE brasil_saas.bc_ia_chat_mensagem (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    sessao_id bigint NOT NULL,
    conteudo text NOT NULL,
    tipo character varying(20) NOT NULL,
    tokens integer,
    tempo_resposta_ms bigint,
    modelo_ia character varying(50),
    data_envio timestamp without time zone DEFAULT now(),
    classificacao integer,
    feedback text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ia_chat_mensagem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_chat_mensagem_id_seq OWNED BY brasil_saas.bc_ia_chat_mensagem.id;

CREATE TABLE brasil_saas.bc_ia_chat_message (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    session_id bigint NOT NULL,
    content text NOT NULL,
    message_type character varying(20) NOT NULL,
    sender character varying(100),
    token_count bigint DEFAULT 0,
    response_time_ms bigint,
    metadata jsonb,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ia_chat_message IS 'Messages in chat sessions';

CREATE SEQUENCE brasil_saas.bc_ia_chat_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_chat_message_id_seq OWNED BY brasil_saas.bc_ia_chat_message.id;

CREATE TABLE brasil_saas.bc_ia_chat_sessao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    titulo character varying(200),
    usuario_id bigint NOT NULL,
    modelo_ia character varying(50) DEFAULT 'gpt-4'::character varying,
    temperatura double precision DEFAULT 0.7,
    max_tokens integer DEFAULT 4096,
    ativo boolean DEFAULT true,
    favorito boolean DEFAULT false,
    data_criacao timestamp without time zone DEFAULT now(),
    data_atualizacao timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ia_chat_sessao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_chat_sessao_id_seq OWNED BY brasil_saas.bc_ia_chat_sessao.id;

CREATE TABLE brasil_saas.bc_ia_chat_session (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    title character varying(200) NOT NULL,
    context text,
    model_name character varying(100) DEFAULT 'gpt-4'::character varying,
    temperature double precision DEFAULT 0.7,
    max_tokens integer DEFAULT 4000,
    session_type character varying(50) DEFAULT 'GENERAL'::character varying,
    is_active boolean DEFAULT true,
    token_count bigint DEFAULT 0,
    cost_usd double precision DEFAULT 0.0,
    user_id bigint,
    metadata jsonb,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ia_chat_session IS 'Chat sessions with AI assistant';

CREATE SEQUENCE brasil_saas.bc_ia_chat_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_chat_session_id_seq OWNED BY brasil_saas.bc_ia_chat_session.id;

CREATE TABLE brasil_saas.bc_ia_classificacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    tipo character varying(50),
    categoria character varying(100),
    tags text,
    confianca double precision,
    classificacao_manual character varying(100),
    classificacao_ia character varying(100),
    status character varying(20),
    entidade_id bigint,
    aprovado_por bigint,
    data_aprovacao timestamp without time zone,
    criado_por bigint,
    data_criacao timestamp without time zone DEFAULT now(),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ia_classificacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_classificacao_id_seq OWNED BY brasil_saas.bc_ia_classificacao.id;

CREATE TABLE brasil_saas.bc_ia_config (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    api_key text,
    api_endpoint character varying(500) DEFAULT 'https://api.openai.com/v1'::character varying,
    default_model character varying(100) DEFAULT 'gpt-4'::character varying,
    temperature double precision DEFAULT 0.7,
    max_tokens integer DEFAULT 4000,
    timeout_seconds integer DEFAULT 60,
    is_enabled boolean DEFAULT false,
    max_daily_tokens bigint DEFAULT 100000,
    daily_token_usage bigint DEFAULT 0,
    last_reset_date date,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ia_config IS 'Configuration for AI integration (OpenAI, etc.)';

CREATE SEQUENCE brasil_saas.bc_ia_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_config_id_seq OWNED BY brasil_saas.bc_ia_config.id;

CREATE TABLE brasil_saas.bc_ia_embedding (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    entidade_tipo character varying(50) NOT NULL,
    entidade_id bigint NOT NULL,
    texto text NOT NULL,
    embedding_vector double precision[],
    dimensoes integer,
    modelo character varying(50) DEFAULT 'text-embedding-3-small'::character varying,
    tamanho_texto integer,
    tokens integer,
    data_criacao timestamp without time zone DEFAULT now(),
    data_atualizacao timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ia_embedding_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_embedding_id_seq OWNED BY brasil_saas.bc_ia_embedding.id;

CREATE TABLE brasil_saas.bc_ia_prompt (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    conteudo text NOT NULL,
    categoria character varying(50),
    tipo character varying(20) DEFAULT 'TEXT'::character varying,
    temperatura double precision DEFAULT 0.7,
    max_tokens integer DEFAULT 4096,
    ativo boolean DEFAULT true,
    publico boolean DEFAULT false,
    favorito boolean DEFAULT false,
    contador_uso integer DEFAULT 0,
    criado_por bigint,
    data_criacao timestamp without time zone DEFAULT now(),
    data_atualizacao timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ia_prompt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_prompt_id_seq OWNED BY brasil_saas.bc_ia_prompt.id;

CREATE TABLE brasil_saas.bc_ia_prompt_template (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    category character varying(50),
    content text NOT NULL,
    description text,
    variables jsonb,
    is_active boolean DEFAULT true,
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ia_prompt_template IS 'Prompt templates for AI interactions';

CREATE SEQUENCE brasil_saas.bc_ia_prompt_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ia_prompt_template_id_seq OWNED BY brasil_saas.bc_ia_prompt_template.id;

CREATE TABLE brasil_saas.bc_migration_log (
    id bigint NOT NULL,
    migration character varying(10) NOT NULL,
    tabela_origem character varying(80),
    tabela_destino character varying(80),
    registros_origem bigint,
    registros_destino bigint,
    divergencias bigint DEFAULT 0 NOT NULL,
    detalhes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE brasil_saas.bc_migration_log IS 'Trilha da migração de dados do legado';

CREATE SEQUENCE brasil_saas.bc_migration_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_migration_log_id_seq OWNED BY brasil_saas.bc_migration_log.id;

CREATE TABLE brasil_saas.bc_pcp_mps (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    periodo character varying(7) NOT NULL,
    produto_id bigint NOT NULL,
    qtd_demandada numeric(15,3) DEFAULT 0 NOT NULL,
    qtd_estoque numeric(15,3) DEFAULT 0 NOT NULL,
    qtd_planejada numeric(15,3) DEFAULT 0 NOT NULL,
    origem character varying(20) DEFAULT 'PEDIDOS'::character varying NOT NULL,
    status character varying(20) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_pcp_mps ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_pcp_mps_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prj_etapa (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    projeto_id bigint NOT NULL,
    pai_id bigint,
    codigo_wbs character varying(60) NOT NULL,
    nome character varying(200) NOT NULL,
    ordem integer DEFAULT 1 NOT NULL,
    responsavel character varying(200),
    data_inicio date,
    data_fim date,
    pct_concluido integer DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'NAO_INICIADA'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_prj_etapa ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_etapa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prj_faturamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    projeto_id bigint NOT NULL,
    descricao character varying(500) NOT NULL,
    valor numeric(15,2) NOT NULL,
    data_prevista date,
    data_faturado date,
    status character varying(20) DEFAULT 'PREVISTO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    titulo_id bigint,
    nfse_id bigint
);

ALTER TABLE brasil_saas.bc_prj_faturamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_faturamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prj_movimento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    projeto_id bigint NOT NULL,
    etapa_id bigint,
    tipo character varying(10) NOT NULL,
    descricao character varying(500) NOT NULL,
    valor numeric(15,2) NOT NULL,
    data date NOT NULL,
    origem_tipo character varying(60),
    origem_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    CONSTRAINT ck_prj_mov_tipo CHECK (((tipo)::text = ANY ((ARRAY['CUSTO'::character varying, 'RECEITA'::character varying])::text[])))
);

ALTER TABLE brasil_saas.bc_prj_movimento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_movimento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prj_mudanca (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    projeto_id bigint NOT NULL,
    descricao character varying(500) NOT NULL,
    status character varying(20) DEFAULT 'SOLICITADA'::character varying NOT NULL,
    impacto_valor numeric(15,2) DEFAULT 0 NOT NULL,
    decidida_por bigint,
    decidida_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_prj_mudanca ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_mudanca_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prj_projeto (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(200) NOT NULL,
    descricao text,
    status character varying(20) DEFAULT 'PLANEJADO'::character varying NOT NULL,
    gerente character varying(200),
    data_inicio date,
    data_fim_prevista date,
    data_fim_real date,
    orcamento_total numeric(15,2) DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_prj_projeto ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_projeto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prj_risco (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    projeto_id bigint NOT NULL,
    descricao character varying(500) NOT NULL,
    probabilidade integer DEFAULT 50 NOT NULL,
    impacto character varying(20) DEFAULT 'MEDIO'::character varying NOT NULL,
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    mitigacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_prj_risco ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_risco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prod_alocacao_capacidade (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    centro_trabalho_id bigint NOT NULL,
    data date NOT NULL,
    horas numeric(12,4) NOT NULL,
    ordem_producao_id bigint,
    operacao_roteiro_id bigint,
    origem character varying(20) DEFAULT 'OP'::character varying NOT NULL,
    observacao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_prod_alocacao_capacidade ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prod_alocacao_capacidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prod_apontamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    producao_id bigint NOT NULL,
    item_producao_id bigint,
    funcionario_id bigint,
    data_apontamento timestamp without time zone,
    horas_trabalhadas numeric(10,2) DEFAULT 0,
    quantidade_produzida numeric(15,4) DEFAULT 0,
    quantidade_refugo numeric(15,4) DEFAULT 0,
    status character varying(20) DEFAULT 'INICIADO'::character varying,
    observacoes text,
    maquina_equipamento_id bigint,
    turno character varying(50),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    operacao_roteiro_id bigint
);

CREATE SEQUENCE brasil_saas.bc_prod_apontamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_prod_apontamento_id_seq OWNED BY brasil_saas.bc_prod_apontamento.id;

CREATE TABLE brasil_saas.bc_prod_centro_trabalho (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(160) NOT NULL,
    capacidade_horas_dia numeric(10,2) DEFAULT 8 NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    created_by bigint,
    updated_by bigint
);

CREATE SEQUENCE brasil_saas.bc_prod_centro_trabalho_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_prod_centro_trabalho_id_seq OWNED BY brasil_saas.bc_prod_centro_trabalho.id;

CREATE TABLE brasil_saas.bc_prod_estrutura (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    produto_pai_id bigint NOT NULL,
    produto_filho_id bigint NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    perda_percentual numeric(7,4) DEFAULT 0 NOT NULL,
    nivel integer DEFAULT 1 NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    CONSTRAINT ck_prod_estrutura_distinto CHECK ((produto_pai_id <> produto_filho_id)),
    CONSTRAINT ck_prod_estrutura_nivel CHECK ((nivel > 0)),
    CONSTRAINT ck_prod_estrutura_perda CHECK (((perda_percentual >= (0)::numeric) AND (perda_percentual < (100)::numeric))),
    CONSTRAINT ck_prod_estrutura_quantidade CHECK ((quantidade > (0)::numeric))
);

CREATE SEQUENCE brasil_saas.bc_prod_estrutura_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_prod_estrutura_id_seq OWNED BY brasil_saas.bc_prod_estrutura.id;

CREATE TABLE brasil_saas.bc_prod_item (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    deleted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    custo_total numeric(15,2),
    custo_unitario numeric(15,2),
    produto_id bigint NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    producao_id bigint NOT NULL
);

ALTER TABLE brasil_saas.bc_prod_item ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prod_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prod_ordem (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    deleted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    custo_total numeric(15,2),
    data_fim timestamp(6) without time zone,
    data_inicio timestamp(6) without time zone,
    densidade numeric(38,2),
    numero character varying(255) NOT NULL,
    produto_final_id bigint NOT NULL,
    quantidade_planejada numeric(15,4),
    status character varying(255) NOT NULL,
    tipo_producao character varying(50),
    unidade_medida character varying(20)
);

ALTER TABLE brasil_saas.bc_prod_ordem ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prod_ordem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_prod_romaneio (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(50) NOT NULL,
    producao_id bigint NOT NULL,
    data_romaneio date DEFAULT CURRENT_DATE NOT NULL,
    destino character varying(150),
    responsavel_id bigint,
    veiculo_id bigint,
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    observacoes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_prod_romaneio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_prod_romaneio_id_seq OWNED BY brasil_saas.bc_prod_romaneio.id;

CREATE TABLE brasil_saas.bc_prod_romaneio_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    romaneio_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    descricao character varying(200),
    quantidade numeric(15,4) NOT NULL,
    unidade_medida character varying(20) NOT NULL,
    lote character varying(100),
    observacoes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_prod_romaneio_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_prod_romaneio_item_id_seq OWNED BY brasil_saas.bc_prod_romaneio_item.id;

CREATE TABLE brasil_saas.bc_prod_roteiro (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(160) NOT NULL,
    versao integer DEFAULT 1 NOT NULL,
    vigencia_inicio date,
    vigencia_fim date,
    ativo boolean DEFAULT true NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    created_by bigint,
    updated_by bigint
);

CREATE SEQUENCE brasil_saas.bc_prod_roteiro_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_prod_roteiro_id_seq OWNED BY brasil_saas.bc_prod_roteiro.id;

CREATE TABLE brasil_saas.bc_prod_roteiro_operacao (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    roteiro_id bigint NOT NULL,
    sequencia integer NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(160) NOT NULL,
    centro_trabalho_id bigint,
    setup_minutos numeric(12,3) DEFAULT 0 NOT NULL,
    maquina_minutos numeric(12,3) DEFAULT 0 NOT NULL,
    homem_minutos numeric(12,3) DEFAULT 0 NOT NULL,
    instrucoes text,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    created_by bigint,
    updated_by bigint
);

CREATE SEQUENCE brasil_saas.bc_prod_roteiro_operacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_prod_roteiro_operacao_id_seq OWNED BY brasil_saas.bc_prod_roteiro_operacao.id;

CREATE TABLE brasil_saas.bc_ptl_acesso (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    pessoa_id bigint NOT NULL,
    token character varying(64) NOT NULL,
    expira_em timestamp without time zone NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    ultimo_uso_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_ptl_acesso ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ptl_acesso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_qual_inspecao (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    plano_id bigint,
    referencia_tipo character varying(30) NOT NULL,
    referencia_id bigint,
    numero character varying(50) NOT NULL,
    data_inspecao date NOT NULL,
    status character varying(30) DEFAULT 'ABERTA'::character varying NOT NULL,
    resultado character varying(30),
    observacao character varying(1000),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_qual_inspecao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_qual_inspecao_id_seq OWNED BY brasil_saas.bc_qual_inspecao.id;

CREATE TABLE brasil_saas.bc_qual_nao_conformidade (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    inspecao_id bigint,
    numero character varying(50) NOT NULL,
    severidade character varying(20) DEFAULT 'MEDIA'::character varying NOT NULL,
    status character varying(30) DEFAULT 'ABERTA'::character varying NOT NULL,
    descricao character varying(2000) NOT NULL,
    causa_raiz character varying(2000),
    acao_corretiva character varying(2000),
    acao_preventiva character varying(2000),
    responsavel_id bigint,
    prazo date,
    encerrada_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_qual_nao_conformidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_qual_nao_conformidade_id_seq OWNED BY brasil_saas.bc_qual_nao_conformidade.id;

CREATE TABLE brasil_saas.bc_qual_plano_inspecao (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    descricao character varying(255) NOT NULL,
    tipo character varying(30) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_qual_plano_inspecao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_qual_plano_inspecao_id_seq OWNED BY brasil_saas.bc_qual_plano_inspecao.id;

CREATE TABLE brasil_saas.bc_rh_cargo (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    salario_base numeric(15,2) DEFAULT 0 NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_rh_cargo IS 'Cargos';

CREATE SEQUENCE brasil_saas.bc_rh_cargo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_rh_cargo_id_seq OWNED BY brasil_saas.bc_rh_cargo.id;

CREATE TABLE brasil_saas.bc_rh_ferias (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data_inicio date NOT NULL,
    dias integer DEFAULT 30 NOT NULL,
    data_fim date NOT NULL,
    status character varying(20) DEFAULT 'PROGRAMADA'::character varying NOT NULL,
    valor_ferias numeric(15,2) DEFAULT 0 NOT NULL,
    valor_terco numeric(15,2) DEFAULT 0 NOT NULL,
    folha_id bigint,
    observacao character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_rh_ferias ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_rh_ferias_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_rh_folha (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia character varying(7) NOT NULL,
    status character varying(255) DEFAULT 'ABERTA'::character varying NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    titulo_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_rh_folha IS 'Folhas de pagamento por competência';

COMMENT ON COLUMN brasil_saas.bc_rh_folha.competencia IS 'Formato YYYY-MM';

COMMENT ON COLUMN brasil_saas.bc_rh_folha.status IS 'ABERTA, PROCESSADA ou CANCELADA';

COMMENT ON COLUMN brasil_saas.bc_rh_folha.titulo_id IS 'Título a pagar gerado no financeiro';

CREATE SEQUENCE brasil_saas.bc_rh_folha_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_rh_folha_id_seq OWNED BY brasil_saas.bc_rh_folha.id;

CREATE TABLE brasil_saas.bc_rh_folha_item (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    folha_id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    tipo character varying(255) NOT NULL,
    descricao character varying(200),
    valor numeric(15,2) DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL
);

COMMENT ON COLUMN brasil_saas.bc_rh_folha_item.tipo IS 'PROVENTO ou DESCONTO';

CREATE SEQUENCE brasil_saas.bc_rh_folha_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_rh_folha_item_id_seq OWNED BY brasil_saas.bc_rh_folha_item.id;

CREATE TABLE brasil_saas.bc_rh_funcionario (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    pessoa_id bigint NOT NULL,
    cargo_id bigint,
    matricula character varying(30),
    data_admissao date,
    data_demissao date,
    salario numeric(15,2) DEFAULT 0 NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    foto_url text,
    foto_tipo_conteudo character varying(50),
    foto_tamanho bigint,
    percentual_comissao numeric(5,2),
    tipo_colaborador character varying(30),
    usuario_id bigint,
    valor_hora numeric(15,2)
);

COMMENT ON TABLE brasil_saas.bc_rh_funcionario IS 'Funcionários vinculados a uma pessoa do cadastro';

COMMENT ON COLUMN brasil_saas.bc_rh_funcionario.foto_url IS 'URL da foto do funcionário';

COMMENT ON COLUMN brasil_saas.bc_rh_funcionario.foto_tipo_conteudo IS 'Tipo MIME da foto do funcionário';

COMMENT ON COLUMN brasil_saas.bc_rh_funcionario.foto_tamanho IS 'Tamanho em bytes da foto do funcionário';

CREATE SEQUENCE brasil_saas.bc_rh_funcionario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_rh_funcionario_id_seq OWNED BY brasil_saas.bc_rh_funcionario.id;

CREATE TABLE brasil_saas.bc_rh_ponto (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data date NOT NULL,
    e1 time without time zone,
    s1 time without time zone,
    e2 time without time zone,
    s2 time without time zone,
    horas_trabalhadas numeric(5,2) DEFAULT 0 NOT NULL,
    falta boolean DEFAULT false NOT NULL,
    observacao character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_rh_ponto ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_rh_ponto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_srv_ordem_servico (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    cliente_id bigint NOT NULL,
    numero character varying(20) NOT NULL,
    equipamento character varying(200),
    descricao text,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    usuario_id bigint,
    abertura_at timestamp without time zone DEFAULT now() NOT NULL,
    previsao_at timestamp without time zone,
    fechamento_at timestamp without time zone,
    laudo text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_srv_ordem_servico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_srv_ordem_servico_id_seq OWNED BY brasil_saas.bc_srv_ordem_servico.id;

CREATE TABLE brasil_saas.bc_srv_os_apontamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    os_id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    data_apontamento timestamp without time zone DEFAULT now() NOT NULL,
    horas numeric(7,2) NOT NULL,
    descricao character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_srv_os_apontamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_srv_os_apontamento_id_seq OWNED BY brasil_saas.bc_srv_os_apontamento.id;

CREATE TABLE brasil_saas.bc_srv_os_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    os_id bigint NOT NULL,
    produto_id bigint,
    servico_id bigint,
    quantidade numeric(15,3) NOT NULL,
    valor_unitario numeric(15,4) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_srv_os_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_srv_os_item_id_seq OWNED BY brasil_saas.bc_srv_os_item.id;

CREATE TABLE brasil_saas.bc_ven_bonificacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    cliente_id bigint NOT NULL,
    pedido_origem_id bigint,
    numero character varying(30) NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    data_emissao date DEFAULT CURRENT_DATE NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    motivo character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ven_bonificacao IS 'Bonificacoes comerciais';

CREATE SEQUENCE brasil_saas.bc_ven_bonificacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_bonificacao_id_seq OWNED BY brasil_saas.bc_ven_bonificacao.id;

CREATE TABLE brasil_saas.bc_ven_bonificacao_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    bonificacao_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    valor_referencia numeric(15,4) DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ven_bonificacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_bonificacao_item_id_seq OWNED BY brasil_saas.bc_ven_bonificacao_item.id;

CREATE TABLE brasil_saas.bc_ven_contrato (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    cliente_id bigint NOT NULL,
    numero character varying(30) NOT NULL,
    descricao character varying(255),
    tipo character varying(30) DEFAULT 'COMERCIAL'::character varying NOT NULL,
    data_inicio date NOT NULL,
    data_fim date,
    valor_mensal numeric(15,2),
    dia_faturamento integer,
    renovacao_automatica boolean DEFAULT false NOT NULL,
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ven_contrato IS 'Contratos comerciais e recorrencia';

CREATE SEQUENCE brasil_saas.bc_ven_contrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_contrato_id_seq OWNED BY brasil_saas.bc_ven_contrato.id;

CREATE TABLE brasil_saas.bc_ven_contrato_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    contrato_id bigint NOT NULL,
    produto_id bigint,
    servico_id bigint,
    quantidade numeric(15,4) DEFAULT 1 NOT NULL,
    valor_unitario numeric(15,4) DEFAULT 0 NOT NULL,
    recorrencia character varying(20),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

CREATE SEQUENCE brasil_saas.bc_ven_contrato_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_contrato_item_id_seq OWNED BY brasil_saas.bc_ven_contrato_item.id;

CREATE TABLE brasil_saas.bc_ven_devolucao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    pedido_id bigint,
    cliente_id bigint NOT NULL,
    numero character varying(30) NOT NULL,
    tipo character varying(20) DEFAULT 'DEVOLUCAO'::character varying NOT NULL,
    motivo character varying(100),
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    data_solicitacao date DEFAULT CURRENT_DATE NOT NULL,
    data_conclusao date,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    decidida_por bigint,
    decidida_em timestamp without time zone,
    recebida_em timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ven_devolucao IS 'Devolucoes e trocas de vendas';

CREATE SEQUENCE brasil_saas.bc_ven_devolucao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_devolucao_id_seq OWNED BY brasil_saas.bc_ven_devolucao.id;

CREATE TABLE brasil_saas.bc_ven_devolucao_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    devolucao_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,4) DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    lote_id bigint,
    numero_serie_id bigint,
    destino_estoque character varying(20) DEFAULT 'DISPONIVEL'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    qtd_recebida numeric(15,3) DEFAULT 0 NOT NULL
);

CREATE SEQUENCE brasil_saas.bc_ven_devolucao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_devolucao_item_id_seq OWNED BY brasil_saas.bc_ven_devolucao_item.id;

CREATE TABLE brasil_saas.bc_ven_pedido (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    cliente_id bigint NOT NULL,
    numero character varying(255),
    tipo character varying(255) DEFAULT 'PEDIDO'::character varying NOT NULL,
    status character varying(255) DEFAULT 'ABERTO'::character varying NOT NULL,
    condicao_pagamento_id bigint,
    data_emissao date NOT NULL,
    data_entrega date,
    valor_produtos numeric(15,2) DEFAULT 0 NOT NULL,
    valor_servicos numeric(15,2) DEFAULT 0 NOT NULL,
    valor_desconto numeric(15,2) DEFAULT 0 NOT NULL,
    valor_frete numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    titulo_id bigint,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    vendedor_id bigint,
    tabela_preco_id bigint,
    percentual_desconto numeric(7,4) DEFAULT 0,
    canal_venda character varying(30),
    origem character varying(30)
);

COMMENT ON TABLE brasil_saas.bc_ven_pedido IS 'Pedidos de venda e orçamentos';

COMMENT ON COLUMN brasil_saas.bc_ven_pedido.tipo IS 'ORCAMENTO ou PEDIDO';

COMMENT ON COLUMN brasil_saas.bc_ven_pedido.status IS 'ABERTO, FATURADO ou CANCELADO';

COMMENT ON COLUMN brasil_saas.bc_ven_pedido.titulo_id IS 'Título a receber gerado no financeiro';

CREATE SEQUENCE brasil_saas.bc_ven_pedido_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_pedido_id_seq OWNED BY brasil_saas.bc_ven_pedido.id;

CREATE TABLE brasil_saas.bc_ven_pedido_item (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    numero_item integer NOT NULL,
    produto_id bigint,
    servico_id bigint,
    descricao character varying(300),
    quantidade numeric(15,3) NOT NULL,
    unidade character varying(10),
    valor_unitario numeric(15,4) NOT NULL,
    valor_desconto numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    criado_estoque boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL
);

CREATE SEQUENCE brasil_saas.bc_ven_pedido_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_pedido_item_id_seq OWNED BY brasil_saas.bc_ven_pedido_item.id;

CREATE TABLE brasil_saas.bc_ven_regra_comissao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    vendedor_id bigint,
    vigencia_inicio date,
    vigencia_fim date,
    meta_valor numeric(15,2),
    faixa_valor_min numeric(15,2) DEFAULT 0 NOT NULL,
    faixa_valor_max numeric(15,2),
    percentual numeric(7,4) NOT NULL,
    base_calculo character varying(30) DEFAULT 'VALOR_LIQUIDO'::character varying NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ven_regra_comissao IS 'Regras de comissao por vendedor, meta e faixa';

CREATE SEQUENCE brasil_saas.bc_ven_regra_comissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_regra_comissao_id_seq OWNED BY brasil_saas.bc_ven_regra_comissao.id;

CREATE TABLE brasil_saas.bc_ven_tabela_preco (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    codigo character varying(30),
    descricao character varying(255),
    moeda character varying(3) DEFAULT 'BRL'::bpchar NOT NULL,
    vigencia_inicio date,
    vigencia_fim date,
    percentual_desconto_maximo numeric(7,4) DEFAULT 0 NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ven_tabela_preco IS 'Tabelas de preco comerciais por empresa';

CREATE SEQUENCE brasil_saas.bc_ven_tabela_preco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_tabela_preco_id_seq OWNED BY brasil_saas.bc_ven_tabela_preco.id;

CREATE TABLE brasil_saas.bc_ven_tabela_preco_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid(),
    empresa_id bigint NOT NULL,
    tabela_preco_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    preco numeric(15,4) NOT NULL,
    preco_minimo numeric(15,4),
    percentual_desconto_maximo numeric(7,4) DEFAULT 0 NOT NULL,
    vigencia_inicio date,
    vigencia_fim date,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

COMMENT ON TABLE brasil_saas.bc_ven_tabela_preco_item IS 'Precos de produtos por tabela comercial';

CREATE SEQUENCE brasil_saas.bc_ven_tabela_preco_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas.bc_ven_tabela_preco_item_id_seq OWNED BY brasil_saas.bc_ven_tabela_preco_item.id;

CREATE TABLE brasil_saas.bc_wkf_definition (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(200) NOT NULL,
    descricao text,
    entidade_alvo character varying(60) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wkf_definition ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_definition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_wkf_instance (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    definition_id bigint NOT NULL,
    entidade_tipo character varying(60) NOT NULL,
    entidade_id bigint NOT NULL,
    status character varying(20) DEFAULT 'EM_ANDAMENTO'::character varying NOT NULL,
    etapa_atual integer DEFAULT 1 NOT NULL,
    solicitado_por bigint,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    concluded_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wkf_instance ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_wkf_stage (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    definition_id bigint NOT NULL,
    ordem integer NOT NULL,
    nome character varying(200) NOT NULL,
    tipo character varying(20) DEFAULT 'APROVACAO'::character varying NOT NULL,
    sla_horas integer DEFAULT 48 NOT NULL,
    aprovadores text,
    exige_todos boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wkf_stage ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_stage_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_wkf_task (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    instance_id bigint NOT NULL,
    stage_id bigint NOT NULL,
    responsavel character varying(200),
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    decided_at timestamp without time zone,
    decidido_por bigint,
    comentario text,
    sla_limite timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wkf_task ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_wms_onda (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    deposito_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    responsavel character varying(200),
    liberada_em timestamp without time zone,
    concluida_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wms_onda ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_onda_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_wms_onda_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    onda_id bigint NOT NULL,
    origem_tipo character varying(20) DEFAULT 'RESERVA'::character varying NOT NULL,
    origem_id bigint,
    produto_id bigint NOT NULL,
    qtd_solicitada numeric(15,3) NOT NULL,
    qtd_separada numeric(15,3) DEFAULT 0 NOT NULL,
    endereco_id bigint,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wms_onda_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_onda_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_wms_volume (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    expedicao_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    peso numeric(15,3),
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wms_volume ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_volume_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas.bc_wms_volume_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    volume_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade numeric(15,3) NOT NULL,
    onda_item_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);

ALTER TABLE brasil_saas.bc_wms_volume_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_volume_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE brasil_saas_dl.dim_cliente (
    id bigint NOT NULL,
    cliente_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    cpf_cnpj character varying(20) NOT NULL,
    tipo_pessoa character varying(20) NOT NULL,
    segmento character varying(100),
    regiao character varying(100),
    estado character varying(2),
    cidade character varying(100),
    grupo_cliente character varying(100),
    classificacao character varying(20),
    data_primeira_compra date,
    data_ultima_compra date,
    ativo boolean DEFAULT true NOT NULL,
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.dim_cliente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.dim_cliente_id_seq OWNED BY brasil_saas_dl.dim_cliente.id;

CREATE TABLE brasil_saas_dl.dim_empresa (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    cnpj character varying(20),
    razao_social character varying(200),
    grupo_economico character varying(100),
    segmento character varying(100),
    porte character varying(20),
    data_fundacao date,
    ativo boolean DEFAULT true NOT NULL,
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.dim_empresa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.dim_empresa_id_seq OWNED BY brasil_saas_dl.dim_empresa.id;

CREATE TABLE brasil_saas_dl.dim_produto (
    id bigint NOT NULL,
    produto_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    nome character varying(200) NOT NULL,
    descricao text,
    categoria_id bigint,
    categoria_nome character varying(100),
    marca_id bigint,
    marca_nome character varying(100),
    unidade character varying(10) NOT NULL,
    tipo_produto character varying(50) NOT NULL,
    familia_produto character varying(100),
    grupo_produto character varying(100),
    linha_produto character varying(100),
    ncm character varying(20),
    cest character varying(20),
    cst character varying(20),
    peso_bruto numeric(15,6),
    peso_liquido numeric(15,6),
    volume numeric(15,6),
    ativo boolean DEFAULT true NOT NULL,
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.dim_produto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.dim_produto_id_seq OWNED BY brasil_saas_dl.dim_produto.id;

CREATE TABLE brasil_saas_dl.dim_tempo (
    id bigint NOT NULL,
    data date NOT NULL,
    dia_da_semana integer NOT NULL,
    nome_dia_da_semana character varying(20) NOT NULL,
    dia_do_mes integer NOT NULL,
    dia_do_ano integer NOT NULL,
    semana_do_ano integer NOT NULL,
    mes integer NOT NULL,
    nome_mes character varying(20) NOT NULL,
    trimestre integer NOT NULL,
    ano integer NOT NULL,
    eh_fim_de_semana boolean NOT NULL,
    eh_feriado boolean DEFAULT false NOT NULL,
    nome_feriado character varying(100),
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.dim_tempo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.dim_tempo_id_seq OWNED BY brasil_saas_dl.dim_tempo.id;

CREATE TABLE brasil_saas_dl.ft_compras (
    id bigint NOT NULL,
    data_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    fornecedor_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    categoria_id bigint,
    pedido_compra_id bigint NOT NULL,
    item_pedido_id bigint,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,2) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    tipo_compra character varying(50) NOT NULL,
    status_pedido character varying(50) NOT NULL,
    data_pedido date NOT NULL,
    data_entrega date,
    data_recebimento date,
    condicao_pagamento character varying(50),
    prazo_entrega integer,
    frete numeric(15,2),
    impostos numeric(15,2),
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.ft_compras_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.ft_compras_id_seq OWNED BY brasil_saas_dl.ft_compras.id;

CREATE TABLE brasil_saas_dl.ft_estoque (
    id bigint NOT NULL,
    data_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    categoria_id bigint,
    centro_custo_id bigint,
    tipo_movimentacao character varying(50) NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,2),
    valor_total numeric(15,2),
    saldo_anterior numeric(15,4),
    saldo_atual numeric(15,4),
    data_movimentacao date NOT NULL,
    documento_origem character varying(100),
    documento_id bigint,
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.ft_estoque_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.ft_estoque_id_seq OWNED BY brasil_saas_dl.ft_estoque.id;

CREATE TABLE brasil_saas_dl.ft_financeiro (
    id bigint NOT NULL,
    data_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    tipo_lancamento character varying(50) NOT NULL,
    categoria_lancamento character varying(100) NOT NULL,
    centro_custo_id bigint,
    cliente_fornecedor_id bigint,
    cliente_fornecedor_tipo character varying(20),
    titulo_id bigint,
    valor numeric(15,2) NOT NULL,
    valor_liquido numeric(15,2),
    data_vencimento date,
    data_pagamento date,
    status character varying(50) NOT NULL,
    forma_pagamento character varying(50),
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.ft_financeiro_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.ft_financeiro_id_seq OWNED BY brasil_saas_dl.ft_financeiro.id;

CREATE TABLE brasil_saas_dl.ft_producao (
    id bigint NOT NULL,
    data_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ordem_producao_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    quantidade_produzida numeric(15,4) NOT NULL,
    quantidade_prevista numeric(15,4),
    tempo_producao_minutos integer,
    tempo_padrao_minutos integer,
    custo_material numeric(15,2),
    custo_mao_obra numeric(15,2),
    custo_indireto numeric(15,2),
    custo_total numeric(15,2),
    status character varying(50) NOT NULL,
    data_inicio date,
    data_fim date,
    centro_custo_id bigint,
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.ft_producao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.ft_producao_id_seq OWNED BY brasil_saas_dl.ft_producao.id;

CREATE TABLE brasil_saas_dl.ft_vendas (
    id bigint NOT NULL,
    data_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    cliente_id bigint NOT NULL,
    vendedor_id bigint,
    produto_id bigint NOT NULL,
    categoria_id bigint,
    pedido_venda_id bigint NOT NULL,
    item_pedido_id bigint,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,2) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    valor_custo numeric(15,2),
    margem_bruta numeric(15,2),
    percentual_margem numeric(10,2),
    tipo_venda character varying(50) NOT NULL,
    status_pedido character varying(50) NOT NULL,
    data_pedido date NOT NULL,
    data_entrega date,
    data_faturamento date,
    condicao_pagamento character varying(50),
    prazo_entrega integer,
    frete numeric(15,2),
    desconto numeric(15,2),
    impostos numeric(15,2),
    data_criacao date DEFAULT CURRENT_DATE
);

CREATE SEQUENCE brasil_saas_dl.ft_vendas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE brasil_saas_dl.ft_vendas_id_seq OWNED BY brasil_saas_dl.ft_vendas.id;

CREATE TABLE public.base_cep (
    cep character varying(8) NOT NULL,
    logradouro character varying(255),
    bairro character varying(255),
    cidade character varying(255),
    uf character varying(2)
);

CREATE TABLE public.fcaixa (
    codcaixa integer NOT NULL,
    descricaocaixa character varying(255) NOT NULL,
    agencia character varying(50),
    nomebanco character varying(100),
    numeroconta character varying(50),
    saldobanco numeric(15,2) DEFAULT 0.0
);

CREATE SEQUENCE public.fcaixa_codcaixa_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fcaixa_codcaixa_seq OWNED BY public.fcaixa.codcaixa;

CREATE TABLE public.fcentrocusto (
    codcusto integer NOT NULL,
    descricaocusto character varying(255) NOT NULL,
    ativo character varying(1) DEFAULT 'S'::character varying,
    aceitalancamento character varying(1) DEFAULT 'S'::character varying,
    tipo character varying(1) DEFAULT 'D'::character varying
);

CREATE SEQUENCE public.fcentrocusto_codcusto_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fcentrocusto_codcusto_seq OWNED BY public.fcentrocusto.codcusto;

CREATE SEQUENCE public.fcfo_codcfo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.fcfo (
    codcfo public.docod DEFAULT nextval('public.fcfo_codcfo_seq'::regclass) NOT NULL,
    nomefantasia character varying(255),
    nomerazao character varying(255),
    endereco character varying(255),
    bairro character varying(255),
    cidade character varying(255),
    cep character varying(255),
    uf public.dom2,
    telefone character varying(255),
    celular character varying(255),
    tipo public.dom1,
    cnpj character varying(255),
    inscestadual character varying(255),
    inscmunicipal character varying(255),
    contato character varying(255),
    obs character varying(255),
    datacad public.dodata,
    mail character varying(255),
    html character varying(255),
    codpais public.dom5,
    pais character varying(255),
    numero character varying(255),
    codmunicipio character varying(255),
    tabelapercentual public.dovalor,
    idcidade public.docod,
    datanascimento public.dodata,
    natureza_juridica character varying(2) DEFAULT 'PJ'::character varying,
    tipo_doc_identidade character varying(3),
    nirf character varying(255),
    classificacao_telefone character varying(255),
    classificacao_celular character varying(255)
);

CREATE SEQUENCE public.fcondicao_codcondicao_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.fcondicao (
    codcondicao integer DEFAULT nextval('public.fcondicao_codcondicao_seq'::regclass) NOT NULL,
    descricaocondicao character varying(255),
    numeroparcela public.docod,
    baixa1 public.dom1,
    par1 public.docod,
    par2 public.docod,
    par3 public.docod,
    par4 public.docod,
    par5 public.docod,
    par6 public.docod,
    par7 public.docod,
    par8 public.docod,
    par9 public.docod,
    par10 public.docod,
    par11 public.docod,
    par12 public.docod,
    par13 public.docod,
    par14 public.docod,
    par15 public.docod
);

CREATE TABLE public.fcusto (
    codcusto public.docod NOT NULL,
    codmascara public.dom5,
    mascaranivel public.dom2,
    descricaocusto character varying(255),
    idgrupo public.docod,
    custotipo public.dom1,
    custoativo public.dom1,
    aceitalan public.dom1,
    descricaocustogrupo character varying(255),
    valoragenda public.dovalor
);

CREATE SEQUENCE public.fcusto_codcusto_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fcusto_codcusto_seq OWNED BY public.fcusto.codcusto;

CREATE TABLE public.fdatas (
    iddata public.docod NOT NULL,
    datainicial public.dodata,
    datafinal public.dodata
);

CREATE SEQUENCE public.fdatas_iddata_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fdatas_iddata_seq OWNED BY public.fdatas.iddata;

CREATE TABLE public.fdia (
    refdia integer NOT NULL,
    baixadia public.dom1,
    datadia public.dodata,
    valorabertura public.dovalor,
    usuariodia public.docod,
    obsdia character varying(255)
);

CREATE SEQUENCE public.fdia_refdia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fdia_refdia_seq OWNED BY public.fdia.refdia;

CREATE SEQUENCE public.fdocumento_coddoc_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.fdocumento (
    coddoc public.docod DEFAULT nextval('public.fdocumento_coddoc_seq'::regclass) NOT NULL,
    descricaodoc character varying(255),
    doccontabil public.dom1
);

CREATE TABLE public.fempresa (
    codigo public.docod NOT NULL,
    nome character varying(255),
    nomerelatorio character varying(255),
    inscricaoestadual character varying(255),
    cnpj character varying(255),
    endereco character varying(255),
    bairro character varying(255),
    cidade character varying(255),
    cep character varying(255),
    uf public.dom2,
    telefone character varying(255),
    logo bytea,
    obs public.dmemo,
    nomefazenda character varying(255)
);

CREATE SEQUENCE public.fempresa_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fempresa_codigo_seq OWNED BY public.fempresa.codigo;

CREATE TABLE public.fempresa_vinculo (
    codvinculo integer NOT NULL,
    codmatriz integer NOT NULL,
    codfilial integer
);

CREATE SEQUENCE public.fempresa_vinculo_codvinculo_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fempresa_vinculo_codvinculo_seq OWNED BY public.fempresa_vinculo.codvinculo;

CREATE TABLE public.fextrato (
    idlanextrato public.docod NOT NULL,
    extratoidempresa public.docod,
    extratodata public.dodata,
    extratoidcaixa public.docod,
    extratoidcfo public.docod,
    extratodocumento character varying(255),
    extratovalor public.dovalor,
    extratobaixado public.dom1,
    extratodatabaixa public.dodata,
    extratohistorico character varying(255),
    extratousuario character varying(255),
    extratoidlan public.docod,
    extratoserie public.dom2,
    extratotipocd public.dom1,
    extratooperacao character varying(255),
    extratonomefavorecido character varying(255),
    extratocheque public.docod,
    extratodatacheque public.dodata,
    extratopre public.dom1,
    extratolancamentohistorico character varying(255)
);

CREATE SEQUENCE public.fextrato_idlanextrato_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fextrato_idlanextrato_seq OWNED BY public.fextrato.idlanextrato;

CREATE TABLE public.ffinanceiro (
    codfinanceiro integer NOT NULL,
    codmovimento integer,
    codcliente integer NOT NULL,
    datavencimento date NOT NULL,
    valor numeric(15,2) DEFAULT 0.00 NOT NULL,
    tipolancamento character varying(255) NOT NULL,
    status character varying(255) DEFAULT 'PENDENTE'::character varying NOT NULL
);

CREATE SEQUENCE public.ffinanceiro_codfinanceiro_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.ffinanceiro_codfinanceiro_seq OWNED BY public.ffinanceiro.codfinanceiro;

CREATE TABLE public.ffuncionario (
    codfuncionario public.docod NOT NULL,
    nomefuncionario character varying(255),
    comissao public.dovalor,
    ativo public.dom1,
    login_db character varying(255)
);

CREATE SEQUENCE public.ffuncionario_codfuncionario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.ffuncionario_codfuncionario_seq OWNED BY public.ffuncionario.codfuncionario;

CREATE TABLE public.fitem (
    refitem public.docod NOT NULL,
    idlanmov public.docod,
    idcfoitem smallint,
    idempresaitem public.docod,
    dataitem public.dodata,
    idproduto public.docod,
    unidadeitem public.dom2,
    produtonomeitem character varying(255),
    tipoitem public.dom1,
    quantidadeitem public.dmkqt,
    valorunitarioitem public.dovalor,
    totalitem public.dovalor,
    codvariacao integer
);

CREATE SEQUENCE public.fitem_refitem_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fitem_refitem_seq OWNED BY public.fitem.refitem;

CREATE SEQUENCE public.flan_idlan_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.flan (
    idlan public.docod DEFAULT nextval('public.flan_idlan_seq'::regclass) NOT NULL,
    datalan public.dodata,
    idempresa public.docod,
    idtipo public.docod,
    idcd public.dom1,
    baixado public.dom1,
    idcfo public.docod,
    idcustolan public.docod,
    idcustomascaralan public.dom5,
    idtipodoc public.docod,
    serie public.dom2,
    idmoeda public.docod,
    moedanome character varying(255),
    documento character varying(255),
    duplicata character varying(255),
    dataemissao public.dodata,
    valorlancamento public.dovalor,
    datavencimento public.dodata,
    usuario character varying(255),
    usuarioedicao character varying(255),
    obslan character varying(255),
    databaixa public.dodata,
    idcaixa public.docod,
    valorbaixado public.dovalor,
    valorquitado public.dovalor,
    valordesconto public.dovalor,
    valorjuro public.dovalor,
    valordiferenca public.dovalor,
    obsbaixa character varying(255),
    idos public.docod,
    idparcela public.docod,
    id_pessoa_parceiro integer
);

CREATE TABLE public.fmov (
    idmov public.docod NOT NULL,
    datalan public.dodata,
    idempresamov public.docod,
    baixamov public.dom1,
    datamov public.dodata,
    idcfomov public.docod,
    idveiculomov public.docod,
    idcustomov public.docod,
    idcustomascaramov public.dom5,
    placamov character varying(255),
    nomemotorista character varying(255),
    modeloveiculomov character varying(255),
    requisicaomov character varying(255),
    notamov character varying(255),
    idfuncionariomov public.docod,
    idpgtomov public.docod,
    idmoedamov public.docod,
    moedamov character varying(255),
    valorbrutomov public.dovalor,
    valordescontomov public.dovalor,
    valorliquidomov public.dovalor,
    obsmov character varying(255),
    usariomov character varying(255),
    obsmovfechamento character varying(255),
    datafechamentomov public.dodata,
    prismamov character varying(255),
    seriemov public.dom2,
    idcaixamov public.docod,
    idagendamento public.docod,
    id_pessoa_cliente integer,
    id_pessoa_funcionario integer
);

CREATE SEQUENCE public.fmov_idmov_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fmov_idmov_seq OWNED BY public.fmov.idmov;

CREATE TABLE public.fmovimento (
    codmovimento integer NOT NULL,
    codcliente integer NOT NULL,
    dataemissao date NOT NULL,
    totalfatura numeric(15,2) DEFAULT 0.00 NOT NULL,
    tipomovimento character varying(255) DEFAULT 'OS'::character varying NOT NULL
);

CREATE SEQUENCE public.fmovimento_codmovimento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fmovimento_codmovimento_seq OWNED BY public.fmovimento.codmovimento;

CREATE TABLE public.fmovimento_item (
    coditem integer NOT NULL,
    codmovimento integer NOT NULL,
    codproduto integer NOT NULL,
    quantidade numeric(15,2) DEFAULT 1.00 NOT NULL,
    valorunitario numeric(15,2) DEFAULT 0.00 NOT NULL,
    totalitem numeric(15,2) DEFAULT 0.00 NOT NULL,
    codvariacao integer
);

CREATE SEQUENCE public.fmovimento_item_coditem_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fmovimento_item_coditem_seq OWNED BY public.fmovimento_item.coditem;

CREATE TABLE public.fnota (
    id_nota integer NOT NULL,
    id_venda integer,
    numero_nota character varying(255) NOT NULL,
    serie character varying(5),
    modelo character varying(5),
    chave_acesso character varying(255),
    data_emissao date DEFAULT CURRENT_DATE,
    centro_custo integer,
    valor_total numeric(15,2) DEFAULT 0.00,
    status character varying(255) DEFAULT 'EMITIDA'::character varying,
    observacao text,
    codmovimento integer
);

CREATE SEQUENCE public.fnota_id_nota_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fnota_id_nota_seq OWNED BY public.fnota.id_nota;

CREATE TABLE public.fnota_item (
    id_item integer NOT NULL,
    id_nota integer NOT NULL,
    codvariacao integer,
    quantidade numeric(15,2) DEFAULT 1.00 NOT NULL,
    valor_unitario numeric(15,2) DEFAULT 0.00 NOT NULL,
    total_item numeric(15,2) DEFAULT 0.00 NOT NULL,
    ncm character varying(255),
    cfop character varying(255),
    icms numeric(5,2),
    pis numeric(5,2),
    cofins numeric(5,2),
    iss numeric(5,2)
);

CREATE SEQUENCE public.fnota_item_id_item_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fnota_item_id_item_seq OWNED BY public.fnota_item.id_item;

CREATE TABLE public.fpessoa (
    id_pessoa integer NOT NULL,
    tipo_pessoa character varying(2) DEFAULT 'PJ'::character varying,
    nome_razao character varying(255) NOT NULL,
    nome_fantasia character varying(255),
    cpf_cnpj character varying(255),
    rg_ie character varying(255),
    im character varying(255),
    cep character varying(255),
    logradouro character varying(255),
    numero character varying(255),
    bairro character varying(255),
    cidade character varying(255),
    uf character varying(2),
    codmunicipio character varying(255),
    idcidade integer,
    telefone character varying(255),
    celular character varying(255),
    contato character varying(255),
    email character varying(255),
    site character varying(255),
    obs character varying(255),
    datacad date DEFAULT CURRENT_DATE,
    datanascimento date,
    comissao numeric(15,4) DEFAULT 0,
    is_cliente boolean DEFAULT false,
    is_fornecedor boolean DEFAULT false,
    is_funcionario boolean DEFAULT false,
    id_usuario integer,
    old_codfuncionario integer
);

CREATE SEQUENCE public.fpessoa_id_pessoa_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fpessoa_id_pessoa_seq OWNED BY public.fpessoa.id_pessoa;

CREATE SEQUENCE public.gen_fproduto_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.fproduto (
    codproduto public.docod DEFAULT nextval('public.gen_fproduto_id'::regclass) NOT NULL,
    descricaoproduto character varying(255),
    unidadeproduto public.dom2,
    precocusto public.dovalor,
    precovenda public.dovalor,
    tipoproduto character varying(255),
    idgrupoproduto public.docod,
    origem character varying(2),
    ncm character varying(255),
    codigo_issqn character varying(20),
    valor_servico numeric(15,2),
    municipio_servico character varying(120),
    aliquota_issqn numeric(8,4)
);

CREATE TABLE public.fproduto_ecommerce (
    codecommerce integer NOT NULL,
    codproduto integer NOT NULL,
    peso_gramas integer DEFAULT 0,
    comprimento_cm numeric(10,2) DEFAULT 0.00,
    largura_cm numeric(10,2) DEFAULT 0.00,
    altura_cm numeric(10,2) DEFAULT 0.00,
    descricao_longa text,
    palavras_chave text
);

CREATE SEQUENCE public.fproduto_ecommerce_codecommerce_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fproduto_ecommerce_codecommerce_seq OWNED BY public.fproduto_ecommerce.codecommerce;

CREATE TABLE public.fproduto_fiscal (
    codproduto integer NOT NULL,
    cest character varying(255),
    icms numeric(5,2),
    pis numeric(5,2),
    cofins numeric(5,2),
    iss numeric(5,2)
);

CREATE SEQUENCE public.fproduto_fiscal_codproduto_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fproduto_fiscal_codproduto_seq OWNED BY public.fproduto_fiscal.codproduto;

CREATE TABLE public.fproduto_imagem (
    codimagem integer NOT NULL,
    codvariacao integer NOT NULL,
    url_caminho character varying(500) NOT NULL,
    is_principal boolean DEFAULT false
);

CREATE SEQUENCE public.fproduto_imagem_codimagem_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fproduto_imagem_codimagem_seq OWNED BY public.fproduto_imagem.codimagem;

CREATE TABLE public.fproduto_kit (
    codkit integer NOT NULL,
    codproduto_filho integer NOT NULL,
    quantidade integer DEFAULT 1
);

CREATE SEQUENCE public.fproduto_kit_codkit_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fproduto_kit_codkit_seq OWNED BY public.fproduto_kit.codkit;

CREATE SEQUENCE public.fproduto_kit_codproduto_filho_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fproduto_kit_codproduto_filho_seq OWNED BY public.fproduto_kit.codproduto_filho;

CREATE TABLE public.fproduto_movimento (
    codmov integer NOT NULL,
    codvariacao integer NOT NULL,
    tipo_mov character varying(255),
    quantidade numeric(15,4) NOT NULL,
    data_mov timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    documento character varying(255),
    observacao text,
    CONSTRAINT fproduto_movimento_tipo_mov_check CHECK (((tipo_mov)::text = ANY (ARRAY[('ENTRADA'::character varying)::text, ('SAIDA'::character varying)::text])))
);

CREATE SEQUENCE public.fproduto_movimento_codmov_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fproduto_movimento_codmov_seq OWNED BY public.fproduto_movimento.codmov;

CREATE TABLE public.fproduto_variacao (
    codvariacao integer NOT NULL,
    codproduto integer NOT NULL,
    cor character varying(255),
    tamanho character varying(255),
    codigo_barras character varying(255),
    preco_fornecedor numeric(15,2),
    preco_venda numeric(15,2),
    saldo_estoque numeric(15,4) DEFAULT 0,
    nome_especifico character varying(255),
    lote character varying(255),
    validade date,
    observacao text
);

CREATE SEQUENCE public.fproduto_variacao_codvariacao_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fproduto_variacao_codvariacao_seq OWNED BY public.fproduto_variacao.codvariacao;

CREATE TABLE public.frecibo (
    idrecibo public.docod NOT NULL,
    cliforrecibo public.docod,
    nomerecibo character varying(255),
    datarecibo public.dodata,
    valorrecibo public.dovalor,
    historicorecibo public.dmemo,
    usuariorecibo character varying(255),
    idempersarecibo public.docod,
    vencimetorecibo public.dodata
);

CREATE SEQUENCE public.frecibo_idrecibo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.frecibo_idrecibo_seq OWNED BY public.frecibo.idrecibo;

CREATE TABLE public.ftipo (
    codtipo public.docod NOT NULL,
    descricaotipo character varying(255),
    tipoconta public.dom1
);

CREATE SEQUENCE public.ftipo_codtipo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.ftipo_codtipo_seq OWNED BY public.ftipo.codtipo;

CREATE SEQUENCE public.ftipopagamento_codtipo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.ftipopagamento (
    codtipo public.docod DEFAULT nextval('public.ftipopagamento_codtipo_seq'::regclass) NOT NULL,
    descricaopagamento character varying(255),
    seriepagamento public.dom2
);

CREATE TABLE public.fusuario (
    codigo public.docod NOT NULL,
    nome character varying(255),
    senha character varying(255),
    datalimite public.dodata,
    acessomodulo public.dom1,
    cupom public.dom1,
    edita public.dom1,
    excluir public.dom1,
    acessomodulo1 public.dom1,
    acessomodulo2 public.dom1,
    acessomodulo3 public.dom1,
    acessomodulo4 public.dom1,
    diretoriopecuaria character varying(255),
    foto public.dom1
);

CREATE SEQUENCE public.fusuario_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fusuario_codigo_seq OWNED BY public.fusuario.codigo;

CREATE TABLE public.fvenda (
    id_venda integer NOT NULL,
    id_cliente integer,
    data_venda date DEFAULT CURRENT_DATE,
    condicao_pagamento integer,
    tipo_pagamento integer,
    centro_custo integer,
    valor_total numeric(15,2) DEFAULT 0.00,
    status character varying(255) DEFAULT 'ABERTA'::character varying,
    observacao text,
    id_recibo integer
);

CREATE SEQUENCE public.fvenda_id_venda_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fvenda_id_venda_seq OWNED BY public.fvenda.id_venda;

CREATE TABLE public.fvenda_item (
    id_item integer NOT NULL,
    id_venda integer NOT NULL,
    codvariacao integer,
    quantidade numeric(15,2) DEFAULT 1.00 NOT NULL,
    valor_unitario numeric(15,2) DEFAULT 0.00 NOT NULL,
    total_item numeric(15,2) DEFAULT 0.00 NOT NULL
);

CREATE SEQUENCE public.fvenda_item_id_item_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fvenda_item_id_item_seq OWNED BY public.fvenda_item.id_item;

CREATE SEQUENCE public.gen_fcfo_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_fcusto_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_fdia_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_fextrato_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_ffuncionario_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_fitem_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_flan_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_fmov_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_frecibo_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.gen_tmunicipo_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.gparametro (
    idparametro public.docod NOT NULL,
    modulo1 public.dom1,
    modulo2 public.dom1,
    modulo3 public.dom1,
    modulo4 public.dom1,
    usarateio public.dom1,
    data public.dodata
);

CREATE SEQUENCE public.gparametro_idparametro_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.gparametro_idparametro_seq OWNED BY public.gparametro.idparametro;

CREATE TABLE public.tcnae_servico (
    cnae character varying(12) NOT NULL,
    cnae_descricao text,
    codigo_item character varying(12) NOT NULL
);

CREATE TABLE public.tissqn (
    id integer NOT NULL,
    cod_ibge integer NOT NULL,
    uf character varying(2) NOT NULL,
    municipio character varying(150) NOT NULL,
    codigo_servico character varying(20) NOT NULL,
    codigo_municipal character varying(20),
    aliquota numeric(5,2) DEFAULT 5 NOT NULL,
    vigencia date,
    descricao text
);

CREATE SEQUENCE public.tissqn_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.tissqn_id_seq OWNED BY public.tissqn.id;

CREATE TABLE public.tmp_restaura (
    x text
);

CREATE SEQUENCE public.tmunicipio_refmunicipio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.tmunicipio (
    refmunicipio public.docod DEFAULT nextval('public.tmunicipio_refmunicipio_seq'::regclass) NOT NULL,
    nomemunucipio character varying(255),
    unfmunicipio public.dom2,
    paismunicipio character varying(255),
    paiscodigo public.dom5,
    codigoibge character varying(255)
);

CREATE SEQUENCE public.tmunicipio_ref_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.tncm (
    codigo character varying(255) NOT NULL,
    descricao text
);

CREATE TABLE public.tservico_lc116 (
    codigo_item character varying(12) NOT NULL,
    descricao text
);

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ativo_imobilizado_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ativo_manutencao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_dashboard_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_dashboard_widget_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_indicador ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_indicador_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_kpi ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_kpi_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_relatorio_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_relatorio_agendado_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_report ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_report_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_report_parameter_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_base_cep ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_base_cep_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_categoria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_categoria_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_cliente ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_cliente_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_contato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_contato_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_documento_fiscal_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_endereco ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_endereco_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_fornecedor_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_marca ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_marca_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_municipio ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_municipio_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_papel ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_papel_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_pessoa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_pessoa_fisica_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_pessoa_juridica_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_produto ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_ecommerce_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_imagem_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_kit_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_variacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_servico ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_servico_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_transportadora_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_unidade_medida_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_conferencia_fatura_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_contrato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_contrato_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_cotacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_cotacao_fornecedor_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_cotacao_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_pedido ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_pedido_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_pedido_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_recebimento_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_recebimento_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_solicitacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_solicitacao_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_auditoria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_auditoria_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_configuracao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_configuracao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_empresa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_empresa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_empresa_vinculo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_integration_event ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_integration_event_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_log_acesso_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_modulo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_modulo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_notificacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_notificacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_perfil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_perfil_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_perfil_permissao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_permissao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_permissao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_sessao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_sessao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_usuario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_empresa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_modulo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_perfil_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_crm_atividade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_crm_atividade_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_crm_oportunidade_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_deposito ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_deposito_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_endereco ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_endereco_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_expedicao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_expedicao_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_inventario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_inventario_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_inventario_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_lote ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_lote_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_movimentacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_reserva ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_reserva_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_saldo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_saldo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_serie ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_serie_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_transferencia_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_transferencia_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_analise_rentabilidade_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_aplicacao_financeira_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_aprovacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_baixa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_boleto ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_boleto_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_caixa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_caixa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_centro_custo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_conciliacao_bancaria_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_conciliacao_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_condicao_pagamento_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_conta_bancaria_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_emprestimo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_extrato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_extrato_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_fluxo_aprovacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_integracao_bancaria_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_lancamento_contabil_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_lancamento_partida_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_orcamento_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_orcamento_realizado_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_periodo_contabil_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_plano_contas_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_provisao_pdd_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_remessa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_remessa_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_renegociacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_retorno_bancario_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_retorno_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_stripe_customer_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_payment ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_stripe_payment_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_stripe_webhook_event_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_tipo_pagamento_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_titulo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_titulo_parcela_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_apuracao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_certificado_digital_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_cest ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cest_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_cfop ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cfop_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_cnae_servico ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cnae_servico_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_cte ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cte_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_cte_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cte_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_ecd ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_ecd_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_ecf ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_ecf_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_esocial ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_esocial_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_imposto ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_imposto_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_issqn ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_issqn_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_manifestacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_mdfe_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nbs ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nbs_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_ncm ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_ncm_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfce_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfce_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfe_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_evento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfe_evento_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfe_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfse_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfse_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_retorno ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfse_retorno_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_palavra_chave ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_palavra_chave_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_regra_tributaria_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_reinf ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_reinf_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_servico_lc116 ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_servico_lc116_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_sped_contribuicoes_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_sped_fiscal_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_analise_preditiva_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_assistente_auditoria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_assistente_auditoria_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_mensagem_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_message_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_sessao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_session_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_classificacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_config ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_config_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_embedding ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_embedding_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_prompt ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_prompt_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_prompt_template_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_migration_log ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_migration_log_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_apontamento_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_centro_trabalho_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_estrutura_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_romaneio_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_romaneio_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_roteiro_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_roteiro_operacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_qual_inspecao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_qual_nao_conformidade_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_qual_plano_inspecao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_rh_cargo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_cargo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_rh_folha ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_folha_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_folha_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_funcionario_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_srv_ordem_servico_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_srv_os_apontamento_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_srv_os_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_srv_os_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_bonificacao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_bonificacao_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_contrato_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_contrato_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_devolucao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_devolucao_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_pedido_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_pedido_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_regra_comissao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_tabela_preco_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_tabela_preco_item_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.dim_cliente ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_cliente_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.dim_empresa ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_empresa_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.dim_produto ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_produto_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.dim_tempo ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_tempo_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.ft_compras ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_compras_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.ft_estoque ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_estoque_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.ft_financeiro ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_financeiro_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.ft_producao ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_producao_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas_dl.ft_vendas ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_vendas_id_seq'::regclass);

ALTER TABLE ONLY public.fcaixa ALTER COLUMN codcaixa SET DEFAULT nextval('public.fcaixa_codcaixa_seq'::regclass);

ALTER TABLE ONLY public.fcentrocusto ALTER COLUMN codcusto SET DEFAULT nextval('public.fcentrocusto_codcusto_seq'::regclass);

ALTER TABLE ONLY public.fcusto ALTER COLUMN codcusto SET DEFAULT nextval('public.fcusto_codcusto_seq'::regclass);

ALTER TABLE ONLY public.fdatas ALTER COLUMN iddata SET DEFAULT nextval('public.fdatas_iddata_seq'::regclass);

ALTER TABLE ONLY public.fdia ALTER COLUMN refdia SET DEFAULT nextval('public.fdia_refdia_seq'::regclass);

ALTER TABLE ONLY public.fempresa ALTER COLUMN codigo SET DEFAULT nextval('public.fempresa_codigo_seq'::regclass);

ALTER TABLE ONLY public.fempresa_vinculo ALTER COLUMN codvinculo SET DEFAULT nextval('public.fempresa_vinculo_codvinculo_seq'::regclass);

ALTER TABLE ONLY public.fextrato ALTER COLUMN idlanextrato SET DEFAULT nextval('public.fextrato_idlanextrato_seq'::regclass);

ALTER TABLE ONLY public.ffinanceiro ALTER COLUMN codfinanceiro SET DEFAULT nextval('public.ffinanceiro_codfinanceiro_seq'::regclass);

ALTER TABLE ONLY public.ffuncionario ALTER COLUMN codfuncionario SET DEFAULT nextval('public.ffuncionario_codfuncionario_seq'::regclass);

ALTER TABLE ONLY public.fitem ALTER COLUMN refitem SET DEFAULT nextval('public.fitem_refitem_seq'::regclass);

ALTER TABLE ONLY public.fmov ALTER COLUMN idmov SET DEFAULT (nextval('public.fmov_idmov_seq'::regclass))::public.docod;

ALTER TABLE ONLY public.fmovimento ALTER COLUMN codmovimento SET DEFAULT nextval('public.fmovimento_codmovimento_seq'::regclass);

ALTER TABLE ONLY public.fmovimento_item ALTER COLUMN coditem SET DEFAULT nextval('public.fmovimento_item_coditem_seq'::regclass);

ALTER TABLE ONLY public.fnota ALTER COLUMN id_nota SET DEFAULT nextval('public.fnota_id_nota_seq'::regclass);

ALTER TABLE ONLY public.fnota_item ALTER COLUMN id_item SET DEFAULT nextval('public.fnota_item_id_item_seq'::regclass);

ALTER TABLE ONLY public.fpessoa ALTER COLUMN id_pessoa SET DEFAULT nextval('public.fpessoa_id_pessoa_seq'::regclass);

ALTER TABLE ONLY public.fproduto_ecommerce ALTER COLUMN codecommerce SET DEFAULT nextval('public.fproduto_ecommerce_codecommerce_seq'::regclass);

ALTER TABLE ONLY public.fproduto_fiscal ALTER COLUMN codproduto SET DEFAULT nextval('public.fproduto_fiscal_codproduto_seq'::regclass);

ALTER TABLE ONLY public.fproduto_imagem ALTER COLUMN codimagem SET DEFAULT nextval('public.fproduto_imagem_codimagem_seq'::regclass);

ALTER TABLE ONLY public.fproduto_kit ALTER COLUMN codkit SET DEFAULT nextval('public.fproduto_kit_codkit_seq'::regclass);

ALTER TABLE ONLY public.fproduto_kit ALTER COLUMN codproduto_filho SET DEFAULT nextval('public.fproduto_kit_codproduto_filho_seq'::regclass);

ALTER TABLE ONLY public.fproduto_movimento ALTER COLUMN codmov SET DEFAULT nextval('public.fproduto_movimento_codmov_seq'::regclass);

ALTER TABLE ONLY public.fproduto_variacao ALTER COLUMN codvariacao SET DEFAULT nextval('public.fproduto_variacao_codvariacao_seq'::regclass);

ALTER TABLE ONLY public.frecibo ALTER COLUMN idrecibo SET DEFAULT nextval('public.frecibo_idrecibo_seq'::regclass);

ALTER TABLE ONLY public.ftipo ALTER COLUMN codtipo SET DEFAULT nextval('public.ftipo_codtipo_seq'::regclass);

ALTER TABLE ONLY public.fusuario ALTER COLUMN codigo SET DEFAULT nextval('public.fusuario_codigo_seq'::regclass);

ALTER TABLE ONLY public.fvenda ALTER COLUMN id_venda SET DEFAULT nextval('public.fvenda_id_venda_seq'::regclass);

ALTER TABLE ONLY public.fvenda_item ALTER COLUMN id_item SET DEFAULT nextval('public.fvenda_item_id_item_seq'::regclass);

ALTER TABLE ONLY public.gparametro ALTER COLUMN idparametro SET DEFAULT nextval('public.gparametro_idparametro_seq'::regclass);

ALTER TABLE ONLY public.tissqn ALTER COLUMN id SET DEFAULT nextval('public.tissqn_id_seq'::regclass);

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado
    ADD CONSTRAINT bc_ativo_imobilizado_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado
    ADD CONSTRAINT bc_ativo_imobilizado_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao
    ADD CONSTRAINT bc_ativo_manutencao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao
    ADD CONSTRAINT bc_ativo_manutencao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard
    ADD CONSTRAINT bc_bi_dashboard_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard
    ADD CONSTRAINT bc_bi_dashboard_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget
    ADD CONSTRAINT bc_bi_dashboard_widget_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget
    ADD CONSTRAINT bc_bi_dashboard_widget_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_indicador
    ADD CONSTRAINT bc_bi_indicador_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_indicador
    ADD CONSTRAINT bc_bi_indicador_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_kpi
    ADD CONSTRAINT bc_bi_kpi_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_kpi
    ADD CONSTRAINT bc_bi_kpi_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio
    ADD CONSTRAINT bc_bi_relatorio_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio
    ADD CONSTRAINT bc_bi_relatorio_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter
    ADD CONSTRAINT bc_bi_report_parameter_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter
    ADD CONSTRAINT bc_bi_report_parameter_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_bi_report
    ADD CONSTRAINT bc_bi_report_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_bi_report
    ADD CONSTRAINT bc_bi_report_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_base_cep
    ADD CONSTRAINT bc_cad_base_cep_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_marca
    ADD CONSTRAINT bc_cad_marca_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_marca
    ADD CONSTRAINT bc_cad_marca_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_municipio
    ADD CONSTRAINT bc_cad_municipio_codigo_ibge_key UNIQUE (codigo_ibge);

ALTER TABLE ONLY brasil_saas.bc_cad_municipio
    ADD CONSTRAINT bc_cad_municipio_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT bc_cad_papel_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT bc_cad_papel_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica
    ADD CONSTRAINT bc_cad_pessoa_fisica_pessoa_id_key UNIQUE (pessoa_id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica
    ADD CONSTRAINT bc_cad_pessoa_fisica_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica
    ADD CONSTRAINT bc_cad_pessoa_juridica_pessoa_id_key UNIQUE (pessoa_id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica
    ADD CONSTRAINT bc_cad_pessoa_juridica_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa
    ADD CONSTRAINT bc_cad_pessoa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa
    ADD CONSTRAINT bc_cad_pessoa_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_produto_id_key UNIQUE (produto_id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem
    ADD CONSTRAINT bc_cad_produto_imagem_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT bc_cad_unidade_medida_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT bc_cad_unidade_medida_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao_item
    ADD CONSTRAINT bc_cmp_devolucao_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao_item
    ADD CONSTRAINT bc_cmp_devolucao_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao
    ADD CONSTRAINT bc_cmp_devolucao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao
    ADD CONSTRAINT bc_cmp_devolucao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_pedido_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao
    ADD CONSTRAINT bc_com_solicitacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao
    ADD CONSTRAINT bc_com_solicitacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_auth_source
    ADD CONSTRAINT bc_core_auth_source_pkey PRIMARY KEY (username);

ALTER TABLE ONLY brasil_saas.bc_core_banco
    ADD CONSTRAINT bc_core_banco_pkey PRIMARY KEY (compe);

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT bc_core_configuracao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT bc_core_configuracao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT bc_core_empresa_cnpj_key UNIQUE (cnpj);

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT bc_core_empresa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT bc_core_empresa_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_integration_event
    ADD CONSTRAINT bc_core_integration_event_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_integration_event
    ADD CONSTRAINT bc_core_integration_event_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso
    ADD CONSTRAINT bc_core_log_acesso_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_modulo
    ADD CONSTRAINT bc_core_modulo_chave_key UNIQUE (chave);

ALTER TABLE ONLY brasil_saas.bc_core_modulo
    ADD CONSTRAINT bc_core_modulo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_modulo
    ADD CONSTRAINT bc_core_modulo_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT bc_core_perfil_permissao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT bc_core_perfil_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT bc_core_perfil_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_permissao
    ADD CONSTRAINT bc_core_permissao_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY brasil_saas.bc_core_permissao
    ADD CONSTRAINT bc_core_permissao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_permissao
    ADD CONSTRAINT bc_core_permissao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_sessao
    ADD CONSTRAINT bc_core_sessao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_sessao
    ADD CONSTRAINT bc_core_sessao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_email_key UNIQUE (email);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT bc_core_usuario_empresa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT bc_core_usuario_modulo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT bc_core_usuario_perfil_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_username_key UNIQUE (username);

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_crm_lead
    ADD CONSTRAINT bc_crm_lead_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_crm_lead
    ADD CONSTRAINT bc_crm_lead_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_crm_tarefa
    ADD CONSTRAINT bc_crm_tarefa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_crm_tarefa
    ADD CONSTRAINT bc_crm_tarefa_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ctb_fechamento
    ADD CONSTRAINT bc_ctb_fechamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ctb_fechamento
    ADD CONSTRAINT bc_ctb_fechamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ctb_lancamento
    ADD CONSTRAINT bc_ctb_lancamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ctb_lancamento
    ADD CONSTRAINT bc_ctb_lancamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ctb_partida
    ADD CONSTRAINT bc_ctb_partida_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ctb_partida
    ADD CONSTRAINT bc_ctb_partida_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_dms_aprovacao
    ADD CONSTRAINT bc_dms_aprovacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_dms_aprovacao
    ADD CONSTRAINT bc_dms_aprovacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_dms_documento
    ADD CONSTRAINT bc_dms_documento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_dms_documento
    ADD CONSTRAINT bc_dms_documento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT bc_dms_versao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT bc_dms_versao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_esocial_evento
    ADD CONSTRAINT bc_esocial_evento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_esocial_evento
    ADD CONSTRAINT bc_esocial_evento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_deposito
    ADD CONSTRAINT bc_est_deposito_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_deposito
    ADD CONSTRAINT bc_est_deposito_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_movimentacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_caixa
    ADD CONSTRAINT bc_fin_caixa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_comissao
    ADD CONSTRAINT bc_fin_comissao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento
    ADD CONSTRAINT bc_fin_condicao_pagamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento
    ADD CONSTRAINT bc_fin_condicao_pagamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria
    ADD CONSTRAINT bc_fin_conta_bancaria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria
    ADD CONSTRAINT bc_fin_conta_bancaria_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo
    ADD CONSTRAINT bc_fin_emprestimo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo
    ADD CONSTRAINT bc_fin_emprestimo_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao
    ADD CONSTRAINT bc_fin_fluxo_aprovacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao
    ADD CONSTRAINT bc_fin_fluxo_aprovacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil
    ADD CONSTRAINT bc_fin_periodo_contabil_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil
    ADD CONSTRAINT bc_fin_periodo_contabil_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa
    ADD CONSTRAINT bc_fin_projecao_fluxo_caixa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa
    ADD CONSTRAINT bc_fin_projecao_fluxo_caixa_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd
    ADD CONSTRAINT bc_fin_provisao_pdd_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd
    ADD CONSTRAINT bc_fin_provisao_pdd_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT bc_fin_stripe_customer_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT bc_fin_stripe_customer_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_payment
    ADD CONSTRAINT bc_fin_stripe_payment_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_payment
    ADD CONSTRAINT bc_fin_stripe_payment_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event
    ADD CONSTRAINT bc_fin_stripe_webhook_event_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event
    ADD CONSTRAINT bc_fin_stripe_webhook_event_stripe_event_id_key UNIQUE (stripe_event_id);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event
    ADD CONSTRAINT bc_fin_stripe_webhook_event_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento
    ADD CONSTRAINT bc_fin_tipo_pagamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento
    ADD CONSTRAINT bc_fin_tipo_pagamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital
    ADD CONSTRAINT bc_fis_certificado_digital_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital
    ADD CONSTRAINT bc_fis_certificado_digital_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_cest
    ADD CONSTRAINT bc_fis_cest_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY brasil_saas.bc_fis_cest
    ADD CONSTRAINT bc_fis_cest_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_cfop
    ADD CONSTRAINT bc_fis_cfop_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY brasil_saas.bc_fis_cfop
    ADD CONSTRAINT bc_fis_cfop_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_cnae_servico
    ADD CONSTRAINT bc_fis_cnae_servico_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_cte_item
    ADD CONSTRAINT bc_fis_cte_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_ecd
    ADD CONSTRAINT bc_fis_ecd_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_ecd
    ADD CONSTRAINT bc_fis_ecd_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_ecf
    ADD CONSTRAINT bc_fis_ecf_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_ecf
    ADD CONSTRAINT bc_fis_ecf_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_esocial
    ADD CONSTRAINT bc_fis_esocial_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_esocial
    ADD CONSTRAINT bc_fis_esocial_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT bc_fis_imposto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT bc_fis_imposto_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_issqn
    ADD CONSTRAINT bc_fis_issqn_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao
    ADD CONSTRAINT bc_fis_manifestacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao
    ADD CONSTRAINT bc_fis_manifestacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe
    ADD CONSTRAINT bc_fis_mdfe_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe
    ADD CONSTRAINT bc_fis_mdfe_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_nbs
    ADD CONSTRAINT bc_fis_nbs_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY brasil_saas.bc_fis_nbs
    ADD CONSTRAINT bc_fis_nbs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_ncm
    ADD CONSTRAINT bc_fis_ncm_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY brasil_saas.bc_fis_ncm
    ADD CONSTRAINT bc_fis_ncm_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item
    ADD CONSTRAINT bc_fis_nfce_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_evento
    ADD CONSTRAINT bc_fis_nfe_evento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item
    ADD CONSTRAINT bc_fis_nfe_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item
    ADD CONSTRAINT bc_fis_nfse_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_retorno
    ADD CONSTRAINT bc_fis_nfse_retorno_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT bc_fis_obrigacao_entrega_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT bc_fis_obrigacao_entrega_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao
    ADD CONSTRAINT bc_fis_obrigacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao
    ADD CONSTRAINT bc_fis_obrigacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_palavra_chave
    ADD CONSTRAINT bc_fis_palavra_chave_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria
    ADD CONSTRAINT bc_fis_regra_tributaria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria
    ADD CONSTRAINT bc_fis_regra_tributaria_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_reinf
    ADD CONSTRAINT bc_fis_reinf_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_reinf
    ADD CONSTRAINT bc_fis_reinf_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_servico_lc116
    ADD CONSTRAINT bc_fis_servico_lc116_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY brasil_saas.bc_fis_servico_lc116
    ADD CONSTRAINT bc_fis_servico_lc116_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes
    ADD CONSTRAINT bc_fis_sped_contribuicoes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes
    ADD CONSTRAINT bc_fis_sped_contribuicoes_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal
    ADD CONSTRAINT bc_fis_sped_fiscal_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal
    ADD CONSTRAINT bc_fis_sped_fiscal_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva
    ADD CONSTRAINT bc_ia_analise_preditiva_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva
    ADD CONSTRAINT bc_ia_analise_preditiva_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_assistente_auditoria
    ADD CONSTRAINT bc_ia_assistente_auditoria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message
    ADD CONSTRAINT bc_ia_chat_message_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message
    ADD CONSTRAINT bc_ia_chat_message_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao
    ADD CONSTRAINT bc_ia_chat_sessao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao
    ADD CONSTRAINT bc_ia_chat_sessao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session
    ADD CONSTRAINT bc_ia_chat_session_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session
    ADD CONSTRAINT bc_ia_chat_session_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao
    ADD CONSTRAINT bc_ia_classificacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao
    ADD CONSTRAINT bc_ia_classificacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_config
    ADD CONSTRAINT bc_ia_config_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_config
    ADD CONSTRAINT bc_ia_config_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_embedding
    ADD CONSTRAINT bc_ia_embedding_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_embedding
    ADD CONSTRAINT bc_ia_embedding_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_prompt
    ADD CONSTRAINT bc_ia_prompt_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template
    ADD CONSTRAINT bc_ia_prompt_template_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template
    ADD CONSTRAINT bc_ia_prompt_template_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ia_prompt
    ADD CONSTRAINT bc_ia_prompt_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_migration_log
    ADD CONSTRAINT bc_migration_log_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_pcp_mps
    ADD CONSTRAINT bc_pcp_mps_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_pcp_mps
    ADD CONSTRAINT bc_pcp_mps_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prj_faturamento
    ADD CONSTRAINT bc_prj_faturamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prj_faturamento
    ADD CONSTRAINT bc_prj_faturamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prj_mudanca
    ADD CONSTRAINT bc_prj_mudanca_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prj_mudanca
    ADD CONSTRAINT bc_prj_mudanca_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prj_projeto
    ADD CONSTRAINT bc_prj_projeto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prj_projeto
    ADD CONSTRAINT bc_prj_projeto_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prj_risco
    ADD CONSTRAINT bc_prj_risco_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prj_risco
    ADD CONSTRAINT bc_prj_risco_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_alocacao_capacidade
    ADD CONSTRAINT bc_prod_alocacao_capacidade_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_alocacao_capacidade
    ADD CONSTRAINT bc_prod_alocacao_capacidade_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho
    ADD CONSTRAINT bc_prod_centro_trabalho_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho
    ADD CONSTRAINT bc_prod_centro_trabalho_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura
    ADD CONSTRAINT bc_prod_estrutura_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura
    ADD CONSTRAINT bc_prod_estrutura_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_item
    ADD CONSTRAINT bc_prod_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_ordem
    ADD CONSTRAINT bc_prod_ordem_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item
    ADD CONSTRAINT bc_prod_romaneio_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item
    ADD CONSTRAINT bc_prod_romaneio_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro
    ADD CONSTRAINT bc_prod_roteiro_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro
    ADD CONSTRAINT bc_prod_roteiro_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ptl_acesso
    ADD CONSTRAINT bc_ptl_acesso_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ptl_acesso
    ADD CONSTRAINT bc_ptl_acesso_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao
    ADD CONSTRAINT bc_qual_inspecao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao
    ADD CONSTRAINT bc_qual_inspecao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade
    ADD CONSTRAINT bc_qual_nao_conformidade_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade
    ADD CONSTRAINT bc_qual_nao_conformidade_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao
    ADD CONSTRAINT bc_qual_plano_inspecao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao
    ADD CONSTRAINT bc_qual_plano_inspecao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_rh_cargo
    ADD CONSTRAINT bc_rh_cargo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_rh_ferias
    ADD CONSTRAINT bc_rh_ferias_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_rh_ferias
    ADD CONSTRAINT bc_rh_ferias_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT bc_rh_folha_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_funcionario_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_rh_ponto
    ADD CONSTRAINT bc_rh_ponto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_rh_ponto
    ADD CONSTRAINT bc_rh_ponto_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_pedido_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco
    ADD CONSTRAINT bc_ven_tabela_preco_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco
    ADD CONSTRAINT bc_ven_tabela_preco_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wkf_definition
    ADD CONSTRAINT bc_wkf_definition_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wkf_definition
    ADD CONSTRAINT bc_wkf_definition_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wkf_instance
    ADD CONSTRAINT bc_wkf_instance_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wkf_instance
    ADD CONSTRAINT bc_wkf_instance_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wkf_stage
    ADD CONSTRAINT bc_wkf_stage_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wkf_stage
    ADD CONSTRAINT bc_wkf_stage_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wms_onda_item
    ADD CONSTRAINT bc_wms_onda_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wms_onda_item
    ADD CONSTRAINT bc_wms_onda_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wms_onda
    ADD CONSTRAINT bc_wms_onda_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wms_onda
    ADD CONSTRAINT bc_wms_onda_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wms_volume_item
    ADD CONSTRAINT bc_wms_volume_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wms_volume_item
    ADD CONSTRAINT bc_wms_volume_item_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_wms_volume
    ADD CONSTRAINT bc_wms_volume_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas.bc_wms_volume
    ADD CONSTRAINT bc_wms_volume_uuid_key UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT uk_apuracao UNIQUE (empresa_id, imposto_id, competencia);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT uk_bc_cad_produto_ecommerce_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT uk_bc_com_pedido_item_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT uk_bc_com_pedido_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT uk_bc_est_mov_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT uk_bc_est_saldo_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_caixa
    ADD CONSTRAINT uk_bc_fin_caixa_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT uk_bc_fis_apuracao_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_rh_cargo
    ADD CONSTRAINT uk_bc_rh_cargo_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT uk_bc_rh_folha_item_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT uk_bc_rh_folha_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT uk_bc_rh_funcionario_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT uk_bc_ven_pedido_item_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT uk_bc_ven_pedido_uuid UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT uk_cliente_pessoa UNIQUE (empresa_id, pessoa_id);

ALTER TABLE ONLY brasil_saas.bc_fis_cnae_servico
    ADD CONSTRAINT uk_cnae_codigo_lc116 UNIQUE (codigo, lc116_codigo);

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT uk_configuracao UNIQUE (empresa_id, chave);

ALTER TABLE ONLY brasil_saas.bc_ctb_fechamento
    ADD CONSTRAINT uk_ctb_fechamento UNIQUE (empresa_id, periodo);

ALTER TABLE ONLY brasil_saas.bc_dms_documento
    ADD CONSTRAINT uk_dms_documento UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT uk_dms_versao UNIQUE (documento_id, versao);

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT uk_empresa_vinculo UNIQUE (empresa_id, empresa_vinculada_id, tipo);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT uk_fin_stripe_customer_empresa_pessoa UNIQUE (empresa_id, pessoa_id);

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT uk_fin_stripe_customer_stripe UNIQUE (stripe_customer_id);

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT uk_fornecedor_pessoa UNIQUE (empresa_id, pessoa_id);

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT uk_imposto UNIQUE (empresa_id, sigla);

ALTER TABLE ONLY brasil_saas.bc_fis_issqn
    ADD CONSTRAINT uk_issqn_ibge_codigo UNIQUE (cod_ibge, codigo);

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT uk_obrigacao_entrega UNIQUE (empresa_id, obrigacao_id, competencia);

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT uk_papel UNIQUE (empresa_id, nome);

ALTER TABLE ONLY brasil_saas.bc_pcp_mps
    ADD CONSTRAINT uk_pcp_mps UNIQUE (empresa_id, periodo, produto_id);

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT uk_perfil_empresa UNIQUE (empresa_id, nome);

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT uk_perfil_permissao UNIQUE (perfil_id, permissao_id);

ALTER TABLE ONLY brasil_saas.bc_prj_projeto
    ADD CONSTRAINT uk_prj_projeto UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho
    ADD CONSTRAINT uk_prod_centro_trabalho_empresa_codigo UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT uk_prod_romaneio_empresa_numero UNIQUE (empresa_id, numero);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro
    ADD CONSTRAINT uk_prod_roteiro_empresa_produto_codigo_versao UNIQUE (empresa_id, produto_id, codigo, versao);

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT uk_produto_codigo UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT uk_produto_kit UNIQUE (kit_id, item_id);

ALTER TABLE ONLY brasil_saas.bc_ptl_acesso
    ADD CONSTRAINT uk_ptl_acesso_token UNIQUE (token);

ALTER TABLE ONLY brasil_saas.bc_rh_ponto
    ADD CONSTRAINT uk_rh_ponto UNIQUE (empresa_id, funcionario_id, data);

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT uk_servico_codigo UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT uk_srv_os_numero UNIQUE (empresa_id, numero);

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT uk_transportadora_pessoa UNIQUE (empresa_id, pessoa_id);

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT uk_unidade_medida UNIQUE (empresa_id, sigla);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT uk_usuario_modulo UNIQUE (usuario_id, modulo_id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT uk_usuario_papel_nome UNIQUE (usuario_id, perfil_nome);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT uk_usuario_perfil UNIQUE (usuario_id, perfil_id);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT uk_ven_tabela_preco_item_produto UNIQUE (tabela_preco_id, produto_id);

ALTER TABLE ONLY brasil_saas.bc_wkf_definition
    ADD CONSTRAINT uk_wkf_definition UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_wms_onda
    ADD CONSTRAINT uk_wms_onda UNIQUE (empresa_id, deposito_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_wms_volume
    ADD CONSTRAINT uk_wms_volume UNIQUE (empresa_id, expedicao_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_prod_item
    ADD CONSTRAINT ukbouojof1acc60wi84fkrr2thm UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_fin_comissao
    ADD CONSTRAINT ukkiimsqq5tikgf0gnsw4lymuk9 UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_ordem
    ADD CONSTRAINT ukm4x148ro7hacu4ylqbxsu9e4n UNIQUE (uuid);

ALTER TABLE ONLY brasil_saas.bc_prod_ordem
    ADD CONSTRAINT uksfiffcaxbc909df77yqfuytcy UNIQUE (numero);

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado
    ADD CONSTRAINT uq_ativo_empresa_codigo UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao
    ADD CONSTRAINT uq_manut_empresa_numero UNIQUE (empresa_id, numero);

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao
    ADD CONSTRAINT uq_qual_inspecao_empresa_numero UNIQUE (empresa_id, numero);

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade
    ADD CONSTRAINT uq_qual_nc_empresa_numero UNIQUE (empresa_id, numero);

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao
    ADD CONSTRAINT uq_qual_plano_empresa_codigo UNIQUE (empresa_id, codigo);

ALTER TABLE ONLY brasil_saas_dl.dim_cliente
    ADD CONSTRAINT dim_cliente_cliente_id_key UNIQUE (cliente_id);

ALTER TABLE ONLY brasil_saas_dl.dim_cliente
    ADD CONSTRAINT dim_cliente_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.dim_empresa
    ADD CONSTRAINT dim_empresa_empresa_id_key UNIQUE (empresa_id);

ALTER TABLE ONLY brasil_saas_dl.dim_empresa
    ADD CONSTRAINT dim_empresa_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.dim_produto
    ADD CONSTRAINT dim_produto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.dim_produto
    ADD CONSTRAINT dim_produto_produto_id_key UNIQUE (produto_id);

ALTER TABLE ONLY brasil_saas_dl.dim_tempo
    ADD CONSTRAINT dim_tempo_data_key UNIQUE (data);

ALTER TABLE ONLY brasil_saas_dl.dim_tempo
    ADD CONSTRAINT dim_tempo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.ft_compras
    ADD CONSTRAINT ft_compras_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.ft_estoque
    ADD CONSTRAINT ft_estoque_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.ft_financeiro
    ADD CONSTRAINT ft_financeiro_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.ft_producao
    ADD CONSTRAINT ft_producao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY brasil_saas_dl.ft_vendas
    ADD CONSTRAINT ft_vendas_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.base_cep
    ADD CONSTRAINT base_cep_pkey PRIMARY KEY (cep);

ALTER TABLE ONLY public.fcaixa
    ADD CONSTRAINT fcaixa_pkey PRIMARY KEY (codcaixa);

ALTER TABLE ONLY public.fcentrocusto
    ADD CONSTRAINT fcentrocusto_pkey PRIMARY KEY (codcusto);

ALTER TABLE ONLY public.fempresa_vinculo
    ADD CONSTRAINT fempresa_vinculo_codmatriz_codfilial_key UNIQUE (codmatriz, codfilial);

ALTER TABLE ONLY public.fempresa_vinculo
    ADD CONSTRAINT fempresa_vinculo_pkey PRIMARY KEY (codvinculo);

ALTER TABLE ONLY public.ffinanceiro
    ADD CONSTRAINT ffinanceiro_pkey PRIMARY KEY (codfinanceiro);

ALTER TABLE ONLY public.fmovimento_item
    ADD CONSTRAINT fmovimento_item_pkey PRIMARY KEY (coditem);

ALTER TABLE ONLY public.fmovimento
    ADD CONSTRAINT fmovimento_pkey PRIMARY KEY (codmovimento);

ALTER TABLE ONLY public.fnota_item
    ADD CONSTRAINT fnota_item_pkey PRIMARY KEY (id_item);

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_pkey PRIMARY KEY (id_nota);

ALTER TABLE ONLY public.fpessoa
    ADD CONSTRAINT fpessoa_pkey PRIMARY KEY (id_pessoa);

ALTER TABLE ONLY public.fproduto_ecommerce
    ADD CONSTRAINT fproduto_ecommerce_pkey PRIMARY KEY (codecommerce);

ALTER TABLE ONLY public.fproduto_fiscal
    ADD CONSTRAINT fproduto_fiscal_pkey PRIMARY KEY (codproduto);

ALTER TABLE ONLY public.fproduto_imagem
    ADD CONSTRAINT fproduto_imagem_pkey PRIMARY KEY (codimagem);

ALTER TABLE ONLY public.fproduto_kit
    ADD CONSTRAINT fproduto_kit_pkey PRIMARY KEY (codkit, codproduto_filho);

ALTER TABLE ONLY public.fproduto_movimento
    ADD CONSTRAINT fproduto_movimento_pkey PRIMARY KEY (codmov);

ALTER TABLE ONLY public.fproduto_variacao
    ADD CONSTRAINT fproduto_variacao_pkey PRIMARY KEY (codvariacao);

ALTER TABLE ONLY public.fvenda_item
    ADD CONSTRAINT fvenda_item_pkey PRIMARY KEY (id_item);

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_pkey PRIMARY KEY (id_venda);

ALTER TABLE ONLY public.fcfo
    ADD CONSTRAINT pk_fcfo PRIMARY KEY (codcfo);

ALTER TABLE ONLY public.fcondicao
    ADD CONSTRAINT pk_fcondicao PRIMARY KEY (codcondicao);

ALTER TABLE ONLY public.fcusto
    ADD CONSTRAINT pk_fcusto PRIMARY KEY (codcusto);

ALTER TABLE ONLY public.fdatas
    ADD CONSTRAINT pk_fdatas_1 PRIMARY KEY (iddata);

ALTER TABLE ONLY public.fdia
    ADD CONSTRAINT pk_fdia PRIMARY KEY (refdia);

ALTER TABLE ONLY public.fdocumento
    ADD CONSTRAINT pk_fdocumento_1 PRIMARY KEY (coddoc);

ALTER TABLE ONLY public.fempresa
    ADD CONSTRAINT pk_fempresa PRIMARY KEY (codigo);

ALTER TABLE ONLY public.fextrato
    ADD CONSTRAINT pk_fextrato PRIMARY KEY (idlanextrato);

ALTER TABLE ONLY public.ffuncionario
    ADD CONSTRAINT pk_ffuncionario PRIMARY KEY (codfuncionario);

ALTER TABLE ONLY public.fitem
    ADD CONSTRAINT pk_fitem PRIMARY KEY (refitem);

ALTER TABLE ONLY public.flan
    ADD CONSTRAINT pk_flan PRIMARY KEY (idlan);

ALTER TABLE ONLY public.fmov
    ADD CONSTRAINT pk_fmov PRIMARY KEY (idmov);

ALTER TABLE ONLY public.fproduto
    ADD CONSTRAINT pk_fproduto PRIMARY KEY (codproduto);

ALTER TABLE ONLY public.frecibo
    ADD CONSTRAINT pk_frecibo_1 PRIMARY KEY (idrecibo);

ALTER TABLE ONLY public.ftipo
    ADD CONSTRAINT pk_ftipo_1 PRIMARY KEY (codtipo);

ALTER TABLE ONLY public.ftipopagamento
    ADD CONSTRAINT pk_ftipopagamento_1 PRIMARY KEY (codtipo);

ALTER TABLE ONLY public.fusuario
    ADD CONSTRAINT pk_fusuario PRIMARY KEY (codigo);

ALTER TABLE ONLY public.gparametro
    ADD CONSTRAINT pk_gparametro PRIMARY KEY (idparametro);

ALTER TABLE ONLY public.tmunicipio
    ADD CONSTRAINT pk_tmunicipio_1 PRIMARY KEY (refmunicipio);

ALTER TABLE ONLY public.tcnae_servico
    ADD CONSTRAINT tcnae_servico_pkey PRIMARY KEY (cnae, codigo_item);

ALTER TABLE ONLY public.tissqn
    ADD CONSTRAINT tissqn_cod_ibge_codigo_servico_key UNIQUE (cod_ibge, codigo_servico);

ALTER TABLE ONLY public.tissqn
    ADD CONSTRAINT tissqn_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tncm
    ADD CONSTRAINT tncm_pkey PRIMARY KEY (codigo);

ALTER TABLE ONLY public.tservico_lc116
    ADD CONSTRAINT tservico_lc116_pkey PRIMARY KEY (codigo_item);

CREATE INDEX idx_apuracao_empresa ON brasil_saas.bc_fis_apuracao USING btree (empresa_id, competencia);

CREATE INDEX idx_assist_empresa_data ON brasil_saas.bc_ia_assistente_auditoria USING btree (empresa_id, created_at DESC) WHERE (deleted_at IS NULL);

CREATE INDEX idx_assist_identity ON brasil_saas.bc_ia_assistente_auditoria USING btree (identity_id) WHERE ((deleted_at IS NULL) AND (identity_id IS NOT NULL));

CREATE INDEX idx_assist_status ON brasil_saas.bc_ia_assistente_auditoria USING btree (status) WHERE (deleted_at IS NULL);

CREATE INDEX idx_ativo_empresa_status ON brasil_saas.bc_ativo_imobilizado USING btree (empresa_id, status);

CREATE INDEX idx_auditoria_empresa ON brasil_saas.bc_core_auditoria USING btree (empresa_id, tabela, created_at);

CREATE INDEX idx_base_cep_cep ON brasil_saas.bc_cad_base_cep USING btree (cep);

CREATE INDEX idx_bc_core_perfil_pai_id ON brasil_saas.bc_core_perfil USING btree (perfil_pai_id);

CREATE INDEX idx_bc_fin_aprovacao_documento ON brasil_saas.bc_fin_aprovacao USING btree (tipo_documento, documento_id) WHERE (deleted_at IS NULL);

CREATE INDEX idx_bc_fin_aprovacao_pendentes ON brasil_saas.bc_fin_aprovacao USING btree (empresa_id, status, data_solicitacao) WHERE (deleted_at IS NULL);

CREATE INDEX idx_bc_fin_aprovacao_titulo_status ON brasil_saas.bc_fin_aprovacao USING btree (titulo_id, status) WHERE (deleted_at IS NULL);

CREATE INDEX idx_bc_municipio_nome ON brasil_saas.bc_cad_municipio USING btree (nome);

CREATE INDEX idx_bi_dashboard_empresa ON brasil_saas.bc_bi_dashboard USING btree (empresa_id);

CREATE INDEX idx_bi_dashboard_type ON brasil_saas.bc_bi_dashboard USING btree (dashboard_type);

CREATE INDEX idx_bi_dashboard_widget_dashboard ON brasil_saas.bc_bi_dashboard_widget USING btree (dashboard_id);

CREATE INDEX idx_bi_indicador_empresa ON brasil_saas.bc_bi_indicador USING btree (empresa_id);

CREATE INDEX idx_bi_kpi_empresa ON brasil_saas.bc_bi_kpi USING btree (empresa_id);

CREATE INDEX idx_bi_kpi_type ON brasil_saas.bc_bi_kpi USING btree (kpi_type);

CREATE INDEX idx_bi_relatorio_agendado_empresa ON brasil_saas.bc_bi_relatorio_agendado USING btree (empresa_id);

CREATE INDEX idx_bi_relatorio_agendado_relatorio ON brasil_saas.bc_bi_relatorio_agendado USING btree (relatorio_id);

CREATE INDEX idx_bi_relatorio_empresa ON brasil_saas.bc_bi_relatorio USING btree (empresa_id);

CREATE INDEX idx_bi_report_category ON brasil_saas.bc_bi_report USING btree (category);

CREATE INDEX idx_bi_report_empresa ON brasil_saas.bc_bi_report USING btree (empresa_id);

CREATE INDEX idx_bi_report_parameter_report ON brasil_saas.bc_bi_report_parameter USING btree (report_id);

CREATE INDEX idx_cliente_empresa ON brasil_saas.bc_cad_cliente USING btree (empresa_id);

CREATE INDEX idx_com_conferencia_fatura_nfe ON brasil_saas.bc_com_conferencia_fatura USING btree (empresa_id, nfe_id);

CREATE INDEX idx_com_conferencia_fatura_pedido ON brasil_saas.bc_com_conferencia_fatura USING btree (empresa_id, pedido_id, status);

CREATE INDEX idx_com_conferencia_fatura_titulo ON brasil_saas.bc_com_conferencia_fatura USING btree (empresa_id, titulo_id);

CREATE INDEX idx_com_cotacao_empresa_status ON brasil_saas.bc_com_cotacao USING btree (empresa_id, status);

CREATE INDEX idx_com_cotacao_fornecedor ON brasil_saas.bc_com_cotacao_fornecedor USING btree (empresa_id, fornecedor_id);

CREATE INDEX idx_com_cotacao_fornecedor_cotacao ON brasil_saas.bc_com_cotacao_fornecedor USING btree (empresa_id, cotacao_id);

CREATE INDEX idx_com_cotacao_item_fornecedor ON brasil_saas.bc_com_cotacao_item USING btree (empresa_id, cotacao_fornecedor_id);

CREATE INDEX idx_com_cotacao_solicitacao ON brasil_saas.bc_com_cotacao USING btree (empresa_id, solicitacao_id);

CREATE INDEX idx_com_item_pedido ON brasil_saas.bc_com_pedido_item USING btree (pedido_id);

CREATE INDEX idx_com_pedido_empresa ON brasil_saas.bc_com_pedido USING btree (empresa_id, status);

CREATE INDEX idx_com_pedido_fornecedor ON brasil_saas.bc_com_pedido USING btree (fornecedor_id);

CREATE INDEX idx_com_recebimento_empresa_data ON brasil_saas.bc_com_recebimento USING btree (empresa_id, data_recebimento DESC);

CREATE INDEX idx_com_recebimento_empresa_status ON brasil_saas.bc_com_recebimento USING btree (empresa_id, status);

CREATE INDEX idx_com_recebimento_item_produto ON brasil_saas.bc_com_recebimento_item USING btree (empresa_id, produto_id);

CREATE INDEX idx_com_recebimento_item_recebimento ON brasil_saas.bc_com_recebimento_item USING btree (empresa_id, recebimento_id);

CREATE INDEX idx_com_recebimento_pedido ON brasil_saas.bc_com_recebimento USING btree (empresa_id, pedido_id);

CREATE INDEX idx_com_solicitacao_empresa_status ON brasil_saas.bc_com_solicitacao USING btree (empresa_id, status);

CREATE INDEX idx_com_solicitacao_item_empresa_solicitacao ON brasil_saas.bc_com_solicitacao_item USING btree (empresa_id, solicitacao_id);

CREATE INDEX idx_crm_atividade_agendada ON brasil_saas.bc_crm_atividade USING btree (empresa_id, data_agendada, status);

CREATE INDEX idx_crm_oportunidade_vendedor_status ON brasil_saas.bc_crm_oportunidade USING btree (empresa_id, vendedor_id, status);

CREATE INDEX idx_endereco_pessoa ON brasil_saas.bc_cad_endereco USING btree (pessoa_id);

CREATE INDEX idx_est_endereco_deposito_ativo ON brasil_saas.bc_est_endereco USING btree (empresa_id, deposito_id, ativo);

CREATE INDEX idx_est_expedicao_item ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, expedicao_id);

CREATE INDEX idx_est_expedicao_item_auditoria ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, expedicao_id, created_at);

CREATE INDEX idx_est_expedicao_item_deleted ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, expedicao_id) WHERE (deleted_at IS NULL);

CREATE INDEX idx_est_expedicao_item_endereco ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, endereco_id, lote_id, status);

CREATE INDEX idx_est_expedicao_item_lote ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, lote_id, status);

CREATE INDEX idx_est_expedicao_status ON brasil_saas.bc_est_expedicao USING btree (empresa_id, deposito_id, status, data_abertura DESC);

CREATE INDEX idx_est_inventario_empresa_status ON brasil_saas.bc_est_inventario USING btree (empresa_id, deposito_id, status);

CREATE INDEX idx_est_inventario_item_inventario ON brasil_saas.bc_est_inventario_item USING btree (inventario_id);

CREATE INDEX idx_est_inventario_item_produto ON brasil_saas.bc_est_inventario_item USING btree (produto_id, lote_id, endereco_id);

CREATE INDEX idx_est_lote_fefo ON brasil_saas.bc_est_lote USING btree (empresa_id, deposito_id, produto_id, status, data_validade);

CREATE INDEX idx_est_lote_validade_ativo ON brasil_saas.bc_est_lote USING btree (empresa_id, deposito_id, produto_id, status, data_validade, id);

CREATE INDEX idx_est_mov_produto ON brasil_saas.bc_est_movimentacao USING btree (empresa_id, produto_id, data_movimento);

CREATE INDEX idx_est_reserva_endereco_status ON brasil_saas.bc_est_reserva USING btree (empresa_id, deposito_id, endereco_id, produto_id, lote_id, status);

CREATE INDEX idx_est_reserva_lote_status ON brasil_saas.bc_est_reserva USING btree (empresa_id, deposito_id, produto_id, lote_id, status);

CREATE INDEX idx_est_reserva_pedido ON brasil_saas.bc_est_reserva USING btree (empresa_id, pedido_venda_id, status);

CREATE INDEX idx_est_reserva_produto_status ON brasil_saas.bc_est_reserva USING btree (empresa_id, deposito_id, produto_id, status);

CREATE INDEX idx_est_saldo_deposito_produto ON brasil_saas.bc_est_saldo USING btree (deposito_id, produto_id);

CREATE INDEX idx_est_saldo_produto ON brasil_saas.bc_est_saldo USING btree (empresa_id, produto_id);

CREATE INDEX idx_est_serie_produto_deposito ON brasil_saas.bc_est_serie USING btree (empresa_id, produto_id, deposito_id, status);

CREATE INDEX idx_est_transferencia_empresa_data ON brasil_saas.bc_est_transferencia USING btree (empresa_id, data_transferencia DESC);

CREATE INDEX idx_est_transferencia_item_transferencia ON brasil_saas.bc_est_transferencia_item USING btree (transferencia_id);

CREATE INDEX idx_est_transferencia_item_wms ON brasil_saas.bc_est_transferencia_item USING btree (lote_id, endereco_origem_id, endereco_destino_id);

CREATE INDEX idx_fin_baixa_empresa_conta_data ON brasil_saas.bc_fin_baixa USING btree (empresa_id, conta_bancaria_id, data_baixa) WHERE (deleted_at IS NULL);

CREATE INDEX idx_fin_baixa_empresa_titulo_data ON brasil_saas.bc_fin_baixa USING btree (empresa_id, titulo_id, data_baixa) WHERE (deleted_at IS NULL);

CREATE INDEX idx_fin_baixa_empresa_titulo_parcela_data ON brasil_saas.bc_fin_baixa USING btree (empresa_id, titulo_id, parcela_id, data_baixa DESC) WHERE (deleted_at IS NULL);

CREATE INDEX idx_fin_extrato_conta_data_id ON brasil_saas.bc_fin_extrato USING btree (conta_bancaria_id, data_movimento DESC, id DESC) WHERE (deleted_at IS NULL);

CREATE INDEX idx_fin_extrato_empresa_conciliado ON brasil_saas.bc_fin_extrato USING btree (empresa_id, conciliado, data_movimento DESC) WHERE (deleted_at IS NULL);

CREATE INDEX idx_fin_stripe_customer_empresa ON brasil_saas.bc_fin_stripe_customer USING btree (empresa_id);

CREATE INDEX idx_fin_stripe_payment_invoice ON brasil_saas.bc_fin_stripe_payment USING btree (invoice_id) WHERE (invoice_id IS NOT NULL);

CREATE INDEX idx_fin_stripe_payment_titulo ON brasil_saas.bc_fin_stripe_payment USING btree (empresa_id, titulo_id);

CREATE INDEX idx_fin_stripe_webhook_empresa ON brasil_saas.bc_fin_stripe_webhook_event USING btree (empresa_id, processed_at);

CREATE INDEX idx_fin_titulo_empresa_status_vencimento ON brasil_saas.bc_fin_titulo USING btree (empresa_id, status, data_vencimento) WHERE (deleted_at IS NULL);

CREATE INDEX idx_fin_titulo_parcela_empresa_status_vencimento ON brasil_saas.bc_fin_titulo_parcela USING btree (empresa_id, status, data_vencimento) WHERE (deleted_at IS NULL);

CREATE INDEX idx_fis_nfe_pedido_compra ON brasil_saas.bc_fis_nfe USING btree (empresa_id, pedido_compra_id);

CREATE INDEX idx_fis_nfe_pedido_venda ON brasil_saas.bc_fis_nfe USING btree (empresa_id, pedido_venda_id);

CREATE INDEX idx_ia_analise_preditiva_empresa ON brasil_saas.bc_ia_analise_preditiva USING btree (empresa_id);

CREATE INDEX idx_ia_chat_mensagem_sessao ON brasil_saas.bc_ia_chat_mensagem USING btree (sessao_id);

CREATE INDEX idx_ia_chat_message_session ON brasil_saas.bc_ia_chat_message USING btree (session_id);

CREATE INDEX idx_ia_chat_message_type ON brasil_saas.bc_ia_chat_message USING btree (message_type);

CREATE INDEX idx_ia_chat_sessao_empresa ON brasil_saas.bc_ia_chat_sessao USING btree (empresa_id);

CREATE INDEX idx_ia_chat_session_empresa ON brasil_saas.bc_ia_chat_session USING btree (empresa_id);

CREATE INDEX idx_ia_chat_session_type ON brasil_saas.bc_ia_chat_session USING btree (session_type);

CREATE INDEX idx_ia_chat_session_user ON brasil_saas.bc_ia_chat_session USING btree (user_id);

CREATE INDEX idx_ia_classificacao_empresa ON brasil_saas.bc_ia_classificacao USING btree (empresa_id);

CREATE INDEX idx_ia_config_empresa ON brasil_saas.bc_ia_config USING btree (empresa_id);

CREATE INDEX idx_ia_embedding_empresa ON brasil_saas.bc_ia_embedding USING btree (empresa_id);

CREATE INDEX idx_ia_prompt_empresa ON brasil_saas.bc_ia_prompt USING btree (empresa_id);

CREATE INDEX idx_ia_prompt_template_category ON brasil_saas.bc_ia_prompt_template USING btree (category);

CREATE INDEX idx_ia_prompt_template_empresa ON brasil_saas.bc_ia_prompt_template USING btree (empresa_id);

CREATE INDEX idx_log_acesso_usuario ON brasil_saas.bc_core_log_acesso USING btree (usuario_id, created_at);

CREATE INDEX idx_manut_empresa_status ON brasil_saas.bc_ativo_manutencao USING btree (empresa_id, status);

CREATE INDEX idx_nfe_chave ON brasil_saas.bc_fis_nfe USING btree (chave_acesso);

CREATE INDEX idx_nfe_empresa ON brasil_saas.bc_fis_nfe USING btree (empresa_id, data_emissao);

CREATE INDEX idx_nfe_item_nfe ON brasil_saas.bc_fis_nfe_item USING btree (nfe_id);

CREATE INDEX idx_nfse_chave ON brasil_saas.bc_fis_nfse USING btree (codigo_verificacao);

CREATE INDEX idx_nfse_empresa ON brasil_saas.bc_fis_nfse USING btree (empresa_id, data_emissao);

CREATE INDEX idx_notificacao_usuario ON brasil_saas.bc_core_notificacao USING btree (usuario_id, lida);

CREATE INDEX idx_palavra ON brasil_saas.bc_fis_palavra_chave USING btree (palavra) WHERE (deleted_at IS NULL);

CREATE INDEX idx_pessoa_empresa ON brasil_saas.bc_cad_pessoa USING btree (empresa_id, nome);

CREATE INDEX idx_prod_apontamento_empresa ON brasil_saas.bc_prod_apontamento USING btree (empresa_id);

CREATE INDEX idx_prod_apontamento_item ON brasil_saas.bc_prod_apontamento USING btree (item_producao_id);

CREATE INDEX idx_prod_apontamento_producao ON brasil_saas.bc_prod_apontamento USING btree (producao_id);

CREATE INDEX idx_prod_centro_trabalho_empresa_ativo ON brasil_saas.bc_prod_centro_trabalho USING btree (empresa_id, ativo);

CREATE INDEX idx_prod_estrutura_filho ON brasil_saas.bc_prod_estrutura USING btree (empresa_id, produto_filho_id) WHERE (deleted_at IS NULL);

CREATE INDEX idx_prod_estrutura_pai ON brasil_saas.bc_prod_estrutura USING btree (empresa_id, produto_pai_id, ativo) WHERE (deleted_at IS NULL);

CREATE INDEX idx_prod_item_empresa ON brasil_saas.bc_prod_item USING btree (empresa_id);

CREATE INDEX idx_prod_item_producao ON brasil_saas.bc_prod_item USING btree (producao_id);

CREATE INDEX idx_prod_ordem_empresa ON brasil_saas.bc_prod_ordem USING btree (empresa_id);

CREATE INDEX idx_prod_romaneio_empresa ON brasil_saas.bc_prod_romaneio USING btree (empresa_id);

CREATE INDEX idx_prod_romaneio_item_produto ON brasil_saas.bc_prod_romaneio_item USING btree (produto_id);

CREATE INDEX idx_prod_romaneio_item_romaneio ON brasil_saas.bc_prod_romaneio_item USING btree (romaneio_id);

CREATE INDEX idx_prod_romaneio_producao ON brasil_saas.bc_prod_romaneio USING btree (producao_id);

CREATE INDEX idx_prod_roteiro_empresa_produto ON brasil_saas.bc_prod_roteiro USING btree (empresa_id, produto_id, ativo);

CREATE INDEX idx_prod_roteiro_operacao_roteiro ON brasil_saas.bc_prod_roteiro_operacao USING btree (empresa_id, roteiro_id, sequencia);

CREATE INDEX idx_produto_empresa ON brasil_saas.bc_cad_produto USING btree (empresa_id, nome);

CREATE INDEX idx_qual_inspecao_empresa_status ON brasil_saas.bc_qual_inspecao USING btree (empresa_id, status);

CREATE INDEX idx_qual_nc_empresa_status ON brasil_saas.bc_qual_nao_conformidade USING btree (empresa_id, status);

CREATE INDEX idx_rh_folha_empresa ON brasil_saas.bc_rh_folha USING btree (empresa_id, competencia);

CREATE INDEX idx_rh_folha_item_folha ON brasil_saas.bc_rh_folha_item USING btree (folha_id);

CREATE INDEX idx_rh_func_empresa ON brasil_saas.bc_rh_funcionario USING btree (empresa_id);

CREATE INDEX idx_servico_empresa ON brasil_saas.bc_cad_servico USING btree (empresa_id);

CREATE INDEX idx_sessao_usuario ON brasil_saas.bc_core_sessao USING btree (usuario_id, expira_at);

CREATE INDEX idx_srv_os_apont_os ON brasil_saas.bc_srv_os_apontamento USING btree (os_id);

CREATE INDEX idx_srv_os_empresa ON brasil_saas.bc_srv_ordem_servico USING btree (empresa_id, status);

CREATE INDEX idx_srv_os_item_os ON brasil_saas.bc_srv_os_item USING btree (os_id);

CREATE INDEX idx_tabela_palavra ON brasil_saas.bc_fis_palavra_chave USING btree (tabela, palavra) WHERE (deleted_at IS NULL);

CREATE INDEX idx_ven_contrato_cliente_status ON brasil_saas.bc_ven_contrato USING btree (empresa_id, cliente_id, status);

CREATE INDEX idx_ven_devolucao_cliente_status ON brasil_saas.bc_ven_devolucao USING btree (empresa_id, cliente_id, status);

CREATE INDEX idx_ven_item_pedido ON brasil_saas.bc_ven_pedido_item USING btree (pedido_id);

CREATE INDEX idx_ven_pedido_cliente ON brasil_saas.bc_ven_pedido USING btree (cliente_id);

CREATE INDEX idx_ven_pedido_empresa ON brasil_saas.bc_ven_pedido USING btree (empresa_id, status);

CREATE INDEX idx_ven_pedido_tabela_preco ON brasil_saas.bc_ven_pedido USING btree (empresa_id, tabela_preco_id);

CREATE INDEX idx_ven_regra_comissao_vendedor ON brasil_saas.bc_ven_regra_comissao USING btree (empresa_id, vendedor_id, ativo);

CREATE INDEX idx_ven_tabela_preco_item_produto ON brasil_saas.bc_ven_tabela_preco_item USING btree (empresa_id, produto_id);

CREATE INDEX ix_bc_cad_produto_codigo_barras ON brasil_saas.bc_cad_produto USING btree (codigo_barras) WHERE ((codigo_barras IS NOT NULL) AND (deleted_at IS NULL));

CREATE INDEX ix_bc_core_banco_nome ON brasil_saas.bc_core_banco USING btree (lower((nome)::text));

CREATE INDEX ix_bc_core_banco_pix ON brasil_saas.bc_core_banco USING btree (aceita_pix) WHERE aceita_pix;

CREATE INDEX ix_bc_fin_caixa_empresa ON brasil_saas.bc_fin_caixa USING btree (empresa_id, nome);

CREATE INDEX ix_bc_fis_nfe_item_codigo_barras ON brasil_saas.bc_fis_nfe_item USING btree (codigo_barras) WHERE (codigo_barras IS NOT NULL);

CREATE INDEX ix_bc_fis_nfe_item_nfe ON brasil_saas.bc_fis_nfe_item USING btree (nfe_id);

CREATE INDEX ix_cmp_devolucao_pedido ON brasil_saas.bc_cmp_devolucao USING btree (empresa_id, pedido_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_crm_lead_etapa ON brasil_saas.bc_crm_lead USING btree (empresa_id, etapa) WHERE (deleted_at IS NULL);

CREATE INDEX ix_crm_tarefa_agenda ON brasil_saas.bc_crm_tarefa USING btree (empresa_id, data_agendada) WHERE (deleted_at IS NULL);

CREATE INDEX ix_crm_tarefa_lead ON brasil_saas.bc_crm_tarefa USING btree (lead_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_ctb_lanc_periodo ON brasil_saas.bc_ctb_lancamento USING btree (empresa_id, periodo) WHERE (deleted_at IS NULL);

CREATE INDEX ix_ctb_part_conta ON brasil_saas.bc_ctb_partida USING btree (empresa_id, conta_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_ctb_part_lanc ON brasil_saas.bc_ctb_partida USING btree (lancamento_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_dms_doc_categoria ON brasil_saas.bc_dms_documento USING btree (empresa_id, categoria) WHERE (deleted_at IS NULL);

CREATE INDEX ix_dms_doc_entidade ON brasil_saas.bc_dms_documento USING btree (empresa_id, entidade_tipo, entidade_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_esocial_status ON brasil_saas.bc_esocial_evento USING btree (empresa_id, status) WHERE (deleted_at IS NULL);

CREATE INDEX ix_fin_boleto_chave ON brasil_saas.bc_fin_boleto USING btree (empresa_id, banco, nosso_numero_chave);

CREATE INDEX ix_fin_boleto_empresa ON brasil_saas.bc_fin_boleto USING btree (empresa_id, criado_em DESC);

CREATE INDEX ix_fin_boleto_nosso_numero ON brasil_saas.bc_fin_boleto USING btree (empresa_id, banco, nosso_numero);

CREATE INDEX ix_fin_boleto_titulo ON brasil_saas.bc_fin_boleto USING btree (titulo_id);

CREATE INDEX ix_fin_boleto_vencimento ON brasil_saas.bc_fin_boleto USING btree (empresa_id, vencimento) WHERE ((status)::text = 'EMITIDO'::text);

CREATE INDEX ix_fin_remessa_empresa ON brasil_saas.bc_fin_remessa USING btree (empresa_id, data_geracao DESC);

CREATE INDEX ix_fin_remessa_item_remessa ON brasil_saas.bc_fin_remessa_item USING btree (remessa_id);

CREATE INDEX ix_fin_remessa_item_titulo ON brasil_saas.bc_fin_remessa_item USING btree (titulo_id);

CREATE INDEX ix_fin_remessa_status ON brasil_saas.bc_fin_remessa USING btree (empresa_id, status);

CREATE INDEX ix_fin_retorno_empresa ON brasil_saas.bc_fin_retorno_bancario USING btree (empresa_id, criado_em DESC);

CREATE INDEX ix_fin_retorno_item_nosso_numero ON brasil_saas.bc_fin_retorno_item USING btree (nosso_numero);

CREATE INDEX ix_fin_retorno_item_retorno ON brasil_saas.bc_fin_retorno_item USING btree (retorno_id);

CREATE INDEX ix_fin_retorno_item_titulo ON brasil_saas.bc_fin_retorno_item USING btree (titulo_id);

CREATE INDEX ix_fis_nfse_empresa_status ON brasil_saas.bc_fis_nfse USING btree (empresa_id, status);

CREATE INDEX ix_fis_nfse_retorno_indeciso ON brasil_saas.bc_fis_nfse_retorno USING btree (empresa_id, created_at DESC) WHERE (sucesso IS NULL);

CREATE INDEX ix_fis_nfse_retorno_nfse ON brasil_saas.bc_fis_nfse_retorno USING btree (nfse_id, created_at DESC);

CREATE INDEX ix_fis_nfse_retorno_sem_sucesso ON brasil_saas.bc_fis_nfse_retorno USING btree (empresa_id, created_at DESC) WHERE (sucesso IS FALSE);

CREATE INDEX ix_integration_event_status ON brasil_saas.bc_core_integration_event USING btree (status, created_at);

CREATE INDEX ix_obrigacao_entrega_comp ON brasil_saas.bc_fis_obrigacao_entrega USING btree (empresa_id, competencia) WHERE (deleted_at IS NULL);

CREATE INDEX ix_pcp_mps_periodo ON brasil_saas.bc_pcp_mps USING btree (empresa_id, periodo) WHERE (deleted_at IS NULL);

CREATE INDEX ix_prj_etapa_proj ON brasil_saas.bc_prj_etapa USING btree (projeto_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_prj_mov_proj ON brasil_saas.bc_prj_movimento USING btree (projeto_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_prod_aloc_cap ON brasil_saas.bc_prod_alocacao_capacidade USING btree (empresa_id, centro_trabalho_id, data) WHERE (deleted_at IS NULL);

CREATE INDEX ix_ptl_acesso_pessoa ON brasil_saas.bc_ptl_acesso USING btree (empresa_id, pessoa_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_rh_ferias_func ON brasil_saas.bc_rh_ferias USING btree (empresa_id, funcionario_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_rh_ponto_mes ON brasil_saas.bc_rh_ponto USING btree (empresa_id, data) WHERE (deleted_at IS NULL);

CREATE INDEX ix_ue_empresa ON brasil_saas.bc_core_usuario_empresa USING btree (empresa_id);

CREATE INDEX ix_ue_perfil ON brasil_saas.bc_core_usuario_empresa USING btree (perfil_id);

CREATE INDEX ix_ue_usuario ON brasil_saas.bc_core_usuario_empresa USING btree (usuario_id);

CREATE INDEX ix_usuario_modulo_modulo ON brasil_saas.bc_core_usuario_modulo USING btree (modulo_id);

CREATE INDEX ix_usuario_modulo_usuario ON brasil_saas.bc_core_usuario_modulo USING btree (usuario_id);

CREATE INDEX ix_ven_dev_pedido ON brasil_saas.bc_ven_devolucao_item USING btree (devolucao_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_wkf_instance_entidade ON brasil_saas.bc_wkf_instance USING btree (empresa_id, entidade_tipo, entidade_id);

CREATE INDEX ix_wkf_task_pendente ON brasil_saas.bc_wkf_task USING btree (empresa_id, status) WHERE (deleted_at IS NULL);

CREATE INDEX ix_wms_onda_item ON brasil_saas.bc_wms_onda_item USING btree (onda_id) WHERE (deleted_at IS NULL);

CREATE INDEX ix_wms_onda_status ON brasil_saas.bc_wms_onda USING btree (empresa_id, status) WHERE (deleted_at IS NULL);

CREATE INDEX ix_wms_vol_exp ON brasil_saas.bc_wms_volume USING btree (expedicao_id) WHERE (deleted_at IS NULL);

CREATE UNIQUE INDEX uk_bc_fin_caixa_empresa_nome ON brasil_saas.bc_fin_caixa USING btree (empresa_id, lower((nome)::text));

CREATE UNIQUE INDEX uk_empresa_matriz_por_grupo ON brasil_saas.bc_core_empresa USING btree (grupo_cnpj_cpf) WHERE (matriz AND (deleted_at IS NULL));

CREATE UNIQUE INDEX uk_est_deposito_codigo ON brasil_saas.bc_est_deposito USING btree (empresa_id, codigo) WHERE (deleted_at IS NULL);

CREATE UNIQUE INDEX uk_est_endereco_codigo ON brasil_saas.bc_est_endereco USING btree (deposito_id, codigo) WHERE (deleted_at IS NULL);

CREATE UNIQUE INDEX uk_est_expedicao_pedido_aberta ON brasil_saas.bc_est_expedicao USING btree (empresa_id, pedido_venda_id) WHERE ((deleted_at IS NULL) AND ((status)::text <> ALL (ARRAY[('EXPEDIDA'::character varying)::text, ('CANCELADA'::character varying)::text])));

CREATE UNIQUE INDEX uk_est_lote_produto_codigo ON brasil_saas.bc_est_lote USING btree (empresa_id, produto_id, codigo) WHERE (deleted_at IS NULL);

CREATE UNIQUE INDEX uk_est_reserva_pedido_produto_deposito_lote ON brasil_saas.bc_est_reserva USING btree (empresa_id, pedido_venda_id, deposito_id, produto_id, lote_id) WHERE ((pedido_venda_id IS NOT NULL) AND (lote_id IS NOT NULL) AND (deleted_at IS NULL) AND ((status)::text = ANY (ARRAY[('RESERVADA'::character varying)::text, ('SEPARACAO'::character varying)::text])));

CREATE UNIQUE INDEX uk_est_reserva_pedido_produto_deposito_sem_lote ON brasil_saas.bc_est_reserva USING btree (empresa_id, pedido_venda_id, deposito_id, produto_id) WHERE ((pedido_venda_id IS NOT NULL) AND (lote_id IS NULL) AND (deleted_at IS NULL) AND ((status)::text = ANY (ARRAY[('RESERVADA'::character varying)::text, ('SEPARACAO'::character varying)::text])));

CREATE UNIQUE INDEX uk_est_saldo_empresa_deposito_produto ON brasil_saas.bc_est_saldo USING btree (empresa_id, deposito_id, produto_id);

CREATE UNIQUE INDEX uk_est_serie_numero ON brasil_saas.bc_est_serie USING btree (empresa_id, numero_serie) WHERE (deleted_at IS NULL);

CREATE UNIQUE INDEX uk_extrato_fitid ON brasil_saas.bc_fin_extrato USING btree (empresa_id, conta_bancaria_id, fitid) WHERE ((deleted_at IS NULL) AND (fitid IS NOT NULL));

CREATE UNIQUE INDEX uk_fin_stripe_payment_session ON brasil_saas.bc_fin_stripe_payment USING btree (checkout_session_id) WHERE (checkout_session_id IS NOT NULL);

CREATE UNIQUE INDEX uk_palavra ON brasil_saas.bc_fis_palavra_chave USING btree (tabela, codigo, palavra) WHERE (deleted_at IS NULL);

CREATE UNIQUE INDEX uk_prod_estrutura_item ON brasil_saas.bc_prod_estrutura USING btree (empresa_id, produto_pai_id, produto_filho_id) WHERE (deleted_at IS NULL);

CREATE UNIQUE INDEX uk_ven_tabela_preco_codigo ON brasil_saas.bc_ven_tabela_preco USING btree (empresa_id, codigo) WHERE ((codigo IS NOT NULL) AND (deleted_at IS NULL));

CREATE UNIQUE INDEX ux_bc_fis_nfe_item_uuid ON brasil_saas.bc_fis_nfe_item USING btree (uuid);

CREATE UNIQUE INDEX ux_cad_servico_codigo_municipal ON brasil_saas.bc_cad_servico USING btree (empresa_id, codigo_tributacao_municipal) WHERE ((codigo_tributacao_municipal IS NOT NULL) AND (deleted_at IS NULL));

CREATE UNIQUE INDEX ux_fis_nfse_chave_nacional ON brasil_saas.bc_fis_nfse USING btree (chave_nota_nacional) WHERE ((chave_nota_nacional IS NOT NULL) AND (deleted_at IS NULL));

CREATE INDEX idx_dl_cliente_empresa ON brasil_saas_dl.dim_cliente USING btree (empresa_id);

CREATE INDEX idx_dl_compras_data_empresa ON brasil_saas_dl.ft_compras USING btree (data_id, empresa_id);

CREATE INDEX idx_dl_estoque_data_empresa ON brasil_saas_dl.ft_estoque USING btree (data_id, empresa_id);

CREATE INDEX idx_dl_financeiro_data_empresa ON brasil_saas_dl.ft_financeiro USING btree (data_id, empresa_id);

CREATE INDEX idx_dl_producao_data_empresa ON brasil_saas_dl.ft_producao USING btree (data_id, empresa_id);

CREATE INDEX idx_dl_produto_empresa ON brasil_saas_dl.dim_produto USING btree (empresa_id);

CREATE INDEX idx_dl_tempo_ano_mes ON brasil_saas_dl.dim_tempo USING btree (ano, mes);

CREATE INDEX idx_dl_vendas_data_empresa ON brasil_saas_dl.ft_vendas USING btree (data_id, empresa_id);

CREATE INDEX idx_fprodvar_ean ON public.fproduto_variacao USING btree (codigo_barras);

CREATE INDEX idx_fprodvar_produto ON public.fproduto_variacao USING btree (codproduto);

CREATE INDEX idx_fvenda_cliente ON public.fvenda USING btree (id_cliente);

CREATE INDEX idx_fvenda_data ON public.fvenda USING btree (data_venda);

CREATE INDEX idx_tissqn_cod ON public.tissqn USING btree (codigo_servico);

CREATE INDEX idx_tissqn_desc ON public.tissqn USING btree (descricao);

CREATE INDEX idx_tissqn_mun ON public.tissqn USING btree (municipio);

CREATE INDEX idx_tmunicipio_ibge ON public.tmunicipio USING btree (codigoibge);

CREATE INDEX idx_tmunicipio_nome ON public.tmunicipio USING btree (nomemunucipio);

CREATE TRIGGER trg_repetir_papel_proibido BEFORE INSERT OR UPDATE ON brasil_saas.bc_core_usuario_empresa FOR EACH ROW EXECUTE FUNCTION brasil_saas.fn_repetir_papel_proibido();

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard
    ADD CONSTRAINT bc_bi_dashboard_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget
    ADD CONSTRAINT bc_bi_dashboard_widget_dashboard_id_fkey FOREIGN KEY (dashboard_id) REFERENCES brasil_saas.bc_bi_dashboard(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_bi_indicador
    ADD CONSTRAINT bc_bi_indicador_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_bi_kpi
    ADD CONSTRAINT bc_bi_kpi_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_relatorio_id_fkey FOREIGN KEY (relatorio_id) REFERENCES brasil_saas.bc_bi_relatorio(id) ON DELETE SET NULL;

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio
    ADD CONSTRAINT bc_bi_relatorio_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_bi_report
    ADD CONSTRAINT bc_bi_report_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter
    ADD CONSTRAINT bc_bi_report_parameter_report_id_fkey FOREIGN KEY (report_id) REFERENCES brasil_saas.bc_bi_report(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_cad_base_cep
    ADD CONSTRAINT bc_cad_base_cep_municipio_id_fkey FOREIGN KEY (municipio_id) REFERENCES brasil_saas.bc_cad_municipio(id);

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_categoria_pai_id_fkey FOREIGN KEY (categoria_pai_id) REFERENCES brasil_saas.bc_cad_categoria(id);

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_municipio_id_fkey FOREIGN KEY (municipio_id) REFERENCES brasil_saas.bc_cad_municipio(id);

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_marca
    ADD CONSTRAINT bc_cad_marca_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT bc_cad_papel_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa
    ADD CONSTRAINT bc_cad_pessoa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica
    ADD CONSTRAINT bc_cad_pessoa_fisica_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica
    ADD CONSTRAINT bc_cad_pessoa_juridica_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES brasil_saas.bc_cad_categoria(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem
    ADD CONSTRAINT bc_cad_produto_imagem_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem
    ADD CONSTRAINT bc_cad_produto_imagem_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_item_id_fkey FOREIGN KEY (item_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_kit_id_fkey FOREIGN KEY (kit_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_marca_id_fkey FOREIGN KEY (marca_id) REFERENCES brasil_saas.bc_cad_marca(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_unidade_medida_id_fkey FOREIGN KEY (unidade_medida_id) REFERENCES brasil_saas.bc_cad_unidade_medida(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_unidade_medida_id_fkey FOREIGN KEY (unidade_medida_id) REFERENCES brasil_saas.bc_cad_unidade_medida(id);

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT bc_cad_unidade_medida_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao_item
    ADD CONSTRAINT bc_cmp_devolucao_item_devolucao_id_fkey FOREIGN KEY (devolucao_id) REFERENCES brasil_saas.bc_cmp_devolucao(id);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_nfe_fkey FOREIGN KEY (nfe_id) REFERENCES brasil_saas.bc_fis_nfe(id);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_pedido_id_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_com_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_recebimento_id_fkey FOREIGN KEY (recebimento_id) REFERENCES brasil_saas.bc_com_recebimento(id);

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_fornecedor_id_fkey FOREIGN KEY (fornecedor_id) REFERENCES brasil_saas.bc_cad_fornecedor(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_condicao_pagamento_id_fkey FOREIGN KEY (condicao_pagamento_id) REFERENCES brasil_saas.bc_fin_condicao_pagamento(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_cotacao_id_fkey FOREIGN KEY (cotacao_id) REFERENCES brasil_saas.bc_com_cotacao(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_fornecedor_id_fkey FOREIGN KEY (fornecedor_id) REFERENCES brasil_saas.bc_cad_fornecedor(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_cotacao_fornecedor_id_fkey FOREIGN KEY (cotacao_fornecedor_id) REFERENCES brasil_saas.bc_com_cotacao_fornecedor(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_solicitacao_id_fkey FOREIGN KEY (solicitacao_id) REFERENCES brasil_saas.bc_com_solicitacao(id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_item_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_item_pedido_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_com_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_item_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_cond_pagto_fkey FOREIGN KEY (condicao_pagamento_id) REFERENCES brasil_saas.bc_fin_condicao_pagamento(id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_fornecedor_fkey FOREIGN KEY (fornecedor_id) REFERENCES brasil_saas.bc_cad_fornecedor(id);

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_titulo_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_recebimento_id_fkey FOREIGN KEY (recebimento_id) REFERENCES brasil_saas.bc_com_recebimento(id);

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_pedido_id_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_com_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao
    ADD CONSTRAINT bc_com_solicitacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_solicitacao_id_fkey FOREIGN KEY (solicitacao_id) REFERENCES brasil_saas.bc_com_solicitacao(id);

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT bc_core_configuracao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_empresa_vinculada_id_fkey FOREIGN KEY (empresa_vinculada_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso
    ADD CONSTRAINT bc_core_log_acesso_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso
    ADD CONSTRAINT bc_core_log_acesso_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT bc_core_perfil_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT bc_core_perfil_permissao_perfil_id_fkey FOREIGN KEY (perfil_id) REFERENCES brasil_saas.bc_core_perfil(id);

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT bc_core_perfil_permissao_permissao_id_fkey FOREIGN KEY (permissao_id) REFERENCES brasil_saas.bc_core_permissao(id);

ALTER TABLE ONLY brasil_saas.bc_core_sessao
    ADD CONSTRAINT bc_core_sessao_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT bc_core_usuario_modulo_modulo_id_fkey FOREIGN KEY (modulo_id) REFERENCES brasil_saas.bc_core_modulo(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT bc_core_usuario_modulo_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT bc_core_usuario_perfil_perfil_id_fkey FOREIGN KEY (perfil_id) REFERENCES brasil_saas.bc_core_perfil(id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT bc_core_usuario_perfil_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_oportunidade_id_fkey FOREIGN KEY (oportunidade_id) REFERENCES brasil_saas.bc_crm_oportunidade(id);

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_vendedor_id_fkey FOREIGN KEY (vendedor_id) REFERENCES brasil_saas.bc_rh_funcionario(id);

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_vendedor_id_fkey FOREIGN KEY (vendedor_id) REFERENCES brasil_saas.bc_rh_funcionario(id);

ALTER TABLE ONLY brasil_saas.bc_crm_tarefa
    ADD CONSTRAINT bc_crm_tarefa_lead_id_fkey FOREIGN KEY (lead_id) REFERENCES brasil_saas.bc_crm_lead(id);

ALTER TABLE ONLY brasil_saas.bc_ctb_partida
    ADD CONSTRAINT bc_ctb_partida_lancamento_id_fkey FOREIGN KEY (lancamento_id) REFERENCES brasil_saas.bc_ctb_lancamento(id);

ALTER TABLE ONLY brasil_saas.bc_dms_aprovacao
    ADD CONSTRAINT bc_dms_aprovacao_documento_id_fkey FOREIGN KEY (documento_id) REFERENCES brasil_saas.bc_dms_documento(id);

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT bc_dms_versao_documento_id_fkey FOREIGN KEY (documento_id) REFERENCES brasil_saas.bc_dms_documento(id);

ALTER TABLE ONLY brasil_saas.bc_est_deposito
    ADD CONSTRAINT bc_est_deposito_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_endereco_fkey FOREIGN KEY (endereco_id) REFERENCES brasil_saas.bc_est_endereco(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_expedicao_id_fkey FOREIGN KEY (expedicao_id) REFERENCES brasil_saas.bc_est_expedicao(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_lote_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES brasil_saas.bc_est_reserva(id);

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_pedido_venda_id_fkey FOREIGN KEY (pedido_venda_id) REFERENCES brasil_saas.bc_ven_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_endereco_id_fkey FOREIGN KEY (endereco_id) REFERENCES brasil_saas.bc_est_endereco(id);

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_inventario_id_fkey FOREIGN KEY (inventario_id) REFERENCES brasil_saas.bc_est_inventario(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_mov_deposito_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_mov_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_mov_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_endereco_fkey FOREIGN KEY (endereco_id) REFERENCES brasil_saas.bc_est_endereco(id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_lote_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_pedido_venda_id_fkey FOREIGN KEY (pedido_venda_id) REFERENCES brasil_saas.bc_ven_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_deposito_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_deposito_destino_id_fkey FOREIGN KEY (deposito_destino_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_deposito_origem_id_fkey FOREIGN KEY (deposito_origem_id) REFERENCES brasil_saas.bc_est_deposito(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_endereco_destino_fkey FOREIGN KEY (endereco_destino_id) REFERENCES brasil_saas.bc_est_endereco(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_endereco_origem_fkey FOREIGN KEY (endereco_origem_id) REFERENCES brasil_saas.bc_est_endereco(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_lote_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_transferencia_id_fkey FOREIGN KEY (transferencia_id) REFERENCES brasil_saas.bc_est_transferencia(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_fluxo_aprovacao_id_fkey FOREIGN KEY (fluxo_aprovacao_id) REFERENCES brasil_saas.bc_fin_fluxo_aprovacao(id);

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_usuario_aprovador_id_fkey FOREIGN KEY (usuario_aprovador_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_parcela_id_fkey FOREIGN KEY (parcela_id) REFERENCES brasil_saas.bc_fin_titulo_parcela(id);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_tipo_pagamento_id_fkey FOREIGN KEY (tipo_pagamento_id) REFERENCES brasil_saas.bc_fin_tipo_pagamento(id);

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_caixa
    ADD CONSTRAINT bc_fin_caixa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_centro_custo_pai_id_fkey FOREIGN KEY (centro_custo_pai_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_baixa_id_fkey FOREIGN KEY (baixa_id) REFERENCES brasil_saas.bc_fin_baixa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_conciliacao_id_fkey FOREIGN KEY (conciliacao_id) REFERENCES brasil_saas.bc_fin_conciliacao_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_extrato_id_fkey FOREIGN KEY (extrato_id) REFERENCES brasil_saas.bc_fin_extrato(id);

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento
    ADD CONSTRAINT bc_fin_condicao_pagamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria
    ADD CONSTRAINT bc_fin_conta_bancaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo
    ADD CONSTRAINT bc_fin_emprestimo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao
    ADD CONSTRAINT bc_fin_fluxo_aprovacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_periodo_contabil_id_fkey FOREIGN KEY (periodo_contabil_id) REFERENCES brasil_saas.bc_fin_periodo_contabil(id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_lancamento_id_fkey FOREIGN KEY (lancamento_id) REFERENCES brasil_saas.bc_fin_lancamento_contabil(id);

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_plano_contas_id_fkey FOREIGN KEY (plano_contas_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_plano_contas_id_fkey FOREIGN KEY (plano_contas_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_orcamento_id_fkey FOREIGN KEY (orcamento_id) REFERENCES brasil_saas.bc_fin_orcamento(id);

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil
    ADD CONSTRAINT bc_fin_periodo_contabil_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_conta_pai_id_fkey FOREIGN KEY (conta_pai_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa
    ADD CONSTRAINT bc_fin_projecao_fluxo_caixa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd
    ADD CONSTRAINT bc_fin_provisao_pdd_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_boleto_id_fkey FOREIGN KEY (boleto_id) REFERENCES brasil_saas.bc_fin_boleto(id);

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_remessa_id_fkey FOREIGN KEY (remessa_id) REFERENCES brasil_saas.bc_fin_remessa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_novo_titulo_id_fkey FOREIGN KEY (novo_titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_titulo_original_id_fkey FOREIGN KEY (titulo_original_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_boleto_id_fkey FOREIGN KEY (boleto_id) REFERENCES brasil_saas.bc_fin_boleto(id);

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_retorno_id_fkey FOREIGN KEY (retorno_id) REFERENCES brasil_saas.bc_fin_retorno_bancario(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento
    ADD CONSTRAINT bc_fin_tipo_pagamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_plano_contas_id_fkey FOREIGN KEY (plano_contas_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_imposto_id_fkey FOREIGN KEY (imposto_id) REFERENCES brasil_saas.bc_fis_imposto(id);

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital
    ADD CONSTRAINT bc_fis_certificado_digital_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_cte_item
    ADD CONSTRAINT bc_fis_cte_item_cte_id_fkey FOREIGN KEY (cte_id) REFERENCES brasil_saas.bc_fis_cte(id);

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_ecd
    ADD CONSTRAINT bc_fis_ecd_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_ecf
    ADD CONSTRAINT bc_fis_ecf_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_esocial
    ADD CONSTRAINT bc_fis_esocial_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT bc_fis_imposto_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao
    ADD CONSTRAINT bc_fis_manifestacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe
    ADD CONSTRAINT bc_fis_mdfe_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item
    ADD CONSTRAINT bc_fis_nfce_item_nfce_id_fkey FOREIGN KEY (nfce_id) REFERENCES brasil_saas.bc_fis_nfce(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item
    ADD CONSTRAINT bc_fis_nfce_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_evento
    ADD CONSTRAINT bc_fis_nfe_evento_nfe_id_fkey FOREIGN KEY (nfe_id) REFERENCES brasil_saas.bc_fis_nfe(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item
    ADD CONSTRAINT bc_fis_nfe_item_nfe_id_fkey FOREIGN KEY (nfe_id) REFERENCES brasil_saas.bc_fis_nfe(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item
    ADD CONSTRAINT bc_fis_nfe_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pedido_compra_fkey FOREIGN KEY (pedido_compra_id) REFERENCES brasil_saas.bc_com_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pedido_venda_fkey FOREIGN KEY (pedido_venda_id) REFERENCES brasil_saas.bc_ven_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item
    ADD CONSTRAINT bc_fis_nfse_item_nfse_id_fkey FOREIGN KEY (nfse_id) REFERENCES brasil_saas.bc_fis_nfse(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item
    ADD CONSTRAINT bc_fis_nfse_item_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT bc_fis_obrigacao_entrega_obrigacao_id_fkey FOREIGN KEY (obrigacao_id) REFERENCES brasil_saas.bc_fis_obrigacao(id);

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria
    ADD CONSTRAINT bc_fis_regra_tributaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_reinf
    ADD CONSTRAINT bc_fis_reinf_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes
    ADD CONSTRAINT bc_fis_sped_contribuicoes_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal
    ADD CONSTRAINT bc_fis_sped_fiscal_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva
    ADD CONSTRAINT bc_ia_analise_preditiva_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_sessao_id_fkey FOREIGN KEY (sessao_id) REFERENCES brasil_saas.bc_ia_chat_sessao(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message
    ADD CONSTRAINT bc_ia_chat_message_session_id_fkey FOREIGN KEY (session_id) REFERENCES brasil_saas.bc_ia_chat_session(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao
    ADD CONSTRAINT bc_ia_chat_sessao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session
    ADD CONSTRAINT bc_ia_chat_session_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao
    ADD CONSTRAINT bc_ia_classificacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_config
    ADD CONSTRAINT bc_ia_config_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_embedding
    ADD CONSTRAINT bc_ia_embedding_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_prompt
    ADD CONSTRAINT bc_ia_prompt_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template
    ADD CONSTRAINT bc_ia_prompt_template_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_pai_id_fkey FOREIGN KEY (pai_id) REFERENCES brasil_saas.bc_prj_etapa(id);

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);

ALTER TABLE ONLY brasil_saas.bc_prj_faturamento
    ADD CONSTRAINT bc_prj_faturamento_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_etapa_id_fkey FOREIGN KEY (etapa_id) REFERENCES brasil_saas.bc_prj_etapa(id);

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);

ALTER TABLE ONLY brasil_saas.bc_prj_mudanca
    ADD CONSTRAINT bc_prj_mudanca_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);

ALTER TABLE ONLY brasil_saas.bc_prj_risco
    ADD CONSTRAINT bc_prj_risco_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_item_producao_id_fkey FOREIGN KEY (item_producao_id) REFERENCES brasil_saas.bc_prod_item(id);

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_producao_id_fkey FOREIGN KEY (producao_id) REFERENCES brasil_saas.bc_prod_ordem(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura
    ADD CONSTRAINT bc_prod_estrutura_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item
    ADD CONSTRAINT bc_prod_romaneio_item_romaneio_id_fkey FOREIGN KEY (romaneio_id) REFERENCES brasil_saas.bc_prod_romaneio(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_producao_id_fkey FOREIGN KEY (producao_id) REFERENCES brasil_saas.bc_prod_ordem(id) ON DELETE RESTRICT;

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_centro_trabalho_id_fkey FOREIGN KEY (centro_trabalho_id) REFERENCES brasil_saas.bc_prod_centro_trabalho(id);

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_roteiro_id_fkey FOREIGN KEY (roteiro_id) REFERENCES brasil_saas.bc_prod_roteiro(id);

ALTER TABLE ONLY brasil_saas.bc_rh_cargo
    ADD CONSTRAINT bc_rh_cargo_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT bc_rh_folha_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_folha_fkey FOREIGN KEY (folha_id) REFERENCES brasil_saas.bc_rh_folha(id);

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_func_fkey FOREIGN KEY (funcionario_id) REFERENCES brasil_saas.bc_rh_funcionario(id);

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT bc_rh_folha_titulo_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_func_cargo_fkey FOREIGN KEY (cargo_id) REFERENCES brasil_saas.bc_rh_cargo(id);

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_func_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_func_pessoa_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_os_id_fkey FOREIGN KEY (os_id) REFERENCES brasil_saas.bc_srv_ordem_servico(id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_os_id_fkey FOREIGN KEY (os_id) REFERENCES brasil_saas.bc_srv_ordem_servico(id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_bonificacao_id_fkey FOREIGN KEY (bonificacao_id) REFERENCES brasil_saas.bc_ven_bonificacao(id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_pedido_origem_id_fkey FOREIGN KEY (pedido_origem_id) REFERENCES brasil_saas.bc_ven_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_contrato_id_fkey FOREIGN KEY (contrato_id) REFERENCES brasil_saas.bc_ven_contrato(id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_devolucao_id_fkey FOREIGN KEY (devolucao_id) REFERENCES brasil_saas.bc_ven_devolucao(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_numero_serie_id_fkey FOREIGN KEY (numero_serie_id) REFERENCES brasil_saas.bc_est_serie(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_pedido_id_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_ven_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_pedido_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_ven_pedido(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_servico_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_cliente_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_cond_pagto_fkey FOREIGN KEY (condicao_pagamento_id) REFERENCES brasil_saas.bc_fin_condicao_pagamento(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_tabela_preco_fkey FOREIGN KEY (tabela_preco_id) REFERENCES brasil_saas.bc_ven_tabela_preco(id);

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_titulo_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_vendedor_id_fkey FOREIGN KEY (vendedor_id) REFERENCES brasil_saas.bc_rh_funcionario(id);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco
    ADD CONSTRAINT bc_ven_tabela_preco_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_tabela_preco_id_fkey FOREIGN KEY (tabela_preco_id) REFERENCES brasil_saas.bc_ven_tabela_preco(id);

ALTER TABLE ONLY brasil_saas.bc_wkf_instance
    ADD CONSTRAINT bc_wkf_instance_definition_id_fkey FOREIGN KEY (definition_id) REFERENCES brasil_saas.bc_wkf_definition(id);

ALTER TABLE ONLY brasil_saas.bc_wkf_stage
    ADD CONSTRAINT bc_wkf_stage_definition_id_fkey FOREIGN KEY (definition_id) REFERENCES brasil_saas.bc_wkf_definition(id);

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_instance_id_fkey FOREIGN KEY (instance_id) REFERENCES brasil_saas.bc_wkf_instance(id);

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_stage_id_fkey FOREIGN KEY (stage_id) REFERENCES brasil_saas.bc_wkf_stage(id);

ALTER TABLE ONLY brasil_saas.bc_wms_onda_item
    ADD CONSTRAINT bc_wms_onda_item_onda_id_fkey FOREIGN KEY (onda_id) REFERENCES brasil_saas.bc_wms_onda(id);

ALTER TABLE ONLY brasil_saas.bc_wms_volume_item
    ADD CONSTRAINT bc_wms_volume_item_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES brasil_saas.bc_wms_volume(id);

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT fk_bc_core_perfil_pai FOREIGN KEY (perfil_pai_id) REFERENCES brasil_saas.bc_core_perfil(id) ON DELETE SET NULL;

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT fk_empresa_matriz_do_grupo FOREIGN KEY (matriz_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE RESTRICT;

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_retorno
    ADD CONSTRAINT fk_fis_nfse_retorno_nfse FOREIGN KEY (nfse_id) REFERENCES brasil_saas.bc_fis_nfse(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT fk_ue_empresa FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT fk_ue_perfil FOREIGN KEY (perfil_id) REFERENCES brasil_saas.bc_core_perfil(id);

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT fk_ue_usuario FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id) ON DELETE CASCADE;

ALTER TABLE ONLY brasil_saas.bc_prod_item
    ADD CONSTRAINT fkal2vl187nvs8xwaq8kbexe66k FOREIGN KEY (producao_id) REFERENCES brasil_saas.bc_prod_ordem(id);

ALTER TABLE ONLY public.ffinanceiro
    ADD CONSTRAINT ffinanceiro_codmovimento_fkey FOREIGN KEY (codmovimento) REFERENCES public.fmovimento(codmovimento) ON DELETE CASCADE;

ALTER TABLE ONLY public.fitem
    ADD CONSTRAINT fitem_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);

ALTER TABLE ONLY public.flan
    ADD CONSTRAINT flan_id_pessoa_parceiro_fkey FOREIGN KEY (id_pessoa_parceiro) REFERENCES public.fpessoa(id_pessoa);

ALTER TABLE ONLY public.fmov
    ADD CONSTRAINT fmov_id_pessoa_cliente_fkey FOREIGN KEY (id_pessoa_cliente) REFERENCES public.fpessoa(id_pessoa);

ALTER TABLE ONLY public.fmov
    ADD CONSTRAINT fmov_id_pessoa_funcionario_fkey FOREIGN KEY (id_pessoa_funcionario) REFERENCES public.fpessoa(id_pessoa);

ALTER TABLE ONLY public.fmovimento_item
    ADD CONSTRAINT fmovimento_item_codmovimento_fkey FOREIGN KEY (codmovimento) REFERENCES public.fmovimento(codmovimento) ON DELETE CASCADE;

ALTER TABLE ONLY public.fmovimento_item
    ADD CONSTRAINT fmovimento_item_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_centro_custo_fkey FOREIGN KEY (centro_custo) REFERENCES public.fcusto(codcusto);

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_codmovimento_fkey FOREIGN KEY (codmovimento) REFERENCES public.fmovimento(codmovimento);

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_id_venda_fkey FOREIGN KEY (id_venda) REFERENCES public.fvenda(id_venda) ON DELETE CASCADE;

ALTER TABLE ONLY public.fnota_item
    ADD CONSTRAINT fnota_item_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);

ALTER TABLE ONLY public.fnota_item
    ADD CONSTRAINT fnota_item_id_nota_fkey FOREIGN KEY (id_nota) REFERENCES public.fnota(id_nota) ON DELETE CASCADE;

ALTER TABLE ONLY public.fpessoa
    ADD CONSTRAINT fpessoa_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.fusuario(codigo);

ALTER TABLE ONLY public.fproduto_ecommerce
    ADD CONSTRAINT fproduto_ecommerce_codproduto_fkey FOREIGN KEY (codproduto) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;

ALTER TABLE ONLY public.fproduto_fiscal
    ADD CONSTRAINT fproduto_fiscal_codproduto_fkey FOREIGN KEY (codproduto) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;

ALTER TABLE ONLY public.fproduto_imagem
    ADD CONSTRAINT fproduto_imagem_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao) ON DELETE CASCADE;

ALTER TABLE ONLY public.fproduto_kit
    ADD CONSTRAINT fproduto_kit_codkit_fkey FOREIGN KEY (codkit) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;

ALTER TABLE ONLY public.fproduto_kit
    ADD CONSTRAINT fproduto_kit_codproduto_filho_fkey FOREIGN KEY (codproduto_filho) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;

ALTER TABLE ONLY public.fproduto_movimento
    ADD CONSTRAINT fproduto_movimento_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao) ON DELETE CASCADE;

ALTER TABLE ONLY public.fproduto_variacao
    ADD CONSTRAINT fproduto_variacao_codproduto_fkey FOREIGN KEY (codproduto) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_centro_custo_fkey FOREIGN KEY (centro_custo) REFERENCES public.fcusto(codcusto);

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_condicao_pagamento_fkey FOREIGN KEY (condicao_pagamento) REFERENCES public.fcondicao(codcondicao);

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.fpessoa(id_pessoa);

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_id_recibo_fkey FOREIGN KEY (id_recibo) REFERENCES public.frecibo(idrecibo);

ALTER TABLE ONLY public.fvenda_item
    ADD CONSTRAINT fvenda_item_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);

ALTER TABLE ONLY public.fvenda_item
    ADD CONSTRAINT fvenda_item_id_venda_fkey FOREIGN KEY (id_venda) REFERENCES public.fvenda(id_venda) ON DELETE CASCADE;

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_tipo_pagamento_fkey FOREIGN KEY (tipo_pagamento) REFERENCES public.ftipopagamento(codtipo);

ALTER TABLE ONLY public.tcnae_servico
    ADD CONSTRAINT tcnae_servico_codigo_item_fkey FOREIGN KEY (codigo_item) REFERENCES public.tservico_lc116(codigo_item);

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('workflow:leitura','workflow','leitura','Consultar definicoes, instancias e tarefas'),
 ('workflow:escrita','workflow','escrita','Criar definicoes, abrir instancias, aprovar/rejeitar')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('workflow:leitura','workflow:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('contabilidade:leitura','contabilidade','leitura','Consultar diario, razao, balancete, balanco e fechamentos'),
 ('contabilidade:escrita','contabilidade','escrita','Lancar, estornar, fechar e gerar lancamentos')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('contabilidade:leitura','contabilidade:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('crm:leitura','crm','leitura','Consultar leads, pipeline, forecast e atividades'),
 ('crm:escrita','crm','escrita','Criar/mover leads, registrar e concluir atividades')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('crm:leitura','crm:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('wms:leitura','wms','leitura','Consultar ondas, volumes e put-away'),
 ('wms:escrita','wms','escrita','Criar ondas/volumes, separar, embalar e expedir')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('wms:leitura','wms:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('portal:leitura','portal','leitura','Consultar acessos de autoatendimento'),
 ('portal:escrita','portal','escrita','Gerar e revogar acessos de autoatendimento')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('portal:leitura','portal:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('projetos:leitura','projetos','leitura','Consultar projetos, WBS, riscos e faturamento'),
 ('projetos:escrita','projetos','escrita','Criar projetos, lancar custos, decidir mudancas e faturar')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('projetos:leitura','projetos:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('dms:leitura','dms','leitura','Consultar documentos e versoes'),
 ('dms:escrita','dms','escrita','Criar versoes, aprovar e gerenciar retencao')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('dms:leitura','dms:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

INSERT INTO brasil_saas.bc_core_modulo (uuid,chave,nome,descricao,icone,rota,ordem,exige_superuser,ativo)
SELECT gen_random_uuid(),'qualidade','Qualidade','Gestao da qualidade, inspeções e não conformidades','pi pi-check-circle','/qualidade',45,false,true
WHERE NOT EXISTS (SELECT 1 FROM brasil_saas.bc_core_modulo WHERE chave='qualidade');
INSERT INTO brasil_saas.bc_core_modulo (uuid,chave,nome,descricao,icone,rota,ordem,exige_superuser,ativo)
SELECT gen_random_uuid(),'ativos','Ativos e Manutenção','Ativo imobilizado e manutenção de equipamentos','pi pi-cog','/ativos',46,false,true
WHERE NOT EXISTS (SELECT 1 FROM brasil_saas.bc_core_modulo WHERE chave='ativos');

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('fiscal:cte:leitura','cte','leitura','Consultar status do servico CT-e'),
 ('fiscal:mdfe:leitura','mdfe','leitura','Consultar status e recibo do MDF-e'),
 ('fiscal:sped:leitura','sped','leitura','Consultar SPED'),
 ('fiscal:sped:escrita','sped','escrita','Gerar SPED')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('fiscal:cte:leitura','fiscal:mdfe:leitura','fiscal:sped:leitura','fiscal:sped:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('producao:mps:leitura','mps','leitura','Consultar plano mestre de producao'),
 ('producao:mps:escrita','mps','escrita','Gerar e confirmar plano mestre')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('producao:mps:leitura','producao:mps:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('vendas:devolucao:leitura','devolucao','leitura','Consultar trocas e devolucoes'),
 ('vendas:devolucao:escrita','devolucao','escrita','Solicitar, aprovar e receber devolucoes')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('vendas:devolucao:leitura','vendas:devolucao:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('rh:ponto:leitura','ponto','leitura','Consultar ponto e espelho'),
 ('rh:ponto:escrita','ponto','escrita','Bater ponto e ajustar')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('rh:ponto:leitura','rh:ponto:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('fiscal:obrigacao:leitura','obrigacao','leitura','Consultar agenda de obrigacoes'),
 ('fiscal:obrigacao:escrita','obrigacao','escrita','Cadastrar obrigacoes e dar baixa')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('fiscal:obrigacao:leitura','fiscal:obrigacao:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('producao:oee:leitura','oee','leitura','Consultar eficiencia operacional')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo = 'producao:oee:leitura'
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('rh:ferias:leitura','ferias','leitura','Consultar ferias'),
 ('rh:ferias:escrita','ferias','escrita','Programar e movimentar ferias')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('rh:ferias:leitura','rh:ferias:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('compras:devolucao:leitura','devolucao','leitura','Consultar devolucoes de compra'),
 ('compras:devolucao:escrita','devolucao','escrita','Solicitar e devolver compras')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('compras:devolucao:leitura','compras:devolucao:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);

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

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_risco (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(500) NOT NULL,
 categoria varchar(80),
 probabilidade numeric(8,2) NOT NULL DEFAULT 0,
 impacto numeric(8,2) NOT NULL DEFAULT 0,
 nivel numeric(12,2) NOT NULL DEFAULT 0,
 status varchar(30) NOT NULL DEFAULT 'ABERTO',
 responsavel varchar(160),
 prazo date,
 created_at timestamp NOT NULL DEFAULT now(),
 updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(500) NOT NULL,
 tipo varchar(40) NOT NULL DEFAULT 'PREVENTIVO',
 frequencia varchar(40),
 responsavel varchar(160),
 status varchar(30) NOT NULL DEFAULT 'ATIVO',
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_risco_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 risco_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_risco(id),
 controle_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_controle(id),
 UNIQUE(empresa_id,risco_id,controle_id)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_teste_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 controle_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_controle(id),
 periodo char(7) NOT NULL,
 resultado varchar(30) NOT NULL DEFAULT 'PENDENTE',
 observacao varchar(1000),
 testado_por varchar(160),
 testado_em timestamp,
 UNIQUE(empresa_id,controle_id,periodo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_evidencia (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 entidade_tipo varchar(60) NOT NULL,
 entidade_id bigint NOT NULL,
 nome varchar(200) NOT NULL,
 localizacao varchar(1000),
 validade date,
 hash_documento varchar(128),
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_grc_risco_tenant ON brasil_saas.bc_grc_risco(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_grc_controle_tenant ON brasil_saas.bc_grc_controle(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_grc_teste_tenant ON brasil_saas.bc_grc_teste_controle(empresa_id,periodo);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_ordem (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 numero varchar(60) NOT NULL,
 origem varchar(200),
 destino varchar(200),
 transportadora_id bigint,
 modalidade varchar(40) NOT NULL DEFAULT 'RODOVIARIO',
 status varchar(30) NOT NULL DEFAULT 'PLANEJADA',
 data_prevista_saida timestamp,
 data_prevista_entrega timestamp,
 peso numeric(15,3) DEFAULT 0,
 volume numeric(15,3) DEFAULT 0,
 frete_previsto numeric(15,2) DEFAULT 0,
 frete_real numeric(15,2) DEFAULT 0,
 UNIQUE(empresa_id,numero)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_evento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 tipo varchar(50) NOT NULL,
 data_evento timestamp NOT NULL DEFAULT now(),
 localizacao varchar(200),
 descricao varchar(500)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_tracking (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 codigo varchar(100) NOT NULL,
 transportadora varchar(160),
 ultimo_status varchar(80),
 ultima_atualizacao timestamp,
 UNIQUE(empresa_id,ordem_id)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_frete (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 componente varchar(80) NOT NULL,
 valor numeric(15,2) NOT NULL DEFAULT 0,
 documento varchar(120),
 status varchar(30) NOT NULL DEFAULT 'PENDENTE'
);
CREATE INDEX IF NOT EXISTS ix_tms_ordem_tenant ON brasil_saas.bc_tms_ordem(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_tms_evento_ordem ON brasil_saas.bc_tms_evento(empresa_id,ordem_id,data_evento);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_regra_lancamento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, codigo varchar(60) NOT NULL,
 nome varchar(200) NOT NULL, origem varchar(50) NOT NULL, conta_debito_id bigint, conta_credito_id bigint,
 centro_custo_id bigint, historico varchar(500), ativo boolean NOT NULL DEFAULT true,
 configuracao jsonb NOT NULL DEFAULT '{}'::jsonb, created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_rateio (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, lancamento_id bigint,
 centro_custo_id bigint, plano_contas_id bigint, percentual numeric(9,4) NOT NULL,
 valor numeric(18,2) NOT NULL DEFAULT 0, competencia date NOT NULL, status varchar(20) DEFAULT 'PENDENTE',
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_fechamento_check (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, periodo_id bigint NOT NULL,
 etapa varchar(50) NOT NULL, codigo varchar(80) NOT NULL, descricao varchar(300) NOT NULL,
 status varchar(20) NOT NULL DEFAULT 'PENDENTE', quantidade numeric(18,2) DEFAULT 0,
 executado_em timestamp, executado_por bigint, observacao varchar(1000),
 UNIQUE(empresa_id,periodo_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_relatorio_snapshot (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, periodo_id bigint NOT NULL,
 tipo varchar(30) NOT NULL, conta_id bigint, codigo_conta varchar(50), descricao varchar(255),
 debito numeric(18,2) NOT NULL DEFAULT 0, credito numeric(18,2) NOT NULL DEFAULT 0,
 saldo numeric(18,2) NOT NULL DEFAULT 0, nivel integer DEFAULT 0, dados jsonb DEFAULT '{}'::jsonb,
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_cont_rateio_lanc ON brasil_saas.bc_cont_rateio(empresa_id,lancamento_id);
CREATE INDEX IF NOT EXISTS ix_cont_snapshot ON brasil_saas.bc_cont_relatorio_snapshot(empresa_id,periodo_id,tipo);

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

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_plano_acao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 risco_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_risco(id),
 descricao varchar(1000) NOT NULL,
 responsavel varchar(160),
 prazo date,
 status varchar(30) NOT NULL DEFAULT 'ABERTO',
 percentual_conclusao numeric(5,2) NOT NULL DEFAULT 0,
 evidencia_id bigint,
 created_at timestamp NOT NULL DEFAULT now(),
 updated_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_grc_plano_acao_tenant ON brasil_saas.bc_grc_plano_acao(empresa_id,status,prazo);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_avaliacao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 risco_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_risco(id),
 periodo char(7) NOT NULL,
 probabilidade numeric(8,2) NOT NULL,
 impacto numeric(8,2) NOT NULL,
 nivel numeric(12,2) NOT NULL,
 tendencia varchar(30),
 avaliador varchar(160),
 observacao varchar(1000),
 avaliado_em timestamp NOT NULL DEFAULT now(),
 UNIQUE(empresa_id,risco_id,periodo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_log (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 entidade_tipo varchar(60) NOT NULL,
 entidade_id bigint,
 acao varchar(60) NOT NULL,
 usuario varchar(160),
 detalhes varchar(2000),
 criado_em timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_grc_log_tenant ON brasil_saas.bc_grc_log(empresa_id,criado_em);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_rota (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(300),
 origem varchar(200) NOT NULL,
 destino varchar(200) NOT NULL,
 distancia_km numeric(12,2),
 tempo_estimado_min integer,
 pedagio_estimado numeric(15,2) DEFAULT 0,
 ativo boolean NOT NULL DEFAULT true,
 UNIQUE(empresa_id,codigo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_parada (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 sequencia integer NOT NULL,
 tipo varchar(30) NOT NULL,
 localizacao varchar(300) NOT NULL,
 prevista_em timestamp,
 realizada_em timestamp,
 status varchar(30) NOT NULL DEFAULT 'PENDENTE',
 UNIQUE(empresa_id,ordem_id,sequencia)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_documento_entrega (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 tipo varchar(40) NOT NULL DEFAULT 'COMPROVANTE_ENTREGA',
 numero varchar(120),
 recebedor varchar(160),
 recebido_em timestamp,
 assinatura_localizacao varchar(500),
 observacao varchar(1000),
 documento_localizacao varchar(1000),
 status varchar(30) NOT NULL DEFAULT 'PENDENTE'
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_fechamento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 frete_contratado numeric(15,2) NOT NULL DEFAULT 0,
 adicionais numeric(15,2) NOT NULL DEFAULT 0,
 descontos numeric(15,2) NOT NULL DEFAULT 0,
 frete_aprovado numeric(15,2) NOT NULL DEFAULT 0,
 documento varchar(120),
 status varchar(30) NOT NULL DEFAULT 'PENDENTE',
 aprovado_por varchar(160),
 aprovado_em timestamp,
 UNIQUE(empresa_id,ordem_id)
);

CREATE INDEX IF NOT EXISTS ix_tms_rota_tenant ON brasil_saas.bc_tms_rota(empresa_id,ativo);
CREATE INDEX IF NOT EXISTS ix_tms_parada_tenant ON brasil_saas.bc_tms_parada(empresa_id,ordem_id,sequencia);
CREATE INDEX IF NOT EXISTS ix_tms_entrega_tenant ON brasil_saas.bc_tms_documento_entrega(empresa_id,ordem_id);

ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS rota_id bigint;
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS veiculo varchar(120);
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS motorista varchar(160);
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS data_saida timestamp;
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS data_entrega timestamp;
ALTER TABLE brasil_saas.bc_tms_ordem ADD COLUMN IF NOT EXISTS distancia_real_km numeric(12,2);
CREATE INDEX IF NOT EXISTS ix_tms_ordem_rota ON brasil_saas.bc_tms_ordem(empresa_id,rota_id);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_documento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, revisao_id bigint REFERENCES brasil_saas.bc_plm_produto_revisao(id),
 mudanca_id bigint REFERENCES brasil_saas.bc_plm_mudanca(id),
 tipo varchar(40) NOT NULL, codigo varchar(100) NOT NULL, versao varchar(30) NOT NULL DEFAULT '1',
 nome varchar(300) NOT NULL, localizacao varchar(1000), hash_documento varchar(128),
 status varchar(30) NOT NULL DEFAULT 'RASCUNHO', obrigatorio boolean NOT NULL DEFAULT false,
 aprovado_por bigint, aprovado_em timestamp, vigencia_inicio date, vigencia_fim date,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp,
 UNIQUE(empresa_id,codigo,versao)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_efeito_mudanca (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, mudanca_id bigint NOT NULL REFERENCES brasil_saas.bc_plm_mudanca(id),
 entidade_tipo varchar(50) NOT NULL, entidade_id bigint NOT NULL, acao varchar(30) NOT NULL,
 revisao_anterior varchar(30), revisao_nova varchar(30), efetiva_em date,
 status varchar(30) NOT NULL DEFAULT 'PENDENTE', observacao varchar(1000)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_aprovacao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, mudanca_id bigint NOT NULL REFERENCES brasil_saas.bc_plm_mudanca(id),
 etapa integer NOT NULL, aprovador_id bigint, decisao varchar(30) NOT NULL DEFAULT 'PENDENTE',
 observacao varchar(1000), decidido_em timestamp,
 UNIQUE(empresa_id,mudanca_id,etapa)
);
CREATE INDEX IF NOT EXISTS ix_plm_doc_tenant ON brasil_saas.bc_plm_documento(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_plm_efeito_tenant ON brasil_saas.bc_plm_efeito_mudanca(empresa_id,mudanca_id);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_risco (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, codigo varchar(80) NOT NULL, perigo varchar(300) NOT NULL,
 atividade varchar(300), localizacao varchar(300), probabilidade numeric(8,2) NOT NULL DEFAULT 1,
 impacto numeric(8,2) NOT NULL DEFAULT 1, nivel numeric(12,2) NOT NULL DEFAULT 1,
 controle_existente text, responsavel varchar(160), status varchar(30) NOT NULL DEFAULT 'ATIVO',
 revisado_em timestamp, UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_inspecao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, tipo varchar(80) NOT NULL,
 localizacao varchar(300), responsavel varchar(160), data_inspecao timestamp NOT NULL DEFAULT now(),
 status varchar(30) NOT NULL DEFAULT 'ABERTA', resultado varchar(30), observacao text,
 UNIQUE(empresa_id,numero)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_acao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, ocorrencia_id bigint REFERENCES brasil_saas.bc_ehs_ocorrencia(id),
 inspecao_id bigint REFERENCES brasil_saas.bc_ehs_inspecao(id), descricao varchar(1000) NOT NULL,
 responsavel varchar(160), prazo date, status varchar(30) NOT NULL DEFAULT 'ABERTA',
 concluida_em timestamp, evidencia varchar(1000)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_permissao_trabalho (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, tipo varchar(80) NOT NULL,
 localizacao varchar(300) NOT NULL, solicitante varchar(160), responsavel varchar(160),
 inicio timestamp, fim timestamp, riscos text, controles text, status varchar(30) NOT NULL DEFAULT 'SOLICITADA',
 aprovada_por varchar(160), aprovada_em timestamp, UNIQUE(empresa_id,numero)
);
CREATE INDEX IF NOT EXISTS ix_ehs_risco_tenant ON brasil_saas.bc_ehs_risco(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_ehs_inspecao_tenant ON brasil_saas.bc_ehs_inspecao(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_ehs_acao_tenant ON brasil_saas.bc_ehs_acao(empresa_id,status);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_int_endpoint (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, codigo varchar(100) NOT NULL, nome varchar(200) NOT NULL,
 tipo varchar(30) NOT NULL DEFAULT 'WEBHOOK', url varchar(1000) NOT NULL,
 segredo_hash varchar(128), eventos text, ativo boolean NOT NULL DEFAULT true,
 timeout_ms integer NOT NULL DEFAULT 10000, tentativas integer NOT NULL DEFAULT 3,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_int_webhook_event (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint, endpoint_id bigint REFERENCES brasil_saas.bc_int_endpoint(id),
 event_id varchar(160) NOT NULL, event_type varchar(160) NOT NULL,
 payload text NOT NULL, signature varchar(500), status varchar(30) NOT NULL DEFAULT 'RECEBIDO',
 attempts integer NOT NULL DEFAULT 0, error_message varchar(2000),
 received_at timestamp NOT NULL DEFAULT now(), processed_at timestamp,
 UNIQUE(event_id)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_int_delivery (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, endpoint_id bigint REFERENCES brasil_saas.bc_int_endpoint(id),
 event_type varchar(160) NOT NULL, aggregate_type varchar(100), aggregate_id bigint,
 payload text NOT NULL, status varchar(30) NOT NULL DEFAULT 'PENDENTE',
 attempts integer NOT NULL DEFAULT 0, next_attempt_at timestamp,
 last_error varchar(2000), delivered_at timestamp, correlation_id uuid,
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_int_event_tenant ON brasil_saas.bc_int_webhook_event(empresa_id,received_at);
CREATE INDEX IF NOT EXISTS ix_int_delivery_tenant ON brasil_saas.bc_int_delivery(empresa_id,status,next_attempt_at);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao(codigo,recurso,acao,descricao) VALUES
 ('plm:admin','plm','admin','Administrar ciclo de vida de engenharia'),
 ('ehs:admin','ehs','admin','Administrar riscos e permissões EHS'),
 ('integracao:leitura','integracao','leitura','Consultar integrações e entregas'),
 ('integracao:escrita','integracao','escrita','Administrar endpoints e integrações')
ON CONFLICT(codigo) DO NOTHING;

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

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_risco (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(500) NOT NULL,
 categoria varchar(80),
 probabilidade numeric(8,2) NOT NULL DEFAULT 0,
 impacto numeric(8,2) NOT NULL DEFAULT 0,
 nivel numeric(12,2) NOT NULL DEFAULT 0,
 status varchar(30) NOT NULL DEFAULT 'ABERTO',
 responsavel varchar(160),
 prazo date,
 created_at timestamp NOT NULL DEFAULT now(),
 updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(500) NOT NULL,
 tipo varchar(40) NOT NULL DEFAULT 'PREVENTIVO',
 frequencia varchar(40),
 responsavel varchar(160),
 status varchar(30) NOT NULL DEFAULT 'ATIVO',
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_risco_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 risco_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_risco(id),
 controle_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_controle(id),
 UNIQUE(empresa_id,risco_id,controle_id)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_teste_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 controle_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_controle(id),
 periodo char(7) NOT NULL,
 resultado varchar(30) NOT NULL DEFAULT 'PENDENTE',
 observacao varchar(1000),
 testado_por varchar(160),
 testado_em timestamp,
 UNIQUE(empresa_id,controle_id,periodo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_evidencia (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 entidade_tipo varchar(60) NOT NULL,
 entidade_id bigint NOT NULL,
 nome varchar(200) NOT NULL,
 localizacao varchar(1000),
 validade date,
 hash_documento varchar(128),
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_grc_risco_tenant ON brasil_saas.bc_grc_risco(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_grc_controle_tenant ON brasil_saas.bc_grc_controle(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_grc_teste_tenant ON brasil_saas.bc_grc_teste_controle(empresa_id,periodo);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_conferencia_fatura (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    recebimento_id bigint,
    titulo_id bigint,
    nfe_id bigint,
    valor_pedido numeric(15,2) NOT NULL DEFAULT 0,
    valor_recebido numeric(15,2) NOT NULL DEFAULT 0,
    valor_fatura numeric(15,2) NOT NULL DEFAULT 0,
    tolerancia numeric(7,4) NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'PENDENTE',
    divergencia text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_com_conferencia_pedido
    ON brasil_saas.bc_com_conferencia_fatura (empresa_id, pedido_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_conferencia_fatura_item (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    conferencia_id bigint NOT NULL,
    produto_id bigint,
    numero_item integer,
    descricao varchar(300),
    quantidade_pedida numeric(15,4) NOT NULL DEFAULT 0,
    quantidade_recebida numeric(15,4) NOT NULL DEFAULT 0,
    quantidade_faturada numeric(15,4) NOT NULL DEFAULT 0,
    valor_unitario_pedido numeric(15,4) NOT NULL DEFAULT 0,
    valor_unitario_recebido numeric(15,4) NOT NULL DEFAULT 0,
    valor_unitario_faturado numeric(15,4) NOT NULL DEFAULT 0,
    valor_total_pedido numeric(15,2) NOT NULL DEFAULT 0,
    valor_total_recebido numeric(15,2) NOT NULL DEFAULT 0,
    valor_total_faturado numeric(15,2) NOT NULL DEFAULT 0,
    conforme boolean NOT NULL DEFAULT true,
    tipo_divergencia varchar(40),
    divergencia varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_com_conferencia_item
    ON brasil_saas.bc_com_conferencia_fatura_item (empresa_id, conferencia_id) WHERE deleted_at IS NULL;

ALTER TABLE brasil_saas.bc_com_conferencia_fatura_item
    ADD COLUMN IF NOT EXISTS pedido_item_id bigint REFERENCES brasil_saas.bc_com_pedido_item(id),
    ADD COLUMN IF NOT EXISTS recebimento_item_id bigint REFERENCES brasil_saas.bc_com_recebimento_item(id),
    ADD COLUMN IF NOT EXISTS nfe_item_id bigint REFERENCES brasil_saas.bc_fis_nfe_item(id),
    ADD COLUMN IF NOT EXISTS numero_item integer,
    ADD COLUMN IF NOT EXISTS descricao varchar(300),
    ADD COLUMN IF NOT EXISTS valor_total_pedido numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS valor_total_recebido numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS valor_total_faturado numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS conforme boolean NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS tipo_divergencia varchar(40);

CREATE INDEX IF NOT EXISTS ix_com_conf_item_pedido
    ON brasil_saas.bc_com_conferencia_fatura_item(empresa_id, pedido_item_id);

CREATE INDEX IF NOT EXISTS ix_com_conf_item_nfe
    ON brasil_saas.bc_com_conferencia_fatura_item(empresa_id, nfe_item_id);

ALTER TABLE brasil_saas.bc_com_conferencia_fatura_item
    ADD COLUMN IF NOT EXISTS tolerancia numeric(18,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status varchar(20) NOT NULL DEFAULT 'DIVERGENTE';

UPDATE brasil_saas.bc_com_conferencia_fatura_item
SET status = CASE WHEN conforme THEN 'APROVADA' ELSE 'DIVERGENTE' END;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_caixa_movimento (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL,
    caixa_id        BIGINT NOT NULL REFERENCES brasil_saas.bc_fin_caixa(id),
    tipo            VARCHAR(20) NOT NULL,
    valor           NUMERIC(15,2) NOT NULL,
    saldo_anterior  NUMERIC(15,2) NOT NULL,
    saldo_posterior NUMERIC(15,2) NOT NULL,
    observacao      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    CONSTRAINT ck_caixa_mov_tipo CHECK (tipo IN ('SANGRIA', 'SUPRIMENTO')),
    CONSTRAINT ck_caixa_mov_valor CHECK (valor > 0)
);
CREATE INDEX IF NOT EXISTS idx_caixa_mov_empresa_caixa ON brasil_saas.bc_fin_caixa_movimento(empresa_id, caixa_id);
CREATE INDEX IF NOT EXISTS idx_caixa_mov_created ON brasil_saas.bc_fin_caixa_movimento(empresa_id, created_at DESC);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_rescisao (
    id                BIGSERIAL PRIMARY KEY,
    empresa_id        BIGINT NOT NULL,
    funcionario_id    BIGINT NOT NULL,
    data_desligamento DATE NOT NULL,
    motivo            VARCHAR(40) NOT NULL,
    meses_trabalhados INT,
    saldo_salario     NUMERIC(15,2) NOT NULL DEFAULT 0,
    decimo_terceiro   NUMERIC(15,2) NOT NULL DEFAULT 0,
    ferias            NUMERIC(15,2) NOT NULL DEFAULT 0,
    terco_ferias      NUMERIC(15,2) NOT NULL DEFAULT 0,
    aviso_previo      NUMERIC(15,2) NOT NULL DEFAULT 0,
    multa_40          NUMERIC(15,2) NOT NULL DEFAULT 0,
    total             NUMERIC(15,2) NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'EFETIVADA',
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        BIGINT,
    updated_at        TIMESTAMP,
    updated_by        BIGINT,
    deleted_at        TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rh_rescisao_empresa ON brasil_saas.bc_rh_rescisao(empresa_id);
CREATE INDEX IF NOT EXISTS idx_rh_rescisao_func ON brasil_saas.bc_rh_rescisao(empresa_id, funcionario_id);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_cobranca_acao (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL,
    titulo_id       BIGINT NOT NULL,
    pessoa_id       BIGINT,
    nivel           INT NOT NULL DEFAULT 1,
    tipo            VARCHAR(30) NOT NULL,
    observacao      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    CONSTRAINT ck_cob_acao_nivel CHECK (nivel BETWEEN 1 AND 5),
    CONSTRAINT ck_cob_acao_tipo CHECK (tipo IN ('LEMBRETE','AVISO','NEGATIVACAO','LIGACAO','EMAIL','WHATSAPP','OUTRO'))
);
CREATE INDEX IF NOT EXISTS idx_cob_acao_empresa_titulo ON brasil_saas.bc_fin_cobranca_acao(empresa_id, titulo_id);
CREATE INDEX IF NOT EXISTS idx_cob_acao_created ON brasil_saas.bc_fin_cobranca_acao(empresa_id, created_at DESC);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_promessa_pagamento (
    id                BIGSERIAL PRIMARY KEY,
    empresa_id        BIGINT NOT NULL,
    titulo_id         BIGINT NOT NULL,
    pessoa_id         BIGINT,
    valor_prometido   NUMERIC(15,2) NOT NULL,
    data_prometida    DATE NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    observacao        VARCHAR(500),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        BIGINT,
    updated_at        TIMESTAMP,
    CONSTRAINT ck_promessa_status CHECK (status IN ('ABERTA','CUMPRIDA','QUEBRADA','CANCELADA')),
    CONSTRAINT ck_promessa_valor CHECK (valor_prometido > 0)
);
CREATE INDEX IF NOT EXISTS idx_promessa_empresa ON brasil_saas.bc_fin_promessa_pagamento(empresa_id, status);
CREATE INDEX IF NOT EXISTS idx_promessa_titulo ON brasil_saas.bc_fin_promessa_pagamento(empresa_id, titulo_id);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_core_documento_fluxo (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL,
    origem_tipo     VARCHAR(40) NOT NULL,
    origem_id       BIGINT NOT NULL,
    origem_numero   VARCHAR(60),
    destino_tipo    VARCHAR(40) NOT NULL,
    destino_id      BIGINT NOT NULL,
    destino_numero  VARCHAR(60),
    relacao         VARCHAR(40) NOT NULL DEFAULT 'GERA',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_doc_fluxo_origem ON brasil_saas.bc_core_documento_fluxo(empresa_id, origem_tipo, origem_id);
CREATE INDEX IF NOT EXISTS idx_doc_fluxo_destino ON brasil_saas.bc_core_documento_fluxo(empresa_id, destino_tipo, destino_id);

ALTER TABLE brasil_saas.bc_fis_reinf
    ADD COLUMN IF NOT EXISTS payload TEXT,
    ADD COLUMN IF NOT EXISTS protocolo VARCHAR(60),
    ADD COLUMN IF NOT EXISTS gerado_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS total_docs INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS valor_total NUMERIC(15,2) DEFAULT 0;

COMMENT ON COLUMN brasil_saas.bc_fis_reinf.payload IS 'JSON do evento R-xxxx gerado (sem assinatura/transmissão)';
COMMENT ON COLUMN brasil_saas.bc_fis_reinf.protocolo IS 'Protocolo local ou da RFB quando houver transmissão';

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_contrato (
    id                    BIGSERIAL PRIMARY KEY,
    uuid                  UUID NOT NULL UNIQUE,
    empresa_id            BIGINT NOT NULL,
    fornecedor_id         BIGINT NOT NULL,
    numero                VARCHAR(30) NOT NULL,
    tipo                  VARCHAR(20) NOT NULL DEFAULT 'QUANTIDADE',
    status                VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    vigencia_inicio       DATE NOT NULL,
    vigencia_fim          DATE NOT NULL,
    condicao_pagamento_id BIGINT,
    valor_limite          NUMERIC(15,2),
    valor_liberado        NUMERIC(15,2) NOT NULL DEFAULT 0,
    observacao            TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP,
    deleted_at            TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_com_contrato_empresa ON brasil_saas.bc_com_contrato(empresa_id);
CREATE INDEX IF NOT EXISTS idx_com_contrato_status ON brasil_saas.bc_com_contrato(empresa_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_com_contrato_numero
    ON brasil_saas.bc_com_contrato(empresa_id, numero) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_contrato_item (
    id                      BIGSERIAL PRIMARY KEY,
    uuid                    UUID NOT NULL UNIQUE,
    empresa_id              BIGINT NOT NULL,
    contrato_id             BIGINT NOT NULL REFERENCES brasil_saas.bc_com_contrato(id),
    numero_item             INTEGER NOT NULL,
    produto_id              BIGINT,
    descricao               VARCHAR(300),
    unidade                 VARCHAR(10),
    quantidade_contratada   NUMERIC(15,3) NOT NULL,
    quantidade_liberada     NUMERIC(15,3) NOT NULL DEFAULT 0,
    valor_unitario          NUMERIC(15,4) NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP,
    deleted_at              TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_com_contrato_item_contrato ON brasil_saas.bc_com_contrato_item(contrato_id);

ALTER TABLE brasil_saas.bc_com_pedido
    ADD COLUMN IF NOT EXISTS contrato_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_com_pedido_contrato
    ON brasil_saas.bc_com_pedido(contrato_id) WHERE contrato_id IS NOT NULL;

ALTER TABLE brasil_saas.bc_fis_regra_tributaria
    ADD COLUMN IF NOT EXISTS mva NUMERIC(7,4),
    ADD COLUMN IF NOT EXISTS aliquota_fcp NUMERIC(7,4),
    ADD COLUMN IF NOT EXISTS aliquota_interna NUMERIC(7,4),
    ADD COLUMN IF NOT EXISTS reducao_base_pct NUMERIC(7,4);

CREATE INDEX IF NOT EXISTS idx_fis_regra_ncm ON brasil_saas.bc_fis_regra_tributaria(empresa_id, ncm)
    WHERE deleted_at IS NULL AND ativa = true;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_hdp_chamado (
    id              BIGSERIAL PRIMARY KEY,
    uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id      BIGINT NOT NULL,
    numero          VARCHAR(30) NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    descricao       TEXT,
    prioridade      VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    status          VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    categoria       VARCHAR(60),
    solicitante     VARCHAR(150),
    responsavel     VARCHAR(150),
    cliente_id      BIGINT,
    aberto_em       TIMESTAMP NOT NULL DEFAULT NOW(),
    fechado_em      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_hdp_chamado_emp ON brasil_saas.bc_hdp_chamado(empresa_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_hdp_chamado_status ON brasil_saas.bc_hdp_chamado(empresa_id, status) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_hdp_comentario (
    id              BIGSERIAL PRIMARY KEY,
    uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id      BIGINT NOT NULL,
    chamado_id      BIGINT NOT NULL REFERENCES brasil_saas.bc_hdp_chamado(id),
    autor           VARCHAR(150),
    texto           TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_hdp_com_chamado ON brasil_saas.bc_hdp_comentario(chamado_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_agd_evento (
    id              BIGSERIAL PRIMARY KEY,
    uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id      BIGINT NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    descricao       TEXT,
    tipo            VARCHAR(40) NOT NULL DEFAULT 'REUNIAO',
    inicio          TIMESTAMP NOT NULL,
    fim             TIMESTAMP,
    local_evento    VARCHAR(200),
    lead_id         BIGINT,
    cliente_id      BIGINT,
    responsavel     VARCHAR(150),
    status          VARCHAR(30) NOT NULL DEFAULT 'AGENDADO',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_agd_emp_inicio ON brasil_saas.bc_agd_evento(empresa_id, inicio) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ven_meta (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    ano INT NOT NULL,
    mes INT NOT NULL,
    vendedor_id BIGINT,
    canal VARCHAR(40),
    valor_meta NUMERIC(18,2) NOT NULL DEFAULT 0,
    valor_realizado NUMERIC(18,2) NOT NULL DEFAULT 0,
    observacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_ven_meta_emp ON brasil_saas.bc_ven_meta(empresa_id, ano, mes) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_core_notificacao (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT,
    titulo VARCHAR(200) NOT NULL,
    mensagem TEXT,
    tipo VARCHAR(40) NOT NULL DEFAULT 'INFO',
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    link VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_notif_emp_user ON brasil_saas.bc_core_notificacao(empresa_id, usuario_id, lida) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_kb_artigo (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    categoria VARCHAR(80),
    conteudo TEXT,
    tags VARCHAR(200),
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_kb_emp ON brasil_saas.bc_kb_artigo(empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ven_contrato (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    numero VARCHAR(40) NOT NULL,
    cliente_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO',
    valor NUMERIC(18,2) NOT NULL DEFAULT 0,
    inicio DATE,
    fim DATE,
    renovacao_auto BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
CREATE INDEX IF NOT EXISTS idx_ven_ctr_emp ON brasil_saas.bc_ven_contrato(empresa_id, status) WHERE deleted_at IS NULL;

ALTER TABLE brasil_saas.bc_fin_intercompany ADD COLUMN IF NOT EXISTS contrapartida_id BIGINT;
ALTER TABLE brasil_saas.bc_fin_intercompany ADD COLUMN IF NOT EXISTS diferenca NUMERIC(18,2);
ALTER TABLE brasil_saas.bc_fin_intercompany ADD COLUMN IF NOT EXISTS reconciliado_em TIMESTAMP;

ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS referencia_tipo VARCHAR(40);
ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS referencia_id BIGINT;
ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS peso NUMERIC(15,3);
ALTER TABLE brasil_saas.bc_tms_parada ADD COLUMN IF NOT EXISTS volume NUMERIC(15,3);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tms_parada_ref ON brasil_saas.bc_tms_parada(empresa_id, referencia_tipo, referencia_id)
    WHERE referencia_id IS NOT NULL AND status <> 'CANCELADA';

ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS classe_id bigint;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS centro_custo_id bigint;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS fornecedor_id bigint;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS numero_documento varchar(60);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS data_inicio_depreciacao date;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS metodo_depreciacao varchar(30);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS taxa_anual numeric(7,4);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS ativo_pai_id bigint;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS fabricante varchar(120);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS modelo varchar(120);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS garantia_ate date;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS valor_reavaliacao numeric(15,2) NOT NULL DEFAULT 0;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS valor_impairment numeric(15,2) NOT NULL DEFAULT 0;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS data_baixa date;
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS valor_baixa numeric(15,2);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS motivo_baixa varchar(500);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS ultimo_periodo_depreciado varchar(7);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS contador_atual numeric(15,2);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS unidade_contador varchar(20);
ALTER TABLE brasil_saas.bc_ativo_imobilizado ADD COLUMN IF NOT EXISTS critico boolean NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_movimento (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    ativo_id bigint NOT NULL,
    tipo varchar(30) NOT NULL,
    data_movimento date NOT NULL,
    periodo varchar(7) NOT NULL,
    valor numeric(15,2) NOT NULL DEFAULT 0,
    valor_depreciacao numeric(15,2) NOT NULL DEFAULT 0,
    valor_venda numeric(15,2),
    resultado numeric(15,2),
    execucao_id bigint,
    lancamento_id bigint,
    centro_custo_origem_id bigint,
    centro_custo_destino_id bigint,
    localizacao_origem varchar(255),
    localizacao_destino varchar(255),
    responsavel_origem_id bigint,
    responsavel_destino_id bigint,
    documento varchar(60),
    observacao varchar(1000),
    status varchar(20) NOT NULL DEFAULT 'ATIVO',
    usuario_id bigint
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_movimento_empresa ON brasil_saas.bc_ativo_movimento(empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_classe (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    codigo varchar(30) NOT NULL,
    descricao varchar(255) NOT NULL,
    metodo_depreciacao varchar(30) NOT NULL DEFAULT 'LINEAR',
    vida_util_meses integer,
    taxa_anual numeric(7,4),
    conta_ativo_id bigint,
    conta_depreciacao_acumulada_id bigint,
    conta_despesa_depreciacao_id bigint,
    conta_ganho_baixa_id bigint,
    conta_perda_baixa_id bigint,
    conta_reavaliacao_id bigint,
    conta_impairment_id bigint,
    ativo boolean NOT NULL DEFAULT true
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_classe_empresa ON brasil_saas.bc_ativo_classe(empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_depreciacao_execucao (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    periodo varchar(7) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'EFETIVADA',
    quantidade_ativos integer NOT NULL DEFAULT 0,
    valor_total numeric(15,2) NOT NULL DEFAULT 0,
    valor_contabilizado numeric(15,2) NOT NULL DEFAULT 0,
    lancamento_id bigint,
    usuario_id bigint,
    estornada_em timestamp
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_depreciacao_execucao_empresa ON brasil_saas.bc_ativo_depreciacao_execucao(empresa_id) WHERE deleted_at IS NULL;

ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS plano_id bigint;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS nota_id bigint;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS data_inicio date;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS horas_trabalhadas numeric(10,2) NOT NULL DEFAULT 0;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS horas_parada numeric(10,2) NOT NULL DEFAULT 0;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS custo_material numeric(15,2) NOT NULL DEFAULT 0;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS custo_mao_obra numeric(15,2) NOT NULL DEFAULT 0;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS custo_servico numeric(15,2) NOT NULL DEFAULT 0;
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS causa varchar(1000);
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS solucao varchar(2000);
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS checklist varchar(4000);
ALTER TABLE brasil_saas.bc_ativo_manutencao ADD COLUMN IF NOT EXISTS centro_custo_id bigint;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_manutencao_apontamento (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    manutencao_id bigint NOT NULL,
    funcionario_id bigint,
    data_apontamento date NOT NULL,
    horas numeric(10,2) NOT NULL DEFAULT 0,
    custo_hora numeric(15,2) NOT NULL DEFAULT 0,
    custo_total numeric(15,2) NOT NULL DEFAULT 0,
    descricao varchar(1000)
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_manutencao_apontamento_empresa ON brasil_saas.bc_ativo_manutencao_apontamento(empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_manutencao_material (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    manutencao_id bigint NOT NULL,
    produto_id bigint,
    descricao varchar(255) NOT NULL,
    quantidade numeric(15,4) NOT NULL DEFAULT 0,
    custo_unitario numeric(15,4) NOT NULL DEFAULT 0,
    custo_total numeric(15,2) NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_manutencao_material_empresa ON brasil_saas.bc_ativo_manutencao_material(empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_medicao (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    ativo_id bigint NOT NULL,
    data_medicao date NOT NULL,
    valor numeric(15,2) NOT NULL,
    unidade varchar(20),
    observacao varchar(500)
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_medicao_empresa ON brasil_saas.bc_ativo_medicao(empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_nota_manutencao (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    ativo_id bigint NOT NULL,
    numero varchar(50) NOT NULL,
    tipo varchar(30) NOT NULL DEFAULT 'AVARIA',
    prioridade varchar(20) NOT NULL DEFAULT 'MEDIA',
    status varchar(30) NOT NULL DEFAULT 'ABERTA',
    descricao varchar(2000) NOT NULL,
    sintoma varchar(1000),
    causa varchar(1000),
    equipamento_parado boolean NOT NULL DEFAULT false,
    inicio_parada timestamp,
    fim_parada timestamp,
    data_nota date NOT NULL,
    manutencao_id bigint,
    solicitante_id bigint
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_nota_manutencao_empresa ON brasil_saas.bc_ativo_nota_manutencao(empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ativo_plano_manutencao (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    deleted_at timestamp,
    ativo_id bigint NOT NULL,
    codigo varchar(30) NOT NULL,
    descricao varchar(255) NOT NULL,
    tipo_ciclo varchar(20) NOT NULL DEFAULT 'TEMPO',
    intervalo_dias integer,
    intervalo_contador numeric(15,2),
    antecedencia_dias integer NOT NULL DEFAULT 0,
    ultima_execucao date,
    contador_ultima_execucao numeric(15,2),
    proxima_data date,
    checklist varchar(4000),
    horas_estimadas numeric(10,2),
    custo_estimado numeric(15,2),
    prioridade varchar(20) NOT NULL DEFAULT 'MEDIA',
    responsavel_id bigint,
    ativo boolean NOT NULL DEFAULT true
);
CREATE INDEX IF NOT EXISTS ix_bc_ativo_plano_manutencao_empresa ON brasil_saas.bc_ativo_plano_manutencao(empresa_id) WHERE deleted_at IS NULL;
