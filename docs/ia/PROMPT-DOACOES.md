> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Prompt de integração — donations no sistema

Para o agente tradutor. Repositório: `BrasilCloudERP`.
O servidor web não deve ser tocado durante a execução deste prompt.
O frontend já está compilado e no ar com estas funcionalidades; o objetivo
aqui é levar o mesmo resultado ao git, sem reimplementar do zero.

---

## Contexto medido

A chave PIX do projeto, e o valor único que aparece em todo o código:

```
24adc62c-b073-4587-974d-03fe35f6733f
```

Ela já está em dois lugares do repositório:

| arquivo | estado |
|---|---|
| `README.md`, seção `## ❤️ Apoie o Projeto` | presente, com a chave e a tabela do Wise |
| `.github/FUNDING.yml` | presente, `custom: ["pix:24adc62c..."]` |

O que falta no git é o frontend. As dependências e os arquivos-fonte **existem
em disco no servidor** e estão prontos para serem commitados — não reescreva.

---

## Arquivos já prontos em disco, para commit

```
M  README.md
?? .github/FUNDING.yml
M  src/main/resources/static/react/package.json          (+ qrcode.react)
M  src/main/resources/static/react/package-lock.json
?? src/main/resources/static/react/src/components/shared/ApoiePix.jsx
?? src/main/resources/static/react/src/components/shared/ApoiePix.css
?? src/main/resources/static/react/src/components/core/Sobre.jsx
?? src/main/resources/static/react/src/components/core/Sobre.css
M  src/main/resources/static/react/src/App.jsx            (rota /sobre)
M  src/main/resources/static/react/src/components/Layout.jsx  (rodapé)
M  src/main/resources/static/react/src/components/Login.jsx   (botão + dialog)
M  src/main/resources/static/react/src/components/Login.css
M  src/main/resources/static/react/src/locales/{pt-BR,en-US,es-ES,fr-FR}.json
```

Confirme o estado com `git status` antes. Commite arquivo por arquivo, nunca
`-A`, nunca `stash`, nunca `reset`.

---

## Regras de UI que já valem — não reverta

Estas três foram decididas na implementação e corrigidas depois de measure.
Elas estão no código pronto:

**1. Chave escrita no rodapé e na página `/sobre`. Dialog do login só QRCode.**
O dialog de_support da tela de login mostra apenas o QRCode. A chave por escrito
fica no rodapé do sistema e na página `/sobre`. Não adicione a chave de volta
dentro do dialog.

**2. QRCode precisa sair quadrado.**
`.pix-qrcode` usa `display: inline-block` e o wrapper recebe `width`/`height`
fixos em `style`. O `<canvas>` do `qrcode.react` se estica até preencher o
container e sai deformado, e um QRCode deformado não é lido por app de banco.
Preserve o `display: block` no `.pix-qrcode canvas`.

**3. O botão "Copiar Chave PIX" precisa lidar com HTTP sem secure context.**
`navigator.clipboard` só existe em HTTPS ou localhost. Rodando em HTTP num IP da
rede, `writeText` não existe e o clique parece funcionar sem copiar. O
`useCopiarPix` no `ApoiePix.jsx` já trata isso com `try/catch` e a chave está
também em `<code class="pix-chave">` com `user-select: all`, para copiar à mão.

---

## Dependência

```
qrcode.react  4.2.0   licença ISC
```

Já instalada. Uma linha no `package.json`. Não substitua por encoder próprio:
QRCode usa correção de Reed-Solomon e um encoder caseiro erra o código.

As 3 vulnerabilidades que o `npm audit` acusa são de `esbuild` e `quill`,
**pré-existentes** e fora do escopo deste trabalho. Não rode `npm audit fix --force`.

---

## Os quatro idiomas

O projeto é multilíngue e o fallback é `pt-BR`. Toda chave nova precisa existir
nos quatro arquivos de `src/locales/`, senão a tela mostra o nome da chave.

Já prontos: `nav.aboutProject`, `footer.*` (4 chaves), `donate.*` (3 chaves) e
`about.*` (20 chaves, com `roadmapNowItems`, `roadmapWipItems` e
`roadmapTodoItems` como arrays).

Os itens do roadmap são **propositalmente iguais nos quatro idiomas**: eles
descrevem o que existe no código. Traduzir os rótulos é obrigatório; inventar um
item só para um idioma, não.

Ao adicionar qualquer texto novo, escreva nos quatro. Um sistema que fala quatro
línguas e mostra português em três delas não avisa o usuário.

---

## O que já está funcionando, verificado em tela

| item | onde |
|---|---|
| botão "Apoiar Projeto" com dialog e QRCode | tela de login |
| rodapé com chave e botão copiar | todas as telas autenticadas |
| página `/sobre` com licença, história, roadmap, tecnologias, créditos e apoio | rota `/sobre`, item "Sobre o Projeto" no menu |

Ao publicar, `npm run build` precisa sair limpo e o `mvn package` precisa
retornar `BUILD SUCCESS`. O jar em produção fica em
`target/brasil-saas-erp-1.0.0-SNAPSHOT.jar` e o serviço é
`brasil-saas-erp`.

---

## Fora do escopo

Não mexa em: banco de dados, Active Directory, Auth Service,
`ModuloAcessoService`, `SQL_AUTHORITIES`, `pg_hba.conf`, `pg_ident.conf`.

A tabela `bc_core_usuario_perfil` não é fonte de autorização e não deve ser
alterada. A autorização vem dos grupos do AD pelos 9 pares mapeados em
`CustomUserDetailsService.rolesDosGrupos`: `GRP_DIRETORIA`→`ROLE_DIRETORIA`,
`GRP_GERENTE`→`ROLE_GERENTE`, `GRP_GESTOR`→`ROLE_GESTOR`,
`GRP_FINANCEIRO`→`ROLE_FINANCEIRO`, `GRP_ESTOQUE`→`ROLE_ESTOQUE`,
`GRP_RH`→`ROLE_RH`, `GRP_VENDEDOR`→`ROLE_VENDEDOR`,
`GRP_ERP_ADMIN`→`ROLE_ADMIN`, `GRP_SUPERUSER`→`ROLE_SUPERUSER`.

Grupo fora dessa lista não vira authority. O grupo universal do domínio,
`srvcloud`, chega para toda conta: se ele virar `ROLE_SRVCLOUD`, alguém ganha um
acesso que ninguém concedeu. Por isso o mapeamento é enumerado e não derivado.
