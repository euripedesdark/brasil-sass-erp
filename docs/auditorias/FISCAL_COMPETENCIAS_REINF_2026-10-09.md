# Fiscal: competencias e fechamento local Reinf

Conferencia sobre main apos PRs #113–#116. Samba/CA funciona localmente, conforme confirmado pelo proprietario; nao faz parte das pendencias desta entrega.

## Defeitos reproduzidos e correcoes

- Competencias como `3/2026`, `03/26` e `03/2026/extra` eram aceitas. Reinf e apuracao exigem agora `MM/AAAA` com mes valido.
- Consultas mensais usavam BETWEEN inclusivo com fim no primeiro instante do mes seguinte. Os novos metodos usam inicio inclusivo e fim exclusivo, preservando empresa, tipo de operacao e exclusao logica.
- O JSON manual do Reinf nao escapava quebras de linha e tabulacoes. Jackson preserva o conteudo em JSON valido.
- Reinf nao deve incluir notas digitadas, em emissao, rejeitadas, canceladas ou com falha de emissao. Aceita EMITIDA e AUTORIZADA.
- Geracao e fechamento adquirem bloqueio transacional na empresa para serializar operacoes da mesma empresa, incluindo a primeira geracao sem eventos existentes. O bloqueio dura somente a transacao; nao altera esquema ou migrations.
- Competencia fechada/transmitida nao e regenerada. Repetir fechamento local devolve o mesmo R-2099 FECHADO, sem inserir outro evento.
- Consulta de detalhe considera empresa e exclusao logica diretamente no repositorio.

## Verificacao

Os testes de regressao inicialmente demonstraram competencia inconsistente e JSON invalido. A suite fiscal focada passou: 27 casos, zero falhas e erros.

`FiscalCompetenciasPostgresScenario` integra o teste nativo `BootstrapPostgresTest`, com dados sinteticos e rollback. Confere NF-e e NFS-e na virada do mes, separacao por empresa/tipo, notas sem emissao, totais, geracao repetida, fechamento repetido e bloqueio de regeneracao. O resultado desse cenario deve ser conferido no job Test Database Migrations da PR.

## Limite funcional

Esta entrega corrige geracao e fechamento locais. O proprio payload informa que transmissao RFB nao esta implementada nesse servico. Nao transforma JSON local em declaracao fiscal transmitida nem altera o IAM compartilhado, os certificados locais ou as migrations historicas.
