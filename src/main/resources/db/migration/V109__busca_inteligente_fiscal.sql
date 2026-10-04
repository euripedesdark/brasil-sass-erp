-- =============================================================================
-- V109: Busca inteligente de código fiscal (NCM, ISSQN, CFOP)
-- =============================================================================
--
-- POR QUE ESTA MIGRATION EXISTE
--
-- O usuário que cadastra um produto NÃO conhece a classificação fiscal. Ele
-- conhece o negócio. A evidência de que isso custa caro está no próprio banco:
-- 4 produtos com NCM `1905.21` (arroz) — e arroz é capítulo 10; 2 com `3402.20`
-- (detergente) — código que não existe na tabela oficial; 7 medicamentos em
-- `3004.90`, que tem 68 códigos possíveis.
--
-- A busca atual só tem descrição (`findByDescricaoContaining`) e, no ISSQN,
-- nada. Com a descrição oficial, "não descafeinado" é a única pista de que um
-- café é 09012100. Quem cadastra não conhece o código.
--
-- O QUE ENTRA, E DE ONDE
--
--   Nível 1 — DERIVADO, sem invenção: os tokens da própria descrição oficial,
--   minúsculos e sem acento. 8.887 palavras distintas. Fonte: bc_fis_ncm,
--   bc_fis_issqn e bc_fis_cfop.
--
--   Nível 2 — CURADO: sinônimos de negócio. Ficam em arquivo à parte
--   (bd/busca_fiscal/palavras_curadas.csv), com a origem declarada linha a
--   linha, para que ninguém confunda curadoria com dado oficial.
--
--   Palavras-chave NÃO são dado fiscal. Uma palavra errada faz a busca errar,
--   não a nota. Ainda assim, a origem é declarada em `origem`, como no V108.
--
-- A NORMALIZAÇÃO
--
--   Texto:   minúsculo, sem acento, só letras e dígitos.
--   Código:  só dígitos, e SEM ZEROS À ESQUERDA. É o que faz o usuário
--            encontrar o código sem conhecer a pontuação:
--              107      -> 107        contra  01.07.01.000 -> 10701000  (prefixo)
--              2203.00  -> 220300     contra  22030000     -> 22030000   (prefixo)
--
-- ============================================================================
SET search_path TO brasil_saas, public;

-- ---------- 1. normalização do CÓDIGO, no banco ----------
--
-- Só dígitos e sem zeros à esquerda. Não usa classe de letra, então não depende
-- da collation — verificado nesta base, que é pt_BR.UTF-8. É o que faz o
-- usuário encontrar o código sem conhecer a pontuação:
--     107     -> 107      contra 01.07.01.000 -> 10701000   (prefixo)
--     2203.00 -> 220300   contra 22030000     -> 22030000   (prefixo)
--
-- A NORMALIZAÇÃO DE TEXTO NÃO É FEITA AQUI, E É DELIBERADO.
--
-- Remover acento e manter só letras tem um problema nesta collation: a classe
-- [a-z] resolve pela ordenação do locale, e o resultado muda entre bancos. Um
-- `regexp_replace(..., '[^a-z0-9]+', ' ', 'g')` sobre 'Cafe ACAO 1.07' devolveu
-- ' afe 1 07' — com o 'C' e o 'ACAO' engolidos. Uma função de normalização que
-- dá resultado diferente conforme o locale do servidor é pior do que nenhuma.
--
-- Então: o Java normaliza (Normalizer + lowercase), grava a palavra já
-- normalizada em `palavra`, e a busca compara igualdade e prefixo. O banco só
-- filtra. Deterministico, testável, e igual em qualquer máquina.

CREATE OR REPLACE FUNCTION brasil_saas.fiscal_normaliza_codigo(p_txt text)
RETURNS text LANGUAGE sql IMMUTABLE STRICT AS $f$
    SELECT ltrim(regexp_replace(p_txt, '[^0-9]', '', 'g'), '0');
$f$;

COMMENT ON FUNCTION brasil_saas.fiscal_normaliza_codigo(text) IS
    'Código sem pontuação e sem zeros à esquerda, para casar o que o usuário digita com o que está cadastrado. Só usa dígito, então independe de collation.';

-- ---------- 2. a estrutura ----------
CREATE TABLE IF NOT EXISTS bc_fis_palavra_chave (
    id         BIGSERIAL PRIMARY KEY,
    uuid       UUID NOT NULL DEFAULT gen_random_uuid(),
    tabela     VARCHAR(16)  NOT NULL,
    codigo     VARCHAR(16)  NOT NULL,
    palavra    VARCHAR(80)  NOT NULL,
    peso       SMALLINT     NOT NULL DEFAULT 5,
    origem     VARCHAR(64),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT ck_palavra_tabela CHECK (tabela IN ('ncm', 'issqn', 'cfop'))
);

COMMENT ON TABLE bc_fis_palavra_chave IS
    'Índice de busca do cadastro fiscal. Nível 1: token da descrição oficial. Nível 2: sinônimo curado, com origem declarada.';

-- A busca é por palavra exata e por prefixo, então o índice precisa servir aos dois.
CREATE INDEX IF NOT EXISTS idx_palavra ON bc_fis_palavra_chave (palavra) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_tabela_palavra ON bc_fis_palavra_chave (tabela, palavra) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_palavra ON bc_fis_palavra_chave (tabela, codigo, palavra)
    WHERE deleted_at IS NULL;

-- ---------- 3. onde os dados vêm ----------
--
-- A carga NÃO é feita aqui, e é deliberado.
--
-- As palavras saem dos tokens da descrição oficial, e tokenizar exige remover
-- acento e partir por separador. Fazer isso em SQL com classe de caractere
-- depende da collation: nesta base, pt_BR.UTF-8, a classe [a-z] resolve pela
-- ordenação do locale e o resultado muda entre bancos. Um
-- `regexp_replace('Cafe ACAO 1.07', '[^a-z0-9]+', ' ', 'g')` aqui devolve
-- ' afe 1 07' — o C e o ACAO engolidos. Se a mesma migration desse resultado
-- diferente em outra máquina, o índice de busca fica silenciosamente errado.
--
-- Então a carga segue o padrão que o projeto já usa para dado oficial
-- (ver `scripts/seed/converter_oficiais.py` e `carregar_oficiais.sql`):
--
--   bd/busca_fiscal/gerar_palavras.py   gera o CSV, com a MESMA normalização
--                                       do Java (Normalizer + lowercase)
--   bd/busca_fiscal/carregar_palavras.sql   COPY para esta tabela
--
-- O Java normaliza, e o Python gera, com a mesma regra — e isso é verificado
-- por um teste que compara os dois sobre a mesma entrada.
--
-- Para recarregar:
--   python3 bd/busca_fiscal/gerar_palavras.py
--   sudo -u postgres psql -d brasil-saas -f bd/busca_fiscal/carregar_palavras.sql
--
