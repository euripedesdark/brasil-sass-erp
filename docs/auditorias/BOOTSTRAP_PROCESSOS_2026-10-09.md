# Instalação vazia e correções de processos — 9 de outubro de 2026

Base: `main` em `e74fbcc762e13d81b8c39200e27a42edbd920a14` (PR #99 incorporado).

## Banco e compatibilidade

A sequência histórica começa em V109 e não cria todas as tabelas necessárias em um banco vazio. Além disso, V163 depende de objetos criados em V170, e V165 depende de V168. Reexecutar todas as migrations sobre o esquema restaurado também encontra tabelas já existentes.

`B184__bootstrap_erp_novo.sql` é uma migration **baseline do Flyway**, executada somente em instalações novas. Ela contém o esquema estrutural do dump sanitizado, sementes de permissões/módulos provenientes das migrations e os complementos V159–V184, com as dependências adiantadas dentro do próprio bootstrap. Não contém linhas COPY do dump, valores de sequências nem histórico Flyway restaurado. Não inclui dados de produção ou clientes.

Gerador: `python scripts/db/gerar_bootstrap184.py`. Ele confere os checksums MD5 das partes e lê as 14 partes na ordem, inclusive linhas cortadas entre partes, elimina os blocos de dados e preserva as funções/DDL. SHA-256 do B184 desta entrega: `65c54434179a59d26cd8734551ab20168561e3858691cfaa28a8cb10110ee88e`. Depois de publicada/aplicada, B184 deve permanecer imutável; alterações futuras usam V185 em diante.

V185 acrescenta 15 colunas efetivamente ausentes em contratos, notificações, caixa, cobrança, promessas e rescisões. Campos de negócio não recebem valores históricos inventados. UUIDs novos usam `gen_random_uuid()`. As entidades de contratos passam a usar as colunas existentes de datas, descrição, renovação e valor utilizado, preservando os dados e evitando colunas paralelas. Valor total de contrato de venda não é presumido a partir de valor mensal.

`baseline-on-migrate` passa a `false`: um banco com tabelas e sem histórico não recebe automaticamente uma versão presumida. Bancos existentes com histórico continuam com suas V migrations. Bancos restaurados sem histórico exigem levantamento da versão real e recuperação controlada do histórico; esta entrega não executa `baseline`, `repair` ou restauração em produção. As dependências antigas V163/V165 ainda precisam de tratamento específico para instalações existentes anteriores a essas versões.

## Processos corrigidos

- Crédito usa cliente da empresa solicitante, sem consultar cadastro de outra empresa.
- Exposição de crédito considera somente títulos a receber ABERTOS/PARCIAIS; contas a pagar e títulos liquidados não consomem limite.
- Faturamento bloqueia o saldo em transação e desconta as reservas ativas de outros pedidos antes da baixa. O parâmetro de autorização excepcional de crédito não ignora reservas de estoque.
- Descontos são validados por item: um desconto negativo ou superior ao bruto não pode ser compensado por outro item.
- Frete negativo, quantidade não positiva e percentual acima de 100 são recusados. Desconto integral continua permitido.
- Contratos legados sem data final mantêm vigência aberta; ausência da data inicial retorna erro de negócio.

## Verificação

A execução real de Flyway 10.17 por JDBC contra PostgreSQL 18.3/PGlite aplica B184 e V185, valida o histórico e aplica zero migrations na segunda execução. Hibernate 6.5 valida as **227 entidades** com a estratégia de nomes usada pelo Spring Boot. Isso confirma compatibilidade estrutural; não equivale ao teste completo do servidor com Redis, RabbitMQ, MongoDB e integrações fiscais.

`BootstrapPostgresTest` reproduz instalação, validação e segunda execução. É habilitado somente com `ERP_BOOTSTRAP_TEST_URL` e recusa bancos que já tenham tabelas em `brasil_saas`/`dl` antes de modificar o esquema. O teste também grava e relê contratos de compra/venda via Hibernate, verificando datas, valor utilizado, descrição, renovação e valor; os dados sintéticos são revertidos ao final. Usar banco descartável e vazio:

```bash
ERP_BOOTSTRAP_TEST_URL='jdbc:postgresql://127.0.0.1:5432/erp_bootstrap_teste' \
ERP_BOOTSTRAP_TEST_USER=postgres \
ERP_BOOTSTRAP_TEST_PASSWORD='senha-do-banco-descartavel' \
mvn test -Dtest=BootstrapPostgresTest
```

No adaptador PGlite Socket, a URL usa `prepareThreshold=0` e o teste desativa o lock transacional do Flyway para contornar limitações do adaptador. A configuração de produção de locks não foi alterada. Flyway 10.17 informa que seu suporte testado vai até PostgreSQL 16; compatibilidade com PostgreSQL 18 foi exercitada aqui, mas a atualização da dependência deve ser avaliada separadamente.

Validação Java: suíte completa com 261 testes aprovada; após a última correção de crédito, os 24 testes afetados foram repetidos. Os relatórios consolidados cobrem 262 testes únicos, sem falhas, erros ou testes ignorados. O teste PostgreSQL foi repetido após acrescentar gravação/leitura de contratos. `git diff --check` aprovado. O frontend não foi alterado nesta etapa. O job de migrações do GitHub confirmou B184/V185 em PostgreSQL 18.6 nativo. A checagem antiga de tabelas procurava no esquema padrão e foi corrigida para validar nomes exatos em `brasil_saas`; o job agora executa também `BootstrapPostgresTest`.

## Meta funcional e próximas evidências

Esta entrega não demonstra equivalência ao SAP. A meta depende de testes completos de processos, com persistência e reversão, e não da quantidade de telas/tabelas.

| Processo | Evidência desta entrega | Evidência ainda necessária |
|---|---|---|
| Instalação nova | Flyway, histórico, repetição e 227 mapeamentos | Inicialização completa e serviços externos |
| Venda e crédito | Isolamento empresarial, exposição correta e reservas respeitadas | Pedido → entrega/faturamento → recebimento → devolução/estorno |
| Compras por contrato | Mapeamento de dados legados e vigência | Liberação → recebimento → conferência da fatura → pagamento/estorno |
| Estoque/WMS | Proteção de reservas na baixa de vendas | Lotes, endereços, separação, expedição e reversão concorrente |
| Financeiro/contábil | Campos estruturais compatíveis | Integração de títulos/baixas com partidas contábeis e fechamento |
| Fiscal Brasil | Correções ICMS-ST/EFD do PR #99 | Autorização, rejeição, contingência e cancelamento com integrações reais |
| Produção/MRP | Suíte existente preservada | Planejamento → consumo → apontamento → custo → contabilização |
| Ativos/manutenção | V184 preservada e entidades compatíveis | Aquisição → depreciação → baixa; ordem → materiais/custos → encerramento |

Nenhuma funcionalidade exclusiva foi removida nesta etapa. Nenhum dump de produção foi restaurado ou alterado.
