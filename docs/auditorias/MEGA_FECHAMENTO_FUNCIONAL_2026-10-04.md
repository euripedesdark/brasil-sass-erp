# Mega fechamento funcional — 2026-10-04

## Objetivo
Este é o último bloco funcional antes de considerar o ERP encerrado em termos de código de aplicação. O foco foi fechar as operações que ainda estavam conscientemente incompletas, sem recriar módulos que já estavam prontos.

CI/CD e IA continuam fora do escopo funcional.

## Fiscal — CT-e
Foi fechada a cadeia operacional: XML recebido; validação; assinatura A1; transmissão; persistência tenant-aware; retorno cStat/xMotivo/protocolo; consulta; cancelamento; listagem; detalhe; XML.

Rotas: POST /api/fiscal/cte/emitir; GET /api/fiscal/cte/consultar/{chave}; POST /api/fiscal/cte/{id}/cancelar; GET /api/fiscal/cte/status; GET /api/fiscal/cte; GET /api/fiscal/cte/{id}.

## Fiscal — MDF-e
Foi fechada a cadeia operacional: XML recebido; validação; assinatura A1; envio síncrono à SVRS; persistência; consulta por chave; recibo; cancelamento; encerramento; listagem; detalhe; XML.

Rotas: POST /api/fiscal/mdfe/emitir; GET /api/fiscal/mdfe/consultar/{chave}; GET /api/fiscal/mdfe/recibo; POST /api/fiscal/mdfe/{id}/cancelar; POST /api/fiscal/mdfe/{id}/encerrar; GET /api/fiscal/mdfe/status; GET /api/fiscal/mdfe; GET /api/fiscal/mdfe/{id}.

## NFS-e / NF-e
Não foram recriadas implementações já existentes. NF-e mantém emissão, assinatura, autorização, consulta, cancelamento e XML. NFS-e mantém os fluxos municipal/nacional já implementados, incluindo São Paulo e Rondonópolis homologado, além do registro de retornos.

## Não fiscal já fechado
Vendas; Compras; Supply Chain/3-way; Estoque/WMS; Produção/PCP/MRP/MPS/Capacidade; Qualidade; Financeiro/Caixa/Boletos; Contabilidade; Ativos; RH; Projetos; Comissões; Aprovações; BI/KPI/Relatórios/Agendamentos; tenant e autorização.

## Segurança
CT-e e MDF-e usam fiscal:cte:leitura, fiscal:cte:escrita, fiscal:mdfe:leitura e fiscal:mdfe:escrita. A empresa vem do principal autenticado; nenhuma operação nova aceita empresaId arbitrário.

## O que ainda pode impedir uma emissão real
Certificado A1 válido; cadeia de confiança; rede; ambiente correto; cadastro fiscal; homologação efetiva por UF/provedor; regras fiscais válidas. Esses são requisitos externos, não lacunas de endpoint.

## Resultado
Depois deste bloco, não há mais módulo de negócio grande a ser criado. O restante deve ser correção de defeito encontrado em execução/homologação ou evolução deliberada, não nova rodada de completar telas/endpoints.