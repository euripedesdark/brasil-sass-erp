> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../README.pt-BR.md) e
> [`docs/INDICE.md`](INDICE.md).

# Mapa Funcional do Brasil SaaS ERP

## 🚀 Enterprise Transformation (Nova Arquitetura)

O sistema passou por uma transformação para nível Enterprise, focando em design profissional, governança corporativa e automação de processos industriais e financeiros.

### Core Enterprise
- **Frontend Moderno**: Interface em React com design translúcido, barra lateral expansível e experiência de usuário de alto padrão.
- **Governança e Permissões**: Hierarquia rígida de acesso (SuperAdmin $\rightarrow$ Diretoria $\rightarrow$ Gerente $\rightarrow$ Usuário) com controle granular de permissões.
- **Power Admin**: Console SQL integrado e Editor de Dados estilo Interbase para Super Admins.
- **UX Inteligente**: Painel de "Entradas Recentes" para edição rápida dos últimos 20 registros alterados no sistema.

### Novos Módulos e Automações
- **Produção Industrial**: Fluxo completo de Ordem de Produção (Insumos → Processo → Produto Final) com suporte a pesos, medidas e densidades. ✅ IMPLEMENTADO
- **Monetização de Equipe**:
    - **Comissões de Vendas**: Cálculo automático de comissão no momento do faturamento do pedido. ✅ IMPLEMENTADO
    - **Mão de Obra Técnica**: Cálculo de remuneração para técnicos/prestadores no fechamento de OS (baseado em horas trabalhadas ou percentual de serviço). ✅ IMPLEMENTADO
- **Gestão de Assets**: Armazenamento dinâmico de imagens de sistema (Login/Background) via MongoDB. ✅ IMPLEMENTADO
- **Novos Cadastros**: Categorias, Marcas, Unidades de Medida, Transportadoras com campo de status. ✅ IMPLEMENTADO
- **Entrada de Notas Fiscais**: Módulo de entrada de notas com integração ao estoque. ✅ IMPLEMENTADO
- **Hierarquia Corporativa**: Níveis de hierarquia em perfis (Diretoria, Gerente, Usuário). ✅ IMPLEMENTADO (V45-V46)
    - **Mão de Obra Técnica**: Cálculo de remuneração para técnicos/prestadores no fechamento de OS (baseado em horas trabalhadas ou percentual de serviço). ✅ IMPLEMENTADO
- **Gestão de Assets**: Armazenamento dinâmico de imagens de sistema (Login/Background) via MongoDB. ✅ IMPLEMENTADO
- **Novos Cadastros**: Categorias, Marcas, Unidades de Medida, Transportadoras com campo de status. ✅ IMPLEMENTADO
- **Entrada de Notas Fiscais**: Módulo de entrada de notas com integração ao estoque. ✅ IMPLEMENTADO
- **Hierarquia Corporativa**: Níveis de hierarquia em perfis (Diretoria, Gerente, Usuário). ✅ IMPLEMENTADO (V45-V46)
### Novos Módulos e Automações
- **Produção Industrial**: Fluxo completo de Ordem de Produção (Insumos → Processo → Produto Final) com suporte a pesos, medidas e densidades. ✅ IMPLEMENTADO
- **Monetização de Equipe**:
    - **Comissões de Vendas**: Cálculo automático de comissão no momento do faturamento do pedido. ✅ IMPLEMENTADO
    - **Mão de Obra Técnica**: Cálculo de remuneração para técnicos/prestadores no fechamento de OS (baseado em horas trabalhadas ou percentual de serviço). ✅ IMPLEMENTADO
- **Gestão de Assets**: Armazenamento dinâmico de imagens de sistema (Login/Background) via MongoDB. ✅ IMPLEMENTADO
- **Novos Cadastros**: Categorias, Marcas, Unidades de Medida, Transportadoras com campo de status. ✅ IMPLEMENTADO
- **Entrada de Notas Fiscais**: Módulo de entrada de notas com integração ao estoque. ✅ IMPLEMENTADO
- **Hierarquia Corporativa**: Níveis de hierarquia em perfis (Diretoria, Gerente, Usuário). ✅ IMPLEMENTADO (V45-V46)
### Novos Módulos e Automações
- **Produção Industrial**: Fluxo completo de Ordem de Produção (Insumos → Processo → Produto Final) com suporte a pesos, medidas e densidades. ✅ IMPLEMENTADO
- **Monetização de Equipe**:
    - **Comissões de Vendas**: Cálculo automático de comissão no momento do faturamento do pedido. ✅ IMPLEMENTADO
    - **Mão de Obra Técnica**: Cálculo de remuneração para técnicos/prestadores no fechamento de OS (baseado em horas trabalhadas ou percentual de serviço). ✅ IMPLEMENTADO
- **Gestão de Assets**: Armazenamento dinâmico de imagens de sistema (Login/Background) via MongoDB. ✅ IMPLEMENTADO
- **Novos Cadastros**: Categorias, Marcas, Unidades de Medida, Transportadoras com campo de status. ✅ IMPLEMENTADO
- **Entrada de Notas Fiscais**: Módulo de entrada de notas com integração ao estoque. ✅ IMPLEMENTADO
- **Hierarquia Corporativa**: Níveis de hierarquia em perfis (Diretoria, Gerente, Usuário). ✅ IMPLEMENTADO (V45-V46)
### Novos Módulos e Automações
- **Produção Industrial**: Fluxo completo de Ordem de Produção (Insumos → Processo → Produto Final) com suporte a pesos, medidas e densidades. ✅ IMPLEMENTADO
- **Monetização de Equipe**:
    - **Comissões de Vendas**: Cálculo automático de comissão no momento do faturamento do pedido. ✅ IMPLEMENTADO
    - **Mão de Obra Técnica**: Cálculo de remuneração para técnicos/prestadores no fechamento de OS (baseado em horas trabalhadas ou percentual de serviço). ✅ IMPLEMENTADO
- **Gestão de Assets**: Armazenamento dinâmico de imagens de sistema (Login/Background) via MongoDB. ✅ IMPLEMENTADO
- **Novos Cadastros**: Categorias, Marcas, Unidades de Medida, Transportadoras com campo de status. ✅ IMPLEMENTADO
- **Entrada de Notas Fiscais**: Módulo de entrada de notas com integração ao estoque. ✅ IMPLEMENTADO
- **Hierarquia Corporativa**: Níveis de hierarquia em perfis (Diretoria, Gerente, Usuário). ✅ IMPLEMENTADO (V45-V46)
---

## Visao geral (Legado/Base)


- Entrada web: paginas Thymeleaf servidas por `ViewsController`.
- API: endpoints REST sob `/api`.
- Persistencia: tabelas PostgreSQL legadas, com acesso misto por JPA e SQL via `JdbcTemplate`.
- Autenticacao: endpoints de login/logout e filtro web de autenticacao.
- Documentos: importacao XML/PDF de nota fiscal, geracao de relatorios PDF e emissao simulada de nota.
- Recursos: os templates e arquivos estaticos em `src/main/resources` fazem parte do runtime e nao devem ser movidos sem validacao.

## Telas web

As telas abaixo sao retornadas por `ViewsController`:

| Rota | Tela | Funcao |
|---|---|---|
| `/`, `/login` | login | Entrada e autenticacao do usuario |
| `/inicio` | inicio | Painel inicial |
| `/financeiro` | financeiro | Menu financeiro |
| `/financeiro/lancamentos` | lan | Contas a pagar e receber |
| `/financeiro/baixa` | baixa | Baixa de titulos |
| `/financeiro/recibos` | recibo | Emissao e consulta de recibos |
| `/financeiro/extrato` | extrato | Extrato por conta caixa |
| `/cadastros` | cadastros | Menu de cadastros |
| `/cadastros/cfo` | cfo | Pessoas fisicas e juridicas |
| `/cadastros/produtos` | produtos | Produtos, importacao e variacoes |
| `/cadastros/centro-custo` | centro_custo | Centros de custo |
| `/cadastros/caixa` | caixa | Contas caixa |
| `/cadastros/tipos-pagamento` | tipo_pagamento | Tipos de pagamento |
| `/cadastros/usuarios` | usuario | Usuarios |
| `/cadastros/empresas` | empresa | Empresas e vinculos |
| `/cadastros/funcionarios` | funcionario | Funcionarios |
| `/cadastros/documentos` | documento | Tipos de documento |
| `/cadastros/condicoes` | condicao | Condicoes de pagamento |
| `/cadastros/municipios` | municipio | Municipios |
| `/movimentos` | movimentos | Menu de movimentacao |
| `/movimentos/os` | os | Ordens de servico |
| `/movimentos/vendas` | vendas | Vendas |
| `/fechamento` | fechamento | Fechamento e notas pendentes |
| `/relatorios` | relatorios | Relatorios |

## API e comportamentos

### Autenticacao

- `POST /api/auth/login`: valida usuario e inicia autenticacao.
- `POST /api/auth/logout`: encerra a sessao/token.
- `AuthFilter`: protege requisicoes conforme a autenticacao configurada.

### Cadastros auxiliares

- Caixas: listar, salvar e excluir por `/api/auxiliares/caixas`, `/api/caixas` e `/api/base/caixas`.
- Centros de custo: listar, salvar e excluir por `/api/auxiliares/centrocusto` e `/api/custos`.
- Tipos de pagamento: listar, salvar e excluir por `/api/auxiliares/tipos-pagamento` e `/api/tipos-pagamento`.
- Tipos de documento: listar, salvar e excluir por `/api/auxiliares/documentos` e `/api/documentos`.
- Condicoes de pagamento: listar, salvar e excluir por `/api/auxiliares/condicoes` e `/api/condicoes`.
- Municipios: listar, salvar, consultar por codigo IBGE e buscar por `/api/auxiliares/municipios` e `/api/municipios`.
- Usuarios: CRUD em `/api/auxiliares/usuarios` e `/api/usuarios`.

### Cadastros base e pessoas

- Centros de custo e caixas: CRUD legado em `/api/base/custos` e `/api/base/caixas`.
- Pessoas: CRUD em `/api/pessoas`.
- Clientes e fornecedores legados: listar e salvar em `/api/cfo-legado`.
- Funcionarios legados: listar e salvar em `/api/funcionarios-legado`.
- CEP: consulta em `/api/cep/{cep}`.
- Empresas: vincular, listar vinculos e excluir vinculo em `/api/empresas`.
- Produtos legados: listar e salvar em `/api/produtos-legado`.
- NCM: busca em `/api/ncms/buscar`, `/api/ncm/buscar`, `/api/ncms` e `/api/ncm`.

### Produtos, estoque e notas

- `GET /api/produtos`: lista produtos, precos, NCM, valor de servico e estoque agregado.
- `POST /api/produtos`: salva produto ou direciona para confirmacao de entrada conforme o payload.
- `POST /api/produtos/salvar` e `/api/produtos/novo`: cria ou atualiza produto.
- `DELETE /api/produtos/{id}`: exclui produto.
- `GET /api/produtos/{id}/variacoes`: lista variacoes.
- `POST /api/produtos/variacoes` e `/api/produtos/{id}/variacoes`: cria ou atualiza variacao.
- `DELETE /api/produtos/variacoes/{id}`: exclui variacao.
- Endpoints de importacao recebem XML/PDF multipart e extraem fornecedor e itens.
- Endpoints de confirmacao de entrada gravam fornecedor, produto e variacao no estoque.
- `GET /api/movimentos/pendentes-fechamento`: lista movimentos ainda sem nota.
- `POST /api/notas/emitir`: grava uma nota simulada e seus itens, com protocolo simulado.

### Movimentacao

- `POST /api/movimentos/os`: grava ordem de servico/movimento.
- `GET /api/movimentos`: lista movimentos.
- `/api/item-os-legado`: lista e salva itens de ordem de servico.
- `GET /api/extratos/{idCaixa}`: consulta extrato de uma conta caixa.

### Financeiro

- `/api/lancamentos` e `/api/lan_jpa`: listar lancamentos abertos/baixados, CRUD e exclusao.
- `/api/lan-legado/resumo`: retorna resumo financeiro legado.
- `/api/lan-legado/abertos`: consulta lancamentos financeiros abertos.
- `/api/lan-legado/baixados`: consulta lancamentos financeiros baixados.
- `/api/operacional/extenso`: converte valores para texto.
- `POST /api/operacional/baixar`: executa baixa financeira.
- `/api/recibos`: consulta e grava recibos.

### Relatorios e utilitarios

- `/api/relatorios/movimento/{id}/pdf`: gera PDF de movimento.
- `/api/relatorios/financeiro`: gera relatorio financeiro.
- `/api/relatorios/ficha-cfo`: gera ficha de cliente/fornecedor.
- `/api/relatorios-analiticos/resumo-periodo`: resumo analitico por periodo.
- `/api/relatorios-analiticos/anual-produtos`: relatorio anual por produto.
- `POST /api/utilitarios/calcular-area`: calcula area.
- `GET /api/utilitarios/converter-tempo`: converte tempo.

## Servicos internos

- `XmlNotaFiscalService`: interpreta XML de NF-e e extrai fornecedor, chave, emissao e produtos; tambem extrai texto basico de PDF.
- `PdfService`, `PdfRelatorioService` e `RelatorioPdfService`: geram documentos PDF.
- Servicos de cadastro: caixas, centros de custo, documentos, empresas, funcionarios, municipios e condicoes de pagamento.
- Servicos financeiros: lancamentos, baixas, fechamento e calculos de itens.
- `CalculadoraGeometricaService`, `ExtensoService` e `TempoService`: rotinas auxiliares.

## Modelo de dados principal

As entidades JPA e consultas JDBC usam tabelas PostgreSQL legadas. Principais tabelas:

- Cadastros: `fpessoa`, `fcfo`, `fempresa`, `ffuncionario`, `fusuario`, `tmunicipio`, `base_cep`.
- Produtos: `fproduto`, `fproduto_variacao`, `fproduto_fiscal`, `fproduto_imagem`, `fproduto_kit`, `fproduto_ecommerce`.
- Financeiro: `fcaixa`, `flan`, `ffinanceiro`, `fextrato`, `frecibo`, `ftipopagamento`, `fcondicao`.
- Movimentacao: `fmov`, `fmovimento`, `fmovimento_item`, `fitem`.
- Notas e fiscal: `fnota`, `fnota_item`, `fdocumento`, `tncm`, `tcnae_servico`, `tissqn`, `tservico_lc116`.
- Parametros e apoio: `gparametro`, `fdatas`, `fdia`, `fempresa_vinculo`.

A coluna `fproduto.valor_servico NUMERIC(15,2)` foi adicionada para suportar o valor de servico no cadastro e na API de produtos.

## Integracoes e arquivos operacionais

- PostgreSQL local configurado em `src/main/resources/application.properties`.
- Consulta externa de CEP pelo ViaCEP no frontend.
- Integracao de NF-e usa os servicos e schemas XML mantidos na pasta `schemas/`.
- Certificados e chaves operacionais ficam em `certs/`; nao devem ser expostos em logs ou documentacao.
- `installbase.sh` instala dependencias de ambiente, incluindo Oracle JDK 25 e Maven.
- `compilar.py` e `importa_ncm.py` apoiam compilacao e carga de NCM.
- `bin/` contem uma copia operacional/legada do projeto e nao e usado pelo Maven da raiz.

## Fluxo principal do usuario

1. Usuario acessa `/login` e autentica.
2. Navega pelos menus de cadastros, financeiro ou movimentacao.
3. Cadastra pessoas, produtos, contas caixa, condicoes e demais referencias.
4. Registra vendas/ordens de servico e movimentacoes financeiras.
5. Importa uma NF-e em XML/PDF quando necessario.
6. Confirma a entrada, atualiza estoque e variacoes.
7. Consulta extratos, baixa titulos e emite recibos.
8. Fecha movimentos e gera relatorios PDF.

## Limites conhecidos

- A emissao de nota fiscal atual e simulada; o protocolo e gerado localmente.
- Alguns endpoints possuem aliases legados para manter compatibilidade com telas antigas.
- O schema e legado e o Hibernate esta configurado para nao alterá-lo automaticamente.
- O mapa foi derivado do codigo e configuracoes presentes; comportamentos externos podem exigir validacao em ambiente real.
