-- Ajusta a propriedade dos objetos do schema brasil_saas para o usuario de
-- aplicacao (sa), que e quem o ERP usa para conectar.
--
-- Contexto: as tabelas criadas por migrations aplicadas manualmente com
-- superusuario (postgres) ficaram com owner = postgres. A aplicacao conecta
-- como sa, e o V161 falhou no boot com:
--
--   ERRO:  e necessario ser o dono da tabela bc_cont_rateio
--
-- porque o ALTER TABLE de uma migration seguinte exige propriedade. O mesmo
-- ocorreria com qualquer banco recriado a partir destas migrations.
--
-- Nao mexe em dado, coluna ou indice: apenas owner. Os objetos sao filtrados
-- por owner diferente do de aplicacao, entao rodar varias vezes nao altera
-- nada e nao interfere em tabelas ja corretas.
--
-- As sequencias seguem a tabela dona por dependencia de IDENTITY/SERIAL, por
-- isso nao sao tratadas aqui separadamente: alterar a tabela arrasta a
-- sequencia, e os indices acompanham a tabela.

DO $$
DECLARE
    r record;
BEGIN
    -- Tabelas cujo owner nao e o usuario de aplicacao.
    FOR r IN
        SELECT c.oid::regclass AS rel
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'brasil_saas'
          AND c.relkind = 'r'
          AND pg_get_userbyid(c.relowner) <> 'sa'
    LOOP
        EXECUTE format('ALTER TABLE %s OWNER TO sa', r.rel);
    END LOOP;

    -- Sequencias orfas, sem tabela dona correspondente.
    FOR r IN
        SELECT c.oid::regclass AS rel
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'brasil_saas'
          AND c.relkind = 'S'
          AND pg_get_userbyid(c.relowner) <> 'sa'
    LOOP
        EXECUTE format('ALTER SEQUENCE %s OWNER TO sa', r.rel);
    END LOOP;
END
$$;
