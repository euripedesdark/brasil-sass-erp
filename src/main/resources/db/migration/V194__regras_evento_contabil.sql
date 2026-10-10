-- Regras de contabilizacao automatica (EventoContabilService).
-- codigo = evento (BAIXA_TITULO, FATURAMENTO_VENDA, ...).
-- Contas: tenta resolver no plano de contas da empresa por descricao/codigo tipicos.
-- Se nao achar conta, a regra fica com NULL e o servico gera pendencia ate o usuario completar.

-- Funcao auxiliar local: primeira conta analitica ativa que case com o padrao.
CREATE OR REPLACE FUNCTION brasil_saas._ctb_conta_por_padrao(p_empresa_id bigint, p_padroes text[])
RETURNS bigint
LANGUAGE plpgsql
AS $$
DECLARE
  v_id bigint;
  p text;
BEGIN
  FOREACH p IN ARRAY p_padroes LOOP
    SELECT c.id INTO v_id
      FROM brasil_saas.bc_fin_plano_contas c
     WHERE c.empresa_id = p_empresa_id
       AND c.deleted_at IS NULL
       AND c.ativa IS TRUE
       AND (
         lower(c.descricao) LIKE '%' || lower(p) || '%'
         OR c.codigo = p
         OR c.codigo LIKE p || '%'
       )
     ORDER BY c.nivel DESC, c.codigo
     LIMIT 1;
    IF v_id IS NOT NULL THEN
      RETURN v_id;
    END IF;
  END LOOP;
  RETURN NULL;
END;
$$;

-- Para cada empresa: upsert das regras dos eventos do EventoContabilService.
DO $$
DECLARE
  e record;
  c_caixa bigint;
  c_cliente bigint;
  c_fornecedor bigint;
  c_estoque bigint;
  c_receita bigint;
  c_cmv bigint;
  c_despesa_pessoal bigint;
  c_salario_pagar bigint;
BEGIN
  FOR e IN
    SELECT empresa_id FROM (
      SELECT DISTINCT empresa_id FROM brasil_saas.bc_fin_plano_contas WHERE deleted_at IS NULL
      UNION
      SELECT id AS empresa_id FROM brasil_saas.bc_core_empresa WHERE deleted_at IS NULL
    ) x
  LOOP
    c_caixa := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['caixa','banco','bancos','disponibilidades','1.1.1']);
    c_cliente := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['cliente','clientes','duplicata a receber','contas a receber','1.1.2']);
    c_fornecedor := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['fornecedor','fornecedores','duplicata a pagar','contas a pagar','2.1.1']);
    c_estoque := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['estoque','estoques','mercadoria','1.1.3']);
    c_receita := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['receita de venda','receita bruta','vendas de mercadoria','3.1']);
    c_cmv := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['cmv','custo da mercadoria','custo das mercadorias','4.1']);
    c_despesa_pessoal := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['despesa com pessoal','salario','folha','ordenado']);
    c_salario_pagar := brasil_saas._ctb_conta_por_padrao(e.empresa_id,
      ARRAY['salarios a pagar','ordenados a pagar','folha a pagar','2.1.2']);

    -- BAIXA_TITULO: padrao recebivel (D caixa / C cliente).
    -- Titulos a pagar podem exigir regra propria; ajuste manual se necessario.
    INSERT INTO brasil_saas.bc_cont_regra_lancamento
      (empresa_id, codigo, nome, origem, conta_debito_id, conta_credito_id, historico, ativo, configuracao)
    VALUES
      (e.empresa_id, 'BAIXA_TITULO', 'Baixa de titulo (automatica)', 'FINANCEIRO',
       c_caixa, c_cliente, 'Baixa automatica de titulo', true,
       jsonb_build_object(
         'evento', 'BAIXA_TITULO',
         'hint', 'Receber: D caixa C cliente. Pagar: inverter contas ou criar BAIXA_TITULO_PAGAR.'
       ))
    ON CONFLICT (empresa_id, codigo) DO UPDATE SET
      nome = EXCLUDED.nome,
      origem = EXCLUDED.origem,
      conta_debito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_debito_id, EXCLUDED.conta_debito_id),
      conta_credito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_credito_id, EXCLUDED.conta_credito_id),
      historico = EXCLUDED.historico,
      ativo = true,
      updated_at = now();

    INSERT INTO brasil_saas.bc_cont_regra_lancamento
      (empresa_id, codigo, nome, origem, conta_debito_id, conta_credito_id, historico, ativo, configuracao)
    VALUES
      (e.empresa_id, 'FATURAMENTO_VENDA', 'Faturamento de venda (automatica)', 'VENDAS',
       c_cliente, c_receita, 'Faturamento automatico de venda', true,
       jsonb_build_object('evento', 'FATURAMENTO_VENDA'))
    ON CONFLICT (empresa_id, codigo) DO UPDATE SET
      nome = EXCLUDED.nome,
      origem = EXCLUDED.origem,
      conta_debito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_debito_id, EXCLUDED.conta_debito_id),
      conta_credito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_credito_id, EXCLUDED.conta_credito_id),
      historico = EXCLUDED.historico,
      ativo = true,
      updated_at = now();

    INSERT INTO brasil_saas.bc_cont_regra_lancamento
      (empresa_id, codigo, nome, origem, conta_debito_id, conta_credito_id, historico, ativo, configuracao)
    VALUES
      (e.empresa_id, 'RECEBIMENTO_COMPRA', 'Recebimento de compra (automatica)', 'COMPRAS',
       c_estoque, c_fornecedor, 'Recebimento automatico de compra', true,
       jsonb_build_object('evento', 'RECEBIMENTO_COMPRA'))
    ON CONFLICT (empresa_id, codigo) DO UPDATE SET
      nome = EXCLUDED.nome,
      origem = EXCLUDED.origem,
      conta_debito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_debito_id, EXCLUDED.conta_debito_id),
      conta_credito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_credito_id, EXCLUDED.conta_credito_id),
      historico = EXCLUDED.historico,
      ativo = true,
      updated_at = now();

    INSERT INTO brasil_saas.bc_cont_regra_lancamento
      (empresa_id, codigo, nome, origem, conta_debito_id, conta_credito_id, historico, ativo, configuracao)
    VALUES
      (e.empresa_id, 'MOVIMENTO_ESTOQUE', 'Movimento de estoque (automatica)', 'ESTOQUE',
       c_cmv, c_estoque, 'Baixa de estoque / CMV automatico', true,
       jsonb_build_object('evento', 'MOVIMENTO_ESTOQUE'))
    ON CONFLICT (empresa_id, codigo) DO UPDATE SET
      nome = EXCLUDED.nome,
      origem = EXCLUDED.origem,
      conta_debito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_debito_id, EXCLUDED.conta_debito_id),
      conta_credito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_credito_id, EXCLUDED.conta_credito_id),
      historico = EXCLUDED.historico,
      ativo = true,
      updated_at = now();

    INSERT INTO brasil_saas.bc_cont_regra_lancamento
      (empresa_id, codigo, nome, origem, conta_debito_id, conta_credito_id, historico, ativo, configuracao)
    VALUES
      (e.empresa_id, 'FOLHA_PROCESSADA', 'Folha processada (automatica)', 'RH',
       c_despesa_pessoal, c_salario_pagar, 'Provisao/folha processada automatica', true,
       jsonb_build_object('evento', 'FOLHA_PROCESSADA'))
    ON CONFLICT (empresa_id, codigo) DO UPDATE SET
      nome = EXCLUDED.nome,
      origem = EXCLUDED.origem,
      conta_debito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_debito_id, EXCLUDED.conta_debito_id),
      conta_credito_id = COALESCE(brasil_saas.bc_cont_regra_lancamento.conta_credito_id, EXCLUDED.conta_credito_id),
      historico = EXCLUDED.historico,
      ativo = true,
      updated_at = now();
  END LOOP;
END $$;

DROP FUNCTION IF EXISTS brasil_saas._ctb_conta_por_padrao(bigint, text[]);
