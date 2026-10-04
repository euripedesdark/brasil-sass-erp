SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('vendas:devolucao:leitura','devolucao','leitura','Consultar trocas e devolucoes'),
 ('vendas:devolucao:escrita','devolucao','escrita','Solicitar, aprovar e receber devolucoes')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('vendas:devolucao:leitura','vendas:devolucao:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);
