# Auditoria de Frontend e Consultas Internas/Externas — 2026-10-03

## Objetivo

Verificar o frontend atual do Brasil SaaS ERP não apenas pela existência de JSX, mas pelo fluxo real:

**tela → consulta interna/externa → API → persistência → retorno → tratamento de erro**.

IA fica fora do escopo de paridade funcional.

## Estado do frontend no branch `main`

Inventário do tree atual:

| Indicador | Estado |
|---|---:|
| Arquivos em `static/react` | 222 |
| JSX | 108 |
| JS | 62 |
| CSS | 45 |
| Serviços JS | 59 |
| Rotas declaradas em `App.jsx` | 96 |
| Smoke test de frontend | 1 |

O CI já detectou um problema real durante esta etapa: o novo `Mrp.jsx` estava sem fechamento de JSX/JavaScript e o Vite parou com **Unexpected end of file**. A tela foi reescrita e a validação foi disparada novamente. O novo run ainda estava em execução no momento desta auditoria; portanto, não se declara CI verde antes do resultado final.

## Regra para consultas

### 1. Consulta interna

Usar a API do ERP quando o dado pertence ao domínio da empresa ou ao catálogo controlado pelo ERP.

Exemplos:

- Pessoa;
- Cliente;
- Fornecedor;
- Transportadora;
- Produto;
- Serviço;
- Funcionário;
- Produto da BOM;
- Estoque e saldo;
- Depósito/endereço/lote;
- Município do cadastro;
- NCM/CFOP/CEST/ISSQN armazenados no ERP;
- plano de contas;
- centro de custo;
- títulos;
- ordens de produção;
- ordens de serviço.

A consulta deve respeitar o tenant da sessão. IDs digitados manualmente não devem ser o caminho normal para selecionar entidades.

### 2. Consulta externa

Usar fonte externa somente quando o dado precisa ser obtido de um serviço externo ou de uma autoridade fiscal/serviço público.

Implementado nesta etapa:

- **CEP:** ViaCEP, acionado pelo usuário na tela de Pessoas.
- **SEFAZ:** consulta passa pelo backend do ERP, usando certificado/configuração fiscal; o navegador não acessa certificado nem endpoint da SEFAZ diretamente.

Fontes externas não devem substituir o cadastro interno. Exemplo: o ViaCEP preenche logradouro/bairro/UF, mas o município é resolvido contra `/api/municipios` e o ERP grava a FK interna.

## Fluxos corrigidos nesta etapa

### Pessoas

- Cadastro passou a persistir o endereço principal na edição.
- CEP é normalizado antes de gravar.
- Consulta externa de CEP tem timeout de 8 segundos.
- Falha externa não impede edição manual.
- Município retornado pelo CEP é procurado no catálogo interno.
- Operações de Pessoa passaram a receber o `empresaId` da sessão.
- Listagem, busca, edição e exclusão ficaram explicitamente tenant-aware.

### RH

- Funcionário agora seleciona uma Pessoa do cadastro interno.
- Criado `GET /api/rh/funcionarios/pessoas` para lookup mínimo de Pessoa.
- O lookup retorna somente ID, nome e documento.
- O endpoint usa o tenant da sessão.
- Comissão e valor/hora passaram a aparecer no formulário.

### Ordem de Serviço

A tela tinha campos visuais que não correspondiam integralmente ao contrato do backend.

Corrigido:

- Cliente selecionado na tela agora gera `clienteId`.
- Edição carrega itens da OS.
- Produtos são consultados no cadastro interno.
- Serviços são consultados no cadastro interno.
- Cada item precisa apontar para Produto ou Serviço.
- Quantidade e valor unitário agora alteram o estado real da tela.
- Itens novos são enviados para `POST /api/servicos/os/{id}/itens`.
- A antiga abertura de `/notas-fiscais`, que não correspondia a uma rota real do ERP, foi removida. A tela informa que a continuidade fiscal deve ocorrer pelo módulo Fiscal.

### MRP

- Tela foi reconstruída para corrigir o erro de sintaxe que quebrava o Vite.
- Produto final agora é selecionado por consulta interna ao cadastro.
- O usuário não precisa digitar ID.
- Quantidade é validada antes da simulação.

### Fiscal / SEFAZ

- Chave de NF-e é normalizada e validada com 44 dígitos.
- CNPJ da distribuição é normalizado e validado com 14 dígitos.
- Respostas HTTP 503 da SEFAZ agora preservam a mensagem do backend.
- A tela consegue informar quando a integração está desabilitada por certificado/configuração.

### Segurança de consultas

- Busca de fornecedor durante importação de NF-e foi alterada para usar o tenant da empresa.
- Produto e Pessoa ganharam operações de leitura/escrita tenant-aware.
- O frontend continua enviando `X-Empresa-Id` através do `ApiConfig`, mas o backend não deve confiar em `empresaId` enviado pela tela para decidir o tenant do usuário.

## Matriz de consulta por domínio

| Domínio | Fonte normal | Externa? | Regra |
|---|---|---|---|
| Pessoas | PostgreSQL ERP | CEP sim | CEP pode preencher; usuário pode corrigir |
| RH | PostgreSQL ERP | não | Pessoa vem do cadastro interno |
| Vendas | PostgreSQL ERP | não na operação básica | Cliente/produto/condição/preço internos |
| Compras | PostgreSQL ERP | fornecedor externo somente em integração futura | Não criar fornecedor automaticamente sem decisão |
| Estoque | PostgreSQL ERP | não | Saldo, lote, endereço e depósito internos |
| Produção | PostgreSQL ERP | não | BOM/produto/OP/estoque internos |
| MRP | PostgreSQL ERP | não | Produto, BOM e estoque internos |
| Fiscal — NCM/CFOP/CEST/ISSQN | PostgreSQL ERP | atualização externa possível | Consulta transacional interna; sincronização oficial deve ser processo controlado |
| NF-e | ERP + SEFAZ | sim | Backend + certificado; nunca browser direto |
| NFS-e | ERP + prefeitura/provedor | sim | Backend + certificado/credenciais conforme município/provedor |
| SPED | ERP | transmissão externa futura | Geração local e validação antes da transmissão |
| Boletos/CNAB | ERP + serviço/banco | sim | Integração externa isolada do frontend |
| eSocial | ERP + governo | sim | Backend/integrador; não chamada direta do React |
| Bancos/OFX/PIX | ERP + banco | sim | Adaptador externo; dados internos devem permanecer no ERP |

## O que ainda precisa ser construído

### PCP / Produção

Ainda faltam como processo completo:

- roteiro versionado;
- operações;
- centros de trabalho;
- capacidade disponível;
- calendário/turnos;
- tempos de setup e produção;
- programação/MPS;
- MRP com demanda, pedidos, lead time e lote;
- ordens sugeridas;
- refugo;
- coprodutos/subprodutos;
- backflush;
- custeio industrial e variações.

### Comercial

- devolução/troca;
- bonificação;
- contratos;
- recorrência;
- CRM/funil;
- metas;
- aprovação de desconto;
- crédito;
- entrega/frete;
- documento fiscal de saída integrado ao pedido.

### Fiscal

- emissão real NF-e/NFC-e;
- eventos fiscais;
- inutilização;
- CC-e;
- contingência;
- CT-e/MDF-e;
- obrigações completas;
- rejeições e reprocessamento;
- transição tributária parametrizada.

### Financeiro/Contabilidade

- orçamento;
- cobrança completa;
- renegociação;
- adiantamentos;
- CNAB/OFX;
- PIX/boleto integrado;
- fechamento financeiro;
- partidas automáticas por documento;
- períodos contábeis;
- fechamento contábil;
- razão/diário/balanço;
- rateios;
- integração contábil completa.

### RH

- admissão;
- férias;
- ponto;
- benefícios;
- afastamentos;
- rescisão;
- documentos;
- autosserviço;
- integração eSocial.

## Critério de funcionamento

Uma tela não será considerada concluída apenas porque abre.

Para cada fluxo, a validação deve cobrir:

1. consulta dos dados necessários;
2. seleção por entidade, e não por ID solto quando houver cadastro;
3. criação/alteração;
4. consulta do resultado gravado;
5. tratamento de erro HTTP;
6. tenant correto;
7. permissões;
8. integração externa quando aplicável;
9. retorno/recusa da integração;
10. auditoria e histórico quando o processo exigir.

## Próxima frente de implementação

A próxima frente funcional é **PCP completo**, começando por:

**roteiro → operação → centro de trabalho → capacidade → programação → MRP → sugestão de OP → execução → apontamento → consumo → produção → refugo → custo**.

Depois, o mesmo critério será aplicado a **Fiscal e Comercial**, mantendo a distinção entre consulta interna e integração externa.
