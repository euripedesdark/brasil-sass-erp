# Ciclo comercial: devolucoes e integridade do credito

Base: main apos PR #117. Entrega agrupada de defeitos comprovados em compras, vendas, estoque e credito, preservando APIs, IAM e migrations historicas.

## Correcoes

1. Devolucao de compra grava quantidade negativa para SAIDA, deposito escolhido e saldo apos. Antes gravava quantidade positiva e deposito nulo, contrariando o livro de movimentos usado pelo estoque.
2. Executar e cancelar devolucao de compra adquirem o mesmo bloqueio pessimista, restrito a empresa. Solicitacoes bloqueiam o pedido tambem restrito a empresa.
3. Devolucoes de compra escolhem deposito ativo pelo tipo PADRAO, com alternativa ativa, sem exigir codigo literal PADRAO. Baixa considera reservas RESERVADA/SEPARACAO e nao consome estoque comprometido.
4. Produtos repetidos numa solicitacao de compra sao agregados antes de conferir recebimento e devolucoes anteriores. Todos os itens sao validados antes de gravar a solicitacao.
5. Quantidades nulas, negativas, zeradas, malformadas ou com mais de tres casas decimais sao rejeitadas, evitando arredondamento divergente entre item e estoque. Motivo de compra respeita os 500 caracteres da coluna.
6. Recebimento de devolucao de venda confere itens antes de movimentar, rejeita devolucao vazia e itens inconsistentes, ordena bloqueios por produto e bloqueia o deposito para serializar criacao do primeiro saldo entre devolucoes. Solicitacao de venda valida o conjunto antes de gravar.
7. Analise financeira de credito conta somente titulos R ABERTO/PARCIAL. Contas a pagar nao reduzem credito nem tornam o cliente inadimplente. Orcamentos e pedidos excluidos nao comprometem limite.
8. Faturamento normal bloqueia o cliente antes da leitura de recebiveis; alterar limite usa a mesma trava. Isso serializa faturamentos do mesmo cliente e alteracao do limite, mantendo a opcao existente de faturamento forcado.

## Evidencia

A suite focada de devolucoes e credito passou com 26 casos. Novos testes exercitam os servicos reais, substituindo o teste de credito anterior que repetia uma regra local sem chamar o servico. Ha regressao para excesso agregado, quantidade invalida, sinal/deposito/saldo do movimento, reservas, repeticao da devolucao, cancelamento e filtragem financeira.

O cenario PostgreSQL `DevolucoesCreditoPostgresScenario` continua o faturamento real da venda: recebe devolucao, confere saldo, impede repeticao/excesso, executa devolucao ao fornecedor, confere movimento negativo, preserva reservas e confere credito excluindo contas a pagar. Todos os dados sao sinteticos e sofrem rollback.

## Limites da verificacao

Os servicos existentes de devolucao fazem movimentacao fisica. Nao ha ajuste financeiro, emissao de NF-e de devolucao ou estorno contabil automatico nesses metodos. Esta entrega nao inventa essas regras nem cancela titulo de uma venda inteira em uma devolucao parcial. Esses encadeamentos precisam de um contrato funcional explicito para serem apresentados como concluidos. Samba/CA esta funcionando localmente e fora das pendencias.
