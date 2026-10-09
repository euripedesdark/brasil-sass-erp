# Vendas e estoque: reservas fracionadas

Base conferida: `75253d3552d2d5b04e445631a8bcc2d694bd11ef` (main depois da PR 112).

## Falha reproduzida

Um pedido com quatro unidades e duas reservas de duas unidades encerrava somente
a primeira reserva. O teste executado antes da correcao falhou duas vezes: a
segunda reserva continuou RESERVADA ou SEPARACAO. A baixa tambem ignorava o
deposito, o lote e o endereco dessas reservas.

## Comportamento corrigido

- O faturamento agrega itens do mesmo produto, bloqueia as reservas do pedido e
  baixa cada alocacao do seu deposito. Uma reserva parcial cobre somente sua
  quantidade; o diferencial usa o deposito padrao existente.
- Cada movimento conserva lote/endereco. O saldo geral e o saldo do lote sao
  atualizados; a disponibilidade do endereco e conferida pela movimentacao.
  Reservas de outros pedidos, inclusive reservas sem pedido, continuam protegidas.
- Todas as reservas utilizadas sao consumidas. Uma expedicao WMS concluida antes
  da fatura conserva sua alocacao CONSUMIDA: o faturamento ainda realiza sua baixa,
  sem duplicar a baixa nem criar outro titulo em uma segunda tentativa.
- Reservas acima da quantidade do pedido sao rejeitadas antes da baixa. Itens
  repetidos nao fazem a primeira linha consumir as reservas das demais linhas.
- O endpoint de reserva exige pedido aberto da empresa atual, quando informado.
  Substituir uma quantidade exclui a propria reserva do calculo de disponibilidade.
  A chave inclui deposito/produto/lote/endereco; reservas anonimas sao independentes.
- Uma reserva em separacao nao volta silenciosamente a RESERVADA, e uma reserva
  consumida nao pode ser liberada. Registros encerrados nao sao reativados pelo
  endpoint de criacao. Duplicatas antigas da mesma posicao exigem ajuste explicito.

As alteracoes usam as tabelas existentes. Nenhuma migration historica foi alterada.
As operacoes permanecem transacionais, e as travas de saldo seguem ordem comum de
deposito/produto. Autenticacao e permissoes IAM permanecem as da main.

## Validacao

- Regressao antes da correcao: 11 testes, duas falhas reproduzindo a reserva residual.
- Depois da correcao: 28 testes focados aprovados.
- Suite local: 307 casos, 306 aprovados, um condicional PostgreSQL reservado ao CI;
  zero falhas ou erros.
- O teste nativo de bootstrap foi ampliado com repositories JPA reais, dois
  depositos, lote/endereco, reservas anonimas, itens repetidos, titulo, parcela e
  vinculos documentais. Confere a baixa e a rejeicao de faturamento duplicado,
  dentro da transacao descartavel existente. Seu resultado PostgreSQL depende da
  execucao do CI e deve ser conferido na PR.

## Dump conferido

As 14 partes sanitizadas totalizam 553.724.583 bytes. Todos os checksums MD5
conferem. Os oito COPY contêm 1.815.632 linhas de referencias fiscais e municipios;
nenhuma linha foi exibida na auditoria.

O novo job restaura o SQL completo em PostgreSQL 18 descartavel, usando cliente
PostgreSQL 18, e compara cada contagem com o dump. A restauracao nao aplica
baseline, repair ou migrations a um banco existente. O historico de migrations e
a compatibilidade das entidades continuam cobertos pelo bootstrap independente.
O dump sanitizado e suficiente para este teste; o dump de producao nao e utilizado.

## Limites de encerramento

Esta entrega encerra os cenarios acima de reserva e faturamento, quando o teste
nativo passar. Nao certifica equivalencia integral ao SAP. O recebimento de
devolucao tem fluxo de estoque existente, mas nao comprova credito financeiro,
documento fiscal ou estorno contabil automaticos. A transmissao Reinf e a cadeia
real de certificados por usuario continuam exigindo validacao das integracoes
externas documentadas no inventario. Nao foram simuladas como funcionalidades
concluidas nem substituidas por implementacoes locais.
