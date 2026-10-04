> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

# Plano de paridade funcional empresarial — Brasil SaaS ERP

Data: 2026-10-03

## Objetivo

Levar o Brasil SaaS ERP a uma cobertura funcional empresarial comparável à família SAP S/4HANA/Cloud ERP nas áreas de ERP tradicional, **excluindo IA como requisito de paridade**.

A comparação usa capacidades públicas documentadas pela SAP em Finanças, Manufacturing/Production, Quality, Asset Management e demais processos centrais.

## Regra de conclusão

Um processo só é considerado equivalente quando possui, conforme aplicável:

1. cadastro/dados mestre;
2. entrada;
3. execução;
4. consulta;
5. tratamento de exceções;
6. aprovação/alçada;
7. integração com módulos dependentes;
8. auditoria;
9. indicadores/relatórios;
10. tela operacional.

Uma tela isolada não conta como cobertura funcional.

## Escopo sem IA

### 1. Finanças e contabilidade
- contas a pagar/receber;
- cobrança;
- crédito;
- pagamentos;
- bancos;
- conciliação;
- caixa;
- liquidez;
- tesouraria;
- orçamento;
- forecast;
- DRE;
- balanço;
- balancete;
- diário/razão;
- fechamento;
- múltiplas visões/ledgers quando necessárias;
- impostos e compliance.

### 2. Vendas
- CRM;
- oportunidade;
- cotação;
- pedido;
- aprovação;
- crédito;
- preço;
- desconto;
- comissão;
- disponibilidade;
- reserva;
- expedição;
- entrega;
- faturamento;
- devolução;
- troca;
- bonificação;
- contratos;
- recorrência;
- pós-venda.

### 3. Compras
- solicitação;
- cotação;
- mapa comparativo;
- fornecedor;
- homologação;
- avaliação;
- pedido;
- contratos;
- recebimento;
- conferência;
- 3-way match;
- aprovação;
- programação.

### 4. Estoque/WMS
- multiempresa;
- multidépósito;
- endereçamento;
- lote;
- validade;
- serial;
- reserva;
- picking;
- packing;
- ondas;
- put-away;
- transferência;
- inventário;
- FEFO/FIFO;
- cross-docking;
- reposição;
- rastreabilidade.

### 5. Produção/PCP
- engenharia;
- BOM multinível;
- versões;
- substitutos;
- roteiros;
- centros de trabalho;
- capacidade;
- demanda;
- MPS;
- MRP;
- sugestões de compra;
- sugestões de produção;
- OP;
- chão de fábrica;
- apontamento;
- tempos;
- paradas;
- refugo;
- retrabalho;
- Kanban;
- produção repetitiva;
- terceirização;
- custeio.

### 6. Qualidade
- planos;
- características;
- inspeção de recebimento;
- inspeção de processo;
- inspeção final;
- resultados;
- aprovação/reprovação;
- quarentena;
- não conformidade;
- causa raiz;
- ação corretiva;
- ação preventiva;
- CAPA/8D;
- indicadores.

### 7. Ativos/EAM
- cadastro;
- classe;
- aquisição;
- capitalização;
- localização;
- responsável;
- depreciação;
- transferência;
- reavaliação;
- inventário;
- baixa;
- manutenção preventiva;
- corretiva;
- ordens;
- peças;
- horas;
- custos;
- disponibilidade;
- histórico.

### 8. Fiscal Brasil
- regras tributárias;
- NCM/CFOP/CEST;
- ICMS/IPI/PIS/COFINS/ISS e demais tributos aplicáveis;
- NF-e;
- NFC-e;
- NFS-e;
- CT-e;
- MDF-e;
- eventos;
- cancelamento;
- CC-e;
- inutilização;
- contingência;
- SEFAZ/prefeituras;
- apurações;
- SPED;
- obrigações;
- reforma tributária.

### 9. RH
- colaborador;
- admissão;
- contrato;
- jornada;
- ponto;
- férias;
- afastamento;
- benefícios;
- folha;
- encargos;
- rescisão;
- eSocial;
- autosserviço.

### 10. Serviços
- catálogo;
- contrato;
- SLA;
- agenda;
- técnico;
- ordem;
- apontamento;
- peças;
- custos;
- faturamento;
- indicadores.

### 11. Projetos
- projeto;
- WBS;
- planejamento;
- orçamento;
- custos;
- receitas;
- materiais;
- horas;
- progresso;
- risco;
- mudança;
- compras;
- contrato;
- faturamento.

### 12. Administração empresarial
- multiempresa;
- filiais;
- centros de custo;
- períodos;
- parâmetros;
- usuários;
- papéis;
- segregação de funções;
- workflow;
- alçadas;
- auditoria;
- documentos;
- notificações;
- integrações;
- jobs;
- monitoramento.

### 13. BI/Analytics sem IA
- KPIs;
- dashboards;
- relatórios;
- drill-down;
- filtros;
- exportação;
- relatórios agendados;
- fatos/dimensões;
- ETL;
- margem;
- rentabilidade;
- ABC;
- análises por empresa, filial, produto, cliente, fornecedor e centro de custo.

## Implementação iniciada

### Qualidade
Backend:
- `/api/qualidade/planos`
- `/api/qualidade/inspecoes`
- `/api/qualidade/nao-conformidades`
- encerramento de NC.

Frontend:
- planos de inspeção;
- inspeções;
- não conformidades;
- encerramento operacional.

### Ativos e manutenção
Backend:
- `/api/ativos`
- `/api/ativos/manutencoes`
- baixa de ativo;
- conclusão de manutenção.

Frontend:
- ativos;
- valores de aquisição/depreciação;
- baixa;
- ordens de manutenção;
- conclusão.

### PCP/MRP
Backend:
- explosão recursiva de BOM;
- detecção de ciclo;
- necessidade bruta;
- estoque disponível;
- necessidade líquida;
- sugestão de ação.

Endpoint:
`POST /api/producao/mrp/simular`

Frontend:
- tela MRP;
- quantidade planejada;
- resultado por componente;
- necessidade bruta;
- estoque;
- necessidade líquida;
- ação.

### Financeiro
Já exposto anteriormente:
- DRE gerencial;
- fluxo de caixa projetado.

## Próximas implementações

A paridade continuará sendo fechada nesta ordem técnica:

1. PCP completo: MPS, capacidade, roteiros, chão de fábrica e custeio.
2. Fiscal end-to-end.
3. Compras: alçadas, fornecedor, contratos e 3-way match.
4. Comercial end-to-end.
5. WMS avançado.
6. Contabilidade/fechamento/tesouraria.
7. RH operacional.
8. Serviços/SLA.
9. Projetos.
10. Workflow transversal.
11. DMS.
12. BI/DW.

## Referências oficiais

- SAP Cloud ERP: https://www.sap.com/brazil/products/erp/s4hana.html
- SAP Cloud ERP Finance: https://www.sap.com/brazil/products/erp/s4hana/features/finance.html
- SAP Cloud ERP Manufacturing: https://www.sap.com/products/erp/s4hana/features/manufacturing.html
- SAP Enterprise Asset Management: https://www.sap.com/products/supply-chain-management/asset-management-eam.html

## Observação

"Equivalente" aqui significa **cobertura funcional de ERP empresarial**, não cópia de interface, código, arquitetura proprietária ou implementação interna de SAP. Recursos específicos de IA ficam fora do critério de paridade por decisão do projeto.
