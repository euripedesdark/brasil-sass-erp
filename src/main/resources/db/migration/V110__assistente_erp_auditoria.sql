-- =============================================================================
-- V110: Assistente ERP — auditoria, e o modelo default de verdade
-- =============================================================================
--
-- POR QUE ESTA MIGRATION EXISTE
--
-- O dono definiu a ordem das fontes do assistente, e ela é o oposto do usual:
--
--   pergunta -> busca fiscal -> dados do ERP -> documentação -> contexto
--            -> OpenRouter -> resposta
--
-- O modelo é a ÚLTIMA fonte. Ele explica, não decide: o código fiscal da
-- resposta vem da `GET /api/fiscal/busca`, que é determinístico, e o modelo só
-- escreve a frase em volta. A evidência de que isso importa está medida: um
-- modelo gratuito de OpenRouter respondeu "o NCM de cerveja é 2203", quando o
-- código é 22030000 — e a busca acerta os dois.
--
-- A auditoria existe porque essa separação precisa ser conferida depois. Se um
-- dia a resposta parecer experiência do modelo, é preciso voltar e ver o que o
-- contexto tinha. Guardar pergunta, contexto, modelo e tempo é o que permite
-- essa volta.
--
-- ============================================================================
SET search_path TO brasil_saas, public;

CREATE TABLE IF NOT EXISTS bc_ia_assistente_auditoria (
    id             BIGSERIAL PRIMARY KEY,
    uuid           UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id     BIGINT NOT NULL,
    usuario_id     BIGINT,

    pergunta       TEXT NOT NULL,
    -- as fontes que responderam, e o que cada uma devolveu
    contexto       JSONB,
    -- ex.: 'fiscal,dados,documentacao'
    fontes         VARCHAR(200),

    modelo         VARCHAR(120),
    -- o texto cru do modelo, separado da resposta final. São coisas diferentes:
    -- a resposta final pode ser montada sem o modelo, e nesse caso o campo vem
    -- vazio, o que é informação por si.
    resposta_modelo TEXT,
    resposta       TEXT NOT NULL,

    -- ok | modelo-indisponivel | erro
    status         VARCHAR(32) NOT NULL DEFAULT 'ok',
    erro           TEXT,

    duracao_ms     BIGINT,
    tokens_entrada INTEGER,
    tokens_saida   INTEGER,

    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    created_by     BIGINT,
    updated_by     BIGINT,
    deleted_at     TIMESTAMP
);

COMMENT ON TABLE bc_ia_assistente_auditoria IS
    'Auditoria do Assistente ERP: pergunta, contexto, modelo e tempo. Sem ela não há como conferir, depois, se a resposta veio do ERP ou do modelo.';

CREATE INDEX IF NOT EXISTS idx_assist_empresa_data
    ON bc_ia_assistente_auditoria (empresa_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_assist_status
    ON bc_ia_assistente_auditoria (status) WHERE deleted_at IS NULL;

-- ---------- 2. o modelo default de verdade ----------
--
-- As 58 linhas de bc_ia_config são semente, e o default_model delas é
-- 'SEED DEFAULT_MODEL n' — um nome que não existe no OpenRouter. Sem corrigir,
-- toda chamada falha.
--
-- Só a empresa 1 é tocada, e só a linha que está com valor de semente. Trocar o
-- modelo depois é uma linha de UPDATE: a arquitetura não tem o modelo preso em
-- lugar nenhum, ele vem daqui.
--
-- O modelo escolhido foi medido. Do free tier do OpenRouter, 20 modelos têm
-- prompt=0 e completion=0; vários devolvem 429, e o 'openrouter/free' cai num
-- modelo que devolve content=null com o texto em 'reasoning'. O que respondeu
-- conteúdo de verdade e rápido foi o 'inclusionai/ling-3.0-flash-sante:free'.
UPDATE bc_ia_config
   SET default_model = 'inclusionai/ling-3.0-flash-sante:free',
       updated_at    = now()
 WHERE empresa_id = 1
   AND default_model LIKE 'SEED%';

