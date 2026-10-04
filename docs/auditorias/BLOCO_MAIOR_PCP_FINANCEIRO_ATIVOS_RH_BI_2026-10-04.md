# Bloco ERP — PCP, Financeiro, Ativos, RH, Projetos e BI — 2026-10-04

## Objetivo

Grande lote funcional fora do Fiscal, priorizando regras de negócio, isolamento multiempresa, ciclo operacional e fechamento de operações já existentes.

## Entregas

### PCP / Produção
- OP rejeita quantidade planejada nula ou não positiva.
- Produto final e insumos são validados dentro da empresa autenticada.
- Número de OP é gerado quando ausente e não pode ser repetido dentro do tenant.
- Produto final não pode ser simultaneamente insumo da própria OP.
- Finalização mantém o consumo/entrada/custeio dentro da mesma transação.

### Financeiro / Contabilidade
- Baixa de título rejeita títulos cancelados ou pendentes de aprovação.
- Data da baixa não pode anteceder a emissão.
- Lançamentos contábeis exigem histórico, data, contas válidas, valores positivos e partidas D/C balanceadas.
- Lançamentos integrados identificados por origem não podem ser alterados ou excluídos manualmente.
- Conciliação existente no bloco anterior permanece preservada.

### Ativos
- CRUD operacional passou a exigir autenticação.
- Cadastro valida código, descrição, valores, residual e vida útil.
- Manutenção só pode apontar para ativo da mesma empresa e que não esteja baixado.
- Baixa de ativo é protegida contra repetição.
- Implementado cálculo de depreciação linear sem nova tabela: mensal, acumulada, meses decorridos e valor contábil.
- Implementado lançamento da depreciação calculada no campo acumulado do ativo.
- Tela de Ativos ganhou consulta e execução de depreciação.

### RH
- Folha exige competência YYYY-MM.
- Itens de folha exigem funcionário ativo do mesmo tenant, tipo válido e valor não negativo.
- Folha sem itens ou com valor líquido inválido não pode ser processada.
- Importação de ponto só aceita funcionário ativo da mesma empresa.
- Cargo valida nome e salário base.

### Projetos
- Projeto e entidades filhas passam a receber explicitamente o tenant autenticado.
- Orçamento, movimentos e faturamentos rejeitam valores negativos.
- Risco valida probabilidade 0–100.
- Mudança exige descrição.
- Faturamento não pode ser faturado duas vezes e exige valor positivo.
- Cliente usado no faturamento é validado pelo tenant.

### BI
- Indicadores, dashboards e relatórios não aceitam mais empresaId arbitrário do request.
- Tenant é obtido exclusivamente do usuário autenticado.
- Dashboard removeu o envio desnecessário de empresaId no frontend.

## Fora deste bloco

Fiscal continua deliberadamente fora do escopo desta rodada. CT-e, MDF-e e matriz final de NFS-e permanecem para o fechamento fiscal, preservando as implementações já existentes de São Paulo, Rondonópolis homologado e NFS-e Nacional.

## Próximo fechamento funcional

1. Aprofundar integrações contábeis automáticas onde o plano de contas já estiver parametrizado.
2. Fechar pendências de telas/endpoints operacionais ainda sem cobertura real.
3. Consolidar BI/relatórios e eliminar duplicidades.
4. Somente então retornar ao Fiscal para CT-e/MDF-e/NFS-e e homologações restantes.
