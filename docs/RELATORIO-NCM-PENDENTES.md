---
id: 2026-09-30-relatorio-ncm-pendentes
status: decisao pendente do dono
data: 2026-09-30
---

# NCM PENDENTES: 18 AMBIGUOS E 4 INEXISTENTES

> Relatório para decisão fiscal. **Nada foi escolhido, corrigido ou inferido.**
> Nenhum valor deste documento é sugestão: é o que a tabela oficial e o cadastro
> dizem, e o que deixa de funcionar por causa disso.

---

## 1. RESUMO

| | |
|---|---|
| Produtos com NCM de 6 dígitos | 22 |
| **NCM ambíguos** (existe mais de um código de 8) | **12 NCMs · 18 produtos** |
| **NCM inexistentes** (zero código de 8 com esse prefixo) | **2 NCMs · 4 produtos** |
| **Produtos que hoje resolvem alguma regra tributária** | **0 de 22** |
| A tabela oficial `bc_fis_ncm` tem | 10.515 códigos, **todos de 8 dígitos** |

O ponto que resume os dois problemas: **a tabela oficial só tem 8 dígitos**, e
todos os 22 produtos estão com 6 ou 7. A diferença entre "ambíguo" e "inexistente"
é se existe **algum** código de 8 com aquele prefixo, não se o código está certo.

---

## 2. OS 18 AMBIGUOS

| Produto | ID | Empresa | NCM atual | Candidatos | Famílias | Motivo da ambiguidade |
|---|---|---|---|---|---|---|
| Peito de Frango Resfriado | 10273 | 236 | `0207.14` | **14** | 3 | A posição de 6 dígitos abre para 14 códigos de 8 |
| Leite Integral 1L | 10271 | 236 | `0401.10` | **2** | 2 | 2 códigos de 8 com o mesmo prefixo |
| Feijão Carioca 1kg | 10267 | 236 | `0713.33` | **6** | 3 | 6 códigos de 8 |
| Óleo de Soja 900ml | 10268 | 236 | `1507.90` | **3** | 2 | 3 códigos de 8 |
| Óleo de Soja 900ml (atacado) | 10301 | 238 | `1507.90` | **3** | 2 | mesmo NCM do item anterior |
| Pão Francês | 10286 | 236 | `1905.20` | **2** | 2 | 2 códigos de 8 |
| Porção de Pão de Queijo | 10305 | 239 | `1905.20` | **2** | 2 | mesmo NCM do anterior |
| Porção de Caldo | 10308 | 239 | `2106.90` | **8** | 7 | 8 códigos de 8, em 7 famílias |
| Amoxicilina 500mg 21cp | 10272 | 237 | `3004.10` | **7** | 2 | 7 códigos de 8 |
| Dipirona 500mg 20cp | 10279 | 237 | `3004.90` | **68** | 8 | 68 códigos de 8 — o maior caso |
| Ibuprofeno 400mg 20cp | 10281 | 237 | `3004.90` | **68** | 8 | idem |
| Loratadina 10mg 12cp | 10285 | 237 | `3004.90` | **68** | 8 | idem |
| Losartana 50mg 30cp | 10282 | 237 | `3004.90` | **68** | 8 | idem |
| Metformina 850mg 30cp | 10283 | 237 | `3004.90` | **68** | 8 | idem |
| Omeprazol 20mg 28cp | 10284 | 237 | `3004.90` | **68** | 8 | idem |
| Paracetamol 750mg 20cp | 10280 | 237 | `3004.90` | **68** | 8 | idem |
| Protetor Solar FPS 60 | 10275 | 237 | `3304.99` | **2** | 2 | 2 códigos de 8 |
| Sabão em Pó 1kg | 10297 | 238 | `3401.11` | **2** | 2 | 2 códigos de 8 |

### O caso `3004.90`, que é o grande

68 candidatos em 8 famílias. Cada família de 7 dígitos tem entre 6 e 9 códigos de
8, e **cada medicamento tem o seu** — é por isso que a ambiguidade aqui não se
resolve por aproximação:

| Família | Códigos de 8 | Primeiro da família |
|---|---|---|
| `3004901` | 6 | `30049011` |
| `3004902` | 9 | `30049021` |
| `3004903` | 9 | `30049031` |
| `3004904` | 9 | `30049041` |
| `3004905` | 8 | `30049051` |
| `3004906` | 9 | `30049061` |
| `3004907` | 9 | `30049071` |
| `3004909` | 9 | `30049091` |

**São 7 medicamentos e 68 códigos.** A escolha é por produto, e é fiscal.

### Os candidatos, para consultar

```sql
select codigo, descricao
  from brasil_saas.bc_fis_ncm
 where codigo like replace('3004.90', '.', '') || '%'
 order by codigo;
```

O mesmo para qualquer outro NCM da tabela acima, trocando o literal.

---

## 3. OS 4 INEXISTENTES

| Produto | ID | Empresa | NCM atual | Dígitos | Candidatos | Motivo |
|---|---|---|---|---|---|---|
| Arroz Tipo 1 5kg | 10266 | 236 | `1905.21` | 7 | **0** | Não existe nenhum código de 8 com o prefixo `190521` |
| Arroz Tipo 1 5kg (atacado) | 10300 | 238 | `1905.21` | 7 | **0** | idem |
| Detergente Concentrado 5L | 10296 | 238 | `3402.20` | 7 | **0** | Não existe nenhum código de 8 com o prefixo `340220` |
| Detergente Neutro 500ml | 10289 | 236 | `3402.20` | 7 | **0** | idem |

Aqui **não há ambiguidade: não há nada para escolher.** A posição não existe na
tabela oficial. Isto é diferente do caso anterior — não é "qual dos 68", é
"nenhum".

### O que existe nos capítulos, para contextualizar — **sem sugestão**

O capítulo tem posições, mas não a que está cadastrada. Isso é fato de estrutura
do NCM, e a escolha é sua:

| Prefixo cadastrado | Posições de 6 dígitos que existem no mesmo capítulo |
|---|---|
| `1905.21` | `190510` (1), `190520` (2), `190531` (1), `190532` (1), `190540` (1), `190590` (3) — **`190521` não está na lista** |
| `3402.20` | `340231` (1), `340239` (4), `340241` (2), `340242` (1) — **`340220` não está na lista** |

Os capítulos 19 e 34 têm 9 e 19 códigos, respectivamente. Nenhum dos dois
prefixos cadastrados existe entre eles.

---

## 4. IMPACTO FISCAL ESPERADO

O que segue é o que **consegui medir**. O que depende da decisão de vocês está
marcado como não estimado, e não vai como número.

### 4.1 O que já está quebrado, e foi medido

| | |
|---|---|
| Produtos que resolvem alguma regra tributária | **0 de 22** |
| Motivo | `bc_fis_regra_tributaria` é indexada pelo NCM exato de 8 dígitos, e nenhum dos 22 tem um |

Ou seja: **hoje nenhum desses 22 produtos tem alíquota de IPI.** Não é alíquota
errada — é alíquota ausente. A venda funciona; a tributação não tem o que usar.

### 4.2 O que NÃO é afetado

| | |
|---|---|
| A venda | `vendas/` não tem nenhuma linha de tributação. Os 22 produtos vendem normalmente |
| O estoque | não passa por NCM |
| O PDV | idem |

Os 22 produtos **não travam nada na operação**. O que para é a emissão de
documento fiscal, e apenas no item.

### 4.3 O que NÃO foi estimado, e por quê

Estes números **não** são mensuráveis antes da decisão, e eu não os chutei:

| | Por que não |
|---|---|
| Qual **CST** de ICMS | o CST é escolhido pela situação fiscal da operação, não derivado do NCM. A própria OCA modela CST como campo da nota, sem ligação com NCM |
| Qual **alíquota de ICMS** | é definida por lei complementar estadual, e a incidência por NCM é **por UF**. Não existe tabela nacional |
| Qual **CST de PIS/COFINS** e alíquotas | dependem do regime tributário da empresa |
| Qual **alíquota de ST** | depende de convênios estaduais e do par NCM/CEST |
| **Diferença** entre escolher certo e escolher errado | depende do ICMS do estado de destino da venda, que não está em lugar nenhum do sistema |

### 4.4 O que a decisão destrava

Medido, não estimado:

| | |
|---|---|
| Produtos que passam a resolver regra | **22** (de 0) |
| IPI | resolvido para os que o tiverem — 43 alíquotas oficiais já carregadas na `V108` |
| Item fiscal em NF-e/NFC-e | o NCM, o CFOP e o CST entram no item; hoje nenhum dos 22 tem |
| `RegraTributariaService` | passa a ter o que resolver |

ICMS, PIS, COFINS e ST continuam pendentes **depois** desta decisão, e pela mesma
razão de antes: não têm fonte no sistema.

---

## 5. O QUE ESTE RELATÓRIO NÃO FAZ

Não escolhe NCM. Não corrige cadastro. Não infere classificação a partir do nome
do produto. Não estima alíquota. Não inventa regra.

Os 7 medicamentos com 68 candidatos, e os 2 NCMs que não existem, são **decisão
fiscal de vocês**. Este relatório só diz o que existe e o que falta.

---

## 6. COMO VERIFICAR

```bash
Q() { sudo -u postgres psql -d brasil-saas "$@"; }

# os 18 ambíguos: existe mais de um código de 8 com o prefixo
Q -c "select p.nome, p.ncm, count(distinct f.codigo) candidatos
  from brasil_saas.bc_cad_produto p
  join brasil_saas.bc_fis_ncm f on f.codigo like replace(p.ncm,'.','') || '%'
 where p.deleted_at is null and p.ncm like '%.%'
 group by 1,2 order by 3 desc;"

# os 4 inexistentes: zero candidatos
Q -c "select p.nome, p.ncm, count(f.codigo) candidatos
  from brasil_saas.bc_cad_produto p
  left join brasil_saas.bc_fis_ncm f on f.codigo like replace(p.ncm,'.','') || '%'
 where p.deleted_at is null and p.ncm like '%.%'
 group by 1,2 having count(f.codigo) = 0;"

# o impacto: quantos resolvem regra
Q -c "select count(*) produtos, count(r.id) com_regra
  from brasil_saas.bc_cad_produto p
  left join brasil_saas.bc_fis_regra_tributaria r
    on r.ncm = p.ncm and r.empresa_id = p.empresa_id
 where p.deleted_at is null and p.ncm like '%.%';"
```

