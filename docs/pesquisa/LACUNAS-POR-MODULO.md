# O que falta em cada módulo, sem IA e sem fiscal

Levantamento de 26/09/2026. Módulos **IA** e **fiscal** estão fora — estão
sendo tratados separadamente.

## Como este documento foi feito

Duas fontes: uma varredura do código com contagem real de linhas e rotas, e
**sete afirmações verificadas ao vivo** antes de entrar aqui. Onde não
verifiquei, o texto diz "da varredura" e não afirma.

Verificado ao vivo, com requisição ou consulta ao banco:

| Afirmação | Como foi verificada |
|---|---|
| `bc_rh_funcionario.pessoa_id` é NOT NULL | `information_schema` |
| A tela de RH não manda `pessoaId` | 0 ocorrências em `RH.jsx` |
| `POST /api/rh/funcionarios` falha com o payload da tela | 409, e o log diz `null value in column "pessoa_id"` |
| `GET /api/core/perfis` não existe | 404 ao vivo |
| Estoque tem 9 controllers e 0 services | contagem de `@Service` |
| 5 tabelas `bc_fin_*` não têm repository | grep por classe em `*Repository.java` |
| `ProdutoServiceImpl.buscarPorId` não filtra `empresaId` | leitura do método: `findById(id)` puro |

O resto — contagens de linhas, número de rotas, serviços órfãos — é leitura de
código, não medição de comportamento. Está marcado como tal.

## Resumo

| Módulo | Estrutura | Responde? | A lacuna mais grave |
|---|---|---|---|
| **cadastro** | 12 ctrl / 12 svc / 21 repo / 10 telas | parcial | `atualizar` e `excluir` de Produto e Pessoa não filtram `empresaId` — **edita registro de outra empresa** |
| **vendas** | 2 / 1 / 5 / 2 | sim, com buracos | não existe rota de edição de pedido, e **linha de serviço é impossível de criar** |
| **compras** | 4 / 2 / 8 / 4 | sim | `findByIdForUpdate(id)` sem `.filter(empresaId)` — recebimento de compra de outra empresa |
| **estoque** | 9 / **0** / 12 / 9 | sim, sem service | 33 rotas de regra de negócio **dentro dos controllers** |
| **produção** | 4 / 3 / 5 / 4 | parcial | `Producao.dataInicio` **nunca é gravado** → o relatório de produção volta vazio com filtro de data |
| **serviços** | 1 / 1 / 3 / 2 | **não** | a tela de OS **grava cliente nulo** e **nunca envia os itens** |
| **financeiro** | 12 / 6 / 23 / 14 | parcial | **10 tabelas `bc_fin_*` sem repository, service ou controller** |
| **rh** | 4 / 1 / 3 / 4 | **não** | o botão "Novo Colaborador" **sempre falha** |
| **bi** | 6 / 7 / 9 / 4 | **não calcula** | `refreshKpiValues` itera lista vazia e devolve 200 |
| **core/admin** | 13 / 18 / 10 / ~14 | parcial | `GET /api/core/perfis` não existe e `listar()` de usuário não filtra tenant |

## O que está verificado ao vivo

### 1. RH — o botão "Novo Colaborador" não funciona

```
pessoa_id em bc_rh_funcionario: is_nullable = NO
RH.jsx: 0 ocorrências de "pessoaId"

POST /api/rh/funcionarios  {"nome":"Teste","matricula":"998",...}
  -> 409 "Violação de integridade (duplicidade ou FK inválida)"
  -> log: null value in column "pessoa_id" of relation "bc_rh_funcionario"
```

A tela monta `{ nome, matricula, tipoColaborador, salario }` e manda
`JSON.stringify(form)`. O `Funcionario` **não tem campo `nome`** — ele aponta
para `Pessoa`. Então faltam duas coisas: o `pessoaId` no payload, e um caminho
para escolher ou criar a pessoa.

Por consequência, a coluna "Nome Completo" da tabela de colaboradores é sempre
em branco, e `percentualComissao` e `valorHora` — que a OS usa para calcular a
comissão do técnico — não têm input em lugar nenhum. **A comissão de OS é
sempre 0,00** a menos que alguém edite o banco.

### 2. Core — `GET /api/core/perfis` não existe

A tela de Usuários chama, com o comentário `// Assumindo que existe`:
`apiFetch('/api/core/perfis')`. A rota de perfil é `/api/core/perfil`
(singular, e é o perfil de quem está logado). **404, sempre.**

O mesmo controller tem `listar()` fazendo `usuarioRepository.findAll()` — sem
filtro de `empresaId`. **Um admin da empresa A vê os logins da empresa B.**

### 3. Estoque — 33 rotas, zero services

```
controllers em estoque/:  9
@Service em estoque/:     0
```

A regra de negócio existe e está correta — transferência com `findForUpdate`,
saldo menos reservas, duas movimentações por transferência. Só que mora no
controller, que injeta 8 repositories e faz tudo inline.

Consequências concretas:

- Não há como testar transferência sem subir contexto Spring + banco.
- A regra "disponível = saldo − reservas ativas" está em **4 cópias**
  (`ReservaEstoqueController` duas vezes, `TransferenciaEstoqueController`,
  `PedidoVendaServiceImpl`). Nenhuma compartilhada. Uma delas divergindo do
  resto dá estoque negativo sem erro.
- `SaldoEstoqueController` e `MovimentacaoEstoqueController` têm **1 rota cada,
  só GET**. Não existe "criar saldo" nem "ajuste manual". Produto cadastrado
  e nunca vendido **não aparece em lugar nenhum** — a tela de estoque lista só
  saldos. E **corretivo de inventário feito à mão é impossível**: só via
  inventário.

### 4. Cadastro — escrita atravessando empresa

`ClienteServiceImpl` foi corrigido e filtra `empresaId` + `deletedAt`.
`Produto` e `Pessoa` não:

```java
// ProdutoServiceImpl.buscarPorId
Produto produto = produtoRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
```

`findById` puro, sem empresa. E `atualizar` e `excluir` seguem o mesmo caminho.
`PUT /api/cadastro/produtos/{id}` edita produto de outra empresa.

Ver as duas linhas (`87`, `118`) antes de mexer — a correção é copiar o
`obter()` do `ClienteServiceImpl`, que já existe e já funciona.

### 5. Financeiro — 10 tabelas que nada escreve

`bc_fin_orcamento`, `bc_fin_orcamento_realizado`, `bc_fin_projecao_fluxo_caixa`,
`bc_fin_emprestimo`, `bc_fin_aplicacao_financeira`, `bc_fin_renegociacao`,
`bc_fin_provisao_pdd`, `bc_fin_analise_rentabilidade`,
`bc_fin_integracao_bancaria`, `bc_fin_periodo_contabil` — e
`bc_fin_fluxo_aprovacao`.

Sem repository, sem service, sem controller, sem rota, sem tela. As classes
existem, o Hibernate cria as tabelas, e elas ficam vazias para sempre.
Orçamento, fluxo de caixa projetado, empréstimo, aplicação, renegociação, PDD,
rentabilidade, integração bancária e fechamento de período contábil
**não existem como funcionalidade**.

Verificado para 5 delas (0 repositories cada). As outras 5 são da mesma
varredura.

Consequência maior, que vem do que **existe**: **faturar uma venda não gera
lançamento contábil.** `PedidoVendaServiceImpl.faturar` dá baixa de estoque,
cria título e parcelas — e não chama `LancamentoContabilService.criar`.
O `bc_fin_lancamento` só ganha linha se alguém montar partida dobrada à mão.

E **`ComissaoServiceImpl.pagar` não paga nada**: troca o status para `PAGO`, sem
`Baixa`, sem `Extrato`, sem mexer na conta. É um carimbo.

## O padrão que mais aparece

**Coluna NOT NULL no banco, valor obrigatório só na aplicação, e quem valida é
o banco na hora do insert.** Aconteceu três vezes no mesmo dia:

| Onde | O que faltava | Sintoma |
|---|---|---|
| `bc_fis_nfe` | `valor_produtos` e mais 6 | 409 na entrada de nota — **nunca funcionou** |
| `bc_fis_nfe_item` | `created_at` (classe sem auditoria) | 409 na entrada de nota — **nunca funcionou** |
| `bc_cad_produto` | `estoque_minimo` e mais 3 | 409 em qualquer cliente de API que omita o campo |

E o agravante que os três compartilham: o `GlobalExceptionHandler` traduz tudo
para **409 "duplicidade ou FK inválida"**, que é a mensagem errada para a causa
real. Quem lê a resposta procura duplicata; a verdade está no log do banco, em
`null value in column "..."`.

Duas correções possíveis, e as duas valem:

1. **Ler o log antes de acreditar na mensagem.** Foi o que resolveu os três.
2. **O handler deveria citar a coluna.** `getMostSpecificCause()` já tem a
   informação. Trocar "duplicidade ou FK inválida" por
   `violação de integridade: estoque_minimo é obrigatório` economiza a próxima
   meia hora.

## O segundo padrão: tela que envia menos do que a tabela exige

Não é só backend. Três telas ligam um campo no formulário e **não o mandam**:

| Tela | O que a tela monta | O que a tabela exige |
|---|---|---|
| `rh/RH.jsx` | `{nome, matricula, tipo, salario}` | `pessoa_id` NOT NULL |
| `cadastro/CadastroPessoas.jsx` | 11 campos de pessoa | renderiza input para 2 |
| `servicos/OrdemServico.jsx` | `clienteId` (nunca setado) | OS sem cliente |

A de OS é a pior em número de defeitos independentes, todos numa tela só:
cliente nunca salvo, itens nunca enviados, três botões que nunca aparecem
(porque leem `baixaMov`, campo que não existe no response — `undefined === 'N'`
é falso), coluna de status que mente, duas colunas sempre em branco, e um
`window.open('/notas-fiscais')` para rota inexistente.

## Sobre "responde?"

A tabela do início diz "responde" para compra, estoque e (com ressalva) vendas
porque **o caminho principal funciona** — receber compra soma saldo e grava
movimentação, faturamento baixa estoque e cria título com parcelas. Isso é
verdade e vale registrar.

Mas "responde" no caminho principal não é "pronto". Os dois buracos de
multi-tenant em compra e cadastro estão no caminho principal. E em estoque,
"responde" significa que a regra está escrita — dentro do controller, sem
service, e com a mesma conta repetida em 4 lugares.

## O que eu não medi

- Se o build Java e o do Vite passam (rodaram, mas isso é outra coisa).
- Quantas linhas tem cada tabela `bc_*`. As listas de "tabela sem escritor" são
  inferência de código — nenhum writer existe — não contagem.
- `ConciliacaoBancaria.jsx`, `AuditoriaFuncionalERP.jsx`, `Configuracoes.jsx`,
  `Usuarios.jsx`, `RelatorioParidadeERP.jsx` e `Login.jsx`: existem mas sem número
  de linhas medido. Não inventei.
- `pom.xml`, `docker-compose.yml`, `application*.yml` e as migrations Flyway
  antigas não foram abertos para este levantamento.

## Onde eu começaria, e por quê

Não por gravidade — por **custo de checagem**. Estes quatro são barato de
confirmar que continuam quebrados e caro de manter quebrados:

1. **`GET /api/core/perfis`** — uma rota, e a tela de Usuários volta a
   funcionar. 404 hoje.
2. **RH sem `pessoaId`** — o botão que hoje não faz nada. Precisa decidir se a
   tela escolhe uma Pessoa existente ou cria uma, e essa decisão é sua.
3. **`Produto`/`Pessoa` cross-tenant** — copiar o `obter()` do `Cliente`, que já
   está escrito e já funciona. É segurança, não usability.
4. **`bc_fin_lancamento` sem escritor** — faturamento que não gera lançamento
   contábil é o furo que mais aparece em fechamento.

E o que eu **não** faria sem você decidir: os 10 módulos de financeiro sem
funcionalidade. Não é bug, é escopo. `bc_fin_orcamento` e `bc_fin_emprestimo`
estão lá porque alguém planejou. Ou se constrói, ou se apaga — deixá-las
vazias é o pior dos dois, porque o relatório de "o que falta" sempre inclui
tabela que ninguém pediu.
