# Auditoria de amplitude ERP — 2026-10-07

## Conclusão executiva

O main atual já possui módulos próprios para Core/multiempresa, cadastros, vendas, compras, WMS, financeiro, fiscal, contabilidade, RH, serviços, produção, qualidade, ativos/manutenção, CRM, projetos, DMS, workflow, portais, BI, IA e integrações.

A quantidade de telas/rotas não é suficiente para afirmar equivalência com SAP. O critério correto é fluxo ponta a ponta:

Tela -> Service React -> HTTP -> Controller -> Service -> regra -> persistência/integração -> auditoria.

## Correção importante do Fiscal

Documentos históricos diziam que NF-e era simulada. Isso não descreve mais o main atual.

O código atual possui NFeXmlBuilder, NFeEmissaoValidator, SefazConfig e NFeServiceImpl. A implementação monta o XML, chama java-nfe para envio, trata retorno síncrono/assíncrono, consulta recibo, valida chave/protocolo, persiste XML e implementa cancelamento e consulta.

A emissão está condicionada a brasil-saas.fiscal.sefaz.enabled=true e a certificado digital válido. Portanto o gap atual é ativação/homologação operacional e conclusão dos demais documentos/eventos, não um protocolo simulado.

Ainda precisam fechar: NFC-e ponta a ponta, CT-e, MDF-e, eventos, contingência, CC-e/inutilização/manifestação quando aplicável, SPED operacional, documentos auxiliares e tratamento/reprocessamento de rejeições.

## Blocos adicionados nesta auditoria

### V155 — Gestão Empresarial

- contratos;
- qualificação de fornecedores;
- metas comerciais;
- períodos/etapas de fechamento;
- orçamento gerencial;
- tesouraria projetada;
- cenários tributários.

Rota: /gestao-empresarial

### V156 — Supply Chain / Engenharia / EHS

- políticas de reposição;
- ordens de transporte;
- revisões de produto;
- mudanças de engenharia;
- ocorrências EHS;
- contratos de serviço.

Rota: /supply-chain-enterprise

### V157 — Governança Corporativa

- intercompany;
- reconciliação intercompany;
- consolidação financeira;
- riscos corporativos;
- controles internos.

Rota: /governanca-corporativa

Também foi atualizada a matriz visual de paridade para não classificar NF-e atual como simulada e para refletir os novos blocos.

## Matriz atual

| Domínio | Estado | Próximo gap |
|---|---|---|
| Core / Multiempresa | AVANÇADO | governança de grupos/filiais |
| Cadastro mestre | PARCIAL/AVANÇADO | MDM, deduplicação, versionamento |
| Vendas | AVANÇADO | contratos, recorrência, pricing/ATP |
| Compras/Sourcing | AVANÇADO | sourcing estratégico e contratos |
| WMS | AVANÇADO | EWM avançado, ondas, serial, put-away |
| Produção/PCP | AVANÇADO | APS, sequenciamento, custeio |
| Financeiro | AVANÇADO | treasury corporativa avançada |
| Contabilidade | PARCIAL/AVANÇADO | close, DRE, balanço, automação |
| Intercompany | BASE FUNCIONAL | eliminações e matching automáticos |
| Fiscal | AVANÇADO | completar documentos/eventos em produção |
| RH/Folha | PARCIAL | CLT/eSocial/benefícios completo |
| Ativos/Manutenção | AVANÇADO | manutenção preventiva e ciclo de peças |
| Qualidade | AVANÇADO | CAPA/8D/auditorias/amostragem |
| CRM | PARCIAL | marketing, forecast, customer success |
| Serviços | PARCIAL | SLA, agenda, parts e billing |
| Projetos | AVANÇADO | portfolio/resource planning |
| Transporte | PARCIAL | TMS, tracking, freight settlement |
| PLM/Engenharia | PARCIAL | ECM/lifecycle completo |
| EHS | BÁSICO | riscos, inspeções e compliance |
| DMS/Workflow | AVANÇADO | BPM/assinatura/retensão avançados |
| BI | PARCIAL | DW/DataLake, ETL e cubos |
| IA | PARCIAL/AVANÇADO | automação transacional e governança |
| GRC | BASE FUNCIONAL | evidências, SoD e testes de controles |

## Próximos blocos de maior retorno

1. Fiscal real completo: NFC-e, CT-e, MDF-e, eventos, contingência, SPED e rejeições.
2. Contabilidade/Close: documento -> regra contábil -> lançamento -> rateio -> período -> DRE/balanço -> consolidação.
3. Supply Chain/TMS: DRP, APS, ATP/CTP, roteirização, tracking e freight settlement.
4. PLM: produto -> revisão -> BOM -> roteiro -> mudança -> aprovação -> produção.
5. RH: CLT, benefícios, afastamentos, eSocial e fechamento.
6. BI/DW: dimensões/fatos, ETL incremental, histórico, métricas, cubos e control tower.
7. GRC/EHS: matriz de risco, controles, evidências, auditoria, CAPA e segregação de funções.

## Critério de conclusão

Não considerar um módulo completo só porque possui CRUD ou uma tela.

Para os processos críticos, o fluxo deve fechar:

documento origem -> aprovação -> execução -> documento destino -> estoque -> financeiro -> contábil -> fiscal -> auditoria.

O projeto já tem amplitude muito maior que um ERP básico, mas ainda não é tecnicamente correto declarar equivalência funcional integral com SAP S/4HANA. O próximo salto é fechar esses fluxos, não apenas adicionar mais CRUDs.
