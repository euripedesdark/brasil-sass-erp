# Teste de NFS-e a partir do cadastro

**Para fazer antes de mexer em Fiscal.**

## O que cadastrar

Um mesmo software, registrado nas duas pontas, porque a NFS-e nasce do serviço
e o produto é o que aparece no pedido:

1. **Serviço cadastrado** — "Licença de software" (venda de licença, não
   suporte). Precisa do `codigo_tributacao_municipal` preenchido, senão a
   prefeitura recusa.
2. **Produto** — o mesmo item como produto, para o ciclo comercial completo
   (pedido → faturamento → NFS-e).

## Por que a ordem importa

A emissão da NFS-e lê o serviço do cadastro, e não o produto. O
`codigo_tributacao_municipal` é o que define o código da prefeitura — o
`01.07` da LC 116 passa no XSD mas é inexistente na tabela municipal e é
recusado. Para a SrvCloud, suporte técnico é `2919`; licença de software é
outro código e precisa ser conferido na tabela da prefeitura.

Sem o serviço cadastrado, não há o que emitir. Por isso o cadastro vem antes
de qualquer tela de Fiscal.

## Estado atual

- Backend de NFS-e: emitido e cancelado 15 vezes, com XML guardado no MongoDB
  (5 anos) e PDF (60 dias)
- `bc_cad_servico` tem o serviço `SUP-001` (id 1) com código `2919`
- Produto: existe, mas o cadastro de produto **estava quebrado** (ver abaixo)

## Pendências que este teste destrava

O cadastro de produto e de pessoa tinham o mesmo bug: o `criar` não preenchia
`empresa_id`, e o banco recusava com violação de not-null devolvida como
**409 "Violação de integridade"** — que parece duplicidade, mas era falta do
tenant. Corrigido nos dois.

A varredura por leitura encontrou `ProdutoServiceImpl` como o único service com
o mesmo padrão, e ele também foi corrigido. Ainda vale procurar o mesmo padrão
nos módulos que faltam, pelo sintoma: 409 em qualquer `criar`.
