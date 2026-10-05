# Auditoria de telas — 2026-10-05

## Resultado do inventário do frontend

O frontend real está em `src/main/resources/static/react`.

No `main` desta auditoria:
- **137 arquivos JSX** em `src/components`;
- **119 componentes** são importados diretamente pelo `App.jsx`;
- `App.jsx` possui **127 declarações de rota**;
- `Layout.jsx` possui **112 caminhos de navegação** detectáveis;
- não foram encontrados caminhos de menu que não tenham uma rota correspondente, exceto os endpoints usados internamente e as rotas curingas `/vendas`, `/compras` e `/estoque`, que são cobertas por rotas com `/*`.

## Componentes não registrados diretamente no App

A diferença de 137 para 119 **não significa 18 telas faltando**. Há componentes de infraestrutura, logos, widgets e componentes legados.

Entre os arquivos não importados diretamente pelo `App.jsx`:
- `IaAssistWidget.jsx` — widget auxiliar;
- `RecentUpdates.jsx` — usado pelo `Layout`;
- `ModuloSelector.jsx` — componente auxiliar;
- `ClienteLogo.jsx`, `FornecedorLogo.jsx`, `EmpresaLogo.jsx`, `Logos.jsx` — componentes de logo;
- `ExigeEmpresa.jsx`, `ImagemRegistro.jsx`, `ImagensProduto.jsx`, `PrintButton.jsx`, `ErrorBoundary.jsx`, `I18nDomBridge.jsx`, `ApoiePix.jsx` — infraestrutura/auxiliares;
- `ImportarNotaXml.jsx` — **modal de importação**, chamado pelo fluxo de entrada de NF-e, não uma rota independente;
- `Financeiro.jsx` e `Fiscal.jsx` — telas/hubs legados substituídos pelos hubs atuais;
- `RegrasComissao.jsx` — **tela funcional que estava sem rota própria**.

## Correção desta auditoria

`RegrasComissao.jsx` agora está disponível em:

`/financeiro/regras-comissao`

e foi adicionada ao menu Financeiro.

Também foram expostas diretamente no menu Fiscal as telas que já existiam e estavam acessíveis apenas por hub/rota:
- NF-e;
- NFS-e;
- Certificados digitais;
- SPED/EFD.

## Sobre o erro `slice is not a function`

Esse erro é separado da ausência de telas.

A proteção `SafeDataTable` foi adicionada no PR anterior para normalizar respostas de API antes do PrimeReact DataTable. **Ela está no GitHub, mas só entra no navegador depois que o servidor recompila e publica o frontend.**

Portanto, um navegador que ainda carregue o bundle antigo continua podendo apresentar:

`TypeError: U.slice is not a function`.

A validação final desse erro precisa ser feita no bundle efetivamente servido por `http://192.168.2.10/`, não apenas no código-fonte do GitHub.

## Próxima etapa

A contagem de JSX/rotas não deve ser usada como sinônimo de cobertura funcional. A auditoria correta é:

`endpoint → service → persistência → tela → menu/rota → operação → consulta → erro → integração externa`.

A próxima varredura deve usar o OpenAPI real publicado pelo backend e marcar somente os endpoints ainda sem fluxo de tela, descartando endpoints internos, autenticação, callbacks e APIs consumidas por modais/widgets.