SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('rh:ferias:leitura','ferias','leitura','Consultar ferias'),
 ('rh:ferias:escrita','ferias','escrita','Programar e movimentar ferias')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('rh:ferias:leitura','rh:ferias:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);
