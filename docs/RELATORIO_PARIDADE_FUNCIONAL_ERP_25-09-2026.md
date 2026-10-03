# Relatório de Paridade Funcional — Brasil SaaS ERP

**Data:** 25/09/2026  
**Escopo:** branch `main`, backend Spring Boot, React 19/PrimeReact/Vite, migrations Flyway e integrações/serviços presentes no repositório.  
**Objetivo:** medir o que já está implementado e o que ainda precisa existir para que o Brasil SaaS ERP tenha a **amplitude e profundidade funcional esperada de uma suíte ERP empresarial de grande porte**, sem limitar a análise a quantidade de telas.

> **Importante:** este relatório substitui avaliações anteriores que consideravam apenas “módulo existente”. A regra adotada agora é: uma capacidade só é considerada madura quando possui, conforme aplicável, **entrada, consulta, tratamento, administração, análise, segurança, integração e interface operacional**.

---

## 1. Resumo executivo

O BRASIL-SAAS já possui um núcleo ERP relevante e integrado:

- cadastro mestre;
- vendas;
- compras;
- estoque;
- WMS;
- financeiro;
- fiscal;
- produção;
- serviços;
- RH;
- BI;
- IA;
- autenticação/RBAC/auditoria;
- multiempresa/tenant;
- migrations versionadas;
- integrações e infraestrutura de aplicação.

O principal gap deixou de ser “falta de módulos”. O gap é **profundidade operacional e cobertura 1:1 entre backend e frontend**.

### Situação qualitativa

| Domínio | Estado | Principal lacuna |
|---|---|---|
| Core / segurança | 🟢 Bom | administração e governança mais profundas |
| Cadastro | 🟢 Bom | dados mestres avançados, versionamento e governança |
| Vendas | 🟡 Parcial-avançado | devoluções/contratos/CRM/fiscal/entrega end-to-end |
| Compras | 🟡 Avançado | aprovação, fornecedores, contratos e 3-way match completo |
| Estoque | 🟢 Avançado | custeio, serialização avançada e integração fiscal/produção |
| WMS | 🟢 Avançado | otimização, ondas, inventário avançado e regras de armazenagem |
| Financeiro | 🟡 Avançado | contabilidade completa, caixa projetado, ativos, cobrança e integrações bancárias |
| Fiscal | 🟡 Parcial | emissão, eventos fiscais, documentos eletrônicos e obrigações |
| Produção | 🟡 Parcial-avançado | BOM multinível, roteiros, MRP, chão de fábrica e custeio |
| Qualidade | 🔴 Gap grande | inspeção, não conformidade e CAPA |
| RH/Folha | 🔴 Gap grande | folha completa, ponto, férias, benefícios e obrigações |
| BI | 🟡 Parcial | camada analítica completa, exploração e governança |
| IA | 🟡 Parcial | transformar APIs avançadas em workspace operacional |
| Documentos | 🔴 Gap | DMS genérico e ciclo documental |
| Workflow/Approvals | 🔴 Gap | motor transversal de aprovação |
| Integrações | 🟡 Parcial | APIs/eventos/conectores operacionais |
| Governança | 🟡 Parcial | auditoria, parametrização, jobs e administração central |

---

# 2. Critério de paridade adotado

Para cada processo, a cobertura desejada é:

1. **Entrada** — criar/importar/lançar.
2. **Consulta** — listar, pesquisar, filtrar, detalhar e histórico.
3. **Tratamento** — aprovar, confirmar, receber, faturar, baixar, cancelar, reprocessar etc.
4. **Administração** — parâmetros, permissões, cadastros auxiliares e manutenção.
5. **Análise** — indicadores, divergências, aging, margem, produtividade, tendências.
6. **Documentação** — anexos, documentos gerados, comprovantes e histórico.
7. **Segurança** — tenant, permissões, segregação de funções e auditoria.
8. **Integração** — alimentar o processo seguinte sem redigitação.
9. **Automação** — jobs, eventos, notificações e tarefas pendentes.
10. **Frontend** — todas as capacidades relevantes devem ser executáveis pelo usuário sem depender diretamente de API/SQL.

---

# 3. Core, empresas, usuários e segurança

## Já feito

- autenticação ERP;
- JWT;
- refresh token separado;
- BCrypt;
- usuários;
- perfis;
- permissões;
- SUPERUSER PostgreSQL com provisionamento especial;
- multiempresa/tenant;
- auditoria;
- recentes;
- estrutura empresarial;
- base de municípios/CEP;
- configuração central;
- controle de acesso por autoridade.

## Ainda falta

### Governança organizacional
- matriz completa empresa → filial → estabelecimento;
- centros de responsabilidade;
- unidades organizacionais;
- calendários empresariais;
- períodos fiscais/contábeis;
- fechamento e reabertura de períodos;
- bloqueio transacional por período fechado.

### Segurança operacional
- segregação de funções;
- aprovação em dois níveis;
- limites financeiros por usuário/perfil;
- limites de desconto;
- limites de compras;
- trilha de aprovação;
- impersonação administrativa auditada;
- gestão de sessões;
- revogação de sessões;
- política de senha configurável;
- MFA/2FA;
- recuperação segura de conta.

### Administração
- tela completa de parâmetros globais;
- parâmetros por empresa;
- parâmetros por filial;
- parâmetros por módulo;
- parâmetros por usuário/perfil;
- feature flags;
- jobs;
- filas;
- erros de integração;
- reprocessamento.

**Prioridade: ALTA.**

---

# 4. Cadastro mestre

## Já feito

- pessoas;
- clientes;
- fornecedores;
- produtos;
- categorias;
- marcas;
- serviços;
- transportadoras;
- unidades;
- municípios;
- cadastros auxiliares;
- imagens;
- estrutura de produtos.

## Ainda falta para nível empresarial

- múltiplos endereços por finalidade;
- contatos;
- documentos;
- contas bancárias;
- histórico de alterações;
- classificação comercial;
- segmentos;
- grupos econômicos;
- relacionamentos entre empresas;
- cadastro de concorrentes;
- classificação ABC;
- atributos customizáveis;
- campos personalizados;
- vigência de dados;
- aprovação de cadastro;
- bloqueio/desbloqueio;
- score de cliente/fornecedor;
- limites de crédito;
- análise cadastral;
- duplicidade de cadastros;
- merge de registros;
- importação/exportação assistida;
- anexos genéricos.

**Prioridade: ALTA.**

---

# 5. Vendas / Comercial

## Já feito

Fluxo principal:

`ORÇAMENTO → CONFIRMAÇÃO → PEDIDO → RESERVA → FATURAMENTO → ESTOQUE → FINANCEIRO`

Também existem:

- tabela de preços;
- itens de tabela;
- comissão;
- devolução;
- bonificação;
- contratos;
- recorrência;
- CRM;
- oportunidades;
- atividades.

A migration comercial já prevê devolução/troca/bonificação, contratos e CRM.

## Gap de frontend

É necessário garantir telas operacionais para **todas** as capacidades que já existem no banco/backend:

- devolução;
- troca;
- bonificação;
- contratos;
- itens de contrato;
- recorrência;
- oportunidades;
- pipeline;
- atividades;
- comissão;
- regras de comissão;
- tabela de preços;
- manutenção dos itens;
- aprovação comercial.

## Gap de processo

Ainda falta fechar:

`Pedido → Expedição → Documento Fiscal → Entrega → Financeiro → Pós-venda`

### Necessário

- política de desconto;
- aprovação comercial;
- limite de crédito;
- bloqueio por inadimplência;
- reserva por pedido;
- backorder;
- entrega parcial;
- faturamento parcial;
- múltiplas entregas;
- comissão por recebimento quando aplicável;
- cálculo de margem;
- custo da venda;
- devolução com impacto financeiro;
- troca com nova movimentação;
- crédito ao cliente;
- cancelamento financeiro/fiscal;
- carteira comercial;
- metas;
- forecast;
- pipeline;
- CRM integrado ao pedido.

**Prioridade: CRÍTICA.**

---

# 6. Compras e suprimentos

## Já feito

`Solicitação → Cotação → Comparação → Pedido → Recebimento → Estoque → Financeiro`

Também existem:

- recebimento parcial;
- histórico de recebimento;
- conferência de fatura;
- vínculo com NFe;
- solicitação;
- cotação;
- fornecedores.

## Ainda falta

### Compras

- aprovação multinível;
- alçadas;
- mapa comparativo completo;
- equalização comercial;
- homologação de fornecedor;
- avaliação de fornecedor;
- lead time;
- contratos de fornecimento;
- pedidos recorrentes;
- programação de entrega;
- previsão de compras;
- compras emergenciais;
- compras por MRP;
- compras por estoque mínimo;
- compras por demanda de venda.

### 3-way match

O processo deve comparar:

`Pedido × Recebimento × Documento/Fatura`

com:

- quantidade;
- preço;
- impostos;
- frete;
- desconto;
- tolerância;
- divergência;
- aprovação;
- bloqueio de pagamento.

**Prioridade: CRÍTICA.**

---

# 7. Estoque e WMS

## Já feito

O módulo está entre os mais avançados:

- depósitos;
- saldos;
- movimentações;
- lotes;
- validade;
- reservas;
- endereços;
- ocupação;
- picking;
- packing;
- expedição;
- transferências;
- transferência interna;
- inventário;
- contagem;
- ajustes;
- rastreabilidade;
- reserva por lote/endereço.

## Ainda falta

### WMS avançado

- estrutura Armazém → Rua → Módulo → Nível → Posição;
- capacidade por endereço;
- regras de armazenagem;
- put-away;
- cross-docking;
- ondas de picking;
- agrupamento de pedidos;
- priorização;
- FIFO/FEFO configurável;
- reposição automática;
- picking por rota;
- conferência por código de barras;
- coletores;
- impressão de etiquetas;
- embalagem;
- volumes;
- cubagem;
- transportadora;
- rastreamento.

### Estoque

- número de série completo;
- garantia por série;
- quarentena;
- bloqueio;
- estoque consignado;
- estoque de terceiros;
- custo médio;
- custo padrão;
- custo por lote;
- custo por depósito;
- custo por movimentação;
- valorização;
- inventário rotativo;
- curva ABC;
- giro;
- cobertura;
- ruptura;
- estoque máximo/mínimo;
- ponto de pedido.

**Prioridade: ALTA.**

---

# 8. Financeiro

## Já feito

- títulos;
- parcelas;
- contas a receber;
- contas a pagar;
- baixa;
- desconto;
- juros;
- multa;
- conta bancária;
- extrato;
- movimentação;
- conciliação bancária;
- tipos de pagamento;
- condições de pagamento;
- plano de contas;
- lançamentos;
- centros de custo;
- comissões;
- integração com vendas/compras.

A cadeia atual está em:

`Título → Parcela → Baixa → Conta → Extrato → Conciliação`

## Ainda falta

### Contas a receber

- cobrança;
- inadimplência;
- aging;
- régua de cobrança;
- renegociação;
- parcelamento;
- acordos;
- juros parametrizados;
- multa parametrizada;
- protesto;
- negativação;
- antecipação;
- crédito do cliente.

### Contas a pagar

- aprovação;
- programação;
- lote de pagamentos;
- pagamentos recorrentes;
- adiantamentos;
- retenções;
- rateios;
- autorização bancária.

### Bancário

- OFX;
- CNAB;
- remessa;
- retorno;
- boletos;
- PIX;
- arquivos bancários;
- múltiplas contas;
- importação automática;
- conciliação automática por regras.

### Tesouraria

- fluxo de caixa realizado;
- fluxo projetado;
- cenários;
- previsão;
- compromissos;
- liquidez;
- necessidade de capital de giro.

### Contabilidade

- partidas dobradas;
- diário;
- razão;
- balancete;
- DRE;
- balanço patrimonial;
- fechamento contábil;
- períodos;
- lançamentos automáticos;
- integração fiscal;
- integração estoque;
- integração folha;
- integração ativo.

### Ativo imobilizado

- cadastro;
- aquisição;
- incorporação;
- depreciação;
- baixa;
- transferência;
- inventário;
- histórico;
- fiscal × contábil.

**Prioridade: CRÍTICA.**

---

# 9. Fiscal

## Já feito / existente no backend

- NCM;
- CFOP;
- CEST;
- ISSQN;
- impostos;
- entrada fiscal;
- SEFAZ;
- estruturas para NF-e/NFC-e/NFS-e;
- estruturas para CT-e;
- estruturas para ECD;
- certificado digital;
- tabelas fiscais.

## Ainda falta consolidar em processo operacional

### Saída

- emissão de NF-e;
- emissão de NFC-e;
- emissão de NFS-e;
- cálculo tributário;
- geração XML;
- assinatura;
- envio;
- autorização;
- DANFE;
- armazenamento;
- consulta;
- cancelamento;
- carta de correção;
- inutilização;
- contingência;
- eventos.

### Entrada

`XML → manifestação → conferência → fornecedor → estoque → financeiro → fiscal`

### Obrigações

- SPED Fiscal;
- SPED Contribuições;
- ECD;
- ECF;
- REINF;
- obrigações municipais;
- apurações;
- guias;
- fechamento fiscal.

### Reforma tributária

- estrutura de regras preparada para novos tributos;
- parametrização de IBS/CBS;
- vigência;
- cenários;
- histórico;
- simulação.

**Prioridade: CRÍTICA.**

---

# 10. Produção / PCP

## Já feito

- ordem de produção;
- itens;
- apontamentos;
- romaneio;
- estrutura/BOM;
- finalização;
- integração com estoque.

## Ainda falta

### Engenharia

- BOM multinível;
- versões;
- vigência;
- substitutos;
- coprodutos;
- subprodutos;
- perdas;
- unidades alternativas;
- engenharia de alteração.

### Roteiros

- operações;
- sequência;
- centros de trabalho;
- máquinas;
- mão de obra;
- setup;
- tempo máquina;
- tempo homem;
- capacidade.

### MRP

`Demanda → Explosão BOM → Estoque → Reservas → Compras → Produção`

Necessário:

- MRP;
- MPS;
- necessidades líquidas;
- lead time;
- lote mínimo;
- lote múltiplo;
- estoque de segurança;
- calendário;
- ordens sugeridas.

### Chão de fábrica

- apontamento por operação;
- operador;
- máquina;
- início/fim;
- produção boa;
- refugo;
- parada;
- motivo;
- eficiência;
- OEE.

### Custeio

- material;
- mão de obra;
- máquina;
- overhead;
- custo padrão;
- custo real;
- variação;
- custo do produto acabado.

**Prioridade: CRÍTICA.**

---

# 11. Qualidade

## Estado

Existe pouca ou nenhuma cobertura operacional comparável aos demais módulos.

## Necessário

- plano de inspeção;
- características;
- amostragem;
- inspeção de recebimento;
- inspeção de processo;
- inspeção final;
- aprovação/reprovação;
- quarentena;
- não conformidade;
- causa;
- ação corretiva;
- ação preventiva;
- CAPA;
- fornecedor;
- lote;
- rastreabilidade;
- indicadores de qualidade;
- auditorias.

**Prioridade: ALTA.**

---

# 12. RH e folha

## Já feito

A estrutura de banco contempla:

- cargos;
- funcionários;
- folha;
- itens da folha;
- vínculo com pessoa;
- vínculo da folha com financeiro.

## Ainda falta

### RH

- admissão;
- desligamento;
- férias;
- afastamentos;
- benefícios;
- dependentes;
- documentos;
- treinamentos;
- avaliações;
- histórico salarial;
- movimentação;
- organograma.

### Ponto

- jornada;
- escalas;
- banco de horas;
- marcações;
- ajustes;
- horas extras;
- adicionais;
- integração relógio.

### Folha

- proventos;
- descontos;
- INSS;
- IRRF;
- FGTS;
- férias;
- 13º;
- rescisão;
- pró-labore;
- encargos;
- contabilização;
- integração financeira;
- fechamento.

### Obrigações

- eSocial;
- eventos;
- lotes;
- retorno;
- rejeição;
- reprocessamento.

**Prioridade: CRÍTICA.**

---

# 13. BI e Analytics

## Já feito

- dashboards;
- indicadores;
- KPIs;
- relatórios;
- relatórios agendados;
- geração PDF;
- Excel;
- CSV;
- estrutura de Data Lake;
- tabelas analíticas previstas;
- jobs agendados.

## Gap

O backend possui mais superfície do que o frontend expõe.

Necessário:

- dashboard executivo;
- dashboard financeiro;
- dashboard comercial;
- dashboard estoque;
- dashboard compras;
- dashboard produção;
- dashboard fiscal;
- dashboard RH;
- exploração multidimensional;
- filtros;
- drill-down;
- drill-through;
- comparação temporal;
- metas;
- orçamento × realizado;
- margem;
- cohort;
- ABC;
- Pareto;
- aging;
- forecast;
- exportação;
- relatórios ad hoc.

### Arquitetura

O Data Lake/warehouse precisa sair de estrutura apenas declarada e possuir:

- ETL/ELT;
- carga incremental;
- controle de watermark;
- reprocessamento;
- qualidade de dados;
- reconciliação OLTP × analítico;
- dimensões históricas;
- fatos;
- agregações;
- governança.

**Prioridade: ALTA.**

---

# 14. IA

## Já feito

O backend possui uma superfície considerável:

- chat;
- sessões;
- mensagens;
- configuração;
- tokens;
- teste de conexão;
- classificação;
- análises;
- previsão de vendas;
- previsão de estoque;
- previsão financeira;
- embeddings;
- prompts;
- templates.

## Gap

O frontend ainda não representa toda essa capacidade.

Necessário:

- central de configuração;
- consumo de tokens;
- teste;
- histórico;
- prompts;
- templates;
- classificação;
- análises;
- análises pendentes;
- análises de alta confiança;
- previsões;
- histórico das previsões;
- embeddings;
- busca semântica;
- reindexação;
- governança de modelos;
- auditoria das respostas;
- explicabilidade;
- aprovação humana para ações críticas.

**Prioridade: ALTA.**

---

# 15. Gestão documental

## Estado atual

Há armazenamento de imagens e infraestrutura MongoDB.

## Necessário

Um DMS transversal:

- upload;
- download;
- preview;
- versionamento;
- hash;
- metadados;
- tags;
- validade;
- entidade vinculada;
- permissões;
- histórico;
- assinatura;
- aprovação;
- retenção;
- pesquisa;
- OCR;
- documentos fiscais;
- contratos;
- comprovantes;
- documentos RH;
- documentos fornecedores/clientes.

**Prioridade: ALTA.**

---

# 16. Workflow e aprovações

Este é um gap transversal importante.

## Necessário

Motor genérico de workflow:

`Evento → Regra → Alçada → Aprovação → Ação → Auditoria`

Casos:

- compra;
- desconto;
- crédito;
- pagamento;
- cadastro;
- devolução;
- inventário;
- ajuste de estoque;
- produção;
- contratos;
- documentos;
- fiscal;
- financeiro.

Precisa suportar:

- níveis;
- substitutos;
- SLA;
- escalonamento;
- aprovação/rejeição;
- comentários;
- anexos;
- histórico;
- notificações.

**Prioridade: CRÍTICA.**

---

# 17. Integrações

## Já existe

- REST;
- Spring Boot;
- PostgreSQL;
- MongoDB;
- Redis;
- RabbitMQ configurado;
- Spring AI;
- serviços fiscais;
- estrutura de microserviços;
- eSocial.

## Gap

RabbitMQ está configurado, mas a auditoria do código não encontrou consumidores `@RabbitListener` ativos.

Portanto ainda falta transformar mensageria em arquitetura operacional real:

- eventos de domínio;
- outbox;
- retry;
- dead-letter;
- idempotência;
- observabilidade;
- reprocessamento;
- contratos de eventos.

Também faltam conectores operacionais para:

- bancos;
- pagamentos;
- transportadoras;
- marketplaces;
- e-commerce;
- mensageria;
- assinatura digital;
- documentos fiscais;
- relógios de ponto;
- parceiros.

**Prioridade: ALTA.**

---

# 18. Portais

Necessário evoluir para interfaces externas:

### Cliente
- pedidos;
- boletos;
- NF;
- contratos;
- chamados;
- devoluções;
- segunda via;
- pagamentos.

### Fornecedor
- cotações;
- pedidos;
- confirmação;
- previsão de entrega;
- documentos;
- notas;
- divergências.

### Funcionário
- holerite;
- férias;
- ponto;
- documentos;
- benefícios.

### Transportadora
- coleta;
- entrega;
- ocorrências;
- comprovante.

**Prioridade: MÉDIA/ALTA.**

---

# 19. Relatórios operacionais

O sistema precisa ter relatórios por processo, não somente dashboards.

## Vendas

- pedidos;
- faturamento;
- margem;
- comissão;
- clientes;
- produtos;
- vendedores;
- inadimplência.

## Compras

- pedidos;
- fornecedores;
- preço;
- prazo;
- recebimento;
- divergências.

## Estoque

- posição;
- giro;
- cobertura;
- inventário;
- lotes;
- validade;
- movimentação;
- rastreabilidade.

## Financeiro

- títulos;
- aging;
- fluxo de caixa;
- DRE;
- contas;
- conciliação.

## Fiscal

- documentos;
- impostos;
- apuração;
- obrigações.

## Produção

- OP;
- consumo;
- produção;
- refugo;
- eficiência;
- custo.

**Prioridade: ALTA.**

---

# 20. Auditoria e observabilidade

## Já existe

- auditoria;
- logs;
- recentes;
- infraestrutura de observabilidade prevista.

## Ainda falta

- timeline por documento;
- timeline por cliente;
- timeline por pedido;
- timeline por título;
- timeline por estoque;
- diff antes/depois;
- usuário;
- IP;
- origem;
- API;
- motivo;
- aprovação;
- correlação;
- trace;
- auditoria de integrações;
- auditoria de jobs.

---

# 21. Fechamentos

Uma suíte ERP empresarial precisa de fechamento formal.

## Necessário

### Estoque
- fechamento de competência;
- bloqueio de movimentos retroativos.

### Fiscal
- fechamento fiscal;
- apuração;
- reabertura controlada.

### Financeiro
- fechamento financeiro;
- conciliação;
- posição.

### Contábil
- fechamento;
- abertura;
- períodos bloqueados.

### Folha
- fechamento de competência.

### Produção
- fechamento de custo.

**Prioridade: CRÍTICA.**

---

# 22. O que NÃO deve ser considerado “feito” apenas por existir no banco

A auditoria encontrou estruturas de backend/migration que não devem automaticamente ser classificadas como funcionalidade entregue.

Exemplos:

- tabela sem tela operacional;
- controller sem service React;
- service React sem rota;
- rota sem menu;
- endpoint administrativo sem UI;
- entidade fiscal sem processo fiscal completo;
- tabela de CRM sem pipeline;
- estrutura de folha sem cálculo completo;
- RabbitMQ configurado sem fluxo de eventos;
- Data Lake sem ETL completo;
- estrutura de NF sem emissão/eventos completos.

O relatório, portanto, diferencia **estrutura existente** de **capacidade operacional entregue**.

---

# 23. Priorização para atingir paridade empresarial

## Fase 1 — Processos críticos

1. Workflow/aprovações.
2. Fiscal operacional completo.
3. Financeiro/contabilidade.
4. Produção/MRP/custeio.
5. RH/folha.
6. Vendas end-to-end.
7. Compras/3-way match.

## Fase 2 — Profundidade operacional

8. WMS avançado.
9. Qualidade.
10. Ativo imobilizado.
11. Tesouraria.
12. Cobrança.
13. CRM.
14. Contratos.
15. Portais.

## Fase 3 — Inteligência

16. Data Lake/warehouse.
17. BI multidimensional.
18. Forecast.
19. IA operacional.
20. automações.

## Fase 4 — Governança

21. DMS.
22. auditoria avançada.
23. integração/eventos.
24. administração central.
25. observabilidade.
26. alta disponibilidade.
27. disaster recovery.
28. performance e escala.

---

# 24. Migrations

As migrations existentes devem ser preservadas.

O histórico atual já contém evolução relevante até **V85**, incluindo:

- comercial;
- WMS;
- reservas;
- lotes;
- endereços;
- expedição;
- recebimento;
- supply chain;
- conferência;
- BOM;
- inventário;
- financeiro;
- índices;
- conciliação.

Novas capacidades devem entrar como novas migrations:

`V86, V87, V88...`

sem reescrever V1–V85.

---

# 25. Conclusão

O BRASIL-SAAS **já não é um CRUD genérico**. Ele possui uma base ERP integrada com processos reais de vendas, compras, estoque/WMS, financeiro, fiscal, produção, RH, BI e IA.

O principal trabalho restante é transformar essa base em uma **suíte ERP empresarial de profundidade completa**.

Os maiores gaps atuais são:

1. **workflow e aprovação transversal**;
2. **fiscal end-to-end**;
3. **contabilidade/tesouraria/ativos**;
4. **MRP, chão de fábrica e custeio**;
5. **folha/ponto/eSocial**;
6. **qualidade**;
7. **frontend completo para APIs já existentes**;
8. **BI/Data Lake operacional**;
9. **IA operacional completa**;
10. **DMS e integrações/eventos**.

### Regra de conclusão

Um módulo só será marcado como **concluído** quando:

`Backend + Banco + Regra de negócio + Frontend + Rota + Menu + Permissão + Auditoria + Integração + Relatórios`

estiverem coerentes.

Isso evita que o projeto pareça completo visualmente enquanto ainda existam operações importantes acessíveis somente por API, SQL ou código.

---

## Próxima matriz de execução

A próxima auditoria deve transformar este relatório em uma matriz executável:

`Domínio | Capacidade | Migration | Entity | Repository | Service | Controller | React Service | React Component | Route | Menu | Permission | Teste | Status`

Essa matriz será a referência para as próximas implementações e permitirá identificar exatamente **qual arquivo ainda falta em cada funcionalidade**, em vez de trabalhar por impressão visual.
