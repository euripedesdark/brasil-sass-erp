# Padrão de UX e composição de telas — 2026-10-02

## Objetivo
Corrigir problemas de legibilidade, composição e usabilidade do frontend sem alterar o shell visual existente.

### O que permanece intocável
- imagem/tela de fundo;
- cor e identidade visual azul;
- barra lateral;
- barra superior;
- navegação principal;
- mecanismo de auto-size/responsividade do shell.

A intervenção ocorre na **superfície de conteúdo e nos formulários**.

## Regras adotadas
### 1. Contraste por superfície
A aplicação possui duas superfícies distintas:
- **superfície azul/vidro:** texto claro;
- **controle de entrada claro:** texto escuro.
Não é aceitável um label escuro sobre o fundo azul do Dialog. O texto do label deve permanecer legível mesmo quando a imagem de fundo estiver muito clara ou muito escura.

### 2. Formulários
Formulários usam uma grade de 12 colunas:
- campos relacionados ficam na mesma linha quando há espaço;
- campos longos ocupam a largura disponível;
- abaixo de 900 px a grade reduz a densidade;
- abaixo de 600 px passa a uma coluna;
- labels permanecem acima do campo;
- mensagens de validação ficam próximas ao campo;
- ações ficam agrupadas no rodapé.

A ideia é manter densidade de informação de ERP sem transformar a tela em uma sequência vertical de inputs.

### 3. Dialogs
Dialogs são portais do PrimeReact e, portanto, precisam de CSS global no `body`.
O tamanho deixa de possuir uma largura mínima fixa que prejudique notebooks e resoluções menores. A largura passa a ser fluida, com limite máximo e comportamento responsivo.
O conteúdo possui rolagem interna. O usuário não perde os controles do sistema atrás do modal.

### 4. Listas
Tabelas devem:
- ocupar a largura disponível;
- permitir rolagem horizontal somente quando necessária;
- ter mensagem explícita quando não há registros;
- manter busca/filtro próximos da tabela;
- evitar colunas com largura fixa excessiva;
- usar paginação do servidor quando o volume justificar.

## Referências utilizadas
A organização segue princípios de formulários empresariais: agrupamento semântico, redução de espaço vazio, grade responsiva, labels claramente associados aos campos e composição consistente.

Referências consultadas:
- SAP Design System — Form: https://www.sap.com/design-system/fiori-design-web/v1-145/ui-elements/form-web-component
- SAP Design System — Content Density: https://www.sap.com/design-system/fiori-design-web/v1-120/foundations/visual/cozy-compact
- SAP Design System — Modal Wizard: https://www.sap.com/design-system/cx-ux-best-practice-guide/patterns/modal-wizard
- PrimeReact — Dialog: https://primereact.org/dialog/

## Problemas corrigidos nesta rodada
- labels escuros em Dialogs azuis;
- largura mínima fixa de Dialog;
- composição vertical excessiva dos grids de formulário;
- falta de regra comum para inputs dentro de Dialog;
- falta de padrão de seção de formulário;
- comportamento inconsistente em resoluções menores;
- rota de PDV com JSX malformado em `App.jsx`.

## Regra para próximas telas
Nenhuma tela nova deve criar um CSS próprio para resolver problemas que já são transversais.

Antes de criar estilos específicos:
1. usar os utilitários `bc-*`;
2. usar a grade existente;
3. verificar contraste na superfície onde o componente realmente é renderizado;
4. testar 1920/1600/1366/1280/1024/768 px;
5. testar zoom do navegador;
6. validar navegação por teclado nos formulários.