# Roadmap — Brasil SaaS ERP

> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

## Status em 04/10/2026

O sistema tem **22 módulos de negócio** implementados, com **762 classes Java**,
**451 endpoints REST**, **204 tabelas** no PostgreSQL e **123 componentes React**.
Smoke test de 67 rotas: **62 respondem 200**, 5 com 400 (parâmetro obrigatório),
0 erro 500.

### Concluído

| Módulo | Status |
|--------|--------|
| Cadastro (pessoas, clientes, fornecedores, produtos, serviços) | ✅ |
| Financeiro (títulos, baixas, orçamento, empréstimos, conciliação) | ✅ |
| Estoque (depósitos, saldos, movimentações, inventários, expedições) | ✅ |
| Vendas (pedidos, tabelas de preço, PDV) | ✅ |
| Compras (pedidos, recebimentos, supply chain) | ✅ |
| Produção (ordens, roteiros, centros de trabalho, MRP) | ✅ |
| Contabilidade (lançamentos, DRE, balancete) | ✅ |
| RH (funcionários, cargos, folha) | ✅ |
| CRM (leads, pipeline, forecast) | ✅ |
| BI (dashboards, KPIs, relatórios) | ✅ |
| IA (chat, embeddings, classificações, análises) | ✅ (precisa de chave API) |
| DMS (documentos, versões, aprovações) | ✅ |
| Qualidade (planos, inspeções, não-conformidades) | ✅ |
| Projetos (etapas, faturamento, riscos) | ✅ |
| WMS (ondas, volumes, putaway) | ✅ |
| Workflow (definições, instâncias, tarefas) | ✅ |
| Portais (acessos, validação pública) | ✅ |
| Ativos (imobilizado, manutenções) | ✅ |
| Relatórios (PDF, HTML) | ✅ |
| Core/Auth (JWT, AD, multiempresa, superadmin) | ✅ |

### Fiscal

| Documento | Status |
|-----------|--------|
| NFS-e São Paulo | ✅ 100% (emissão via API 4568/4569) |
| SPED EFD ICMS/IPI | ⚠️ 40% (gera arquivo válido) |
| MDF-e | ⚠️ 25% (status apenas; emissão não implementada) |
| CT-e | ⚠️ 17% (status apenas; emissão não implementada) |
| NF-e / NFC-e | ❌ 0% (NFeServiceImpl tem 3 TODOs) |
| SPED EFD Contribuições | ⚠️ 20% (biblioteca apenas, sem endpoint) |
| DistDFe / consulta | ✅ 100% |

### Pendências

| Item | Prioridade |
|------|-----------|
| NF-e / NFC-e | Alta |
| Emissão MDF-e / CT-e | Alta |
| SPED EFD Contribuições | Média |
| Proxy failover 4567 (caído) | Alta |
| Tela da IA (só 1 tela para 74 endpoints) | Baixa |

---

## Fases concluídas (histórico)

[x] Fase 0 — Preparação (dump SYSFLUXO, dados oficiais NCM/IBGE/ISSQN)
[x] Fase 1 — pom.xml + Java 21 (Boot 3.3.5)
[x] Fase 2 — Estrutura de pastas
[x] Fase 3 — application.yml + perfis (dev/hom/prod) + logback + mTLS
[x] Fase 4 — Migrations Flyway
[x] Fase 5 — Módulo shared (BaseEntity, TenantEntity, ApiResponse, exceções)
[x] Fase 6 — Módulo core (Empresa, Usuario, Perfil, Permissao, Auth JWT)
[x] Fase 7 — Módulo cadastro (Pessoa, Cliente, Fornecedor, Produto, Serviço...)
[x] Fase 8 — Vendas + Ordem de Serviço
[x] Fase 9 — Compras + Estoque
[x] Fase 10 — Financeiro (títulos, contas a pagar/receber, fluxo de caixa)
[x] Fase 11 — RH (funcionários, folha)
[x] Fase 12 — Fiscal (NF-e, NFCe, NFSe, CTe, apurações)
[x] Fase 13 — IA (assistente, classificação NCM, embeddings)
[x] Fase 14 — Frontend React + TypeScript
[x] Fase 15 — CRM, BI, Contabilidade, Ativos, DMS, Qualidade, Projetos, WMS, Workflow, Portais
[x] Fase 16 — Multiempresa, superadmin, auth AD/SPNEGO

## Regra de ouro
> Fiscal é o último módulo antes da IA. Ele só é implementado depois que
> vendas, OS, compras, estoque e financeiro estiverem funcionando, porque
> o documento fiscal nasce desses processos — nunca antes.
