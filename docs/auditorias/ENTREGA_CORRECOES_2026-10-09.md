# Correções verificadas e lacunas restantes — 09/10/2026

Base auditada: `fab4c063aa98a6d8ac1c633558c6d49085e7f50c` (`main`, PR #98).
Este pacote corrige falhas comprovadas; não certifica paridade SAP nem prontidão para produção.

Pacote de fontes autorizado para publicação em PR no `euripedesdark/brasil-sass-erp`. Bundles compilados ficam fora deste pacote; o build os gera a partir dos fontes.

## Entregas recentes conferidas

PRs [#38](https://github.com/euripedesdark/brasil-sass-erp/pull/38), [#39](https://github.com/euripedesdark/brasil-sass-erp/pull/39), [#77](https://github.com/euripedesdark/brasil-sass-erp/pull/77), [#79](https://github.com/euripedesdark/brasil-sass-erp/pull/79), [#80](https://github.com/euripedesdark/brasil-sass-erp/pull/80) e [#81](https://github.com/euripedesdark/brasil-sass-erp/pull/81): mesclados, confirmados pelo GitHub e cotejados com o código atual. Comentários antigos não foram tratados como falhas atuais sem conferir as mudanças posteriores.

- FI-AA/PM: regras e testes de depreciação foram melhorados após #38, mas as tabelas novas e colunas dos modelos continuam sem migration correspondente. V173 atual é de compras, não de ativos. Este pacote adiciona **V184**, sem modificar V129 ou outra migration histórica.
- Intercompany: V183 **já adiciona** `diferenca`, `contrapartida_id` e `reconciliado_em`; não é preciso duplicar essa alteração. Persistiam a incompatibilidade da data enviada pela tela e a soma de moedas diferentes.
- TMS, contratos de fornecimento, eSocial e DIFAL: código integrado não equivale à comprovação de fluxos completos homologados.

## Alterações

| Área | Problema | Resultado |
|---|---|---|
| Compilação/SPED | `moedaOuZero` chamado sem existir | Formatação decimal com zero explícito para E110; formatação de campos opcionais preservada |
| Compilação/estoque | Pedido usa campo `padrao` inexistente e setters de documento ausentes | Consulta determinística por `tipo=PADRAO`, empresa e ativo; movimento usa `origem/origemId` existentes |
| ICMS-ST | Validação `@NotNull` inativa; percentuais inválidos e nulos convertidos em zero | `@Valid`, limites e validação no serviço; MVA acima de 100 continua permitida; redução opcional; tela pede JSON e limpa resultado anterior |
| Intercompany | Tela manda AAAA-MM para LocalDate; soma BRL e USD; filtra apenas um dia | Envia AAAA-MM-01, consulta o mês inteiro, exibe totais por moeda; `total` antigo permanece para moeda única e é nulo quando há moedas diferentes |
| Reconciliação | Mesmo número pode localizar espelho de competência diferente | Competência deve coincidir, incluindo comparação segura de nulos |
| FI-AA/PM | Modelos exigem 8 tabelas novas e colunas ausentes | V184 adiciona esquema correspondente, com valores padrão seguros e sem exclusão de dados |
| Prévia tributária | Serviço modifica o mapa recebido do simulador | Copia o mapa antes de acrescentar UFs, aceitando retornos imutáveis |
| Testes | Perfil H2 referencia arquivo ausente e tipos PostgreSQL não reconhecidos | Fixture sintética H2 com DDL correspondente às entidades; exceção explícita no gitignore |
| Testes | Stub CNAB não utilizado e expectativa de consulta de lançamento inexistente | Remove stub ocioso; teste verifica o 404 e a mensagem de domínio, distinguindo controller executado de rota ausente |
| Consolidação | Dois backups Stripe junto do código atual | Removidos após comparação completa; nenhuma funcionalidade exclusiva a migrar |

Inventário: [INVENTARIO_CONSOLIDACAO_2026-10-09.md](INVENTARIO_CONSOLIDACAO_2026-10-09.md). Foram examinados 1.231 arquivos de código próprio; nenhum par com conteúdo idêntico acima de 100 bytes; seis grupos com nomes coincidentes, preservados por contratos distintos. Este levantamento não prova que todos os arquivos sejam utilizados.

## Verificação executada

- Java 21: compilação do backend e dos testes; **250 testes, zero falhas, zero erros, zero ignorados**.
- Frontend: `npm ci`, cinco verificações de smoke e `npm run build`, todos aprovados. Distribuição estática regenerada localmente a partir do lockfile; os bundles não fazem parte do PR. O build do projeto gera os assets a partir dos fontes. Vite ainda alerta sobre tamanho de chunks.
- `git diff --check`: aprovado.
- Dump sanitizado: **14 partes conferidas por MD5**, todas corretas. Os arquivos são SQL em texto, não arquivo customizado `PGDMP`. O caminho `/home/euripedes/dump/...` não está montado neste ambiente; as partes do repositório foram suficientes.
- Extração apenas do esquema do dump sanitizado e execução em PostgreSQL embutido (PGlite). V184 aplicada duas vezes, oito tabelas verificadas, registro legado de teste manteve valor de aquisição e recebeu defaults seguros.
- Esse ensaio **não é restauração integral dos dados**, execução de Flyway real, validação Hibernate em PostgreSQL 18 nem homologação ponta a ponta. O dump de produção foi preservado.

### Reproduzir testes Java/frontend

```bash
mvn test
cd src/main/resources/static/react
npm ci
npm test
npm run build
```

O ambiente restrito de validação precisou carregar o agente Byte Buddy na inicialização da JVM para o Mockito, pois o attach dinâmico não estava disponível. Em ambiente normal, `mvn test` usa o comportamento padrão do Mockito. Os testes H2 têm escopo de aplicação e contratos; não substituem as migrations PostgreSQL.

## Bloqueios de banco reproduzidos

1. Migrations em banco sem esquema-base falham porque o histórico disponível começa em V109 e V110 exige `bc_core_empresa`. Não basta criar somente o schema `brasil_saas`.
2. Restaurar o esquema do dump sem histórico Flyway e repetir migrations antigas falha na **V129** por tabela já existente. O dump sanitizado não forneceu linhas de histórico Flyway para validar um baseline. **Não executar `baseline`/`repair` automático com número presumido.**
3. Ensaio de atualização a partir do esquema do dump, começando experimentalmente em V159, reproduziu **V163 → `bc_grc_risco` (criada em V170)** e **V165 → `bc_plm_produto_revisao`/`bc_plm_mudanca` (V168)**. É dependência fora de ordem.
4. Em ensaio isolado, carregar os pré-requisitos de V168/V170 antes das migrations posteriores permitiu executar V159–V184. Isso é diagnóstico, **não procedimento aprovado de atualização de banco existente**, não prova o baseline V158 e não altera os arquivos históricos.
5. V184 não resolve falhas anteriores na sequência: o Flyway precisa alcançar V184. Exigir histórico real do banco e um bootstrap novo, testado em PostgreSQL 18 e em cópia sanitizada, antes de aplicar em produção.

## Funcionalidades ainda necessárias para equivalência empresarial

Sem percentuais de cobertura presumidos. Cada fluxo precisa de persistência, autorização, isolamento por empresa, auditoria, concorrência, estorno, tela, integração e teste ponta a ponta.

| Prioridade | Fluxo | Evidência de conclusão exigida |
|---|---|---|
| P0 | Bootstrap, migrations e boot | PostgreSQL 18 vazio e cópia sanitizada; validação Hibernate e boot; upgrade com histórico íntegro |
| P1 | Contabilidade integrada/consolidação | Lançamento por evento, razão, fechamento/reabertura, conciliação e eliminações com câmbio e reconciliação do resultado |
| P1 | Compras e vendas completas | Contrato → pedido → recebimento parcial → conferência 3-way → documento → estoque → financeiro; crédito, devoluções e estornos |
| P1 | WMS/PCP/MRP/ATP | Reservas concorrentes, lote/série, disponibilidade temporal, capacidade, lead time, produção e custeio reconciliados |
| P1 | FI-AA/PM | Aquisição, depreciação, contabilização, baixa/estorno, manutenção, materiais e mão de obra, com relatórios conciliados |
| P1 | RH/TMS | Folha/encargos/eventos, carga/rota/entrega/frete e seus estornos, com integrações e homologação |
| P2 | Fiscal | Calculadoras integradas a documentos/apuração/SPED e testes por regime/UF; verificar fontes legais e homologar provedores |
| P2 | Governança/BI | Dados mestres, aprovações, trilha, relatórios reconciliados, observabilidade, recuperação e desempenho |

Os blockers de banco permanecem explícitos. Não mesclar este pacote como prova de que a instalação limpa ou a paridade SAP estão concluídas.
