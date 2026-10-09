# Encerramento baseado em evidencia atual

Base: main `694fdb3da551cd9e8f0089d4f50986af07d0d39b`, apos o PR #111. Os PRs #99–#111 incorporados nao sao backlog. IAM compartilhado, login e regras de administrador foram alterados e resolvidos nesse historico; este incremento preserva seu codigo de producao.

O inventario adjacente e reproduzivel por `python scripts/audit/inventario_modulos.py`. As contagens confirmam implementacoes significativas; nao medem conclusao funcional. Contabilidade possui lancar, estornar, razao, balancete, balanco, DRE, fechamento/reabertura e geracao por titulo. WMS possui ondas, separacao, putaway, volumes, embalagem, conferencia e finalizacao de expedicao. Esses recursos existentes nao devem ser apresentados como modulos a construir do zero.

## Evidencia de funcionamento existente

Os checks dessa base aprovaram frontend, migracoes PostgreSQL, rollback de migracao, imagem Docker e verificacoes de qualidade/seguranca. O backend registrou 290 testes, com cinco falhas no mesmo teste de IAM, zero erros e um teste condicionado ao ambiente. Os cinco casos ainda proibiam consulta ao perfil SUPERUSER, mas o PR #111 passou a consultar o perfil existente no provisionamento dos administradores. A correcao desta auditoria mantem a regra implementada e verifica a consulta, sem permitir criacao de perfil por esse caminho. Usuarios Domain Users continuam sem essa consulta quando nao sao administradores.

Referencias dos jobs da base:

- Backend: https://github.com/euripedesdark/brasil-sass-erp/actions/runs/37960911777/job/113923201048
- Build/test: job `113923203440`; ambos falharam pelos mesmos cinco casos de AuthServiceIamTest.
- Falhas de deployment automatico sao registradas separadamente; nao demonstram falta de um modulo de negocio nem autorizam alteracao de credenciais/servidores.

## Lacunas delimitadas pelo codigo

| Fluxo | Evidencia atual | Escopo a conferir antes de qualquer PR funcional |
|---|---|---|
| Vendas: faturamento e reservas | PedidoVendaServiceImpl.faturar usa findFirst para consumir reserva por item/produto | Reproduzir reservas fracionadas para o mesmo produto e verificar quantidade/deposito antes de alterar consumo; nao reescrever faturamento |
| Devolucao de venda | DevolucaoService.receber grava saldo/movimento de estoque e recebimento | Verificar o encadeamento com titulo, documento fiscal e lancamento contabil; estornar existe em contabilidade, mas isso nao prova integracao automatica da devolucao |
| Fiscal: Reinf | ReinfService declara no payload do fechamento: transmissao RFB nao implementada | Separar fechamento local da transmissao externa; implementacao depende do contrato, certificados e ambiente fiscal reais |
| Certificados IAM | CertIssueService gera chave, CSR e certificado autoassinado | Confirmar etapa externa de assinatura pela CA antes de mudar o IAM compartilhado ou seus certificados; pertence a outro repositorio |

Esses itens sao observacoes sobre caminhos concretos. Nao permitem rotular Fiscal, Vendas, Financeiro ou Contabilidade inteiros como incompletos. Ausencia de uma chamada em um metodo exige rastrear outros fluxos antes de afirmar ausencia no sistema.

## Regra para PRs por modulo

Uma PR funcional deve incluir o cenario que reproduz a lacuna, a regra de negocio esperada, a alteracao localizada, testes de regressao significativos e evidencia do fluxo. Preservar APIs e recursos existentes; criar migration nova somente quando uma alteracao de esquema for necessaria. Nao editar migrations historicas nem restaurar dados de producao para demonstrar funcionalidade.

Marcar como verificado o cenario efetivamente demonstrado. Declarar um modulo encerrado apenas quando seus criterios de aceite forem atendidos, com integracoes e dependencias delimitadas. A semelhanca com SAP nao e medida por quantidade de classes nem por uma lista antiga de nomes de modulos.
