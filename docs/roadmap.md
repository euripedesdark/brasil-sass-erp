# Roadmap — Brasil SaaS ERP

## Fases concluídas
[x] Fase 0 — Preparação (dump SYSFLUXO, dados oficiais NCM/IBGE/ISSQN)
[x] Fase 1 — pom.xml + Java 21 (Boot 3.3.5)
[x] Fase 2 — Estrutura de pastas
[x] Fase 3 — application.yml + perfis (dev/hom/prod) + logback + mTLS
[x] Fase 4 — Migrations Flyway V1–V17
[x] Fase 5 — Módulo shared (BaseEntity, TenantEntity, ApiResponse, exceções)
[x] Fase 6 — Módulo core (Empresa, Usuario, Perfil, Permissao, Auth JWT)
[x] Fase 7 — Módulo cadastro (Pessoa, Cliente, Fornecedor, Produto, Serviço...)

## Próximas fases (ordem correta de dependência)
[ ] Fase 8  — Vendas + Ordem de Serviço
[ ] Fase 9  — Compras + Estoque
[ ] Fase 10 — Financeiro (títulos, contas a pagar/receber, fluxo de caixa)
[ ] Fase 11 — RH (funcionários, folha)
[ ] Fase 12 — Fiscal (NF-e, NFCe, NFSe, CTe, apurações) — ÚLTIMO operacional
[ ] Fase 13 — IA (assistente, classificação NCM, embeddings)
[ ] Fase 14 — Frontend React + TypeScript

## Regra de ouro
> Fiscal é o último módulo antes da IA. Ele só é implementado depois que
> vendas, OS, compras, estoque e financeiro estiverem funcionando, porque
> o documento fiscal nasce desses processos — nunca antes.
