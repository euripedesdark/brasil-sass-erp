---
id: 2026-09-26-datas-fiscais-que-venceram
status: confirmado
data: 2026-09-26
---

# Datas fiscais que já venceram ou estão chegando

Fatos datados, com a fonte de cada um. serve para decidir o que já é urgente.

Hoje: **26 de setembro de 2026**.

## CNPJ alfanumérico — VENCEU

| | |
|---|---|
| Homologação | 06/04/2026 |
| **Produção** | **06/07/2026** |
| Primeiros CNPJ | a partir de julho de 2026 |

Fonte: `NFSe-SaoPaulo-SP/` não tem isto. Vem da
[NT Conjunta 2025.001](https://www.nfe.fazenda.gov.br/), lida em
setembro/2026, e que **não está arquivada em pasta nenhuma**.

O que a NT diz:

- As 14 posições continuam. As 12 primeiras aceitam **letra maiúscula**
  (8 da raiz + 4 do número de ordem), as 2 últimas seguem numéricas.
- O DV não muda de estrutura, muda o valor do caractere: `ASCII - 48`, então
  `A = 17`, `B = 18`, … `Z = 42`.
- Para dígito numérico, `ASCII - 48` **é** o dígito. O cálculo novo é
  idêntico ao histórico para CNPJ numérico.
- A chave de acesso usa o mesmo `ASCII - 48`, com pesos de 9 a 2 em ciclo.
- A SEFAZ valida o DV pelo cálculo novo desde a produção.

Letras que **não** devem ser aceitas: I, O, U, Q, F. A própria NT diz que a
exclusão *"precisa ser confirmada"* pela Receita. Por isso não se barra
nenhuma delas.

**Estado do ERP: não aceita CNPJ alfanumérico.** Quatro camadas, cada uma
precisando ser corrigida antes da próxima aparecer:

| Camada | Arquivo | O que faz |
|---|---|---|
| máscara do formulário | `CadastroPessoas.jsx` | `InputMask mask="99.999.999/9999-99"` — `9` só aceita dígito |
| validação do formulário | `CadastroPessoas.jsx` | `replace(/\D/g,'')` apaga a letra |
| bean validation | `PessoaDtos.java` | `@Pattern(regexp = "\\d{14}")` recusa |
| gravação | `EmpresaDoUsuarioService` | `digitos()` → `replaceAll("\\D","")` |

A última é a grave: `12ABC34501DE35` vira `123450135` e **passa** no teste de
tamanho. Grava o CNPJ de outra empresa sem erro em lugar nenhum.

`ConfigurarEmpresa.jsx` tem o mesmo par máscara + validação. `Nfse.jsx` e
`NfseEmissaoService.somenteDigitos()` também, o que faria a prefeitura recusar
a nota por documento do tomador inválido.

O banco não precisa migrar: `varchar(14)` guarda 14 caracteres, alfanumérico ou
não, e não há `CHECK` nem trigger.

## CIOT no MDF-e — VENCE EM 2 MESES

| | |
|---|---|
| Homologação | **21/09/2026** (ativa há 5 dias) |
| **Produção** | **23/11/2026** |

Fonte: `PL_MDFe_300b_NT012025_1.05/MDFe_Nota_Tecnica_2026_001.pdf`, v1.00 de
maio de 2026.

**Rejeição 684** — CIOT é obrigatório quando:

- `modal = 1` (rodoviário), **e**
- `tpEmit = 1` (prestador de serviço de transporte por conta de terceiros) ou
  `tpEmit = 3` (transportador que emite CT-e globalizado), ou
  `tpEmit = 2` com a tag `tpTransp` informada.

A regra vem do Ajuste SINIEF nº 03 de 2026.

### O que o XSD diz — e o que não diz

Conferido nos 41 XSD do 3.00b:

| Afirmação | Medido |
|---|---|
| `infPag` (pagamento do frete) obrigatório para lotação | `minOccurs="0"` |
| `infLotacao` | `minOccurs="0"` |
| NCM predominante obrigatório | `minOccurs="0"` |
| Tipo novo de CNPJ para alfanumérico | **não existe** — só `TCnpj` e `TCnpjOpc` |
| IBS/CBS/IS | **zero ocorrências** nos 41 XSD |

O diff do 3.00a para o 3.00b tem só 4 mudanças: `tpEmis` ganhou `value="4"`
(PAA), o `CNPJ` do grupo de pagamento passou de `TCnpjOpc` para `TCnpj`, um
`xs:pattern` de `*` para `{0,}` (no-op), e espaços.

O `pattern` `[A-Z0-9]{12}[0-9]{2}` do CNPJ **já** existia no 3.00a — foi
publicado antes da NT.

**Conclusão:** o schema é permissivo de propósito. A obrigatoriedade é regra do
serviço de autorização da SVRS, não restrição de schema. Implementar contra o XSD
só não basta para o MDF-e ser autorizado.

O catálogo de rejeições — quais `cStat` exigem o quê — **não está em pasta
nenhuma** deste repositório.

## Códigos de serviço encerrados em 31/12/2025 — JÁ VENCEU

Fonte: `NFSe-SaoPaulo-SP/atribuicao-de-codigos.xlsx`, 30 códigos encerrados, cada
um com os substitutos. **8 deles estão** na tabela que a API usa
(`nfse-sp-api/codigos-servico-sp.json`, gerada em 25/09/2026, que não
incorporou a mudança):

| Encerrado | LC 116 | Substitutos |
|---|---|---|
| `1520` | 07.01 | 1521, 1522, 1523 |
| `1546` | 07.01 | 1547, 1548, 1549 |
| `1589` | 07.01 | 1590, 1591 |
| `1627` | 07.01 | 1628, 1629 |
| `5870` | 15.03 | 5909, 5910, 5911, 5912, 5913, 5914 |
| `2340` | 16.01 | 2341, 2342, 2343, 2344 |
| `2143` | 30.01 | 2144, 2145, 2146 |
| `8274` | 12.07 | 8275, 8276, 8277 |

O código tem 4 dígitos, então **passa no `pattern` do XSD**. A recusa só vem da
prefeitura, como `[1001] The 'CodigoServico' element is invalid - Pattern
constraint failed` — mensagem que fala de formato, não de código encerrado. A
pessoa procura formato e não acha.

O serviço `SUP-001` do cadastro usa `2919`, que **não** está na lista. O risco é
serviço novo cadastrado com um dos oito.

`alteracao_codigos.xlsx` tem 191 registros de vigência (início e término) que
também não foram incorporados.

## O proxy da NFS-e perdia o corpo das requisições

**Corrigido em 26/09/2026.** Bug, não prazo. Achado testando a emissão.

O proxy lia o corpo HTTP direto do socket e só sabia ler quando vinha
`Content-Length`. O ERP envia `Transfer-Encoding: chunked`, então o corpo era
descartado e a API recebia POST vazio:

```
{"erro":"Falha inesperada: Required request body is missing: public
org.springframework.http.ResponseEntity<?> ...NfseSpController.emitirRps(...)"}
```

**Nenhuma emissão de NFS-e funcionava pelo ERP na configuração padrão**, porque
`brasil-saas.fiscal.nfse.url` tem como padrão a 4567, que era o proxy.

Trocado pelo nginx. Serviços de **3 para 2**. Ver
`docs/pesquisa/proxy-nginx-e-bug-do-fallback.md`.

## O fallback emitia a nota e o ERP acusava falha

**Corrigido em 26/09/2026.** Bug, não prazo. Achado testando o fallback.

Com a API Java fora, o bridge Ruby emitia a nota com `success=true` e a
prefeitura aceitava. O ERP lia `sucesso` no nível de cima, não achava — o Ruby
embrulha em `error` e escreve em inglês — e respondia *"A prefeitura não
confirmou a emissão"*, marcando `FALHA_EMISSAO` **sem guardar número, código de
verificação nem chave**.

O código dizia à pessoa *"Nenhuma nota foi emitida — pode tentar de novo"*. Ela
reemitia, e a prefeitura criava a segunda.

Produziu as notas **29** e **30**, que existiram na prefeitura sem registro
utilizável no ERP. As duas foram **canceladas** em 26/09/2026.

A correção foi contrato único nas duas implementações, e um terceiro estado no
leitor — "não deu para saber" — que manda conferir em vez de dizer que não
emitiu. Ver `docs/pesquisa/contrato-nfse-unico.md`.

## O que fazer por ordem de urgência

1. **CNPJ alfanumérico** — já está em produção desde julho. Sem isso, nenhum
   cliente ou fornecedor novo com CNPJ alfanumérico pode ser cadastrado.
2. **Códigos encerrados** — 8 de 304 que a API aceita não existem mais. Barato:
   é uma lista.
3. **Teste que compare as duas APIs da NFS-e** — a divergência de contrato
   custou duas notas órfãs, e nada impede que ela volte no próximo commit
4. **CIOT no MDF-e** — homologação já valendo, produção em novembro. Só importa
   se houver MDF-e; não há MDF-e no ERP hoje (`Java_MDFe` está vazia)

## O que precisa ser perguntado a quem decide

- **Data de corte do padrão fiscal** para o NFS-e. Sem isso, qualquer seletor de
  "padrão atual" ou "padrão reforma" no ERP é chute.
- **MDF-e entra no escopo agora?** Se entra, CIOT é o primeiro requisito, com
  prazo em novembro.
- **eSocial roda como serviço ou entra no ERP?** O material é do TST, não do ERP.
- **NFS-e nacional**: o fork das ~5 classes com problema, ou wrapper Spring em
  volta do cliente?

## O que não está arquivado aqui

A **NT Conjunta 2025.001 do CNPJ alfanumérico** não está em pasta nenhuma do
repositório. Foi lida na web em setembro/2026. Se a referência importa, ela
precisa ser baixada e guardada junto com `PL_MDFe_300b_NT012025_1.05/` — que é
como as notas do MDF-e estão.
