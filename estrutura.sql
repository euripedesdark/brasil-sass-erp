--
-- PostgreSQL database dump
--

\restrict B6qyw1UnbSRcM55QH7rsKPXWXMJXwynYMNstyMgy4w7R1UnJzdpsRxDA51myPxL

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: brasil_saas; Type: SCHEMA; Schema: -; Owner: sa
--

CREATE SCHEMA brasil_saas;


ALTER SCHEMA brasil_saas OWNER TO sa;

--
-- Name: SCHEMA brasil_saas; Type: COMMENT; Schema: -; Owner: sa
--

COMMENT ON SCHEMA brasil_saas IS 'Schema principal do Brasil SaaS ERP';


--
-- Name: brasil_saas_dl; Type: SCHEMA; Schema: -; Owner: sa
--

CREATE SCHEMA brasil_saas_dl;


ALTER SCHEMA brasil_saas_dl OWNER TO sa;

--
-- Name: SCHEMA brasil_saas_dl; Type: COMMENT; Schema: -; Owner: sa
--

COMMENT ON SCHEMA brasil_saas_dl IS 'Datalake do BI - Brasil SaaS ERP';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA brasil_saas;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: unaccent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA brasil_saas;


--
-- Name: EXTENSION unaccent; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA brasil_saas;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: dmemo; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dmemo AS character varying(500);


ALTER DOMAIN public.dmemo OWNER TO postgres;

--
-- Name: dmkqt; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dmkqt AS numeric(15,2);


ALTER DOMAIN public.dmkqt OWNER TO postgres;

--
-- Name: docod; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.docod AS integer;


ALTER DOMAIN public.docod OWNER TO postgres;

--
-- Name: dodata; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dodata AS date;


ALTER DOMAIN public.dodata OWNER TO postgres;

--
-- Name: dohoras; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dohoras AS time without time zone;


ALTER DOMAIN public.dohoras OWNER TO postgres;

--
-- Name: dom1; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom1 AS character(1);


ALTER DOMAIN public.dom1 OWNER TO postgres;

--
-- Name: dom10; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom10 AS character varying(10);


ALTER DOMAIN public.dom10 OWNER TO postgres;

--
-- Name: dom100; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom100 AS character varying(100);


ALTER DOMAIN public.dom100 OWNER TO postgres;

--
-- Name: dom15; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom15 AS character varying(15);


ALTER DOMAIN public.dom15 OWNER TO postgres;

--
-- Name: dom150; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom150 AS character varying(150);


ALTER DOMAIN public.dom150 OWNER TO postgres;

--
-- Name: dom2; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom2 AS character(2);


ALTER DOMAIN public.dom2 OWNER TO postgres;

--
-- Name: dom20; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom20 AS character varying(20);


ALTER DOMAIN public.dom20 OWNER TO postgres;

--
-- Name: dom200; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom200 AS character varying(200);


ALTER DOMAIN public.dom200 OWNER TO postgres;

--
-- Name: dom30; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom30 AS character varying(30);


ALTER DOMAIN public.dom30 OWNER TO postgres;

--
-- Name: dom40; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom40 AS character varying(40);


ALTER DOMAIN public.dom40 OWNER TO postgres;

--
-- Name: dom45; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom45 AS character varying(45);


ALTER DOMAIN public.dom45 OWNER TO postgres;

--
-- Name: dom5; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom5 AS character varying(5);


ALTER DOMAIN public.dom5 OWNER TO postgres;

--
-- Name: dom50; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom50 AS character varying(50);


ALTER DOMAIN public.dom50 OWNER TO postgres;

--
-- Name: dom60; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom60 AS character varying(60);


ALTER DOMAIN public.dom60 OWNER TO postgres;

--
-- Name: dom80; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dom80 AS character varying(80);


ALTER DOMAIN public.dom80 OWNER TO postgres;

--
-- Name: dompeso; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dompeso AS numeric(9,3);


ALTER DOMAIN public.dompeso OWNER TO postgres;

--
-- Name: domtraco; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.domtraco AS numeric(15,5);


ALTER DOMAIN public.domtraco OWNER TO postgres;

--
-- Name: domvalor4casas; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.domvalor4casas AS numeric(15,4);


ALTER DOMAIN public.domvalor4casas OWNER TO postgres;

--
-- Name: dovalor; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dovalor AS numeric(15,2);


ALTER DOMAIN public.dovalor OWNER TO postgres;

--
-- Name: dovalort; Type: DOMAIN; Schema: public; Owner: postgres
--

CREATE DOMAIN public.dovalort AS numeric(15,4);


ALTER DOMAIN public.dovalort OWNER TO postgres;

--
-- Name: fiscal_normaliza_codigo(text); Type: FUNCTION; Schema: brasil_saas; Owner: sa
--

CREATE FUNCTION brasil_saas.fiscal_normaliza_codigo(p_txt text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $$
    SELECT ltrim(regexp_replace(p_txt, '[^0-9]', '', 'g'), '0');
$$;


ALTER FUNCTION brasil_saas.fiscal_normaliza_codigo(p_txt text) OWNER TO sa;

--
-- Name: FUNCTION fiscal_normaliza_codigo(p_txt text); Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON FUNCTION brasil_saas.fiscal_normaliza_codigo(p_txt text) IS 'Código sem pontuação e sem zeros à esquerda, para casar o que o usuário digita com o que está cadastrado. Só usa dígito, então independe de collation.';


--
-- Name: fn_repetir_papel_proibido(); Type: FUNCTION; Schema: brasil_saas; Owner: sa
--

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


ALTER FUNCTION brasil_saas.fn_repetir_papel_proibido() OWNER TO sa;

--
-- Name: set_updated_at(); Type: FUNCTION; Schema: brasil_saas; Owner: sa
--

CREATE FUNCTION brasil_saas.set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


ALTER FUNCTION brasil_saas.set_updated_at() OWNER TO sa;

--
-- Name: FUNCTION set_updated_at(); Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON FUNCTION brasil_saas.set_updated_at() IS 'Atualiza updated_at em triggers de UPDATE';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: bc_agd_evento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_agd_evento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    titulo character varying(200) NOT NULL,
    descricao text,
    tipo character varying(40) DEFAULT 'REUNIAO'::character varying NOT NULL,
    inicio timestamp without time zone NOT NULL,
    fim timestamp without time zone,
    local_evento character varying(200),
    lead_id bigint,
    cliente_id bigint,
    responsavel character varying(150),
    status character varying(30) DEFAULT 'AGENDADO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);


ALTER TABLE brasil_saas.bc_agd_evento OWNER TO sa;

--
-- Name: bc_agd_evento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_agd_evento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_agd_evento_id_seq OWNER TO sa;

--
-- Name: bc_agd_evento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_agd_evento_id_seq OWNED BY brasil_saas.bc_agd_evento.id;


--
-- Name: bc_ativo_classe; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_classe (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    codigo character varying(30) NOT NULL,
    descricao character varying(255) NOT NULL,
    metodo_depreciacao character varying(30) DEFAULT 'LINEAR'::character varying NOT NULL,
    vida_util_meses integer,
    taxa_anual numeric(7,4),
    conta_ativo_id bigint,
    conta_depreciacao_acumulada_id bigint,
    conta_despesa_depreciacao_id bigint,
    conta_ganho_baixa_id bigint,
    conta_perda_baixa_id bigint,
    conta_reavaliacao_id bigint,
    conta_impairment_id bigint,
    ativo boolean DEFAULT true NOT NULL
);


ALTER TABLE brasil_saas.bc_ativo_classe OWNER TO sa;

--
-- Name: bc_ativo_classe_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_classe ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_classe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ativo_depreciacao_execucao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_depreciacao_execucao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    periodo character varying(7) NOT NULL,
    status character varying(20) DEFAULT 'EFETIVADA'::character varying NOT NULL,
    quantidade_ativos integer DEFAULT 0 NOT NULL,
    valor_total numeric(15,2) DEFAULT 0 NOT NULL,
    valor_contabilizado numeric(15,2) DEFAULT 0 NOT NULL,
    lancamento_id bigint,
    usuario_id bigint,
    estornada_em timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ativo_depreciacao_execucao OWNER TO sa;

--
-- Name: bc_ativo_depreciacao_execucao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_depreciacao_execucao ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_depreciacao_execucao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ativo_imobilizado; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    deleted_at timestamp without time zone,
    classe_id bigint,
    centro_custo_id bigint,
    fornecedor_id bigint,
    numero_documento character varying(60),
    data_inicio_depreciacao date,
    metodo_depreciacao character varying(30),
    taxa_anual numeric(7,4),
    ativo_pai_id bigint,
    fabricante character varying(120),
    modelo character varying(120),
    garantia_ate date,
    valor_reavaliacao numeric(15,2) DEFAULT 0 NOT NULL,
    valor_impairment numeric(15,2) DEFAULT 0 NOT NULL,
    data_baixa date,
    valor_baixa numeric(15,2),
    motivo_baixa character varying(500),
    ultimo_periodo_depreciado character varying(7),
    contador_atual numeric(15,2),
    unidade_contador character varying(20),
    critico boolean DEFAULT false NOT NULL
);


ALTER TABLE brasil_saas.bc_ativo_imobilizado OWNER TO sa;

--
-- Name: bc_ativo_imobilizado_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ativo_imobilizado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ativo_imobilizado_id_seq OWNER TO sa;

--
-- Name: bc_ativo_imobilizado_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ativo_imobilizado_id_seq OWNED BY brasil_saas.bc_ativo_imobilizado.id;


--
-- Name: bc_ativo_manutencao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    deleted_at timestamp without time zone,
    plano_id bigint,
    nota_id bigint,
    data_inicio date,
    horas_trabalhadas numeric(10,2) DEFAULT 0 NOT NULL,
    horas_parada numeric(10,2) DEFAULT 0 NOT NULL,
    custo_material numeric(15,2) DEFAULT 0 NOT NULL,
    custo_mao_obra numeric(15,2) DEFAULT 0 NOT NULL,
    custo_servico numeric(15,2) DEFAULT 0 NOT NULL,
    causa character varying(1000),
    solucao character varying(2000),
    checklist character varying(4000),
    centro_custo_id bigint
);


ALTER TABLE brasil_saas.bc_ativo_manutencao OWNER TO sa;

--
-- Name: bc_ativo_manutencao_apontamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_manutencao_apontamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    manutencao_id bigint NOT NULL,
    funcionario_id bigint,
    data_apontamento date NOT NULL,
    horas numeric(10,2) DEFAULT 0 NOT NULL,
    custo_hora numeric(15,2) DEFAULT 0 NOT NULL,
    custo_total numeric(15,2) DEFAULT 0 NOT NULL,
    descricao character varying(1000)
);


ALTER TABLE brasil_saas.bc_ativo_manutencao_apontamento OWNER TO sa;

--
-- Name: bc_ativo_manutencao_apontamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_manutencao_apontamento ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_manutencao_apontamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ativo_manutencao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ativo_manutencao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ativo_manutencao_id_seq OWNER TO sa;

--
-- Name: bc_ativo_manutencao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ativo_manutencao_id_seq OWNED BY brasil_saas.bc_ativo_manutencao.id;


--
-- Name: bc_ativo_manutencao_material; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_manutencao_material (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    manutencao_id bigint NOT NULL,
    produto_id bigint,
    descricao character varying(255) NOT NULL,
    quantidade numeric(15,4) DEFAULT 0 NOT NULL,
    custo_unitario numeric(15,4) DEFAULT 0 NOT NULL,
    custo_total numeric(15,2) DEFAULT 0 NOT NULL
);


ALTER TABLE brasil_saas.bc_ativo_manutencao_material OWNER TO sa;

--
-- Name: bc_ativo_manutencao_material_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_manutencao_material ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_manutencao_material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ativo_medicao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_medicao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    ativo_id bigint NOT NULL,
    data_medicao date NOT NULL,
    valor numeric(15,2) NOT NULL,
    unidade character varying(20),
    observacao character varying(500)
);


ALTER TABLE brasil_saas.bc_ativo_medicao OWNER TO sa;

--
-- Name: bc_ativo_medicao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_medicao ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_medicao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ativo_movimento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_movimento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    ativo_id bigint NOT NULL,
    tipo character varying(30) NOT NULL,
    data_movimento date NOT NULL,
    periodo character varying(7) NOT NULL,
    valor numeric(15,2) DEFAULT 0 NOT NULL,
    valor_depreciacao numeric(15,2) DEFAULT 0 NOT NULL,
    valor_venda numeric(15,2),
    resultado numeric(15,2),
    execucao_id bigint,
    lancamento_id bigint,
    centro_custo_origem_id bigint,
    centro_custo_destino_id bigint,
    localizacao_origem character varying(255),
    localizacao_destino character varying(255),
    responsavel_origem_id bigint,
    responsavel_destino_id bigint,
    documento character varying(60),
    observacao character varying(1000),
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    usuario_id bigint
);


ALTER TABLE brasil_saas.bc_ativo_movimento OWNER TO sa;

--
-- Name: bc_ativo_movimento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_movimento ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_movimento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ativo_nota_manutencao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_nota_manutencao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    ativo_id bigint NOT NULL,
    numero character varying(50) NOT NULL,
    tipo character varying(30) DEFAULT 'AVARIA'::character varying NOT NULL,
    prioridade character varying(20) DEFAULT 'MEDIA'::character varying NOT NULL,
    status character varying(30) DEFAULT 'ABERTA'::character varying NOT NULL,
    descricao character varying(2000) NOT NULL,
    sintoma character varying(1000),
    causa character varying(1000),
    equipamento_parado boolean DEFAULT false NOT NULL,
    inicio_parada timestamp without time zone,
    fim_parada timestamp without time zone,
    data_nota date NOT NULL,
    manutencao_id bigint,
    solicitante_id bigint
);


ALTER TABLE brasil_saas.bc_ativo_nota_manutencao OWNER TO sa;

--
-- Name: bc_ativo_nota_manutencao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_nota_manutencao ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_nota_manutencao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ativo_plano_manutencao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ativo_plano_manutencao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    ativo_id bigint NOT NULL,
    codigo character varying(30) NOT NULL,
    descricao character varying(255) NOT NULL,
    tipo_ciclo character varying(20) DEFAULT 'TEMPO'::character varying NOT NULL,
    intervalo_dias integer,
    intervalo_contador numeric(15,2),
    antecedencia_dias integer DEFAULT 0 NOT NULL,
    ultima_execucao date,
    contador_ultima_execucao numeric(15,2),
    proxima_data date,
    checklist character varying(4000),
    horas_estimadas numeric(10,2),
    custo_estimado numeric(15,2),
    prioridade character varying(20) DEFAULT 'MEDIA'::character varying NOT NULL,
    responsavel_id bigint,
    ativo boolean DEFAULT true NOT NULL
);


ALTER TABLE brasil_saas.bc_ativo_plano_manutencao OWNER TO sa;

--
-- Name: bc_ativo_plano_manutencao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ativo_plano_manutencao ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ativo_plano_manutencao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_bi_dashboard; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_dashboard OWNER TO sa;

--
-- Name: TABLE bc_bi_dashboard; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_bi_dashboard IS 'Business Intelligence dashboards';


--
-- Name: bc_bi_dashboard_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_dashboard_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_dashboard_id_seq OWNER TO sa;

--
-- Name: bc_bi_dashboard_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_dashboard_id_seq OWNED BY brasil_saas.bc_bi_dashboard.id;


--
-- Name: bc_bi_dashboard_widget; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_dashboard_widget OWNER TO sa;

--
-- Name: TABLE bc_bi_dashboard_widget; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_bi_dashboard_widget IS 'Widgets that compose dashboards';


--
-- Name: bc_bi_dashboard_widget_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_dashboard_widget_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_dashboard_widget_id_seq OWNER TO sa;

--
-- Name: bc_bi_dashboard_widget_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_dashboard_widget_id_seq OWNED BY brasil_saas.bc_bi_dashboard_widget.id;


--
-- Name: bc_bi_indicador; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_indicador OWNER TO sa;

--
-- Name: bc_bi_indicador_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_indicador_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_indicador_id_seq OWNER TO sa;

--
-- Name: bc_bi_indicador_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_indicador_id_seq OWNED BY brasil_saas.bc_bi_indicador.id;


--
-- Name: bc_bi_kpi; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_kpi OWNER TO sa;

--
-- Name: TABLE bc_bi_kpi; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_bi_kpi IS 'Key Performance Indicators';


--
-- Name: bc_bi_kpi_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_kpi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_kpi_id_seq OWNER TO sa;

--
-- Name: bc_bi_kpi_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_kpi_id_seq OWNED BY brasil_saas.bc_bi_kpi.id;


--
-- Name: bc_bi_relatorio; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_relatorio OWNER TO sa;

--
-- Name: bc_bi_relatorio_agendado; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_relatorio_agendado OWNER TO sa;

--
-- Name: bc_bi_relatorio_agendado_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_relatorio_agendado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_relatorio_agendado_id_seq OWNER TO sa;

--
-- Name: bc_bi_relatorio_agendado_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_relatorio_agendado_id_seq OWNED BY brasil_saas.bc_bi_relatorio_agendado.id;


--
-- Name: bc_bi_relatorio_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_relatorio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_relatorio_id_seq OWNER TO sa;

--
-- Name: bc_bi_relatorio_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_relatorio_id_seq OWNED BY brasil_saas.bc_bi_relatorio.id;


--
-- Name: bc_bi_report; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_report OWNER TO sa;

--
-- Name: TABLE bc_bi_report; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_bi_report IS 'Business Intelligence reports';


--
-- Name: bc_bi_report_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_report_id_seq OWNER TO sa;

--
-- Name: bc_bi_report_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_report_id_seq OWNED BY brasil_saas.bc_bi_report.id;


--
-- Name: bc_bi_report_parameter; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_bi_report_parameter OWNER TO sa;

--
-- Name: TABLE bc_bi_report_parameter; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_bi_report_parameter IS 'Parameters for reports';


--
-- Name: bc_bi_report_parameter_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_bi_report_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_bi_report_parameter_id_seq OWNER TO sa;

--
-- Name: bc_bi_report_parameter_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_bi_report_parameter_id_seq OWNED BY brasil_saas.bc_bi_report_parameter.id;


--
-- Name: bc_cad_base_cep; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_base_cep OWNER TO sa;

--
-- Name: TABLE bc_cad_base_cep; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_base_cep IS 'Base de CEPs (global)';


--
-- Name: bc_cad_base_cep_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_base_cep_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_base_cep_id_seq OWNER TO sa;

--
-- Name: bc_cad_base_cep_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_base_cep_id_seq OWNED BY brasil_saas.bc_cad_base_cep.id;


--
-- Name: bc_cad_categoria; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_categoria OWNER TO sa;

--
-- Name: TABLE bc_cad_categoria; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_categoria IS 'Categorias de produtos/serviços (hierárquica)';


--
-- Name: bc_cad_categoria_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_categoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_categoria_id_seq OWNER TO sa;

--
-- Name: bc_cad_categoria_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_categoria_id_seq OWNED BY brasil_saas.bc_cad_categoria.id;


--
-- Name: bc_cad_cliente; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_cliente OWNER TO sa;

--
-- Name: TABLE bc_cad_cliente; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_cliente IS 'Clientes';


--
-- Name: COLUMN bc_cad_cliente.logo_url; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_cliente.logo_url IS 'URL do logo do cliente para relatórios e documentos';


--
-- Name: COLUMN bc_cad_cliente.logo_tipo_conteudo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_cliente.logo_tipo_conteudo IS 'Tipo MIME do logo do cliente';


--
-- Name: COLUMN bc_cad_cliente.logo_tamanho; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_cliente.logo_tamanho IS 'Tamanho em bytes do logo do cliente';


--
-- Name: bc_cad_cliente_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_cliente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_cliente_id_seq OWNER TO sa;

--
-- Name: bc_cad_cliente_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_cliente_id_seq OWNED BY brasil_saas.bc_cad_cliente.id;


--
-- Name: bc_cad_contato; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_contato OWNER TO sa;

--
-- Name: TABLE bc_cad_contato; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_contato IS 'Contatos das pessoas';


--
-- Name: bc_cad_contato_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_contato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_contato_id_seq OWNER TO sa;

--
-- Name: bc_cad_contato_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_contato_id_seq OWNED BY brasil_saas.bc_cad_contato.id;


--
-- Name: bc_cad_documento_fiscal; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_documento_fiscal OWNER TO sa;

--
-- Name: TABLE bc_cad_documento_fiscal; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_documento_fiscal IS 'Documentos fiscais recebidos/emitidos vinculados à pessoa';


--
-- Name: bc_cad_documento_fiscal_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_documento_fiscal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_documento_fiscal_id_seq OWNER TO sa;

--
-- Name: bc_cad_documento_fiscal_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_documento_fiscal_id_seq OWNED BY brasil_saas.bc_cad_documento_fiscal.id;


--
-- Name: bc_cad_endereco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_endereco OWNER TO sa;

--
-- Name: TABLE bc_cad_endereco; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_endereco IS 'Endereços das pessoas';


--
-- Name: bc_cad_endereco_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_endereco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_endereco_id_seq OWNER TO sa;

--
-- Name: bc_cad_endereco_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_endereco_id_seq OWNED BY brasil_saas.bc_cad_endereco.id;


--
-- Name: bc_cad_fornecedor; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_fornecedor OWNER TO sa;

--
-- Name: TABLE bc_cad_fornecedor; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_fornecedor IS 'Fornecedores';


--
-- Name: COLUMN bc_cad_fornecedor.logo_url; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_fornecedor.logo_url IS 'URL do logo do fornecedor';


--
-- Name: COLUMN bc_cad_fornecedor.logo_tipo_conteudo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_fornecedor.logo_tipo_conteudo IS 'Tipo MIME do logo do fornecedor';


--
-- Name: COLUMN bc_cad_fornecedor.logo_tamanho; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_fornecedor.logo_tamanho IS 'Tamanho em bytes do logo do fornecedor';


--
-- Name: bc_cad_fornecedor_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_fornecedor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_fornecedor_id_seq OWNER TO sa;

--
-- Name: bc_cad_fornecedor_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_fornecedor_id_seq OWNED BY brasil_saas.bc_cad_fornecedor.id;


--
-- Name: bc_cad_marca; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_marca OWNER TO sa;

--
-- Name: TABLE bc_cad_marca; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_marca IS 'Marcas de produtos';


--
-- Name: bc_cad_marca_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_marca_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_marca_id_seq OWNER TO sa;

--
-- Name: bc_cad_marca_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_marca_id_seq OWNED BY brasil_saas.bc_cad_marca.id;


--
-- Name: bc_cad_municipio; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_cad_municipio (
    id bigint NOT NULL,
    codigo_ibge character varying(7) NOT NULL,
    nome character varying(100) NOT NULL,
    uf character(2) NOT NULL,
    codigo_siafi character varying(10),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_cad_municipio OWNER TO sa;

--
-- Name: TABLE bc_cad_municipio; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_municipio IS 'Municípios IBGE (tabela oficial, global)';


--
-- Name: bc_cad_municipio_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_municipio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_municipio_id_seq OWNER TO sa;

--
-- Name: bc_cad_municipio_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_municipio_id_seq OWNED BY brasil_saas.bc_cad_municipio.id;


--
-- Name: bc_cad_papel; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_papel OWNER TO sa;

--
-- Name: TABLE bc_cad_papel; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_papel IS 'Papéis de relacionamento (CLIENTE, FORNECEDOR, TRANSPORTADORA...)';


--
-- Name: bc_cad_papel_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_papel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_papel_id_seq OWNER TO sa;

--
-- Name: bc_cad_papel_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_papel_id_seq OWNED BY brasil_saas.bc_cad_papel.id;


--
-- Name: bc_cad_pessoa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_pessoa OWNER TO sa;

--
-- Name: TABLE bc_cad_pessoa; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_pessoa IS 'Pessoas físicas e jurídicas (cadastro único)';


--
-- Name: COLUMN bc_cad_pessoa.tipo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_pessoa.tipo IS 'FISICA ou JURIDICA';


--
-- Name: bc_cad_pessoa_fisica; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_pessoa_fisica OWNER TO sa;

--
-- Name: TABLE bc_cad_pessoa_fisica; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_pessoa_fisica IS 'Complemento de pessoa física';


--
-- Name: bc_cad_pessoa_fisica_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_pessoa_fisica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_pessoa_fisica_id_seq OWNER TO sa;

--
-- Name: bc_cad_pessoa_fisica_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_pessoa_fisica_id_seq OWNED BY brasil_saas.bc_cad_pessoa_fisica.id;


--
-- Name: bc_cad_pessoa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_pessoa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_pessoa_id_seq OWNER TO sa;

--
-- Name: bc_cad_pessoa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_pessoa_id_seq OWNED BY brasil_saas.bc_cad_pessoa.id;


--
-- Name: bc_cad_pessoa_juridica; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_pessoa_juridica OWNER TO sa;

--
-- Name: TABLE bc_cad_pessoa_juridica; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_pessoa_juridica IS 'Complemento de pessoa jurídica';


--
-- Name: bc_cad_pessoa_juridica_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_pessoa_juridica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_pessoa_juridica_id_seq OWNER TO sa;

--
-- Name: bc_cad_pessoa_juridica_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_pessoa_juridica_id_seq OWNED BY brasil_saas.bc_cad_pessoa_juridica.id;


--
-- Name: bc_cad_produto; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_produto OWNER TO sa;

--
-- Name: TABLE bc_cad_produto; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_produto IS 'Produtos';


--
-- Name: COLUMN bc_cad_produto.cest; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_produto.cest IS 'CEST do produto, especificador da substituicao tributaria (7 digitos).';


--
-- Name: bc_cad_produto_ecommerce; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_produto_ecommerce OWNER TO sa;

--
-- Name: TABLE bc_cad_produto_ecommerce; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_produto_ecommerce IS 'Publicação de produtos em e-commerce/marketplace';


--
-- Name: bc_cad_produto_ecommerce_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_produto_ecommerce_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_produto_ecommerce_id_seq OWNER TO sa;

--
-- Name: bc_cad_produto_ecommerce_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_produto_ecommerce_id_seq OWNED BY brasil_saas.bc_cad_produto_ecommerce.id;


--
-- Name: bc_cad_produto_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_produto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_produto_id_seq OWNER TO sa;

--
-- Name: bc_cad_produto_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_produto_id_seq OWNED BY brasil_saas.bc_cad_produto.id;


--
-- Name: bc_cad_produto_imagem; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_produto_imagem OWNER TO sa;

--
-- Name: TABLE bc_cad_produto_imagem; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_produto_imagem IS 'Imagens de produtos';


--
-- Name: bc_cad_produto_imagem_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_produto_imagem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_produto_imagem_id_seq OWNER TO sa;

--
-- Name: bc_cad_produto_imagem_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_produto_imagem_id_seq OWNED BY brasil_saas.bc_cad_produto_imagem.id;


--
-- Name: bc_cad_produto_kit; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_cad_produto_kit (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    kit_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantidade numeric(15,3) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint
);


ALTER TABLE brasil_saas.bc_cad_produto_kit OWNER TO sa;

--
-- Name: TABLE bc_cad_produto_kit; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_produto_kit IS 'Composição de kits';


--
-- Name: bc_cad_produto_kit_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_produto_kit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_produto_kit_id_seq OWNER TO sa;

--
-- Name: bc_cad_produto_kit_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_produto_kit_id_seq OWNED BY brasil_saas.bc_cad_produto_kit.id;


--
-- Name: bc_cad_produto_variacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_produto_variacao OWNER TO sa;

--
-- Name: TABLE bc_cad_produto_variacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_produto_variacao IS 'Variações de produto (cor, tamanho...)';


--
-- Name: bc_cad_produto_variacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_produto_variacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_produto_variacao_id_seq OWNER TO sa;

--
-- Name: bc_cad_produto_variacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_produto_variacao_id_seq OWNED BY brasil_saas.bc_cad_produto_variacao.id;


--
-- Name: bc_cad_servico; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_servico OWNER TO sa;

--
-- Name: TABLE bc_cad_servico; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_servico IS 'Serviços ( NFS-e )';


--
-- Name: COLUMN bc_cad_servico.codigo_tributacao_municipal; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_cad_servico.codigo_tributacao_municipal IS 'Codigo de servico da Tabela de Servicos da Prefeitura de Sao Paulo (4 digitos). Ex.: 2919 = suporte tecnico em informatica (LC 116 01.07). Usado na emissao de NFS-e.';


--
-- Name: bc_cad_servico_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_servico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_servico_id_seq OWNER TO sa;

--
-- Name: bc_cad_servico_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_servico_id_seq OWNED BY brasil_saas.bc_cad_servico.id;


--
-- Name: bc_cad_transportadora; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_transportadora OWNER TO sa;

--
-- Name: TABLE bc_cad_transportadora; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_transportadora IS 'Transportadoras';


--
-- Name: bc_cad_transportadora_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_transportadora_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_transportadora_id_seq OWNER TO sa;

--
-- Name: bc_cad_transportadora_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_transportadora_id_seq OWNED BY brasil_saas.bc_cad_transportadora.id;


--
-- Name: bc_cad_unidade_medida; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cad_unidade_medida OWNER TO sa;

--
-- Name: TABLE bc_cad_unidade_medida; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_cad_unidade_medida IS 'Unidades de medida';


--
-- Name: bc_cad_unidade_medida_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_cad_unidade_medida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_cad_unidade_medida_id_seq OWNER TO sa;

--
-- Name: bc_cad_unidade_medida_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_cad_unidade_medida_id_seq OWNED BY brasil_saas.bc_cad_unidade_medida.id;


--
-- Name: bc_cmp_devolucao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cmp_devolucao OWNER TO sa;

--
-- Name: bc_cmp_devolucao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_cmp_devolucao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cmp_devolucao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_cmp_devolucao_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_cmp_devolucao_item OWNER TO sa;

--
-- Name: bc_cmp_devolucao_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_cmp_devolucao_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cmp_devolucao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_com_conferencia_fatura; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_conferencia_fatura OWNER TO sa;

--
-- Name: TABLE bc_com_conferencia_fatura; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_com_conferencia_fatura IS 'Conferencia tripla pedido, recebimento e titulo';


--
-- Name: COLUMN bc_com_conferencia_fatura.nfe_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_com_conferencia_fatura.nfe_id IS 'NF-e de entrada vinculada à conferência 3-way';


--
-- Name: bc_com_conferencia_fatura_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_conferencia_fatura_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_conferencia_fatura_id_seq OWNER TO sa;

--
-- Name: bc_com_conferencia_fatura_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_conferencia_fatura_id_seq OWNED BY brasil_saas.bc_com_conferencia_fatura.id;


--
-- Name: bc_com_conferencia_fatura_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_com_conferencia_fatura_item (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    conferencia_id bigint NOT NULL,
    produto_id bigint,
    numero_item integer,
    descricao character varying(300),
    quantidade_pedida numeric(15,4) DEFAULT 0 NOT NULL,
    quantidade_recebida numeric(15,4) DEFAULT 0 NOT NULL,
    quantidade_faturada numeric(15,4) DEFAULT 0 NOT NULL,
    valor_unitario_pedido numeric(15,4) DEFAULT 0 NOT NULL,
    valor_unitario_recebido numeric(15,4) DEFAULT 0 NOT NULL,
    valor_unitario_faturado numeric(15,4) DEFAULT 0 NOT NULL,
    valor_total_pedido numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total_recebido numeric(15,2) DEFAULT 0 NOT NULL,
    valor_total_faturado numeric(15,2) DEFAULT 0 NOT NULL,
    conforme boolean DEFAULT true NOT NULL,
    tipo_divergencia character varying(40),
    divergencia character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    pedido_item_id bigint,
    recebimento_item_id bigint,
    nfe_item_id bigint,
    tolerancia numeric(18,4) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'DIVERGENTE'::character varying NOT NULL
);


ALTER TABLE brasil_saas.bc_com_conferencia_fatura_item OWNER TO sa;

--
-- Name: bc_com_conferencia_fatura_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_com_conferencia_fatura_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_com_conferencia_fatura_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_com_contrato; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    deleted_at timestamp without time zone,
    condicao_pagamento_id bigint,
    observacao text,
    tipo character varying(20)
);


ALTER TABLE brasil_saas.bc_com_contrato OWNER TO sa;

--
-- Name: TABLE bc_com_contrato; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_com_contrato IS 'Contratos de fornecimento';


--
-- Name: bc_com_contrato_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_contrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_contrato_id_seq OWNER TO sa;

--
-- Name: bc_com_contrato_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_contrato_id_seq OWNED BY brasil_saas.bc_com_contrato.id;


--
-- Name: bc_com_contrato_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_com_contrato_item (
    id bigint NOT NULL,
    uuid uuid NOT NULL,
    empresa_id bigint NOT NULL,
    contrato_id bigint NOT NULL,
    numero_item integer NOT NULL,
    produto_id bigint,
    descricao character varying(300),
    unidade character varying(10),
    quantidade_contratada numeric(15,3) NOT NULL,
    quantidade_liberada numeric(15,3) DEFAULT 0 NOT NULL,
    valor_unitario numeric(15,4) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_com_contrato_item OWNER TO sa;

--
-- Name: bc_com_contrato_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_contrato_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_contrato_item_id_seq OWNER TO sa;

--
-- Name: bc_com_contrato_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_contrato_item_id_seq OWNED BY brasil_saas.bc_com_contrato_item.id;


--
-- Name: bc_com_cotacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_cotacao OWNER TO sa;

--
-- Name: TABLE bc_com_cotacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_com_cotacao IS 'Cotacoes de compra';


--
-- Name: bc_com_cotacao_fornecedor; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_cotacao_fornecedor OWNER TO sa;

--
-- Name: bc_com_cotacao_fornecedor_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_cotacao_fornecedor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_cotacao_fornecedor_id_seq OWNER TO sa;

--
-- Name: bc_com_cotacao_fornecedor_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_cotacao_fornecedor_id_seq OWNED BY brasil_saas.bc_com_cotacao_fornecedor.id;


--
-- Name: bc_com_cotacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_cotacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_cotacao_id_seq OWNER TO sa;

--
-- Name: bc_com_cotacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_cotacao_id_seq OWNED BY brasil_saas.bc_com_cotacao.id;


--
-- Name: bc_com_cotacao_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_cotacao_item OWNER TO sa;

--
-- Name: bc_com_cotacao_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_cotacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_cotacao_item_id_seq OWNER TO sa;

--
-- Name: bc_com_cotacao_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_cotacao_item_id_seq OWNED BY brasil_saas.bc_com_cotacao_item.id;


--
-- Name: bc_com_pedido; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    deleted_at timestamp without time zone,
    contrato_id bigint
);


ALTER TABLE brasil_saas.bc_com_pedido OWNER TO sa;

--
-- Name: TABLE bc_com_pedido; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_com_pedido IS 'Pedidos de compra';


--
-- Name: COLUMN bc_com_pedido.status; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_com_pedido.status IS 'ABERTO, RECEBIDO ou CANCELADO';


--
-- Name: COLUMN bc_com_pedido.titulo_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_com_pedido.titulo_id IS 'Título a pagar gerado no financeiro';


--
-- Name: bc_com_pedido_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_pedido_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_pedido_id_seq OWNER TO sa;

--
-- Name: bc_com_pedido_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_pedido_id_seq OWNED BY brasil_saas.bc_com_pedido.id;


--
-- Name: bc_com_pedido_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_pedido_item OWNER TO sa;

--
-- Name: COLUMN bc_com_pedido_item.quantidade_recebida; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_com_pedido_item.quantidade_recebida IS 'Quantidade fisicamente recebida e conferida do item do pedido';


--
-- Name: bc_com_pedido_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_pedido_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_pedido_item_id_seq OWNER TO sa;

--
-- Name: bc_com_pedido_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_pedido_item_id_seq OWNED BY brasil_saas.bc_com_pedido_item.id;


--
-- Name: bc_com_recebimento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_recebimento OWNER TO sa;

--
-- Name: TABLE bc_com_recebimento; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_com_recebimento IS 'Recebimentos/conferencia de compras';


--
-- Name: COLUMN bc_com_recebimento.documento_fornecedor; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_com_recebimento.documento_fornecedor IS 'Numero do documento fiscal ou documento de entrada informado pelo fornecedor';


--
-- Name: bc_com_recebimento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_recebimento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_recebimento_id_seq OWNER TO sa;

--
-- Name: bc_com_recebimento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_recebimento_id_seq OWNED BY brasil_saas.bc_com_recebimento.id;


--
-- Name: bc_com_recebimento_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_recebimento_item OWNER TO sa;

--
-- Name: bc_com_recebimento_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_recebimento_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_recebimento_item_id_seq OWNER TO sa;

--
-- Name: bc_com_recebimento_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_recebimento_item_id_seq OWNED BY brasil_saas.bc_com_recebimento_item.id;


--
-- Name: bc_com_solicitacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_solicitacao OWNER TO sa;

--
-- Name: TABLE bc_com_solicitacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_com_solicitacao IS 'Solicitacoes internas de compra';


--
-- Name: bc_com_solicitacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_solicitacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_solicitacao_id_seq OWNER TO sa;

--
-- Name: bc_com_solicitacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_solicitacao_id_seq OWNED BY brasil_saas.bc_com_solicitacao.id;


--
-- Name: bc_com_solicitacao_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_com_solicitacao_item OWNER TO sa;

--
-- Name: bc_com_solicitacao_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_com_solicitacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_com_solicitacao_item_id_seq OWNER TO sa;

--
-- Name: bc_com_solicitacao_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_com_solicitacao_item_id_seq OWNED BY brasil_saas.bc_com_solicitacao_item.id;


--
-- Name: bc_cont_fechamento_check; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_cont_fechamento_check (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    periodo_id bigint NOT NULL,
    etapa character varying(50) NOT NULL,
    codigo character varying(80) NOT NULL,
    descricao character varying(300) NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    quantidade numeric(18,2) DEFAULT 0,
    executado_em timestamp without time zone,
    executado_por bigint,
    observacao character varying(1000)
);


ALTER TABLE brasil_saas.bc_cont_fechamento_check OWNER TO sa;

--
-- Name: bc_cont_fechamento_check_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_cont_fechamento_check ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cont_fechamento_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_cont_rateio; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_cont_rateio (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    lancamento_id bigint,
    centro_custo_id bigint,
    plano_contas_id bigint,
    percentual numeric(9,4) NOT NULL,
    valor numeric(18,2) DEFAULT 0 NOT NULL,
    competencia date NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_cont_rateio OWNER TO sa;

--
-- Name: bc_cont_rateio_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_cont_rateio ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cont_rateio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_cont_regra_lancamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_cont_regra_lancamento (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(200) NOT NULL,
    origem character varying(50) NOT NULL,
    conta_debito_id bigint,
    conta_credito_id bigint,
    centro_custo_id bigint,
    historico character varying(500),
    ativo boolean DEFAULT true NOT NULL,
    configuracao jsonb DEFAULT '{}'::jsonb NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_cont_regra_lancamento OWNER TO sa;

--
-- Name: bc_cont_regra_lancamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_cont_regra_lancamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cont_regra_lancamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_cont_relatorio_snapshot; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_cont_relatorio_snapshot (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    periodo_id bigint NOT NULL,
    tipo character varying(30) NOT NULL,
    conta_id bigint,
    codigo_conta character varying(50),
    descricao character varying(255),
    debito numeric(18,2) DEFAULT 0 NOT NULL,
    credito numeric(18,2) DEFAULT 0 NOT NULL,
    saldo numeric(18,2) DEFAULT 0 NOT NULL,
    nivel integer DEFAULT 0,
    dados jsonb DEFAULT '{}'::jsonb,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_cont_relatorio_snapshot OWNER TO sa;

--
-- Name: bc_cont_relatorio_snapshot_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_cont_relatorio_snapshot ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_cont_relatorio_snapshot_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_core_auditoria; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_auditoria OWNER TO sa;

--
-- Name: TABLE bc_core_auditoria; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_auditoria IS 'Trilha de auditoria de operações sensíveis';


--
-- Name: bc_core_auditoria_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_auditoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_auditoria_id_seq OWNER TO sa;

--
-- Name: bc_core_auditoria_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_auditoria_id_seq OWNED BY brasil_saas.bc_core_auditoria.id;


--
-- Name: bc_core_auth_source; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_core_auth_source (
    username character varying(256) NOT NULL,
    source character varying(16) NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT bc_core_auth_source_source_check CHECK (((source)::text = ANY ((ARRAY['AD'::character varying, 'POSTGRES'::character varying, 'LINUX'::character varying])::text[])))
);


ALTER TABLE brasil_saas.bc_core_auth_source OWNER TO sa;

--
-- Name: bc_core_banco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_banco OWNER TO sa;

--
-- Name: bc_core_configuracao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_configuracao OWNER TO sa;

--
-- Name: TABLE bc_core_configuracao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_configuracao IS 'Configurações por empresa (chave/valor)';


--
-- Name: bc_core_configuracao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_configuracao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_configuracao_id_seq OWNER TO sa;

--
-- Name: bc_core_configuracao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_configuracao_id_seq OWNED BY brasil_saas.bc_core_configuracao.id;


--
-- Name: bc_core_documento_fluxo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_core_documento_fluxo (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    origem_tipo character varying(40) NOT NULL,
    origem_id bigint NOT NULL,
    origem_numero character varying(60),
    destino_tipo character varying(40) NOT NULL,
    destino_id bigint NOT NULL,
    destino_numero character varying(60),
    relacao character varying(40) DEFAULT 'GERA'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint
);


ALTER TABLE brasil_saas.bc_core_documento_fluxo OWNER TO sa;

--
-- Name: bc_core_documento_fluxo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_documento_fluxo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_documento_fluxo_id_seq OWNER TO sa;

--
-- Name: bc_core_documento_fluxo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_documento_fluxo_id_seq OWNED BY brasil_saas.bc_core_documento_fluxo.id;


--
-- Name: bc_core_empresa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_empresa OWNER TO sa;

--
-- Name: TABLE bc_core_empresa; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_empresa IS 'Empresas (raiz do multi-tenant)';


--
-- Name: COLUMN bc_core_empresa.logo_url; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.logo_url IS 'URL do logo da empresa para relatórios, NF-e e OS';


--
-- Name: COLUMN bc_core_empresa.logo_tipo_conteudo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.logo_tipo_conteudo IS 'Tipo MIME do logo da empresa';


--
-- Name: COLUMN bc_core_empresa.logo_tamanho; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.logo_tamanho IS 'Tamanho em bytes do logo da empresa';


--
-- Name: COLUMN bc_core_empresa.matriz; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.matriz IS 'true quando esta empresa e'' a matriz do grupo. Uma por grupo, garantido pelo indice parcial unico em grupo_cnpj_cpf.';


--
-- Name: COLUMN bc_core_empresa.grupo_cnpj_cpf; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.grupo_cnpj_cpf IS 'A chave do grupo empresarial: CPF para produtor rural, CNPJ para empresa comum. E'' a coluna que separa uma empresa que a outra.';


--
-- Name: COLUMN bc_core_empresa.matriz_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.matriz_id IS 'A matriz do grupo desta filial. NULL na propria matriz. A constraint abaixo garante que a empresa apontada e'' de verdade a matriz do mesmo grupo.';


--
-- Name: COLUMN bc_core_empresa.tipo_pessoa; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.tipo_pessoa IS 'PF para produtor rural, PJ para empresa. Determina se a chave do grupo e'' CPF ou CNPJ.';


--
-- Name: COLUMN bc_core_empresa.stripe_secret_key_encrypted; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.stripe_secret_key_encrypted IS 'Chave secreta Stripe da empresa, cifrada com APP_SECURITY_SECRET_ENCRYPTION_KEY';


--
-- Name: COLUMN bc_core_empresa.stripe_webhook_secret_encrypted; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_empresa.stripe_webhook_secret_encrypted IS 'Signing secret do webhook Stripe da empresa, cifrado';


--
-- Name: bc_core_empresa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_empresa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_empresa_id_seq OWNER TO sa;

--
-- Name: bc_core_empresa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_empresa_id_seq OWNED BY brasil_saas.bc_core_empresa.id;


--
-- Name: bc_core_empresa_vinculo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_empresa_vinculo OWNER TO sa;

--
-- Name: TABLE bc_core_empresa_vinculo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_empresa_vinculo IS 'Vínculos entre empresas (matriz/filial, contador, parceira)';


--
-- Name: bc_core_empresa_vinculo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_empresa_vinculo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_empresa_vinculo_id_seq OWNER TO sa;

--
-- Name: bc_core_empresa_vinculo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_empresa_vinculo_id_seq OWNED BY brasil_saas.bc_core_empresa_vinculo.id;


--
-- Name: bc_core_integration_event; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_integration_event OWNER TO sa;

--
-- Name: bc_core_integration_event_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_integration_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_integration_event_id_seq OWNER TO sa;

--
-- Name: bc_core_integration_event_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_integration_event_id_seq OWNED BY brasil_saas.bc_core_integration_event.id;


--
-- Name: bc_core_log_acesso; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_log_acesso OWNER TO sa;

--
-- Name: TABLE bc_core_log_acesso; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_log_acesso IS 'Log de tentativas de login/acesso';


--
-- Name: bc_core_log_acesso_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_log_acesso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_log_acesso_id_seq OWNER TO sa;

--
-- Name: bc_core_log_acesso_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_log_acesso_id_seq OWNED BY brasil_saas.bc_core_log_acesso.id;


--
-- Name: bc_core_modulo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_modulo OWNER TO sa;

--
-- Name: TABLE bc_core_modulo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_modulo IS 'Modulos do ERP. A coluna exige_superuser marca os quelidam documento/certificado e exigem SUPERUSER.';


--
-- Name: bc_core_modulo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_modulo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_modulo_id_seq OWNER TO sa;

--
-- Name: bc_core_modulo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_modulo_id_seq OWNED BY brasil_saas.bc_core_modulo.id;


--
-- Name: bc_core_notificacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    deleted_at timestamp without time zone,
    updated_at timestamp(6) without time zone,
    link character varying(300)
);


ALTER TABLE brasil_saas.bc_core_notificacao OWNER TO sa;

--
-- Name: TABLE bc_core_notificacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_notificacao IS 'Notificações para usuários';


--
-- Name: bc_core_notificacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_notificacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_notificacao_id_seq OWNER TO sa;

--
-- Name: bc_core_notificacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_notificacao_id_seq OWNED BY brasil_saas.bc_core_notificacao.id;


--
-- Name: bc_core_perfil; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_perfil OWNER TO sa;

--
-- Name: TABLE bc_core_perfil; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_perfil IS 'Perfil de acesso. SUPERUSER opera o gerenciador SQL; enquanto a separacao nao for avaliada, SUPERUSER e ADMIN tem as mesmas permissoes.';


--
-- Name: COLUMN bc_core_perfil.hierarquia_nivel; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_perfil.hierarquia_nivel IS 'Nível hierárquico do perfil (usado para ordenação e permissões em cascata)';


--
-- Name: COLUMN bc_core_perfil.perfil_pai_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_perfil.perfil_pai_id IS 'Perfil pai do qual este perfil herda permissões efetivas';


--
-- Name: bc_core_perfil_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_perfil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_perfil_id_seq OWNER TO sa;

--
-- Name: bc_core_perfil_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_perfil_id_seq OWNED BY brasil_saas.bc_core_perfil.id;


--
-- Name: bc_core_perfil_permissao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_core_perfil_permissao (
    id bigint NOT NULL,
    perfil_id bigint NOT NULL,
    permissao_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint
);


ALTER TABLE brasil_saas.bc_core_perfil_permissao OWNER TO sa;

--
-- Name: TABLE bc_core_perfil_permissao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_perfil_permissao IS 'Associação perfil ↔ permissão';


--
-- Name: bc_core_perfil_permissao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_perfil_permissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_perfil_permissao_id_seq OWNER TO sa;

--
-- Name: bc_core_perfil_permissao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_perfil_permissao_id_seq OWNED BY brasil_saas.bc_core_perfil_permissao.id;


--
-- Name: bc_core_permissao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_permissao OWNER TO sa;

--
-- Name: TABLE bc_core_permissao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_permissao IS 'Permissões por recurso/ação (ex.: CADASTRO_CLIENTE_CRIAR)';


--
-- Name: bc_core_permissao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_permissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_permissao_id_seq OWNER TO sa;

--
-- Name: bc_core_permissao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_permissao_id_seq OWNED BY brasil_saas.bc_core_permissao.id;


--
-- Name: bc_core_sessao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_sessao OWNER TO sa;

--
-- Name: TABLE bc_core_sessao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_sessao IS 'Sessões JWT ativas/revogadas';


--
-- Name: bc_core_sessao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_sessao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_sessao_id_seq OWNER TO sa;

--
-- Name: bc_core_sessao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_sessao_id_seq OWNED BY brasil_saas.bc_core_sessao.id;


--
-- Name: bc_core_usuario; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_core_usuario OWNER TO sa;

--
-- Name: TABLE bc_core_usuario; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_usuario IS 'Usuários do sistema';


--
-- Name: COLUMN bc_core_usuario.empresa_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_usuario.empresa_id IS 'Empresa do usuario. NULL significa que ele ainda nao cadastrou a sua: o sistema so libera o resto depois do cadastro da empresa.';


--
-- Name: COLUMN bc_core_usuario.senha_hash; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_usuario.senha_hash IS 'BCrypt hash — nunca armazenar senha em texto';


--
-- Name: COLUMN bc_core_usuario.foto_url; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_usuario.foto_url IS 'URL da foto do usuário para perfil';


--
-- Name: COLUMN bc_core_usuario.foto_tipo_conteudo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_usuario.foto_tipo_conteudo IS 'Tipo MIME da foto do usuário';


--
-- Name: COLUMN bc_core_usuario.foto_tamanho; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_core_usuario.foto_tamanho IS 'Tamanho em bytes da foto do usuário';


--
-- Name: bc_core_usuario_empresa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_core_usuario_empresa (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    perfil_id bigint NOT NULL,
    perfil_nome character varying(50) NOT NULL,
    criado_em timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em timestamp with time zone
);


ALTER TABLE brasil_saas.bc_core_usuario_empresa OWNER TO sa;

--
-- Name: TABLE bc_core_usuario_empresa; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_usuario_empresa IS '
    Vinculo usuario x empresa x papel. UNIQUE(usuario_id, perfil_nome) impede a
    mesma pessoa de repetir o mesmo papel em duas empresas. Permite papeis
    diferentes em empresas diferentes. perfil_nome e'' desnormalizado de proposito:
    a constraint precisa do nome, e nao do id do perfil, porque cada empresa tem
    a sua propria linha em bc_core_perfil com o mesmo nome.
';


--
-- Name: bc_core_usuario_empresa_backup_20260928; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_core_usuario_empresa_backup_20260928 (
    id bigint,
    usuario_id bigint,
    empresa_id bigint,
    perfil_id bigint,
    perfil_nome character varying(50),
    criado_em timestamp with time zone,
    atualizado_em timestamp with time zone
);


ALTER TABLE brasil_saas.bc_core_usuario_empresa_backup_20260928 OWNER TO sa;

--
-- Name: bc_core_usuario_empresa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_usuario_empresa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_usuario_empresa_id_seq OWNER TO sa;

--
-- Name: bc_core_usuario_empresa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_usuario_empresa_id_seq OWNED BY brasil_saas.bc_core_usuario_empresa.id;


--
-- Name: bc_core_usuario_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_usuario_id_seq OWNER TO sa;

--
-- Name: bc_core_usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_usuario_id_seq OWNED BY brasil_saas.bc_core_usuario.id;


--
-- Name: bc_core_usuario_modulo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_core_usuario_modulo (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    modulo_id bigint NOT NULL,
    somente_leitura boolean DEFAULT false NOT NULL,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    criado_por bigint
);


ALTER TABLE brasil_saas.bc_core_usuario_modulo OWNER TO sa;

--
-- Name: TABLE bc_core_usuario_modulo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_usuario_modulo IS 'Modulos liberados por usuario. many-to-many: permite Financeiro + Estoque sem criar perfil novo.';


--
-- Name: bc_core_usuario_modulo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_usuario_modulo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_usuario_modulo_id_seq OWNER TO sa;

--
-- Name: bc_core_usuario_modulo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_usuario_modulo_id_seq OWNED BY brasil_saas.bc_core_usuario_modulo.id;


--
-- Name: bc_core_usuario_perfil; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_core_usuario_perfil (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    perfil_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint
);


ALTER TABLE brasil_saas.bc_core_usuario_perfil OWNER TO sa;

--
-- Name: TABLE bc_core_usuario_perfil; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_core_usuario_perfil IS 'Associação usuário ↔ perfil';


--
-- Name: bc_core_usuario_perfil_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_core_usuario_perfil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_core_usuario_perfil_id_seq OWNER TO sa;

--
-- Name: bc_core_usuario_perfil_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_core_usuario_perfil_id_seq OWNED BY brasil_saas.bc_core_usuario_perfil.id;


--
-- Name: bc_crm_atividade; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_crm_atividade OWNER TO sa;

--
-- Name: TABLE bc_crm_atividade; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_crm_atividade IS 'Atividades comerciais e follow-up';


--
-- Name: bc_crm_atividade_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_crm_atividade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_crm_atividade_id_seq OWNER TO sa;

--
-- Name: bc_crm_atividade_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_crm_atividade_id_seq OWNED BY brasil_saas.bc_crm_atividade.id;


--
-- Name: bc_crm_lead; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_crm_lead OWNER TO sa;

--
-- Name: bc_crm_lead_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_crm_lead ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_crm_lead_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_crm_oportunidade; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_crm_oportunidade OWNER TO sa;

--
-- Name: TABLE bc_crm_oportunidade; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_crm_oportunidade IS 'Pipeline comercial/CRM';


--
-- Name: bc_crm_oportunidade_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_crm_oportunidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_crm_oportunidade_id_seq OWNER TO sa;

--
-- Name: bc_crm_oportunidade_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_crm_oportunidade_id_seq OWNED BY brasil_saas.bc_crm_oportunidade.id;


--
-- Name: bc_crm_tarefa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_crm_tarefa OWNER TO sa;

--
-- Name: bc_crm_tarefa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_crm_tarefa ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_crm_tarefa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ctb_fechamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ctb_fechamento OWNER TO sa;

--
-- Name: bc_ctb_fechamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ctb_fechamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ctb_fechamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ctb_lancamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ctb_lancamento OWNER TO sa;

--
-- Name: bc_ctb_lancamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ctb_lancamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ctb_lancamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ctb_partida; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ctb_partida OWNER TO sa;

--
-- Name: bc_ctb_partida_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ctb_partida ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ctb_partida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_dms_aprovacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_dms_aprovacao OWNER TO sa;

--
-- Name: bc_dms_aprovacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_dms_aprovacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_dms_aprovacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_dms_documento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_dms_documento OWNER TO sa;

--
-- Name: bc_dms_documento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_dms_documento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_dms_documento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_dms_versao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_dms_versao OWNER TO sa;

--
-- Name: bc_dms_versao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_dms_versao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_dms_versao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ehs_acao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ehs_acao (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ocorrencia_id bigint,
    inspecao_id bigint,
    descricao character varying(1000) NOT NULL,
    responsavel character varying(160),
    prazo date,
    status character varying(30) DEFAULT 'ABERTA'::character varying NOT NULL,
    concluida_em timestamp without time zone,
    evidencia character varying(1000)
);


ALTER TABLE brasil_saas.bc_ehs_acao OWNER TO sa;

--
-- Name: bc_ehs_acao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ehs_acao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ehs_acao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ehs_inspecao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ehs_inspecao (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(80) NOT NULL,
    tipo character varying(80) NOT NULL,
    localizacao character varying(300),
    responsavel character varying(160),
    data_inspecao timestamp without time zone DEFAULT now() NOT NULL,
    status character varying(30) DEFAULT 'ABERTA'::character varying NOT NULL,
    resultado character varying(30),
    observacao text
);


ALTER TABLE brasil_saas.bc_ehs_inspecao OWNER TO sa;

--
-- Name: bc_ehs_inspecao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ehs_inspecao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ehs_inspecao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ehs_ocorrencia; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ehs_ocorrencia (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(80) NOT NULL,
    tipo character varying(40) NOT NULL,
    severidade character varying(20) DEFAULT 'MEDIA'::character varying NOT NULL,
    data_ocorrencia timestamp without time zone DEFAULT now() NOT NULL,
    local_ocorrencia character varying(200),
    funcionario_id bigint,
    ativo_id bigint,
    descricao text NOT NULL,
    causa_raiz text,
    acao_corretiva text,
    status character varying(25) DEFAULT 'ABERTA'::character varying NOT NULL,
    prazo date,
    encerrado_em timestamp without time zone,
    encerrado_por bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ehs_ocorrencia OWNER TO sa;

--
-- Name: bc_ehs_ocorrencia_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ehs_ocorrencia ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ehs_ocorrencia_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ehs_permissao_trabalho; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ehs_permissao_trabalho (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(80) NOT NULL,
    tipo character varying(80) NOT NULL,
    localizacao character varying(300) NOT NULL,
    solicitante character varying(160),
    responsavel character varying(160),
    inicio timestamp without time zone,
    fim timestamp without time zone,
    riscos text,
    controles text,
    status character varying(30) DEFAULT 'SOLICITADA'::character varying NOT NULL,
    aprovada_por character varying(160),
    aprovada_em timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ehs_permissao_trabalho OWNER TO sa;

--
-- Name: bc_ehs_permissao_trabalho_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ehs_permissao_trabalho ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ehs_permissao_trabalho_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ehs_risco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ehs_risco (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(80) NOT NULL,
    perigo character varying(300) NOT NULL,
    atividade character varying(300),
    localizacao character varying(300),
    probabilidade numeric(8,2) DEFAULT 1 NOT NULL,
    impacto numeric(8,2) DEFAULT 1 NOT NULL,
    nivel numeric(12,2) DEFAULT 1 NOT NULL,
    controle_existente text,
    responsavel character varying(160),
    status character varying(30) DEFAULT 'ATIVO'::character varying NOT NULL,
    revisado_em timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ehs_risco OWNER TO sa;

--
-- Name: bc_ehs_risco_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ehs_risco ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ehs_risco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ent_cenario_tributario; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ent_cenario_tributario (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    nome character varying(120) NOT NULL,
    vigencia_inicio date NOT NULL,
    vigencia_fim date,
    regime character varying(30),
    uf_origem character(2),
    uf_destino character(2),
    cst character varying(10),
    cfop character varying(10),
    aliquota_icms numeric(8,4),
    aliquota_ibs numeric(8,4),
    aliquota_cbs numeric(8,4),
    reducao numeric(8,4) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ent_cenario_tributario OWNER TO sa;

--
-- Name: bc_ent_cenario_tributario_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ent_cenario_tributario ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ent_cenario_tributario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ent_contrato; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ent_contrato (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    tipo character varying(40) NOT NULL,
    numero character varying(80) NOT NULL,
    parceiro_tipo character varying(30),
    parceiro_id bigint,
    descricao character varying(500) NOT NULL,
    status character varying(20) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    inicio date,
    fim date,
    valor_total numeric(18,2) DEFAULT 0 NOT NULL,
    renovacao_automatica boolean DEFAULT false NOT NULL,
    indice_reajuste character varying(40),
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ent_contrato OWNER TO sa;

--
-- Name: bc_ent_contrato_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ent_contrato ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ent_contrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ent_fornecedor_qualificacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ent_fornecedor_qualificacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    fornecedor_id bigint NOT NULL,
    status character varying(25) DEFAULT 'PENDENTE'::character varying NOT NULL,
    score numeric(6,2),
    validade date,
    categoria character varying(80),
    observacao text,
    aprovado_em timestamp without time zone,
    aprovado_por bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ent_fornecedor_qualificacao OWNER TO sa;

--
-- Name: bc_ent_fornecedor_qualificacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ent_fornecedor_qualificacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ent_fornecedor_qualificacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ent_meta_comercial; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ent_meta_comercial (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    vendedor_id bigint,
    periodo_inicio date NOT NULL,
    periodo_fim date NOT NULL,
    meta_valor numeric(18,2) DEFAULT 0 NOT NULL,
    meta_quantidade numeric(18,3) DEFAULT 0 NOT NULL,
    realizado_valor numeric(18,2) DEFAULT 0 NOT NULL,
    realizado_quantidade numeric(18,3) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ent_meta_comercial OWNER TO sa;

--
-- Name: bc_ent_meta_comercial_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ent_meta_comercial ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ent_meta_comercial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ent_orcamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ent_orcamento (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia date NOT NULL,
    centro_custo_id bigint,
    conta_contabil_id bigint,
    versao integer DEFAULT 1 NOT NULL,
    valor_orcado numeric(18,2) DEFAULT 0 NOT NULL,
    valor_revisado numeric(18,2) DEFAULT 0 NOT NULL,
    valor_realizado numeric(18,2) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    observacao character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ent_orcamento OWNER TO sa;

--
-- Name: bc_ent_orcamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ent_orcamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ent_orcamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ent_periodo_contabil; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ent_periodo_contabil (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia date NOT NULL,
    status character varying(20) DEFAULT 'ABERTO'::character varying NOT NULL,
    fechamento_financeiro boolean DEFAULT false NOT NULL,
    fechamento_fiscal boolean DEFAULT false NOT NULL,
    fechamento_contabil boolean DEFAULT false NOT NULL,
    observacao character varying(500),
    fechado_em timestamp without time zone,
    fechado_por bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);


ALTER TABLE brasil_saas.bc_ent_periodo_contabil OWNER TO sa;

--
-- Name: bc_ent_periodo_contabil_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ent_periodo_contabil ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ent_periodo_contabil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ent_tesouraria_previsao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ent_tesouraria_previsao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    data_prevista date NOT NULL,
    tipo character varying(20) NOT NULL,
    origem character varying(40),
    referencia_id bigint,
    descricao character varying(500) NOT NULL,
    valor numeric(18,2) NOT NULL,
    probabilidade numeric(5,2) DEFAULT 100 NOT NULL,
    status character varying(20) DEFAULT 'PROJETADA'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_ent_tesouraria_previsao OWNER TO sa;

--
-- Name: bc_ent_tesouraria_previsao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ent_tesouraria_previsao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ent_tesouraria_previsao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_esocial_evento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_esocial_evento OWNER TO sa;

--
-- Name: bc_esocial_evento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_esocial_evento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_esocial_evento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_est_deposito; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_deposito OWNER TO sa;

--
-- Name: TABLE bc_est_deposito; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_est_deposito IS 'Depositos/almoxarifados por empresa';


--
-- Name: bc_est_deposito_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_deposito_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_deposito_id_seq OWNER TO sa;

--
-- Name: bc_est_deposito_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_deposito_id_seq OWNED BY brasil_saas.bc_est_deposito.id;


--
-- Name: bc_est_endereco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_endereco OWNER TO sa;

--
-- Name: TABLE bc_est_endereco; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_est_endereco IS 'Enderecamento fisico de estoque';


--
-- Name: bc_est_endereco_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_endereco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_endereco_id_seq OWNER TO sa;

--
-- Name: bc_est_endereco_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_endereco_id_seq OWNED BY brasil_saas.bc_est_endereco.id;


--
-- Name: bc_est_expedicao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_expedicao OWNER TO sa;

--
-- Name: bc_est_expedicao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_expedicao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_expedicao_id_seq OWNER TO sa;

--
-- Name: bc_est_expedicao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_expedicao_id_seq OWNED BY brasil_saas.bc_est_expedicao.id;


--
-- Name: bc_est_expedicao_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_expedicao_item OWNER TO sa;

--
-- Name: bc_est_expedicao_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_expedicao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_expedicao_item_id_seq OWNER TO sa;

--
-- Name: bc_est_expedicao_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_expedicao_item_id_seq OWNED BY brasil_saas.bc_est_expedicao_item.id;


--
-- Name: bc_est_inventario; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_inventario OWNER TO sa;

--
-- Name: bc_est_inventario_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_inventario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_inventario_id_seq OWNER TO sa;

--
-- Name: bc_est_inventario_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_inventario_id_seq OWNED BY brasil_saas.bc_est_inventario.id;


--
-- Name: bc_est_inventario_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_inventario_item OWNER TO sa;

--
-- Name: bc_est_inventario_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_inventario_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_inventario_item_id_seq OWNER TO sa;

--
-- Name: bc_est_inventario_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_inventario_item_id_seq OWNED BY brasil_saas.bc_est_inventario_item.id;


--
-- Name: bc_est_lote; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_lote OWNER TO sa;

--
-- Name: TABLE bc_est_lote; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_est_lote IS 'Lotes com validade e rastreabilidade';


--
-- Name: bc_est_lote_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_lote_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_lote_id_seq OWNER TO sa;

--
-- Name: bc_est_lote_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_lote_id_seq OWNED BY brasil_saas.bc_est_lote.id;


--
-- Name: bc_est_movimentacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_movimentacao OWNER TO sa;

--
-- Name: TABLE bc_est_movimentacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_est_movimentacao IS 'Movimentações de estoque';


--
-- Name: COLUMN bc_est_movimentacao.tipo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_est_movimentacao.tipo IS 'ENTRADA, SAIDA ou AJUSTE';


--
-- Name: COLUMN bc_est_movimentacao.origem; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_est_movimentacao.origem IS 'VENDA, COMPRA ou MANUAL';


--
-- Name: bc_est_movimentacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_movimentacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_movimentacao_id_seq OWNER TO sa;

--
-- Name: bc_est_movimentacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_movimentacao_id_seq OWNED BY brasil_saas.bc_est_movimentacao.id;


--
-- Name: bc_est_reserva; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_reserva OWNER TO sa;

--
-- Name: TABLE bc_est_reserva; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_est_reserva IS 'Reservas de estoque para operacoes comerciais';


--
-- Name: COLUMN bc_est_reserva.lote_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_est_reserva.lote_id IS 'Lote fisico reservado; permite rastreabilidade e separacao por FEFO';


--
-- Name: bc_est_reserva_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_reserva_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_reserva_id_seq OWNER TO sa;

--
-- Name: bc_est_reserva_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_reserva_id_seq OWNED BY brasil_saas.bc_est_reserva.id;


--
-- Name: bc_est_saldo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_saldo OWNER TO sa;

--
-- Name: TABLE bc_est_saldo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_est_saldo IS 'Saldo de estoque por produto e empresa';


--
-- Name: bc_est_saldo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_saldo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_saldo_id_seq OWNER TO sa;

--
-- Name: bc_est_saldo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_saldo_id_seq OWNED BY brasil_saas.bc_est_saldo.id;


--
-- Name: bc_est_serie; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_serie OWNER TO sa;

--
-- Name: TABLE bc_est_serie; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_est_serie IS 'Numeros de serie e rastreabilidade unitaria';


--
-- Name: bc_est_serie_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_serie_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_serie_id_seq OWNER TO sa;

--
-- Name: bc_est_serie_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_serie_id_seq OWNED BY brasil_saas.bc_est_serie.id;


--
-- Name: bc_est_transferencia; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_transferencia OWNER TO sa;

--
-- Name: bc_est_transferencia_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_transferencia_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_transferencia_id_seq OWNER TO sa;

--
-- Name: bc_est_transferencia_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_transferencia_id_seq OWNED BY brasil_saas.bc_est_transferencia.id;


--
-- Name: bc_est_transferencia_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_est_transferencia_item OWNER TO sa;

--
-- Name: bc_est_transferencia_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_est_transferencia_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_est_transferencia_item_id_seq OWNER TO sa;

--
-- Name: bc_est_transferencia_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_est_transferencia_item_id_seq OWNED BY brasil_saas.bc_est_transferencia_item.id;


--
-- Name: bc_fin_analise_rentabilidade; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_analise_rentabilidade OWNER TO sa;

--
-- Name: bc_fin_analise_rentabilidade_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_analise_rentabilidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_analise_rentabilidade_id_seq OWNER TO sa;

--
-- Name: bc_fin_analise_rentabilidade_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_analise_rentabilidade_id_seq OWNED BY brasil_saas.bc_fin_analise_rentabilidade.id;


--
-- Name: bc_fin_aplicacao_financeira; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_aplicacao_financeira OWNER TO sa;

--
-- Name: bc_fin_aplicacao_financeira_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_aplicacao_financeira_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_aplicacao_financeira_id_seq OWNER TO sa;

--
-- Name: bc_fin_aplicacao_financeira_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_aplicacao_financeira_id_seq OWNED BY brasil_saas.bc_fin_aplicacao_financeira.id;


--
-- Name: bc_fin_aprovacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_aprovacao OWNER TO sa;

--
-- Name: COLUMN bc_fin_aprovacao.status; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.status IS 'PENDENTE | APROVADO | REJEITADO | CANCELADO';


--
-- Name: COLUMN bc_fin_aprovacao.tipo_documento; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.tipo_documento IS 'Domínio do documento aprovado: TITULO, COMPRA, VENDA etc.';


--
-- Name: COLUMN bc_fin_aprovacao.documento_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.documento_id IS 'Id genérico do documento no domínio (para workflow transversal).';


--
-- Name: COLUMN bc_fin_aprovacao.nivel; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.nivel IS 'Nível sequencial dentro do fluxo (aprovação multinível).';


--
-- Name: COLUMN bc_fin_aprovacao.numero_documento; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_aprovacao.numero_documento IS 'Número do documento aprovado (snapshot no momento da solicitação).';


--
-- Name: bc_fin_aprovacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_aprovacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_aprovacao_id_seq OWNER TO sa;

--
-- Name: bc_fin_aprovacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_aprovacao_id_seq OWNED BY brasil_saas.bc_fin_aprovacao.id;


--
-- Name: bc_fin_baixa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_baixa OWNER TO sa;

--
-- Name: TABLE bc_fin_baixa; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_baixa IS 'Baixas financeiras (pagamentos e recebimentos)';


--
-- Name: bc_fin_baixa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_baixa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_baixa_id_seq OWNER TO sa;

--
-- Name: bc_fin_baixa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_baixa_id_seq OWNED BY brasil_saas.bc_fin_baixa.id;


--
-- Name: bc_fin_boleto; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_boleto OWNER TO sa;

--
-- Name: TABLE bc_fin_boleto; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_boleto IS 'Boleto emitido via boleto_cnab_api. documento_id aponta para a colecao "documentos" do Mongo.';


--
-- Name: COLUMN bc_fin_boleto.documento_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_boleto.documento_id IS 'Id do documento no Mongo (colecao "documentos"); nao e caminho de arquivo.';


--
-- Name: bc_fin_boleto_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_boleto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_boleto_id_seq OWNER TO sa;

--
-- Name: bc_fin_boleto_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_boleto_id_seq OWNED BY brasil_saas.bc_fin_boleto.id;


--
-- Name: bc_fin_caixa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_caixa OWNER TO sa;

--
-- Name: TABLE bc_fin_caixa; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_caixa IS 'Caixas do modulo financeiro. Tela /financeiro/caixa.';


--
-- Name: COLUMN bc_fin_caixa.saldo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_caixa.saldo IS 'Saldo atual. Mantido aqui para consulta rapida; a origem do movimento e o extrato.';


--
-- Name: bc_fin_caixa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_caixa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_caixa_id_seq OWNER TO sa;

--
-- Name: bc_fin_caixa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_caixa_id_seq OWNED BY brasil_saas.bc_fin_caixa.id;


--
-- Name: bc_fin_caixa_movimento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fin_caixa_movimento (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    caixa_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    valor numeric(15,2) NOT NULL,
    saldo_anterior numeric(15,2) NOT NULL,
    saldo_posterior numeric(15,2) NOT NULL,
    observacao character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    deleted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    uuid uuid DEFAULT gen_random_uuid(),
    CONSTRAINT ck_caixa_mov_tipo CHECK (((tipo)::text = ANY ((ARRAY['SANGRIA'::character varying, 'SUPRIMENTO'::character varying])::text[]))),
    CONSTRAINT ck_caixa_mov_valor CHECK ((valor > (0)::numeric))
);


ALTER TABLE brasil_saas.bc_fin_caixa_movimento OWNER TO sa;

--
-- Name: bc_fin_caixa_movimento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_caixa_movimento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_caixa_movimento_id_seq OWNER TO sa;

--
-- Name: bc_fin_caixa_movimento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_caixa_movimento_id_seq OWNED BY brasil_saas.bc_fin_caixa_movimento.id;


--
-- Name: bc_fin_centro_custo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_centro_custo OWNER TO sa;

--
-- Name: TABLE bc_fin_centro_custo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_centro_custo IS 'Centros de custo para rateio e análise';


--
-- Name: bc_fin_centro_custo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_centro_custo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_centro_custo_id_seq OWNER TO sa;

--
-- Name: bc_fin_centro_custo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_centro_custo_id_seq OWNED BY brasil_saas.bc_fin_centro_custo.id;


--
-- Name: bc_fin_cobranca_acao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fin_cobranca_acao (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    titulo_id bigint NOT NULL,
    pessoa_id bigint,
    nivel integer DEFAULT 1 NOT NULL,
    tipo character varying(30) NOT NULL,
    observacao character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    deleted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    uuid uuid DEFAULT gen_random_uuid(),
    CONSTRAINT ck_cob_acao_nivel CHECK (((nivel >= 1) AND (nivel <= 5))),
    CONSTRAINT ck_cob_acao_tipo CHECK (((tipo)::text = ANY ((ARRAY['LEMBRETE'::character varying, 'AVISO'::character varying, 'NEGATIVACAO'::character varying, 'LIGACAO'::character varying, 'EMAIL'::character varying, 'WHATSAPP'::character varying, 'OUTRO'::character varying])::text[])))
);


ALTER TABLE brasil_saas.bc_fin_cobranca_acao OWNER TO sa;

--
-- Name: bc_fin_cobranca_acao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_cobranca_acao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_cobranca_acao_id_seq OWNER TO sa;

--
-- Name: bc_fin_cobranca_acao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_cobranca_acao_id_seq OWNED BY brasil_saas.bc_fin_cobranca_acao.id;


--
-- Name: bc_fin_comissao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_comissao OWNER TO sa;

--
-- Name: bc_fin_comissao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_fin_comissao ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fin_comissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_fin_conciliacao_bancaria; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_conciliacao_bancaria OWNER TO sa;

--
-- Name: bc_fin_conciliacao_bancaria_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_conciliacao_bancaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_conciliacao_bancaria_id_seq OWNER TO sa;

--
-- Name: bc_fin_conciliacao_bancaria_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_conciliacao_bancaria_id_seq OWNED BY brasil_saas.bc_fin_conciliacao_bancaria.id;


--
-- Name: bc_fin_conciliacao_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_conciliacao_item OWNER TO sa;

--
-- Name: bc_fin_conciliacao_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_conciliacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_conciliacao_item_id_seq OWNER TO sa;

--
-- Name: bc_fin_conciliacao_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_conciliacao_item_id_seq OWNED BY brasil_saas.bc_fin_conciliacao_item.id;


--
-- Name: bc_fin_condicao_pagamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_condicao_pagamento OWNER TO sa;

--
-- Name: TABLE bc_fin_condicao_pagamento; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_condicao_pagamento IS 'Condições de pagamento (prazos)';


--
-- Name: bc_fin_condicao_pagamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_condicao_pagamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_condicao_pagamento_id_seq OWNER TO sa;

--
-- Name: bc_fin_condicao_pagamento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_condicao_pagamento_id_seq OWNED BY brasil_saas.bc_fin_condicao_pagamento.id;


--
-- Name: bc_fin_consolidacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fin_consolidacao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    competencia date NOT NULL,
    grupo character varying(100) DEFAULT 'PADRAO'::character varying NOT NULL,
    status character varying(25) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    eliminacoes numeric(18,2) DEFAULT 0 NOT NULL,
    ajustes numeric(18,2) DEFAULT 0 NOT NULL,
    cambio_medio numeric(18,8),
    cambio_fechamento numeric(18,8),
    valor_consolidado numeric(18,2) DEFAULT 0 NOT NULL,
    consolidado_em timestamp without time zone,
    consolidado_por bigint,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);


ALTER TABLE brasil_saas.bc_fin_consolidacao OWNER TO sa;

--
-- Name: bc_fin_consolidacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_fin_consolidacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fin_consolidacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_fin_conta_bancaria; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_conta_bancaria OWNER TO sa;

--
-- Name: TABLE bc_fin_conta_bancaria; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_conta_bancaria IS 'Contas bancárias da empresa';


--
-- Name: bc_fin_conta_bancaria_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_conta_bancaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_conta_bancaria_id_seq OWNER TO sa;

--
-- Name: bc_fin_conta_bancaria_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_conta_bancaria_id_seq OWNED BY brasil_saas.bc_fin_conta_bancaria.id;


--
-- Name: bc_fin_emprestimo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_emprestimo OWNER TO sa;

--
-- Name: bc_fin_emprestimo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_emprestimo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_emprestimo_id_seq OWNER TO sa;

--
-- Name: bc_fin_emprestimo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_emprestimo_id_seq OWNED BY brasil_saas.bc_fin_emprestimo.id;


--
-- Name: bc_fin_extrato; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_extrato OWNER TO sa;

--
-- Name: TABLE bc_fin_extrato; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_extrato IS 'Extrato bancário importado ou manual';


--
-- Name: bc_fin_extrato_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_extrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_extrato_id_seq OWNER TO sa;

--
-- Name: bc_fin_extrato_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_extrato_id_seq OWNED BY brasil_saas.bc_fin_extrato.id;


--
-- Name: bc_fin_fluxo_aprovacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_fluxo_aprovacao OWNER TO sa;

--
-- Name: bc_fin_fluxo_aprovacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_fluxo_aprovacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_fluxo_aprovacao_id_seq OWNER TO sa;

--
-- Name: bc_fin_fluxo_aprovacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_fluxo_aprovacao_id_seq OWNED BY brasil_saas.bc_fin_fluxo_aprovacao.id;


--
-- Name: bc_fin_integracao_bancaria; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_integracao_bancaria OWNER TO sa;

--
-- Name: bc_fin_integracao_bancaria_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_integracao_bancaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_integracao_bancaria_id_seq OWNER TO sa;

--
-- Name: bc_fin_integracao_bancaria_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_integracao_bancaria_id_seq OWNED BY brasil_saas.bc_fin_integracao_bancaria.id;


--
-- Name: bc_fin_intercompany; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fin_intercompany (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    empresa_parceira_id bigint NOT NULL,
    numero character varying(80) NOT NULL,
    tipo character varying(30) DEFAULT 'LANCAMENTO'::character varying NOT NULL,
    data_documento date NOT NULL,
    competencia date,
    descricao character varying(500) NOT NULL,
    valor numeric(18,2) DEFAULT 0 NOT NULL,
    moeda character(3) DEFAULT 'BRL'::bpchar NOT NULL,
    status character varying(25) DEFAULT 'ABERTO'::character varying NOT NULL,
    reconciliado boolean DEFAULT false NOT NULL,
    documento_origem_tipo character varying(50),
    documento_origem_id bigint,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone,
    contrapartida_id bigint,
    diferenca numeric(18,2),
    reconciliado_em timestamp without time zone
);


ALTER TABLE brasil_saas.bc_fin_intercompany OWNER TO sa;

--
-- Name: bc_fin_intercompany_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_fin_intercompany ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fin_intercompany_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_fin_lancamento_contabil; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_lancamento_contabil OWNER TO sa;

--
-- Name: TABLE bc_fin_lancamento_contabil; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_lancamento_contabil IS 'Lançamentos contábeis (cabeçalho)';


--
-- Name: bc_fin_lancamento_contabil_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_lancamento_contabil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_lancamento_contabil_id_seq OWNER TO sa;

--
-- Name: bc_fin_lancamento_contabil_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_lancamento_contabil_id_seq OWNED BY brasil_saas.bc_fin_lancamento_contabil.id;


--
-- Name: bc_fin_lancamento_partida; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_lancamento_partida OWNER TO sa;

--
-- Name: TABLE bc_fin_lancamento_partida; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_lancamento_partida IS 'Partidas dobradas (débito e crédito)';


--
-- Name: bc_fin_lancamento_partida_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_lancamento_partida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_lancamento_partida_id_seq OWNER TO sa;

--
-- Name: bc_fin_lancamento_partida_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_lancamento_partida_id_seq OWNED BY brasil_saas.bc_fin_lancamento_partida.id;


--
-- Name: bc_fin_orcamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_orcamento OWNER TO sa;

--
-- Name: bc_fin_orcamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_orcamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_orcamento_id_seq OWNER TO sa;

--
-- Name: bc_fin_orcamento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_orcamento_id_seq OWNED BY brasil_saas.bc_fin_orcamento.id;


--
-- Name: bc_fin_orcamento_realizado; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_orcamento_realizado OWNER TO sa;

--
-- Name: bc_fin_orcamento_realizado_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_orcamento_realizado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_orcamento_realizado_id_seq OWNER TO sa;

--
-- Name: bc_fin_orcamento_realizado_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_orcamento_realizado_id_seq OWNED BY brasil_saas.bc_fin_orcamento_realizado.id;


--
-- Name: bc_fin_periodo_contabil; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_periodo_contabil OWNER TO sa;

--
-- Name: TABLE bc_fin_periodo_contabil; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_periodo_contabil IS 'Períodos contábeis para fechamento';


--
-- Name: bc_fin_periodo_contabil_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_periodo_contabil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_periodo_contabil_id_seq OWNER TO sa;

--
-- Name: bc_fin_periodo_contabil_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_periodo_contabil_id_seq OWNED BY brasil_saas.bc_fin_periodo_contabil.id;


--
-- Name: bc_fin_plano_contas; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_plano_contas OWNER TO sa;

--
-- Name: TABLE bc_fin_plano_contas; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_plano_contas IS 'Plano de contas contábil';


--
-- Name: bc_fin_plano_contas_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_plano_contas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_plano_contas_id_seq OWNER TO sa;

--
-- Name: bc_fin_plano_contas_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_plano_contas_id_seq OWNED BY brasil_saas.bc_fin_plano_contas.id;


--
-- Name: bc_fin_projecao_fluxo_caixa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_projecao_fluxo_caixa OWNER TO sa;

--
-- Name: bc_fin_projecao_fluxo_caixa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq OWNER TO sa;

--
-- Name: bc_fin_projecao_fluxo_caixa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq OWNED BY brasil_saas.bc_fin_projecao_fluxo_caixa.id;


--
-- Name: bc_fin_promessa_pagamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fin_promessa_pagamento (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    titulo_id bigint NOT NULL,
    pessoa_id bigint,
    valor_prometido numeric(15,2) NOT NULL,
    data_prometida date NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    observacao character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_at timestamp without time zone,
    deleted_at timestamp(6) without time zone,
    uuid uuid DEFAULT gen_random_uuid(),
    CONSTRAINT ck_promessa_status CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'CUMPRIDA'::character varying, 'QUEBRADA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT ck_promessa_valor CHECK ((valor_prometido > (0)::numeric))
);


ALTER TABLE brasil_saas.bc_fin_promessa_pagamento OWNER TO sa;

--
-- Name: bc_fin_promessa_pagamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_promessa_pagamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_promessa_pagamento_id_seq OWNER TO sa;

--
-- Name: bc_fin_promessa_pagamento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_promessa_pagamento_id_seq OWNED BY brasil_saas.bc_fin_promessa_pagamento.id;


--
-- Name: bc_fin_provisao_pdd; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_provisao_pdd OWNER TO sa;

--
-- Name: bc_fin_provisao_pdd_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_provisao_pdd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_provisao_pdd_id_seq OWNER TO sa;

--
-- Name: bc_fin_provisao_pdd_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_provisao_pdd_id_seq OWNED BY brasil_saas.bc_fin_provisao_pdd.id;


--
-- Name: bc_fin_remessa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_remessa OWNER TO sa;

--
-- Name: TABLE bc_fin_remessa; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_remessa IS 'Remessa CNAB gerada pelo boleto_cnab_api e enviada ao banco.';


--
-- Name: bc_fin_remessa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_remessa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_remessa_id_seq OWNER TO sa;

--
-- Name: bc_fin_remessa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_remessa_id_seq OWNED BY brasil_saas.bc_fin_remessa.id;


--
-- Name: bc_fin_remessa_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_remessa_item OWNER TO sa;

--
-- Name: bc_fin_remessa_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_remessa_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_remessa_item_id_seq OWNER TO sa;

--
-- Name: bc_fin_remessa_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_remessa_item_id_seq OWNED BY brasil_saas.bc_fin_remessa_item.id;


--
-- Name: bc_fin_renegociacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_renegociacao OWNER TO sa;

--
-- Name: bc_fin_renegociacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_renegociacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_renegociacao_id_seq OWNER TO sa;

--
-- Name: bc_fin_renegociacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_renegociacao_id_seq OWNED BY brasil_saas.bc_fin_renegociacao.id;


--
-- Name: bc_fin_retorno_bancario; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_retorno_bancario OWNER TO sa;

--
-- Name: bc_fin_retorno_bancario_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_retorno_bancario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_retorno_bancario_id_seq OWNER TO sa;

--
-- Name: bc_fin_retorno_bancario_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_retorno_bancario_id_seq OWNED BY brasil_saas.bc_fin_retorno_bancario.id;


--
-- Name: bc_fin_retorno_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_retorno_item OWNER TO sa;

--
-- Name: bc_fin_retorno_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_retorno_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_retorno_item_id_seq OWNER TO sa;

--
-- Name: bc_fin_retorno_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_retorno_item_id_seq OWNED BY brasil_saas.bc_fin_retorno_item.id;


--
-- Name: bc_fin_stripe_customer; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_stripe_customer OWNER TO sa;

--
-- Name: bc_fin_stripe_customer_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_stripe_customer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_stripe_customer_id_seq OWNER TO sa;

--
-- Name: bc_fin_stripe_customer_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_stripe_customer_id_seq OWNED BY brasil_saas.bc_fin_stripe_customer.id;


--
-- Name: bc_fin_stripe_payment; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_stripe_payment OWNER TO sa;

--
-- Name: bc_fin_stripe_payment_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_stripe_payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_stripe_payment_id_seq OWNER TO sa;

--
-- Name: bc_fin_stripe_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_stripe_payment_id_seq OWNED BY brasil_saas.bc_fin_stripe_payment.id;


--
-- Name: bc_fin_stripe_webhook_event; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_stripe_webhook_event OWNER TO sa;

--
-- Name: bc_fin_stripe_webhook_event_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_stripe_webhook_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_stripe_webhook_event_id_seq OWNER TO sa;

--
-- Name: bc_fin_stripe_webhook_event_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_stripe_webhook_event_id_seq OWNED BY brasil_saas.bc_fin_stripe_webhook_event.id;


--
-- Name: bc_fin_tipo_pagamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_tipo_pagamento OWNER TO sa;

--
-- Name: TABLE bc_fin_tipo_pagamento; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_tipo_pagamento IS 'Tipos/Meios de pagamento';


--
-- Name: bc_fin_tipo_pagamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_tipo_pagamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_tipo_pagamento_id_seq OWNER TO sa;

--
-- Name: bc_fin_tipo_pagamento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_tipo_pagamento_id_seq OWNED BY brasil_saas.bc_fin_tipo_pagamento.id;


--
-- Name: bc_fin_titulo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_titulo OWNER TO sa;

--
-- Name: TABLE bc_fin_titulo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_titulo IS 'Títulos a pagar e a receber';


--
-- Name: COLUMN bc_fin_titulo.status; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fin_titulo.status IS 'ABERTO | PENDENTE_APROVACAO | BAIXADO | CANCELADO (demais estados por regra de negócio)';


--
-- Name: bc_fin_titulo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_titulo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_titulo_id_seq OWNER TO sa;

--
-- Name: bc_fin_titulo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_titulo_id_seq OWNED BY brasil_saas.bc_fin_titulo.id;


--
-- Name: bc_fin_titulo_parcela; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fin_titulo_parcela OWNER TO sa;

--
-- Name: TABLE bc_fin_titulo_parcela; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fin_titulo_parcela IS 'Parcelas dos títulos';


--
-- Name: bc_fin_titulo_parcela_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fin_titulo_parcela_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fin_titulo_parcela_id_seq OWNER TO sa;

--
-- Name: bc_fin_titulo_parcela_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fin_titulo_parcela_id_seq OWNED BY brasil_saas.bc_fin_titulo_parcela.id;


--
-- Name: bc_fis_apuracao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_apuracao OWNER TO sa;

--
-- Name: TABLE bc_fis_apuracao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_apuracao IS 'Apurações mensais de impostos';


--
-- Name: bc_fis_apuracao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_apuracao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_apuracao_id_seq OWNER TO sa;

--
-- Name: bc_fis_apuracao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_apuracao_id_seq OWNED BY brasil_saas.bc_fis_apuracao.id;


--
-- Name: bc_fis_certificado_digital; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_certificado_digital OWNER TO sa;

--
-- Name: TABLE bc_fis_certificado_digital; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_certificado_digital IS 'Certificados digitais (A1/A3)';


--
-- Name: COLUMN bc_fis_certificado_digital.arquivo_url; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_certificado_digital.arquivo_url IS 'Referência MinIO — nunca versionar o arquivo em si';


--
-- Name: bc_fis_certificado_digital_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_certificado_digital_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_certificado_digital_id_seq OWNER TO sa;

--
-- Name: bc_fis_certificado_digital_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_certificado_digital_id_seq OWNED BY brasil_saas.bc_fis_certificado_digital.id;


--
-- Name: bc_fis_cest; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_cest (
    id bigint NOT NULL,
    codigo character varying(7) NOT NULL,
    ncm character varying(8),
    descricao text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_fis_cest OWNER TO sa;

--
-- Name: TABLE bc_fis_cest; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_cest IS 'CEST — Código Especificador da Substituição Tributária (oficial)';


--
-- Name: bc_fis_cest_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_cest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_cest_id_seq OWNER TO sa;

--
-- Name: bc_fis_cest_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_cest_id_seq OWNED BY brasil_saas.bc_fis_cest.id;


--
-- Name: bc_fis_cfop; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_cfop (
    id bigint NOT NULL,
    codigo character varying(4) NOT NULL,
    descricao text NOT NULL,
    tipo character varying(20),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_fis_cfop OWNER TO sa;

--
-- Name: TABLE bc_fis_cfop; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_cfop IS 'CFOP — Código Fiscal de Operações e Prestações (oficial)';


--
-- Name: bc_fis_cfop_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_cfop_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_cfop_id_seq OWNER TO sa;

--
-- Name: bc_fis_cfop_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_cfop_id_seq OWNED BY brasil_saas.bc_fis_cfop.id;


--
-- Name: bc_fis_cnae_servico; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_cnae_servico (
    id bigint NOT NULL,
    codigo character varying(10) NOT NULL,
    descricao text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    lc116_codigo character varying(12)
);


ALTER TABLE brasil_saas.bc_fis_cnae_servico OWNER TO sa;

--
-- Name: TABLE bc_fis_cnae_servico; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_cnae_servico IS 'CNAE de serviços (oficial IBGE)';


--
-- Name: bc_fis_cnae_servico_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_cnae_servico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_cnae_servico_id_seq OWNER TO sa;

--
-- Name: bc_fis_cnae_servico_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_cnae_servico_id_seq OWNED BY brasil_saas.bc_fis_cnae_servico.id;


--
-- Name: bc_fis_cte; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_cte OWNER TO sa;

--
-- Name: TABLE bc_fis_cte; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_cte IS 'Conhecimentos de transporte eletrônicos (modelo 57)';


--
-- Name: COLUMN bc_fis_cte.tipo_operacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_cte.tipo_operacao IS 'E=entrada, S=saída';


--
-- Name: bc_fis_cte_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_cte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_cte_id_seq OWNER TO sa;

--
-- Name: bc_fis_cte_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_cte_id_seq OWNED BY brasil_saas.bc_fis_cte.id;


--
-- Name: bc_fis_cte_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_cte_item (
    id bigint NOT NULL,
    cte_id bigint NOT NULL,
    descricao text,
    quantidade numeric(15,4),
    valor numeric(15,2),
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_fis_cte_item OWNER TO sa;

--
-- Name: TABLE bc_fis_cte_item; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_cte_item IS 'Cargas/itens do CT-e';


--
-- Name: bc_fis_cte_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_cte_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_cte_item_id_seq OWNER TO sa;

--
-- Name: bc_fis_cte_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_cte_item_id_seq OWNED BY brasil_saas.bc_fis_cte_item.id;


--
-- Name: bc_fis_ecd; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_ecd OWNER TO sa;

--
-- Name: TABLE bc_fis_ecd; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_ecd IS 'ECD — Escrituração Contábil Digital';


--
-- Name: bc_fis_ecd_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_ecd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_ecd_id_seq OWNER TO sa;

--
-- Name: bc_fis_ecd_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_ecd_id_seq OWNED BY brasil_saas.bc_fis_ecd.id;


--
-- Name: bc_fis_ecf; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_ecf OWNER TO sa;

--
-- Name: TABLE bc_fis_ecf; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_ecf IS 'ECF — Escrituração Contábil Fiscal';


--
-- Name: bc_fis_ecf_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_ecf_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_ecf_id_seq OWNER TO sa;

--
-- Name: bc_fis_ecf_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_ecf_id_seq OWNED BY brasil_saas.bc_fis_ecf.id;


--
-- Name: bc_fis_esocial; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_esocial OWNER TO sa;

--
-- Name: TABLE bc_fis_esocial; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_esocial IS 'Eventos de eSocial';


--
-- Name: bc_fis_esocial_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_esocial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_esocial_id_seq OWNER TO sa;

--
-- Name: bc_fis_esocial_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_esocial_id_seq OWNED BY brasil_saas.bc_fis_esocial.id;


--
-- Name: bc_fis_imposto; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_imposto OWNER TO sa;

--
-- Name: TABLE bc_fis_imposto; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_imposto IS 'Impostos configurados por empresa';


--
-- Name: bc_fis_imposto_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_imposto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_imposto_id_seq OWNER TO sa;

--
-- Name: bc_fis_imposto_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_imposto_id_seq OWNED BY brasil_saas.bc_fis_imposto.id;


--
-- Name: bc_fis_issqn; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_issqn OWNER TO sa;

--
-- Name: TABLE bc_fis_issqn; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_issqn IS 'Códigos de tributação municipal de ISSQN';


--
-- Name: bc_fis_issqn_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_issqn_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_issqn_id_seq OWNER TO sa;

--
-- Name: bc_fis_issqn_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_issqn_id_seq OWNED BY brasil_saas.bc_fis_issqn.id;


--
-- Name: bc_fis_manifestacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_manifestacao OWNER TO sa;

--
-- Name: TABLE bc_fis_manifestacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_manifestacao IS 'Manifestação do destinatário';


--
-- Name: bc_fis_manifestacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_manifestacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_manifestacao_id_seq OWNER TO sa;

--
-- Name: bc_fis_manifestacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_manifestacao_id_seq OWNED BY brasil_saas.bc_fis_manifestacao.id;


--
-- Name: bc_fis_mdfe; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_mdfe OWNER TO sa;

--
-- Name: TABLE bc_fis_mdfe; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_mdfe IS 'Manifestos eletrônicos de documentos fiscais (modelo 58)';


--
-- Name: bc_fis_mdfe_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_mdfe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_mdfe_id_seq OWNER TO sa;

--
-- Name: bc_fis_mdfe_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_mdfe_id_seq OWNED BY brasil_saas.bc_fis_mdfe.id;


--
-- Name: bc_fis_nbs; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_nbs (
    id bigint NOT NULL,
    codigo character varying(12) NOT NULL,
    descricao text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_fis_nbs OWNER TO sa;

--
-- Name: TABLE bc_fis_nbs; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nbs IS 'NBS — Nomenclatura Brasileira de Serviços (oficial)';


--
-- Name: bc_fis_nbs_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nbs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nbs_id_seq OWNER TO sa;

--
-- Name: bc_fis_nbs_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nbs_id_seq OWNED BY brasil_saas.bc_fis_nbs.id;


--
-- Name: bc_fis_ncm; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_ncm (
    id bigint NOT NULL,
    codigo character varying(8) NOT NULL,
    descricao text NOT NULL,
    aliquota_ipi numeric(7,4),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_fis_ncm OWNER TO sa;

--
-- Name: TABLE bc_fis_ncm; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_ncm IS 'NCM — Nomenclatura Comum do Mercosul (oficial)';


--
-- Name: bc_fis_ncm_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_ncm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_ncm_id_seq OWNER TO sa;

--
-- Name: bc_fis_ncm_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_ncm_id_seq OWNED BY brasil_saas.bc_fis_ncm.id;


--
-- Name: bc_fis_nfce; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_nfce OWNER TO sa;

--
-- Name: TABLE bc_fis_nfce; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfce IS 'Notas fiscais de consumidor eletrônicas (modelo 65)';


--
-- Name: bc_fis_nfce_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfce_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfce_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfce_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfce_id_seq OWNED BY brasil_saas.bc_fis_nfce.id;


--
-- Name: bc_fis_nfce_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_nfce_item (
    id bigint NOT NULL,
    nfce_id bigint NOT NULL,
    produto_id bigint,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,4) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_fis_nfce_item OWNER TO sa;

--
-- Name: TABLE bc_fis_nfce_item; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfce_item IS 'Itens da NFC-e';


--
-- Name: bc_fis_nfce_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfce_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfce_item_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfce_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfce_item_id_seq OWNED BY brasil_saas.bc_fis_nfce_item.id;


--
-- Name: bc_fis_nfe; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_nfe OWNER TO sa;

--
-- Name: TABLE bc_fis_nfe; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfe IS 'Notas fiscais eletrônicas (modelo 55)';


--
-- Name: COLUMN bc_fis_nfe.tipo_operacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfe.tipo_operacao IS 'E=entrada, S=saída';


--
-- Name: COLUMN bc_fis_nfe.documento_origem_tipo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfe.documento_origem_tipo IS 'VENDA, COMPRA, SERVICO ou OUTRO';


--
-- Name: bc_fis_nfe_evento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_nfe_evento OWNER TO sa;

--
-- Name: TABLE bc_fis_nfe_evento; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfe_evento IS 'Eventos de NF-e (cancelamento, carta de correção...)';


--
-- Name: bc_fis_nfe_evento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfe_evento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfe_evento_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfe_evento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfe_evento_id_seq OWNED BY brasil_saas.bc_fis_nfe_evento.id;


--
-- Name: bc_fis_nfe_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfe_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfe_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfe_id_seq OWNED BY brasil_saas.bc_fis_nfe.id;


--
-- Name: bc_fis_nfe_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_nfe_item OWNER TO sa;

--
-- Name: TABLE bc_fis_nfe_item; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfe_item IS 'Itens da NF-e';


--
-- Name: COLUMN bc_fis_nfe_item.codigo_produto; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.codigo_produto IS 'cProd: codigo do produto no fornecedor. Nao e o codigo interno do ERP.';


--
-- Name: COLUMN bc_fis_nfe_item.codigo_barras; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.codigo_barras IS 'cEAN do item, so quando o emissor escreveu digitos. Texto como "SEM GTIN" vira NULL.';


--
-- Name: COLUMN bc_fis_nfe_item.uuid; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.uuid IS 'Chave publica do item. NULL so em linha criada antes da V103.';


--
-- Name: COLUMN bc_fis_nfe_item.deleted_at; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfe_item.deleted_at IS 'Item corrigido nao se apaga: a nota e documento fiscal e o historico precisa ficar.';


--
-- Name: bc_fis_nfe_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfe_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfe_item_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfe_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfe_item_id_seq OWNED BY brasil_saas.bc_fis_nfe_item.id;


--
-- Name: bc_fis_nfse; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_nfse OWNER TO sa;

--
-- Name: TABLE bc_fis_nfse; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfse IS 'Notas fiscais de serviço eletrônicas';


--
-- Name: COLUMN bc_fis_nfse.tipo_operacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.tipo_operacao IS 'E=entrada, S=saída';


--
-- Name: COLUMN bc_fis_nfse.xml_documento_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.xml_documento_id IS 'Id do documento no MongoDB (tipoEntidade nfse_xml). Guarda 5 anos.';


--
-- Name: COLUMN bc_fis_nfse.pdf_documento_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.pdf_documento_id IS 'Id do documento no MongoDB (tipoEntidade nfse_pdf). Expira em 60 dias.';


--
-- Name: COLUMN bc_fis_nfse.chave_nota_nacional; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse.chave_nota_nacional IS 'Chave da nota nacional devolvida pela prefeitura (44 digitos).';


--
-- Name: bc_fis_nfse_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfse_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfse_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfse_id_seq OWNED BY brasil_saas.bc_fis_nfse.id;


--
-- Name: bc_fis_nfse_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_nfse_item OWNER TO sa;

--
-- Name: TABLE bc_fis_nfse_item; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfse_item IS 'Itens da NFS-e';


--
-- Name: bc_fis_nfse_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfse_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfse_item_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfse_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfse_item_id_seq OWNED BY brasil_saas.bc_fis_nfse_item.id;


--
-- Name: bc_fis_nfse_retorno; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_nfse_retorno OWNER TO sa;

--
-- Name: TABLE bc_fis_nfse_retorno; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_nfse_retorno IS 'Uma linha por chamada a prefeitura: emissao, cancelamento, consulta. O JSON bruto da resposta fica no Mongo (documento_id); o que da para consultar fica aqui. E o registro que sobrevive ao fechamento da tela de erro.';


--
-- Name: COLUMN bc_fis_nfse_retorno.sucesso; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.sucesso IS 'true aceitou; false recusou (com mensagem); NULL nao deu para saber — unico caso em que reemitir e perigoso.';


--
-- Name: COLUMN bc_fis_nfse_retorno.mensagem; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.mensagem IS 'Texto da prefeitura, literal. Sobrevive ao fechamento da tela.';


--
-- Name: COLUMN bc_fis_nfse_retorno.codigo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.codigo IS 'Codigo de erro da prefeitura, extraido da mensagem (ex.: 1001).';


--
-- Name: COLUMN bc_fis_nfse_retorno.documento_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_nfse_retorno.documento_id IS 'Id do documento no MongoDB (tipoEntidade nfse_retorno). Guarda o JSON bruto da resposta.';


--
-- Name: bc_fis_nfse_retorno_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_nfse_retorno_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_nfse_retorno_id_seq OWNER TO sa;

--
-- Name: bc_fis_nfse_retorno_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_nfse_retorno_id_seq OWNED BY brasil_saas.bc_fis_nfse_retorno.id;


--
-- Name: bc_fis_obrigacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_obrigacao OWNER TO sa;

--
-- Name: bc_fis_obrigacao_entrega; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_obrigacao_entrega OWNER TO sa;

--
-- Name: bc_fis_obrigacao_entrega_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_fis_obrigacao_entrega ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fis_obrigacao_entrega_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_fis_obrigacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_fis_obrigacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_fis_obrigacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_fis_palavra_chave; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_palavra_chave OWNER TO sa;

--
-- Name: TABLE bc_fis_palavra_chave; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_palavra_chave IS 'Índice de busca do cadastro fiscal. Nível 1: token da descrição oficial. Nível 2: sinônimo curado, com origem declarada.';


--
-- Name: bc_fis_palavra_chave_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_palavra_chave_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_palavra_chave_id_seq OWNER TO sa;

--
-- Name: bc_fis_palavra_chave_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_palavra_chave_id_seq OWNED BY brasil_saas.bc_fis_palavra_chave.id;


--
-- Name: bc_fis_regra_tributaria; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    origem_referencia character varying(180),
    mva numeric(7,4),
    aliquota_fcp numeric(7,4),
    aliquota_interna numeric(7,4),
    reducao_base_pct numeric(7,4)
);


ALTER TABLE brasil_saas.bc_fis_regra_tributaria OWNER TO sa;

--
-- Name: TABLE bc_fis_regra_tributaria; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_regra_tributaria IS 'Regras de tributação por NCM/CFOP/UF';


--
-- Name: COLUMN bc_fis_regra_tributaria.origem; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_regra_tributaria.origem IS 'Procedência da regra: identificador da fonte. NUNCA confiar linhas sem este valor.';


--
-- Name: COLUMN bc_fis_regra_tributaria.origem_referencia; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_regra_tributaria.origem_referencia IS 'Arquivo e tag de origem, para auditar a carga.';


--
-- Name: bc_fis_regra_tributaria_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_regra_tributaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_regra_tributaria_id_seq OWNER TO sa;

--
-- Name: bc_fis_regra_tributaria_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_regra_tributaria_id_seq OWNED BY brasil_saas.bc_fis_regra_tributaria.id;


--
-- Name: bc_fis_reinf; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    deleted_at timestamp without time zone,
    payload text,
    protocolo character varying(60),
    gerado_at timestamp without time zone,
    total_docs integer DEFAULT 0,
    valor_total numeric(15,2) DEFAULT 0
);


ALTER TABLE brasil_saas.bc_fis_reinf OWNER TO sa;

--
-- Name: TABLE bc_fis_reinf; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_reinf IS 'EFD-REINF';


--
-- Name: COLUMN bc_fis_reinf.payload; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_reinf.payload IS 'JSON do evento R-xxxx gerado (sem assinatura/transmissão)';


--
-- Name: COLUMN bc_fis_reinf.protocolo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_fis_reinf.protocolo IS 'Protocolo local ou da RFB quando houver transmissão';


--
-- Name: bc_fis_reinf_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_reinf_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_reinf_id_seq OWNER TO sa;

--
-- Name: bc_fis_reinf_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_reinf_id_seq OWNED BY brasil_saas.bc_fis_reinf.id;


--
-- Name: bc_fis_servico_lc116; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_fis_servico_lc116 (
    id bigint NOT NULL,
    codigo character varying(10) NOT NULL,
    descricao text NOT NULL,
    aliquota_min numeric(7,4),
    aliquota_max numeric(7,4),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_fis_servico_lc116 OWNER TO sa;

--
-- Name: TABLE bc_fis_servico_lc116; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_servico_lc116 IS 'Itens da Lista de Serviços da LC 116/2003 (oficial)';


--
-- Name: bc_fis_servico_lc116_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_servico_lc116_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_servico_lc116_id_seq OWNER TO sa;

--
-- Name: bc_fis_servico_lc116_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_servico_lc116_id_seq OWNED BY brasil_saas.bc_fis_servico_lc116.id;


--
-- Name: bc_fis_sped_contribuicoes; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_sped_contribuicoes OWNER TO sa;

--
-- Name: TABLE bc_fis_sped_contribuicoes; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_sped_contribuicoes IS 'Gerações de SPED Contribuições (PIS/COFINS)';


--
-- Name: bc_fis_sped_contribuicoes_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_sped_contribuicoes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_sped_contribuicoes_id_seq OWNER TO sa;

--
-- Name: bc_fis_sped_contribuicoes_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_sped_contribuicoes_id_seq OWNED BY brasil_saas.bc_fis_sped_contribuicoes.id;


--
-- Name: bc_fis_sped_fiscal; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_fis_sped_fiscal OWNER TO sa;

--
-- Name: TABLE bc_fis_sped_fiscal; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_fis_sped_fiscal IS 'Gerações de SPED Fiscal (ICMS/IPI)';


--
-- Name: bc_fis_sped_fiscal_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_fis_sped_fiscal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_fis_sped_fiscal_id_seq OWNER TO sa;

--
-- Name: bc_fis_sped_fiscal_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_fis_sped_fiscal_id_seq OWNED BY brasil_saas.bc_fis_sped_fiscal.id;


--
-- Name: bc_gov_controle; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_gov_controle (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(80) NOT NULL,
    processo character varying(100) NOT NULL,
    titulo character varying(200) NOT NULL,
    objetivo text,
    frequencia character varying(30) DEFAULT 'MENSAL'::character varying NOT NULL,
    tipo character varying(30) DEFAULT 'PREVENTIVO'::character varying NOT NULL,
    responsavel_id bigint,
    status character varying(25) DEFAULT 'ATIVO'::character varying NOT NULL,
    ultima_execucao date,
    proxima_execucao date,
    evidencias text,
    resultado character varying(30),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_gov_controle OWNER TO sa;

--
-- Name: bc_gov_controle_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_gov_controle ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_gov_controle_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_gov_risco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_gov_risco (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(80) NOT NULL,
    categoria character varying(60) NOT NULL,
    titulo character varying(200) NOT NULL,
    descricao text,
    probabilidade numeric(6,2) DEFAULT 0 NOT NULL,
    impacto numeric(6,2) DEFAULT 0 NOT NULL,
    score numeric(10,2) DEFAULT 0 NOT NULL,
    tratamento character varying(40) DEFAULT 'MITIGAR'::character varying,
    responsavel_id bigint,
    status character varying(25) DEFAULT 'ABERTO'::character varying NOT NULL,
    prazo date,
    risco_residual numeric(10,2),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_gov_risco OWNER TO sa;

--
-- Name: bc_gov_risco_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_gov_risco ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_gov_risco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_avaliacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_avaliacao (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    risco_id bigint NOT NULL,
    periodo character(7) NOT NULL,
    probabilidade numeric(8,2) NOT NULL,
    impacto numeric(8,2) NOT NULL,
    nivel numeric(12,2) NOT NULL,
    tendencia character varying(30),
    avaliador character varying(160),
    observacao character varying(1000),
    avaliado_em timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_grc_avaliacao OWNER TO sa;

--
-- Name: bc_grc_avaliacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_avaliacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_avaliacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_controle; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_controle (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    descricao character varying(500) NOT NULL,
    tipo character varying(40) DEFAULT 'PREVENTIVO'::character varying NOT NULL,
    frequencia character varying(40),
    responsavel character varying(160),
    status character varying(30) DEFAULT 'ATIVO'::character varying NOT NULL
);


ALTER TABLE brasil_saas.bc_grc_controle OWNER TO sa;

--
-- Name: bc_grc_controle_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_controle ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_controle_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_evidencia; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_evidencia (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    entidade_tipo character varying(60) NOT NULL,
    entidade_id bigint NOT NULL,
    nome character varying(200) NOT NULL,
    localizacao character varying(1000),
    validade date,
    hash_documento character varying(128),
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_grc_evidencia OWNER TO sa;

--
-- Name: bc_grc_evidencia_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_evidencia ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_evidencia_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_log; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_log (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    entidade_tipo character varying(60) NOT NULL,
    entidade_id bigint,
    acao character varying(60) NOT NULL,
    usuario character varying(160),
    detalhes character varying(2000),
    criado_em timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_grc_log OWNER TO sa;

--
-- Name: bc_grc_log_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_log ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_plano_acao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_plano_acao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    risco_id bigint NOT NULL,
    descricao character varying(1000) NOT NULL,
    responsavel character varying(160),
    prazo date,
    status character varying(30) DEFAULT 'ABERTO'::character varying NOT NULL,
    percentual_conclusao numeric(5,2) DEFAULT 0 NOT NULL,
    evidencia_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_grc_plano_acao OWNER TO sa;

--
-- Name: bc_grc_plano_acao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_plano_acao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_plano_acao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_risco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_risco (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    descricao character varying(500) NOT NULL,
    categoria character varying(80),
    probabilidade numeric(8,2) DEFAULT 0 NOT NULL,
    impacto numeric(8,2) DEFAULT 0 NOT NULL,
    nivel numeric(12,2) DEFAULT 0 NOT NULL,
    status character varying(30) DEFAULT 'ABERTO'::character varying NOT NULL,
    responsavel character varying(160),
    prazo date,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_grc_risco OWNER TO sa;

--
-- Name: bc_grc_risco_controle; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_risco_controle (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    risco_id bigint NOT NULL,
    controle_id bigint NOT NULL
);


ALTER TABLE brasil_saas.bc_grc_risco_controle OWNER TO sa;

--
-- Name: bc_grc_risco_controle_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_risco_controle ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_risco_controle_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_risco_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_risco ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_risco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_grc_teste_controle; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_grc_teste_controle (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    controle_id bigint NOT NULL,
    periodo character(7) NOT NULL,
    resultado character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
    observacao character varying(1000),
    testado_por character varying(160),
    testado_em timestamp without time zone
);


ALTER TABLE brasil_saas.bc_grc_teste_controle OWNER TO sa;

--
-- Name: bc_grc_teste_controle_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_grc_teste_controle ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_grc_teste_controle_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_hdp_chamado; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_hdp_chamado (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(30) NOT NULL,
    titulo character varying(200) NOT NULL,
    descricao text,
    prioridade character varying(20) DEFAULT 'MEDIA'::character varying NOT NULL,
    status character varying(30) DEFAULT 'ABERTO'::character varying NOT NULL,
    categoria character varying(60),
    solicitante character varying(150),
    responsavel character varying(150),
    cliente_id bigint,
    aberto_em timestamp without time zone DEFAULT now() NOT NULL,
    fechado_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);


ALTER TABLE brasil_saas.bc_hdp_chamado OWNER TO sa;

--
-- Name: bc_hdp_chamado_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_hdp_chamado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_hdp_chamado_id_seq OWNER TO sa;

--
-- Name: bc_hdp_chamado_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_hdp_chamado_id_seq OWNED BY brasil_saas.bc_hdp_chamado.id;


--
-- Name: bc_hdp_comentario; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_hdp_comentario (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    chamado_id bigint NOT NULL,
    autor character varying(150),
    texto text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);


ALTER TABLE brasil_saas.bc_hdp_comentario OWNER TO sa;

--
-- Name: bc_hdp_comentario_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_hdp_comentario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_hdp_comentario_id_seq OWNER TO sa;

--
-- Name: bc_hdp_comentario_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_hdp_comentario_id_seq OWNED BY brasil_saas.bc_hdp_comentario.id;


--
-- Name: bc_ia_analise_preditiva; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_analise_preditiva OWNER TO sa;

--
-- Name: bc_ia_analise_preditiva_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_analise_preditiva_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_analise_preditiva_id_seq OWNER TO sa;

--
-- Name: bc_ia_analise_preditiva_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_analise_preditiva_id_seq OWNED BY brasil_saas.bc_ia_analise_preditiva.id;


--
-- Name: bc_ia_assistente_auditoria; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_assistente_auditoria OWNER TO sa;

--
-- Name: TABLE bc_ia_assistente_auditoria; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ia_assistente_auditoria IS 'Auditoria do Assistente ERP: pergunta, contexto, modelo e tempo. Sem ela não há como conferir, depois, se a resposta veio do ERP ou do modelo.';


--
-- Name: COLUMN bc_ia_assistente_auditoria.usuario_nome; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_ia_assistente_auditoria.usuario_nome IS 'Quem perguntou, pelo nome. É o campo confiável: vale para AD, Postgres e Linux, e não depende de JOIN com bc_core_usuario.';


--
-- Name: COLUMN bc_ia_assistente_auditoria.identity_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_ia_assistente_auditoria.identity_id IS 'identityId do contrato oficial do Auth Service. Âncora estavel da identidade. NULL enquanto o IdentityClient não existir; username é rótulo, não chave.';


--
-- Name: bc_ia_assistente_auditoria_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_assistente_auditoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_assistente_auditoria_id_seq OWNER TO sa;

--
-- Name: bc_ia_assistente_auditoria_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_assistente_auditoria_id_seq OWNED BY brasil_saas.bc_ia_assistente_auditoria.id;


--
-- Name: bc_ia_chat_mensagem; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_chat_mensagem OWNER TO sa;

--
-- Name: bc_ia_chat_mensagem_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_chat_mensagem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_chat_mensagem_id_seq OWNER TO sa;

--
-- Name: bc_ia_chat_mensagem_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_chat_mensagem_id_seq OWNED BY brasil_saas.bc_ia_chat_mensagem.id;


--
-- Name: bc_ia_chat_message; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_chat_message OWNER TO sa;

--
-- Name: TABLE bc_ia_chat_message; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ia_chat_message IS 'Messages in chat sessions';


--
-- Name: bc_ia_chat_message_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_chat_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_chat_message_id_seq OWNER TO sa;

--
-- Name: bc_ia_chat_message_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_chat_message_id_seq OWNED BY brasil_saas.bc_ia_chat_message.id;


--
-- Name: bc_ia_chat_sessao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_chat_sessao OWNER TO sa;

--
-- Name: bc_ia_chat_sessao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_chat_sessao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_chat_sessao_id_seq OWNER TO sa;

--
-- Name: bc_ia_chat_sessao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_chat_sessao_id_seq OWNED BY brasil_saas.bc_ia_chat_sessao.id;


--
-- Name: bc_ia_chat_session; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_chat_session OWNER TO sa;

--
-- Name: TABLE bc_ia_chat_session; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ia_chat_session IS 'Chat sessions with AI assistant';


--
-- Name: bc_ia_chat_session_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_chat_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_chat_session_id_seq OWNER TO sa;

--
-- Name: bc_ia_chat_session_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_chat_session_id_seq OWNED BY brasil_saas.bc_ia_chat_session.id;


--
-- Name: bc_ia_classificacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_classificacao OWNER TO sa;

--
-- Name: bc_ia_classificacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_classificacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_classificacao_id_seq OWNER TO sa;

--
-- Name: bc_ia_classificacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_classificacao_id_seq OWNED BY brasil_saas.bc_ia_classificacao.id;


--
-- Name: bc_ia_config; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_config OWNER TO sa;

--
-- Name: TABLE bc_ia_config; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ia_config IS 'Configuration for AI integration (OpenAI, etc.)';


--
-- Name: bc_ia_config_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_config_id_seq OWNER TO sa;

--
-- Name: bc_ia_config_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_config_id_seq OWNED BY brasil_saas.bc_ia_config.id;


--
-- Name: bc_ia_embedding; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_embedding OWNER TO sa;

--
-- Name: bc_ia_embedding_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_embedding_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_embedding_id_seq OWNER TO sa;

--
-- Name: bc_ia_embedding_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_embedding_id_seq OWNED BY brasil_saas.bc_ia_embedding.id;


--
-- Name: bc_ia_prompt; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_prompt OWNER TO sa;

--
-- Name: bc_ia_prompt_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_prompt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_prompt_id_seq OWNER TO sa;

--
-- Name: bc_ia_prompt_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_prompt_id_seq OWNED BY brasil_saas.bc_ia_prompt.id;


--
-- Name: bc_ia_prompt_template; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ia_prompt_template OWNER TO sa;

--
-- Name: TABLE bc_ia_prompt_template; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ia_prompt_template IS 'Prompt templates for AI interactions';


--
-- Name: bc_ia_prompt_template_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ia_prompt_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ia_prompt_template_id_seq OWNER TO sa;

--
-- Name: bc_ia_prompt_template_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ia_prompt_template_id_seq OWNED BY brasil_saas.bc_ia_prompt_template.id;


--
-- Name: bc_int_delivery; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_int_delivery (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    endpoint_id bigint,
    event_type character varying(160) NOT NULL,
    aggregate_type character varying(100),
    aggregate_id bigint,
    payload text NOT NULL,
    status character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
    attempts integer DEFAULT 0 NOT NULL,
    next_attempt_at timestamp without time zone,
    last_error character varying(2000),
    delivered_at timestamp without time zone,
    correlation_id uuid,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_int_delivery OWNER TO sa;

--
-- Name: bc_int_delivery_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_int_delivery ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_int_delivery_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_int_endpoint; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_int_endpoint (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(100) NOT NULL,
    nome character varying(200) NOT NULL,
    tipo character varying(30) DEFAULT 'WEBHOOK'::character varying NOT NULL,
    url character varying(1000) NOT NULL,
    segredo_hash character varying(128),
    eventos text,
    ativo boolean DEFAULT true NOT NULL,
    timeout_ms integer DEFAULT 10000 NOT NULL,
    tentativas integer DEFAULT 3 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_int_endpoint OWNER TO sa;

--
-- Name: bc_int_endpoint_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_int_endpoint ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_int_endpoint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_int_webhook_event; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_int_webhook_event (
    id bigint NOT NULL,
    empresa_id bigint,
    endpoint_id bigint,
    event_id character varying(160) NOT NULL,
    event_type character varying(160) NOT NULL,
    payload text NOT NULL,
    signature character varying(500),
    status character varying(30) DEFAULT 'RECEBIDO'::character varying NOT NULL,
    attempts integer DEFAULT 0 NOT NULL,
    error_message character varying(2000),
    received_at timestamp without time zone DEFAULT now() NOT NULL,
    processed_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_int_webhook_event OWNER TO sa;

--
-- Name: bc_int_webhook_event_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_int_webhook_event ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_int_webhook_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_kb_artigo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_kb_artigo (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    titulo character varying(200) NOT NULL,
    categoria character varying(80),
    conteudo text,
    tags character varying(200),
    publicado boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);


ALTER TABLE brasil_saas.bc_kb_artigo OWNER TO sa;

--
-- Name: bc_kb_artigo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_kb_artigo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_kb_artigo_id_seq OWNER TO sa;

--
-- Name: bc_kb_artigo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_kb_artigo_id_seq OWNED BY brasil_saas.bc_kb_artigo.id;


--
-- Name: bc_migration_log; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_migration_log OWNER TO sa;

--
-- Name: TABLE bc_migration_log; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_migration_log IS 'Trilha da migração de dados do legado';


--
-- Name: bc_migration_log_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_migration_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_migration_log_id_seq OWNER TO sa;

--
-- Name: bc_migration_log_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_migration_log_id_seq OWNED BY brasil_saas.bc_migration_log.id;


--
-- Name: bc_pcp_mps; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_pcp_mps OWNER TO sa;

--
-- Name: bc_pcp_mps_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_pcp_mps ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_pcp_mps_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_plm_aprovacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_plm_aprovacao (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    mudanca_id bigint NOT NULL,
    etapa integer NOT NULL,
    aprovador_id bigint,
    decisao character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
    observacao character varying(1000),
    decidido_em timestamp without time zone,
    obrigatoria boolean DEFAULT true NOT NULL
);


ALTER TABLE brasil_saas.bc_plm_aprovacao OWNER TO sa;

--
-- Name: bc_plm_aprovacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_plm_aprovacao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_plm_aprovacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_plm_documento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_plm_documento (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    revisao_id bigint,
    mudanca_id bigint,
    tipo character varying(40) NOT NULL,
    codigo character varying(100) NOT NULL,
    versao character varying(30) DEFAULT '1'::character varying NOT NULL,
    nome character varying(300) NOT NULL,
    localizacao character varying(1000),
    hash_documento character varying(128),
    status character varying(30) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    obrigatorio boolean DEFAULT false NOT NULL,
    aprovado_por bigint,
    aprovado_em timestamp without time zone,
    vigencia_inicio date,
    vigencia_fim date,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_plm_documento OWNER TO sa;

--
-- Name: bc_plm_documento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_plm_documento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_plm_documento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_plm_efeito_mudanca; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_plm_efeito_mudanca (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    mudanca_id bigint NOT NULL,
    entidade_tipo character varying(50) NOT NULL,
    entidade_id bigint NOT NULL,
    acao character varying(30) NOT NULL,
    revisao_anterior character varying(30),
    revisao_nova character varying(30),
    efetiva_em date,
    status character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
    observacao character varying(1000),
    ordem_execucao integer DEFAULT 1 NOT NULL,
    obrigatorio boolean DEFAULT true NOT NULL,
    aplicado_em timestamp without time zone,
    aplicado_por bigint,
    erro_implementacao character varying(2000)
);


ALTER TABLE brasil_saas.bc_plm_efeito_mudanca OWNER TO sa;

--
-- Name: bc_plm_efeito_mudanca_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_plm_efeito_mudanca ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_plm_efeito_mudanca_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_plm_mudanca; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_plm_mudanca (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(80) NOT NULL,
    tipo character varying(30) DEFAULT 'ENGENHARIA'::character varying NOT NULL,
    titulo character varying(200) NOT NULL,
    descricao text,
    prioridade character varying(20) DEFAULT 'NORMAL'::character varying NOT NULL,
    status character varying(25) DEFAULT 'ABERTA'::character varying NOT NULL,
    solicitante_id bigint,
    aprovador_id bigint,
    aprovado_em timestamp without time zone,
    implementado_em timestamp without time zone,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_plm_mudanca OWNER TO sa;

--
-- Name: bc_plm_mudanca_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_plm_mudanca ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_plm_mudanca_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_plm_produto_revisao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_plm_produto_revisao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    revisao character varying(30) NOT NULL,
    descricao character varying(500),
    status character varying(25) DEFAULT 'EM_DESENVOLVIMENTO'::character varying NOT NULL,
    vigente_desde date,
    vigente_ate date,
    motivo character varying(500),
    documento_id character varying(120),
    criado_por bigint,
    aprovado_por bigint,
    aprovado_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_plm_produto_revisao OWNER TO sa;

--
-- Name: bc_plm_produto_revisao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_plm_produto_revisao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_plm_produto_revisao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prj_etapa; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prj_etapa OWNER TO sa;

--
-- Name: bc_prj_etapa_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prj_etapa ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_etapa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prj_faturamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prj_faturamento OWNER TO sa;

--
-- Name: bc_prj_faturamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prj_faturamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_faturamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prj_movimento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prj_movimento OWNER TO sa;

--
-- Name: bc_prj_movimento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prj_movimento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_movimento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prj_mudanca; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prj_mudanca OWNER TO sa;

--
-- Name: bc_prj_mudanca_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prj_mudanca ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_mudanca_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prj_projeto; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prj_projeto OWNER TO sa;

--
-- Name: bc_prj_projeto_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prj_projeto ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_projeto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prj_risco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prj_risco OWNER TO sa;

--
-- Name: bc_prj_risco_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prj_risco ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prj_risco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prod_alocacao_capacidade; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_alocacao_capacidade OWNER TO sa;

--
-- Name: bc_prod_alocacao_capacidade_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prod_alocacao_capacidade ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prod_alocacao_capacidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prod_apontamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_apontamento OWNER TO sa;

--
-- Name: bc_prod_apontamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_prod_apontamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_prod_apontamento_id_seq OWNER TO sa;

--
-- Name: bc_prod_apontamento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_prod_apontamento_id_seq OWNED BY brasil_saas.bc_prod_apontamento.id;


--
-- Name: bc_prod_centro_trabalho; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_centro_trabalho OWNER TO sa;

--
-- Name: bc_prod_centro_trabalho_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_prod_centro_trabalho_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_prod_centro_trabalho_id_seq OWNER TO sa;

--
-- Name: bc_prod_centro_trabalho_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_prod_centro_trabalho_id_seq OWNED BY brasil_saas.bc_prod_centro_trabalho.id;


--
-- Name: bc_prod_estrutura; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_estrutura OWNER TO sa;

--
-- Name: bc_prod_estrutura_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_prod_estrutura_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_prod_estrutura_id_seq OWNER TO sa;

--
-- Name: bc_prod_estrutura_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_prod_estrutura_id_seq OWNED BY brasil_saas.bc_prod_estrutura.id;


--
-- Name: bc_prod_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_item OWNER TO sa;

--
-- Name: bc_prod_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prod_item ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prod_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prod_ordem; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_ordem OWNER TO sa;

--
-- Name: bc_prod_ordem_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_prod_ordem ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_prod_ordem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_prod_romaneio; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_romaneio OWNER TO sa;

--
-- Name: bc_prod_romaneio_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_prod_romaneio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_prod_romaneio_id_seq OWNER TO sa;

--
-- Name: bc_prod_romaneio_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_prod_romaneio_id_seq OWNED BY brasil_saas.bc_prod_romaneio.id;


--
-- Name: bc_prod_romaneio_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_romaneio_item OWNER TO sa;

--
-- Name: bc_prod_romaneio_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_prod_romaneio_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_prod_romaneio_item_id_seq OWNER TO sa;

--
-- Name: bc_prod_romaneio_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_prod_romaneio_item_id_seq OWNED BY brasil_saas.bc_prod_romaneio_item.id;


--
-- Name: bc_prod_roteiro; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_roteiro OWNER TO sa;

--
-- Name: bc_prod_roteiro_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_prod_roteiro_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_prod_roteiro_id_seq OWNER TO sa;

--
-- Name: bc_prod_roteiro_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_prod_roteiro_id_seq OWNED BY brasil_saas.bc_prod_roteiro.id;


--
-- Name: bc_prod_roteiro_operacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_prod_roteiro_operacao OWNER TO sa;

--
-- Name: bc_prod_roteiro_operacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_prod_roteiro_operacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_prod_roteiro_operacao_id_seq OWNER TO sa;

--
-- Name: bc_prod_roteiro_operacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_prod_roteiro_operacao_id_seq OWNED BY brasil_saas.bc_prod_roteiro_operacao.id;


--
-- Name: bc_ptl_acesso; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ptl_acesso OWNER TO sa;

--
-- Name: bc_ptl_acesso_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_ptl_acesso ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_ptl_acesso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_qual_inspecao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_qual_inspecao OWNER TO sa;

--
-- Name: bc_qual_inspecao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_qual_inspecao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_qual_inspecao_id_seq OWNER TO sa;

--
-- Name: bc_qual_inspecao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_qual_inspecao_id_seq OWNED BY brasil_saas.bc_qual_inspecao.id;


--
-- Name: bc_qual_nao_conformidade; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_qual_nao_conformidade OWNER TO sa;

--
-- Name: bc_qual_nao_conformidade_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_qual_nao_conformidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_qual_nao_conformidade_id_seq OWNER TO sa;

--
-- Name: bc_qual_nao_conformidade_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_qual_nao_conformidade_id_seq OWNED BY brasil_saas.bc_qual_nao_conformidade.id;


--
-- Name: bc_qual_plano_inspecao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_qual_plano_inspecao OWNER TO sa;

--
-- Name: bc_qual_plano_inspecao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_qual_plano_inspecao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_qual_plano_inspecao_id_seq OWNER TO sa;

--
-- Name: bc_qual_plano_inspecao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_qual_plano_inspecao_id_seq OWNED BY brasil_saas.bc_qual_plano_inspecao.id;


--
-- Name: bc_rh_cargo; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_rh_cargo OWNER TO sa;

--
-- Name: TABLE bc_rh_cargo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_rh_cargo IS 'Cargos';


--
-- Name: bc_rh_cargo_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_rh_cargo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_rh_cargo_id_seq OWNER TO sa;

--
-- Name: bc_rh_cargo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_rh_cargo_id_seq OWNED BY brasil_saas.bc_rh_cargo.id;


--
-- Name: bc_rh_ferias; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_rh_ferias OWNER TO sa;

--
-- Name: bc_rh_ferias_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_rh_ferias ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_rh_ferias_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_rh_folha; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_rh_folha OWNER TO sa;

--
-- Name: TABLE bc_rh_folha; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_rh_folha IS 'Folhas de pagamento por competência';


--
-- Name: COLUMN bc_rh_folha.competencia; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_rh_folha.competencia IS 'Formato YYYY-MM';


--
-- Name: COLUMN bc_rh_folha.status; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_rh_folha.status IS 'ABERTA, PROCESSADA ou CANCELADA';


--
-- Name: COLUMN bc_rh_folha.titulo_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_rh_folha.titulo_id IS 'Título a pagar gerado no financeiro';


--
-- Name: bc_rh_folha_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_rh_folha_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_rh_folha_id_seq OWNER TO sa;

--
-- Name: bc_rh_folha_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_rh_folha_id_seq OWNED BY brasil_saas.bc_rh_folha.id;


--
-- Name: bc_rh_folha_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_rh_folha_item OWNER TO sa;

--
-- Name: COLUMN bc_rh_folha_item.tipo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_rh_folha_item.tipo IS 'PROVENTO ou DESCONTO';


--
-- Name: bc_rh_folha_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_rh_folha_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_rh_folha_item_id_seq OWNER TO sa;

--
-- Name: bc_rh_folha_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_rh_folha_item_id_seq OWNED BY brasil_saas.bc_rh_folha_item.id;


--
-- Name: bc_rh_funcionario; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_rh_funcionario OWNER TO sa;

--
-- Name: TABLE bc_rh_funcionario; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_rh_funcionario IS 'Funcionários vinculados a uma pessoa do cadastro';


--
-- Name: COLUMN bc_rh_funcionario.foto_url; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_rh_funcionario.foto_url IS 'URL da foto do funcionário';


--
-- Name: COLUMN bc_rh_funcionario.foto_tipo_conteudo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_rh_funcionario.foto_tipo_conteudo IS 'Tipo MIME da foto do funcionário';


--
-- Name: COLUMN bc_rh_funcionario.foto_tamanho; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_rh_funcionario.foto_tamanho IS 'Tamanho em bytes da foto do funcionário';


--
-- Name: bc_rh_funcionario_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_rh_funcionario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_rh_funcionario_id_seq OWNER TO sa;

--
-- Name: bc_rh_funcionario_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_rh_funcionario_id_seq OWNED BY brasil_saas.bc_rh_funcionario.id;


--
-- Name: bc_rh_ponto; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_rh_ponto OWNER TO sa;

--
-- Name: bc_rh_ponto_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_rh_ponto ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_rh_ponto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_rh_rescisao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_rh_rescisao (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data_desligamento date NOT NULL,
    motivo character varying(40) NOT NULL,
    meses_trabalhados integer,
    saldo_salario numeric(15,2) DEFAULT 0 NOT NULL,
    decimo_terceiro numeric(15,2) DEFAULT 0 NOT NULL,
    ferias numeric(15,2) DEFAULT 0 NOT NULL,
    terco_ferias numeric(15,2) DEFAULT 0 NOT NULL,
    aviso_previo numeric(15,2) DEFAULT 0 NOT NULL,
    multa_40 numeric(15,2) DEFAULT 0 NOT NULL,
    total numeric(15,2) DEFAULT 0 NOT NULL,
    status character varying(20) DEFAULT 'EFETIVADA'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_at timestamp without time zone,
    updated_by bigint,
    deleted_at timestamp without time zone,
    uuid uuid DEFAULT gen_random_uuid()
);


ALTER TABLE brasil_saas.bc_rh_rescisao OWNER TO sa;

--
-- Name: bc_rh_rescisao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_rh_rescisao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_rh_rescisao_id_seq OWNER TO sa;

--
-- Name: bc_rh_rescisao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_rh_rescisao_id_seq OWNED BY brasil_saas.bc_rh_rescisao.id;


--
-- Name: bc_sc_atp; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_sc_atp (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    local_id bigint,
    data date NOT NULL,
    estoque_disponivel numeric(18,4) DEFAULT 0,
    entradas_confirmadas numeric(18,4) DEFAULT 0,
    reservas numeric(18,4) DEFAULT 0,
    demanda_aberta numeric(18,4) DEFAULT 0,
    quantidade_atp numeric(18,4) DEFAULT 0,
    quantidade_ctp numeric(18,4) DEFAULT 0,
    calculado_em timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_sc_atp OWNER TO sa;

--
-- Name: bc_sc_atp_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_sc_atp ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_sc_atp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_sc_carga; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_sc_carga (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(80) NOT NULL,
    transportadora_id bigint,
    veiculo character varying(100),
    origem character varying(200),
    destino character varying(200),
    peso numeric(18,3) DEFAULT 0,
    volume numeric(18,3) DEFAULT 0,
    valor_mercadoria numeric(18,2) DEFAULT 0,
    rota_id bigint,
    frete_estimado numeric(18,2) DEFAULT 0,
    frete_real numeric(18,2) DEFAULT 0,
    status character varying(30) DEFAULT 'PLANEJADA'::character varying NOT NULL,
    saida timestamp without time zone,
    entrega_prevista timestamp without time zone,
    entrega timestamp without time zone
);


ALTER TABLE brasil_saas.bc_sc_carga OWNER TO sa;

--
-- Name: bc_sc_carga_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_sc_carga ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_sc_carga_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_sc_demanda; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_sc_demanda (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    local_id bigint,
    periodo date NOT NULL,
    tipo character varying(30) DEFAULT 'PREVISAO'::character varying NOT NULL,
    quantidade numeric(18,4) DEFAULT 0 NOT NULL,
    confianca numeric(7,4),
    origem character varying(50),
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_sc_demanda OWNER TO sa;

--
-- Name: bc_sc_demanda_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_sc_demanda ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_sc_demanda_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_sc_frete; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_sc_frete (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    carga_id bigint NOT NULL,
    transportadora_id bigint,
    documento character varying(100),
    frete numeric(18,2) DEFAULT 0 NOT NULL,
    pedagio numeric(18,2) DEFAULT 0,
    adicionais numeric(18,2) DEFAULT 0,
    total numeric(18,2) DEFAULT 0 NOT NULL,
    status character varying(25) DEFAULT 'PENDENTE'::character varying NOT NULL,
    conferido_em timestamp without time zone,
    pago_em timestamp without time zone
);


ALTER TABLE brasil_saas.bc_sc_frete OWNER TO sa;

--
-- Name: bc_sc_frete_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_sc_frete ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_sc_frete_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_sc_planejamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_sc_planejamento (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(80) NOT NULL,
    periodo_inicio date NOT NULL,
    periodo_fim date NOT NULL,
    tipo character varying(30) NOT NULL,
    status character varying(25) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    parametros jsonb DEFAULT '{}'::jsonb,
    resultado jsonb DEFAULT '{}'::jsonb,
    executado_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE brasil_saas.bc_sc_planejamento OWNER TO sa;

--
-- Name: bc_sc_planejamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_sc_planejamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_sc_planejamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_sc_rota; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_sc_rota (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(80) NOT NULL,
    origem character varying(200) NOT NULL,
    destino character varying(200) NOT NULL,
    distancia_km numeric(12,2),
    tempo_minutos integer,
    custo_base numeric(18,2) DEFAULT 0,
    custo_km numeric(18,4) DEFAULT 0,
    pedagio numeric(18,2) DEFAULT 0,
    restricoes jsonb DEFAULT '{}'::jsonb,
    ativo boolean DEFAULT true
);


ALTER TABLE brasil_saas.bc_sc_rota OWNER TO sa;

--
-- Name: bc_sc_rota_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_sc_rota ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_sc_rota_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_sc_tracking; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_sc_tracking (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    carga_id bigint NOT NULL,
    evento character varying(50) NOT NULL,
    data_evento timestamp without time zone DEFAULT now() NOT NULL,
    localizacao character varying(200),
    latitude numeric(10,7),
    longitude numeric(10,7),
    ocorrencia character varying(500),
    comprovante character varying(500)
);


ALTER TABLE brasil_saas.bc_sc_tracking OWNER TO sa;

--
-- Name: bc_sc_tracking_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_sc_tracking ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_sc_tracking_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_scm_ordem_transporte; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_scm_ordem_transporte (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(80) NOT NULL,
    tipo character varying(30) DEFAULT 'ENTREGA'::character varying NOT NULL,
    status character varying(25) DEFAULT 'PLANEJADA'::character varying NOT NULL,
    origem character varying(200),
    destino character varying(200),
    transportadora_id bigint,
    veiculo character varying(100),
    motorista character varying(160),
    data_prevista date,
    data_saida date,
    data_entrega date,
    valor_frete numeric(18,2) DEFAULT 0 NOT NULL,
    peso numeric(18,3),
    volume numeric(18,3),
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_scm_ordem_transporte OWNER TO sa;

--
-- Name: bc_scm_ordem_transporte_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_scm_ordem_transporte ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_scm_ordem_transporte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_scm_politica_reposicao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_scm_politica_reposicao (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    produto_id bigint NOT NULL,
    deposito_id bigint,
    metodo character varying(30) DEFAULT 'PONTO_PEDIDO'::character varying NOT NULL,
    estoque_minimo numeric(18,3) DEFAULT 0 NOT NULL,
    estoque_maximo numeric(18,3) DEFAULT 0 NOT NULL,
    estoque_seguranca numeric(18,3) DEFAULT 0 NOT NULL,
    ponto_pedido numeric(18,3) DEFAULT 0 NOT NULL,
    lote_economico numeric(18,3) DEFAULT 0 NOT NULL,
    lead_time_dias integer DEFAULT 0 NOT NULL,
    fornecedor_preferencial_id bigint,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_scm_politica_reposicao OWNER TO sa;

--
-- Name: bc_scm_politica_reposicao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_scm_politica_reposicao ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_scm_politica_reposicao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_srv_contrato; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_srv_contrato (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(80) NOT NULL,
    cliente_id bigint,
    descricao character varying(500) NOT NULL,
    inicio date NOT NULL,
    fim date,
    tipo character varying(30) DEFAULT 'SUPORTE'::character varying NOT NULL,
    sla_horas numeric(10,2),
    valor_mensal numeric(18,2) DEFAULT 0 NOT NULL,
    franquia_horas numeric(10,2),
    horas_consumidas numeric(10,2) DEFAULT 0 NOT NULL,
    status character varying(25) DEFAULT 'ATIVO'::character varying NOT NULL,
    renovacao_automatica boolean DEFAULT false NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp without time zone
);


ALTER TABLE brasil_saas.bc_srv_contrato OWNER TO sa;

--
-- Name: bc_srv_contrato_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_srv_contrato ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_srv_contrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_srv_ordem_servico; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_srv_ordem_servico OWNER TO sa;

--
-- Name: bc_srv_ordem_servico_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_srv_ordem_servico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_srv_ordem_servico_id_seq OWNER TO sa;

--
-- Name: bc_srv_ordem_servico_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_srv_ordem_servico_id_seq OWNED BY brasil_saas.bc_srv_ordem_servico.id;


--
-- Name: bc_srv_os_apontamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_srv_os_apontamento OWNER TO sa;

--
-- Name: bc_srv_os_apontamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_srv_os_apontamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_srv_os_apontamento_id_seq OWNER TO sa;

--
-- Name: bc_srv_os_apontamento_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_srv_os_apontamento_id_seq OWNED BY brasil_saas.bc_srv_os_apontamento.id;


--
-- Name: bc_srv_os_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_srv_os_item OWNER TO sa;

--
-- Name: bc_srv_os_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_srv_os_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_srv_os_item_id_seq OWNER TO sa;

--
-- Name: bc_srv_os_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_srv_os_item_id_seq OWNED BY brasil_saas.bc_srv_os_item.id;


--
-- Name: bc_tms_documento_entrega; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_documento_entrega (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ordem_id bigint NOT NULL,
    tipo character varying(40) DEFAULT 'COMPROVANTE_ENTREGA'::character varying NOT NULL,
    numero character varying(120),
    recebedor character varying(160),
    recebido_em timestamp without time zone,
    assinatura_localizacao character varying(500),
    observacao character varying(1000),
    documento_localizacao character varying(1000),
    status character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL
);


ALTER TABLE brasil_saas.bc_tms_documento_entrega OWNER TO sa;

--
-- Name: bc_tms_documento_entrega_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_documento_entrega ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_documento_entrega_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_tms_evento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_evento (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ordem_id bigint NOT NULL,
    tipo character varying(50) NOT NULL,
    data_evento timestamp without time zone DEFAULT now() NOT NULL,
    localizacao character varying(200),
    descricao character varying(500)
);


ALTER TABLE brasil_saas.bc_tms_evento OWNER TO sa;

--
-- Name: bc_tms_evento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_evento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_evento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_tms_fechamento; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_fechamento (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ordem_id bigint NOT NULL,
    frete_contratado numeric(15,2) DEFAULT 0 NOT NULL,
    adicionais numeric(15,2) DEFAULT 0 NOT NULL,
    descontos numeric(15,2) DEFAULT 0 NOT NULL,
    frete_aprovado numeric(15,2) DEFAULT 0 NOT NULL,
    documento character varying(120),
    status character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
    aprovado_por character varying(160),
    aprovado_em timestamp without time zone
);


ALTER TABLE brasil_saas.bc_tms_fechamento OWNER TO sa;

--
-- Name: bc_tms_fechamento_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_fechamento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_fechamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_tms_frete; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_frete (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ordem_id bigint NOT NULL,
    componente character varying(80) NOT NULL,
    valor numeric(15,2) DEFAULT 0 NOT NULL,
    documento character varying(120),
    status character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL
);


ALTER TABLE brasil_saas.bc_tms_frete OWNER TO sa;

--
-- Name: bc_tms_frete_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_frete ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_frete_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_tms_ordem; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_ordem (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    numero character varying(60) NOT NULL,
    origem character varying(200),
    destino character varying(200),
    transportadora_id bigint,
    modalidade character varying(40) DEFAULT 'RODOVIARIO'::character varying NOT NULL,
    status character varying(30) DEFAULT 'PLANEJADA'::character varying NOT NULL,
    data_prevista_saida timestamp without time zone,
    data_prevista_entrega timestamp without time zone,
    peso numeric(15,3) DEFAULT 0,
    volume numeric(15,3) DEFAULT 0,
    frete_previsto numeric(15,2) DEFAULT 0,
    frete_real numeric(15,2) DEFAULT 0,
    rota_id bigint,
    veiculo character varying(120),
    motorista character varying(160),
    data_saida timestamp without time zone,
    data_entrega timestamp without time zone,
    distancia_real_km numeric(12,2)
);


ALTER TABLE brasil_saas.bc_tms_ordem OWNER TO sa;

--
-- Name: bc_tms_ordem_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_ordem ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_ordem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_tms_parada; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_parada (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ordem_id bigint NOT NULL,
    sequencia integer NOT NULL,
    tipo character varying(30) NOT NULL,
    localizacao character varying(300) NOT NULL,
    prevista_em timestamp without time zone,
    realizada_em timestamp without time zone,
    status character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
    referencia_tipo character varying(40),
    referencia_id bigint,
    peso numeric(15,3),
    volume numeric(15,3)
);


ALTER TABLE brasil_saas.bc_tms_parada OWNER TO sa;

--
-- Name: bc_tms_parada_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_parada ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_parada_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_tms_rota; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_rota (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    codigo character varying(60) NOT NULL,
    descricao character varying(300),
    origem character varying(200) NOT NULL,
    destino character varying(200) NOT NULL,
    distancia_km numeric(12,2),
    tempo_estimado_min integer,
    pedagio_estimado numeric(15,2) DEFAULT 0,
    ativo boolean DEFAULT true NOT NULL
);


ALTER TABLE brasil_saas.bc_tms_rota OWNER TO sa;

--
-- Name: bc_tms_rota_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_rota ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_rota_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_tms_tracking; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_tms_tracking (
    id bigint NOT NULL,
    empresa_id bigint NOT NULL,
    ordem_id bigint NOT NULL,
    codigo character varying(100) NOT NULL,
    transportadora character varying(160),
    ultimo_status character varying(80),
    ultima_atualizacao timestamp without time zone
);


ALTER TABLE brasil_saas.bc_tms_tracking OWNER TO sa;

--
-- Name: bc_tms_tracking_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_tms_tracking ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_tms_tracking_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_ven_bonificacao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_bonificacao OWNER TO sa;

--
-- Name: TABLE bc_ven_bonificacao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ven_bonificacao IS 'Bonificacoes comerciais';


--
-- Name: bc_ven_bonificacao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_bonificacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_bonificacao_id_seq OWNER TO sa;

--
-- Name: bc_ven_bonificacao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_bonificacao_id_seq OWNED BY brasil_saas.bc_ven_bonificacao.id;


--
-- Name: bc_ven_bonificacao_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_bonificacao_item OWNER TO sa;

--
-- Name: bc_ven_bonificacao_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_bonificacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_bonificacao_item_id_seq OWNER TO sa;

--
-- Name: bc_ven_bonificacao_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_bonificacao_item_id_seq OWNED BY brasil_saas.bc_ven_bonificacao_item.id;


--
-- Name: bc_ven_contrato; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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
    deleted_at timestamp without time zone,
    valor numeric(18,2)
);


ALTER TABLE brasil_saas.bc_ven_contrato OWNER TO sa;

--
-- Name: TABLE bc_ven_contrato; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ven_contrato IS 'Contratos comerciais e recorrencia';


--
-- Name: bc_ven_contrato_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_contrato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_contrato_id_seq OWNER TO sa;

--
-- Name: bc_ven_contrato_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_contrato_id_seq OWNED BY brasil_saas.bc_ven_contrato.id;


--
-- Name: bc_ven_contrato_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_contrato_item OWNER TO sa;

--
-- Name: bc_ven_contrato_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_contrato_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_contrato_item_id_seq OWNER TO sa;

--
-- Name: bc_ven_contrato_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_contrato_item_id_seq OWNED BY brasil_saas.bc_ven_contrato_item.id;


--
-- Name: bc_ven_devolucao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_devolucao OWNER TO sa;

--
-- Name: TABLE bc_ven_devolucao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ven_devolucao IS 'Devolucoes e trocas de vendas';


--
-- Name: bc_ven_devolucao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_devolucao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_devolucao_id_seq OWNER TO sa;

--
-- Name: bc_ven_devolucao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_devolucao_id_seq OWNED BY brasil_saas.bc_ven_devolucao.id;


--
-- Name: bc_ven_devolucao_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_devolucao_item OWNER TO sa;

--
-- Name: bc_ven_devolucao_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_devolucao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_devolucao_item_id_seq OWNER TO sa;

--
-- Name: bc_ven_devolucao_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_devolucao_item_id_seq OWNED BY brasil_saas.bc_ven_devolucao_item.id;


--
-- Name: bc_ven_meta; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bc_ven_meta (
    id bigint NOT NULL,
    uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    empresa_id bigint NOT NULL,
    ano integer NOT NULL,
    mes integer NOT NULL,
    vendedor_id bigint,
    canal character varying(40),
    valor_meta numeric(18,2) DEFAULT 0 NOT NULL,
    valor_realizado numeric(18,2) DEFAULT 0 NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    created_by bigint,
    updated_by bigint
);


ALTER TABLE brasil_saas.bc_ven_meta OWNER TO sa;

--
-- Name: bc_ven_meta_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_meta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_meta_id_seq OWNER TO sa;

--
-- Name: bc_ven_meta_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_meta_id_seq OWNED BY brasil_saas.bc_ven_meta.id;


--
-- Name: bc_ven_pedido; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_pedido OWNER TO sa;

--
-- Name: TABLE bc_ven_pedido; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ven_pedido IS 'Pedidos de venda e orçamentos';


--
-- Name: COLUMN bc_ven_pedido.tipo; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_ven_pedido.tipo IS 'ORCAMENTO ou PEDIDO';


--
-- Name: COLUMN bc_ven_pedido.status; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_ven_pedido.status IS 'ABERTO, FATURADO ou CANCELADO';


--
-- Name: COLUMN bc_ven_pedido.titulo_id; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON COLUMN brasil_saas.bc_ven_pedido.titulo_id IS 'Título a receber gerado no financeiro';


--
-- Name: bc_ven_pedido_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_pedido_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_pedido_id_seq OWNER TO sa;

--
-- Name: bc_ven_pedido_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_pedido_id_seq OWNED BY brasil_saas.bc_ven_pedido.id;


--
-- Name: bc_ven_pedido_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_pedido_item OWNER TO sa;

--
-- Name: bc_ven_pedido_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_pedido_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_pedido_item_id_seq OWNER TO sa;

--
-- Name: bc_ven_pedido_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_pedido_item_id_seq OWNED BY brasil_saas.bc_ven_pedido_item.id;


--
-- Name: bc_ven_regra_comissao; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_regra_comissao OWNER TO sa;

--
-- Name: TABLE bc_ven_regra_comissao; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ven_regra_comissao IS 'Regras de comissao por vendedor, meta e faixa';


--
-- Name: bc_ven_regra_comissao_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_regra_comissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_regra_comissao_id_seq OWNER TO sa;

--
-- Name: bc_ven_regra_comissao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_regra_comissao_id_seq OWNED BY brasil_saas.bc_ven_regra_comissao.id;


--
-- Name: bc_ven_tabela_preco; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_tabela_preco OWNER TO sa;

--
-- Name: TABLE bc_ven_tabela_preco; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ven_tabela_preco IS 'Tabelas de preco comerciais por empresa';


--
-- Name: bc_ven_tabela_preco_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_tabela_preco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_tabela_preco_id_seq OWNER TO sa;

--
-- Name: bc_ven_tabela_preco_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_tabela_preco_id_seq OWNED BY brasil_saas.bc_ven_tabela_preco.id;


--
-- Name: bc_ven_tabela_preco_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_ven_tabela_preco_item OWNER TO sa;

--
-- Name: TABLE bc_ven_tabela_preco_item; Type: COMMENT; Schema: brasil_saas; Owner: sa
--

COMMENT ON TABLE brasil_saas.bc_ven_tabela_preco_item IS 'Precos de produtos por tabela comercial';


--
-- Name: bc_ven_tabela_preco_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

CREATE SEQUENCE brasil_saas.bc_ven_tabela_preco_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas.bc_ven_tabela_preco_item_id_seq OWNER TO sa;

--
-- Name: bc_ven_tabela_preco_item_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas; Owner: sa
--

ALTER SEQUENCE brasil_saas.bc_ven_tabela_preco_item_id_seq OWNED BY brasil_saas.bc_ven_tabela_preco_item.id;


--
-- Name: bc_wkf_definition; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wkf_definition OWNER TO sa;

--
-- Name: bc_wkf_definition_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wkf_definition ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_definition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_wkf_instance; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wkf_instance OWNER TO sa;

--
-- Name: bc_wkf_instance_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wkf_instance ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_wkf_stage; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wkf_stage OWNER TO sa;

--
-- Name: bc_wkf_stage_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wkf_stage ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_stage_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_wkf_task; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wkf_task OWNER TO sa;

--
-- Name: bc_wkf_task_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wkf_task ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wkf_task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_wms_onda; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wms_onda OWNER TO sa;

--
-- Name: bc_wms_onda_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wms_onda ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_onda_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_wms_onda_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wms_onda_item OWNER TO sa;

--
-- Name: bc_wms_onda_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wms_onda_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_onda_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_wms_volume; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wms_volume OWNER TO sa;

--
-- Name: bc_wms_volume_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wms_volume ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_volume_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bc_wms_volume_item; Type: TABLE; Schema: brasil_saas; Owner: sa
--

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


ALTER TABLE brasil_saas.bc_wms_volume_item OWNER TO sa;

--
-- Name: bc_wms_volume_item_id_seq; Type: SEQUENCE; Schema: brasil_saas; Owner: sa
--

ALTER TABLE brasil_saas.bc_wms_volume_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME brasil_saas.bc_wms_volume_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bk_usuario_perfil_20261009; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.bk_usuario_perfil_20261009 (
    id bigint,
    usuario_id bigint,
    perfil_id bigint,
    created_at timestamp without time zone,
    created_by bigint
);


ALTER TABLE brasil_saas.bk_usuario_perfil_20261009 OWNER TO sa;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: brasil_saas; Owner: sa
--

CREATE TABLE brasil_saas.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE brasil_saas.flyway_schema_history OWNER TO sa;

--
-- Name: dim_cliente; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.dim_cliente OWNER TO sa;

--
-- Name: dim_cliente_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.dim_cliente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.dim_cliente_id_seq OWNER TO sa;

--
-- Name: dim_cliente_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.dim_cliente_id_seq OWNED BY brasil_saas_dl.dim_cliente.id;


--
-- Name: dim_empresa; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.dim_empresa OWNER TO sa;

--
-- Name: dim_empresa_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.dim_empresa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.dim_empresa_id_seq OWNER TO sa;

--
-- Name: dim_empresa_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.dim_empresa_id_seq OWNED BY brasil_saas_dl.dim_empresa.id;


--
-- Name: dim_produto; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.dim_produto OWNER TO sa;

--
-- Name: dim_produto_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.dim_produto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.dim_produto_id_seq OWNER TO sa;

--
-- Name: dim_produto_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.dim_produto_id_seq OWNED BY brasil_saas_dl.dim_produto.id;


--
-- Name: dim_tempo; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.dim_tempo OWNER TO sa;

--
-- Name: dim_tempo_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.dim_tempo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.dim_tempo_id_seq OWNER TO sa;

--
-- Name: dim_tempo_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.dim_tempo_id_seq OWNED BY brasil_saas_dl.dim_tempo.id;


--
-- Name: ft_compras; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.ft_compras OWNER TO sa;

--
-- Name: ft_compras_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.ft_compras_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.ft_compras_id_seq OWNER TO sa;

--
-- Name: ft_compras_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.ft_compras_id_seq OWNED BY brasil_saas_dl.ft_compras.id;


--
-- Name: ft_estoque; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.ft_estoque OWNER TO sa;

--
-- Name: ft_estoque_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.ft_estoque_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.ft_estoque_id_seq OWNER TO sa;

--
-- Name: ft_estoque_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.ft_estoque_id_seq OWNED BY brasil_saas_dl.ft_estoque.id;


--
-- Name: ft_financeiro; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.ft_financeiro OWNER TO sa;

--
-- Name: ft_financeiro_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.ft_financeiro_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.ft_financeiro_id_seq OWNER TO sa;

--
-- Name: ft_financeiro_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.ft_financeiro_id_seq OWNED BY brasil_saas_dl.ft_financeiro.id;


--
-- Name: ft_producao; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.ft_producao OWNER TO sa;

--
-- Name: ft_producao_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.ft_producao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.ft_producao_id_seq OWNER TO sa;

--
-- Name: ft_producao_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.ft_producao_id_seq OWNED BY brasil_saas_dl.ft_producao.id;


--
-- Name: ft_vendas; Type: TABLE; Schema: brasil_saas_dl; Owner: sa
--

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


ALTER TABLE brasil_saas_dl.ft_vendas OWNER TO sa;

--
-- Name: ft_vendas_id_seq; Type: SEQUENCE; Schema: brasil_saas_dl; Owner: sa
--

CREATE SEQUENCE brasil_saas_dl.ft_vendas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE brasil_saas_dl.ft_vendas_id_seq OWNER TO sa;

--
-- Name: ft_vendas_id_seq; Type: SEQUENCE OWNED BY; Schema: brasil_saas_dl; Owner: sa
--

ALTER SEQUENCE brasil_saas_dl.ft_vendas_id_seq OWNED BY brasil_saas_dl.ft_vendas.id;


--
-- Name: base_cep; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.base_cep (
    cep character varying(8) NOT NULL,
    logradouro character varying(255),
    bairro character varying(255),
    cidade character varying(255),
    uf character varying(2)
);


ALTER TABLE public.base_cep OWNER TO postgres;

--
-- Name: fcaixa; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fcaixa (
    codcaixa integer NOT NULL,
    descricaocaixa character varying(255) NOT NULL,
    agencia character varying(50),
    nomebanco character varying(100),
    numeroconta character varying(50),
    saldobanco numeric(15,2) DEFAULT 0.0
);


ALTER TABLE public.fcaixa OWNER TO postgres;

--
-- Name: fcaixa_codcaixa_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fcaixa_codcaixa_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fcaixa_codcaixa_seq OWNER TO postgres;

--
-- Name: fcaixa_codcaixa_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fcaixa_codcaixa_seq OWNED BY public.fcaixa.codcaixa;


--
-- Name: fcentrocusto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fcentrocusto (
    codcusto integer NOT NULL,
    descricaocusto character varying(255) NOT NULL,
    ativo character varying(1) DEFAULT 'S'::character varying,
    aceitalancamento character varying(1) DEFAULT 'S'::character varying,
    tipo character varying(1) DEFAULT 'D'::character varying
);


ALTER TABLE public.fcentrocusto OWNER TO postgres;

--
-- Name: fcentrocusto_codcusto_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fcentrocusto_codcusto_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fcentrocusto_codcusto_seq OWNER TO postgres;

--
-- Name: fcentrocusto_codcusto_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fcentrocusto_codcusto_seq OWNED BY public.fcentrocusto.codcusto;


--
-- Name: fcfo_codcfo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fcfo_codcfo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fcfo_codcfo_seq OWNER TO postgres;

--
-- Name: fcfo; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fcfo OWNER TO postgres;

--
-- Name: fcondicao_codcondicao_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fcondicao_codcondicao_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fcondicao_codcondicao_seq OWNER TO postgres;

--
-- Name: fcondicao; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fcondicao OWNER TO postgres;

--
-- Name: fcusto; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fcusto OWNER TO postgres;

--
-- Name: fcusto_codcusto_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fcusto_codcusto_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fcusto_codcusto_seq OWNER TO postgres;

--
-- Name: fcusto_codcusto_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fcusto_codcusto_seq OWNED BY public.fcusto.codcusto;


--
-- Name: fdatas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fdatas (
    iddata public.docod NOT NULL,
    datainicial public.dodata,
    datafinal public.dodata
);


ALTER TABLE public.fdatas OWNER TO postgres;

--
-- Name: fdatas_iddata_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fdatas_iddata_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fdatas_iddata_seq OWNER TO postgres;

--
-- Name: fdatas_iddata_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fdatas_iddata_seq OWNED BY public.fdatas.iddata;


--
-- Name: fdia; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fdia (
    refdia integer NOT NULL,
    baixadia public.dom1,
    datadia public.dodata,
    valorabertura public.dovalor,
    usuariodia public.docod,
    obsdia character varying(255)
);


ALTER TABLE public.fdia OWNER TO postgres;

--
-- Name: fdia_refdia_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fdia_refdia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fdia_refdia_seq OWNER TO postgres;

--
-- Name: fdia_refdia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fdia_refdia_seq OWNED BY public.fdia.refdia;


--
-- Name: fdocumento_coddoc_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fdocumento_coddoc_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fdocumento_coddoc_seq OWNER TO postgres;

--
-- Name: fdocumento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fdocumento (
    coddoc public.docod DEFAULT nextval('public.fdocumento_coddoc_seq'::regclass) NOT NULL,
    descricaodoc character varying(255),
    doccontabil public.dom1
);


ALTER TABLE public.fdocumento OWNER TO postgres;

--
-- Name: fempresa; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fempresa OWNER TO postgres;

--
-- Name: fempresa_codigo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fempresa_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fempresa_codigo_seq OWNER TO postgres;

--
-- Name: fempresa_codigo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fempresa_codigo_seq OWNED BY public.fempresa.codigo;


--
-- Name: fempresa_vinculo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fempresa_vinculo (
    codvinculo integer NOT NULL,
    codmatriz integer NOT NULL,
    codfilial integer
);


ALTER TABLE public.fempresa_vinculo OWNER TO postgres;

--
-- Name: fempresa_vinculo_codvinculo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fempresa_vinculo_codvinculo_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fempresa_vinculo_codvinculo_seq OWNER TO postgres;

--
-- Name: fempresa_vinculo_codvinculo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fempresa_vinculo_codvinculo_seq OWNED BY public.fempresa_vinculo.codvinculo;


--
-- Name: fextrato; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fextrato OWNER TO postgres;

--
-- Name: fextrato_idlanextrato_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fextrato_idlanextrato_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fextrato_idlanextrato_seq OWNER TO postgres;

--
-- Name: fextrato_idlanextrato_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fextrato_idlanextrato_seq OWNED BY public.fextrato.idlanextrato;


--
-- Name: ffinanceiro; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ffinanceiro (
    codfinanceiro integer NOT NULL,
    codmovimento integer,
    codcliente integer NOT NULL,
    datavencimento date NOT NULL,
    valor numeric(15,2) DEFAULT 0.00 NOT NULL,
    tipolancamento character varying(255) NOT NULL,
    status character varying(255) DEFAULT 'PENDENTE'::character varying NOT NULL
);


ALTER TABLE public.ffinanceiro OWNER TO postgres;

--
-- Name: ffinanceiro_codfinanceiro_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ffinanceiro_codfinanceiro_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ffinanceiro_codfinanceiro_seq OWNER TO postgres;

--
-- Name: ffinanceiro_codfinanceiro_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ffinanceiro_codfinanceiro_seq OWNED BY public.ffinanceiro.codfinanceiro;


--
-- Name: ffuncionario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ffuncionario (
    codfuncionario public.docod NOT NULL,
    nomefuncionario character varying(255),
    comissao public.dovalor,
    ativo public.dom1,
    login_db character varying(255)
);


ALTER TABLE public.ffuncionario OWNER TO postgres;

--
-- Name: ffuncionario_codfuncionario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ffuncionario_codfuncionario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ffuncionario_codfuncionario_seq OWNER TO postgres;

--
-- Name: ffuncionario_codfuncionario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ffuncionario_codfuncionario_seq OWNED BY public.ffuncionario.codfuncionario;


--
-- Name: fitem; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fitem OWNER TO postgres;

--
-- Name: fitem_refitem_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fitem_refitem_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitem_refitem_seq OWNER TO postgres;

--
-- Name: fitem_refitem_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fitem_refitem_seq OWNED BY public.fitem.refitem;


--
-- Name: flan_idlan_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.flan_idlan_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.flan_idlan_seq OWNER TO postgres;

--
-- Name: flan; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.flan OWNER TO postgres;

--
-- Name: fmov; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fmov OWNER TO postgres;

--
-- Name: fmov_idmov_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fmov_idmov_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fmov_idmov_seq OWNER TO postgres;

--
-- Name: fmov_idmov_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fmov_idmov_seq OWNED BY public.fmov.idmov;


--
-- Name: fmovimento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fmovimento (
    codmovimento integer NOT NULL,
    codcliente integer NOT NULL,
    dataemissao date NOT NULL,
    totalfatura numeric(15,2) DEFAULT 0.00 NOT NULL,
    tipomovimento character varying(255) DEFAULT 'OS'::character varying NOT NULL
);


ALTER TABLE public.fmovimento OWNER TO postgres;

--
-- Name: fmovimento_codmovimento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fmovimento_codmovimento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fmovimento_codmovimento_seq OWNER TO postgres;

--
-- Name: fmovimento_codmovimento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fmovimento_codmovimento_seq OWNED BY public.fmovimento.codmovimento;


--
-- Name: fmovimento_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fmovimento_item (
    coditem integer NOT NULL,
    codmovimento integer NOT NULL,
    codproduto integer NOT NULL,
    quantidade numeric(15,2) DEFAULT 1.00 NOT NULL,
    valorunitario numeric(15,2) DEFAULT 0.00 NOT NULL,
    totalitem numeric(15,2) DEFAULT 0.00 NOT NULL,
    codvariacao integer
);


ALTER TABLE public.fmovimento_item OWNER TO postgres;

--
-- Name: fmovimento_item_coditem_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fmovimento_item_coditem_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fmovimento_item_coditem_seq OWNER TO postgres;

--
-- Name: fmovimento_item_coditem_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fmovimento_item_coditem_seq OWNED BY public.fmovimento_item.coditem;


--
-- Name: fnota; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fnota OWNER TO postgres;

--
-- Name: fnota_id_nota_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fnota_id_nota_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fnota_id_nota_seq OWNER TO postgres;

--
-- Name: fnota_id_nota_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fnota_id_nota_seq OWNED BY public.fnota.id_nota;


--
-- Name: fnota_item; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fnota_item OWNER TO postgres;

--
-- Name: fnota_item_id_item_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fnota_item_id_item_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fnota_item_id_item_seq OWNER TO postgres;

--
-- Name: fnota_item_id_item_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fnota_item_id_item_seq OWNED BY public.fnota_item.id_item;


--
-- Name: fpessoa; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fpessoa OWNER TO postgres;

--
-- Name: fpessoa_id_pessoa_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fpessoa_id_pessoa_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fpessoa_id_pessoa_seq OWNER TO postgres;

--
-- Name: fpessoa_id_pessoa_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fpessoa_id_pessoa_seq OWNED BY public.fpessoa.id_pessoa;


--
-- Name: gen_fproduto_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_fproduto_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_fproduto_id OWNER TO postgres;

--
-- Name: fproduto; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fproduto OWNER TO postgres;

--
-- Name: fproduto_ecommerce; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fproduto_ecommerce OWNER TO postgres;

--
-- Name: fproduto_ecommerce_codecommerce_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fproduto_ecommerce_codecommerce_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fproduto_ecommerce_codecommerce_seq OWNER TO postgres;

--
-- Name: fproduto_ecommerce_codecommerce_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fproduto_ecommerce_codecommerce_seq OWNED BY public.fproduto_ecommerce.codecommerce;


--
-- Name: fproduto_fiscal; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fproduto_fiscal (
    codproduto integer NOT NULL,
    cest character varying(255),
    icms numeric(5,2),
    pis numeric(5,2),
    cofins numeric(5,2),
    iss numeric(5,2)
);


ALTER TABLE public.fproduto_fiscal OWNER TO postgres;

--
-- Name: fproduto_fiscal_codproduto_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fproduto_fiscal_codproduto_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fproduto_fiscal_codproduto_seq OWNER TO postgres;

--
-- Name: fproduto_fiscal_codproduto_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fproduto_fiscal_codproduto_seq OWNED BY public.fproduto_fiscal.codproduto;


--
-- Name: fproduto_imagem; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fproduto_imagem (
    codimagem integer NOT NULL,
    codvariacao integer NOT NULL,
    url_caminho character varying(500) NOT NULL,
    is_principal boolean DEFAULT false
);


ALTER TABLE public.fproduto_imagem OWNER TO postgres;

--
-- Name: fproduto_imagem_codimagem_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fproduto_imagem_codimagem_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fproduto_imagem_codimagem_seq OWNER TO postgres;

--
-- Name: fproduto_imagem_codimagem_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fproduto_imagem_codimagem_seq OWNED BY public.fproduto_imagem.codimagem;


--
-- Name: fproduto_kit; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fproduto_kit (
    codkit integer NOT NULL,
    codproduto_filho integer NOT NULL,
    quantidade integer DEFAULT 1
);


ALTER TABLE public.fproduto_kit OWNER TO postgres;

--
-- Name: fproduto_kit_codkit_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fproduto_kit_codkit_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fproduto_kit_codkit_seq OWNER TO postgres;

--
-- Name: fproduto_kit_codkit_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fproduto_kit_codkit_seq OWNED BY public.fproduto_kit.codkit;


--
-- Name: fproduto_kit_codproduto_filho_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fproduto_kit_codproduto_filho_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fproduto_kit_codproduto_filho_seq OWNER TO postgres;

--
-- Name: fproduto_kit_codproduto_filho_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fproduto_kit_codproduto_filho_seq OWNED BY public.fproduto_kit.codproduto_filho;


--
-- Name: fproduto_movimento; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fproduto_movimento OWNER TO postgres;

--
-- Name: fproduto_movimento_codmov_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fproduto_movimento_codmov_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fproduto_movimento_codmov_seq OWNER TO postgres;

--
-- Name: fproduto_movimento_codmov_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fproduto_movimento_codmov_seq OWNED BY public.fproduto_movimento.codmov;


--
-- Name: fproduto_variacao; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fproduto_variacao OWNER TO postgres;

--
-- Name: fproduto_variacao_codvariacao_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fproduto_variacao_codvariacao_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fproduto_variacao_codvariacao_seq OWNER TO postgres;

--
-- Name: fproduto_variacao_codvariacao_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fproduto_variacao_codvariacao_seq OWNED BY public.fproduto_variacao.codvariacao;


--
-- Name: frecibo; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.frecibo OWNER TO postgres;

--
-- Name: frecibo_idrecibo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.frecibo_idrecibo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.frecibo_idrecibo_seq OWNER TO postgres;

--
-- Name: frecibo_idrecibo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.frecibo_idrecibo_seq OWNED BY public.frecibo.idrecibo;


--
-- Name: ftipo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ftipo (
    codtipo public.docod NOT NULL,
    descricaotipo character varying(255),
    tipoconta public.dom1
);


ALTER TABLE public.ftipo OWNER TO postgres;

--
-- Name: ftipo_codtipo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ftipo_codtipo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ftipo_codtipo_seq OWNER TO postgres;

--
-- Name: ftipo_codtipo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ftipo_codtipo_seq OWNED BY public.ftipo.codtipo;


--
-- Name: ftipopagamento_codtipo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ftipopagamento_codtipo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ftipopagamento_codtipo_seq OWNER TO postgres;

--
-- Name: ftipopagamento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ftipopagamento (
    codtipo public.docod DEFAULT nextval('public.ftipopagamento_codtipo_seq'::regclass) NOT NULL,
    descricaopagamento character varying(255),
    seriepagamento public.dom2
);


ALTER TABLE public.ftipopagamento OWNER TO postgres;

--
-- Name: fusuario; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fusuario OWNER TO postgres;

--
-- Name: fusuario_codigo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fusuario_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fusuario_codigo_seq OWNER TO postgres;

--
-- Name: fusuario_codigo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fusuario_codigo_seq OWNED BY public.fusuario.codigo;


--
-- Name: fvenda; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.fvenda OWNER TO postgres;

--
-- Name: fvenda_id_venda_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fvenda_id_venda_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fvenda_id_venda_seq OWNER TO postgres;

--
-- Name: fvenda_id_venda_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fvenda_id_venda_seq OWNED BY public.fvenda.id_venda;


--
-- Name: fvenda_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fvenda_item (
    id_item integer NOT NULL,
    id_venda integer NOT NULL,
    codvariacao integer,
    quantidade numeric(15,2) DEFAULT 1.00 NOT NULL,
    valor_unitario numeric(15,2) DEFAULT 0.00 NOT NULL,
    total_item numeric(15,2) DEFAULT 0.00 NOT NULL
);


ALTER TABLE public.fvenda_item OWNER TO postgres;

--
-- Name: fvenda_item_id_item_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fvenda_item_id_item_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fvenda_item_id_item_seq OWNER TO postgres;

--
-- Name: fvenda_item_id_item_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fvenda_item_id_item_seq OWNED BY public.fvenda_item.id_item;


--
-- Name: gen_fcfo_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_fcfo_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_fcfo_id OWNER TO postgres;

--
-- Name: gen_fcusto_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_fcusto_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_fcusto_id OWNER TO postgres;

--
-- Name: gen_fdia_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_fdia_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_fdia_id OWNER TO postgres;

--
-- Name: gen_fextrato_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_fextrato_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_fextrato_id OWNER TO postgres;

--
-- Name: gen_ffuncionario_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_ffuncionario_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_ffuncionario_id OWNER TO postgres;

--
-- Name: gen_fitem_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_fitem_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_fitem_id OWNER TO postgres;

--
-- Name: gen_flan_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_flan_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_flan_id OWNER TO postgres;

--
-- Name: gen_fmov_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_fmov_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_fmov_id OWNER TO postgres;

--
-- Name: gen_frecibo_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_frecibo_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_frecibo_id OWNER TO postgres;

--
-- Name: gen_tmunicipo_id; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gen_tmunicipo_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gen_tmunicipo_id OWNER TO postgres;

--
-- Name: gparametro; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gparametro (
    idparametro public.docod NOT NULL,
    modulo1 public.dom1,
    modulo2 public.dom1,
    modulo3 public.dom1,
    modulo4 public.dom1,
    usarateio public.dom1,
    data public.dodata
);


ALTER TABLE public.gparametro OWNER TO postgres;

--
-- Name: gparametro_idparametro_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.gparametro_idparametro_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gparametro_idparametro_seq OWNER TO postgres;

--
-- Name: gparametro_idparametro_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.gparametro_idparametro_seq OWNED BY public.gparametro.idparametro;


--
-- Name: tcnae_servico; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tcnae_servico (
    cnae character varying(12) NOT NULL,
    cnae_descricao text,
    codigo_item character varying(12) NOT NULL
);


ALTER TABLE public.tcnae_servico OWNER TO postgres;

--
-- Name: tissqn; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.tissqn OWNER TO postgres;

--
-- Name: tissqn_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tissqn_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tissqn_id_seq OWNER TO postgres;

--
-- Name: tissqn_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tissqn_id_seq OWNED BY public.tissqn.id;


--
-- Name: tmp_restaura; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tmp_restaura (
    x text
);


ALTER TABLE public.tmp_restaura OWNER TO postgres;

--
-- Name: tmunicipio_refmunicipio_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tmunicipio_refmunicipio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tmunicipio_refmunicipio_seq OWNER TO postgres;

--
-- Name: tmunicipio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tmunicipio (
    refmunicipio public.docod DEFAULT nextval('public.tmunicipio_refmunicipio_seq'::regclass) NOT NULL,
    nomemunucipio character varying(255),
    unfmunicipio public.dom2,
    paismunicipio character varying(255),
    paiscodigo public.dom5,
    codigoibge character varying(255)
);


ALTER TABLE public.tmunicipio OWNER TO postgres;

--
-- Name: tmunicipio_ref_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tmunicipio_ref_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tmunicipio_ref_seq OWNER TO postgres;

--
-- Name: tncm; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tncm (
    codigo character varying(255) NOT NULL,
    descricao text
);


ALTER TABLE public.tncm OWNER TO postgres;

--
-- Name: tservico_lc116; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tservico_lc116 (
    codigo_item character varying(12) NOT NULL,
    descricao text
);


ALTER TABLE public.tservico_lc116 OWNER TO postgres;

--
-- Name: bc_agd_evento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_agd_evento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_agd_evento_id_seq'::regclass);


--
-- Name: bc_ativo_imobilizado id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ativo_imobilizado_id_seq'::regclass);


--
-- Name: bc_ativo_manutencao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ativo_manutencao_id_seq'::regclass);


--
-- Name: bc_bi_dashboard id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_dashboard_id_seq'::regclass);


--
-- Name: bc_bi_dashboard_widget id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_dashboard_widget_id_seq'::regclass);


--
-- Name: bc_bi_indicador id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_indicador ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_indicador_id_seq'::regclass);


--
-- Name: bc_bi_kpi id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_kpi ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_kpi_id_seq'::regclass);


--
-- Name: bc_bi_relatorio id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_relatorio_id_seq'::regclass);


--
-- Name: bc_bi_relatorio_agendado id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_relatorio_agendado_id_seq'::regclass);


--
-- Name: bc_bi_report id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_report_id_seq'::regclass);


--
-- Name: bc_bi_report_parameter id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_bi_report_parameter_id_seq'::regclass);


--
-- Name: bc_cad_base_cep id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_base_cep ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_base_cep_id_seq'::regclass);


--
-- Name: bc_cad_categoria id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_categoria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_categoria_id_seq'::regclass);


--
-- Name: bc_cad_cliente id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_cliente ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_cliente_id_seq'::regclass);


--
-- Name: bc_cad_contato id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_contato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_contato_id_seq'::regclass);


--
-- Name: bc_cad_documento_fiscal id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_documento_fiscal_id_seq'::regclass);


--
-- Name: bc_cad_endereco id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_endereco ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_endereco_id_seq'::regclass);


--
-- Name: bc_cad_fornecedor id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_fornecedor_id_seq'::regclass);


--
-- Name: bc_cad_marca id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_marca ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_marca_id_seq'::regclass);


--
-- Name: bc_cad_municipio id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_municipio ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_municipio_id_seq'::regclass);


--
-- Name: bc_cad_papel id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_papel ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_papel_id_seq'::regclass);


--
-- Name: bc_cad_pessoa id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_pessoa_id_seq'::regclass);


--
-- Name: bc_cad_pessoa_fisica id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_pessoa_fisica_id_seq'::regclass);


--
-- Name: bc_cad_pessoa_juridica id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_pessoa_juridica_id_seq'::regclass);


--
-- Name: bc_cad_produto id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_id_seq'::regclass);


--
-- Name: bc_cad_produto_ecommerce id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_ecommerce_id_seq'::regclass);


--
-- Name: bc_cad_produto_imagem id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_imagem_id_seq'::regclass);


--
-- Name: bc_cad_produto_kit id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_kit_id_seq'::regclass);


--
-- Name: bc_cad_produto_variacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_produto_variacao_id_seq'::regclass);


--
-- Name: bc_cad_servico id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_servico ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_servico_id_seq'::regclass);


--
-- Name: bc_cad_transportadora id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_transportadora_id_seq'::regclass);


--
-- Name: bc_cad_unidade_medida id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_cad_unidade_medida_id_seq'::regclass);


--
-- Name: bc_com_conferencia_fatura id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_conferencia_fatura_id_seq'::regclass);


--
-- Name: bc_com_contrato id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_contrato_id_seq'::regclass);


--
-- Name: bc_com_contrato_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_contrato_item_id_seq'::regclass);


--
-- Name: bc_com_cotacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_cotacao_id_seq'::regclass);


--
-- Name: bc_com_cotacao_fornecedor id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_cotacao_fornecedor_id_seq'::regclass);


--
-- Name: bc_com_cotacao_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_cotacao_item_id_seq'::regclass);


--
-- Name: bc_com_pedido id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_pedido_id_seq'::regclass);


--
-- Name: bc_com_pedido_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_pedido_item_id_seq'::regclass);


--
-- Name: bc_com_recebimento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_recebimento_id_seq'::regclass);


--
-- Name: bc_com_recebimento_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_recebimento_item_id_seq'::regclass);


--
-- Name: bc_com_solicitacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_solicitacao_id_seq'::regclass);


--
-- Name: bc_com_solicitacao_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_com_solicitacao_item_id_seq'::regclass);


--
-- Name: bc_core_auditoria id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_auditoria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_auditoria_id_seq'::regclass);


--
-- Name: bc_core_configuracao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_configuracao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_configuracao_id_seq'::regclass);


--
-- Name: bc_core_documento_fluxo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_documento_fluxo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_documento_fluxo_id_seq'::regclass);


--
-- Name: bc_core_empresa id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_empresa_id_seq'::regclass);


--
-- Name: bc_core_empresa_vinculo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_empresa_vinculo_id_seq'::regclass);


--
-- Name: bc_core_integration_event id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_integration_event ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_integration_event_id_seq'::regclass);


--
-- Name: bc_core_log_acesso id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_log_acesso_id_seq'::regclass);


--
-- Name: bc_core_modulo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_modulo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_modulo_id_seq'::regclass);


--
-- Name: bc_core_notificacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_notificacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_notificacao_id_seq'::regclass);


--
-- Name: bc_core_perfil id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_perfil_id_seq'::regclass);


--
-- Name: bc_core_perfil_permissao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_perfil_permissao_id_seq'::regclass);


--
-- Name: bc_core_permissao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_permissao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_permissao_id_seq'::regclass);


--
-- Name: bc_core_sessao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_sessao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_sessao_id_seq'::regclass);


--
-- Name: bc_core_usuario id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_id_seq'::regclass);


--
-- Name: bc_core_usuario_empresa id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_empresa_id_seq'::regclass);


--
-- Name: bc_core_usuario_modulo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_modulo_id_seq'::regclass);


--
-- Name: bc_core_usuario_perfil id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_core_usuario_perfil_id_seq'::regclass);


--
-- Name: bc_crm_atividade id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_atividade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_crm_atividade_id_seq'::regclass);


--
-- Name: bc_crm_oportunidade id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_crm_oportunidade_id_seq'::regclass);


--
-- Name: bc_est_deposito id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_deposito ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_deposito_id_seq'::regclass);


--
-- Name: bc_est_endereco id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_endereco ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_endereco_id_seq'::regclass);


--
-- Name: bc_est_expedicao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_expedicao_id_seq'::regclass);


--
-- Name: bc_est_expedicao_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_expedicao_item_id_seq'::regclass);


--
-- Name: bc_est_inventario id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_inventario_id_seq'::regclass);


--
-- Name: bc_est_inventario_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_inventario_item_id_seq'::regclass);


--
-- Name: bc_est_lote id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_lote ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_lote_id_seq'::regclass);


--
-- Name: bc_est_movimentacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_movimentacao_id_seq'::regclass);


--
-- Name: bc_est_reserva id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_reserva_id_seq'::regclass);


--
-- Name: bc_est_saldo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_saldo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_saldo_id_seq'::regclass);


--
-- Name: bc_est_serie id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_serie ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_serie_id_seq'::regclass);


--
-- Name: bc_est_transferencia id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_transferencia_id_seq'::regclass);


--
-- Name: bc_est_transferencia_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_est_transferencia_item_id_seq'::regclass);


--
-- Name: bc_fin_analise_rentabilidade id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_analise_rentabilidade_id_seq'::regclass);


--
-- Name: bc_fin_aplicacao_financeira id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_aplicacao_financeira_id_seq'::regclass);


--
-- Name: bc_fin_aprovacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_aprovacao_id_seq'::regclass);


--
-- Name: bc_fin_baixa id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_baixa_id_seq'::regclass);


--
-- Name: bc_fin_boleto id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_boleto ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_boleto_id_seq'::regclass);


--
-- Name: bc_fin_caixa id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_caixa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_caixa_id_seq'::regclass);


--
-- Name: bc_fin_caixa_movimento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_caixa_movimento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_caixa_movimento_id_seq'::regclass);


--
-- Name: bc_fin_centro_custo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_centro_custo_id_seq'::regclass);


--
-- Name: bc_fin_cobranca_acao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_cobranca_acao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_cobranca_acao_id_seq'::regclass);


--
-- Name: bc_fin_conciliacao_bancaria id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_conciliacao_bancaria_id_seq'::regclass);


--
-- Name: bc_fin_conciliacao_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_conciliacao_item_id_seq'::regclass);


--
-- Name: bc_fin_condicao_pagamento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_condicao_pagamento_id_seq'::regclass);


--
-- Name: bc_fin_conta_bancaria id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_conta_bancaria_id_seq'::regclass);


--
-- Name: bc_fin_emprestimo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_emprestimo_id_seq'::regclass);


--
-- Name: bc_fin_extrato id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_extrato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_extrato_id_seq'::regclass);


--
-- Name: bc_fin_fluxo_aprovacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_fluxo_aprovacao_id_seq'::regclass);


--
-- Name: bc_fin_integracao_bancaria id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_integracao_bancaria_id_seq'::regclass);


--
-- Name: bc_fin_lancamento_contabil id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_lancamento_contabil_id_seq'::regclass);


--
-- Name: bc_fin_lancamento_partida id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_lancamento_partida_id_seq'::regclass);


--
-- Name: bc_fin_orcamento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_orcamento_id_seq'::regclass);


--
-- Name: bc_fin_orcamento_realizado id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_orcamento_realizado_id_seq'::regclass);


--
-- Name: bc_fin_periodo_contabil id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_periodo_contabil_id_seq'::regclass);


--
-- Name: bc_fin_plano_contas id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_plano_contas_id_seq'::regclass);


--
-- Name: bc_fin_projecao_fluxo_caixa id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq'::regclass);


--
-- Name: bc_fin_promessa_pagamento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_promessa_pagamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_promessa_pagamento_id_seq'::regclass);


--
-- Name: bc_fin_provisao_pdd id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_provisao_pdd_id_seq'::regclass);


--
-- Name: bc_fin_remessa id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_remessa_id_seq'::regclass);


--
-- Name: bc_fin_remessa_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_remessa_item_id_seq'::regclass);


--
-- Name: bc_fin_renegociacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_renegociacao_id_seq'::regclass);


--
-- Name: bc_fin_retorno_bancario id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_retorno_bancario_id_seq'::regclass);


--
-- Name: bc_fin_retorno_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_retorno_item_id_seq'::regclass);


--
-- Name: bc_fin_stripe_customer id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_stripe_customer_id_seq'::regclass);


--
-- Name: bc_fin_stripe_payment id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_payment ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_stripe_payment_id_seq'::regclass);


--
-- Name: bc_fin_stripe_webhook_event id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_stripe_webhook_event_id_seq'::regclass);


--
-- Name: bc_fin_tipo_pagamento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_tipo_pagamento_id_seq'::regclass);


--
-- Name: bc_fin_titulo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_titulo_id_seq'::regclass);


--
-- Name: bc_fin_titulo_parcela id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fin_titulo_parcela_id_seq'::regclass);


--
-- Name: bc_fis_apuracao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_apuracao_id_seq'::regclass);


--
-- Name: bc_fis_certificado_digital id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_certificado_digital_id_seq'::regclass);


--
-- Name: bc_fis_cest id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cest ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cest_id_seq'::regclass);


--
-- Name: bc_fis_cfop id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cfop ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cfop_id_seq'::regclass);


--
-- Name: bc_fis_cnae_servico id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cnae_servico ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cnae_servico_id_seq'::regclass);


--
-- Name: bc_fis_cte id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cte_id_seq'::regclass);


--
-- Name: bc_fis_cte_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_cte_item_id_seq'::regclass);


--
-- Name: bc_fis_ecd id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecd ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_ecd_id_seq'::regclass);


--
-- Name: bc_fis_ecf id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecf ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_ecf_id_seq'::regclass);


--
-- Name: bc_fis_esocial id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_esocial ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_esocial_id_seq'::regclass);


--
-- Name: bc_fis_imposto id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_imposto ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_imposto_id_seq'::regclass);


--
-- Name: bc_fis_issqn id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_issqn ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_issqn_id_seq'::regclass);


--
-- Name: bc_fis_manifestacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_manifestacao_id_seq'::regclass);


--
-- Name: bc_fis_mdfe id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_mdfe_id_seq'::regclass);


--
-- Name: bc_fis_nbs id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nbs ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nbs_id_seq'::regclass);


--
-- Name: bc_fis_ncm id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ncm ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_ncm_id_seq'::regclass);


--
-- Name: bc_fis_nfce id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfce_id_seq'::regclass);


--
-- Name: bc_fis_nfce_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfce_item_id_seq'::regclass);


--
-- Name: bc_fis_nfe id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfe_id_seq'::regclass);


--
-- Name: bc_fis_nfe_evento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_evento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfe_evento_id_seq'::regclass);


--
-- Name: bc_fis_nfe_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfe_item_id_seq'::regclass);


--
-- Name: bc_fis_nfse id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfse_id_seq'::regclass);


--
-- Name: bc_fis_nfse_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfse_item_id_seq'::regclass);


--
-- Name: bc_fis_nfse_retorno id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_retorno ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_nfse_retorno_id_seq'::regclass);


--
-- Name: bc_fis_palavra_chave id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_palavra_chave ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_palavra_chave_id_seq'::regclass);


--
-- Name: bc_fis_regra_tributaria id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_regra_tributaria_id_seq'::regclass);


--
-- Name: bc_fis_reinf id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_reinf ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_reinf_id_seq'::regclass);


--
-- Name: bc_fis_servico_lc116 id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_servico_lc116 ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_servico_lc116_id_seq'::regclass);


--
-- Name: bc_fis_sped_contribuicoes id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_sped_contribuicoes_id_seq'::regclass);


--
-- Name: bc_fis_sped_fiscal id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_fis_sped_fiscal_id_seq'::regclass);


--
-- Name: bc_hdp_chamado id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_hdp_chamado ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_hdp_chamado_id_seq'::regclass);


--
-- Name: bc_hdp_comentario id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_hdp_comentario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_hdp_comentario_id_seq'::regclass);


--
-- Name: bc_ia_analise_preditiva id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_analise_preditiva_id_seq'::regclass);


--
-- Name: bc_ia_assistente_auditoria id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_assistente_auditoria ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_assistente_auditoria_id_seq'::regclass);


--
-- Name: bc_ia_chat_mensagem id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_mensagem_id_seq'::regclass);


--
-- Name: bc_ia_chat_message id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_message_id_seq'::regclass);


--
-- Name: bc_ia_chat_sessao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_sessao_id_seq'::regclass);


--
-- Name: bc_ia_chat_session id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_chat_session_id_seq'::regclass);


--
-- Name: bc_ia_classificacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_classificacao_id_seq'::regclass);


--
-- Name: bc_ia_config id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_config ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_config_id_seq'::regclass);


--
-- Name: bc_ia_embedding id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_embedding ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_embedding_id_seq'::regclass);


--
-- Name: bc_ia_prompt id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_prompt_id_seq'::regclass);


--
-- Name: bc_ia_prompt_template id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ia_prompt_template_id_seq'::regclass);


--
-- Name: bc_kb_artigo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_kb_artigo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_kb_artigo_id_seq'::regclass);


--
-- Name: bc_migration_log id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_migration_log ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_migration_log_id_seq'::regclass);


--
-- Name: bc_prod_apontamento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_apontamento_id_seq'::regclass);


--
-- Name: bc_prod_centro_trabalho id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_centro_trabalho_id_seq'::regclass);


--
-- Name: bc_prod_estrutura id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_estrutura_id_seq'::regclass);


--
-- Name: bc_prod_romaneio id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_romaneio_id_seq'::regclass);


--
-- Name: bc_prod_romaneio_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_romaneio_item_id_seq'::regclass);


--
-- Name: bc_prod_roteiro id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_roteiro_id_seq'::regclass);


--
-- Name: bc_prod_roteiro_operacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_prod_roteiro_operacao_id_seq'::regclass);


--
-- Name: bc_qual_inspecao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_qual_inspecao_id_seq'::regclass);


--
-- Name: bc_qual_nao_conformidade id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_qual_nao_conformidade_id_seq'::regclass);


--
-- Name: bc_qual_plano_inspecao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_qual_plano_inspecao_id_seq'::regclass);


--
-- Name: bc_rh_cargo id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_cargo ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_cargo_id_seq'::regclass);


--
-- Name: bc_rh_folha id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_folha_id_seq'::regclass);


--
-- Name: bc_rh_folha_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_folha_item_id_seq'::regclass);


--
-- Name: bc_rh_funcionario id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_funcionario_id_seq'::regclass);


--
-- Name: bc_rh_rescisao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_rescisao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_rh_rescisao_id_seq'::regclass);


--
-- Name: bc_srv_ordem_servico id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_srv_ordem_servico_id_seq'::regclass);


--
-- Name: bc_srv_os_apontamento id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_srv_os_apontamento_id_seq'::regclass);


--
-- Name: bc_srv_os_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_srv_os_item_id_seq'::regclass);


--
-- Name: bc_ven_bonificacao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_bonificacao_id_seq'::regclass);


--
-- Name: bc_ven_bonificacao_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_bonificacao_item_id_seq'::regclass);


--
-- Name: bc_ven_contrato id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_contrato_id_seq'::regclass);


--
-- Name: bc_ven_contrato_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_contrato_item_id_seq'::regclass);


--
-- Name: bc_ven_devolucao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_devolucao_id_seq'::regclass);


--
-- Name: bc_ven_devolucao_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_devolucao_item_id_seq'::regclass);


--
-- Name: bc_ven_meta id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_meta ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_meta_id_seq'::regclass);


--
-- Name: bc_ven_pedido id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_pedido_id_seq'::regclass);


--
-- Name: bc_ven_pedido_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_pedido_item_id_seq'::regclass);


--
-- Name: bc_ven_regra_comissao id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_regra_comissao_id_seq'::regclass);


--
-- Name: bc_ven_tabela_preco id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_tabela_preco_id_seq'::regclass);


--
-- Name: bc_ven_tabela_preco_item id; Type: DEFAULT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item ALTER COLUMN id SET DEFAULT nextval('brasil_saas.bc_ven_tabela_preco_item_id_seq'::regclass);


--
-- Name: dim_cliente id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_cliente ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_cliente_id_seq'::regclass);


--
-- Name: dim_empresa id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_empresa ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_empresa_id_seq'::regclass);


--
-- Name: dim_produto id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_produto ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_produto_id_seq'::regclass);


--
-- Name: dim_tempo id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_tempo ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.dim_tempo_id_seq'::regclass);


--
-- Name: ft_compras id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_compras ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_compras_id_seq'::regclass);


--
-- Name: ft_estoque id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_estoque ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_estoque_id_seq'::regclass);


--
-- Name: ft_financeiro id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_financeiro ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_financeiro_id_seq'::regclass);


--
-- Name: ft_producao id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_producao ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_producao_id_seq'::regclass);


--
-- Name: ft_vendas id; Type: DEFAULT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_vendas ALTER COLUMN id SET DEFAULT nextval('brasil_saas_dl.ft_vendas_id_seq'::regclass);


--
-- Name: fcaixa codcaixa; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcaixa ALTER COLUMN codcaixa SET DEFAULT nextval('public.fcaixa_codcaixa_seq'::regclass);


--
-- Name: fcentrocusto codcusto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcentrocusto ALTER COLUMN codcusto SET DEFAULT nextval('public.fcentrocusto_codcusto_seq'::regclass);


--
-- Name: fcusto codcusto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcusto ALTER COLUMN codcusto SET DEFAULT nextval('public.fcusto_codcusto_seq'::regclass);


--
-- Name: fdatas iddata; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fdatas ALTER COLUMN iddata SET DEFAULT nextval('public.fdatas_iddata_seq'::regclass);


--
-- Name: fdia refdia; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fdia ALTER COLUMN refdia SET DEFAULT nextval('public.fdia_refdia_seq'::regclass);


--
-- Name: fempresa codigo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fempresa ALTER COLUMN codigo SET DEFAULT nextval('public.fempresa_codigo_seq'::regclass);


--
-- Name: fempresa_vinculo codvinculo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fempresa_vinculo ALTER COLUMN codvinculo SET DEFAULT nextval('public.fempresa_vinculo_codvinculo_seq'::regclass);


--
-- Name: fextrato idlanextrato; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fextrato ALTER COLUMN idlanextrato SET DEFAULT nextval('public.fextrato_idlanextrato_seq'::regclass);


--
-- Name: ffinanceiro codfinanceiro; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ffinanceiro ALTER COLUMN codfinanceiro SET DEFAULT nextval('public.ffinanceiro_codfinanceiro_seq'::regclass);


--
-- Name: ffuncionario codfuncionario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ffuncionario ALTER COLUMN codfuncionario SET DEFAULT nextval('public.ffuncionario_codfuncionario_seq'::regclass);


--
-- Name: fitem refitem; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fitem ALTER COLUMN refitem SET DEFAULT nextval('public.fitem_refitem_seq'::regclass);


--
-- Name: fmov idmov; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmov ALTER COLUMN idmov SET DEFAULT (nextval('public.fmov_idmov_seq'::regclass))::public.docod;


--
-- Name: fmovimento codmovimento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmovimento ALTER COLUMN codmovimento SET DEFAULT nextval('public.fmovimento_codmovimento_seq'::regclass);


--
-- Name: fmovimento_item coditem; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmovimento_item ALTER COLUMN coditem SET DEFAULT nextval('public.fmovimento_item_coditem_seq'::regclass);


--
-- Name: fnota id_nota; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota ALTER COLUMN id_nota SET DEFAULT nextval('public.fnota_id_nota_seq'::regclass);


--
-- Name: fnota_item id_item; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota_item ALTER COLUMN id_item SET DEFAULT nextval('public.fnota_item_id_item_seq'::regclass);


--
-- Name: fpessoa id_pessoa; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fpessoa ALTER COLUMN id_pessoa SET DEFAULT nextval('public.fpessoa_id_pessoa_seq'::regclass);


--
-- Name: fproduto_ecommerce codecommerce; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_ecommerce ALTER COLUMN codecommerce SET DEFAULT nextval('public.fproduto_ecommerce_codecommerce_seq'::regclass);


--
-- Name: fproduto_fiscal codproduto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_fiscal ALTER COLUMN codproduto SET DEFAULT nextval('public.fproduto_fiscal_codproduto_seq'::regclass);


--
-- Name: fproduto_imagem codimagem; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_imagem ALTER COLUMN codimagem SET DEFAULT nextval('public.fproduto_imagem_codimagem_seq'::regclass);


--
-- Name: fproduto_kit codkit; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_kit ALTER COLUMN codkit SET DEFAULT nextval('public.fproduto_kit_codkit_seq'::regclass);


--
-- Name: fproduto_kit codproduto_filho; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_kit ALTER COLUMN codproduto_filho SET DEFAULT nextval('public.fproduto_kit_codproduto_filho_seq'::regclass);


--
-- Name: fproduto_movimento codmov; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_movimento ALTER COLUMN codmov SET DEFAULT nextval('public.fproduto_movimento_codmov_seq'::regclass);


--
-- Name: fproduto_variacao codvariacao; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_variacao ALTER COLUMN codvariacao SET DEFAULT nextval('public.fproduto_variacao_codvariacao_seq'::regclass);


--
-- Name: frecibo idrecibo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.frecibo ALTER COLUMN idrecibo SET DEFAULT nextval('public.frecibo_idrecibo_seq'::regclass);


--
-- Name: ftipo codtipo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ftipo ALTER COLUMN codtipo SET DEFAULT nextval('public.ftipo_codtipo_seq'::regclass);


--
-- Name: fusuario codigo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fusuario ALTER COLUMN codigo SET DEFAULT nextval('public.fusuario_codigo_seq'::regclass);


--
-- Name: fvenda id_venda; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda ALTER COLUMN id_venda SET DEFAULT nextval('public.fvenda_id_venda_seq'::regclass);


--
-- Name: fvenda_item id_item; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda_item ALTER COLUMN id_item SET DEFAULT nextval('public.fvenda_item_id_item_seq'::regclass);


--
-- Name: gparametro idparametro; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gparametro ALTER COLUMN idparametro SET DEFAULT nextval('public.gparametro_idparametro_seq'::regclass);


--
-- Name: tissqn id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tissqn ALTER COLUMN id SET DEFAULT nextval('public.tissqn_id_seq'::regclass);


--
-- Name: bc_agd_evento bc_agd_evento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_agd_evento
    ADD CONSTRAINT bc_agd_evento_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_classe bc_ativo_classe_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_classe
    ADD CONSTRAINT bc_ativo_classe_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_classe bc_ativo_classe_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_classe
    ADD CONSTRAINT bc_ativo_classe_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_depreciacao_execucao bc_ativo_depreciacao_execucao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_depreciacao_execucao
    ADD CONSTRAINT bc_ativo_depreciacao_execucao_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_depreciacao_execucao bc_ativo_depreciacao_execucao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_depreciacao_execucao
    ADD CONSTRAINT bc_ativo_depreciacao_execucao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_imobilizado bc_ativo_imobilizado_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado
    ADD CONSTRAINT bc_ativo_imobilizado_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_imobilizado bc_ativo_imobilizado_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado
    ADD CONSTRAINT bc_ativo_imobilizado_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_manutencao_apontamento bc_ativo_manutencao_apontamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao_apontamento
    ADD CONSTRAINT bc_ativo_manutencao_apontamento_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_manutencao_apontamento bc_ativo_manutencao_apontamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao_apontamento
    ADD CONSTRAINT bc_ativo_manutencao_apontamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_manutencao_material bc_ativo_manutencao_material_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao_material
    ADD CONSTRAINT bc_ativo_manutencao_material_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_manutencao_material bc_ativo_manutencao_material_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao_material
    ADD CONSTRAINT bc_ativo_manutencao_material_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_manutencao bc_ativo_manutencao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao
    ADD CONSTRAINT bc_ativo_manutencao_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_manutencao bc_ativo_manutencao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao
    ADD CONSTRAINT bc_ativo_manutencao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_medicao bc_ativo_medicao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_medicao
    ADD CONSTRAINT bc_ativo_medicao_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_medicao bc_ativo_medicao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_medicao
    ADD CONSTRAINT bc_ativo_medicao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_movimento bc_ativo_movimento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_movimento
    ADD CONSTRAINT bc_ativo_movimento_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_movimento bc_ativo_movimento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_movimento
    ADD CONSTRAINT bc_ativo_movimento_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_nota_manutencao bc_ativo_nota_manutencao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_nota_manutencao
    ADD CONSTRAINT bc_ativo_nota_manutencao_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_nota_manutencao bc_ativo_nota_manutencao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_nota_manutencao
    ADD CONSTRAINT bc_ativo_nota_manutencao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ativo_plano_manutencao bc_ativo_plano_manutencao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_plano_manutencao
    ADD CONSTRAINT bc_ativo_plano_manutencao_pkey PRIMARY KEY (id);


--
-- Name: bc_ativo_plano_manutencao bc_ativo_plano_manutencao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_plano_manutencao
    ADD CONSTRAINT bc_ativo_plano_manutencao_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_dashboard bc_bi_dashboard_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard
    ADD CONSTRAINT bc_bi_dashboard_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_dashboard bc_bi_dashboard_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard
    ADD CONSTRAINT bc_bi_dashboard_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_dashboard_widget bc_bi_dashboard_widget_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget
    ADD CONSTRAINT bc_bi_dashboard_widget_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_dashboard_widget bc_bi_dashboard_widget_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget
    ADD CONSTRAINT bc_bi_dashboard_widget_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_indicador bc_bi_indicador_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_indicador
    ADD CONSTRAINT bc_bi_indicador_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_indicador bc_bi_indicador_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_indicador
    ADD CONSTRAINT bc_bi_indicador_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_kpi bc_bi_kpi_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_kpi
    ADD CONSTRAINT bc_bi_kpi_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_kpi bc_bi_kpi_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_kpi
    ADD CONSTRAINT bc_bi_kpi_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_relatorio_agendado bc_bi_relatorio_agendado_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_relatorio_agendado bc_bi_relatorio_agendado_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_relatorio bc_bi_relatorio_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio
    ADD CONSTRAINT bc_bi_relatorio_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_relatorio bc_bi_relatorio_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio
    ADD CONSTRAINT bc_bi_relatorio_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_report_parameter bc_bi_report_parameter_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter
    ADD CONSTRAINT bc_bi_report_parameter_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_report_parameter bc_bi_report_parameter_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter
    ADD CONSTRAINT bc_bi_report_parameter_uuid_key UNIQUE (uuid);


--
-- Name: bc_bi_report bc_bi_report_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report
    ADD CONSTRAINT bc_bi_report_pkey PRIMARY KEY (id);


--
-- Name: bc_bi_report bc_bi_report_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report
    ADD CONSTRAINT bc_bi_report_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_base_cep bc_cad_base_cep_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_base_cep
    ADD CONSTRAINT bc_cad_base_cep_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_categoria bc_cad_categoria_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_categoria bc_cad_categoria_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_cliente bc_cad_cliente_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_cliente bc_cad_cliente_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_contato bc_cad_contato_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_contato bc_cad_contato_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_documento_fiscal bc_cad_documento_fiscal_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_documento_fiscal bc_cad_documento_fiscal_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_endereco bc_cad_endereco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_endereco bc_cad_endereco_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_fornecedor bc_cad_fornecedor_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_fornecedor bc_cad_fornecedor_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_marca bc_cad_marca_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_marca
    ADD CONSTRAINT bc_cad_marca_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_marca bc_cad_marca_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_marca
    ADD CONSTRAINT bc_cad_marca_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_municipio bc_cad_municipio_codigo_ibge_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_municipio
    ADD CONSTRAINT bc_cad_municipio_codigo_ibge_key UNIQUE (codigo_ibge);


--
-- Name: bc_cad_municipio bc_cad_municipio_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_municipio
    ADD CONSTRAINT bc_cad_municipio_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_papel bc_cad_papel_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT bc_cad_papel_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_papel bc_cad_papel_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT bc_cad_papel_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_pessoa_fisica bc_cad_pessoa_fisica_pessoa_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica
    ADD CONSTRAINT bc_cad_pessoa_fisica_pessoa_id_key UNIQUE (pessoa_id);


--
-- Name: bc_cad_pessoa_fisica bc_cad_pessoa_fisica_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica
    ADD CONSTRAINT bc_cad_pessoa_fisica_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_pessoa_juridica bc_cad_pessoa_juridica_pessoa_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica
    ADD CONSTRAINT bc_cad_pessoa_juridica_pessoa_id_key UNIQUE (pessoa_id);


--
-- Name: bc_cad_pessoa_juridica bc_cad_pessoa_juridica_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica
    ADD CONSTRAINT bc_cad_pessoa_juridica_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_pessoa bc_cad_pessoa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa
    ADD CONSTRAINT bc_cad_pessoa_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_pessoa bc_cad_pessoa_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa
    ADD CONSTRAINT bc_cad_pessoa_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_produto_ecommerce bc_cad_produto_ecommerce_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_produto_ecommerce bc_cad_produto_ecommerce_produto_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_produto_id_key UNIQUE (produto_id);


--
-- Name: bc_cad_produto_imagem bc_cad_produto_imagem_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem
    ADD CONSTRAINT bc_cad_produto_imagem_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_produto_kit bc_cad_produto_kit_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_produto bc_cad_produto_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_produto bc_cad_produto_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_produto_variacao bc_cad_produto_variacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_produto_variacao bc_cad_produto_variacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_servico bc_cad_servico_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_servico bc_cad_servico_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_transportadora bc_cad_transportadora_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_transportadora bc_cad_transportadora_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_uuid_key UNIQUE (uuid);


--
-- Name: bc_cad_unidade_medida bc_cad_unidade_medida_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT bc_cad_unidade_medida_pkey PRIMARY KEY (id);


--
-- Name: bc_cad_unidade_medida bc_cad_unidade_medida_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT bc_cad_unidade_medida_uuid_key UNIQUE (uuid);


--
-- Name: bc_cmp_devolucao_item bc_cmp_devolucao_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao_item
    ADD CONSTRAINT bc_cmp_devolucao_item_pkey PRIMARY KEY (id);


--
-- Name: bc_cmp_devolucao_item bc_cmp_devolucao_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao_item
    ADD CONSTRAINT bc_cmp_devolucao_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_cmp_devolucao bc_cmp_devolucao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao
    ADD CONSTRAINT bc_cmp_devolucao_pkey PRIMARY KEY (id);


--
-- Name: bc_cmp_devolucao bc_cmp_devolucao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao
    ADD CONSTRAINT bc_cmp_devolucao_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_conferencia_fatura_item bc_com_conferencia_fatura_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura_item
    ADD CONSTRAINT bc_com_conferencia_fatura_item_pkey PRIMARY KEY (id);


--
-- Name: bc_com_conferencia_fatura_item bc_com_conferencia_fatura_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura_item
    ADD CONSTRAINT bc_com_conferencia_fatura_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_conferencia_fatura bc_com_conferencia_fatura_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_pkey PRIMARY KEY (id);


--
-- Name: bc_com_conferencia_fatura bc_com_conferencia_fatura_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_contrato_item bc_com_contrato_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato_item
    ADD CONSTRAINT bc_com_contrato_item_pkey PRIMARY KEY (id);


--
-- Name: bc_com_contrato_item bc_com_contrato_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato_item
    ADD CONSTRAINT bc_com_contrato_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_contrato bc_com_contrato_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_pkey PRIMARY KEY (id);


--
-- Name: bc_com_contrato bc_com_contrato_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_cotacao_fornecedor bc_com_cotacao_fornecedor_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_pkey PRIMARY KEY (id);


--
-- Name: bc_com_cotacao_fornecedor bc_com_cotacao_fornecedor_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_cotacao_item bc_com_cotacao_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_pkey PRIMARY KEY (id);


--
-- Name: bc_com_cotacao_item bc_com_cotacao_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_cotacao bc_com_cotacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_pkey PRIMARY KEY (id);


--
-- Name: bc_com_cotacao bc_com_cotacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_pedido_item bc_com_pedido_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_pedido_item_pkey PRIMARY KEY (id);


--
-- Name: bc_com_pedido bc_com_pedido_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_pkey PRIMARY KEY (id);


--
-- Name: bc_com_recebimento_item bc_com_recebimento_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_pkey PRIMARY KEY (id);


--
-- Name: bc_com_recebimento_item bc_com_recebimento_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_recebimento bc_com_recebimento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_pkey PRIMARY KEY (id);


--
-- Name: bc_com_recebimento bc_com_recebimento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_solicitacao_item bc_com_solicitacao_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_pkey PRIMARY KEY (id);


--
-- Name: bc_com_solicitacao_item bc_com_solicitacao_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_com_solicitacao bc_com_solicitacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao
    ADD CONSTRAINT bc_com_solicitacao_pkey PRIMARY KEY (id);


--
-- Name: bc_com_solicitacao bc_com_solicitacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao
    ADD CONSTRAINT bc_com_solicitacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_cont_fechamento_check bc_cont_fechamento_check_empresa_id_periodo_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cont_fechamento_check
    ADD CONSTRAINT bc_cont_fechamento_check_empresa_id_periodo_id_codigo_key UNIQUE (empresa_id, periodo_id, codigo);


--
-- Name: bc_cont_fechamento_check bc_cont_fechamento_check_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cont_fechamento_check
    ADD CONSTRAINT bc_cont_fechamento_check_pkey PRIMARY KEY (id);


--
-- Name: bc_cont_rateio bc_cont_rateio_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cont_rateio
    ADD CONSTRAINT bc_cont_rateio_pkey PRIMARY KEY (id);


--
-- Name: bc_cont_regra_lancamento bc_cont_regra_lancamento_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cont_regra_lancamento
    ADD CONSTRAINT bc_cont_regra_lancamento_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_cont_regra_lancamento bc_cont_regra_lancamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cont_regra_lancamento
    ADD CONSTRAINT bc_cont_regra_lancamento_pkey PRIMARY KEY (id);


--
-- Name: bc_cont_relatorio_snapshot bc_cont_relatorio_snapshot_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cont_relatorio_snapshot
    ADD CONSTRAINT bc_cont_relatorio_snapshot_pkey PRIMARY KEY (id);


--
-- Name: bc_core_auditoria bc_core_auditoria_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_pkey PRIMARY KEY (id);


--
-- Name: bc_core_auditoria bc_core_auditoria_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_auth_source bc_core_auth_source_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_auth_source
    ADD CONSTRAINT bc_core_auth_source_pkey PRIMARY KEY (username);


--
-- Name: bc_core_banco bc_core_banco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_banco
    ADD CONSTRAINT bc_core_banco_pkey PRIMARY KEY (compe);


--
-- Name: bc_core_configuracao bc_core_configuracao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT bc_core_configuracao_pkey PRIMARY KEY (id);


--
-- Name: bc_core_configuracao bc_core_configuracao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT bc_core_configuracao_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_documento_fluxo bc_core_documento_fluxo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_documento_fluxo
    ADD CONSTRAINT bc_core_documento_fluxo_pkey PRIMARY KEY (id);


--
-- Name: bc_core_empresa bc_core_empresa_cnpj_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT bc_core_empresa_cnpj_key UNIQUE (cnpj);


--
-- Name: bc_core_empresa bc_core_empresa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT bc_core_empresa_pkey PRIMARY KEY (id);


--
-- Name: bc_core_empresa bc_core_empresa_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT bc_core_empresa_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_empresa_vinculo bc_core_empresa_vinculo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_pkey PRIMARY KEY (id);


--
-- Name: bc_core_empresa_vinculo bc_core_empresa_vinculo_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_integration_event bc_core_integration_event_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_integration_event
    ADD CONSTRAINT bc_core_integration_event_pkey PRIMARY KEY (id);


--
-- Name: bc_core_integration_event bc_core_integration_event_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_integration_event
    ADD CONSTRAINT bc_core_integration_event_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_log_acesso bc_core_log_acesso_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso
    ADD CONSTRAINT bc_core_log_acesso_pkey PRIMARY KEY (id);


--
-- Name: bc_core_modulo bc_core_modulo_chave_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_modulo
    ADD CONSTRAINT bc_core_modulo_chave_key UNIQUE (chave);


--
-- Name: bc_core_modulo bc_core_modulo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_modulo
    ADD CONSTRAINT bc_core_modulo_pkey PRIMARY KEY (id);


--
-- Name: bc_core_modulo bc_core_modulo_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_modulo
    ADD CONSTRAINT bc_core_modulo_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_notificacao bc_core_notificacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_pkey PRIMARY KEY (id);


--
-- Name: bc_core_notificacao bc_core_notificacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_perfil_permissao bc_core_perfil_permissao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT bc_core_perfil_permissao_pkey PRIMARY KEY (id);


--
-- Name: bc_core_perfil bc_core_perfil_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT bc_core_perfil_pkey PRIMARY KEY (id);


--
-- Name: bc_core_perfil bc_core_perfil_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT bc_core_perfil_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_permissao bc_core_permissao_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_permissao
    ADD CONSTRAINT bc_core_permissao_codigo_key UNIQUE (codigo);


--
-- Name: bc_core_permissao bc_core_permissao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_permissao
    ADD CONSTRAINT bc_core_permissao_pkey PRIMARY KEY (id);


--
-- Name: bc_core_permissao bc_core_permissao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_permissao
    ADD CONSTRAINT bc_core_permissao_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_sessao bc_core_sessao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_sessao
    ADD CONSTRAINT bc_core_sessao_pkey PRIMARY KEY (id);


--
-- Name: bc_core_sessao bc_core_sessao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_sessao
    ADD CONSTRAINT bc_core_sessao_uuid_key UNIQUE (uuid);


--
-- Name: bc_core_usuario bc_core_usuario_email_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_email_key UNIQUE (email);


--
-- Name: bc_core_usuario_empresa bc_core_usuario_empresa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT bc_core_usuario_empresa_pkey PRIMARY KEY (id);


--
-- Name: bc_core_usuario_modulo bc_core_usuario_modulo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT bc_core_usuario_modulo_pkey PRIMARY KEY (id);


--
-- Name: bc_core_usuario_perfil bc_core_usuario_perfil_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT bc_core_usuario_perfil_pkey PRIMARY KEY (id);


--
-- Name: bc_core_usuario bc_core_usuario_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_pkey PRIMARY KEY (id);


--
-- Name: bc_core_usuario bc_core_usuario_username_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_username_key UNIQUE (username);


--
-- Name: bc_core_usuario bc_core_usuario_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_uuid_key UNIQUE (uuid);


--
-- Name: bc_crm_atividade bc_crm_atividade_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_pkey PRIMARY KEY (id);


--
-- Name: bc_crm_atividade bc_crm_atividade_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_uuid_key UNIQUE (uuid);


--
-- Name: bc_crm_lead bc_crm_lead_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_lead
    ADD CONSTRAINT bc_crm_lead_pkey PRIMARY KEY (id);


--
-- Name: bc_crm_lead bc_crm_lead_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_lead
    ADD CONSTRAINT bc_crm_lead_uuid_key UNIQUE (uuid);


--
-- Name: bc_crm_oportunidade bc_crm_oportunidade_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_pkey PRIMARY KEY (id);


--
-- Name: bc_crm_oportunidade bc_crm_oportunidade_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_uuid_key UNIQUE (uuid);


--
-- Name: bc_crm_tarefa bc_crm_tarefa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_tarefa
    ADD CONSTRAINT bc_crm_tarefa_pkey PRIMARY KEY (id);


--
-- Name: bc_crm_tarefa bc_crm_tarefa_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_tarefa
    ADD CONSTRAINT bc_crm_tarefa_uuid_key UNIQUE (uuid);


--
-- Name: bc_ctb_fechamento bc_ctb_fechamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_fechamento
    ADD CONSTRAINT bc_ctb_fechamento_pkey PRIMARY KEY (id);


--
-- Name: bc_ctb_fechamento bc_ctb_fechamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_fechamento
    ADD CONSTRAINT bc_ctb_fechamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_ctb_lancamento bc_ctb_lancamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_lancamento
    ADD CONSTRAINT bc_ctb_lancamento_pkey PRIMARY KEY (id);


--
-- Name: bc_ctb_lancamento bc_ctb_lancamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_lancamento
    ADD CONSTRAINT bc_ctb_lancamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_ctb_partida bc_ctb_partida_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_partida
    ADD CONSTRAINT bc_ctb_partida_pkey PRIMARY KEY (id);


--
-- Name: bc_ctb_partida bc_ctb_partida_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_partida
    ADD CONSTRAINT bc_ctb_partida_uuid_key UNIQUE (uuid);


--
-- Name: bc_dms_aprovacao bc_dms_aprovacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_aprovacao
    ADD CONSTRAINT bc_dms_aprovacao_pkey PRIMARY KEY (id);


--
-- Name: bc_dms_aprovacao bc_dms_aprovacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_aprovacao
    ADD CONSTRAINT bc_dms_aprovacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_dms_documento bc_dms_documento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_documento
    ADD CONSTRAINT bc_dms_documento_pkey PRIMARY KEY (id);


--
-- Name: bc_dms_documento bc_dms_documento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_documento
    ADD CONSTRAINT bc_dms_documento_uuid_key UNIQUE (uuid);


--
-- Name: bc_dms_versao bc_dms_versao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT bc_dms_versao_pkey PRIMARY KEY (id);


--
-- Name: bc_dms_versao bc_dms_versao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT bc_dms_versao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ehs_acao bc_ehs_acao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_acao
    ADD CONSTRAINT bc_ehs_acao_pkey PRIMARY KEY (id);


--
-- Name: bc_ehs_inspecao bc_ehs_inspecao_empresa_id_numero_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_inspecao
    ADD CONSTRAINT bc_ehs_inspecao_empresa_id_numero_key UNIQUE (empresa_id, numero);


--
-- Name: bc_ehs_inspecao bc_ehs_inspecao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_inspecao
    ADD CONSTRAINT bc_ehs_inspecao_pkey PRIMARY KEY (id);


--
-- Name: bc_ehs_ocorrencia bc_ehs_ocorrencia_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_ocorrencia
    ADD CONSTRAINT bc_ehs_ocorrencia_pkey PRIMARY KEY (id);


--
-- Name: bc_ehs_ocorrencia bc_ehs_ocorrencia_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_ocorrencia
    ADD CONSTRAINT bc_ehs_ocorrencia_uuid_key UNIQUE (uuid);


--
-- Name: bc_ehs_permissao_trabalho bc_ehs_permissao_trabalho_empresa_id_numero_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_permissao_trabalho
    ADD CONSTRAINT bc_ehs_permissao_trabalho_empresa_id_numero_key UNIQUE (empresa_id, numero);


--
-- Name: bc_ehs_permissao_trabalho bc_ehs_permissao_trabalho_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_permissao_trabalho
    ADD CONSTRAINT bc_ehs_permissao_trabalho_pkey PRIMARY KEY (id);


--
-- Name: bc_ehs_risco bc_ehs_risco_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_risco
    ADD CONSTRAINT bc_ehs_risco_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_ehs_risco bc_ehs_risco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_risco
    ADD CONSTRAINT bc_ehs_risco_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_cenario_tributario bc_ent_cenario_tributario_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_cenario_tributario
    ADD CONSTRAINT bc_ent_cenario_tributario_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_cenario_tributario bc_ent_cenario_tributario_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_cenario_tributario
    ADD CONSTRAINT bc_ent_cenario_tributario_uuid_key UNIQUE (uuid);


--
-- Name: bc_ent_contrato bc_ent_contrato_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_contrato
    ADD CONSTRAINT bc_ent_contrato_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_contrato bc_ent_contrato_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_contrato
    ADD CONSTRAINT bc_ent_contrato_uuid_key UNIQUE (uuid);


--
-- Name: bc_ent_fornecedor_qualificacao bc_ent_fornecedor_qualificacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_fornecedor_qualificacao
    ADD CONSTRAINT bc_ent_fornecedor_qualificacao_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_fornecedor_qualificacao bc_ent_fornecedor_qualificacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_fornecedor_qualificacao
    ADD CONSTRAINT bc_ent_fornecedor_qualificacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ent_meta_comercial bc_ent_meta_comercial_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_meta_comercial
    ADD CONSTRAINT bc_ent_meta_comercial_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_meta_comercial bc_ent_meta_comercial_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_meta_comercial
    ADD CONSTRAINT bc_ent_meta_comercial_uuid_key UNIQUE (uuid);


--
-- Name: bc_ent_orcamento bc_ent_orcamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_orcamento
    ADD CONSTRAINT bc_ent_orcamento_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_orcamento bc_ent_orcamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_orcamento
    ADD CONSTRAINT bc_ent_orcamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_ent_periodo_contabil bc_ent_periodo_contabil_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_periodo_contabil
    ADD CONSTRAINT bc_ent_periodo_contabil_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_periodo_contabil bc_ent_periodo_contabil_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_periodo_contabil
    ADD CONSTRAINT bc_ent_periodo_contabil_uuid_key UNIQUE (uuid);


--
-- Name: bc_ent_tesouraria_previsao bc_ent_tesouraria_previsao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_tesouraria_previsao
    ADD CONSTRAINT bc_ent_tesouraria_previsao_pkey PRIMARY KEY (id);


--
-- Name: bc_ent_tesouraria_previsao bc_ent_tesouraria_previsao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_tesouraria_previsao
    ADD CONSTRAINT bc_ent_tesouraria_previsao_uuid_key UNIQUE (uuid);


--
-- Name: bc_esocial_evento bc_esocial_evento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_esocial_evento
    ADD CONSTRAINT bc_esocial_evento_pkey PRIMARY KEY (id);


--
-- Name: bc_esocial_evento bc_esocial_evento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_esocial_evento
    ADD CONSTRAINT bc_esocial_evento_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_deposito bc_est_deposito_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_deposito
    ADD CONSTRAINT bc_est_deposito_pkey PRIMARY KEY (id);


--
-- Name: bc_est_deposito bc_est_deposito_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_deposito
    ADD CONSTRAINT bc_est_deposito_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_endereco bc_est_endereco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_pkey PRIMARY KEY (id);


--
-- Name: bc_est_endereco bc_est_endereco_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_pkey PRIMARY KEY (id);


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_expedicao bc_est_expedicao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_pkey PRIMARY KEY (id);


--
-- Name: bc_est_expedicao bc_est_expedicao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_inventario_item bc_est_inventario_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_pkey PRIMARY KEY (id);


--
-- Name: bc_est_inventario bc_est_inventario_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_pkey PRIMARY KEY (id);


--
-- Name: bc_est_inventario bc_est_inventario_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_lote bc_est_lote_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_pkey PRIMARY KEY (id);


--
-- Name: bc_est_lote bc_est_lote_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_movimentacao bc_est_movimentacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_movimentacao_pkey PRIMARY KEY (id);


--
-- Name: bc_est_reserva bc_est_reserva_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_pkey PRIMARY KEY (id);


--
-- Name: bc_est_reserva bc_est_reserva_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_saldo bc_est_saldo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_pkey PRIMARY KEY (id);


--
-- Name: bc_est_serie bc_est_serie_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_pkey PRIMARY KEY (id);


--
-- Name: bc_est_serie bc_est_serie_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_transferencia_item bc_est_transferencia_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_pkey PRIMARY KEY (id);


--
-- Name: bc_est_transferencia_item bc_est_transferencia_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_est_transferencia bc_est_transferencia_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_pkey PRIMARY KEY (id);


--
-- Name: bc_est_transferencia bc_est_transferencia_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_analise_rentabilidade bc_fin_analise_rentabilidade_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_analise_rentabilidade bc_fin_analise_rentabilidade_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_aplicacao_financeira bc_fin_aplicacao_financeira_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_aplicacao_financeira bc_fin_aplicacao_financeira_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_aprovacao bc_fin_aprovacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_aprovacao bc_fin_aprovacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_baixa bc_fin_baixa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_baixa bc_fin_baixa_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_boleto bc_fin_boleto_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_boleto bc_fin_boleto_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_caixa_movimento bc_fin_caixa_movimento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_caixa_movimento
    ADD CONSTRAINT bc_fin_caixa_movimento_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_caixa bc_fin_caixa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_caixa
    ADD CONSTRAINT bc_fin_caixa_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_centro_custo bc_fin_centro_custo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_centro_custo bc_fin_centro_custo_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_cobranca_acao bc_fin_cobranca_acao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_cobranca_acao
    ADD CONSTRAINT bc_fin_cobranca_acao_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_comissao bc_fin_comissao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_comissao
    ADD CONSTRAINT bc_fin_comissao_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_conciliacao_bancaria bc_fin_conciliacao_bancaria_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_conciliacao_bancaria bc_fin_conciliacao_bancaria_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_conciliacao_item bc_fin_conciliacao_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_conciliacao_item bc_fin_conciliacao_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_condicao_pagamento bc_fin_condicao_pagamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento
    ADD CONSTRAINT bc_fin_condicao_pagamento_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_condicao_pagamento bc_fin_condicao_pagamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento
    ADD CONSTRAINT bc_fin_condicao_pagamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_consolidacao bc_fin_consolidacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_consolidacao
    ADD CONSTRAINT bc_fin_consolidacao_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_consolidacao bc_fin_consolidacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_consolidacao
    ADD CONSTRAINT bc_fin_consolidacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_conta_bancaria bc_fin_conta_bancaria_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria
    ADD CONSTRAINT bc_fin_conta_bancaria_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_conta_bancaria bc_fin_conta_bancaria_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria
    ADD CONSTRAINT bc_fin_conta_bancaria_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_emprestimo bc_fin_emprestimo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo
    ADD CONSTRAINT bc_fin_emprestimo_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_emprestimo bc_fin_emprestimo_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo
    ADD CONSTRAINT bc_fin_emprestimo_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_extrato bc_fin_extrato_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_extrato bc_fin_extrato_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_fluxo_aprovacao bc_fin_fluxo_aprovacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao
    ADD CONSTRAINT bc_fin_fluxo_aprovacao_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_fluxo_aprovacao bc_fin_fluxo_aprovacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao
    ADD CONSTRAINT bc_fin_fluxo_aprovacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_integracao_bancaria bc_fin_integracao_bancaria_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_integracao_bancaria bc_fin_integracao_bancaria_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_intercompany bc_fin_intercompany_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_intercompany
    ADD CONSTRAINT bc_fin_intercompany_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_intercompany bc_fin_intercompany_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_intercompany
    ADD CONSTRAINT bc_fin_intercompany_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_lancamento_contabil bc_fin_lancamento_contabil_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_lancamento_contabil bc_fin_lancamento_contabil_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_lancamento_partida bc_fin_lancamento_partida_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_lancamento_partida bc_fin_lancamento_partida_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_orcamento bc_fin_orcamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_orcamento_realizado bc_fin_orcamento_realizado_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_orcamento_realizado bc_fin_orcamento_realizado_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_orcamento bc_fin_orcamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_periodo_contabil bc_fin_periodo_contabil_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil
    ADD CONSTRAINT bc_fin_periodo_contabil_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_periodo_contabil bc_fin_periodo_contabil_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil
    ADD CONSTRAINT bc_fin_periodo_contabil_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_plano_contas bc_fin_plano_contas_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_plano_contas bc_fin_plano_contas_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_projecao_fluxo_caixa bc_fin_projecao_fluxo_caixa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa
    ADD CONSTRAINT bc_fin_projecao_fluxo_caixa_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_projecao_fluxo_caixa bc_fin_projecao_fluxo_caixa_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa
    ADD CONSTRAINT bc_fin_projecao_fluxo_caixa_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_promessa_pagamento bc_fin_promessa_pagamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_promessa_pagamento
    ADD CONSTRAINT bc_fin_promessa_pagamento_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_provisao_pdd bc_fin_provisao_pdd_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd
    ADD CONSTRAINT bc_fin_provisao_pdd_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_provisao_pdd bc_fin_provisao_pdd_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd
    ADD CONSTRAINT bc_fin_provisao_pdd_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_remessa_item bc_fin_remessa_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_remessa bc_fin_remessa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_remessa bc_fin_remessa_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_renegociacao bc_fin_renegociacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_renegociacao bc_fin_renegociacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_retorno_bancario bc_fin_retorno_bancario_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_retorno_bancario bc_fin_retorno_bancario_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_retorno_item bc_fin_retorno_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_stripe_customer bc_fin_stripe_customer_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT bc_fin_stripe_customer_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_stripe_customer bc_fin_stripe_customer_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT bc_fin_stripe_customer_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_stripe_payment bc_fin_stripe_payment_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_payment
    ADD CONSTRAINT bc_fin_stripe_payment_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_stripe_payment bc_fin_stripe_payment_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_payment
    ADD CONSTRAINT bc_fin_stripe_payment_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_stripe_webhook_event bc_fin_stripe_webhook_event_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event
    ADD CONSTRAINT bc_fin_stripe_webhook_event_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_stripe_webhook_event bc_fin_stripe_webhook_event_stripe_event_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event
    ADD CONSTRAINT bc_fin_stripe_webhook_event_stripe_event_id_key UNIQUE (stripe_event_id);


--
-- Name: bc_fin_stripe_webhook_event bc_fin_stripe_webhook_event_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_webhook_event
    ADD CONSTRAINT bc_fin_stripe_webhook_event_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_tipo_pagamento bc_fin_tipo_pagamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento
    ADD CONSTRAINT bc_fin_tipo_pagamento_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_tipo_pagamento bc_fin_tipo_pagamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento
    ADD CONSTRAINT bc_fin_tipo_pagamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_titulo_parcela bc_fin_titulo_parcela_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_titulo_parcela bc_fin_titulo_parcela_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_uuid_key UNIQUE (uuid);


--
-- Name: bc_fin_titulo bc_fin_titulo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_pkey PRIMARY KEY (id);


--
-- Name: bc_fin_titulo bc_fin_titulo_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_apuracao bc_fis_apuracao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_apuracao bc_fis_apuracao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_certificado_digital bc_fis_certificado_digital_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital
    ADD CONSTRAINT bc_fis_certificado_digital_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_certificado_digital bc_fis_certificado_digital_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital
    ADD CONSTRAINT bc_fis_certificado_digital_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_cest bc_fis_cest_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cest
    ADD CONSTRAINT bc_fis_cest_codigo_key UNIQUE (codigo);


--
-- Name: bc_fis_cest bc_fis_cest_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cest
    ADD CONSTRAINT bc_fis_cest_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_cfop bc_fis_cfop_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cfop
    ADD CONSTRAINT bc_fis_cfop_codigo_key UNIQUE (codigo);


--
-- Name: bc_fis_cfop bc_fis_cfop_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cfop
    ADD CONSTRAINT bc_fis_cfop_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_cnae_servico bc_fis_cnae_servico_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cnae_servico
    ADD CONSTRAINT bc_fis_cnae_servico_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_cte_item bc_fis_cte_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte_item
    ADD CONSTRAINT bc_fis_cte_item_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_cte bc_fis_cte_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_cte bc_fis_cte_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_ecd bc_fis_ecd_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecd
    ADD CONSTRAINT bc_fis_ecd_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_ecd bc_fis_ecd_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecd
    ADD CONSTRAINT bc_fis_ecd_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_ecf bc_fis_ecf_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecf
    ADD CONSTRAINT bc_fis_ecf_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_ecf bc_fis_ecf_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecf
    ADD CONSTRAINT bc_fis_ecf_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_esocial bc_fis_esocial_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_esocial
    ADD CONSTRAINT bc_fis_esocial_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_esocial bc_fis_esocial_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_esocial
    ADD CONSTRAINT bc_fis_esocial_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_imposto bc_fis_imposto_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT bc_fis_imposto_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_imposto bc_fis_imposto_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT bc_fis_imposto_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_issqn bc_fis_issqn_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_issqn
    ADD CONSTRAINT bc_fis_issqn_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_manifestacao bc_fis_manifestacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao
    ADD CONSTRAINT bc_fis_manifestacao_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_manifestacao bc_fis_manifestacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao
    ADD CONSTRAINT bc_fis_manifestacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_mdfe bc_fis_mdfe_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe
    ADD CONSTRAINT bc_fis_mdfe_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_mdfe bc_fis_mdfe_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe
    ADD CONSTRAINT bc_fis_mdfe_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_nbs bc_fis_nbs_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nbs
    ADD CONSTRAINT bc_fis_nbs_codigo_key UNIQUE (codigo);


--
-- Name: bc_fis_nbs bc_fis_nbs_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nbs
    ADD CONSTRAINT bc_fis_nbs_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_ncm bc_fis_ncm_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ncm
    ADD CONSTRAINT bc_fis_ncm_codigo_key UNIQUE (codigo);


--
-- Name: bc_fis_ncm bc_fis_ncm_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ncm
    ADD CONSTRAINT bc_fis_ncm_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfce_item bc_fis_nfce_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item
    ADD CONSTRAINT bc_fis_nfce_item_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfce bc_fis_nfce_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfce bc_fis_nfce_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_nfe_evento bc_fis_nfe_evento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_evento
    ADD CONSTRAINT bc_fis_nfe_evento_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfe_item bc_fis_nfe_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item
    ADD CONSTRAINT bc_fis_nfe_item_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfe bc_fis_nfe_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfe bc_fis_nfe_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_nfse_item bc_fis_nfse_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item
    ADD CONSTRAINT bc_fis_nfse_item_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfse bc_fis_nfse_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfse_retorno bc_fis_nfse_retorno_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_retorno
    ADD CONSTRAINT bc_fis_nfse_retorno_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_nfse bc_fis_nfse_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_obrigacao_entrega bc_fis_obrigacao_entrega_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT bc_fis_obrigacao_entrega_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_obrigacao_entrega bc_fis_obrigacao_entrega_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT bc_fis_obrigacao_entrega_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_obrigacao bc_fis_obrigacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao
    ADD CONSTRAINT bc_fis_obrigacao_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_obrigacao bc_fis_obrigacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao
    ADD CONSTRAINT bc_fis_obrigacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_palavra_chave bc_fis_palavra_chave_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_palavra_chave
    ADD CONSTRAINT bc_fis_palavra_chave_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_regra_tributaria bc_fis_regra_tributaria_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria
    ADD CONSTRAINT bc_fis_regra_tributaria_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_regra_tributaria bc_fis_regra_tributaria_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria
    ADD CONSTRAINT bc_fis_regra_tributaria_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_reinf bc_fis_reinf_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_reinf
    ADD CONSTRAINT bc_fis_reinf_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_reinf bc_fis_reinf_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_reinf
    ADD CONSTRAINT bc_fis_reinf_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_servico_lc116 bc_fis_servico_lc116_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_servico_lc116
    ADD CONSTRAINT bc_fis_servico_lc116_codigo_key UNIQUE (codigo);


--
-- Name: bc_fis_servico_lc116 bc_fis_servico_lc116_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_servico_lc116
    ADD CONSTRAINT bc_fis_servico_lc116_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_sped_contribuicoes bc_fis_sped_contribuicoes_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes
    ADD CONSTRAINT bc_fis_sped_contribuicoes_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_sped_contribuicoes bc_fis_sped_contribuicoes_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes
    ADD CONSTRAINT bc_fis_sped_contribuicoes_uuid_key UNIQUE (uuid);


--
-- Name: bc_fis_sped_fiscal bc_fis_sped_fiscal_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal
    ADD CONSTRAINT bc_fis_sped_fiscal_pkey PRIMARY KEY (id);


--
-- Name: bc_fis_sped_fiscal bc_fis_sped_fiscal_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal
    ADD CONSTRAINT bc_fis_sped_fiscal_uuid_key UNIQUE (uuid);


--
-- Name: bc_gov_controle bc_gov_controle_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_gov_controle
    ADD CONSTRAINT bc_gov_controle_pkey PRIMARY KEY (id);


--
-- Name: bc_gov_controle bc_gov_controle_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_gov_controle
    ADD CONSTRAINT bc_gov_controle_uuid_key UNIQUE (uuid);


--
-- Name: bc_gov_risco bc_gov_risco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_gov_risco
    ADD CONSTRAINT bc_gov_risco_pkey PRIMARY KEY (id);


--
-- Name: bc_gov_risco bc_gov_risco_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_gov_risco
    ADD CONSTRAINT bc_gov_risco_uuid_key UNIQUE (uuid);


--
-- Name: bc_grc_avaliacao bc_grc_avaliacao_empresa_id_risco_id_periodo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_avaliacao
    ADD CONSTRAINT bc_grc_avaliacao_empresa_id_risco_id_periodo_key UNIQUE (empresa_id, risco_id, periodo);


--
-- Name: bc_grc_avaliacao bc_grc_avaliacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_avaliacao
    ADD CONSTRAINT bc_grc_avaliacao_pkey PRIMARY KEY (id);


--
-- Name: bc_grc_controle bc_grc_controle_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_controle
    ADD CONSTRAINT bc_grc_controle_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_grc_controle bc_grc_controle_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_controle
    ADD CONSTRAINT bc_grc_controle_pkey PRIMARY KEY (id);


--
-- Name: bc_grc_controle bc_grc_controle_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_controle
    ADD CONSTRAINT bc_grc_controle_uuid_key UNIQUE (uuid);


--
-- Name: bc_grc_evidencia bc_grc_evidencia_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_evidencia
    ADD CONSTRAINT bc_grc_evidencia_pkey PRIMARY KEY (id);


--
-- Name: bc_grc_log bc_grc_log_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_log
    ADD CONSTRAINT bc_grc_log_pkey PRIMARY KEY (id);


--
-- Name: bc_grc_plano_acao bc_grc_plano_acao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_plano_acao
    ADD CONSTRAINT bc_grc_plano_acao_pkey PRIMARY KEY (id);


--
-- Name: bc_grc_plano_acao bc_grc_plano_acao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_plano_acao
    ADD CONSTRAINT bc_grc_plano_acao_uuid_key UNIQUE (uuid);


--
-- Name: bc_grc_risco_controle bc_grc_risco_controle_empresa_id_risco_id_controle_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_risco_controle
    ADD CONSTRAINT bc_grc_risco_controle_empresa_id_risco_id_controle_id_key UNIQUE (empresa_id, risco_id, controle_id);


--
-- Name: bc_grc_risco_controle bc_grc_risco_controle_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_risco_controle
    ADD CONSTRAINT bc_grc_risco_controle_pkey PRIMARY KEY (id);


--
-- Name: bc_grc_risco bc_grc_risco_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_risco
    ADD CONSTRAINT bc_grc_risco_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_grc_risco bc_grc_risco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_risco
    ADD CONSTRAINT bc_grc_risco_pkey PRIMARY KEY (id);


--
-- Name: bc_grc_risco bc_grc_risco_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_risco
    ADD CONSTRAINT bc_grc_risco_uuid_key UNIQUE (uuid);


--
-- Name: bc_grc_teste_controle bc_grc_teste_controle_empresa_id_controle_id_periodo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_teste_controle
    ADD CONSTRAINT bc_grc_teste_controle_empresa_id_controle_id_periodo_key UNIQUE (empresa_id, controle_id, periodo);


--
-- Name: bc_grc_teste_controle bc_grc_teste_controle_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_teste_controle
    ADD CONSTRAINT bc_grc_teste_controle_pkey PRIMARY KEY (id);


--
-- Name: bc_hdp_chamado bc_hdp_chamado_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_hdp_chamado
    ADD CONSTRAINT bc_hdp_chamado_pkey PRIMARY KEY (id);


--
-- Name: bc_hdp_comentario bc_hdp_comentario_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_hdp_comentario
    ADD CONSTRAINT bc_hdp_comentario_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_analise_preditiva bc_ia_analise_preditiva_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva
    ADD CONSTRAINT bc_ia_analise_preditiva_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_analise_preditiva bc_ia_analise_preditiva_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva
    ADD CONSTRAINT bc_ia_analise_preditiva_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_assistente_auditoria bc_ia_assistente_auditoria_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_assistente_auditoria
    ADD CONSTRAINT bc_ia_assistente_auditoria_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_chat_mensagem bc_ia_chat_mensagem_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_chat_mensagem bc_ia_chat_mensagem_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_chat_message bc_ia_chat_message_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message
    ADD CONSTRAINT bc_ia_chat_message_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_chat_message bc_ia_chat_message_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message
    ADD CONSTRAINT bc_ia_chat_message_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_chat_sessao bc_ia_chat_sessao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao
    ADD CONSTRAINT bc_ia_chat_sessao_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_chat_sessao bc_ia_chat_sessao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao
    ADD CONSTRAINT bc_ia_chat_sessao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_chat_session bc_ia_chat_session_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session
    ADD CONSTRAINT bc_ia_chat_session_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_chat_session bc_ia_chat_session_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session
    ADD CONSTRAINT bc_ia_chat_session_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_classificacao bc_ia_classificacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao
    ADD CONSTRAINT bc_ia_classificacao_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_classificacao bc_ia_classificacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao
    ADD CONSTRAINT bc_ia_classificacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_config bc_ia_config_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_config
    ADD CONSTRAINT bc_ia_config_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_config bc_ia_config_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_config
    ADD CONSTRAINT bc_ia_config_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_embedding bc_ia_embedding_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_embedding
    ADD CONSTRAINT bc_ia_embedding_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_embedding bc_ia_embedding_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_embedding
    ADD CONSTRAINT bc_ia_embedding_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_prompt bc_ia_prompt_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt
    ADD CONSTRAINT bc_ia_prompt_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_prompt_template bc_ia_prompt_template_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template
    ADD CONSTRAINT bc_ia_prompt_template_pkey PRIMARY KEY (id);


--
-- Name: bc_ia_prompt_template bc_ia_prompt_template_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template
    ADD CONSTRAINT bc_ia_prompt_template_uuid_key UNIQUE (uuid);


--
-- Name: bc_ia_prompt bc_ia_prompt_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt
    ADD CONSTRAINT bc_ia_prompt_uuid_key UNIQUE (uuid);


--
-- Name: bc_int_delivery bc_int_delivery_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_int_delivery
    ADD CONSTRAINT bc_int_delivery_pkey PRIMARY KEY (id);


--
-- Name: bc_int_endpoint bc_int_endpoint_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_int_endpoint
    ADD CONSTRAINT bc_int_endpoint_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_int_endpoint bc_int_endpoint_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_int_endpoint
    ADD CONSTRAINT bc_int_endpoint_pkey PRIMARY KEY (id);


--
-- Name: bc_int_webhook_event bc_int_webhook_event_event_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_int_webhook_event
    ADD CONSTRAINT bc_int_webhook_event_event_id_key UNIQUE (event_id);


--
-- Name: bc_int_webhook_event bc_int_webhook_event_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_int_webhook_event
    ADD CONSTRAINT bc_int_webhook_event_pkey PRIMARY KEY (id);


--
-- Name: bc_kb_artigo bc_kb_artigo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_kb_artigo
    ADD CONSTRAINT bc_kb_artigo_pkey PRIMARY KEY (id);


--
-- Name: bc_migration_log bc_migration_log_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_migration_log
    ADD CONSTRAINT bc_migration_log_pkey PRIMARY KEY (id);


--
-- Name: bc_pcp_mps bc_pcp_mps_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_pcp_mps
    ADD CONSTRAINT bc_pcp_mps_pkey PRIMARY KEY (id);


--
-- Name: bc_pcp_mps bc_pcp_mps_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_pcp_mps
    ADD CONSTRAINT bc_pcp_mps_uuid_key UNIQUE (uuid);


--
-- Name: bc_plm_aprovacao bc_plm_aprovacao_empresa_id_mudanca_id_etapa_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_aprovacao
    ADD CONSTRAINT bc_plm_aprovacao_empresa_id_mudanca_id_etapa_key UNIQUE (empresa_id, mudanca_id, etapa);


--
-- Name: bc_plm_aprovacao bc_plm_aprovacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_aprovacao
    ADD CONSTRAINT bc_plm_aprovacao_pkey PRIMARY KEY (id);


--
-- Name: bc_plm_documento bc_plm_documento_empresa_id_codigo_versao_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_documento
    ADD CONSTRAINT bc_plm_documento_empresa_id_codigo_versao_key UNIQUE (empresa_id, codigo, versao);


--
-- Name: bc_plm_documento bc_plm_documento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_documento
    ADD CONSTRAINT bc_plm_documento_pkey PRIMARY KEY (id);


--
-- Name: bc_plm_efeito_mudanca bc_plm_efeito_mudanca_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_efeito_mudanca
    ADD CONSTRAINT bc_plm_efeito_mudanca_pkey PRIMARY KEY (id);


--
-- Name: bc_plm_mudanca bc_plm_mudanca_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_mudanca
    ADD CONSTRAINT bc_plm_mudanca_pkey PRIMARY KEY (id);


--
-- Name: bc_plm_mudanca bc_plm_mudanca_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_mudanca
    ADD CONSTRAINT bc_plm_mudanca_uuid_key UNIQUE (uuid);


--
-- Name: bc_plm_produto_revisao bc_plm_produto_revisao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_produto_revisao
    ADD CONSTRAINT bc_plm_produto_revisao_pkey PRIMARY KEY (id);


--
-- Name: bc_plm_produto_revisao bc_plm_produto_revisao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_produto_revisao
    ADD CONSTRAINT bc_plm_produto_revisao_uuid_key UNIQUE (uuid);


--
-- Name: bc_prj_etapa bc_prj_etapa_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_pkey PRIMARY KEY (id);


--
-- Name: bc_prj_etapa bc_prj_etapa_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_uuid_key UNIQUE (uuid);


--
-- Name: bc_prj_faturamento bc_prj_faturamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_faturamento
    ADD CONSTRAINT bc_prj_faturamento_pkey PRIMARY KEY (id);


--
-- Name: bc_prj_faturamento bc_prj_faturamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_faturamento
    ADD CONSTRAINT bc_prj_faturamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_prj_movimento bc_prj_movimento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_pkey PRIMARY KEY (id);


--
-- Name: bc_prj_movimento bc_prj_movimento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_uuid_key UNIQUE (uuid);


--
-- Name: bc_prj_mudanca bc_prj_mudanca_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_mudanca
    ADD CONSTRAINT bc_prj_mudanca_pkey PRIMARY KEY (id);


--
-- Name: bc_prj_mudanca bc_prj_mudanca_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_mudanca
    ADD CONSTRAINT bc_prj_mudanca_uuid_key UNIQUE (uuid);


--
-- Name: bc_prj_projeto bc_prj_projeto_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_projeto
    ADD CONSTRAINT bc_prj_projeto_pkey PRIMARY KEY (id);


--
-- Name: bc_prj_projeto bc_prj_projeto_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_projeto
    ADD CONSTRAINT bc_prj_projeto_uuid_key UNIQUE (uuid);


--
-- Name: bc_prj_risco bc_prj_risco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_risco
    ADD CONSTRAINT bc_prj_risco_pkey PRIMARY KEY (id);


--
-- Name: bc_prj_risco bc_prj_risco_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_risco
    ADD CONSTRAINT bc_prj_risco_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_alocacao_capacidade bc_prod_alocacao_capacidade_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_alocacao_capacidade
    ADD CONSTRAINT bc_prod_alocacao_capacidade_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_alocacao_capacidade bc_prod_alocacao_capacidade_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_alocacao_capacidade
    ADD CONSTRAINT bc_prod_alocacao_capacidade_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_apontamento bc_prod_apontamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_apontamento bc_prod_apontamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_centro_trabalho bc_prod_centro_trabalho_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho
    ADD CONSTRAINT bc_prod_centro_trabalho_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_centro_trabalho bc_prod_centro_trabalho_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho
    ADD CONSTRAINT bc_prod_centro_trabalho_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_estrutura bc_prod_estrutura_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura
    ADD CONSTRAINT bc_prod_estrutura_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_estrutura bc_prod_estrutura_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura
    ADD CONSTRAINT bc_prod_estrutura_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_item bc_prod_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_item
    ADD CONSTRAINT bc_prod_item_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_ordem bc_prod_ordem_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_ordem
    ADD CONSTRAINT bc_prod_ordem_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_romaneio_item bc_prod_romaneio_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item
    ADD CONSTRAINT bc_prod_romaneio_item_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_romaneio_item bc_prod_romaneio_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item
    ADD CONSTRAINT bc_prod_romaneio_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_romaneio bc_prod_romaneio_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_romaneio bc_prod_romaneio_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_roteiro_operacao bc_prod_roteiro_operacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_roteiro_operacao bc_prod_roteiro_operacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_prod_roteiro bc_prod_roteiro_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro
    ADD CONSTRAINT bc_prod_roteiro_pkey PRIMARY KEY (id);


--
-- Name: bc_prod_roteiro bc_prod_roteiro_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro
    ADD CONSTRAINT bc_prod_roteiro_uuid_key UNIQUE (uuid);


--
-- Name: bc_ptl_acesso bc_ptl_acesso_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ptl_acesso
    ADD CONSTRAINT bc_ptl_acesso_pkey PRIMARY KEY (id);


--
-- Name: bc_ptl_acesso bc_ptl_acesso_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ptl_acesso
    ADD CONSTRAINT bc_ptl_acesso_uuid_key UNIQUE (uuid);


--
-- Name: bc_qual_inspecao bc_qual_inspecao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao
    ADD CONSTRAINT bc_qual_inspecao_pkey PRIMARY KEY (id);


--
-- Name: bc_qual_inspecao bc_qual_inspecao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao
    ADD CONSTRAINT bc_qual_inspecao_uuid_key UNIQUE (uuid);


--
-- Name: bc_qual_nao_conformidade bc_qual_nao_conformidade_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade
    ADD CONSTRAINT bc_qual_nao_conformidade_pkey PRIMARY KEY (id);


--
-- Name: bc_qual_nao_conformidade bc_qual_nao_conformidade_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade
    ADD CONSTRAINT bc_qual_nao_conformidade_uuid_key UNIQUE (uuid);


--
-- Name: bc_qual_plano_inspecao bc_qual_plano_inspecao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao
    ADD CONSTRAINT bc_qual_plano_inspecao_pkey PRIMARY KEY (id);


--
-- Name: bc_qual_plano_inspecao bc_qual_plano_inspecao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao
    ADD CONSTRAINT bc_qual_plano_inspecao_uuid_key UNIQUE (uuid);


--
-- Name: bc_rh_cargo bc_rh_cargo_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_cargo
    ADD CONSTRAINT bc_rh_cargo_pkey PRIMARY KEY (id);


--
-- Name: bc_rh_ferias bc_rh_ferias_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_ferias
    ADD CONSTRAINT bc_rh_ferias_pkey PRIMARY KEY (id);


--
-- Name: bc_rh_ferias bc_rh_ferias_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_ferias
    ADD CONSTRAINT bc_rh_ferias_uuid_key UNIQUE (uuid);


--
-- Name: bc_rh_folha_item bc_rh_folha_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_pkey PRIMARY KEY (id);


--
-- Name: bc_rh_folha bc_rh_folha_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT bc_rh_folha_pkey PRIMARY KEY (id);


--
-- Name: bc_rh_funcionario bc_rh_funcionario_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_funcionario_pkey PRIMARY KEY (id);


--
-- Name: bc_rh_ponto bc_rh_ponto_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_ponto
    ADD CONSTRAINT bc_rh_ponto_pkey PRIMARY KEY (id);


--
-- Name: bc_rh_ponto bc_rh_ponto_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_ponto
    ADD CONSTRAINT bc_rh_ponto_uuid_key UNIQUE (uuid);


--
-- Name: bc_rh_rescisao bc_rh_rescisao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_rescisao
    ADD CONSTRAINT bc_rh_rescisao_pkey PRIMARY KEY (id);


--
-- Name: bc_sc_atp bc_sc_atp_empresa_id_produto_id_local_id_data_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_atp
    ADD CONSTRAINT bc_sc_atp_empresa_id_produto_id_local_id_data_key UNIQUE (empresa_id, produto_id, local_id, data);


--
-- Name: bc_sc_atp bc_sc_atp_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_atp
    ADD CONSTRAINT bc_sc_atp_pkey PRIMARY KEY (id);


--
-- Name: bc_sc_carga bc_sc_carga_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_carga
    ADD CONSTRAINT bc_sc_carga_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_sc_carga bc_sc_carga_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_carga
    ADD CONSTRAINT bc_sc_carga_pkey PRIMARY KEY (id);


--
-- Name: bc_sc_demanda bc_sc_demanda_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_demanda
    ADD CONSTRAINT bc_sc_demanda_pkey PRIMARY KEY (id);


--
-- Name: bc_sc_frete bc_sc_frete_empresa_id_carga_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_frete
    ADD CONSTRAINT bc_sc_frete_empresa_id_carga_id_key UNIQUE (empresa_id, carga_id);


--
-- Name: bc_sc_frete bc_sc_frete_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_frete
    ADD CONSTRAINT bc_sc_frete_pkey PRIMARY KEY (id);


--
-- Name: bc_sc_planejamento bc_sc_planejamento_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_planejamento
    ADD CONSTRAINT bc_sc_planejamento_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_sc_planejamento bc_sc_planejamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_planejamento
    ADD CONSTRAINT bc_sc_planejamento_pkey PRIMARY KEY (id);


--
-- Name: bc_sc_rota bc_sc_rota_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_rota
    ADD CONSTRAINT bc_sc_rota_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_sc_rota bc_sc_rota_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_rota
    ADD CONSTRAINT bc_sc_rota_pkey PRIMARY KEY (id);


--
-- Name: bc_sc_tracking bc_sc_tracking_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_sc_tracking
    ADD CONSTRAINT bc_sc_tracking_pkey PRIMARY KEY (id);


--
-- Name: bc_scm_ordem_transporte bc_scm_ordem_transporte_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_scm_ordem_transporte
    ADD CONSTRAINT bc_scm_ordem_transporte_pkey PRIMARY KEY (id);


--
-- Name: bc_scm_ordem_transporte bc_scm_ordem_transporte_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_scm_ordem_transporte
    ADD CONSTRAINT bc_scm_ordem_transporte_uuid_key UNIQUE (uuid);


--
-- Name: bc_scm_politica_reposicao bc_scm_politica_reposicao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_scm_politica_reposicao
    ADD CONSTRAINT bc_scm_politica_reposicao_pkey PRIMARY KEY (id);


--
-- Name: bc_scm_politica_reposicao bc_scm_politica_reposicao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_scm_politica_reposicao
    ADD CONSTRAINT bc_scm_politica_reposicao_uuid_key UNIQUE (uuid);


--
-- Name: bc_srv_contrato bc_srv_contrato_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_contrato
    ADD CONSTRAINT bc_srv_contrato_pkey PRIMARY KEY (id);


--
-- Name: bc_srv_contrato bc_srv_contrato_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_contrato
    ADD CONSTRAINT bc_srv_contrato_uuid_key UNIQUE (uuid);


--
-- Name: bc_srv_ordem_servico bc_srv_ordem_servico_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_pkey PRIMARY KEY (id);


--
-- Name: bc_srv_ordem_servico bc_srv_ordem_servico_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_uuid_key UNIQUE (uuid);


--
-- Name: bc_srv_os_apontamento bc_srv_os_apontamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_pkey PRIMARY KEY (id);


--
-- Name: bc_srv_os_apontamento bc_srv_os_apontamento_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_uuid_key UNIQUE (uuid);


--
-- Name: bc_srv_os_item bc_srv_os_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_pkey PRIMARY KEY (id);


--
-- Name: bc_srv_os_item bc_srv_os_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_tms_documento_entrega bc_tms_documento_entrega_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_documento_entrega
    ADD CONSTRAINT bc_tms_documento_entrega_pkey PRIMARY KEY (id);


--
-- Name: bc_tms_evento bc_tms_evento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_evento
    ADD CONSTRAINT bc_tms_evento_pkey PRIMARY KEY (id);


--
-- Name: bc_tms_fechamento bc_tms_fechamento_empresa_id_ordem_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_fechamento
    ADD CONSTRAINT bc_tms_fechamento_empresa_id_ordem_id_key UNIQUE (empresa_id, ordem_id);


--
-- Name: bc_tms_fechamento bc_tms_fechamento_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_fechamento
    ADD CONSTRAINT bc_tms_fechamento_pkey PRIMARY KEY (id);


--
-- Name: bc_tms_frete bc_tms_frete_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_frete
    ADD CONSTRAINT bc_tms_frete_pkey PRIMARY KEY (id);


--
-- Name: bc_tms_ordem bc_tms_ordem_empresa_id_numero_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_ordem
    ADD CONSTRAINT bc_tms_ordem_empresa_id_numero_key UNIQUE (empresa_id, numero);


--
-- Name: bc_tms_ordem bc_tms_ordem_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_ordem
    ADD CONSTRAINT bc_tms_ordem_pkey PRIMARY KEY (id);


--
-- Name: bc_tms_ordem bc_tms_ordem_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_ordem
    ADD CONSTRAINT bc_tms_ordem_uuid_key UNIQUE (uuid);


--
-- Name: bc_tms_parada bc_tms_parada_empresa_id_ordem_id_sequencia_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_parada
    ADD CONSTRAINT bc_tms_parada_empresa_id_ordem_id_sequencia_key UNIQUE (empresa_id, ordem_id, sequencia);


--
-- Name: bc_tms_parada bc_tms_parada_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_parada
    ADD CONSTRAINT bc_tms_parada_pkey PRIMARY KEY (id);


--
-- Name: bc_tms_rota bc_tms_rota_empresa_id_codigo_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_rota
    ADD CONSTRAINT bc_tms_rota_empresa_id_codigo_key UNIQUE (empresa_id, codigo);


--
-- Name: bc_tms_rota bc_tms_rota_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_rota
    ADD CONSTRAINT bc_tms_rota_pkey PRIMARY KEY (id);


--
-- Name: bc_tms_tracking bc_tms_tracking_empresa_id_ordem_id_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_tracking
    ADD CONSTRAINT bc_tms_tracking_empresa_id_ordem_id_key UNIQUE (empresa_id, ordem_id);


--
-- Name: bc_tms_tracking bc_tms_tracking_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_tracking
    ADD CONSTRAINT bc_tms_tracking_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_bonificacao_item bc_ven_bonificacao_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_bonificacao_item bc_ven_bonificacao_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_bonificacao bc_ven_bonificacao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_bonificacao bc_ven_bonificacao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_contrato_item bc_ven_contrato_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_contrato_item bc_ven_contrato_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_contrato bc_ven_contrato_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_contrato bc_ven_contrato_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_devolucao_item bc_ven_devolucao_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_devolucao_item bc_ven_devolucao_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_devolucao bc_ven_devolucao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_devolucao bc_ven_devolucao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_meta bc_ven_meta_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_meta
    ADD CONSTRAINT bc_ven_meta_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_pedido_item bc_ven_pedido_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_pedido_item_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_pedido bc_ven_pedido_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_regra_comissao bc_ven_regra_comissao_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_regra_comissao bc_ven_regra_comissao_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_tabela_preco_item bc_ven_tabela_preco_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_tabela_preco_item bc_ven_tabela_preco_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_ven_tabela_preco bc_ven_tabela_preco_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco
    ADD CONSTRAINT bc_ven_tabela_preco_pkey PRIMARY KEY (id);


--
-- Name: bc_ven_tabela_preco bc_ven_tabela_preco_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco
    ADD CONSTRAINT bc_ven_tabela_preco_uuid_key UNIQUE (uuid);


--
-- Name: bc_wkf_definition bc_wkf_definition_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_definition
    ADD CONSTRAINT bc_wkf_definition_pkey PRIMARY KEY (id);


--
-- Name: bc_wkf_definition bc_wkf_definition_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_definition
    ADD CONSTRAINT bc_wkf_definition_uuid_key UNIQUE (uuid);


--
-- Name: bc_wkf_instance bc_wkf_instance_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_instance
    ADD CONSTRAINT bc_wkf_instance_pkey PRIMARY KEY (id);


--
-- Name: bc_wkf_instance bc_wkf_instance_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_instance
    ADD CONSTRAINT bc_wkf_instance_uuid_key UNIQUE (uuid);


--
-- Name: bc_wkf_stage bc_wkf_stage_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_stage
    ADD CONSTRAINT bc_wkf_stage_pkey PRIMARY KEY (id);


--
-- Name: bc_wkf_stage bc_wkf_stage_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_stage
    ADD CONSTRAINT bc_wkf_stage_uuid_key UNIQUE (uuid);


--
-- Name: bc_wkf_task bc_wkf_task_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_pkey PRIMARY KEY (id);


--
-- Name: bc_wkf_task bc_wkf_task_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_uuid_key UNIQUE (uuid);


--
-- Name: bc_wms_onda_item bc_wms_onda_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_onda_item
    ADD CONSTRAINT bc_wms_onda_item_pkey PRIMARY KEY (id);


--
-- Name: bc_wms_onda_item bc_wms_onda_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_onda_item
    ADD CONSTRAINT bc_wms_onda_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_wms_onda bc_wms_onda_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_onda
    ADD CONSTRAINT bc_wms_onda_pkey PRIMARY KEY (id);


--
-- Name: bc_wms_onda bc_wms_onda_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_onda
    ADD CONSTRAINT bc_wms_onda_uuid_key UNIQUE (uuid);


--
-- Name: bc_wms_volume_item bc_wms_volume_item_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_volume_item
    ADD CONSTRAINT bc_wms_volume_item_pkey PRIMARY KEY (id);


--
-- Name: bc_wms_volume_item bc_wms_volume_item_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_volume_item
    ADD CONSTRAINT bc_wms_volume_item_uuid_key UNIQUE (uuid);


--
-- Name: bc_wms_volume bc_wms_volume_pkey; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_volume
    ADD CONSTRAINT bc_wms_volume_pkey PRIMARY KEY (id);


--
-- Name: bc_wms_volume bc_wms_volume_uuid_key; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_volume
    ADD CONSTRAINT bc_wms_volume_uuid_key UNIQUE (uuid);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: bc_fis_apuracao uk_apuracao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT uk_apuracao UNIQUE (empresa_id, imposto_id, competencia);


--
-- Name: bc_cad_produto_ecommerce uk_bc_cad_produto_ecommerce_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT uk_bc_cad_produto_ecommerce_uuid UNIQUE (uuid);


--
-- Name: bc_com_pedido_item uk_bc_com_pedido_item_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT uk_bc_com_pedido_item_uuid UNIQUE (uuid);


--
-- Name: bc_com_pedido uk_bc_com_pedido_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT uk_bc_com_pedido_uuid UNIQUE (uuid);


--
-- Name: bc_est_movimentacao uk_bc_est_mov_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT uk_bc_est_mov_uuid UNIQUE (uuid);


--
-- Name: bc_est_saldo uk_bc_est_saldo_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT uk_bc_est_saldo_uuid UNIQUE (uuid);


--
-- Name: bc_fin_caixa uk_bc_fin_caixa_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_caixa
    ADD CONSTRAINT uk_bc_fin_caixa_uuid UNIQUE (uuid);


--
-- Name: bc_fis_apuracao uk_bc_fis_apuracao_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT uk_bc_fis_apuracao_uuid UNIQUE (uuid);


--
-- Name: bc_rh_cargo uk_bc_rh_cargo_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_cargo
    ADD CONSTRAINT uk_bc_rh_cargo_uuid UNIQUE (uuid);


--
-- Name: bc_rh_folha_item uk_bc_rh_folha_item_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT uk_bc_rh_folha_item_uuid UNIQUE (uuid);


--
-- Name: bc_rh_folha uk_bc_rh_folha_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT uk_bc_rh_folha_uuid UNIQUE (uuid);


--
-- Name: bc_rh_funcionario uk_bc_rh_funcionario_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT uk_bc_rh_funcionario_uuid UNIQUE (uuid);


--
-- Name: bc_ven_pedido_item uk_bc_ven_pedido_item_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT uk_bc_ven_pedido_item_uuid UNIQUE (uuid);


--
-- Name: bc_ven_pedido uk_bc_ven_pedido_uuid; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT uk_bc_ven_pedido_uuid UNIQUE (uuid);


--
-- Name: bc_cad_cliente uk_cliente_pessoa; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT uk_cliente_pessoa UNIQUE (empresa_id, pessoa_id);


--
-- Name: bc_fis_cnae_servico uk_cnae_codigo_lc116; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cnae_servico
    ADD CONSTRAINT uk_cnae_codigo_lc116 UNIQUE (codigo, lc116_codigo);


--
-- Name: bc_core_configuracao uk_configuracao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT uk_configuracao UNIQUE (empresa_id, chave);


--
-- Name: bc_ctb_fechamento uk_ctb_fechamento; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_fechamento
    ADD CONSTRAINT uk_ctb_fechamento UNIQUE (empresa_id, periodo);


--
-- Name: bc_dms_documento uk_dms_documento; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_documento
    ADD CONSTRAINT uk_dms_documento UNIQUE (empresa_id, codigo);


--
-- Name: bc_dms_versao uk_dms_versao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT uk_dms_versao UNIQUE (documento_id, versao);


--
-- Name: bc_core_empresa_vinculo uk_empresa_vinculo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT uk_empresa_vinculo UNIQUE (empresa_id, empresa_vinculada_id, tipo);


--
-- Name: bc_fin_stripe_customer uk_fin_stripe_customer_empresa_pessoa; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT uk_fin_stripe_customer_empresa_pessoa UNIQUE (empresa_id, pessoa_id);


--
-- Name: bc_fin_stripe_customer uk_fin_stripe_customer_stripe; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_stripe_customer
    ADD CONSTRAINT uk_fin_stripe_customer_stripe UNIQUE (stripe_customer_id);


--
-- Name: bc_cad_fornecedor uk_fornecedor_pessoa; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT uk_fornecedor_pessoa UNIQUE (empresa_id, pessoa_id);


--
-- Name: bc_fis_imposto uk_imposto; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT uk_imposto UNIQUE (empresa_id, sigla);


--
-- Name: bc_fis_issqn uk_issqn_ibge_codigo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_issqn
    ADD CONSTRAINT uk_issqn_ibge_codigo UNIQUE (cod_ibge, codigo);


--
-- Name: bc_fis_obrigacao_entrega uk_obrigacao_entrega; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT uk_obrigacao_entrega UNIQUE (empresa_id, obrigacao_id, competencia);


--
-- Name: bc_cad_papel uk_papel; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT uk_papel UNIQUE (empresa_id, nome);


--
-- Name: bc_pcp_mps uk_pcp_mps; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_pcp_mps
    ADD CONSTRAINT uk_pcp_mps UNIQUE (empresa_id, periodo, produto_id);


--
-- Name: bc_core_perfil uk_perfil_empresa; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT uk_perfil_empresa UNIQUE (empresa_id, nome);


--
-- Name: bc_core_perfil_permissao uk_perfil_permissao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT uk_perfil_permissao UNIQUE (perfil_id, permissao_id);


--
-- Name: bc_prj_projeto uk_prj_projeto; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_projeto
    ADD CONSTRAINT uk_prj_projeto UNIQUE (empresa_id, codigo);


--
-- Name: bc_prod_centro_trabalho uk_prod_centro_trabalho_empresa_codigo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_centro_trabalho
    ADD CONSTRAINT uk_prod_centro_trabalho_empresa_codigo UNIQUE (empresa_id, codigo);


--
-- Name: bc_prod_romaneio uk_prod_romaneio_empresa_numero; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT uk_prod_romaneio_empresa_numero UNIQUE (empresa_id, numero);


--
-- Name: bc_prod_roteiro uk_prod_roteiro_empresa_produto_codigo_versao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro
    ADD CONSTRAINT uk_prod_roteiro_empresa_produto_codigo_versao UNIQUE (empresa_id, produto_id, codigo, versao);


--
-- Name: bc_cad_produto uk_produto_codigo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT uk_produto_codigo UNIQUE (empresa_id, codigo);


--
-- Name: bc_cad_produto_kit uk_produto_kit; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT uk_produto_kit UNIQUE (kit_id, item_id);


--
-- Name: bc_ptl_acesso uk_ptl_acesso_token; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ptl_acesso
    ADD CONSTRAINT uk_ptl_acesso_token UNIQUE (token);


--
-- Name: bc_rh_ponto uk_rh_ponto; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_ponto
    ADD CONSTRAINT uk_rh_ponto UNIQUE (empresa_id, funcionario_id, data);


--
-- Name: bc_cad_servico uk_servico_codigo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT uk_servico_codigo UNIQUE (empresa_id, codigo);


--
-- Name: bc_srv_ordem_servico uk_srv_os_numero; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT uk_srv_os_numero UNIQUE (empresa_id, numero);


--
-- Name: bc_cad_transportadora uk_transportadora_pessoa; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT uk_transportadora_pessoa UNIQUE (empresa_id, pessoa_id);


--
-- Name: bc_cad_unidade_medida uk_unidade_medida; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT uk_unidade_medida UNIQUE (empresa_id, sigla);


--
-- Name: bc_core_usuario_modulo uk_usuario_modulo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT uk_usuario_modulo UNIQUE (usuario_id, modulo_id);


--
-- Name: bc_core_usuario_empresa uk_usuario_papel_nome; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT uk_usuario_papel_nome UNIQUE (usuario_id, perfil_nome);


--
-- Name: bc_core_usuario_perfil uk_usuario_perfil; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT uk_usuario_perfil UNIQUE (usuario_id, perfil_id);


--
-- Name: bc_ven_tabela_preco_item uk_ven_tabela_preco_item_produto; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT uk_ven_tabela_preco_item_produto UNIQUE (tabela_preco_id, produto_id);


--
-- Name: bc_wkf_definition uk_wkf_definition; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_definition
    ADD CONSTRAINT uk_wkf_definition UNIQUE (empresa_id, codigo);


--
-- Name: bc_wms_onda uk_wms_onda; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_onda
    ADD CONSTRAINT uk_wms_onda UNIQUE (empresa_id, deposito_id, codigo);


--
-- Name: bc_wms_volume uk_wms_volume; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_volume
    ADD CONSTRAINT uk_wms_volume UNIQUE (empresa_id, expedicao_id, codigo);


--
-- Name: bc_prod_item ukbouojof1acc60wi84fkrr2thm; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_item
    ADD CONSTRAINT ukbouojof1acc60wi84fkrr2thm UNIQUE (uuid);


--
-- Name: bc_fin_comissao ukkiimsqq5tikgf0gnsw4lymuk9; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_comissao
    ADD CONSTRAINT ukkiimsqq5tikgf0gnsw4lymuk9 UNIQUE (uuid);


--
-- Name: bc_prod_ordem ukm4x148ro7hacu4ylqbxsu9e4n; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_ordem
    ADD CONSTRAINT ukm4x148ro7hacu4ylqbxsu9e4n UNIQUE (uuid);


--
-- Name: bc_prod_ordem uksfiffcaxbc909df77yqfuytcy; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_ordem
    ADD CONSTRAINT uksfiffcaxbc909df77yqfuytcy UNIQUE (numero);


--
-- Name: bc_ativo_imobilizado uq_ativo_empresa_codigo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_imobilizado
    ADD CONSTRAINT uq_ativo_empresa_codigo UNIQUE (empresa_id, codigo);


--
-- Name: bc_ehs_ocorrencia uq_ehs_ocorrencia; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_ocorrencia
    ADD CONSTRAINT uq_ehs_ocorrencia UNIQUE (empresa_id, numero);


--
-- Name: bc_ent_contrato uq_ent_contrato_empresa_numero; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_contrato
    ADD CONSTRAINT uq_ent_contrato_empresa_numero UNIQUE (empresa_id, numero);


--
-- Name: bc_ent_fornecedor_qualificacao uq_ent_fornecedor_qualificacao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_fornecedor_qualificacao
    ADD CONSTRAINT uq_ent_fornecedor_qualificacao UNIQUE (empresa_id, fornecedor_id);


--
-- Name: bc_ent_periodo_contabil uq_ent_periodo_contabil; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ent_periodo_contabil
    ADD CONSTRAINT uq_ent_periodo_contabil UNIQUE (empresa_id, competencia);


--
-- Name: bc_fin_consolidacao uq_fin_consolidacao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_consolidacao
    ADD CONSTRAINT uq_fin_consolidacao UNIQUE (empresa_id, competencia, grupo);


--
-- Name: bc_fin_intercompany uq_fin_intercompany; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_intercompany
    ADD CONSTRAINT uq_fin_intercompany UNIQUE (empresa_id, numero);


--
-- Name: bc_gov_controle uq_gov_controle; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_gov_controle
    ADD CONSTRAINT uq_gov_controle UNIQUE (empresa_id, codigo);


--
-- Name: bc_gov_risco uq_gov_risco; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_gov_risco
    ADD CONSTRAINT uq_gov_risco UNIQUE (empresa_id, codigo);


--
-- Name: bc_ativo_manutencao uq_manut_empresa_numero; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ativo_manutencao
    ADD CONSTRAINT uq_manut_empresa_numero UNIQUE (empresa_id, numero);


--
-- Name: bc_plm_mudanca uq_plm_mudanca; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_mudanca
    ADD CONSTRAINT uq_plm_mudanca UNIQUE (empresa_id, numero);


--
-- Name: bc_plm_produto_revisao uq_plm_revisao; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_produto_revisao
    ADD CONSTRAINT uq_plm_revisao UNIQUE (empresa_id, produto_id, revisao);


--
-- Name: bc_qual_inspecao uq_qual_inspecao_empresa_numero; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_inspecao
    ADD CONSTRAINT uq_qual_inspecao_empresa_numero UNIQUE (empresa_id, numero);


--
-- Name: bc_qual_nao_conformidade uq_qual_nc_empresa_numero; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_nao_conformidade
    ADD CONSTRAINT uq_qual_nc_empresa_numero UNIQUE (empresa_id, numero);


--
-- Name: bc_qual_plano_inspecao uq_qual_plano_empresa_codigo; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_qual_plano_inspecao
    ADD CONSTRAINT uq_qual_plano_empresa_codigo UNIQUE (empresa_id, codigo);


--
-- Name: bc_scm_ordem_transporte uq_scm_ordem_transporte; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_scm_ordem_transporte
    ADD CONSTRAINT uq_scm_ordem_transporte UNIQUE (empresa_id, numero);


--
-- Name: bc_srv_contrato uq_srv_contrato; Type: CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_contrato
    ADD CONSTRAINT uq_srv_contrato UNIQUE (empresa_id, numero);


--
-- Name: dim_cliente dim_cliente_cliente_id_key; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_cliente
    ADD CONSTRAINT dim_cliente_cliente_id_key UNIQUE (cliente_id);


--
-- Name: dim_cliente dim_cliente_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_cliente
    ADD CONSTRAINT dim_cliente_pkey PRIMARY KEY (id);


--
-- Name: dim_empresa dim_empresa_empresa_id_key; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_empresa
    ADD CONSTRAINT dim_empresa_empresa_id_key UNIQUE (empresa_id);


--
-- Name: dim_empresa dim_empresa_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_empresa
    ADD CONSTRAINT dim_empresa_pkey PRIMARY KEY (id);


--
-- Name: dim_produto dim_produto_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_produto
    ADD CONSTRAINT dim_produto_pkey PRIMARY KEY (id);


--
-- Name: dim_produto dim_produto_produto_id_key; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_produto
    ADD CONSTRAINT dim_produto_produto_id_key UNIQUE (produto_id);


--
-- Name: dim_tempo dim_tempo_data_key; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_tempo
    ADD CONSTRAINT dim_tempo_data_key UNIQUE (data);


--
-- Name: dim_tempo dim_tempo_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.dim_tempo
    ADD CONSTRAINT dim_tempo_pkey PRIMARY KEY (id);


--
-- Name: ft_compras ft_compras_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_compras
    ADD CONSTRAINT ft_compras_pkey PRIMARY KEY (id);


--
-- Name: ft_estoque ft_estoque_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_estoque
    ADD CONSTRAINT ft_estoque_pkey PRIMARY KEY (id);


--
-- Name: ft_financeiro ft_financeiro_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_financeiro
    ADD CONSTRAINT ft_financeiro_pkey PRIMARY KEY (id);


--
-- Name: ft_producao ft_producao_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_producao
    ADD CONSTRAINT ft_producao_pkey PRIMARY KEY (id);


--
-- Name: ft_vendas ft_vendas_pkey; Type: CONSTRAINT; Schema: brasil_saas_dl; Owner: sa
--

ALTER TABLE ONLY brasil_saas_dl.ft_vendas
    ADD CONSTRAINT ft_vendas_pkey PRIMARY KEY (id);


--
-- Name: base_cep base_cep_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.base_cep
    ADD CONSTRAINT base_cep_pkey PRIMARY KEY (cep);


--
-- Name: fcaixa fcaixa_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcaixa
    ADD CONSTRAINT fcaixa_pkey PRIMARY KEY (codcaixa);


--
-- Name: fcentrocusto fcentrocusto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcentrocusto
    ADD CONSTRAINT fcentrocusto_pkey PRIMARY KEY (codcusto);


--
-- Name: fempresa_vinculo fempresa_vinculo_codmatriz_codfilial_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fempresa_vinculo
    ADD CONSTRAINT fempresa_vinculo_codmatriz_codfilial_key UNIQUE (codmatriz, codfilial);


--
-- Name: fempresa_vinculo fempresa_vinculo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fempresa_vinculo
    ADD CONSTRAINT fempresa_vinculo_pkey PRIMARY KEY (codvinculo);


--
-- Name: ffinanceiro ffinanceiro_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ffinanceiro
    ADD CONSTRAINT ffinanceiro_pkey PRIMARY KEY (codfinanceiro);


--
-- Name: fmovimento_item fmovimento_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmovimento_item
    ADD CONSTRAINT fmovimento_item_pkey PRIMARY KEY (coditem);


--
-- Name: fmovimento fmovimento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmovimento
    ADD CONSTRAINT fmovimento_pkey PRIMARY KEY (codmovimento);


--
-- Name: fnota_item fnota_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota_item
    ADD CONSTRAINT fnota_item_pkey PRIMARY KEY (id_item);


--
-- Name: fnota fnota_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_pkey PRIMARY KEY (id_nota);


--
-- Name: fpessoa fpessoa_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fpessoa
    ADD CONSTRAINT fpessoa_pkey PRIMARY KEY (id_pessoa);


--
-- Name: fproduto_ecommerce fproduto_ecommerce_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_ecommerce
    ADD CONSTRAINT fproduto_ecommerce_pkey PRIMARY KEY (codecommerce);


--
-- Name: fproduto_fiscal fproduto_fiscal_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_fiscal
    ADD CONSTRAINT fproduto_fiscal_pkey PRIMARY KEY (codproduto);


--
-- Name: fproduto_imagem fproduto_imagem_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_imagem
    ADD CONSTRAINT fproduto_imagem_pkey PRIMARY KEY (codimagem);


--
-- Name: fproduto_kit fproduto_kit_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_kit
    ADD CONSTRAINT fproduto_kit_pkey PRIMARY KEY (codkit, codproduto_filho);


--
-- Name: fproduto_movimento fproduto_movimento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_movimento
    ADD CONSTRAINT fproduto_movimento_pkey PRIMARY KEY (codmov);


--
-- Name: fproduto_variacao fproduto_variacao_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_variacao
    ADD CONSTRAINT fproduto_variacao_pkey PRIMARY KEY (codvariacao);


--
-- Name: fvenda_item fvenda_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda_item
    ADD CONSTRAINT fvenda_item_pkey PRIMARY KEY (id_item);


--
-- Name: fvenda fvenda_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_pkey PRIMARY KEY (id_venda);


--
-- Name: fcfo pk_fcfo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcfo
    ADD CONSTRAINT pk_fcfo PRIMARY KEY (codcfo);


--
-- Name: fcondicao pk_fcondicao; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcondicao
    ADD CONSTRAINT pk_fcondicao PRIMARY KEY (codcondicao);


--
-- Name: fcusto pk_fcusto; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fcusto
    ADD CONSTRAINT pk_fcusto PRIMARY KEY (codcusto);


--
-- Name: fdatas pk_fdatas_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fdatas
    ADD CONSTRAINT pk_fdatas_1 PRIMARY KEY (iddata);


--
-- Name: fdia pk_fdia; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fdia
    ADD CONSTRAINT pk_fdia PRIMARY KEY (refdia);


--
-- Name: fdocumento pk_fdocumento_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fdocumento
    ADD CONSTRAINT pk_fdocumento_1 PRIMARY KEY (coddoc);


--
-- Name: fempresa pk_fempresa; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fempresa
    ADD CONSTRAINT pk_fempresa PRIMARY KEY (codigo);


--
-- Name: fextrato pk_fextrato; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fextrato
    ADD CONSTRAINT pk_fextrato PRIMARY KEY (idlanextrato);


--
-- Name: ffuncionario pk_ffuncionario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ffuncionario
    ADD CONSTRAINT pk_ffuncionario PRIMARY KEY (codfuncionario);


--
-- Name: fitem pk_fitem; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fitem
    ADD CONSTRAINT pk_fitem PRIMARY KEY (refitem);


--
-- Name: flan pk_flan; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flan
    ADD CONSTRAINT pk_flan PRIMARY KEY (idlan);


--
-- Name: fmov pk_fmov; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmov
    ADD CONSTRAINT pk_fmov PRIMARY KEY (idmov);


--
-- Name: fproduto pk_fproduto; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto
    ADD CONSTRAINT pk_fproduto PRIMARY KEY (codproduto);


--
-- Name: frecibo pk_frecibo_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.frecibo
    ADD CONSTRAINT pk_frecibo_1 PRIMARY KEY (idrecibo);


--
-- Name: ftipo pk_ftipo_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ftipo
    ADD CONSTRAINT pk_ftipo_1 PRIMARY KEY (codtipo);


--
-- Name: ftipopagamento pk_ftipopagamento_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ftipopagamento
    ADD CONSTRAINT pk_ftipopagamento_1 PRIMARY KEY (codtipo);


--
-- Name: fusuario pk_fusuario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fusuario
    ADD CONSTRAINT pk_fusuario PRIMARY KEY (codigo);


--
-- Name: gparametro pk_gparametro; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gparametro
    ADD CONSTRAINT pk_gparametro PRIMARY KEY (idparametro);


--
-- Name: tmunicipio pk_tmunicipio_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tmunicipio
    ADD CONSTRAINT pk_tmunicipio_1 PRIMARY KEY (refmunicipio);


--
-- Name: tcnae_servico tcnae_servico_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tcnae_servico
    ADD CONSTRAINT tcnae_servico_pkey PRIMARY KEY (cnae, codigo_item);


--
-- Name: tissqn tissqn_cod_ibge_codigo_servico_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tissqn
    ADD CONSTRAINT tissqn_cod_ibge_codigo_servico_key UNIQUE (cod_ibge, codigo_servico);


--
-- Name: tissqn tissqn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tissqn
    ADD CONSTRAINT tissqn_pkey PRIMARY KEY (id);


--
-- Name: tncm tncm_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tncm
    ADD CONSTRAINT tncm_pkey PRIMARY KEY (codigo);


--
-- Name: tservico_lc116 tservico_lc116_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tservico_lc116
    ADD CONSTRAINT tservico_lc116_pkey PRIMARY KEY (codigo_item);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX flyway_schema_history_s_idx ON brasil_saas.flyway_schema_history USING btree (success);


--
-- Name: idx_agd_emp_inicio; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_agd_emp_inicio ON brasil_saas.bc_agd_evento USING btree (empresa_id, inicio) WHERE (deleted_at IS NULL);


--
-- Name: idx_apuracao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_apuracao_empresa ON brasil_saas.bc_fis_apuracao USING btree (empresa_id, competencia);


--
-- Name: idx_assist_empresa_data; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_assist_empresa_data ON brasil_saas.bc_ia_assistente_auditoria USING btree (empresa_id, created_at DESC) WHERE (deleted_at IS NULL);


--
-- Name: idx_assist_identity; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_assist_identity ON brasil_saas.bc_ia_assistente_auditoria USING btree (identity_id) WHERE ((deleted_at IS NULL) AND (identity_id IS NOT NULL));


--
-- Name: idx_assist_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_assist_status ON brasil_saas.bc_ia_assistente_auditoria USING btree (status) WHERE (deleted_at IS NULL);


--
-- Name: idx_ativo_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ativo_empresa_status ON brasil_saas.bc_ativo_imobilizado USING btree (empresa_id, status);


--
-- Name: idx_auditoria_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_auditoria_empresa ON brasil_saas.bc_core_auditoria USING btree (empresa_id, tabela, created_at);


--
-- Name: idx_base_cep_cep; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_base_cep_cep ON brasil_saas.bc_cad_base_cep USING btree (cep);


--
-- Name: idx_bc_core_perfil_pai_id; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bc_core_perfil_pai_id ON brasil_saas.bc_core_perfil USING btree (perfil_pai_id);


--
-- Name: idx_bc_fin_aprovacao_documento; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bc_fin_aprovacao_documento ON brasil_saas.bc_fin_aprovacao USING btree (tipo_documento, documento_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_bc_fin_aprovacao_pendentes; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bc_fin_aprovacao_pendentes ON brasil_saas.bc_fin_aprovacao USING btree (empresa_id, status, data_solicitacao) WHERE (deleted_at IS NULL);


--
-- Name: idx_bc_fin_aprovacao_titulo_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bc_fin_aprovacao_titulo_status ON brasil_saas.bc_fin_aprovacao USING btree (titulo_id, status) WHERE (deleted_at IS NULL);


--
-- Name: idx_bc_municipio_nome; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bc_municipio_nome ON brasil_saas.bc_cad_municipio USING btree (nome);


--
-- Name: idx_bi_dashboard_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_dashboard_empresa ON brasil_saas.bc_bi_dashboard USING btree (empresa_id);


--
-- Name: idx_bi_dashboard_type; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_dashboard_type ON brasil_saas.bc_bi_dashboard USING btree (dashboard_type);


--
-- Name: idx_bi_dashboard_widget_dashboard; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_dashboard_widget_dashboard ON brasil_saas.bc_bi_dashboard_widget USING btree (dashboard_id);


--
-- Name: idx_bi_indicador_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_indicador_empresa ON brasil_saas.bc_bi_indicador USING btree (empresa_id);


--
-- Name: idx_bi_kpi_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_kpi_empresa ON brasil_saas.bc_bi_kpi USING btree (empresa_id);


--
-- Name: idx_bi_kpi_type; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_kpi_type ON brasil_saas.bc_bi_kpi USING btree (kpi_type);


--
-- Name: idx_bi_relatorio_agendado_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_relatorio_agendado_empresa ON brasil_saas.bc_bi_relatorio_agendado USING btree (empresa_id);


--
-- Name: idx_bi_relatorio_agendado_relatorio; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_relatorio_agendado_relatorio ON brasil_saas.bc_bi_relatorio_agendado USING btree (relatorio_id);


--
-- Name: idx_bi_relatorio_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_relatorio_empresa ON brasil_saas.bc_bi_relatorio USING btree (empresa_id);


--
-- Name: idx_bi_report_category; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_report_category ON brasil_saas.bc_bi_report USING btree (category);


--
-- Name: idx_bi_report_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_report_empresa ON brasil_saas.bc_bi_report USING btree (empresa_id);


--
-- Name: idx_bi_report_parameter_report; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_bi_report_parameter_report ON brasil_saas.bc_bi_report_parameter USING btree (report_id);


--
-- Name: idx_caixa_mov_created; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_caixa_mov_created ON brasil_saas.bc_fin_caixa_movimento USING btree (empresa_id, created_at DESC);


--
-- Name: idx_caixa_mov_empresa_caixa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_caixa_mov_empresa_caixa ON brasil_saas.bc_fin_caixa_movimento USING btree (empresa_id, caixa_id);


--
-- Name: idx_cliente_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_cliente_empresa ON brasil_saas.bc_cad_cliente USING btree (empresa_id);


--
-- Name: idx_cob_acao_created; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_cob_acao_created ON brasil_saas.bc_fin_cobranca_acao USING btree (empresa_id, created_at DESC);


--
-- Name: idx_cob_acao_empresa_titulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_cob_acao_empresa_titulo ON brasil_saas.bc_fin_cobranca_acao USING btree (empresa_id, titulo_id);


--
-- Name: idx_com_conferencia_fatura_nfe; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_conferencia_fatura_nfe ON brasil_saas.bc_com_conferencia_fatura USING btree (empresa_id, nfe_id);


--
-- Name: idx_com_conferencia_fatura_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_conferencia_fatura_pedido ON brasil_saas.bc_com_conferencia_fatura USING btree (empresa_id, pedido_id, status);


--
-- Name: idx_com_conferencia_fatura_titulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_conferencia_fatura_titulo ON brasil_saas.bc_com_conferencia_fatura USING btree (empresa_id, titulo_id);


--
-- Name: idx_com_contrato_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_contrato_empresa ON brasil_saas.bc_com_contrato USING btree (empresa_id);


--
-- Name: idx_com_contrato_item_contrato; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_contrato_item_contrato ON brasil_saas.bc_com_contrato_item USING btree (contrato_id);


--
-- Name: idx_com_contrato_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_contrato_status ON brasil_saas.bc_com_contrato USING btree (empresa_id, status);


--
-- Name: idx_com_cotacao_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_cotacao_empresa_status ON brasil_saas.bc_com_cotacao USING btree (empresa_id, status);


--
-- Name: idx_com_cotacao_fornecedor; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_cotacao_fornecedor ON brasil_saas.bc_com_cotacao_fornecedor USING btree (empresa_id, fornecedor_id);


--
-- Name: idx_com_cotacao_fornecedor_cotacao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_cotacao_fornecedor_cotacao ON brasil_saas.bc_com_cotacao_fornecedor USING btree (empresa_id, cotacao_id);


--
-- Name: idx_com_cotacao_item_fornecedor; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_cotacao_item_fornecedor ON brasil_saas.bc_com_cotacao_item USING btree (empresa_id, cotacao_fornecedor_id);


--
-- Name: idx_com_cotacao_solicitacao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_cotacao_solicitacao ON brasil_saas.bc_com_cotacao USING btree (empresa_id, solicitacao_id);


--
-- Name: idx_com_item_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_item_pedido ON brasil_saas.bc_com_pedido_item USING btree (pedido_id);


--
-- Name: idx_com_pedido_contrato; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_pedido_contrato ON brasil_saas.bc_com_pedido USING btree (contrato_id) WHERE (contrato_id IS NOT NULL);


--
-- Name: idx_com_pedido_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_pedido_empresa ON brasil_saas.bc_com_pedido USING btree (empresa_id, status);


--
-- Name: idx_com_pedido_fornecedor; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_pedido_fornecedor ON brasil_saas.bc_com_pedido USING btree (fornecedor_id);


--
-- Name: idx_com_recebimento_empresa_data; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_recebimento_empresa_data ON brasil_saas.bc_com_recebimento USING btree (empresa_id, data_recebimento DESC);


--
-- Name: idx_com_recebimento_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_recebimento_empresa_status ON brasil_saas.bc_com_recebimento USING btree (empresa_id, status);


--
-- Name: idx_com_recebimento_item_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_recebimento_item_produto ON brasil_saas.bc_com_recebimento_item USING btree (empresa_id, produto_id);


--
-- Name: idx_com_recebimento_item_recebimento; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_recebimento_item_recebimento ON brasil_saas.bc_com_recebimento_item USING btree (empresa_id, recebimento_id);


--
-- Name: idx_com_recebimento_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_recebimento_pedido ON brasil_saas.bc_com_recebimento USING btree (empresa_id, pedido_id);


--
-- Name: idx_com_solicitacao_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_solicitacao_empresa_status ON brasil_saas.bc_com_solicitacao USING btree (empresa_id, status);


--
-- Name: idx_com_solicitacao_item_empresa_solicitacao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_com_solicitacao_item_empresa_solicitacao ON brasil_saas.bc_com_solicitacao_item USING btree (empresa_id, solicitacao_id);


--
-- Name: idx_crm_atividade_agendada; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_crm_atividade_agendada ON brasil_saas.bc_crm_atividade USING btree (empresa_id, data_agendada, status);


--
-- Name: idx_crm_oportunidade_vendedor_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_crm_oportunidade_vendedor_status ON brasil_saas.bc_crm_oportunidade USING btree (empresa_id, vendedor_id, status);


--
-- Name: idx_doc_fluxo_destino; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_doc_fluxo_destino ON brasil_saas.bc_core_documento_fluxo USING btree (empresa_id, destino_tipo, destino_id);


--
-- Name: idx_doc_fluxo_origem; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_doc_fluxo_origem ON brasil_saas.bc_core_documento_fluxo USING btree (empresa_id, origem_tipo, origem_id);


--
-- Name: idx_endereco_pessoa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_endereco_pessoa ON brasil_saas.bc_cad_endereco USING btree (pessoa_id);


--
-- Name: idx_est_endereco_deposito_ativo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_endereco_deposito_ativo ON brasil_saas.bc_est_endereco USING btree (empresa_id, deposito_id, ativo);


--
-- Name: idx_est_expedicao_item; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_expedicao_item ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, expedicao_id);


--
-- Name: idx_est_expedicao_item_auditoria; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_expedicao_item_auditoria ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, expedicao_id, created_at);


--
-- Name: idx_est_expedicao_item_deleted; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_expedicao_item_deleted ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, expedicao_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_est_expedicao_item_endereco; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_expedicao_item_endereco ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, endereco_id, lote_id, status);


--
-- Name: idx_est_expedicao_item_lote; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_expedicao_item_lote ON brasil_saas.bc_est_expedicao_item USING btree (empresa_id, lote_id, status);


--
-- Name: idx_est_expedicao_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_expedicao_status ON brasil_saas.bc_est_expedicao USING btree (empresa_id, deposito_id, status, data_abertura DESC);


--
-- Name: idx_est_inventario_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_inventario_empresa_status ON brasil_saas.bc_est_inventario USING btree (empresa_id, deposito_id, status);


--
-- Name: idx_est_inventario_item_inventario; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_inventario_item_inventario ON brasil_saas.bc_est_inventario_item USING btree (inventario_id);


--
-- Name: idx_est_inventario_item_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_inventario_item_produto ON brasil_saas.bc_est_inventario_item USING btree (produto_id, lote_id, endereco_id);


--
-- Name: idx_est_lote_fefo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_lote_fefo ON brasil_saas.bc_est_lote USING btree (empresa_id, deposito_id, produto_id, status, data_validade);


--
-- Name: idx_est_lote_validade_ativo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_lote_validade_ativo ON brasil_saas.bc_est_lote USING btree (empresa_id, deposito_id, produto_id, status, data_validade, id);


--
-- Name: idx_est_mov_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_mov_produto ON brasil_saas.bc_est_movimentacao USING btree (empresa_id, produto_id, data_movimento);


--
-- Name: idx_est_reserva_endereco_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_reserva_endereco_status ON brasil_saas.bc_est_reserva USING btree (empresa_id, deposito_id, endereco_id, produto_id, lote_id, status);


--
-- Name: idx_est_reserva_lote_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_reserva_lote_status ON brasil_saas.bc_est_reserva USING btree (empresa_id, deposito_id, produto_id, lote_id, status);


--
-- Name: idx_est_reserva_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_reserva_pedido ON brasil_saas.bc_est_reserva USING btree (empresa_id, pedido_venda_id, status);


--
-- Name: idx_est_reserva_produto_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_reserva_produto_status ON brasil_saas.bc_est_reserva USING btree (empresa_id, deposito_id, produto_id, status);


--
-- Name: idx_est_saldo_deposito_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_saldo_deposito_produto ON brasil_saas.bc_est_saldo USING btree (deposito_id, produto_id);


--
-- Name: idx_est_saldo_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_saldo_produto ON brasil_saas.bc_est_saldo USING btree (empresa_id, produto_id);


--
-- Name: idx_est_serie_produto_deposito; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_serie_produto_deposito ON brasil_saas.bc_est_serie USING btree (empresa_id, produto_id, deposito_id, status);


--
-- Name: idx_est_transferencia_empresa_data; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_transferencia_empresa_data ON brasil_saas.bc_est_transferencia USING btree (empresa_id, data_transferencia DESC);


--
-- Name: idx_est_transferencia_item_transferencia; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_transferencia_item_transferencia ON brasil_saas.bc_est_transferencia_item USING btree (transferencia_id);


--
-- Name: idx_est_transferencia_item_wms; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_est_transferencia_item_wms ON brasil_saas.bc_est_transferencia_item USING btree (lote_id, endereco_origem_id, endereco_destino_id);


--
-- Name: idx_fin_baixa_empresa_conta_data; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_baixa_empresa_conta_data ON brasil_saas.bc_fin_baixa USING btree (empresa_id, conta_bancaria_id, data_baixa) WHERE (deleted_at IS NULL);


--
-- Name: idx_fin_baixa_empresa_titulo_data; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_baixa_empresa_titulo_data ON brasil_saas.bc_fin_baixa USING btree (empresa_id, titulo_id, data_baixa) WHERE (deleted_at IS NULL);


--
-- Name: idx_fin_baixa_empresa_titulo_parcela_data; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_baixa_empresa_titulo_parcela_data ON brasil_saas.bc_fin_baixa USING btree (empresa_id, titulo_id, parcela_id, data_baixa DESC) WHERE (deleted_at IS NULL);


--
-- Name: idx_fin_extrato_conta_data_id; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_extrato_conta_data_id ON brasil_saas.bc_fin_extrato USING btree (conta_bancaria_id, data_movimento DESC, id DESC) WHERE (deleted_at IS NULL);


--
-- Name: idx_fin_extrato_empresa_conciliado; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_extrato_empresa_conciliado ON brasil_saas.bc_fin_extrato USING btree (empresa_id, conciliado, data_movimento DESC) WHERE (deleted_at IS NULL);


--
-- Name: idx_fin_stripe_customer_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_stripe_customer_empresa ON brasil_saas.bc_fin_stripe_customer USING btree (empresa_id);


--
-- Name: idx_fin_stripe_payment_invoice; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_stripe_payment_invoice ON brasil_saas.bc_fin_stripe_payment USING btree (invoice_id) WHERE (invoice_id IS NOT NULL);


--
-- Name: idx_fin_stripe_payment_titulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_stripe_payment_titulo ON brasil_saas.bc_fin_stripe_payment USING btree (empresa_id, titulo_id);


--
-- Name: idx_fin_stripe_webhook_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_stripe_webhook_empresa ON brasil_saas.bc_fin_stripe_webhook_event USING btree (empresa_id, processed_at);


--
-- Name: idx_fin_titulo_empresa_status_vencimento; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_titulo_empresa_status_vencimento ON brasil_saas.bc_fin_titulo USING btree (empresa_id, status, data_vencimento) WHERE (deleted_at IS NULL);


--
-- Name: idx_fin_titulo_parcela_empresa_status_vencimento; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fin_titulo_parcela_empresa_status_vencimento ON brasil_saas.bc_fin_titulo_parcela USING btree (empresa_id, status, data_vencimento) WHERE (deleted_at IS NULL);


--
-- Name: idx_fis_nfe_pedido_compra; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fis_nfe_pedido_compra ON brasil_saas.bc_fis_nfe USING btree (empresa_id, pedido_compra_id);


--
-- Name: idx_fis_nfe_pedido_venda; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fis_nfe_pedido_venda ON brasil_saas.bc_fis_nfe USING btree (empresa_id, pedido_venda_id);


--
-- Name: idx_fis_regra_ncm; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_fis_regra_ncm ON brasil_saas.bc_fis_regra_tributaria USING btree (empresa_id, ncm) WHERE ((deleted_at IS NULL) AND (ativa = true));


--
-- Name: idx_hdp_chamado_emp; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_hdp_chamado_emp ON brasil_saas.bc_hdp_chamado USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_hdp_chamado_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_hdp_chamado_status ON brasil_saas.bc_hdp_chamado USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: idx_hdp_com_chamado; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_hdp_com_chamado ON brasil_saas.bc_hdp_comentario USING btree (chamado_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_ia_analise_preditiva_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_analise_preditiva_empresa ON brasil_saas.bc_ia_analise_preditiva USING btree (empresa_id);


--
-- Name: idx_ia_chat_mensagem_sessao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_chat_mensagem_sessao ON brasil_saas.bc_ia_chat_mensagem USING btree (sessao_id);


--
-- Name: idx_ia_chat_message_session; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_chat_message_session ON brasil_saas.bc_ia_chat_message USING btree (session_id);


--
-- Name: idx_ia_chat_message_type; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_chat_message_type ON brasil_saas.bc_ia_chat_message USING btree (message_type);


--
-- Name: idx_ia_chat_sessao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_chat_sessao_empresa ON brasil_saas.bc_ia_chat_sessao USING btree (empresa_id);


--
-- Name: idx_ia_chat_session_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_chat_session_empresa ON brasil_saas.bc_ia_chat_session USING btree (empresa_id);


--
-- Name: idx_ia_chat_session_type; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_chat_session_type ON brasil_saas.bc_ia_chat_session USING btree (session_type);


--
-- Name: idx_ia_chat_session_user; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_chat_session_user ON brasil_saas.bc_ia_chat_session USING btree (user_id);


--
-- Name: idx_ia_classificacao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_classificacao_empresa ON brasil_saas.bc_ia_classificacao USING btree (empresa_id);


--
-- Name: idx_ia_config_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_config_empresa ON brasil_saas.bc_ia_config USING btree (empresa_id);


--
-- Name: idx_ia_embedding_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_embedding_empresa ON brasil_saas.bc_ia_embedding USING btree (empresa_id);


--
-- Name: idx_ia_prompt_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_prompt_empresa ON brasil_saas.bc_ia_prompt USING btree (empresa_id);


--
-- Name: idx_ia_prompt_template_category; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_prompt_template_category ON brasil_saas.bc_ia_prompt_template USING btree (category);


--
-- Name: idx_ia_prompt_template_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ia_prompt_template_empresa ON brasil_saas.bc_ia_prompt_template USING btree (empresa_id);


--
-- Name: idx_kb_emp; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_kb_emp ON brasil_saas.bc_kb_artigo USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_log_acesso_usuario; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_log_acesso_usuario ON brasil_saas.bc_core_log_acesso USING btree (usuario_id, created_at);


--
-- Name: idx_manut_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_manut_empresa_status ON brasil_saas.bc_ativo_manutencao USING btree (empresa_id, status);


--
-- Name: idx_nfe_chave; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_nfe_chave ON brasil_saas.bc_fis_nfe USING btree (chave_acesso);


--
-- Name: idx_nfe_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_nfe_empresa ON brasil_saas.bc_fis_nfe USING btree (empresa_id, data_emissao);


--
-- Name: idx_nfe_item_nfe; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_nfe_item_nfe ON brasil_saas.bc_fis_nfe_item USING btree (nfe_id);


--
-- Name: idx_nfse_chave; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_nfse_chave ON brasil_saas.bc_fis_nfse USING btree (codigo_verificacao);


--
-- Name: idx_nfse_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_nfse_empresa ON brasil_saas.bc_fis_nfse USING btree (empresa_id, data_emissao);


--
-- Name: idx_notif_emp_user; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_notif_emp_user ON brasil_saas.bc_core_notificacao USING btree (empresa_id, usuario_id, lida) WHERE (deleted_at IS NULL);


--
-- Name: idx_notificacao_usuario; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_notificacao_usuario ON brasil_saas.bc_core_notificacao USING btree (usuario_id, lida);


--
-- Name: idx_palavra; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_palavra ON brasil_saas.bc_fis_palavra_chave USING btree (palavra) WHERE (deleted_at IS NULL);


--
-- Name: idx_pessoa_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_pessoa_empresa ON brasil_saas.bc_cad_pessoa USING btree (empresa_id, nome);


--
-- Name: idx_prod_apontamento_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_apontamento_empresa ON brasil_saas.bc_prod_apontamento USING btree (empresa_id);


--
-- Name: idx_prod_apontamento_item; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_apontamento_item ON brasil_saas.bc_prod_apontamento USING btree (item_producao_id);


--
-- Name: idx_prod_apontamento_producao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_apontamento_producao ON brasil_saas.bc_prod_apontamento USING btree (producao_id);


--
-- Name: idx_prod_centro_trabalho_empresa_ativo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_centro_trabalho_empresa_ativo ON brasil_saas.bc_prod_centro_trabalho USING btree (empresa_id, ativo);


--
-- Name: idx_prod_estrutura_filho; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_estrutura_filho ON brasil_saas.bc_prod_estrutura USING btree (empresa_id, produto_filho_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_prod_estrutura_pai; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_estrutura_pai ON brasil_saas.bc_prod_estrutura USING btree (empresa_id, produto_pai_id, ativo) WHERE (deleted_at IS NULL);


--
-- Name: idx_prod_item_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_item_empresa ON brasil_saas.bc_prod_item USING btree (empresa_id);


--
-- Name: idx_prod_item_producao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_item_producao ON brasil_saas.bc_prod_item USING btree (producao_id);


--
-- Name: idx_prod_ordem_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_ordem_empresa ON brasil_saas.bc_prod_ordem USING btree (empresa_id);


--
-- Name: idx_prod_romaneio_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_romaneio_empresa ON brasil_saas.bc_prod_romaneio USING btree (empresa_id);


--
-- Name: idx_prod_romaneio_item_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_romaneio_item_produto ON brasil_saas.bc_prod_romaneio_item USING btree (produto_id);


--
-- Name: idx_prod_romaneio_item_romaneio; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_romaneio_item_romaneio ON brasil_saas.bc_prod_romaneio_item USING btree (romaneio_id);


--
-- Name: idx_prod_romaneio_producao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_romaneio_producao ON brasil_saas.bc_prod_romaneio USING btree (producao_id);


--
-- Name: idx_prod_roteiro_empresa_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_roteiro_empresa_produto ON brasil_saas.bc_prod_roteiro USING btree (empresa_id, produto_id, ativo);


--
-- Name: idx_prod_roteiro_operacao_roteiro; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_prod_roteiro_operacao_roteiro ON brasil_saas.bc_prod_roteiro_operacao USING btree (empresa_id, roteiro_id, sequencia);


--
-- Name: idx_produto_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_produto_empresa ON brasil_saas.bc_cad_produto USING btree (empresa_id, nome);


--
-- Name: idx_promessa_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_promessa_empresa ON brasil_saas.bc_fin_promessa_pagamento USING btree (empresa_id, status);


--
-- Name: idx_promessa_titulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_promessa_titulo ON brasil_saas.bc_fin_promessa_pagamento USING btree (empresa_id, titulo_id);


--
-- Name: idx_qual_inspecao_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_qual_inspecao_empresa_status ON brasil_saas.bc_qual_inspecao USING btree (empresa_id, status);


--
-- Name: idx_qual_nc_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_qual_nc_empresa_status ON brasil_saas.bc_qual_nao_conformidade USING btree (empresa_id, status);


--
-- Name: idx_rh_folha_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_rh_folha_empresa ON brasil_saas.bc_rh_folha USING btree (empresa_id, competencia);


--
-- Name: idx_rh_folha_item_folha; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_rh_folha_item_folha ON brasil_saas.bc_rh_folha_item USING btree (folha_id);


--
-- Name: idx_rh_func_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_rh_func_empresa ON brasil_saas.bc_rh_funcionario USING btree (empresa_id);


--
-- Name: idx_rh_rescisao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_rh_rescisao_empresa ON brasil_saas.bc_rh_rescisao USING btree (empresa_id);


--
-- Name: idx_rh_rescisao_func; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_rh_rescisao_func ON brasil_saas.bc_rh_rescisao USING btree (empresa_id, funcionario_id);


--
-- Name: idx_servico_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_servico_empresa ON brasil_saas.bc_cad_servico USING btree (empresa_id);


--
-- Name: idx_sessao_usuario; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_sessao_usuario ON brasil_saas.bc_core_sessao USING btree (usuario_id, expira_at);


--
-- Name: idx_srv_os_apont_os; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_srv_os_apont_os ON brasil_saas.bc_srv_os_apontamento USING btree (os_id);


--
-- Name: idx_srv_os_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_srv_os_empresa ON brasil_saas.bc_srv_ordem_servico USING btree (empresa_id, status);


--
-- Name: idx_srv_os_item_os; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_srv_os_item_os ON brasil_saas.bc_srv_os_item USING btree (os_id);


--
-- Name: idx_tabela_palavra; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_tabela_palavra ON brasil_saas.bc_fis_palavra_chave USING btree (tabela, palavra) WHERE (deleted_at IS NULL);


--
-- Name: idx_ven_contrato_cliente_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_contrato_cliente_status ON brasil_saas.bc_ven_contrato USING btree (empresa_id, cliente_id, status);


--
-- Name: idx_ven_ctr_emp; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_ctr_emp ON brasil_saas.bc_ven_contrato USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: idx_ven_devolucao_cliente_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_devolucao_cliente_status ON brasil_saas.bc_ven_devolucao USING btree (empresa_id, cliente_id, status);


--
-- Name: idx_ven_item_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_item_pedido ON brasil_saas.bc_ven_pedido_item USING btree (pedido_id);


--
-- Name: idx_ven_meta_emp; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_meta_emp ON brasil_saas.bc_ven_meta USING btree (empresa_id, ano, mes) WHERE (deleted_at IS NULL);


--
-- Name: idx_ven_pedido_cliente; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_pedido_cliente ON brasil_saas.bc_ven_pedido USING btree (cliente_id);


--
-- Name: idx_ven_pedido_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_pedido_empresa ON brasil_saas.bc_ven_pedido USING btree (empresa_id, status);


--
-- Name: idx_ven_pedido_tabela_preco; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_pedido_tabela_preco ON brasil_saas.bc_ven_pedido USING btree (empresa_id, tabela_preco_id);


--
-- Name: idx_ven_regra_comissao_vendedor; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_regra_comissao_vendedor ON brasil_saas.bc_ven_regra_comissao USING btree (empresa_id, vendedor_id, ativo);


--
-- Name: idx_ven_tabela_preco_item_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX idx_ven_tabela_preco_item_produto ON brasil_saas.bc_ven_tabela_preco_item USING btree (empresa_id, produto_id);


--
-- Name: ix_bc_ativo_classe_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_classe_empresa ON brasil_saas.bc_ativo_classe USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_ativo_depreciacao_execucao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_depreciacao_execucao_empresa ON brasil_saas.bc_ativo_depreciacao_execucao USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_ativo_manutencao_apontamento_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_manutencao_apontamento_empresa ON brasil_saas.bc_ativo_manutencao_apontamento USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_ativo_manutencao_material_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_manutencao_material_empresa ON brasil_saas.bc_ativo_manutencao_material USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_ativo_medicao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_medicao_empresa ON brasil_saas.bc_ativo_medicao USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_ativo_movimento_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_movimento_empresa ON brasil_saas.bc_ativo_movimento USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_ativo_nota_manutencao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_nota_manutencao_empresa ON brasil_saas.bc_ativo_nota_manutencao USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_ativo_plano_manutencao_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_ativo_plano_manutencao_empresa ON brasil_saas.bc_ativo_plano_manutencao USING btree (empresa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_bc_cad_produto_codigo_barras; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_cad_produto_codigo_barras ON brasil_saas.bc_cad_produto USING btree (codigo_barras) WHERE ((codigo_barras IS NOT NULL) AND (deleted_at IS NULL));


--
-- Name: ix_bc_core_banco_nome; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_core_banco_nome ON brasil_saas.bc_core_banco USING btree (lower((nome)::text));


--
-- Name: ix_bc_core_banco_pix; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_core_banco_pix ON brasil_saas.bc_core_banco USING btree (aceita_pix) WHERE aceita_pix;


--
-- Name: ix_bc_fin_caixa_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_fin_caixa_empresa ON brasil_saas.bc_fin_caixa USING btree (empresa_id, nome);


--
-- Name: ix_bc_fis_nfe_item_codigo_barras; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_fis_nfe_item_codigo_barras ON brasil_saas.bc_fis_nfe_item USING btree (codigo_barras) WHERE (codigo_barras IS NOT NULL);


--
-- Name: ix_bc_fis_nfe_item_nfe; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_bc_fis_nfe_item_nfe ON brasil_saas.bc_fis_nfe_item USING btree (nfe_id);


--
-- Name: ix_cmp_devolucao_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_cmp_devolucao_pedido ON brasil_saas.bc_cmp_devolucao USING btree (empresa_id, pedido_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_com_conf_item_nfe; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_com_conf_item_nfe ON brasil_saas.bc_com_conferencia_fatura_item USING btree (empresa_id, nfe_item_id);


--
-- Name: ix_com_conf_item_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_com_conf_item_pedido ON brasil_saas.bc_com_conferencia_fatura_item USING btree (empresa_id, pedido_item_id);


--
-- Name: ix_com_conferencia_item; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_com_conferencia_item ON brasil_saas.bc_com_conferencia_fatura_item USING btree (empresa_id, conferencia_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_com_conferencia_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_com_conferencia_pedido ON brasil_saas.bc_com_conferencia_fatura USING btree (empresa_id, pedido_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_cont_rateio_lanc; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_cont_rateio_lanc ON brasil_saas.bc_cont_rateio USING btree (empresa_id, lancamento_id);


--
-- Name: ix_cont_snapshot; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_cont_snapshot ON brasil_saas.bc_cont_relatorio_snapshot USING btree (empresa_id, periodo_id, tipo);


--
-- Name: ix_crm_lead_etapa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_crm_lead_etapa ON brasil_saas.bc_crm_lead USING btree (empresa_id, etapa) WHERE (deleted_at IS NULL);


--
-- Name: ix_crm_tarefa_agenda; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_crm_tarefa_agenda ON brasil_saas.bc_crm_tarefa USING btree (empresa_id, data_agendada) WHERE (deleted_at IS NULL);


--
-- Name: ix_crm_tarefa_lead; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_crm_tarefa_lead ON brasil_saas.bc_crm_tarefa USING btree (lead_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_ctb_lanc_periodo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ctb_lanc_periodo ON brasil_saas.bc_ctb_lancamento USING btree (empresa_id, periodo) WHERE (deleted_at IS NULL);


--
-- Name: ix_ctb_part_conta; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ctb_part_conta ON brasil_saas.bc_ctb_partida USING btree (empresa_id, conta_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_ctb_part_lanc; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ctb_part_lanc ON brasil_saas.bc_ctb_partida USING btree (lancamento_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_dms_doc_categoria; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_dms_doc_categoria ON brasil_saas.bc_dms_documento USING btree (empresa_id, categoria) WHERE (deleted_at IS NULL);


--
-- Name: ix_dms_doc_entidade; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_dms_doc_entidade ON brasil_saas.bc_dms_documento USING btree (empresa_id, entidade_tipo, entidade_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_ehs_acao_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ehs_acao_tenant ON brasil_saas.bc_ehs_acao USING btree (empresa_id, status);


--
-- Name: ix_ehs_inspecao_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ehs_inspecao_tenant ON brasil_saas.bc_ehs_inspecao USING btree (empresa_id, status);


--
-- Name: ix_ehs_risco_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ehs_risco_tenant ON brasil_saas.bc_ehs_risco USING btree (empresa_id, status);


--
-- Name: ix_ent_cenario_tributario_vigencia; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ent_cenario_tributario_vigencia ON brasil_saas.bc_ent_cenario_tributario USING btree (empresa_id, vigencia_inicio) WHERE (deleted_at IS NULL);


--
-- Name: ix_ent_contrato_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ent_contrato_empresa_status ON brasil_saas.bc_ent_contrato USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: ix_ent_fornecedor_qualificacao_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ent_fornecedor_qualificacao_status ON brasil_saas.bc_ent_fornecedor_qualificacao USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: ix_ent_meta_comercial_periodo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ent_meta_comercial_periodo ON brasil_saas.bc_ent_meta_comercial USING btree (empresa_id, periodo_inicio, periodo_fim) WHERE (deleted_at IS NULL);


--
-- Name: ix_ent_orcamento_competencia; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ent_orcamento_competencia ON brasil_saas.bc_ent_orcamento USING btree (empresa_id, competencia) WHERE (deleted_at IS NULL);


--
-- Name: ix_ent_tesouraria_data; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ent_tesouraria_data ON brasil_saas.bc_ent_tesouraria_previsao USING btree (empresa_id, data_prevista) WHERE (deleted_at IS NULL);


--
-- Name: ix_esocial_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_esocial_status ON brasil_saas.bc_esocial_evento USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: ix_fin_boleto_chave; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_boleto_chave ON brasil_saas.bc_fin_boleto USING btree (empresa_id, banco, nosso_numero_chave);


--
-- Name: ix_fin_boleto_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_boleto_empresa ON brasil_saas.bc_fin_boleto USING btree (empresa_id, criado_em DESC);


--
-- Name: ix_fin_boleto_nosso_numero; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_boleto_nosso_numero ON brasil_saas.bc_fin_boleto USING btree (empresa_id, banco, nosso_numero);


--
-- Name: ix_fin_boleto_titulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_boleto_titulo ON brasil_saas.bc_fin_boleto USING btree (titulo_id);


--
-- Name: ix_fin_boleto_vencimento; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_boleto_vencimento ON brasil_saas.bc_fin_boleto USING btree (empresa_id, vencimento) WHERE ((status)::text = 'EMITIDO'::text);


--
-- Name: ix_fin_intercompany_partner; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_intercompany_partner ON brasil_saas.bc_fin_intercompany USING btree (empresa_id, empresa_parceira_id, competencia) WHERE (deleted_at IS NULL);


--
-- Name: ix_fin_remessa_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_remessa_empresa ON brasil_saas.bc_fin_remessa USING btree (empresa_id, data_geracao DESC);


--
-- Name: ix_fin_remessa_item_remessa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_remessa_item_remessa ON brasil_saas.bc_fin_remessa_item USING btree (remessa_id);


--
-- Name: ix_fin_remessa_item_titulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_remessa_item_titulo ON brasil_saas.bc_fin_remessa_item USING btree (titulo_id);


--
-- Name: ix_fin_remessa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_remessa_status ON brasil_saas.bc_fin_remessa USING btree (empresa_id, status);


--
-- Name: ix_fin_retorno_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_retorno_empresa ON brasil_saas.bc_fin_retorno_bancario USING btree (empresa_id, criado_em DESC);


--
-- Name: ix_fin_retorno_item_nosso_numero; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_retorno_item_nosso_numero ON brasil_saas.bc_fin_retorno_item USING btree (nosso_numero);


--
-- Name: ix_fin_retorno_item_retorno; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_retorno_item_retorno ON brasil_saas.bc_fin_retorno_item USING btree (retorno_id);


--
-- Name: ix_fin_retorno_item_titulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fin_retorno_item_titulo ON brasil_saas.bc_fin_retorno_item USING btree (titulo_id);


--
-- Name: ix_fis_nfse_empresa_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fis_nfse_empresa_status ON brasil_saas.bc_fis_nfse USING btree (empresa_id, status);


--
-- Name: ix_fis_nfse_retorno_indeciso; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fis_nfse_retorno_indeciso ON brasil_saas.bc_fis_nfse_retorno USING btree (empresa_id, created_at DESC) WHERE (sucesso IS NULL);


--
-- Name: ix_fis_nfse_retorno_nfse; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fis_nfse_retorno_nfse ON brasil_saas.bc_fis_nfse_retorno USING btree (nfse_id, created_at DESC);


--
-- Name: ix_fis_nfse_retorno_sem_sucesso; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_fis_nfse_retorno_sem_sucesso ON brasil_saas.bc_fis_nfse_retorno USING btree (empresa_id, created_at DESC) WHERE (sucesso IS FALSE);


--
-- Name: ix_grc_controle_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_grc_controle_tenant ON brasil_saas.bc_grc_controle USING btree (empresa_id, status);


--
-- Name: ix_grc_log_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_grc_log_tenant ON brasil_saas.bc_grc_log USING btree (empresa_id, criado_em);


--
-- Name: ix_grc_plano_acao_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_grc_plano_acao_tenant ON brasil_saas.bc_grc_plano_acao USING btree (empresa_id, status, prazo);


--
-- Name: ix_grc_risco_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_grc_risco_tenant ON brasil_saas.bc_grc_risco USING btree (empresa_id, status);


--
-- Name: ix_grc_teste_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_grc_teste_tenant ON brasil_saas.bc_grc_teste_controle USING btree (empresa_id, periodo);


--
-- Name: ix_int_delivery_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_int_delivery_tenant ON brasil_saas.bc_int_delivery USING btree (empresa_id, status, next_attempt_at);


--
-- Name: ix_int_event_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_int_event_tenant ON brasil_saas.bc_int_webhook_event USING btree (empresa_id, received_at);


--
-- Name: ix_integration_event_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_integration_event_status ON brasil_saas.bc_core_integration_event USING btree (status, created_at);


--
-- Name: ix_obrigacao_entrega_comp; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_obrigacao_entrega_comp ON brasil_saas.bc_fis_obrigacao_entrega USING btree (empresa_id, competencia) WHERE (deleted_at IS NULL);


--
-- Name: ix_pcp_mps_periodo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_pcp_mps_periodo ON brasil_saas.bc_pcp_mps USING btree (empresa_id, periodo) WHERE (deleted_at IS NULL);


--
-- Name: ix_plm_aprovacao_workflow; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_plm_aprovacao_workflow ON brasil_saas.bc_plm_aprovacao USING btree (empresa_id, mudanca_id, etapa, decisao);


--
-- Name: ix_plm_doc_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_plm_doc_tenant ON brasil_saas.bc_plm_documento USING btree (empresa_id, status);


--
-- Name: ix_plm_efeito_execucao; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_plm_efeito_execucao ON brasil_saas.bc_plm_efeito_mudanca USING btree (empresa_id, mudanca_id, ordem_execucao);


--
-- Name: ix_plm_efeito_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_plm_efeito_tenant ON brasil_saas.bc_plm_efeito_mudanca USING btree (empresa_id, mudanca_id);


--
-- Name: ix_prj_etapa_proj; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_prj_etapa_proj ON brasil_saas.bc_prj_etapa USING btree (projeto_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_prj_mov_proj; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_prj_mov_proj ON brasil_saas.bc_prj_movimento USING btree (projeto_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_prod_aloc_cap; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_prod_aloc_cap ON brasil_saas.bc_prod_alocacao_capacidade USING btree (empresa_id, centro_trabalho_id, data) WHERE (deleted_at IS NULL);


--
-- Name: ix_ptl_acesso_pessoa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ptl_acesso_pessoa ON brasil_saas.bc_ptl_acesso USING btree (empresa_id, pessoa_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_rh_ferias_func; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_rh_ferias_func ON brasil_saas.bc_rh_ferias USING btree (empresa_id, funcionario_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_rh_ponto_mes; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_rh_ponto_mes ON brasil_saas.bc_rh_ponto USING btree (empresa_id, data) WHERE (deleted_at IS NULL);


--
-- Name: ix_sc_demanda_periodo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_sc_demanda_periodo ON brasil_saas.bc_sc_demanda USING btree (empresa_id, produto_id, periodo);


--
-- Name: ix_sc_tracking_carga; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_sc_tracking_carga ON brasil_saas.bc_sc_tracking USING btree (empresa_id, carga_id, data_evento);


--
-- Name: ix_scm_reposicao_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_scm_reposicao_produto ON brasil_saas.bc_scm_politica_reposicao USING btree (empresa_id, produto_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_scm_transporte_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_scm_transporte_status ON brasil_saas.bc_scm_ordem_transporte USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: ix_tms_entrega_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_tms_entrega_tenant ON brasil_saas.bc_tms_documento_entrega USING btree (empresa_id, ordem_id);


--
-- Name: ix_tms_evento_ordem; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_tms_evento_ordem ON brasil_saas.bc_tms_evento USING btree (empresa_id, ordem_id, data_evento);


--
-- Name: ix_tms_ordem_rota; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_tms_ordem_rota ON brasil_saas.bc_tms_ordem USING btree (empresa_id, rota_id);


--
-- Name: ix_tms_ordem_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_tms_ordem_tenant ON brasil_saas.bc_tms_ordem USING btree (empresa_id, status);


--
-- Name: ix_tms_parada_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_tms_parada_tenant ON brasil_saas.bc_tms_parada USING btree (empresa_id, ordem_id, sequencia);


--
-- Name: ix_tms_rota_tenant; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_tms_rota_tenant ON brasil_saas.bc_tms_rota USING btree (empresa_id, ativo);


--
-- Name: ix_ue_empresa; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ue_empresa ON brasil_saas.bc_core_usuario_empresa USING btree (empresa_id);


--
-- Name: ix_ue_perfil; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ue_perfil ON brasil_saas.bc_core_usuario_empresa USING btree (perfil_id);


--
-- Name: ix_ue_usuario; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ue_usuario ON brasil_saas.bc_core_usuario_empresa USING btree (usuario_id);


--
-- Name: ix_usuario_modulo_modulo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_usuario_modulo_modulo ON brasil_saas.bc_core_usuario_modulo USING btree (modulo_id);


--
-- Name: ix_usuario_modulo_usuario; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_usuario_modulo_usuario ON brasil_saas.bc_core_usuario_modulo USING btree (usuario_id);


--
-- Name: ix_ven_dev_pedido; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_ven_dev_pedido ON brasil_saas.bc_ven_devolucao_item USING btree (devolucao_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_wkf_instance_entidade; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_wkf_instance_entidade ON brasil_saas.bc_wkf_instance USING btree (empresa_id, entidade_tipo, entidade_id);


--
-- Name: ix_wkf_task_pendente; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_wkf_task_pendente ON brasil_saas.bc_wkf_task USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: ix_wms_onda_item; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_wms_onda_item ON brasil_saas.bc_wms_onda_item USING btree (onda_id) WHERE (deleted_at IS NULL);


--
-- Name: ix_wms_onda_status; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_wms_onda_status ON brasil_saas.bc_wms_onda USING btree (empresa_id, status) WHERE (deleted_at IS NULL);


--
-- Name: ix_wms_vol_exp; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE INDEX ix_wms_vol_exp ON brasil_saas.bc_wms_volume USING btree (expedicao_id) WHERE (deleted_at IS NULL);


--
-- Name: uk_bc_fin_caixa_empresa_nome; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_bc_fin_caixa_empresa_nome ON brasil_saas.bc_fin_caixa USING btree (empresa_id, lower((nome)::text));


--
-- Name: uk_com_contrato_numero; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_com_contrato_numero ON brasil_saas.bc_com_contrato USING btree (empresa_id, numero) WHERE (deleted_at IS NULL);


--
-- Name: uk_empresa_matriz_por_grupo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_empresa_matriz_por_grupo ON brasil_saas.bc_core_empresa USING btree (grupo_cnpj_cpf) WHERE (matriz AND (deleted_at IS NULL));


--
-- Name: uk_est_deposito_codigo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_deposito_codigo ON brasil_saas.bc_est_deposito USING btree (empresa_id, codigo) WHERE (deleted_at IS NULL);


--
-- Name: uk_est_endereco_codigo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_endereco_codigo ON brasil_saas.bc_est_endereco USING btree (deposito_id, codigo) WHERE (deleted_at IS NULL);


--
-- Name: uk_est_expedicao_pedido_aberta; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_expedicao_pedido_aberta ON brasil_saas.bc_est_expedicao USING btree (empresa_id, pedido_venda_id) WHERE ((deleted_at IS NULL) AND ((status)::text <> ALL (ARRAY[('EXPEDIDA'::character varying)::text, ('CANCELADA'::character varying)::text])));


--
-- Name: uk_est_lote_produto_codigo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_lote_produto_codigo ON brasil_saas.bc_est_lote USING btree (empresa_id, produto_id, codigo) WHERE (deleted_at IS NULL);


--
-- Name: uk_est_reserva_pedido_produto_deposito_lote; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_reserva_pedido_produto_deposito_lote ON brasil_saas.bc_est_reserva USING btree (empresa_id, pedido_venda_id, deposito_id, produto_id, lote_id) WHERE ((pedido_venda_id IS NOT NULL) AND (lote_id IS NOT NULL) AND (deleted_at IS NULL) AND ((status)::text = ANY (ARRAY[('RESERVADA'::character varying)::text, ('SEPARACAO'::character varying)::text])));


--
-- Name: uk_est_reserva_pedido_produto_deposito_sem_lote; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_reserva_pedido_produto_deposito_sem_lote ON brasil_saas.bc_est_reserva USING btree (empresa_id, pedido_venda_id, deposito_id, produto_id) WHERE ((pedido_venda_id IS NOT NULL) AND (lote_id IS NULL) AND (deleted_at IS NULL) AND ((status)::text = ANY (ARRAY[('RESERVADA'::character varying)::text, ('SEPARACAO'::character varying)::text])));


--
-- Name: uk_est_saldo_empresa_deposito_produto; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_saldo_empresa_deposito_produto ON brasil_saas.bc_est_saldo USING btree (empresa_id, deposito_id, produto_id);


--
-- Name: uk_est_serie_numero; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_est_serie_numero ON brasil_saas.bc_est_serie USING btree (empresa_id, numero_serie) WHERE (deleted_at IS NULL);


--
-- Name: uk_extrato_fitid; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_extrato_fitid ON brasil_saas.bc_fin_extrato USING btree (empresa_id, conta_bancaria_id, fitid) WHERE ((deleted_at IS NULL) AND (fitid IS NOT NULL));


--
-- Name: uk_fin_stripe_payment_session; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_fin_stripe_payment_session ON brasil_saas.bc_fin_stripe_payment USING btree (checkout_session_id) WHERE (checkout_session_id IS NOT NULL);


--
-- Name: uk_palavra; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_palavra ON brasil_saas.bc_fis_palavra_chave USING btree (tabela, codigo, palavra) WHERE (deleted_at IS NULL);


--
-- Name: uk_prod_estrutura_item; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_prod_estrutura_item ON brasil_saas.bc_prod_estrutura USING btree (empresa_id, produto_pai_id, produto_filho_id) WHERE (deleted_at IS NULL);


--
-- Name: uk_tms_parada_ref; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_tms_parada_ref ON brasil_saas.bc_tms_parada USING btree (empresa_id, referencia_tipo, referencia_id) WHERE ((referencia_id IS NOT NULL) AND ((status)::text <> 'CANCELADA'::text));


--
-- Name: uk_ven_tabela_preco_codigo; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX uk_ven_tabela_preco_codigo ON brasil_saas.bc_ven_tabela_preco USING btree (empresa_id, codigo) WHERE ((codigo IS NOT NULL) AND (deleted_at IS NULL));


--
-- Name: ux_bc_fis_nfe_item_uuid; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX ux_bc_fis_nfe_item_uuid ON brasil_saas.bc_fis_nfe_item USING btree (uuid);


--
-- Name: ux_cad_servico_codigo_municipal; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX ux_cad_servico_codigo_municipal ON brasil_saas.bc_cad_servico USING btree (empresa_id, codigo_tributacao_municipal) WHERE ((codigo_tributacao_municipal IS NOT NULL) AND (deleted_at IS NULL));


--
-- Name: ux_fis_nfse_chave_nacional; Type: INDEX; Schema: brasil_saas; Owner: sa
--

CREATE UNIQUE INDEX ux_fis_nfse_chave_nacional ON brasil_saas.bc_fis_nfse USING btree (chave_nota_nacional) WHERE ((chave_nota_nacional IS NOT NULL) AND (deleted_at IS NULL));


--
-- Name: idx_dl_cliente_empresa; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_cliente_empresa ON brasil_saas_dl.dim_cliente USING btree (empresa_id);


--
-- Name: idx_dl_compras_data_empresa; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_compras_data_empresa ON brasil_saas_dl.ft_compras USING btree (data_id, empresa_id);


--
-- Name: idx_dl_estoque_data_empresa; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_estoque_data_empresa ON brasil_saas_dl.ft_estoque USING btree (data_id, empresa_id);


--
-- Name: idx_dl_financeiro_data_empresa; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_financeiro_data_empresa ON brasil_saas_dl.ft_financeiro USING btree (data_id, empresa_id);


--
-- Name: idx_dl_producao_data_empresa; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_producao_data_empresa ON brasil_saas_dl.ft_producao USING btree (data_id, empresa_id);


--
-- Name: idx_dl_produto_empresa; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_produto_empresa ON brasil_saas_dl.dim_produto USING btree (empresa_id);


--
-- Name: idx_dl_tempo_ano_mes; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_tempo_ano_mes ON brasil_saas_dl.dim_tempo USING btree (ano, mes);


--
-- Name: idx_dl_vendas_data_empresa; Type: INDEX; Schema: brasil_saas_dl; Owner: sa
--

CREATE INDEX idx_dl_vendas_data_empresa ON brasil_saas_dl.ft_vendas USING btree (data_id, empresa_id);


--
-- Name: idx_fprodvar_ean; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fprodvar_ean ON public.fproduto_variacao USING btree (codigo_barras);


--
-- Name: idx_fprodvar_produto; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fprodvar_produto ON public.fproduto_variacao USING btree (codproduto);


--
-- Name: idx_fvenda_cliente; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fvenda_cliente ON public.fvenda USING btree (id_cliente);


--
-- Name: idx_fvenda_data; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fvenda_data ON public.fvenda USING btree (data_venda);


--
-- Name: idx_tissqn_cod; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tissqn_cod ON public.tissqn USING btree (codigo_servico);


--
-- Name: idx_tissqn_desc; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tissqn_desc ON public.tissqn USING btree (descricao);


--
-- Name: idx_tissqn_mun; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tissqn_mun ON public.tissqn USING btree (municipio);


--
-- Name: idx_tmunicipio_ibge; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tmunicipio_ibge ON public.tmunicipio USING btree (codigoibge);


--
-- Name: idx_tmunicipio_nome; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tmunicipio_nome ON public.tmunicipio USING btree (nomemunucipio);


--
-- Name: bc_core_usuario_empresa trg_repetir_papel_proibido; Type: TRIGGER; Schema: brasil_saas; Owner: sa
--

CREATE TRIGGER trg_repetir_papel_proibido BEFORE INSERT OR UPDATE ON brasil_saas.bc_core_usuario_empresa FOR EACH ROW EXECUTE FUNCTION brasil_saas.fn_repetir_papel_proibido();


--
-- Name: bc_bi_dashboard bc_bi_dashboard_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard
    ADD CONSTRAINT bc_bi_dashboard_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_bi_dashboard_widget bc_bi_dashboard_widget_dashboard_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_dashboard_widget
    ADD CONSTRAINT bc_bi_dashboard_widget_dashboard_id_fkey FOREIGN KEY (dashboard_id) REFERENCES brasil_saas.bc_bi_dashboard(id) ON DELETE CASCADE;


--
-- Name: bc_bi_indicador bc_bi_indicador_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_indicador
    ADD CONSTRAINT bc_bi_indicador_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_bi_kpi bc_bi_kpi_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_kpi
    ADD CONSTRAINT bc_bi_kpi_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_bi_relatorio_agendado bc_bi_relatorio_agendado_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_bi_relatorio_agendado bc_bi_relatorio_agendado_relatorio_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio_agendado
    ADD CONSTRAINT bc_bi_relatorio_agendado_relatorio_id_fkey FOREIGN KEY (relatorio_id) REFERENCES brasil_saas.bc_bi_relatorio(id) ON DELETE SET NULL;


--
-- Name: bc_bi_relatorio bc_bi_relatorio_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_relatorio
    ADD CONSTRAINT bc_bi_relatorio_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_bi_report bc_bi_report_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report
    ADD CONSTRAINT bc_bi_report_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_bi_report_parameter bc_bi_report_parameter_report_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_bi_report_parameter
    ADD CONSTRAINT bc_bi_report_parameter_report_id_fkey FOREIGN KEY (report_id) REFERENCES brasil_saas.bc_bi_report(id) ON DELETE CASCADE;


--
-- Name: bc_cad_base_cep bc_cad_base_cep_municipio_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_base_cep
    ADD CONSTRAINT bc_cad_base_cep_municipio_id_fkey FOREIGN KEY (municipio_id) REFERENCES brasil_saas.bc_cad_municipio(id);


--
-- Name: bc_cad_categoria bc_cad_categoria_categoria_pai_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_categoria_pai_id_fkey FOREIGN KEY (categoria_pai_id) REFERENCES brasil_saas.bc_cad_categoria(id);


--
-- Name: bc_cad_categoria bc_cad_categoria_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_categoria
    ADD CONSTRAINT bc_cad_categoria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_cliente bc_cad_cliente_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_cliente bc_cad_cliente_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_cliente
    ADD CONSTRAINT bc_cad_cliente_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_contato bc_cad_contato_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_contato bc_cad_contato_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_contato
    ADD CONSTRAINT bc_cad_contato_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_documento_fiscal bc_cad_documento_fiscal_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_documento_fiscal bc_cad_documento_fiscal_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_documento_fiscal
    ADD CONSTRAINT bc_cad_documento_fiscal_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_endereco bc_cad_endereco_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_endereco bc_cad_endereco_municipio_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_municipio_id_fkey FOREIGN KEY (municipio_id) REFERENCES brasil_saas.bc_cad_municipio(id);


--
-- Name: bc_cad_endereco bc_cad_endereco_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_endereco
    ADD CONSTRAINT bc_cad_endereco_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_fornecedor bc_cad_fornecedor_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_fornecedor bc_cad_fornecedor_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_fornecedor
    ADD CONSTRAINT bc_cad_fornecedor_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_marca bc_cad_marca_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_marca
    ADD CONSTRAINT bc_cad_marca_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_papel bc_cad_papel_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_papel
    ADD CONSTRAINT bc_cad_papel_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_pessoa bc_cad_pessoa_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa
    ADD CONSTRAINT bc_cad_pessoa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_pessoa_fisica bc_cad_pessoa_fisica_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_fisica
    ADD CONSTRAINT bc_cad_pessoa_fisica_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_pessoa_juridica bc_cad_pessoa_juridica_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_pessoa_juridica
    ADD CONSTRAINT bc_cad_pessoa_juridica_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_produto bc_cad_produto_categoria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES brasil_saas.bc_cad_categoria(id);


--
-- Name: bc_cad_produto_ecommerce bc_cad_produto_ecommerce_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_produto_ecommerce bc_cad_produto_ecommerce_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_ecommerce
    ADD CONSTRAINT bc_cad_produto_ecommerce_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_cad_produto bc_cad_produto_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_produto_imagem bc_cad_produto_imagem_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem
    ADD CONSTRAINT bc_cad_produto_imagem_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_produto_imagem bc_cad_produto_imagem_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_imagem
    ADD CONSTRAINT bc_cad_produto_imagem_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_cad_produto_kit bc_cad_produto_kit_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_produto_kit bc_cad_produto_kit_item_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_item_id_fkey FOREIGN KEY (item_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_cad_produto_kit bc_cad_produto_kit_kit_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_kit
    ADD CONSTRAINT bc_cad_produto_kit_kit_id_fkey FOREIGN KEY (kit_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_cad_produto bc_cad_produto_marca_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_marca_id_fkey FOREIGN KEY (marca_id) REFERENCES brasil_saas.bc_cad_marca(id);


--
-- Name: bc_cad_produto bc_cad_produto_unidade_medida_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto
    ADD CONSTRAINT bc_cad_produto_unidade_medida_id_fkey FOREIGN KEY (unidade_medida_id) REFERENCES brasil_saas.bc_cad_unidade_medida(id);


--
-- Name: bc_cad_produto_variacao bc_cad_produto_variacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_produto_variacao bc_cad_produto_variacao_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_produto_variacao
    ADD CONSTRAINT bc_cad_produto_variacao_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_cad_servico bc_cad_servico_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_servico bc_cad_servico_unidade_medida_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_servico
    ADD CONSTRAINT bc_cad_servico_unidade_medida_id_fkey FOREIGN KEY (unidade_medida_id) REFERENCES brasil_saas.bc_cad_unidade_medida(id);


--
-- Name: bc_cad_transportadora bc_cad_transportadora_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cad_transportadora bc_cad_transportadora_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_transportadora
    ADD CONSTRAINT bc_cad_transportadora_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_cad_unidade_medida bc_cad_unidade_medida_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cad_unidade_medida
    ADD CONSTRAINT bc_cad_unidade_medida_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_cmp_devolucao_item bc_cmp_devolucao_item_devolucao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_cmp_devolucao_item
    ADD CONSTRAINT bc_cmp_devolucao_item_devolucao_id_fkey FOREIGN KEY (devolucao_id) REFERENCES brasil_saas.bc_cmp_devolucao(id);


--
-- Name: bc_com_conferencia_fatura bc_com_conferencia_fatura_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_conferencia_fatura_item bc_com_conferencia_fatura_item_nfe_item_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura_item
    ADD CONSTRAINT bc_com_conferencia_fatura_item_nfe_item_id_fkey FOREIGN KEY (nfe_item_id) REFERENCES brasil_saas.bc_fis_nfe_item(id);


--
-- Name: bc_com_conferencia_fatura_item bc_com_conferencia_fatura_item_pedido_item_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura_item
    ADD CONSTRAINT bc_com_conferencia_fatura_item_pedido_item_id_fkey FOREIGN KEY (pedido_item_id) REFERENCES brasil_saas.bc_com_pedido_item(id);


--
-- Name: bc_com_conferencia_fatura_item bc_com_conferencia_fatura_item_recebimento_item_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura_item
    ADD CONSTRAINT bc_com_conferencia_fatura_item_recebimento_item_id_fkey FOREIGN KEY (recebimento_item_id) REFERENCES brasil_saas.bc_com_recebimento_item(id);


--
-- Name: bc_com_conferencia_fatura bc_com_conferencia_fatura_nfe_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_nfe_fkey FOREIGN KEY (nfe_id) REFERENCES brasil_saas.bc_fis_nfe(id);


--
-- Name: bc_com_conferencia_fatura bc_com_conferencia_fatura_pedido_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_pedido_id_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_com_pedido(id);


--
-- Name: bc_com_conferencia_fatura bc_com_conferencia_fatura_recebimento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_recebimento_id_fkey FOREIGN KEY (recebimento_id) REFERENCES brasil_saas.bc_com_recebimento(id);


--
-- Name: bc_com_conferencia_fatura bc_com_conferencia_fatura_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_conferencia_fatura
    ADD CONSTRAINT bc_com_conferencia_fatura_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_com_contrato bc_com_contrato_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_contrato bc_com_contrato_fornecedor_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato
    ADD CONSTRAINT bc_com_contrato_fornecedor_id_fkey FOREIGN KEY (fornecedor_id) REFERENCES brasil_saas.bc_cad_fornecedor(id);


--
-- Name: bc_com_contrato_item bc_com_contrato_item_contrato_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_contrato_item
    ADD CONSTRAINT bc_com_contrato_item_contrato_id_fkey FOREIGN KEY (contrato_id) REFERENCES brasil_saas.bc_com_contrato(id);


--
-- Name: bc_com_cotacao bc_com_cotacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_cotacao_fornecedor bc_com_cotacao_fornecedor_condicao_pagamento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_condicao_pagamento_id_fkey FOREIGN KEY (condicao_pagamento_id) REFERENCES brasil_saas.bc_fin_condicao_pagamento(id);


--
-- Name: bc_com_cotacao_fornecedor bc_com_cotacao_fornecedor_cotacao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_cotacao_id_fkey FOREIGN KEY (cotacao_id) REFERENCES brasil_saas.bc_com_cotacao(id);


--
-- Name: bc_com_cotacao_fornecedor bc_com_cotacao_fornecedor_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_cotacao_fornecedor bc_com_cotacao_fornecedor_fornecedor_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_fornecedor
    ADD CONSTRAINT bc_com_cotacao_fornecedor_fornecedor_id_fkey FOREIGN KEY (fornecedor_id) REFERENCES brasil_saas.bc_cad_fornecedor(id);


--
-- Name: bc_com_cotacao_item bc_com_cotacao_item_cotacao_fornecedor_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_cotacao_fornecedor_id_fkey FOREIGN KEY (cotacao_fornecedor_id) REFERENCES brasil_saas.bc_com_cotacao_fornecedor(id);


--
-- Name: bc_com_cotacao_item bc_com_cotacao_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_cotacao_item bc_com_cotacao_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao_item
    ADD CONSTRAINT bc_com_cotacao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_com_cotacao bc_com_cotacao_solicitacao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_cotacao
    ADD CONSTRAINT bc_com_cotacao_solicitacao_id_fkey FOREIGN KEY (solicitacao_id) REFERENCES brasil_saas.bc_com_solicitacao(id);


--
-- Name: bc_com_pedido_item bc_com_item_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_item_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_pedido_item bc_com_item_pedido_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_item_pedido_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_com_pedido(id);


--
-- Name: bc_com_pedido_item bc_com_item_produto_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido_item
    ADD CONSTRAINT bc_com_item_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_com_pedido bc_com_pedido_cond_pagto_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_cond_pagto_fkey FOREIGN KEY (condicao_pagamento_id) REFERENCES brasil_saas.bc_fin_condicao_pagamento(id);


--
-- Name: bc_com_pedido bc_com_pedido_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_pedido bc_com_pedido_fornecedor_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_fornecedor_fkey FOREIGN KEY (fornecedor_id) REFERENCES brasil_saas.bc_cad_fornecedor(id);


--
-- Name: bc_com_pedido bc_com_pedido_titulo_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_pedido
    ADD CONSTRAINT bc_com_pedido_titulo_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_com_recebimento bc_com_recebimento_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_recebimento_item bc_com_recebimento_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_recebimento_item bc_com_recebimento_item_lote_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);


--
-- Name: bc_com_recebimento_item bc_com_recebimento_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_com_recebimento_item bc_com_recebimento_item_recebimento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento_item
    ADD CONSTRAINT bc_com_recebimento_item_recebimento_id_fkey FOREIGN KEY (recebimento_id) REFERENCES brasil_saas.bc_com_recebimento(id);


--
-- Name: bc_com_recebimento bc_com_recebimento_pedido_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_recebimento
    ADD CONSTRAINT bc_com_recebimento_pedido_id_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_com_pedido(id);


--
-- Name: bc_com_solicitacao bc_com_solicitacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao
    ADD CONSTRAINT bc_com_solicitacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_solicitacao_item bc_com_solicitacao_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_com_solicitacao_item bc_com_solicitacao_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_com_solicitacao_item bc_com_solicitacao_item_solicitacao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_com_solicitacao_item
    ADD CONSTRAINT bc_com_solicitacao_item_solicitacao_id_fkey FOREIGN KEY (solicitacao_id) REFERENCES brasil_saas.bc_com_solicitacao(id);


--
-- Name: bc_core_auditoria bc_core_auditoria_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_auditoria bc_core_auditoria_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_auditoria
    ADD CONSTRAINT bc_core_auditoria_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_core_configuracao bc_core_configuracao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_configuracao
    ADD CONSTRAINT bc_core_configuracao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_empresa_vinculo bc_core_empresa_vinculo_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_empresa_vinculo bc_core_empresa_vinculo_empresa_vinculada_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa_vinculo
    ADD CONSTRAINT bc_core_empresa_vinculo_empresa_vinculada_id_fkey FOREIGN KEY (empresa_vinculada_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_log_acesso bc_core_log_acesso_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso
    ADD CONSTRAINT bc_core_log_acesso_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_log_acesso bc_core_log_acesso_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_log_acesso
    ADD CONSTRAINT bc_core_log_acesso_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_core_notificacao bc_core_notificacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_notificacao bc_core_notificacao_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_notificacao
    ADD CONSTRAINT bc_core_notificacao_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_core_perfil bc_core_perfil_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT bc_core_perfil_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_perfil_permissao bc_core_perfil_permissao_perfil_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT bc_core_perfil_permissao_perfil_id_fkey FOREIGN KEY (perfil_id) REFERENCES brasil_saas.bc_core_perfil(id);


--
-- Name: bc_core_perfil_permissao bc_core_perfil_permissao_permissao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil_permissao
    ADD CONSTRAINT bc_core_perfil_permissao_permissao_id_fkey FOREIGN KEY (permissao_id) REFERENCES brasil_saas.bc_core_permissao(id);


--
-- Name: bc_core_sessao bc_core_sessao_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_sessao
    ADD CONSTRAINT bc_core_sessao_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_core_usuario bc_core_usuario_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario
    ADD CONSTRAINT bc_core_usuario_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_usuario_modulo bc_core_usuario_modulo_modulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT bc_core_usuario_modulo_modulo_id_fkey FOREIGN KEY (modulo_id) REFERENCES brasil_saas.bc_core_modulo(id) ON DELETE CASCADE;


--
-- Name: bc_core_usuario_modulo bc_core_usuario_modulo_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_modulo
    ADD CONSTRAINT bc_core_usuario_modulo_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id) ON DELETE CASCADE;


--
-- Name: bc_core_usuario_perfil bc_core_usuario_perfil_perfil_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT bc_core_usuario_perfil_perfil_id_fkey FOREIGN KEY (perfil_id) REFERENCES brasil_saas.bc_core_perfil(id);


--
-- Name: bc_core_usuario_perfil bc_core_usuario_perfil_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_perfil
    ADD CONSTRAINT bc_core_usuario_perfil_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_crm_atividade bc_crm_atividade_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_crm_atividade bc_crm_atividade_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_crm_atividade bc_crm_atividade_oportunidade_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_oportunidade_id_fkey FOREIGN KEY (oportunidade_id) REFERENCES brasil_saas.bc_crm_oportunidade(id);


--
-- Name: bc_crm_atividade bc_crm_atividade_vendedor_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_atividade
    ADD CONSTRAINT bc_crm_atividade_vendedor_id_fkey FOREIGN KEY (vendedor_id) REFERENCES brasil_saas.bc_rh_funcionario(id);


--
-- Name: bc_crm_oportunidade bc_crm_oportunidade_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_crm_oportunidade bc_crm_oportunidade_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_crm_oportunidade bc_crm_oportunidade_vendedor_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_oportunidade
    ADD CONSTRAINT bc_crm_oportunidade_vendedor_id_fkey FOREIGN KEY (vendedor_id) REFERENCES brasil_saas.bc_rh_funcionario(id);


--
-- Name: bc_crm_tarefa bc_crm_tarefa_lead_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_crm_tarefa
    ADD CONSTRAINT bc_crm_tarefa_lead_id_fkey FOREIGN KEY (lead_id) REFERENCES brasil_saas.bc_crm_lead(id);


--
-- Name: bc_ctb_partida bc_ctb_partida_lancamento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ctb_partida
    ADD CONSTRAINT bc_ctb_partida_lancamento_id_fkey FOREIGN KEY (lancamento_id) REFERENCES brasil_saas.bc_ctb_lancamento(id);


--
-- Name: bc_dms_aprovacao bc_dms_aprovacao_documento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_aprovacao
    ADD CONSTRAINT bc_dms_aprovacao_documento_id_fkey FOREIGN KEY (documento_id) REFERENCES brasil_saas.bc_dms_documento(id);


--
-- Name: bc_dms_versao bc_dms_versao_documento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_dms_versao
    ADD CONSTRAINT bc_dms_versao_documento_id_fkey FOREIGN KEY (documento_id) REFERENCES brasil_saas.bc_dms_documento(id);


--
-- Name: bc_ehs_acao bc_ehs_acao_inspecao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_acao
    ADD CONSTRAINT bc_ehs_acao_inspecao_id_fkey FOREIGN KEY (inspecao_id) REFERENCES brasil_saas.bc_ehs_inspecao(id);


--
-- Name: bc_ehs_acao bc_ehs_acao_ocorrencia_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ehs_acao
    ADD CONSTRAINT bc_ehs_acao_ocorrencia_id_fkey FOREIGN KEY (ocorrencia_id) REFERENCES brasil_saas.bc_ehs_ocorrencia(id);


--
-- Name: bc_est_deposito bc_est_deposito_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_deposito
    ADD CONSTRAINT bc_est_deposito_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_endereco bc_est_endereco_deposito_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_endereco bc_est_endereco_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_endereco
    ADD CONSTRAINT bc_est_endereco_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_expedicao bc_est_expedicao_deposito_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_expedicao bc_est_expedicao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_endereco_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_endereco_fkey FOREIGN KEY (endereco_id) REFERENCES brasil_saas.bc_est_endereco(id);


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_expedicao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_expedicao_id_fkey FOREIGN KEY (expedicao_id) REFERENCES brasil_saas.bc_est_expedicao(id) ON DELETE CASCADE;


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_lote_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_lote_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_expedicao_item bc_est_expedicao_item_reserva_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao_item
    ADD CONSTRAINT bc_est_expedicao_item_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES brasil_saas.bc_est_reserva(id);


--
-- Name: bc_est_expedicao bc_est_expedicao_pedido_venda_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_expedicao
    ADD CONSTRAINT bc_est_expedicao_pedido_venda_id_fkey FOREIGN KEY (pedido_venda_id) REFERENCES brasil_saas.bc_ven_pedido(id);


--
-- Name: bc_est_inventario bc_est_inventario_deposito_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_inventario bc_est_inventario_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario
    ADD CONSTRAINT bc_est_inventario_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_inventario_item bc_est_inventario_item_endereco_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_endereco_id_fkey FOREIGN KEY (endereco_id) REFERENCES brasil_saas.bc_est_endereco(id);


--
-- Name: bc_est_inventario_item bc_est_inventario_item_inventario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_inventario_id_fkey FOREIGN KEY (inventario_id) REFERENCES brasil_saas.bc_est_inventario(id) ON DELETE CASCADE;


--
-- Name: bc_est_inventario_item bc_est_inventario_item_lote_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);


--
-- Name: bc_est_inventario_item bc_est_inventario_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_inventario_item
    ADD CONSTRAINT bc_est_inventario_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_lote bc_est_lote_deposito_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_lote bc_est_lote_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_lote bc_est_lote_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_lote
    ADD CONSTRAINT bc_est_lote_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_movimentacao bc_est_mov_deposito_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_mov_deposito_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_movimentacao bc_est_mov_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_mov_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_movimentacao bc_est_mov_produto_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_movimentacao
    ADD CONSTRAINT bc_est_mov_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_reserva bc_est_reserva_deposito_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_reserva bc_est_reserva_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_reserva bc_est_reserva_endereco_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_endereco_fkey FOREIGN KEY (endereco_id) REFERENCES brasil_saas.bc_est_endereco(id);


--
-- Name: bc_est_reserva bc_est_reserva_lote_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_lote_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);


--
-- Name: bc_est_reserva bc_est_reserva_pedido_venda_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_pedido_venda_id_fkey FOREIGN KEY (pedido_venda_id) REFERENCES brasil_saas.bc_ven_pedido(id);


--
-- Name: bc_est_reserva bc_est_reserva_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_reserva
    ADD CONSTRAINT bc_est_reserva_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_saldo bc_est_saldo_deposito_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_deposito_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_saldo bc_est_saldo_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_saldo bc_est_saldo_produto_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_saldo
    ADD CONSTRAINT bc_est_saldo_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_serie bc_est_serie_deposito_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_deposito_id_fkey FOREIGN KEY (deposito_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_serie bc_est_serie_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_serie bc_est_serie_lote_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);


--
-- Name: bc_est_serie bc_est_serie_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_serie
    ADD CONSTRAINT bc_est_serie_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_transferencia bc_est_transferencia_deposito_destino_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_deposito_destino_id_fkey FOREIGN KEY (deposito_destino_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_transferencia bc_est_transferencia_deposito_origem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_deposito_origem_id_fkey FOREIGN KEY (deposito_origem_id) REFERENCES brasil_saas.bc_est_deposito(id);


--
-- Name: bc_est_transferencia bc_est_transferencia_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia
    ADD CONSTRAINT bc_est_transferencia_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_est_transferencia_item bc_est_transferencia_item_endereco_destino_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_endereco_destino_fkey FOREIGN KEY (endereco_destino_id) REFERENCES brasil_saas.bc_est_endereco(id);


--
-- Name: bc_est_transferencia_item bc_est_transferencia_item_endereco_origem_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_endereco_origem_fkey FOREIGN KEY (endereco_origem_id) REFERENCES brasil_saas.bc_est_endereco(id);


--
-- Name: bc_est_transferencia_item bc_est_transferencia_item_lote_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_lote_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);


--
-- Name: bc_est_transferencia_item bc_est_transferencia_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_est_transferencia_item bc_est_transferencia_item_transferencia_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_est_transferencia_item
    ADD CONSTRAINT bc_est_transferencia_item_transferencia_id_fkey FOREIGN KEY (transferencia_id) REFERENCES brasil_saas.bc_est_transferencia(id) ON DELETE CASCADE;


--
-- Name: bc_fin_analise_rentabilidade bc_fin_analise_rentabilidade_centro_custo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);


--
-- Name: bc_fin_analise_rentabilidade bc_fin_analise_rentabilidade_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_analise_rentabilidade
    ADD CONSTRAINT bc_fin_analise_rentabilidade_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_aplicacao_financeira bc_fin_aplicacao_financeira_conta_bancaria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);


--
-- Name: bc_fin_aplicacao_financeira bc_fin_aplicacao_financeira_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aplicacao_financeira
    ADD CONSTRAINT bc_fin_aplicacao_financeira_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_aprovacao bc_fin_aprovacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_aprovacao bc_fin_aprovacao_fluxo_aprovacao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_fluxo_aprovacao_id_fkey FOREIGN KEY (fluxo_aprovacao_id) REFERENCES brasil_saas.bc_fin_fluxo_aprovacao(id);


--
-- Name: bc_fin_aprovacao bc_fin_aprovacao_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_aprovacao bc_fin_aprovacao_usuario_aprovador_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_aprovacao
    ADD CONSTRAINT bc_fin_aprovacao_usuario_aprovador_id_fkey FOREIGN KEY (usuario_aprovador_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_fin_baixa bc_fin_baixa_conta_bancaria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);


--
-- Name: bc_fin_baixa bc_fin_baixa_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_baixa bc_fin_baixa_parcela_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_parcela_id_fkey FOREIGN KEY (parcela_id) REFERENCES brasil_saas.bc_fin_titulo_parcela(id);


--
-- Name: bc_fin_baixa bc_fin_baixa_tipo_pagamento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_tipo_pagamento_id_fkey FOREIGN KEY (tipo_pagamento_id) REFERENCES brasil_saas.bc_fin_tipo_pagamento(id);


--
-- Name: bc_fin_baixa bc_fin_baixa_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_baixa
    ADD CONSTRAINT bc_fin_baixa_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_boleto bc_fin_boleto_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_boleto bc_fin_boleto_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_boleto
    ADD CONSTRAINT bc_fin_boleto_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_caixa bc_fin_caixa_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_caixa
    ADD CONSTRAINT bc_fin_caixa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_caixa_movimento bc_fin_caixa_movimento_caixa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_caixa_movimento
    ADD CONSTRAINT bc_fin_caixa_movimento_caixa_id_fkey FOREIGN KEY (caixa_id) REFERENCES brasil_saas.bc_fin_caixa(id);


--
-- Name: bc_fin_centro_custo bc_fin_centro_custo_centro_custo_pai_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_centro_custo_pai_id_fkey FOREIGN KEY (centro_custo_pai_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);


--
-- Name: bc_fin_centro_custo bc_fin_centro_custo_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_centro_custo
    ADD CONSTRAINT bc_fin_centro_custo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_conciliacao_bancaria bc_fin_conciliacao_bancaria_conta_bancaria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);


--
-- Name: bc_fin_conciliacao_bancaria bc_fin_conciliacao_bancaria_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_bancaria
    ADD CONSTRAINT bc_fin_conciliacao_bancaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_conciliacao_item bc_fin_conciliacao_item_baixa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_baixa_id_fkey FOREIGN KEY (baixa_id) REFERENCES brasil_saas.bc_fin_baixa(id);


--
-- Name: bc_fin_conciliacao_item bc_fin_conciliacao_item_conciliacao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_conciliacao_id_fkey FOREIGN KEY (conciliacao_id) REFERENCES brasil_saas.bc_fin_conciliacao_bancaria(id);


--
-- Name: bc_fin_conciliacao_item bc_fin_conciliacao_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_conciliacao_item bc_fin_conciliacao_item_extrato_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conciliacao_item
    ADD CONSTRAINT bc_fin_conciliacao_item_extrato_id_fkey FOREIGN KEY (extrato_id) REFERENCES brasil_saas.bc_fin_extrato(id);


--
-- Name: bc_fin_condicao_pagamento bc_fin_condicao_pagamento_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_condicao_pagamento
    ADD CONSTRAINT bc_fin_condicao_pagamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_conta_bancaria bc_fin_conta_bancaria_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_conta_bancaria
    ADD CONSTRAINT bc_fin_conta_bancaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_emprestimo bc_fin_emprestimo_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_emprestimo
    ADD CONSTRAINT bc_fin_emprestimo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_extrato bc_fin_extrato_conta_bancaria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);


--
-- Name: bc_fin_extrato bc_fin_extrato_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_extrato
    ADD CONSTRAINT bc_fin_extrato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_fluxo_aprovacao bc_fin_fluxo_aprovacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_fluxo_aprovacao
    ADD CONSTRAINT bc_fin_fluxo_aprovacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_integracao_bancaria bc_fin_integracao_bancaria_conta_bancaria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);


--
-- Name: bc_fin_integracao_bancaria bc_fin_integracao_bancaria_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_integracao_bancaria
    ADD CONSTRAINT bc_fin_integracao_bancaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_lancamento_contabil bc_fin_lancamento_contabil_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_lancamento_contabil bc_fin_lancamento_contabil_periodo_contabil_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_contabil
    ADD CONSTRAINT bc_fin_lancamento_contabil_periodo_contabil_id_fkey FOREIGN KEY (periodo_contabil_id) REFERENCES brasil_saas.bc_fin_periodo_contabil(id);


--
-- Name: bc_fin_lancamento_partida bc_fin_lancamento_partida_centro_custo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);


--
-- Name: bc_fin_lancamento_partida bc_fin_lancamento_partida_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_lancamento_partida bc_fin_lancamento_partida_lancamento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_lancamento_id_fkey FOREIGN KEY (lancamento_id) REFERENCES brasil_saas.bc_fin_lancamento_contabil(id);


--
-- Name: bc_fin_lancamento_partida bc_fin_lancamento_partida_plano_contas_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_lancamento_partida
    ADD CONSTRAINT bc_fin_lancamento_partida_plano_contas_id_fkey FOREIGN KEY (plano_contas_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);


--
-- Name: bc_fin_orcamento bc_fin_orcamento_centro_custo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);


--
-- Name: bc_fin_orcamento bc_fin_orcamento_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_orcamento bc_fin_orcamento_plano_contas_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento
    ADD CONSTRAINT bc_fin_orcamento_plano_contas_id_fkey FOREIGN KEY (plano_contas_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);


--
-- Name: bc_fin_orcamento_realizado bc_fin_orcamento_realizado_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_orcamento_realizado bc_fin_orcamento_realizado_orcamento_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_orcamento_realizado
    ADD CONSTRAINT bc_fin_orcamento_realizado_orcamento_id_fkey FOREIGN KEY (orcamento_id) REFERENCES brasil_saas.bc_fin_orcamento(id);


--
-- Name: bc_fin_periodo_contabil bc_fin_periodo_contabil_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_periodo_contabil
    ADD CONSTRAINT bc_fin_periodo_contabil_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_plano_contas bc_fin_plano_contas_conta_pai_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_conta_pai_id_fkey FOREIGN KEY (conta_pai_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);


--
-- Name: bc_fin_plano_contas bc_fin_plano_contas_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_plano_contas
    ADD CONSTRAINT bc_fin_plano_contas_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_projecao_fluxo_caixa bc_fin_projecao_fluxo_caixa_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_projecao_fluxo_caixa
    ADD CONSTRAINT bc_fin_projecao_fluxo_caixa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_provisao_pdd bc_fin_provisao_pdd_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_provisao_pdd
    ADD CONSTRAINT bc_fin_provisao_pdd_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_remessa bc_fin_remessa_conta_bancaria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);


--
-- Name: bc_fin_remessa bc_fin_remessa_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa
    ADD CONSTRAINT bc_fin_remessa_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_remessa_item bc_fin_remessa_item_boleto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_boleto_id_fkey FOREIGN KEY (boleto_id) REFERENCES brasil_saas.bc_fin_boleto(id);


--
-- Name: bc_fin_remessa_item bc_fin_remessa_item_remessa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_remessa_id_fkey FOREIGN KEY (remessa_id) REFERENCES brasil_saas.bc_fin_remessa(id) ON DELETE CASCADE;


--
-- Name: bc_fin_remessa_item bc_fin_remessa_item_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_remessa_item
    ADD CONSTRAINT bc_fin_remessa_item_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_renegociacao bc_fin_renegociacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_renegociacao bc_fin_renegociacao_novo_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_novo_titulo_id_fkey FOREIGN KEY (novo_titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_renegociacao bc_fin_renegociacao_titulo_original_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_renegociacao
    ADD CONSTRAINT bc_fin_renegociacao_titulo_original_id_fkey FOREIGN KEY (titulo_original_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_retorno_bancario bc_fin_retorno_bancario_conta_bancaria_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_conta_bancaria_id_fkey FOREIGN KEY (conta_bancaria_id) REFERENCES brasil_saas.bc_fin_conta_bancaria(id);


--
-- Name: bc_fin_retorno_bancario bc_fin_retorno_bancario_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_bancario
    ADD CONSTRAINT bc_fin_retorno_bancario_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_retorno_item bc_fin_retorno_item_boleto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_boleto_id_fkey FOREIGN KEY (boleto_id) REFERENCES brasil_saas.bc_fin_boleto(id);


--
-- Name: bc_fin_retorno_item bc_fin_retorno_item_retorno_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_retorno_id_fkey FOREIGN KEY (retorno_id) REFERENCES brasil_saas.bc_fin_retorno_bancario(id) ON DELETE CASCADE;


--
-- Name: bc_fin_retorno_item bc_fin_retorno_item_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_retorno_item
    ADD CONSTRAINT bc_fin_retorno_item_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_tipo_pagamento bc_fin_tipo_pagamento_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_tipo_pagamento
    ADD CONSTRAINT bc_fin_tipo_pagamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_titulo bc_fin_titulo_centro_custo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_centro_custo_id_fkey FOREIGN KEY (centro_custo_id) REFERENCES brasil_saas.bc_fin_centro_custo(id);


--
-- Name: bc_fin_titulo bc_fin_titulo_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_titulo_parcela bc_fin_titulo_parcela_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fin_titulo_parcela bc_fin_titulo_parcela_titulo_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo_parcela
    ADD CONSTRAINT bc_fin_titulo_parcela_titulo_id_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_fin_titulo bc_fin_titulo_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_fin_titulo bc_fin_titulo_plano_contas_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fin_titulo
    ADD CONSTRAINT bc_fin_titulo_plano_contas_id_fkey FOREIGN KEY (plano_contas_id) REFERENCES brasil_saas.bc_fin_plano_contas(id);


--
-- Name: bc_fis_apuracao bc_fis_apuracao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_apuracao bc_fis_apuracao_imposto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_apuracao
    ADD CONSTRAINT bc_fis_apuracao_imposto_id_fkey FOREIGN KEY (imposto_id) REFERENCES brasil_saas.bc_fis_imposto(id);


--
-- Name: bc_fis_certificado_digital bc_fis_certificado_digital_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_certificado_digital
    ADD CONSTRAINT bc_fis_certificado_digital_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_cte bc_fis_cte_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_cte_item bc_fis_cte_item_cte_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte_item
    ADD CONSTRAINT bc_fis_cte_item_cte_id_fkey FOREIGN KEY (cte_id) REFERENCES brasil_saas.bc_fis_cte(id);


--
-- Name: bc_fis_cte bc_fis_cte_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_cte
    ADD CONSTRAINT bc_fis_cte_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_fis_ecd bc_fis_ecd_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecd
    ADD CONSTRAINT bc_fis_ecd_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_ecf bc_fis_ecf_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_ecf
    ADD CONSTRAINT bc_fis_ecf_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_esocial bc_fis_esocial_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_esocial
    ADD CONSTRAINT bc_fis_esocial_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_imposto bc_fis_imposto_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_imposto
    ADD CONSTRAINT bc_fis_imposto_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_manifestacao bc_fis_manifestacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_manifestacao
    ADD CONSTRAINT bc_fis_manifestacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_mdfe bc_fis_mdfe_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_mdfe
    ADD CONSTRAINT bc_fis_mdfe_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_nfce bc_fis_nfce_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_nfce_item bc_fis_nfce_item_nfce_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item
    ADD CONSTRAINT bc_fis_nfce_item_nfce_id_fkey FOREIGN KEY (nfce_id) REFERENCES brasil_saas.bc_fis_nfce(id);


--
-- Name: bc_fis_nfce_item bc_fis_nfce_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce_item
    ADD CONSTRAINT bc_fis_nfce_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_fis_nfce bc_fis_nfce_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfce
    ADD CONSTRAINT bc_fis_nfce_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_fis_nfe bc_fis_nfe_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_fis_nfe bc_fis_nfe_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_nfe_evento bc_fis_nfe_evento_nfe_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_evento
    ADD CONSTRAINT bc_fis_nfe_evento_nfe_id_fkey FOREIGN KEY (nfe_id) REFERENCES brasil_saas.bc_fis_nfe(id);


--
-- Name: bc_fis_nfe_item bc_fis_nfe_item_nfe_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item
    ADD CONSTRAINT bc_fis_nfe_item_nfe_id_fkey FOREIGN KEY (nfe_id) REFERENCES brasil_saas.bc_fis_nfe(id);


--
-- Name: bc_fis_nfe_item bc_fis_nfe_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe_item
    ADD CONSTRAINT bc_fis_nfe_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_fis_nfe bc_fis_nfe_pedido_compra_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pedido_compra_fkey FOREIGN KEY (pedido_compra_id) REFERENCES brasil_saas.bc_com_pedido(id);


--
-- Name: bc_fis_nfe bc_fis_nfe_pedido_venda_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pedido_venda_fkey FOREIGN KEY (pedido_venda_id) REFERENCES brasil_saas.bc_ven_pedido(id);


--
-- Name: bc_fis_nfe bc_fis_nfe_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfe
    ADD CONSTRAINT bc_fis_nfe_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_fis_nfse bc_fis_nfse_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_fis_nfse bc_fis_nfse_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_nfse_item bc_fis_nfse_item_nfse_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item
    ADD CONSTRAINT bc_fis_nfse_item_nfse_id_fkey FOREIGN KEY (nfse_id) REFERENCES brasil_saas.bc_fis_nfse(id);


--
-- Name: bc_fis_nfse_item bc_fis_nfse_item_servico_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_item
    ADD CONSTRAINT bc_fis_nfse_item_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);


--
-- Name: bc_fis_nfse bc_fis_nfse_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_fis_nfse bc_fis_nfse_servico_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse
    ADD CONSTRAINT bc_fis_nfse_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);


--
-- Name: bc_fis_obrigacao_entrega bc_fis_obrigacao_entrega_obrigacao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_obrigacao_entrega
    ADD CONSTRAINT bc_fis_obrigacao_entrega_obrigacao_id_fkey FOREIGN KEY (obrigacao_id) REFERENCES brasil_saas.bc_fis_obrigacao(id);


--
-- Name: bc_fis_regra_tributaria bc_fis_regra_tributaria_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_regra_tributaria
    ADD CONSTRAINT bc_fis_regra_tributaria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_reinf bc_fis_reinf_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_reinf
    ADD CONSTRAINT bc_fis_reinf_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_sped_contribuicoes bc_fis_sped_contribuicoes_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_contribuicoes
    ADD CONSTRAINT bc_fis_sped_contribuicoes_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_fis_sped_fiscal bc_fis_sped_fiscal_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_sped_fiscal
    ADD CONSTRAINT bc_fis_sped_fiscal_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_grc_avaliacao bc_grc_avaliacao_risco_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_avaliacao
    ADD CONSTRAINT bc_grc_avaliacao_risco_id_fkey FOREIGN KEY (risco_id) REFERENCES brasil_saas.bc_grc_risco(id);


--
-- Name: bc_grc_plano_acao bc_grc_plano_acao_risco_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_plano_acao
    ADD CONSTRAINT bc_grc_plano_acao_risco_id_fkey FOREIGN KEY (risco_id) REFERENCES brasil_saas.bc_grc_risco(id);


--
-- Name: bc_grc_risco_controle bc_grc_risco_controle_controle_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_risco_controle
    ADD CONSTRAINT bc_grc_risco_controle_controle_id_fkey FOREIGN KEY (controle_id) REFERENCES brasil_saas.bc_grc_controle(id);


--
-- Name: bc_grc_risco_controle bc_grc_risco_controle_risco_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_risco_controle
    ADD CONSTRAINT bc_grc_risco_controle_risco_id_fkey FOREIGN KEY (risco_id) REFERENCES brasil_saas.bc_grc_risco(id);


--
-- Name: bc_grc_teste_controle bc_grc_teste_controle_controle_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_grc_teste_controle
    ADD CONSTRAINT bc_grc_teste_controle_controle_id_fkey FOREIGN KEY (controle_id) REFERENCES brasil_saas.bc_grc_controle(id);


--
-- Name: bc_hdp_comentario bc_hdp_comentario_chamado_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_hdp_comentario
    ADD CONSTRAINT bc_hdp_comentario_chamado_id_fkey FOREIGN KEY (chamado_id) REFERENCES brasil_saas.bc_hdp_chamado(id);


--
-- Name: bc_ia_analise_preditiva bc_ia_analise_preditiva_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_analise_preditiva
    ADD CONSTRAINT bc_ia_analise_preditiva_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_chat_mensagem bc_ia_chat_mensagem_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_chat_mensagem bc_ia_chat_mensagem_sessao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_mensagem
    ADD CONSTRAINT bc_ia_chat_mensagem_sessao_id_fkey FOREIGN KEY (sessao_id) REFERENCES brasil_saas.bc_ia_chat_sessao(id) ON DELETE CASCADE;


--
-- Name: bc_ia_chat_message bc_ia_chat_message_session_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_message
    ADD CONSTRAINT bc_ia_chat_message_session_id_fkey FOREIGN KEY (session_id) REFERENCES brasil_saas.bc_ia_chat_session(id) ON DELETE CASCADE;


--
-- Name: bc_ia_chat_sessao bc_ia_chat_sessao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_sessao
    ADD CONSTRAINT bc_ia_chat_sessao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_chat_session bc_ia_chat_session_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_chat_session
    ADD CONSTRAINT bc_ia_chat_session_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_classificacao bc_ia_classificacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_classificacao
    ADD CONSTRAINT bc_ia_classificacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_config bc_ia_config_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_config
    ADD CONSTRAINT bc_ia_config_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_embedding bc_ia_embedding_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_embedding
    ADD CONSTRAINT bc_ia_embedding_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_prompt bc_ia_prompt_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt
    ADD CONSTRAINT bc_ia_prompt_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_ia_prompt_template bc_ia_prompt_template_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ia_prompt_template
    ADD CONSTRAINT bc_ia_prompt_template_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_int_delivery bc_int_delivery_endpoint_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_int_delivery
    ADD CONSTRAINT bc_int_delivery_endpoint_id_fkey FOREIGN KEY (endpoint_id) REFERENCES brasil_saas.bc_int_endpoint(id);


--
-- Name: bc_int_webhook_event bc_int_webhook_event_endpoint_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_int_webhook_event
    ADD CONSTRAINT bc_int_webhook_event_endpoint_id_fkey FOREIGN KEY (endpoint_id) REFERENCES brasil_saas.bc_int_endpoint(id);


--
-- Name: bc_plm_aprovacao bc_plm_aprovacao_mudanca_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_aprovacao
    ADD CONSTRAINT bc_plm_aprovacao_mudanca_id_fkey FOREIGN KEY (mudanca_id) REFERENCES brasil_saas.bc_plm_mudanca(id);


--
-- Name: bc_plm_documento bc_plm_documento_mudanca_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_documento
    ADD CONSTRAINT bc_plm_documento_mudanca_id_fkey FOREIGN KEY (mudanca_id) REFERENCES brasil_saas.bc_plm_mudanca(id);


--
-- Name: bc_plm_documento bc_plm_documento_revisao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_documento
    ADD CONSTRAINT bc_plm_documento_revisao_id_fkey FOREIGN KEY (revisao_id) REFERENCES brasil_saas.bc_plm_produto_revisao(id);


--
-- Name: bc_plm_efeito_mudanca bc_plm_efeito_mudanca_mudanca_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_plm_efeito_mudanca
    ADD CONSTRAINT bc_plm_efeito_mudanca_mudanca_id_fkey FOREIGN KEY (mudanca_id) REFERENCES brasil_saas.bc_plm_mudanca(id);


--
-- Name: bc_prj_etapa bc_prj_etapa_pai_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_pai_id_fkey FOREIGN KEY (pai_id) REFERENCES brasil_saas.bc_prj_etapa(id);


--
-- Name: bc_prj_etapa bc_prj_etapa_projeto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_etapa
    ADD CONSTRAINT bc_prj_etapa_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);


--
-- Name: bc_prj_faturamento bc_prj_faturamento_projeto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_faturamento
    ADD CONSTRAINT bc_prj_faturamento_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);


--
-- Name: bc_prj_movimento bc_prj_movimento_etapa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_etapa_id_fkey FOREIGN KEY (etapa_id) REFERENCES brasil_saas.bc_prj_etapa(id);


--
-- Name: bc_prj_movimento bc_prj_movimento_projeto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_movimento
    ADD CONSTRAINT bc_prj_movimento_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);


--
-- Name: bc_prj_mudanca bc_prj_mudanca_projeto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_mudanca
    ADD CONSTRAINT bc_prj_mudanca_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);


--
-- Name: bc_prj_risco bc_prj_risco_projeto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prj_risco
    ADD CONSTRAINT bc_prj_risco_projeto_id_fkey FOREIGN KEY (projeto_id) REFERENCES brasil_saas.bc_prj_projeto(id);


--
-- Name: bc_prod_apontamento bc_prod_apontamento_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_prod_apontamento bc_prod_apontamento_item_producao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_item_producao_id_fkey FOREIGN KEY (item_producao_id) REFERENCES brasil_saas.bc_prod_item(id);


--
-- Name: bc_prod_apontamento bc_prod_apontamento_producao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_apontamento
    ADD CONSTRAINT bc_prod_apontamento_producao_id_fkey FOREIGN KEY (producao_id) REFERENCES brasil_saas.bc_prod_ordem(id) ON DELETE CASCADE;


--
-- Name: bc_prod_estrutura bc_prod_estrutura_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_estrutura
    ADD CONSTRAINT bc_prod_estrutura_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_prod_romaneio bc_prod_romaneio_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE CASCADE;


--
-- Name: bc_prod_romaneio_item bc_prod_romaneio_item_romaneio_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio_item
    ADD CONSTRAINT bc_prod_romaneio_item_romaneio_id_fkey FOREIGN KEY (romaneio_id) REFERENCES brasil_saas.bc_prod_romaneio(id) ON DELETE CASCADE;


--
-- Name: bc_prod_romaneio bc_prod_romaneio_producao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_romaneio
    ADD CONSTRAINT bc_prod_romaneio_producao_id_fkey FOREIGN KEY (producao_id) REFERENCES brasil_saas.bc_prod_ordem(id) ON DELETE RESTRICT;


--
-- Name: bc_prod_roteiro_operacao bc_prod_roteiro_operacao_centro_trabalho_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_centro_trabalho_id_fkey FOREIGN KEY (centro_trabalho_id) REFERENCES brasil_saas.bc_prod_centro_trabalho(id);


--
-- Name: bc_prod_roteiro_operacao bc_prod_roteiro_operacao_roteiro_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_roteiro_operacao
    ADD CONSTRAINT bc_prod_roteiro_operacao_roteiro_id_fkey FOREIGN KEY (roteiro_id) REFERENCES brasil_saas.bc_prod_roteiro(id);


--
-- Name: bc_rh_cargo bc_rh_cargo_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_cargo
    ADD CONSTRAINT bc_rh_cargo_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_rh_folha bc_rh_folha_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT bc_rh_folha_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_rh_folha_item bc_rh_folha_item_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_rh_folha_item bc_rh_folha_item_folha_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_folha_fkey FOREIGN KEY (folha_id) REFERENCES brasil_saas.bc_rh_folha(id);


--
-- Name: bc_rh_folha_item bc_rh_folha_item_func_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha_item
    ADD CONSTRAINT bc_rh_folha_item_func_fkey FOREIGN KEY (funcionario_id) REFERENCES brasil_saas.bc_rh_funcionario(id);


--
-- Name: bc_rh_folha bc_rh_folha_titulo_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_folha
    ADD CONSTRAINT bc_rh_folha_titulo_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_rh_funcionario bc_rh_func_cargo_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_func_cargo_fkey FOREIGN KEY (cargo_id) REFERENCES brasil_saas.bc_rh_cargo(id);


--
-- Name: bc_rh_funcionario bc_rh_func_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_func_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_rh_funcionario bc_rh_func_pessoa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_rh_funcionario
    ADD CONSTRAINT bc_rh_func_pessoa_fkey FOREIGN KEY (pessoa_id) REFERENCES brasil_saas.bc_cad_pessoa(id);


--
-- Name: bc_srv_ordem_servico bc_srv_ordem_servico_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_srv_ordem_servico bc_srv_ordem_servico_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_srv_ordem_servico bc_srv_ordem_servico_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_ordem_servico
    ADD CONSTRAINT bc_srv_ordem_servico_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_srv_os_apontamento bc_srv_os_apontamento_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_srv_os_apontamento bc_srv_os_apontamento_os_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_os_id_fkey FOREIGN KEY (os_id) REFERENCES brasil_saas.bc_srv_ordem_servico(id);


--
-- Name: bc_srv_os_apontamento bc_srv_os_apontamento_usuario_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_apontamento
    ADD CONSTRAINT bc_srv_os_apontamento_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id);


--
-- Name: bc_srv_os_item bc_srv_os_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_srv_os_item bc_srv_os_item_os_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_os_id_fkey FOREIGN KEY (os_id) REFERENCES brasil_saas.bc_srv_ordem_servico(id);


--
-- Name: bc_srv_os_item bc_srv_os_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_srv_os_item bc_srv_os_item_servico_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_srv_os_item
    ADD CONSTRAINT bc_srv_os_item_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);


--
-- Name: bc_tms_documento_entrega bc_tms_documento_entrega_ordem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_documento_entrega
    ADD CONSTRAINT bc_tms_documento_entrega_ordem_id_fkey FOREIGN KEY (ordem_id) REFERENCES brasil_saas.bc_tms_ordem(id);


--
-- Name: bc_tms_evento bc_tms_evento_ordem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_evento
    ADD CONSTRAINT bc_tms_evento_ordem_id_fkey FOREIGN KEY (ordem_id) REFERENCES brasil_saas.bc_tms_ordem(id);


--
-- Name: bc_tms_fechamento bc_tms_fechamento_ordem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_fechamento
    ADD CONSTRAINT bc_tms_fechamento_ordem_id_fkey FOREIGN KEY (ordem_id) REFERENCES brasil_saas.bc_tms_ordem(id);


--
-- Name: bc_tms_frete bc_tms_frete_ordem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_frete
    ADD CONSTRAINT bc_tms_frete_ordem_id_fkey FOREIGN KEY (ordem_id) REFERENCES brasil_saas.bc_tms_ordem(id);


--
-- Name: bc_tms_parada bc_tms_parada_ordem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_parada
    ADD CONSTRAINT bc_tms_parada_ordem_id_fkey FOREIGN KEY (ordem_id) REFERENCES brasil_saas.bc_tms_ordem(id);


--
-- Name: bc_tms_tracking bc_tms_tracking_ordem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_tms_tracking
    ADD CONSTRAINT bc_tms_tracking_ordem_id_fkey FOREIGN KEY (ordem_id) REFERENCES brasil_saas.bc_tms_ordem(id);


--
-- Name: bc_ven_bonificacao bc_ven_bonificacao_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_ven_bonificacao bc_ven_bonificacao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_bonificacao_item bc_ven_bonificacao_item_bonificacao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_bonificacao_id_fkey FOREIGN KEY (bonificacao_id) REFERENCES brasil_saas.bc_ven_bonificacao(id);


--
-- Name: bc_ven_bonificacao_item bc_ven_bonificacao_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_bonificacao_item bc_ven_bonificacao_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao_item
    ADD CONSTRAINT bc_ven_bonificacao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_ven_bonificacao bc_ven_bonificacao_pedido_origem_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_bonificacao
    ADD CONSTRAINT bc_ven_bonificacao_pedido_origem_id_fkey FOREIGN KEY (pedido_origem_id) REFERENCES brasil_saas.bc_ven_pedido(id);


--
-- Name: bc_ven_contrato bc_ven_contrato_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_ven_contrato bc_ven_contrato_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato
    ADD CONSTRAINT bc_ven_contrato_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_contrato_item bc_ven_contrato_item_contrato_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_contrato_id_fkey FOREIGN KEY (contrato_id) REFERENCES brasil_saas.bc_ven_contrato(id);


--
-- Name: bc_ven_contrato_item bc_ven_contrato_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_contrato_item bc_ven_contrato_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_ven_contrato_item bc_ven_contrato_item_servico_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_contrato_item
    ADD CONSTRAINT bc_ven_contrato_item_servico_id_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);


--
-- Name: bc_ven_devolucao bc_ven_devolucao_cliente_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_ven_devolucao bc_ven_devolucao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_devolucao_item bc_ven_devolucao_item_devolucao_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_devolucao_id_fkey FOREIGN KEY (devolucao_id) REFERENCES brasil_saas.bc_ven_devolucao(id);


--
-- Name: bc_ven_devolucao_item bc_ven_devolucao_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_devolucao_item bc_ven_devolucao_item_lote_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_lote_id_fkey FOREIGN KEY (lote_id) REFERENCES brasil_saas.bc_est_lote(id);


--
-- Name: bc_ven_devolucao_item bc_ven_devolucao_item_numero_serie_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_numero_serie_id_fkey FOREIGN KEY (numero_serie_id) REFERENCES brasil_saas.bc_est_serie(id);


--
-- Name: bc_ven_devolucao_item bc_ven_devolucao_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao_item
    ADD CONSTRAINT bc_ven_devolucao_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_ven_devolucao bc_ven_devolucao_pedido_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_devolucao
    ADD CONSTRAINT bc_ven_devolucao_pedido_id_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_ven_pedido(id);


--
-- Name: bc_ven_pedido_item bc_ven_item_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_pedido_item bc_ven_item_pedido_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_pedido_fkey FOREIGN KEY (pedido_id) REFERENCES brasil_saas.bc_ven_pedido(id);


--
-- Name: bc_ven_pedido_item bc_ven_item_produto_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_produto_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_ven_pedido_item bc_ven_item_servico_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido_item
    ADD CONSTRAINT bc_ven_item_servico_fkey FOREIGN KEY (servico_id) REFERENCES brasil_saas.bc_cad_servico(id);


--
-- Name: bc_ven_pedido bc_ven_pedido_cliente_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_cliente_fkey FOREIGN KEY (cliente_id) REFERENCES brasil_saas.bc_cad_cliente(id);


--
-- Name: bc_ven_pedido bc_ven_pedido_cond_pagto_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_cond_pagto_fkey FOREIGN KEY (condicao_pagamento_id) REFERENCES brasil_saas.bc_fin_condicao_pagamento(id);


--
-- Name: bc_ven_pedido bc_ven_pedido_empresa_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_pedido bc_ven_pedido_tabela_preco_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_tabela_preco_fkey FOREIGN KEY (tabela_preco_id) REFERENCES brasil_saas.bc_ven_tabela_preco(id);


--
-- Name: bc_ven_pedido bc_ven_pedido_titulo_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_pedido
    ADD CONSTRAINT bc_ven_pedido_titulo_fkey FOREIGN KEY (titulo_id) REFERENCES brasil_saas.bc_fin_titulo(id);


--
-- Name: bc_ven_regra_comissao bc_ven_regra_comissao_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_regra_comissao bc_ven_regra_comissao_vendedor_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_regra_comissao
    ADD CONSTRAINT bc_ven_regra_comissao_vendedor_id_fkey FOREIGN KEY (vendedor_id) REFERENCES brasil_saas.bc_rh_funcionario(id);


--
-- Name: bc_ven_tabela_preco bc_ven_tabela_preco_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco
    ADD CONSTRAINT bc_ven_tabela_preco_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_tabela_preco_item bc_ven_tabela_preco_item_empresa_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_ven_tabela_preco_item bc_ven_tabela_preco_item_produto_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_produto_id_fkey FOREIGN KEY (produto_id) REFERENCES brasil_saas.bc_cad_produto(id);


--
-- Name: bc_ven_tabela_preco_item bc_ven_tabela_preco_item_tabela_preco_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_ven_tabela_preco_item
    ADD CONSTRAINT bc_ven_tabela_preco_item_tabela_preco_id_fkey FOREIGN KEY (tabela_preco_id) REFERENCES brasil_saas.bc_ven_tabela_preco(id);


--
-- Name: bc_wkf_instance bc_wkf_instance_definition_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_instance
    ADD CONSTRAINT bc_wkf_instance_definition_id_fkey FOREIGN KEY (definition_id) REFERENCES brasil_saas.bc_wkf_definition(id);


--
-- Name: bc_wkf_stage bc_wkf_stage_definition_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_stage
    ADD CONSTRAINT bc_wkf_stage_definition_id_fkey FOREIGN KEY (definition_id) REFERENCES brasil_saas.bc_wkf_definition(id);


--
-- Name: bc_wkf_task bc_wkf_task_instance_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_instance_id_fkey FOREIGN KEY (instance_id) REFERENCES brasil_saas.bc_wkf_instance(id);


--
-- Name: bc_wkf_task bc_wkf_task_stage_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wkf_task
    ADD CONSTRAINT bc_wkf_task_stage_id_fkey FOREIGN KEY (stage_id) REFERENCES brasil_saas.bc_wkf_stage(id);


--
-- Name: bc_wms_onda_item bc_wms_onda_item_onda_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_onda_item
    ADD CONSTRAINT bc_wms_onda_item_onda_id_fkey FOREIGN KEY (onda_id) REFERENCES brasil_saas.bc_wms_onda(id);


--
-- Name: bc_wms_volume_item bc_wms_volume_item_volume_id_fkey; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_wms_volume_item
    ADD CONSTRAINT bc_wms_volume_item_volume_id_fkey FOREIGN KEY (volume_id) REFERENCES brasil_saas.bc_wms_volume(id);


--
-- Name: bc_core_perfil fk_bc_core_perfil_pai; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_perfil
    ADD CONSTRAINT fk_bc_core_perfil_pai FOREIGN KEY (perfil_pai_id) REFERENCES brasil_saas.bc_core_perfil(id) ON DELETE SET NULL;


--
-- Name: bc_core_empresa fk_empresa_matriz_do_grupo; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_empresa
    ADD CONSTRAINT fk_empresa_matriz_do_grupo FOREIGN KEY (matriz_id) REFERENCES brasil_saas.bc_core_empresa(id) ON DELETE RESTRICT;


--
-- Name: bc_fis_nfse_retorno fk_fis_nfse_retorno_nfse; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_fis_nfse_retorno
    ADD CONSTRAINT fk_fis_nfse_retorno_nfse FOREIGN KEY (nfse_id) REFERENCES brasil_saas.bc_fis_nfse(id) ON DELETE CASCADE;


--
-- Name: bc_core_usuario_empresa fk_ue_empresa; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT fk_ue_empresa FOREIGN KEY (empresa_id) REFERENCES brasil_saas.bc_core_empresa(id);


--
-- Name: bc_core_usuario_empresa fk_ue_perfil; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT fk_ue_perfil FOREIGN KEY (perfil_id) REFERENCES brasil_saas.bc_core_perfil(id);


--
-- Name: bc_core_usuario_empresa fk_ue_usuario; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_core_usuario_empresa
    ADD CONSTRAINT fk_ue_usuario FOREIGN KEY (usuario_id) REFERENCES brasil_saas.bc_core_usuario(id) ON DELETE CASCADE;


--
-- Name: bc_prod_item fkal2vl187nvs8xwaq8kbexe66k; Type: FK CONSTRAINT; Schema: brasil_saas; Owner: sa
--

ALTER TABLE ONLY brasil_saas.bc_prod_item
    ADD CONSTRAINT fkal2vl187nvs8xwaq8kbexe66k FOREIGN KEY (producao_id) REFERENCES brasil_saas.bc_prod_ordem(id);


--
-- Name: ffinanceiro ffinanceiro_codmovimento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ffinanceiro
    ADD CONSTRAINT ffinanceiro_codmovimento_fkey FOREIGN KEY (codmovimento) REFERENCES public.fmovimento(codmovimento) ON DELETE CASCADE;


--
-- Name: fitem fitem_codvariacao_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fitem
    ADD CONSTRAINT fitem_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);


--
-- Name: flan flan_id_pessoa_parceiro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flan
    ADD CONSTRAINT flan_id_pessoa_parceiro_fkey FOREIGN KEY (id_pessoa_parceiro) REFERENCES public.fpessoa(id_pessoa);


--
-- Name: fmov fmov_id_pessoa_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmov
    ADD CONSTRAINT fmov_id_pessoa_cliente_fkey FOREIGN KEY (id_pessoa_cliente) REFERENCES public.fpessoa(id_pessoa);


--
-- Name: fmov fmov_id_pessoa_funcionario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmov
    ADD CONSTRAINT fmov_id_pessoa_funcionario_fkey FOREIGN KEY (id_pessoa_funcionario) REFERENCES public.fpessoa(id_pessoa);


--
-- Name: fmovimento_item fmovimento_item_codmovimento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmovimento_item
    ADD CONSTRAINT fmovimento_item_codmovimento_fkey FOREIGN KEY (codmovimento) REFERENCES public.fmovimento(codmovimento) ON DELETE CASCADE;


--
-- Name: fmovimento_item fmovimento_item_codvariacao_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fmovimento_item
    ADD CONSTRAINT fmovimento_item_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);


--
-- Name: fnota fnota_centro_custo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_centro_custo_fkey FOREIGN KEY (centro_custo) REFERENCES public.fcusto(codcusto);


--
-- Name: fnota fnota_codmovimento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_codmovimento_fkey FOREIGN KEY (codmovimento) REFERENCES public.fmovimento(codmovimento);


--
-- Name: fnota fnota_id_venda_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota
    ADD CONSTRAINT fnota_id_venda_fkey FOREIGN KEY (id_venda) REFERENCES public.fvenda(id_venda) ON DELETE CASCADE;


--
-- Name: fnota_item fnota_item_codvariacao_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota_item
    ADD CONSTRAINT fnota_item_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);


--
-- Name: fnota_item fnota_item_id_nota_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fnota_item
    ADD CONSTRAINT fnota_item_id_nota_fkey FOREIGN KEY (id_nota) REFERENCES public.fnota(id_nota) ON DELETE CASCADE;


--
-- Name: fpessoa fpessoa_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fpessoa
    ADD CONSTRAINT fpessoa_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.fusuario(codigo);


--
-- Name: fproduto_ecommerce fproduto_ecommerce_codproduto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_ecommerce
    ADD CONSTRAINT fproduto_ecommerce_codproduto_fkey FOREIGN KEY (codproduto) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;


--
-- Name: fproduto_fiscal fproduto_fiscal_codproduto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_fiscal
    ADD CONSTRAINT fproduto_fiscal_codproduto_fkey FOREIGN KEY (codproduto) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;


--
-- Name: fproduto_imagem fproduto_imagem_codvariacao_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_imagem
    ADD CONSTRAINT fproduto_imagem_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao) ON DELETE CASCADE;


--
-- Name: fproduto_kit fproduto_kit_codkit_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_kit
    ADD CONSTRAINT fproduto_kit_codkit_fkey FOREIGN KEY (codkit) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;


--
-- Name: fproduto_kit fproduto_kit_codproduto_filho_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_kit
    ADD CONSTRAINT fproduto_kit_codproduto_filho_fkey FOREIGN KEY (codproduto_filho) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;


--
-- Name: fproduto_movimento fproduto_movimento_codvariacao_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_movimento
    ADD CONSTRAINT fproduto_movimento_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao) ON DELETE CASCADE;


--
-- Name: fproduto_variacao fproduto_variacao_codproduto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fproduto_variacao
    ADD CONSTRAINT fproduto_variacao_codproduto_fkey FOREIGN KEY (codproduto) REFERENCES public.fproduto(codproduto) ON DELETE CASCADE;


--
-- Name: fvenda fvenda_centro_custo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_centro_custo_fkey FOREIGN KEY (centro_custo) REFERENCES public.fcusto(codcusto);


--
-- Name: fvenda fvenda_condicao_pagamento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_condicao_pagamento_fkey FOREIGN KEY (condicao_pagamento) REFERENCES public.fcondicao(codcondicao);


--
-- Name: fvenda fvenda_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.fpessoa(id_pessoa);


--
-- Name: fvenda fvenda_id_recibo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_id_recibo_fkey FOREIGN KEY (id_recibo) REFERENCES public.frecibo(idrecibo);


--
-- Name: fvenda_item fvenda_item_codvariacao_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda_item
    ADD CONSTRAINT fvenda_item_codvariacao_fkey FOREIGN KEY (codvariacao) REFERENCES public.fproduto_variacao(codvariacao);


--
-- Name: fvenda_item fvenda_item_id_venda_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda_item
    ADD CONSTRAINT fvenda_item_id_venda_fkey FOREIGN KEY (id_venda) REFERENCES public.fvenda(id_venda) ON DELETE CASCADE;


--
-- Name: fvenda fvenda_tipo_pagamento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fvenda
    ADD CONSTRAINT fvenda_tipo_pagamento_fkey FOREIGN KEY (tipo_pagamento) REFERENCES public.ftipopagamento(codtipo);


--
-- Name: tcnae_servico tcnae_servico_codigo_item_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tcnae_servico
    ADD CONSTRAINT tcnae_servico_codigo_item_fkey FOREIGN KEY (codigo_item) REFERENCES public.tservico_lc116(codigo_item);


--
-- Name: SCHEMA brasil_saas; Type: ACL; Schema: -; Owner: sa
--

GRANT USAGE ON SCHEMA brasil_saas TO astral;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT USAGE ON SCHEMA public TO dbm_app;


--
-- Name: TABLE bc_agd_evento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_agd_evento TO astral;


--
-- Name: SEQUENCE bc_agd_evento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_agd_evento_id_seq TO astral;


--
-- Name: TABLE bc_ativo_classe; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_classe TO astral;


--
-- Name: SEQUENCE bc_ativo_classe_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_classe_id_seq TO astral;


--
-- Name: TABLE bc_ativo_depreciacao_execucao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_depreciacao_execucao TO astral;


--
-- Name: SEQUENCE bc_ativo_depreciacao_execucao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_depreciacao_execucao_id_seq TO astral;


--
-- Name: TABLE bc_ativo_imobilizado; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_imobilizado TO astral;


--
-- Name: SEQUENCE bc_ativo_imobilizado_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_imobilizado_id_seq TO astral;


--
-- Name: TABLE bc_ativo_manutencao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_manutencao TO astral;


--
-- Name: TABLE bc_ativo_manutencao_apontamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_manutencao_apontamento TO astral;


--
-- Name: SEQUENCE bc_ativo_manutencao_apontamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_manutencao_apontamento_id_seq TO astral;


--
-- Name: SEQUENCE bc_ativo_manutencao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_manutencao_id_seq TO astral;


--
-- Name: TABLE bc_ativo_manutencao_material; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_manutencao_material TO astral;


--
-- Name: SEQUENCE bc_ativo_manutencao_material_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_manutencao_material_id_seq TO astral;


--
-- Name: TABLE bc_ativo_medicao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_medicao TO astral;


--
-- Name: SEQUENCE bc_ativo_medicao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_medicao_id_seq TO astral;


--
-- Name: TABLE bc_ativo_movimento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_movimento TO astral;


--
-- Name: SEQUENCE bc_ativo_movimento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_movimento_id_seq TO astral;


--
-- Name: TABLE bc_ativo_nota_manutencao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_nota_manutencao TO astral;


--
-- Name: SEQUENCE bc_ativo_nota_manutencao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_nota_manutencao_id_seq TO astral;


--
-- Name: TABLE bc_ativo_plano_manutencao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ativo_plano_manutencao TO astral;


--
-- Name: SEQUENCE bc_ativo_plano_manutencao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ativo_plano_manutencao_id_seq TO astral;


--
-- Name: TABLE bc_bi_dashboard; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_dashboard TO astral;


--
-- Name: SEQUENCE bc_bi_dashboard_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_dashboard_id_seq TO astral;


--
-- Name: TABLE bc_bi_dashboard_widget; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_dashboard_widget TO astral;


--
-- Name: SEQUENCE bc_bi_dashboard_widget_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_dashboard_widget_id_seq TO astral;


--
-- Name: TABLE bc_bi_indicador; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_indicador TO astral;


--
-- Name: SEQUENCE bc_bi_indicador_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_indicador_id_seq TO astral;


--
-- Name: TABLE bc_bi_kpi; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_kpi TO astral;


--
-- Name: SEQUENCE bc_bi_kpi_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_kpi_id_seq TO astral;


--
-- Name: TABLE bc_bi_relatorio; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_relatorio TO astral;


--
-- Name: TABLE bc_bi_relatorio_agendado; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_relatorio_agendado TO astral;


--
-- Name: SEQUENCE bc_bi_relatorio_agendado_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_relatorio_agendado_id_seq TO astral;


--
-- Name: SEQUENCE bc_bi_relatorio_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_relatorio_id_seq TO astral;


--
-- Name: TABLE bc_bi_report; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_report TO astral;


--
-- Name: SEQUENCE bc_bi_report_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_report_id_seq TO astral;


--
-- Name: TABLE bc_bi_report_parameter; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_bi_report_parameter TO astral;


--
-- Name: SEQUENCE bc_bi_report_parameter_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_bi_report_parameter_id_seq TO astral;


--
-- Name: TABLE bc_cad_base_cep; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_base_cep TO astral;


--
-- Name: SEQUENCE bc_cad_base_cep_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_base_cep_id_seq TO astral;


--
-- Name: TABLE bc_cad_categoria; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_categoria TO astral;


--
-- Name: SEQUENCE bc_cad_categoria_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_categoria_id_seq TO astral;


--
-- Name: TABLE bc_cad_cliente; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_cliente TO astral;


--
-- Name: SEQUENCE bc_cad_cliente_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_cliente_id_seq TO astral;


--
-- Name: TABLE bc_cad_contato; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_contato TO astral;


--
-- Name: SEQUENCE bc_cad_contato_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_contato_id_seq TO astral;


--
-- Name: TABLE bc_cad_documento_fiscal; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_documento_fiscal TO astral;


--
-- Name: SEQUENCE bc_cad_documento_fiscal_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_documento_fiscal_id_seq TO astral;


--
-- Name: TABLE bc_cad_endereco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_endereco TO astral;


--
-- Name: SEQUENCE bc_cad_endereco_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_endereco_id_seq TO astral;


--
-- Name: TABLE bc_cad_fornecedor; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_fornecedor TO astral;


--
-- Name: SEQUENCE bc_cad_fornecedor_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_fornecedor_id_seq TO astral;


--
-- Name: TABLE bc_cad_marca; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_marca TO astral;


--
-- Name: SEQUENCE bc_cad_marca_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_marca_id_seq TO astral;


--
-- Name: TABLE bc_cad_municipio; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_municipio TO astral;


--
-- Name: SEQUENCE bc_cad_municipio_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_municipio_id_seq TO astral;


--
-- Name: TABLE bc_cad_papel; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_papel TO astral;


--
-- Name: SEQUENCE bc_cad_papel_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_papel_id_seq TO astral;


--
-- Name: TABLE bc_cad_pessoa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_pessoa TO astral;


--
-- Name: TABLE bc_cad_pessoa_fisica; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_pessoa_fisica TO astral;


--
-- Name: SEQUENCE bc_cad_pessoa_fisica_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_pessoa_fisica_id_seq TO astral;


--
-- Name: SEQUENCE bc_cad_pessoa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_pessoa_id_seq TO astral;


--
-- Name: TABLE bc_cad_pessoa_juridica; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_pessoa_juridica TO astral;


--
-- Name: SEQUENCE bc_cad_pessoa_juridica_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_pessoa_juridica_id_seq TO astral;


--
-- Name: TABLE bc_cad_produto; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_produto TO astral;


--
-- Name: TABLE bc_cad_produto_ecommerce; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_produto_ecommerce TO astral;


--
-- Name: SEQUENCE bc_cad_produto_ecommerce_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_produto_ecommerce_id_seq TO astral;


--
-- Name: SEQUENCE bc_cad_produto_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_produto_id_seq TO astral;


--
-- Name: TABLE bc_cad_produto_imagem; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_produto_imagem TO astral;


--
-- Name: SEQUENCE bc_cad_produto_imagem_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_produto_imagem_id_seq TO astral;


--
-- Name: TABLE bc_cad_produto_kit; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_produto_kit TO astral;


--
-- Name: SEQUENCE bc_cad_produto_kit_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_produto_kit_id_seq TO astral;


--
-- Name: TABLE bc_cad_produto_variacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_produto_variacao TO astral;


--
-- Name: SEQUENCE bc_cad_produto_variacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_produto_variacao_id_seq TO astral;


--
-- Name: TABLE bc_cad_servico; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_servico TO astral;


--
-- Name: SEQUENCE bc_cad_servico_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_servico_id_seq TO astral;


--
-- Name: TABLE bc_cad_transportadora; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_transportadora TO astral;


--
-- Name: SEQUENCE bc_cad_transportadora_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_transportadora_id_seq TO astral;


--
-- Name: TABLE bc_cad_unidade_medida; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cad_unidade_medida TO astral;


--
-- Name: SEQUENCE bc_cad_unidade_medida_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cad_unidade_medida_id_seq TO astral;


--
-- Name: TABLE bc_cmp_devolucao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cmp_devolucao TO astral;


--
-- Name: SEQUENCE bc_cmp_devolucao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cmp_devolucao_id_seq TO astral;


--
-- Name: TABLE bc_cmp_devolucao_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cmp_devolucao_item TO astral;


--
-- Name: SEQUENCE bc_cmp_devolucao_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cmp_devolucao_item_id_seq TO astral;


--
-- Name: TABLE bc_com_conferencia_fatura; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_conferencia_fatura TO astral;


--
-- Name: SEQUENCE bc_com_conferencia_fatura_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_conferencia_fatura_id_seq TO astral;


--
-- Name: TABLE bc_com_conferencia_fatura_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_conferencia_fatura_item TO astral;


--
-- Name: SEQUENCE bc_com_conferencia_fatura_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_conferencia_fatura_item_id_seq TO astral;


--
-- Name: TABLE bc_com_contrato; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_contrato TO astral;


--
-- Name: SEQUENCE bc_com_contrato_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_contrato_id_seq TO astral;


--
-- Name: TABLE bc_com_contrato_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_contrato_item TO astral;


--
-- Name: SEQUENCE bc_com_contrato_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_contrato_item_id_seq TO astral;


--
-- Name: TABLE bc_com_cotacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_cotacao TO astral;


--
-- Name: TABLE bc_com_cotacao_fornecedor; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_cotacao_fornecedor TO astral;


--
-- Name: SEQUENCE bc_com_cotacao_fornecedor_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_cotacao_fornecedor_id_seq TO astral;


--
-- Name: SEQUENCE bc_com_cotacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_cotacao_id_seq TO astral;


--
-- Name: TABLE bc_com_cotacao_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_cotacao_item TO astral;


--
-- Name: SEQUENCE bc_com_cotacao_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_cotacao_item_id_seq TO astral;


--
-- Name: TABLE bc_com_pedido; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_pedido TO astral;


--
-- Name: SEQUENCE bc_com_pedido_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_pedido_id_seq TO astral;


--
-- Name: TABLE bc_com_pedido_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_pedido_item TO astral;


--
-- Name: SEQUENCE bc_com_pedido_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_pedido_item_id_seq TO astral;


--
-- Name: TABLE bc_com_recebimento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_recebimento TO astral;


--
-- Name: SEQUENCE bc_com_recebimento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_recebimento_id_seq TO astral;


--
-- Name: TABLE bc_com_recebimento_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_recebimento_item TO astral;


--
-- Name: SEQUENCE bc_com_recebimento_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_recebimento_item_id_seq TO astral;


--
-- Name: TABLE bc_com_solicitacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_solicitacao TO astral;


--
-- Name: SEQUENCE bc_com_solicitacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_solicitacao_id_seq TO astral;


--
-- Name: TABLE bc_com_solicitacao_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_com_solicitacao_item TO astral;


--
-- Name: SEQUENCE bc_com_solicitacao_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_com_solicitacao_item_id_seq TO astral;


--
-- Name: TABLE bc_cont_fechamento_check; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cont_fechamento_check TO astral;


--
-- Name: SEQUENCE bc_cont_fechamento_check_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cont_fechamento_check_id_seq TO astral;


--
-- Name: TABLE bc_cont_rateio; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cont_rateio TO astral;


--
-- Name: SEQUENCE bc_cont_rateio_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cont_rateio_id_seq TO astral;


--
-- Name: TABLE bc_cont_regra_lancamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cont_regra_lancamento TO astral;


--
-- Name: SEQUENCE bc_cont_regra_lancamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cont_regra_lancamento_id_seq TO astral;


--
-- Name: TABLE bc_cont_relatorio_snapshot; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_cont_relatorio_snapshot TO astral;


--
-- Name: SEQUENCE bc_cont_relatorio_snapshot_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_cont_relatorio_snapshot_id_seq TO astral;


--
-- Name: TABLE bc_core_auditoria; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_auditoria TO astral;


--
-- Name: SEQUENCE bc_core_auditoria_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_auditoria_id_seq TO astral;


--
-- Name: TABLE bc_core_auth_source; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_auth_source TO astral;


--
-- Name: TABLE bc_core_banco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_banco TO astral;


--
-- Name: TABLE bc_core_configuracao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_configuracao TO astral;


--
-- Name: SEQUENCE bc_core_configuracao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_configuracao_id_seq TO astral;


--
-- Name: TABLE bc_core_documento_fluxo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_documento_fluxo TO astral;


--
-- Name: SEQUENCE bc_core_documento_fluxo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_documento_fluxo_id_seq TO astral;


--
-- Name: TABLE bc_core_empresa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_empresa TO astral;


--
-- Name: SEQUENCE bc_core_empresa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_empresa_id_seq TO astral;


--
-- Name: TABLE bc_core_empresa_vinculo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_empresa_vinculo TO astral;


--
-- Name: SEQUENCE bc_core_empresa_vinculo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_empresa_vinculo_id_seq TO astral;


--
-- Name: TABLE bc_core_integration_event; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_integration_event TO astral;


--
-- Name: SEQUENCE bc_core_integration_event_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_integration_event_id_seq TO astral;


--
-- Name: TABLE bc_core_log_acesso; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_log_acesso TO astral;


--
-- Name: SEQUENCE bc_core_log_acesso_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_log_acesso_id_seq TO astral;


--
-- Name: TABLE bc_core_modulo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_modulo TO astral;


--
-- Name: SEQUENCE bc_core_modulo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_modulo_id_seq TO astral;


--
-- Name: TABLE bc_core_notificacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_notificacao TO astral;


--
-- Name: SEQUENCE bc_core_notificacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_notificacao_id_seq TO astral;


--
-- Name: TABLE bc_core_perfil; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_perfil TO astral;


--
-- Name: SEQUENCE bc_core_perfil_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_perfil_id_seq TO astral;


--
-- Name: TABLE bc_core_perfil_permissao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_perfil_permissao TO astral;


--
-- Name: SEQUENCE bc_core_perfil_permissao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_perfil_permissao_id_seq TO astral;


--
-- Name: TABLE bc_core_permissao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_permissao TO astral;


--
-- Name: SEQUENCE bc_core_permissao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_permissao_id_seq TO astral;


--
-- Name: TABLE bc_core_sessao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_sessao TO astral;


--
-- Name: SEQUENCE bc_core_sessao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_sessao_id_seq TO astral;


--
-- Name: TABLE bc_core_usuario; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_usuario TO astral;


--
-- Name: TABLE bc_core_usuario_empresa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_usuario_empresa TO astral;


--
-- Name: TABLE bc_core_usuario_empresa_backup_20260928; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_usuario_empresa_backup_20260928 TO astral;


--
-- Name: SEQUENCE bc_core_usuario_empresa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_usuario_empresa_id_seq TO astral;


--
-- Name: SEQUENCE bc_core_usuario_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_usuario_id_seq TO astral;


--
-- Name: TABLE bc_core_usuario_modulo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_usuario_modulo TO astral;


--
-- Name: SEQUENCE bc_core_usuario_modulo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_usuario_modulo_id_seq TO astral;


--
-- Name: TABLE bc_core_usuario_perfil; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_core_usuario_perfil TO astral;


--
-- Name: SEQUENCE bc_core_usuario_perfil_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_core_usuario_perfil_id_seq TO astral;


--
-- Name: TABLE bc_crm_atividade; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_crm_atividade TO astral;


--
-- Name: SEQUENCE bc_crm_atividade_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_crm_atividade_id_seq TO astral;


--
-- Name: TABLE bc_crm_lead; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_crm_lead TO astral;


--
-- Name: SEQUENCE bc_crm_lead_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_crm_lead_id_seq TO astral;


--
-- Name: TABLE bc_crm_oportunidade; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_crm_oportunidade TO astral;


--
-- Name: SEQUENCE bc_crm_oportunidade_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_crm_oportunidade_id_seq TO astral;


--
-- Name: TABLE bc_crm_tarefa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_crm_tarefa TO astral;


--
-- Name: SEQUENCE bc_crm_tarefa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_crm_tarefa_id_seq TO astral;


--
-- Name: TABLE bc_ctb_fechamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ctb_fechamento TO astral;


--
-- Name: SEQUENCE bc_ctb_fechamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ctb_fechamento_id_seq TO astral;


--
-- Name: TABLE bc_ctb_lancamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ctb_lancamento TO astral;


--
-- Name: SEQUENCE bc_ctb_lancamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ctb_lancamento_id_seq TO astral;


--
-- Name: TABLE bc_ctb_partida; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ctb_partida TO astral;


--
-- Name: SEQUENCE bc_ctb_partida_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ctb_partida_id_seq TO astral;


--
-- Name: TABLE bc_dms_aprovacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_dms_aprovacao TO astral;


--
-- Name: SEQUENCE bc_dms_aprovacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_dms_aprovacao_id_seq TO astral;


--
-- Name: TABLE bc_dms_documento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_dms_documento TO astral;


--
-- Name: SEQUENCE bc_dms_documento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_dms_documento_id_seq TO astral;


--
-- Name: TABLE bc_dms_versao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_dms_versao TO astral;


--
-- Name: SEQUENCE bc_dms_versao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_dms_versao_id_seq TO astral;


--
-- Name: TABLE bc_ehs_acao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ehs_acao TO astral;


--
-- Name: SEQUENCE bc_ehs_acao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ehs_acao_id_seq TO astral;


--
-- Name: TABLE bc_ehs_inspecao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ehs_inspecao TO astral;


--
-- Name: SEQUENCE bc_ehs_inspecao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ehs_inspecao_id_seq TO astral;


--
-- Name: TABLE bc_ehs_ocorrencia; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ehs_ocorrencia TO astral;


--
-- Name: SEQUENCE bc_ehs_ocorrencia_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ehs_ocorrencia_id_seq TO astral;


--
-- Name: TABLE bc_ehs_permissao_trabalho; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ehs_permissao_trabalho TO astral;


--
-- Name: SEQUENCE bc_ehs_permissao_trabalho_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ehs_permissao_trabalho_id_seq TO astral;


--
-- Name: TABLE bc_ehs_risco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ehs_risco TO astral;


--
-- Name: SEQUENCE bc_ehs_risco_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ehs_risco_id_seq TO astral;


--
-- Name: TABLE bc_ent_cenario_tributario; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ent_cenario_tributario TO astral;


--
-- Name: SEQUENCE bc_ent_cenario_tributario_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ent_cenario_tributario_id_seq TO astral;


--
-- Name: TABLE bc_ent_contrato; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ent_contrato TO astral;


--
-- Name: SEQUENCE bc_ent_contrato_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ent_contrato_id_seq TO astral;


--
-- Name: TABLE bc_ent_fornecedor_qualificacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ent_fornecedor_qualificacao TO astral;


--
-- Name: SEQUENCE bc_ent_fornecedor_qualificacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ent_fornecedor_qualificacao_id_seq TO astral;


--
-- Name: TABLE bc_ent_meta_comercial; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ent_meta_comercial TO astral;


--
-- Name: SEQUENCE bc_ent_meta_comercial_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ent_meta_comercial_id_seq TO astral;


--
-- Name: TABLE bc_ent_orcamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ent_orcamento TO astral;


--
-- Name: SEQUENCE bc_ent_orcamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ent_orcamento_id_seq TO astral;


--
-- Name: TABLE bc_ent_periodo_contabil; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ent_periodo_contabil TO astral;


--
-- Name: SEQUENCE bc_ent_periodo_contabil_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ent_periodo_contabil_id_seq TO astral;


--
-- Name: TABLE bc_ent_tesouraria_previsao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ent_tesouraria_previsao TO astral;


--
-- Name: SEQUENCE bc_ent_tesouraria_previsao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ent_tesouraria_previsao_id_seq TO astral;


--
-- Name: TABLE bc_esocial_evento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_esocial_evento TO astral;


--
-- Name: SEQUENCE bc_esocial_evento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_esocial_evento_id_seq TO astral;


--
-- Name: TABLE bc_est_deposito; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_deposito TO astral;


--
-- Name: SEQUENCE bc_est_deposito_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_deposito_id_seq TO astral;


--
-- Name: TABLE bc_est_endereco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_endereco TO astral;


--
-- Name: SEQUENCE bc_est_endereco_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_endereco_id_seq TO astral;


--
-- Name: TABLE bc_est_expedicao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_expedicao TO astral;


--
-- Name: SEQUENCE bc_est_expedicao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_expedicao_id_seq TO astral;


--
-- Name: TABLE bc_est_expedicao_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_expedicao_item TO astral;


--
-- Name: SEQUENCE bc_est_expedicao_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_expedicao_item_id_seq TO astral;


--
-- Name: TABLE bc_est_inventario; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_inventario TO astral;


--
-- Name: SEQUENCE bc_est_inventario_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_inventario_id_seq TO astral;


--
-- Name: TABLE bc_est_inventario_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_inventario_item TO astral;


--
-- Name: SEQUENCE bc_est_inventario_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_inventario_item_id_seq TO astral;


--
-- Name: TABLE bc_est_lote; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_lote TO astral;


--
-- Name: SEQUENCE bc_est_lote_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_lote_id_seq TO astral;


--
-- Name: TABLE bc_est_movimentacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_movimentacao TO astral;


--
-- Name: SEQUENCE bc_est_movimentacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_movimentacao_id_seq TO astral;


--
-- Name: TABLE bc_est_reserva; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_reserva TO astral;


--
-- Name: SEQUENCE bc_est_reserva_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_reserva_id_seq TO astral;


--
-- Name: TABLE bc_est_saldo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_saldo TO astral;


--
-- Name: SEQUENCE bc_est_saldo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_saldo_id_seq TO astral;


--
-- Name: TABLE bc_est_serie; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_serie TO astral;


--
-- Name: SEQUENCE bc_est_serie_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_serie_id_seq TO astral;


--
-- Name: TABLE bc_est_transferencia; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_transferencia TO astral;


--
-- Name: SEQUENCE bc_est_transferencia_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_transferencia_id_seq TO astral;


--
-- Name: TABLE bc_est_transferencia_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_est_transferencia_item TO astral;


--
-- Name: SEQUENCE bc_est_transferencia_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_est_transferencia_item_id_seq TO astral;


--
-- Name: TABLE bc_fin_analise_rentabilidade; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_analise_rentabilidade TO astral;


--
-- Name: SEQUENCE bc_fin_analise_rentabilidade_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_analise_rentabilidade_id_seq TO astral;


--
-- Name: TABLE bc_fin_aplicacao_financeira; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_aplicacao_financeira TO astral;


--
-- Name: SEQUENCE bc_fin_aplicacao_financeira_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_aplicacao_financeira_id_seq TO astral;


--
-- Name: TABLE bc_fin_aprovacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_aprovacao TO astral;


--
-- Name: SEQUENCE bc_fin_aprovacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_aprovacao_id_seq TO astral;


--
-- Name: TABLE bc_fin_baixa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_baixa TO astral;


--
-- Name: SEQUENCE bc_fin_baixa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_baixa_id_seq TO astral;


--
-- Name: TABLE bc_fin_boleto; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_boleto TO astral;


--
-- Name: SEQUENCE bc_fin_boleto_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_boleto_id_seq TO astral;


--
-- Name: TABLE bc_fin_caixa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_caixa TO astral;


--
-- Name: SEQUENCE bc_fin_caixa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_caixa_id_seq TO astral;


--
-- Name: TABLE bc_fin_caixa_movimento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_caixa_movimento TO astral;


--
-- Name: SEQUENCE bc_fin_caixa_movimento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_caixa_movimento_id_seq TO astral;


--
-- Name: TABLE bc_fin_centro_custo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_centro_custo TO astral;


--
-- Name: SEQUENCE bc_fin_centro_custo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_centro_custo_id_seq TO astral;


--
-- Name: TABLE bc_fin_cobranca_acao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_cobranca_acao TO astral;


--
-- Name: SEQUENCE bc_fin_cobranca_acao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_cobranca_acao_id_seq TO astral;


--
-- Name: TABLE bc_fin_comissao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_comissao TO astral;


--
-- Name: SEQUENCE bc_fin_comissao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_comissao_id_seq TO astral;


--
-- Name: TABLE bc_fin_conciliacao_bancaria; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_conciliacao_bancaria TO astral;


--
-- Name: SEQUENCE bc_fin_conciliacao_bancaria_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_conciliacao_bancaria_id_seq TO astral;


--
-- Name: TABLE bc_fin_conciliacao_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_conciliacao_item TO astral;


--
-- Name: SEQUENCE bc_fin_conciliacao_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_conciliacao_item_id_seq TO astral;


--
-- Name: TABLE bc_fin_condicao_pagamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_condicao_pagamento TO astral;


--
-- Name: SEQUENCE bc_fin_condicao_pagamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_condicao_pagamento_id_seq TO astral;


--
-- Name: TABLE bc_fin_consolidacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_consolidacao TO astral;


--
-- Name: SEQUENCE bc_fin_consolidacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_consolidacao_id_seq TO astral;


--
-- Name: TABLE bc_fin_conta_bancaria; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_conta_bancaria TO astral;


--
-- Name: SEQUENCE bc_fin_conta_bancaria_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_conta_bancaria_id_seq TO astral;


--
-- Name: TABLE bc_fin_emprestimo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_emprestimo TO astral;


--
-- Name: SEQUENCE bc_fin_emprestimo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_emprestimo_id_seq TO astral;


--
-- Name: TABLE bc_fin_extrato; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_extrato TO astral;


--
-- Name: SEQUENCE bc_fin_extrato_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_extrato_id_seq TO astral;


--
-- Name: TABLE bc_fin_fluxo_aprovacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_fluxo_aprovacao TO astral;


--
-- Name: SEQUENCE bc_fin_fluxo_aprovacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_fluxo_aprovacao_id_seq TO astral;


--
-- Name: TABLE bc_fin_integracao_bancaria; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_integracao_bancaria TO astral;


--
-- Name: SEQUENCE bc_fin_integracao_bancaria_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_integracao_bancaria_id_seq TO astral;


--
-- Name: TABLE bc_fin_intercompany; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_intercompany TO astral;


--
-- Name: SEQUENCE bc_fin_intercompany_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_intercompany_id_seq TO astral;


--
-- Name: TABLE bc_fin_lancamento_contabil; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_lancamento_contabil TO astral;


--
-- Name: SEQUENCE bc_fin_lancamento_contabil_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_lancamento_contabil_id_seq TO astral;


--
-- Name: TABLE bc_fin_lancamento_partida; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_lancamento_partida TO astral;


--
-- Name: SEQUENCE bc_fin_lancamento_partida_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_lancamento_partida_id_seq TO astral;


--
-- Name: TABLE bc_fin_orcamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_orcamento TO astral;


--
-- Name: SEQUENCE bc_fin_orcamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_orcamento_id_seq TO astral;


--
-- Name: TABLE bc_fin_orcamento_realizado; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_orcamento_realizado TO astral;


--
-- Name: SEQUENCE bc_fin_orcamento_realizado_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_orcamento_realizado_id_seq TO astral;


--
-- Name: TABLE bc_fin_periodo_contabil; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_periodo_contabil TO astral;


--
-- Name: SEQUENCE bc_fin_periodo_contabil_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_periodo_contabil_id_seq TO astral;


--
-- Name: TABLE bc_fin_plano_contas; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_plano_contas TO astral;


--
-- Name: SEQUENCE bc_fin_plano_contas_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_plano_contas_id_seq TO astral;


--
-- Name: TABLE bc_fin_projecao_fluxo_caixa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_projecao_fluxo_caixa TO astral;


--
-- Name: SEQUENCE bc_fin_projecao_fluxo_caixa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_projecao_fluxo_caixa_id_seq TO astral;


--
-- Name: TABLE bc_fin_promessa_pagamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_promessa_pagamento TO astral;


--
-- Name: SEQUENCE bc_fin_promessa_pagamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_promessa_pagamento_id_seq TO astral;


--
-- Name: TABLE bc_fin_provisao_pdd; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_provisao_pdd TO astral;


--
-- Name: SEQUENCE bc_fin_provisao_pdd_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_provisao_pdd_id_seq TO astral;


--
-- Name: TABLE bc_fin_remessa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_remessa TO astral;


--
-- Name: SEQUENCE bc_fin_remessa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_remessa_id_seq TO astral;


--
-- Name: TABLE bc_fin_remessa_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_remessa_item TO astral;


--
-- Name: SEQUENCE bc_fin_remessa_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_remessa_item_id_seq TO astral;


--
-- Name: TABLE bc_fin_renegociacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_renegociacao TO astral;


--
-- Name: SEQUENCE bc_fin_renegociacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_renegociacao_id_seq TO astral;


--
-- Name: TABLE bc_fin_retorno_bancario; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_retorno_bancario TO astral;


--
-- Name: SEQUENCE bc_fin_retorno_bancario_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_retorno_bancario_id_seq TO astral;


--
-- Name: TABLE bc_fin_retorno_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_retorno_item TO astral;


--
-- Name: SEQUENCE bc_fin_retorno_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_retorno_item_id_seq TO astral;


--
-- Name: TABLE bc_fin_stripe_customer; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_stripe_customer TO astral;


--
-- Name: SEQUENCE bc_fin_stripe_customer_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_stripe_customer_id_seq TO astral;


--
-- Name: TABLE bc_fin_stripe_payment; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_stripe_payment TO astral;


--
-- Name: SEQUENCE bc_fin_stripe_payment_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_stripe_payment_id_seq TO astral;


--
-- Name: TABLE bc_fin_stripe_webhook_event; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_stripe_webhook_event TO astral;


--
-- Name: SEQUENCE bc_fin_stripe_webhook_event_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_stripe_webhook_event_id_seq TO astral;


--
-- Name: TABLE bc_fin_tipo_pagamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_tipo_pagamento TO astral;


--
-- Name: SEQUENCE bc_fin_tipo_pagamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_tipo_pagamento_id_seq TO astral;


--
-- Name: TABLE bc_fin_titulo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_titulo TO astral;


--
-- Name: SEQUENCE bc_fin_titulo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_titulo_id_seq TO astral;


--
-- Name: TABLE bc_fin_titulo_parcela; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fin_titulo_parcela TO astral;


--
-- Name: SEQUENCE bc_fin_titulo_parcela_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fin_titulo_parcela_id_seq TO astral;


--
-- Name: TABLE bc_fis_apuracao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_apuracao TO astral;


--
-- Name: SEQUENCE bc_fis_apuracao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_apuracao_id_seq TO astral;


--
-- Name: TABLE bc_fis_certificado_digital; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_certificado_digital TO astral;


--
-- Name: SEQUENCE bc_fis_certificado_digital_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_certificado_digital_id_seq TO astral;


--
-- Name: TABLE bc_fis_cest; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_cest TO astral;


--
-- Name: SEQUENCE bc_fis_cest_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_cest_id_seq TO astral;


--
-- Name: TABLE bc_fis_cfop; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_cfop TO astral;


--
-- Name: SEQUENCE bc_fis_cfop_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_cfop_id_seq TO astral;


--
-- Name: TABLE bc_fis_cnae_servico; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_cnae_servico TO astral;


--
-- Name: SEQUENCE bc_fis_cnae_servico_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_cnae_servico_id_seq TO astral;


--
-- Name: TABLE bc_fis_cte; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_cte TO astral;


--
-- Name: SEQUENCE bc_fis_cte_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_cte_id_seq TO astral;


--
-- Name: TABLE bc_fis_cte_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_cte_item TO astral;


--
-- Name: SEQUENCE bc_fis_cte_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_cte_item_id_seq TO astral;


--
-- Name: TABLE bc_fis_ecd; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_ecd TO astral;


--
-- Name: SEQUENCE bc_fis_ecd_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_ecd_id_seq TO astral;


--
-- Name: TABLE bc_fis_ecf; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_ecf TO astral;


--
-- Name: SEQUENCE bc_fis_ecf_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_ecf_id_seq TO astral;


--
-- Name: TABLE bc_fis_esocial; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_esocial TO astral;


--
-- Name: SEQUENCE bc_fis_esocial_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_esocial_id_seq TO astral;


--
-- Name: TABLE bc_fis_imposto; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_imposto TO astral;


--
-- Name: SEQUENCE bc_fis_imposto_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_imposto_id_seq TO astral;


--
-- Name: TABLE bc_fis_issqn; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_issqn TO astral;


--
-- Name: SEQUENCE bc_fis_issqn_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_issqn_id_seq TO astral;


--
-- Name: TABLE bc_fis_manifestacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_manifestacao TO astral;


--
-- Name: SEQUENCE bc_fis_manifestacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_manifestacao_id_seq TO astral;


--
-- Name: TABLE bc_fis_mdfe; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_mdfe TO astral;


--
-- Name: SEQUENCE bc_fis_mdfe_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_mdfe_id_seq TO astral;


--
-- Name: TABLE bc_fis_nbs; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nbs TO astral;


--
-- Name: SEQUENCE bc_fis_nbs_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nbs_id_seq TO astral;


--
-- Name: TABLE bc_fis_ncm; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_ncm TO astral;


--
-- Name: SEQUENCE bc_fis_ncm_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_ncm_id_seq TO astral;


--
-- Name: TABLE bc_fis_nfce; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfce TO astral;


--
-- Name: SEQUENCE bc_fis_nfce_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfce_id_seq TO astral;


--
-- Name: TABLE bc_fis_nfce_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfce_item TO astral;


--
-- Name: SEQUENCE bc_fis_nfce_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfce_item_id_seq TO astral;


--
-- Name: TABLE bc_fis_nfe; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfe TO astral;


--
-- Name: TABLE bc_fis_nfe_evento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfe_evento TO astral;


--
-- Name: SEQUENCE bc_fis_nfe_evento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfe_evento_id_seq TO astral;


--
-- Name: SEQUENCE bc_fis_nfe_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfe_id_seq TO astral;


--
-- Name: TABLE bc_fis_nfe_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfe_item TO astral;


--
-- Name: SEQUENCE bc_fis_nfe_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfe_item_id_seq TO astral;


--
-- Name: TABLE bc_fis_nfse; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfse TO astral;


--
-- Name: SEQUENCE bc_fis_nfse_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfse_id_seq TO astral;


--
-- Name: TABLE bc_fis_nfse_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfse_item TO astral;


--
-- Name: SEQUENCE bc_fis_nfse_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfse_item_id_seq TO astral;


--
-- Name: TABLE bc_fis_nfse_retorno; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_nfse_retorno TO astral;


--
-- Name: SEQUENCE bc_fis_nfse_retorno_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_nfse_retorno_id_seq TO astral;


--
-- Name: TABLE bc_fis_obrigacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_obrigacao TO astral;


--
-- Name: TABLE bc_fis_obrigacao_entrega; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_obrigacao_entrega TO astral;


--
-- Name: SEQUENCE bc_fis_obrigacao_entrega_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_obrigacao_entrega_id_seq TO astral;


--
-- Name: SEQUENCE bc_fis_obrigacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_obrigacao_id_seq TO astral;


--
-- Name: TABLE bc_fis_palavra_chave; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_palavra_chave TO astral;


--
-- Name: SEQUENCE bc_fis_palavra_chave_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_palavra_chave_id_seq TO astral;


--
-- Name: TABLE bc_fis_regra_tributaria; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_regra_tributaria TO astral;


--
-- Name: SEQUENCE bc_fis_regra_tributaria_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_regra_tributaria_id_seq TO astral;


--
-- Name: TABLE bc_fis_reinf; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_reinf TO astral;


--
-- Name: SEQUENCE bc_fis_reinf_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_reinf_id_seq TO astral;


--
-- Name: TABLE bc_fis_servico_lc116; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_servico_lc116 TO astral;


--
-- Name: SEQUENCE bc_fis_servico_lc116_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_servico_lc116_id_seq TO astral;


--
-- Name: TABLE bc_fis_sped_contribuicoes; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_sped_contribuicoes TO astral;


--
-- Name: SEQUENCE bc_fis_sped_contribuicoes_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_sped_contribuicoes_id_seq TO astral;


--
-- Name: TABLE bc_fis_sped_fiscal; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_fis_sped_fiscal TO astral;


--
-- Name: SEQUENCE bc_fis_sped_fiscal_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_fis_sped_fiscal_id_seq TO astral;


--
-- Name: TABLE bc_gov_controle; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_gov_controle TO astral;


--
-- Name: SEQUENCE bc_gov_controle_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_gov_controle_id_seq TO astral;


--
-- Name: TABLE bc_gov_risco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_gov_risco TO astral;


--
-- Name: SEQUENCE bc_gov_risco_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_gov_risco_id_seq TO astral;


--
-- Name: TABLE bc_grc_avaliacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_avaliacao TO astral;


--
-- Name: SEQUENCE bc_grc_avaliacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_avaliacao_id_seq TO astral;


--
-- Name: TABLE bc_grc_controle; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_controle TO astral;


--
-- Name: SEQUENCE bc_grc_controle_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_controle_id_seq TO astral;


--
-- Name: TABLE bc_grc_evidencia; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_evidencia TO astral;


--
-- Name: SEQUENCE bc_grc_evidencia_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_evidencia_id_seq TO astral;


--
-- Name: TABLE bc_grc_log; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_log TO astral;


--
-- Name: SEQUENCE bc_grc_log_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_log_id_seq TO astral;


--
-- Name: TABLE bc_grc_plano_acao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_plano_acao TO astral;


--
-- Name: SEQUENCE bc_grc_plano_acao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_plano_acao_id_seq TO astral;


--
-- Name: TABLE bc_grc_risco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_risco TO astral;


--
-- Name: TABLE bc_grc_risco_controle; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_risco_controle TO astral;


--
-- Name: SEQUENCE bc_grc_risco_controle_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_risco_controle_id_seq TO astral;


--
-- Name: SEQUENCE bc_grc_risco_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_risco_id_seq TO astral;


--
-- Name: TABLE bc_grc_teste_controle; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_grc_teste_controle TO astral;


--
-- Name: SEQUENCE bc_grc_teste_controle_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_grc_teste_controle_id_seq TO astral;


--
-- Name: TABLE bc_hdp_chamado; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_hdp_chamado TO astral;


--
-- Name: SEQUENCE bc_hdp_chamado_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_hdp_chamado_id_seq TO astral;


--
-- Name: TABLE bc_hdp_comentario; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_hdp_comentario TO astral;


--
-- Name: SEQUENCE bc_hdp_comentario_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_hdp_comentario_id_seq TO astral;


--
-- Name: TABLE bc_ia_analise_preditiva; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_analise_preditiva TO astral;


--
-- Name: SEQUENCE bc_ia_analise_preditiva_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_analise_preditiva_id_seq TO astral;


--
-- Name: TABLE bc_ia_assistente_auditoria; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_assistente_auditoria TO astral;


--
-- Name: SEQUENCE bc_ia_assistente_auditoria_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_assistente_auditoria_id_seq TO astral;


--
-- Name: TABLE bc_ia_chat_mensagem; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_chat_mensagem TO astral;


--
-- Name: SEQUENCE bc_ia_chat_mensagem_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_chat_mensagem_id_seq TO astral;


--
-- Name: TABLE bc_ia_chat_message; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_chat_message TO astral;


--
-- Name: SEQUENCE bc_ia_chat_message_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_chat_message_id_seq TO astral;


--
-- Name: TABLE bc_ia_chat_sessao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_chat_sessao TO astral;


--
-- Name: SEQUENCE bc_ia_chat_sessao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_chat_sessao_id_seq TO astral;


--
-- Name: TABLE bc_ia_chat_session; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_chat_session TO astral;


--
-- Name: SEQUENCE bc_ia_chat_session_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_chat_session_id_seq TO astral;


--
-- Name: TABLE bc_ia_classificacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_classificacao TO astral;


--
-- Name: SEQUENCE bc_ia_classificacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_classificacao_id_seq TO astral;


--
-- Name: TABLE bc_ia_config; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_config TO astral;


--
-- Name: SEQUENCE bc_ia_config_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_config_id_seq TO astral;


--
-- Name: TABLE bc_ia_embedding; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_embedding TO astral;


--
-- Name: SEQUENCE bc_ia_embedding_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_embedding_id_seq TO astral;


--
-- Name: TABLE bc_ia_prompt; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_prompt TO astral;


--
-- Name: SEQUENCE bc_ia_prompt_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_prompt_id_seq TO astral;


--
-- Name: TABLE bc_ia_prompt_template; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ia_prompt_template TO astral;


--
-- Name: SEQUENCE bc_ia_prompt_template_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ia_prompt_template_id_seq TO astral;


--
-- Name: TABLE bc_int_delivery; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_int_delivery TO astral;


--
-- Name: SEQUENCE bc_int_delivery_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_int_delivery_id_seq TO astral;


--
-- Name: TABLE bc_int_endpoint; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_int_endpoint TO astral;


--
-- Name: SEQUENCE bc_int_endpoint_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_int_endpoint_id_seq TO astral;


--
-- Name: TABLE bc_int_webhook_event; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_int_webhook_event TO astral;


--
-- Name: SEQUENCE bc_int_webhook_event_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_int_webhook_event_id_seq TO astral;


--
-- Name: TABLE bc_kb_artigo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_kb_artigo TO astral;


--
-- Name: SEQUENCE bc_kb_artigo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_kb_artigo_id_seq TO astral;


--
-- Name: TABLE bc_migration_log; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_migration_log TO astral;


--
-- Name: SEQUENCE bc_migration_log_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_migration_log_id_seq TO astral;


--
-- Name: TABLE bc_pcp_mps; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_pcp_mps TO astral;


--
-- Name: SEQUENCE bc_pcp_mps_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_pcp_mps_id_seq TO astral;


--
-- Name: TABLE bc_plm_aprovacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_plm_aprovacao TO astral;


--
-- Name: SEQUENCE bc_plm_aprovacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_plm_aprovacao_id_seq TO astral;


--
-- Name: TABLE bc_plm_documento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_plm_documento TO astral;


--
-- Name: SEQUENCE bc_plm_documento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_plm_documento_id_seq TO astral;


--
-- Name: TABLE bc_plm_efeito_mudanca; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_plm_efeito_mudanca TO astral;


--
-- Name: SEQUENCE bc_plm_efeito_mudanca_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_plm_efeito_mudanca_id_seq TO astral;


--
-- Name: TABLE bc_plm_mudanca; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_plm_mudanca TO astral;


--
-- Name: SEQUENCE bc_plm_mudanca_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_plm_mudanca_id_seq TO astral;


--
-- Name: TABLE bc_plm_produto_revisao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_plm_produto_revisao TO astral;


--
-- Name: SEQUENCE bc_plm_produto_revisao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_plm_produto_revisao_id_seq TO astral;


--
-- Name: TABLE bc_prj_etapa; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prj_etapa TO astral;


--
-- Name: SEQUENCE bc_prj_etapa_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prj_etapa_id_seq TO astral;


--
-- Name: TABLE bc_prj_faturamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prj_faturamento TO astral;


--
-- Name: SEQUENCE bc_prj_faturamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prj_faturamento_id_seq TO astral;


--
-- Name: TABLE bc_prj_movimento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prj_movimento TO astral;


--
-- Name: SEQUENCE bc_prj_movimento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prj_movimento_id_seq TO astral;


--
-- Name: TABLE bc_prj_mudanca; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prj_mudanca TO astral;


--
-- Name: SEQUENCE bc_prj_mudanca_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prj_mudanca_id_seq TO astral;


--
-- Name: TABLE bc_prj_projeto; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prj_projeto TO astral;


--
-- Name: SEQUENCE bc_prj_projeto_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prj_projeto_id_seq TO astral;


--
-- Name: TABLE bc_prj_risco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prj_risco TO astral;


--
-- Name: SEQUENCE bc_prj_risco_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prj_risco_id_seq TO astral;


--
-- Name: TABLE bc_prod_alocacao_capacidade; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_alocacao_capacidade TO astral;


--
-- Name: SEQUENCE bc_prod_alocacao_capacidade_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_alocacao_capacidade_id_seq TO astral;


--
-- Name: TABLE bc_prod_apontamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_apontamento TO astral;


--
-- Name: SEQUENCE bc_prod_apontamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_apontamento_id_seq TO astral;


--
-- Name: TABLE bc_prod_centro_trabalho; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_centro_trabalho TO astral;


--
-- Name: SEQUENCE bc_prod_centro_trabalho_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_centro_trabalho_id_seq TO astral;


--
-- Name: TABLE bc_prod_estrutura; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_estrutura TO astral;


--
-- Name: SEQUENCE bc_prod_estrutura_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_estrutura_id_seq TO astral;


--
-- Name: TABLE bc_prod_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_item TO astral;


--
-- Name: SEQUENCE bc_prod_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_item_id_seq TO astral;


--
-- Name: TABLE bc_prod_ordem; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_ordem TO astral;


--
-- Name: SEQUENCE bc_prod_ordem_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_ordem_id_seq TO astral;


--
-- Name: TABLE bc_prod_romaneio; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_romaneio TO astral;


--
-- Name: SEQUENCE bc_prod_romaneio_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_romaneio_id_seq TO astral;


--
-- Name: TABLE bc_prod_romaneio_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_romaneio_item TO astral;


--
-- Name: SEQUENCE bc_prod_romaneio_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_romaneio_item_id_seq TO astral;


--
-- Name: TABLE bc_prod_roteiro; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_roteiro TO astral;


--
-- Name: SEQUENCE bc_prod_roteiro_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_roteiro_id_seq TO astral;


--
-- Name: TABLE bc_prod_roteiro_operacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_prod_roteiro_operacao TO astral;


--
-- Name: SEQUENCE bc_prod_roteiro_operacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_prod_roteiro_operacao_id_seq TO astral;


--
-- Name: TABLE bc_ptl_acesso; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ptl_acesso TO astral;


--
-- Name: SEQUENCE bc_ptl_acesso_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ptl_acesso_id_seq TO astral;


--
-- Name: TABLE bc_qual_inspecao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_qual_inspecao TO astral;


--
-- Name: SEQUENCE bc_qual_inspecao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_qual_inspecao_id_seq TO astral;


--
-- Name: TABLE bc_qual_nao_conformidade; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_qual_nao_conformidade TO astral;


--
-- Name: SEQUENCE bc_qual_nao_conformidade_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_qual_nao_conformidade_id_seq TO astral;


--
-- Name: TABLE bc_qual_plano_inspecao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_qual_plano_inspecao TO astral;


--
-- Name: SEQUENCE bc_qual_plano_inspecao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_qual_plano_inspecao_id_seq TO astral;


--
-- Name: TABLE bc_rh_cargo; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_rh_cargo TO astral;


--
-- Name: SEQUENCE bc_rh_cargo_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_rh_cargo_id_seq TO astral;


--
-- Name: TABLE bc_rh_ferias; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_rh_ferias TO astral;


--
-- Name: SEQUENCE bc_rh_ferias_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_rh_ferias_id_seq TO astral;


--
-- Name: TABLE bc_rh_folha; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_rh_folha TO astral;


--
-- Name: SEQUENCE bc_rh_folha_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_rh_folha_id_seq TO astral;


--
-- Name: TABLE bc_rh_folha_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_rh_folha_item TO astral;


--
-- Name: SEQUENCE bc_rh_folha_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_rh_folha_item_id_seq TO astral;


--
-- Name: TABLE bc_rh_funcionario; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_rh_funcionario TO astral;


--
-- Name: SEQUENCE bc_rh_funcionario_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_rh_funcionario_id_seq TO astral;


--
-- Name: TABLE bc_rh_ponto; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_rh_ponto TO astral;


--
-- Name: SEQUENCE bc_rh_ponto_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_rh_ponto_id_seq TO astral;


--
-- Name: TABLE bc_rh_rescisao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_rh_rescisao TO astral;


--
-- Name: SEQUENCE bc_rh_rescisao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_rh_rescisao_id_seq TO astral;


--
-- Name: TABLE bc_sc_atp; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_sc_atp TO astral;


--
-- Name: SEQUENCE bc_sc_atp_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_sc_atp_id_seq TO astral;


--
-- Name: TABLE bc_sc_carga; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_sc_carga TO astral;


--
-- Name: SEQUENCE bc_sc_carga_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_sc_carga_id_seq TO astral;


--
-- Name: TABLE bc_sc_demanda; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_sc_demanda TO astral;


--
-- Name: SEQUENCE bc_sc_demanda_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_sc_demanda_id_seq TO astral;


--
-- Name: TABLE bc_sc_frete; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_sc_frete TO astral;


--
-- Name: SEQUENCE bc_sc_frete_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_sc_frete_id_seq TO astral;


--
-- Name: TABLE bc_sc_planejamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_sc_planejamento TO astral;


--
-- Name: SEQUENCE bc_sc_planejamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_sc_planejamento_id_seq TO astral;


--
-- Name: TABLE bc_sc_rota; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_sc_rota TO astral;


--
-- Name: SEQUENCE bc_sc_rota_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_sc_rota_id_seq TO astral;


--
-- Name: TABLE bc_sc_tracking; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_sc_tracking TO astral;


--
-- Name: SEQUENCE bc_sc_tracking_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_sc_tracking_id_seq TO astral;


--
-- Name: TABLE bc_scm_ordem_transporte; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_scm_ordem_transporte TO astral;


--
-- Name: SEQUENCE bc_scm_ordem_transporte_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_scm_ordem_transporte_id_seq TO astral;


--
-- Name: TABLE bc_scm_politica_reposicao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_scm_politica_reposicao TO astral;


--
-- Name: SEQUENCE bc_scm_politica_reposicao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_scm_politica_reposicao_id_seq TO astral;


--
-- Name: TABLE bc_srv_contrato; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_srv_contrato TO astral;


--
-- Name: SEQUENCE bc_srv_contrato_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_srv_contrato_id_seq TO astral;


--
-- Name: TABLE bc_srv_ordem_servico; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_srv_ordem_servico TO astral;


--
-- Name: SEQUENCE bc_srv_ordem_servico_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_srv_ordem_servico_id_seq TO astral;


--
-- Name: TABLE bc_srv_os_apontamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_srv_os_apontamento TO astral;


--
-- Name: SEQUENCE bc_srv_os_apontamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_srv_os_apontamento_id_seq TO astral;


--
-- Name: TABLE bc_srv_os_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_srv_os_item TO astral;


--
-- Name: SEQUENCE bc_srv_os_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_srv_os_item_id_seq TO astral;


--
-- Name: TABLE bc_tms_documento_entrega; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_documento_entrega TO astral;


--
-- Name: SEQUENCE bc_tms_documento_entrega_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_documento_entrega_id_seq TO astral;


--
-- Name: TABLE bc_tms_evento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_evento TO astral;


--
-- Name: SEQUENCE bc_tms_evento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_evento_id_seq TO astral;


--
-- Name: TABLE bc_tms_fechamento; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_fechamento TO astral;


--
-- Name: SEQUENCE bc_tms_fechamento_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_fechamento_id_seq TO astral;


--
-- Name: TABLE bc_tms_frete; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_frete TO astral;


--
-- Name: SEQUENCE bc_tms_frete_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_frete_id_seq TO astral;


--
-- Name: TABLE bc_tms_ordem; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_ordem TO astral;


--
-- Name: SEQUENCE bc_tms_ordem_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_ordem_id_seq TO astral;


--
-- Name: TABLE bc_tms_parada; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_parada TO astral;


--
-- Name: SEQUENCE bc_tms_parada_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_parada_id_seq TO astral;


--
-- Name: TABLE bc_tms_rota; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_rota TO astral;


--
-- Name: SEQUENCE bc_tms_rota_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_rota_id_seq TO astral;


--
-- Name: TABLE bc_tms_tracking; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_tms_tracking TO astral;


--
-- Name: SEQUENCE bc_tms_tracking_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_tms_tracking_id_seq TO astral;


--
-- Name: TABLE bc_ven_bonificacao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_bonificacao TO astral;


--
-- Name: SEQUENCE bc_ven_bonificacao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_bonificacao_id_seq TO astral;


--
-- Name: TABLE bc_ven_bonificacao_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_bonificacao_item TO astral;


--
-- Name: SEQUENCE bc_ven_bonificacao_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_bonificacao_item_id_seq TO astral;


--
-- Name: TABLE bc_ven_contrato; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_contrato TO astral;


--
-- Name: SEQUENCE bc_ven_contrato_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_contrato_id_seq TO astral;


--
-- Name: TABLE bc_ven_contrato_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_contrato_item TO astral;


--
-- Name: SEQUENCE bc_ven_contrato_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_contrato_item_id_seq TO astral;


--
-- Name: TABLE bc_ven_devolucao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_devolucao TO astral;


--
-- Name: SEQUENCE bc_ven_devolucao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_devolucao_id_seq TO astral;


--
-- Name: TABLE bc_ven_devolucao_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_devolucao_item TO astral;


--
-- Name: SEQUENCE bc_ven_devolucao_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_devolucao_item_id_seq TO astral;


--
-- Name: TABLE bc_ven_meta; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_meta TO astral;


--
-- Name: SEQUENCE bc_ven_meta_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_meta_id_seq TO astral;


--
-- Name: TABLE bc_ven_pedido; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_pedido TO astral;


--
-- Name: SEQUENCE bc_ven_pedido_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_pedido_id_seq TO astral;


--
-- Name: TABLE bc_ven_pedido_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_pedido_item TO astral;


--
-- Name: SEQUENCE bc_ven_pedido_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_pedido_item_id_seq TO astral;


--
-- Name: TABLE bc_ven_regra_comissao; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_regra_comissao TO astral;


--
-- Name: SEQUENCE bc_ven_regra_comissao_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_regra_comissao_id_seq TO astral;


--
-- Name: TABLE bc_ven_tabela_preco; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_tabela_preco TO astral;


--
-- Name: SEQUENCE bc_ven_tabela_preco_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_tabela_preco_id_seq TO astral;


--
-- Name: TABLE bc_ven_tabela_preco_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_ven_tabela_preco_item TO astral;


--
-- Name: SEQUENCE bc_ven_tabela_preco_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_ven_tabela_preco_item_id_seq TO astral;


--
-- Name: TABLE bc_wkf_definition; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wkf_definition TO astral;


--
-- Name: SEQUENCE bc_wkf_definition_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wkf_definition_id_seq TO astral;


--
-- Name: TABLE bc_wkf_instance; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wkf_instance TO astral;


--
-- Name: SEQUENCE bc_wkf_instance_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wkf_instance_id_seq TO astral;


--
-- Name: TABLE bc_wkf_stage; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wkf_stage TO astral;


--
-- Name: SEQUENCE bc_wkf_stage_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wkf_stage_id_seq TO astral;


--
-- Name: TABLE bc_wkf_task; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wkf_task TO astral;


--
-- Name: SEQUENCE bc_wkf_task_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wkf_task_id_seq TO astral;


--
-- Name: TABLE bc_wms_onda; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wms_onda TO astral;


--
-- Name: SEQUENCE bc_wms_onda_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wms_onda_id_seq TO astral;


--
-- Name: TABLE bc_wms_onda_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wms_onda_item TO astral;


--
-- Name: SEQUENCE bc_wms_onda_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wms_onda_item_id_seq TO astral;


--
-- Name: TABLE bc_wms_volume; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wms_volume TO astral;


--
-- Name: SEQUENCE bc_wms_volume_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wms_volume_id_seq TO astral;


--
-- Name: TABLE bc_wms_volume_item; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bc_wms_volume_item TO astral;


--
-- Name: SEQUENCE bc_wms_volume_item_id_seq; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON SEQUENCE brasil_saas.bc_wms_volume_item_id_seq TO astral;


--
-- Name: TABLE bk_usuario_perfil_20261009; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.bk_usuario_perfil_20261009 TO astral;


--
-- Name: TABLE flyway_schema_history; Type: ACL; Schema: brasil_saas; Owner: sa
--

GRANT ALL ON TABLE brasil_saas.flyway_schema_history TO astral;


--
-- Name: TABLE base_cep; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.base_cep TO dbm_app;


--
-- Name: TABLE fcaixa; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fcaixa TO dbm_app;


--
-- Name: SEQUENCE fcaixa_codcaixa_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fcaixa_codcaixa_seq TO dbm_app;


--
-- Name: TABLE fcentrocusto; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fcentrocusto TO dbm_app;


--
-- Name: SEQUENCE fcentrocusto_codcusto_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fcentrocusto_codcusto_seq TO dbm_app;


--
-- Name: SEQUENCE fcfo_codcfo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fcfo_codcfo_seq TO dbm_app;


--
-- Name: TABLE fcfo; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fcfo TO dbm_app;


--
-- Name: SEQUENCE fcondicao_codcondicao_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fcondicao_codcondicao_seq TO dbm_app;


--
-- Name: TABLE fcondicao; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fcondicao TO dbm_app;


--
-- Name: TABLE fcusto; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fcusto TO dbm_app;


--
-- Name: SEQUENCE fcusto_codcusto_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fcusto_codcusto_seq TO dbm_app;


--
-- Name: TABLE fdatas; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fdatas TO dbm_app;


--
-- Name: SEQUENCE fdatas_iddata_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fdatas_iddata_seq TO dbm_app;


--
-- Name: TABLE fdia; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fdia TO dbm_app;


--
-- Name: SEQUENCE fdia_refdia_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fdia_refdia_seq TO dbm_app;


--
-- Name: SEQUENCE fdocumento_coddoc_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fdocumento_coddoc_seq TO dbm_app;


--
-- Name: TABLE fdocumento; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fdocumento TO dbm_app;


--
-- Name: TABLE fempresa; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fempresa TO dbm_app;


--
-- Name: SEQUENCE fempresa_codigo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fempresa_codigo_seq TO dbm_app;


--
-- Name: TABLE fempresa_vinculo; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fempresa_vinculo TO dbm_app;


--
-- Name: SEQUENCE fempresa_vinculo_codvinculo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fempresa_vinculo_codvinculo_seq TO dbm_app;


--
-- Name: TABLE fextrato; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fextrato TO dbm_app;


--
-- Name: SEQUENCE fextrato_idlanextrato_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fextrato_idlanextrato_seq TO dbm_app;


--
-- Name: TABLE ffinanceiro; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ffinanceiro TO dbm_app;


--
-- Name: SEQUENCE ffinanceiro_codfinanceiro_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ffinanceiro_codfinanceiro_seq TO dbm_app;


--
-- Name: TABLE ffuncionario; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ffuncionario TO dbm_app;


--
-- Name: SEQUENCE ffuncionario_codfuncionario_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ffuncionario_codfuncionario_seq TO dbm_app;


--
-- Name: TABLE fitem; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fitem TO dbm_app;


--
-- Name: SEQUENCE fitem_refitem_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fitem_refitem_seq TO dbm_app;


--
-- Name: SEQUENCE flan_idlan_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.flan_idlan_seq TO dbm_app;


--
-- Name: TABLE flan; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.flan TO dbm_app;


--
-- Name: TABLE fmov; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fmov TO dbm_app;


--
-- Name: SEQUENCE fmov_idmov_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fmov_idmov_seq TO dbm_app;


--
-- Name: TABLE fmovimento; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fmovimento TO dbm_app;


--
-- Name: SEQUENCE fmovimento_codmovimento_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fmovimento_codmovimento_seq TO dbm_app;


--
-- Name: TABLE fmovimento_item; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fmovimento_item TO dbm_app;


--
-- Name: SEQUENCE fmovimento_item_coditem_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fmovimento_item_coditem_seq TO dbm_app;


--
-- Name: TABLE fnota; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fnota TO dbm_app;


--
-- Name: SEQUENCE fnota_id_nota_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fnota_id_nota_seq TO dbm_app;


--
-- Name: TABLE fnota_item; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fnota_item TO dbm_app;


--
-- Name: SEQUENCE fnota_item_id_item_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fnota_item_id_item_seq TO dbm_app;


--
-- Name: TABLE fpessoa; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fpessoa TO dbm_app;


--
-- Name: SEQUENCE fpessoa_id_pessoa_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fpessoa_id_pessoa_seq TO dbm_app;


--
-- Name: SEQUENCE gen_fproduto_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_fproduto_id TO dbm_app;


--
-- Name: TABLE fproduto; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fproduto TO dbm_app;


--
-- Name: TABLE fproduto_ecommerce; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fproduto_ecommerce TO dbm_app;


--
-- Name: SEQUENCE fproduto_ecommerce_codecommerce_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fproduto_ecommerce_codecommerce_seq TO dbm_app;


--
-- Name: TABLE fproduto_fiscal; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fproduto_fiscal TO dbm_app;


--
-- Name: SEQUENCE fproduto_fiscal_codproduto_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fproduto_fiscal_codproduto_seq TO dbm_app;


--
-- Name: TABLE fproduto_imagem; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fproduto_imagem TO dbm_app;


--
-- Name: SEQUENCE fproduto_imagem_codimagem_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fproduto_imagem_codimagem_seq TO dbm_app;


--
-- Name: TABLE fproduto_kit; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fproduto_kit TO dbm_app;


--
-- Name: SEQUENCE fproduto_kit_codkit_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fproduto_kit_codkit_seq TO dbm_app;


--
-- Name: SEQUENCE fproduto_kit_codproduto_filho_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fproduto_kit_codproduto_filho_seq TO dbm_app;


--
-- Name: TABLE fproduto_movimento; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fproduto_movimento TO dbm_app;


--
-- Name: SEQUENCE fproduto_movimento_codmov_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fproduto_movimento_codmov_seq TO dbm_app;


--
-- Name: TABLE fproduto_variacao; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fproduto_variacao TO dbm_app;


--
-- Name: SEQUENCE fproduto_variacao_codvariacao_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fproduto_variacao_codvariacao_seq TO dbm_app;


--
-- Name: TABLE frecibo; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.frecibo TO dbm_app;


--
-- Name: SEQUENCE frecibo_idrecibo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.frecibo_idrecibo_seq TO dbm_app;


--
-- Name: TABLE ftipo; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ftipo TO dbm_app;


--
-- Name: SEQUENCE ftipo_codtipo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ftipo_codtipo_seq TO dbm_app;


--
-- Name: SEQUENCE ftipopagamento_codtipo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ftipopagamento_codtipo_seq TO dbm_app;


--
-- Name: TABLE ftipopagamento; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ftipopagamento TO dbm_app;


--
-- Name: TABLE fusuario; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fusuario TO dbm_app;


--
-- Name: SEQUENCE fusuario_codigo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fusuario_codigo_seq TO dbm_app;


--
-- Name: TABLE fvenda; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fvenda TO dbm_app;


--
-- Name: SEQUENCE fvenda_id_venda_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fvenda_id_venda_seq TO dbm_app;


--
-- Name: TABLE fvenda_item; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.fvenda_item TO dbm_app;


--
-- Name: SEQUENCE fvenda_item_id_item_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.fvenda_item_id_item_seq TO dbm_app;


--
-- Name: SEQUENCE gen_fcfo_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_fcfo_id TO dbm_app;


--
-- Name: SEQUENCE gen_fcusto_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_fcusto_id TO dbm_app;


--
-- Name: SEQUENCE gen_fdia_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_fdia_id TO dbm_app;


--
-- Name: SEQUENCE gen_fextrato_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_fextrato_id TO dbm_app;


--
-- Name: SEQUENCE gen_ffuncionario_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_ffuncionario_id TO dbm_app;


--
-- Name: SEQUENCE gen_fitem_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_fitem_id TO dbm_app;


--
-- Name: SEQUENCE gen_flan_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_flan_id TO dbm_app;


--
-- Name: SEQUENCE gen_fmov_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_fmov_id TO dbm_app;


--
-- Name: SEQUENCE gen_frecibo_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_frecibo_id TO dbm_app;


--
-- Name: SEQUENCE gen_tmunicipo_id; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gen_tmunicipo_id TO dbm_app;


--
-- Name: TABLE gparametro; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.gparametro TO dbm_app;


--
-- Name: SEQUENCE gparametro_idparametro_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.gparametro_idparametro_seq TO dbm_app;


--
-- Name: TABLE tcnae_servico; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.tcnae_servico TO dbm_app;


--
-- Name: TABLE tissqn; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.tissqn TO dbm_app;


--
-- Name: SEQUENCE tissqn_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.tissqn_id_seq TO dbm_app;


--
-- Name: TABLE tmp_restaura; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.tmp_restaura TO dbm_app;


--
-- Name: SEQUENCE tmunicipio_refmunicipio_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.tmunicipio_refmunicipio_seq TO dbm_app;


--
-- Name: TABLE tmunicipio; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.tmunicipio TO dbm_app;


--
-- Name: SEQUENCE tmunicipio_ref_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.tmunicipio_ref_seq TO dbm_app;


--
-- Name: TABLE tncm; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.tncm TO dbm_app;


--
-- Name: TABLE tservico_lc116; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.tservico_lc116 TO dbm_app;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: brasil_saas; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA brasil_saas GRANT ALL ON SEQUENCES TO astral;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: brasil_saas; Owner: sa
--

ALTER DEFAULT PRIVILEGES FOR ROLE sa IN SCHEMA brasil_saas GRANT ALL ON SEQUENCES TO astral;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: brasil_saas; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA brasil_saas GRANT ALL ON TABLES TO astral;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: brasil_saas; Owner: sa
--

ALTER DEFAULT PRIVILEGES FOR ROLE sa IN SCHEMA brasil_saas GRANT ALL ON TABLES TO astral;


--
-- PostgreSQL database dump complete
--

\unrestrict B6qyw1UnbSRcM55QH7rsKPXWXMJXwynYMNstyMgy4w7R1UnJzdpsRxDA51myPxL

