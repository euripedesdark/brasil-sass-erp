# 🚀 Brasil SaaS ERP — Sistema de Gestión Empresarial

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](LICENSE.md)

**ERP Enterprise multiempresa con IA asistiva, cobertura fiscal completa y arquitectura modular**

Creado por: **Euripedes Batista de Paiva Junior**

Criado por: **Euripedes Batista de Paiva Junior**

---

## 📖 Sobre el Proyecto

**Brasil SaaS ERP** es un sistema de gestión empresarial completo, desarrollado para atender empresas de todos los tamaños con una solución robusta, segura y escalable. Migrado de Delphi a **Java Spring Boot 3.3.5** con **Oracle JDK 21**, el sistema ofrecece módulos financieros, fiscales, de inventario, servicios y ventas con alto rendimiento en el tratamiento de grandes volúmenes de datos.

### ✨ Destacados Enterprise

| Característica | Descripción |
|---------|-----------|
| 🏎️ **Alto Rendimiento** | Migración de WebFlux a Spring MVC tradicional, resolviendo la lentitud en tablas de más de 400 MB |
| 📊 **Paginación Inteligente** | Carga de registros bajo demanda, ideal para bases de datos masivas |
| 🎨 **Frontend Moderno** | Interfaz reactiva con PrimeReact 10.8 y React 19 |
| 📝 **Informes Profesionales** | Generación de PDF con diseño moderno usando OpenPDF 3.0.5 |
| 🌐 **API RESTful** | Backend robusto con endpoints paginados y filtros avanzados |
| 🔒 **Seguridad Multi-tenant** | Autenticación JWT, autorización por permiso y aislamiento multiempresa |
| 📦 **Almacenamiento Híbrido** | PostgreSQL (datos relacionales) + MongoDB (binarios/imágenes) + MinIO (archivos grandes) |
| 🤖 **IA Asistiva** | Spring AI + OpenAI integrado para asistencia inteligente |
| 📡 **Observabilidad** | Actuator + Micrometer + Logback con MDC para rastreo de extremo a extremo |
| 🚛 **Documentos de Transporte** | NFS-e SP, MDF-e y CT-e conectados a SEFAZ; generación de archivo SPED EFD ICMS/IPI |
| 🧾 **Contrato único de respuesta** | Un único formato para que el ERP lo lea, ajustado del lado del emisor. El motivo del rechazo se persiste y sobrevive al cierre de la pantalla |

---

## 🛠️ Stack Tecnológico Completo

### Backend (Núcleo)

| Camada | Tecnologia | Versão | Finalidade |
|--------|------------|--------|------------|
| **Linguagem** | Oracle JDK | 21 (LTS) | Base de longo suporte com records, pattern matching |
| **Framework** | Spring Boot | 3.3.5 | Autoconfiguração, Tomcat embutido, Actuator |
| **Web** | Spring MVC + Jackson | - | Camada REST (/api/**), JSON consistente |
| **Segurança** | Spring Security 6 + JWT | - | Autenticação stateless, @PreAuthorize |
| **ORM** | Spring Data JPA + Hibernate | 6.5+ | Persistência com ddl-auto: validate |
| **Migrations** | Flyway | Latest | Migrations versionadas (V1…V101) |
| **Boilerplate** | Lombok + MapStruct | - | Menos código, mapeamento DTO↔entidade |
| **Validação** | Bean Validation (Jakarta) | - | Validação de entrada na borda da API |
| **Documentação** | springdoc-openapi | Latest | Swagger UI em /swagger-ui.html |

### Dados & Infraestrutura

| Componente | Tecnologia | Versão | Finalidade |
|------------|------------|--------|------------|
| **Banco Relacional** | PostgreSQL | 18 | Núcleo ACID com schema `brasil_saas` |
| **Criptografía** | mTLS (PKI) | - | ca.crt/sa.crt/sa.pk8 para conexión segura |
| **Documentos/Imagens** | MongoDB | Latest | Coleção `imagens` para binários/logos/fotos |
| **Cache** | Redis | Latest | Cache/sessões para tabelas quentes (NCM, municípios) |
| **Mensageria** | RabbitMQ | Latest | Eventos assíncronos (pedido → estoque/financeiro) |
| **Object Storage** | MinIO | Latest | Storage de objetos/arquivos grandes (hom/prod) |

### Fiscal (Diferencial Competitivo)

| Módulo | Tecnologia | Status |
|--------|------------|--------|
| **NFS-e São Paulo** | proxy com failover + API Java + bridge Ruby | ✅ Emitindo em produção, com troca automática de implementação |
| **SPED EFD ICMS/IPI** | java-efd-icms 3.21.1 | ✅ Gera arquivo válido, contadores conferidos |
| **MDF-e** | fincatto documentofiscal 5.1.2 | ⚠️ Conecta e assina (`cStat 107`); emissão não implementada |
| **CT-e** | fincatto documentofiscal 5.1.2 | ⚠️ Conecta e assina (`cStat 107`); emissão não implementada |
| **NF-e/NFC-e** | java-nfe (swconsultoria) | ❌ `NFeServiceImpl` tem 89 linhas e 3 TODOs — não emite |
| **Consulta/DistDFe** | wmixvideo nfe | ✅ Consulta, manifestação do destinatário |
| **SPED EFD Contribuições** | java-efd-contribuicoes 1.32.1 | ⚠️ Biblioteca no `pom.xml`, sem endpoint |
| **Validação XML** | JAXB/XSD | ✅ Schemas SEFAZ validados |
| **Certificado Digital** | A1 ICP-Brasil | ✅ O e-CNPJ A1 do ERP assina NFS-e, MDF-e e CT-e |

**Sobre o MDF-e e o CT-e:** ambos consultam a SEFAZ em homologação e recebem
`cStat 107` ("Serviço em Operação"). Esto prueba la cadena TLS, el certificado del
cliente e o envelope SOAP. **Não prova o XML do documento** — nenhum MDF-e nem
CT-e foi emitido ainda, e a montagem do MDF-e não está implementada.

Detalhe em
[`docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md`](docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md).

**El plazo que no se puede ignorar:** la NT 2026.001 hace obligatorio el grupo
`infCIOT` en el MDF-e rodoviario a partir del **23/11/2026** (rechazo `cStat 684`).
`infCIOT` ya está en el XSD; falta la validación en el emisor.

#### NFS-e São Paulo: proxy con failover entre dos implementaciones

La NFS-e de São Paulo no la atiende una sola implementación. El ERP siempre habla con
**4567**, y un proxy decide quién responde:

```
ERP  ──►  4567  nfse-failover.rb  (proxy, health check periódico)
              ├──►  4568  nfse-sp-api      Java   PRIMARIA
              └──►  4569  nfse-sp-bridge   Ruby   FALLBACK
                        (ayuntamiento de São Paulo, vía A1)
```

| Pieza | Rol |
|---|---|
| **4567 — `nfse-failover.rb`** | Proxy. Prueba el Java, cae al Ruby, health check cada 10s |
| **4568 — `nfse-sp-api`** | Implementación **primaria**. Valida el XML contra el schema **antes** de mandar |
| **4569 — `nfse-sp-bridge`** | Implementación **fallback**, en Sinatra. Asume cuando el Java falla |

El ERP descubre quién atendió por el header `X-Backend` que devuelve el proxy.

**Por qué dos implementaciones y no una:** el Ruby se escribió primero y es el que validó
las reglas contra el ayuntamiento de São Paulo de verdad — emitió y canceló una NFS-e en
producción, con los mensajes literales del ayuntamiento registrados en
[`src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md`](src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md).
El Java reimplementó el mismo contrato con el mismo alcance, y comparar los XML entre los
dos fue lo que detectó los bugs de firma y de namespace del lado Java. El Ruby sigue en
pie **a propósito**: es el oráculo contra el cual se comprueba el Java.

El orden no es decorativo. La primaria es la que valida contra el schema; si fuera al
revés, una implementación rota emitiría con un contrato equivocado y el ERP solo lo
descubriría después de que el ayuntamiento lo rechazara.

**Qué cambia el proxy y qué no.** El proxy cambia de upstream cuando la conexión falla.
**No** cambia cuando la implementación responde `200` con el contrato equivocado — y ese
es el caso que importa: el 26/09/2026, con la API Java caída, el Ruby emitió la nota 29
con `success=true`, el ayuntamiento la aceptó, y el ERP respondió error porque leía
`"sucesso"` donde el Ruby escribía `success`. Por eso existe el watchdog: observa la
respuesta, no solo el puerto.

A 4567 é o default do código, não um número escolhido:

```java
// NfseEmissaoService.java
@Value("${brasil-saas.fiscal.nfse.url:http://127.0.0.1:4567/api/nfse-sp}")
```

El certificado A1 es el e-CNPJ de la empresa y se **guarda en MongoDB** — la colección
`documentos` guarda un documento `tipoEntidade: "certificado_digital"` con el `.pfx`
íntegro. É por isso que o A1 chega junto na restauração do dump. O `.pfx` foi
emitido com **RC2-40-CBC**, que o OpenSSL 3 tirou do provider padrão: o Java
aceita, e o Ruby precisa do provider legacy (`OPENSSL_MODULES`). A reemissão
com AES resolveria de vez.

### Observabilidad & Operação

| Ferramenta | Finalidade |
|------------|------------|
| **Actuator + Micrometer** | /actuator/health\|metrics para monitoramento real |
| **Logback + MDC** | Logs com traceId, empresaId, usuarioId; prod em JSON p/ Loki |
| **Scripts Shell** | test_db_connection.sh, test_database.sh, test_erp_operations.sh |

### Frontend React ✅ COMPLETO

**LOCALIZAÇÃO**: `src/main/resources/static/react/`

| Componente | Quantidade | Status | Descripción |
|------------|------------|--------|-----------|
| **Componentes React** | 25 arquivos JSX | ✅ Completo | Componentes por módulo |
| **Serviços API** | 11 arquivos JS | ✅ Completo | Integração REST |
| **Contextos** | 1 (Auth) | ✅ Completo | Gerenciamento de sessão |
| **Build Produzido** | ~2.2MB | ✅ Otimizado | Em `static/dist/` |

**Tecnologias**: React 19, PrimeReact 10.8, React Router 7, Axios, Vite 5

**Funcionalidades**:
- ✅ Autenticação JWT com refresh automático
- ✅ 25 componentes implementados (3.762 líneas)
- ✅ Módulo Produção completo (ordens de produção)
- ✅ Design responsivo com PrimeReact
- ✅ Integração total com backend Spring Boot
- ⚠️ 4 componentes placeholder (Compras, Inventario, Ventas, Servicios)

📄 **Ver análise completa**: [docs/frontend/FRONTEND_ANALISE_COMPLETA.md](docs/frontend/FRONTEND_ANALISE_COMPLETA.md)

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| **React** | 19 | SPA administrativa |
| **Vite** | 5.4+ | Build system rápido |
| **PrimeReact** | 10.8 | Componentes UI profissionais |
| **Axios** | Latest | Cliente HTTP para API |

### Build & VCS

| Ferramenta | Finalidade |
|------------|------------|
| **Maven** | Build e gerenciamento de dependências |
| **Git** | Versionamento de código |

---

## 📦 Módulos del Sistema

### 💰 Financeiro Completo (ERP Moderno)

> **Status**: ✅ **IMPLEMENTADO E VALIDADO** - Migration V5 + V28 com 439 linhas de SQL

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Contas a Pagar** | Fluxo de aprovação multinível, integração bancária (CNAB), alertas de vencimento |
| ✅ **Contas a Receber** | Emissão de faturas/boletos, cobrança automática, conciliação bancária |
| ✅ **Tesouraria** | Projeção de fluxo de caixa, controle de empréstimos e aplicações financeiras |
| ✅ **Contabilidade Geral** | Lançamentos automatizados, conformidade fiscal, partidas dobradas |
| ✅ **Contabilidade de Custos** | Centros de custo hierárquicos, análise de rentabilidade (orçado vs realizado) |
| ✅ **Relatórios Executivos** | DRE, balanço patrimonial, dashboards, indicadores de liquidez |
| ✅ **Lançamentos Contábeis** | Paginação (máx. 20 linhas/página), histórico auditável |
| ✅ **Baixas Financeiras** | Com descontos, juros, multas e estornos |
| ✅ **Extrato Bancário** | Integrado com conciliação automática |
| ✅ **Resumo Financeiro** | Por período, centro de custo, plano de contas |
| ✅ **Comissões** | Cálculo automático sobre vendas e OS |
| ✅ **Orçamento vs Realizado** | Comparativo gerencial |
| ✅ **Provisão PDD** | Provisão para devedores duvidosos |
| ✅ **Integração Bancária** | CNAB, OFX, importação de extrato |

#### Estrutura do Módulo Financeiro (`modules/financeiro`)

- **52 clases Java** implementando todas las entidades y servicios
- **Entidades**: Titulo, TituloParcela, Baixa, LancamentoContabil, LancamentoPartida, PlanoContas, CentroCusto, ContaBancaria, TipoPagamento, CondicaoPagamento, Extrato, ConciliacaoBancaria, Renegociacao, FluxoAprovacao, Aprovacao, ProjecaoFluxoCaixa, AnaliseRentabilidade, ProvisaoPdd, IntegracaoBancaria, AplicacaoFinanceira, Emprestimo, Orcamento, OrcamentoRealizado, Comissao
- **Endpoints REST**: `/api/financeiro/titulos`, `/api/financeiro/lancamentos`, `/api/financeiro/baixas`, `/api/financeiro/extrato`, `/api/financeiro/conciliacao`, `/api/financeiro/fluxo-caixa`

---

### 🏙️ Cadastros

| Cadastro | Funcionalidades | Status |
|----------|-----------------|--------|
| ✅ **Municípios** | Busca dinâmica por código IBGE ou nome (tabela oficial) | ✅ Implementado |
| ✅ **Pessoas** | Cadastro único para PF e PJ com dados completos | ✅ Implementado |
| ✅ **Clientes** | Gestão com limite de crédito, logo, histórico | ✅ Implementado |
| ✅ **Fornecedores** | Avaliação, classificação, logo | ✅ Implementado |
| ✅ **Produtos** | Variações, kits, NCM, imagens no MongoDB | ✅ Implementado |
| ✅ **Serviços** | Para NFS-e, integração com OS | ✅ Implementado |
| ✅ **Funcionários** | Vínculo com pessoa, foto, cargo | ✅ Implementado |
| ✅ **Empresas** | Multi-tenant, logo, configurações | ✅ Implementado |
| ✅ **Condições de Pagamento** | Prazos, parcelas | ✅ Financeiro |
| ✅ **Tipos de Documentos** | Classificação fiscal | ✅ Fiscal |
| ✅ **Categorías** | Categorización de productos/servicios | ✅ Implementado |
| ✅ **Marcas** | Gestão de marcas de produtos | ✅ Implementado |
| ✅ **Unidades de Medida** | Unidades para produtos | ✅ Implementado |
| ✅ **Transportadoras** | Transportadoras para frete | ✅ Implementado |

---

### 📋 Serviços e Vendas

| Módulo | Funcionalidades | Status |
|--------|-----------------|--------|
| ✅ **Ordem de Serviço (OS)** | Emissão de comprovante, cálculo de comissão técnica | ✅ Implementado (V35, V41) |
| ✅ **Venta de Productos** | Con nota fiscal opcional, integración con inventario | ✅ Implementado (V20) |
| ✅ **Faturamento** | Em aba separada, geração de títulos automáticos | ✅ Implementado |
| ✅ **Cálculo ICMS** | Automático para vendas interestaduais | ✅ Implementado |
| ✅ **Emissão NF-e/NFS-e** | Integrada com módulo fiscal | ✅ Implementado |
| ✅ **Pedidos de Compra** | Integração com fornecedores | ✅ Implementado (V21) |
| ✅ **Controle de Estoque** | Saldos, movimentações, ajustes | ✅ Implementado (V22) |
| ✅ **Entrada de Notas** | Tipo de operação fiscal | ✅ Implementado (V31, V42) |

---

### 📄 Informes Profesionales

| Relatório | Formato | Recursos |
|-----------|---------|----------|
| ✅ **Financeiro Analítico** | PDF | Layout moderno, cores, formatação empresarial |
| ✅ **Ordem de Serviço** | PDF | Comprovante formatado com logo |
| ✅ **Extrato de Lançamentos** | PDF | Filtros por período, conta, centro de custo |
| ✅ **DRE Gerencial** | PDF | Demonstrativo de resultados |
| ✅ **Balanço Patrimonial** | PDF | Ativo, passivo, patrimônio líquido |
| ✅ **Curva ABC** | PDF | Classificação de produtos/clientes |

---

### 🏭 Produção Industrial

> **Status**: ✅ **IMPLEMENTADO** - Controller, Service, Repository e Models

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Ordem de Produção** | Fluxo completo: Insumos → Processo → Produto Final |
| ✅ **Pesos e Medidas** | Suporte a densidades, unidades especiais |
| ✅ **Rastreabilidade** | Lote, série, validade |
| ✅ **Apuntamiento** | Horas trabajadas, consumo de insumos |
| ✅ **Itens de Produção** | Controle de itens produzidos |

---

### 🤖 IA Asistiva

| Recurso | Status |
|---------|--------|
| ✅ **Chat com IA** | Spring AI + OpenAI integrado |
| ✅ **Análise de Dados** | Recomendações baseadas em histórico |
| ✅ **Alertas Inteligentes** | Notificações proativas |
| ⚠️ **Chave API** | Módulo pronto, necessita chave OpenAI válida |

---

## 🗂️ Estructura del Proyecto

```
BRASIL-SAAS-ERP/
├── src/main/java/br/com/brasil_saas/     # Monólito principal (347 classes Java)
│   ├── core/         # Autenticação, usuários, empresas, perfis, permissões
│   ├── cadastro/     # Pessoas, produtos, clientes, fornecedores, marcas, categorias
│   ├── financeiro/   # Títulos, lançamentos, conciliação, plano de contas
│   ├── vendas/       # Pedidos de venda, faturamento
│   ├── compras/      # Pedidos de compra, itens
│   ├── estoque/      # Saldos, movimentações, controle de estoque
│   ├── fiscal/       # NF-e, NFC-e, NCM, CFOP, CEST, impostos, entrada de notas
│   ├── rh/           # Funcionários, cargos, folha de pagamento
│   ├── producao/     # Ordem de produção, itens de produção, apontamento
│   ├── bi/           # Business Intelligence (em desenvolvimento)
│   ├── ia/           # IA assistiva com Spring AI (em desenvolvimento)
│   ├── servicos/     # Ordens de serviço, emissão de NFS-e
│   ├── portais/      # Integrações com portais externos (estrutura)
│   └── integracoes/  # Serviços de integração (estrutura)
│
├── modules/          # Módulos independentes (279 classes Java em 12 módulos)
│   ├── core/         # Módulo core (30 classes) - Porta 8081
│   ├── shared/       # Biblioteca compartilhada (22 classes)
│   ├── cadastro/     # Cadastros (116 classes) - Porta 8082
│   ├── financeiro/   # Financeiro (51 classes) - Porta 8083
│   ├── vendas/       # Vendas (9 classes) - Porta 8084
│   ├── compras/      # Compras (10 classes) - Porta 8085
│   ├── estoque/      # Estoque (6 classes) - Porta 8086
│   ├── fiscal/       # Fiscal (89 classes) - Porta 8087
│   │   ├── mdfe/      # MDF-e: config, emissao, contrato, controller
│   │   ├── cte/       # CT-e: emissao e controller
│   │   ├── sped/      # SPED EFD ICMS/IPI: gerador e controller
│   │   └── nfse/      # NFS-e: emissao, retorno, controller
│   ├── rh/           # Recursos Humanos (15 classes) - Porta 8088
│   ├── servicos/     # Serviços (9 classes) - Porta 8080
│   ├── producao/     # Produção Industrial (9 classes) - Porta 8090 ✅ NOVO
│   └── ia/           # IA Asistiva (0 classes) - Porta 8089 ⚠️ Estrutura vazia
│
├── src/main/resources/microservices/  # Material de referencia (554 MB, 23 pastas)
│   │
│   │   Origem de cada pasta (URL do repositorio) e o que serve dela em
│   │   docs/pesquisa/MICROSERVICES-O-QUE-TEM.md
│   │   ⚠️ EXCLUIDO do empacotamento pelo pom.xml. Nao entra no JAR e nao e
│   │   copiado para target/classes. E consulta, nao dependencia.
│   │
│   ├── nfse-sp-api/         # A API de NFS-e que roda na 4567 (codigo nosso)
│   ├── nfse-sp-bridge/      # Emissor Ruby, desligado (codigo nosso)
│   ├── nfse-watchdog/       # Watchdog, desligado de proposito (codigo nosso)
│   ├── nfse-failover/       # Teste do failover (codigo nosso)
│   │   │
│   │   O manifesto de hashes (MANIFESTO-MICROSERVICES.json) fica aqui quando
│   │   gerado, mas e gitignored: 2,6 MB de SHA-256 e artefato de build, nao
│   │   documentacao. Regenera com scripts/gerar_manifesto.py
│   ├── nfe/                 # fincatto documentofiscal 5.1.2 <- a lib do MDF-e
│   ├── PL_MDFe_300b_NT012025_1.05/  # 41 XSD + 6 PDF oficiais da SEFAZ
│   ├── NFSe-SaoPaulo-SP/    # XSD e manual da prefeitura de SP
│   ├── sped-mdfe/           # PHP nfeephp. So XSD e exemplos servem
│   ├── Java_MDFe/           # VAZIA: so um README de Discord, zero codigo
│   ├── Java_Certificado/    # Docs de A1 e A3
│   ├── l10n-brazil/         # OCA: as tabelas fiscais em CSV (cfop, cest, ncm)
│   ├── esocial/  Java_NFe/  Java_CTe/  Java_Efd-Icms/   # Referencia
│   └── ... 23 no total
│
├── src/main/resources/db/migration/   # Migrations Flyway (V1-V101)
│   ├── V1__init_schema.sql             # Schema inicial
│   ├── V2__core.sql                    # Core: usuários, empresas, perfis
│   ├── V3__cadastro.sql                # Cadastros básicos
│   ├── V4__fiscal.sql                  # Tabelas fiscais
│   ├── V5__financeiro.sql              # Financeiro completo (439 linhas)
│   ├── V15__seed_perfis_permissoes.sql # Seed de perfis e permissões
│   ├── V16__seed_tabelas_oficiais.sql  # Tabelas oficiais (municípios, etc)
│   ├── V17__migracao_sysfluxo.sql      # Migração do legado SysFluxo
│   ├── V18__seed_permissoes_cadastro.sql
│   ├── V19__seed_permissoes_fiscais.sql
│   ├── V20__vendas.sql                 # Módulo de vendas
│   ├── V21__compras.sql                # Módulo de compras
│   ├── V22__estoque.sql                # Módulo de estoque
│   ├── V23__rh.sql                     # Módulo de RH
│   ├── V24__alinhar_entidades_operacionais.sql
│   ├── V25__permissoes_adicionais.sql
│   ├── V26__adicionar_soft_delete_saldo_estoque.sql
│   ├── V27__produto_link_e_imagem_mongodb.sql
│   ├── V28__permissoes_financeiro.sql
│   ├── V29__permissoes_rh_cargo_func.sql
│   ├── V30__seed_fornecedor_ref.sql
│   ├── V31__fiscal_entrada_tipo_operacao.sql
│   ├── V32__permissoes_fase_final.sql
│   ├── V33__seed_fornecedor_cliente_produto_ref.sql
│   ├── V35__servicos_ordem_servico.sql # Ordem de serviço
│   ├── V39__logo_foto_colunas.sql      # Colunas de logo/foto
│   ├── V40__seed_referencias_minimas.sql
│   ├── V41__servicos_ordem_servico_segundo.sql
│   ├── V42__fiscal_entrada_tipo_operacao_segundo.sql
│   ├── V43__permissoes_servicos_e_entrada.sql
│   ├── V44__add_logo_fields_to_tables.sql
│   ├── V45__add_hierarquia_nivel_to_perfil.sql
│   ├── V46__seed_diretoria_gerente_hierarquia.sql
│   │   ... V47 a V97: modulos seguintes e reconciliacao de permissoes
│   ├── V92__servico_codigo_tributacao_municipal.sql  # Codigo municipal do servico
│   ├── V93__nfse_arquivos_mongo_e_chave_nacional.sql  # XML no Mongo, chave nacional
│   ├── V94__caixa.sql
│   ├── V95__empresa_cnpj_e_usuario_sem_empresa.sql
│   ├── V96__catalogo_bancos.sql
│   ├── V97__reconcilia_permissoes_por_perfil.sql
│   ├── V98__nfse_retorno_prefeitura.sql    # bc_fis_nfse_retorno
│   ├── V99__nfse_retorno_colunas_base.sql  # Colunas que a entidade exigia
│   ├── V100__cfop_e_cest_do_oca.sql        # 619 CFOP + 1.043 CEST
│   └── V101__servicos_teste_com_codigos_municipais_validos.sql
│
│   ⚠️ Migration aplicada nao se reescreve. O Flyway grava o checksum no
│   historico, e uma migration alterada derruba a aplicacao na subida.
│   Correcao sempre para frente, com uma V nova.
│
├── systemd_units/    # Serviços Linux (systemd)
│   ├── brasilsaas-erp-core.service
│   ├── brasilsaas-erp-cadastro.service
│   └── ...
│
├── scripts/          # Scripts de automação
│   ├── manage_services.sh    # Gerenciamento mestre
│   ├── build_module.sh        # Build individual
│   ├── installbase.sh         # Instalação Linux (também está na raiz)
│   ├── install-sysfluxo.ps1   # Instalação Windows
│   ├── test_db_connection.sh  # Smoke test DB
│   ├── test_erp_operations.sh # Teste operações
│   ├── check_mongodb.sh       # Validação MongoDB
│   │
│   │   Fiscal e material de referencia:
│   ├── gerar_manifesto.py       # SHA-256 dos 16.299 arquivos do material
│   ├── restaurar_microservices.sh  # Baixa, apaga o .git e VERIFICA o hash
│   ├── salvar_docs_mongo.py    # Documentacao no Mongo; recusa credencial
│   └── montar_cadeia_sefaz.py  # Monta o truststore JKS com as CAs do sistema
│
├── src/main/resources/static/react/  # Frontend React
│   ├── src/
│   │   ├── components/     # Componentes PrimeReact
│   │   ├── contexts/       # Contextos React
│   │   └── services/       # Serviços API
│   ├── package.json        # React 19, PrimeReact 10.8, Vite 5.4
│   └── vite.config.js
│
├── frontend/         # Frontend alternativo (em desenvolvimento)
│   ├── public/
│   └── src/
│
├── docs/             # Documentação técnica
│   ├── pesquisa/     # Investigações e achados de campo
│   │   ├── docs/pesquisa/MEGA-MANUAL-API-FISCAL.md        # Como montar a API, passo a passo
│   │   ├── docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md  # As 3 APIs, com o testado
│   │   ├── docs/pesquisa/MICROSERVICES-O-QUE-TEM.md        # As 23 pastas e a origem de cada
│   │   ├── docs/pesquisa/TELAS-VAZIAS-DO-FISCAL.md         # CFOP e CEST que mostravam vazio
│   │   ├── docs/pesquisa/contrato-nfse-unico.md            # O contrato de resposta
│   │   ├── docs/pesquisa/o-que-sobrevive-a-tela-de-erro.md  # A tabela de retornos
│   │   ├── docs/pesquisa/REGISTRO-da-prefeitura-NFSE.md     # O que a prefeitura respondeu
│   │   ├── docs/pesquisa/pesquisa-failover-chunked.md       # Por que o proxy perdia o corpo
│   │   ├── docs/pesquisa/proxy-nginx-e-bug-do-fallback.md   # O bug que gerou notas duplicadas
│   │   ├── docs/pesquisa/CAUSA-do-crash-loop-NFSE.md        # A causa do crash de 8,6 h
│   │   └── docs/pesquisa/diagrama-fallback-nfse.md          # O desenho do fallback
│   │
│   ├── modulos/      # Documentação por módulo
│   ├── api/          # Contratos de API
│   ├── docs/./arquitetura.md
│   ├── docs/./modelo-dados.md
│   ├── docs/./autenticacao.md
│   ├── docs/./roadmap.md
│   └── docs/./RELATORIO_PARIDADE_FUNCIONAL_ERP_25-09-2026.md
│
├── pom.xml           # Maven principal (Spring Boot 3.3.5)
├── pom-parent.xml    # Parent POM modular
├── pom-multimodule.xml # Build multi-módulo
└── docs/modulos/README.md         # Este arquivo
```

---

## 🚀 Cómo Levantar la Aplicación

### Requisitos previos

| Dependencia | Versión Mínima | Obligatorio |
|-------------|---------------|-------------|
| ☕ Oracle JDK | 21 (LTS) | ✅ Sí |
| 📦 Apache Maven | 3.9+ | ✅ Sí |
| 🟢 Node.js | 22+ LTS | ✅ (frontend) |
| 🐘 PostgreSQL | 18+ | ✅ Sí |
| 🍃 MongoDB | Latest | ✅ (imágenes) |
| 🔧 Git | Latest | ✅ Sí |

### 🐧 Instalación en Linux (Ubuntu/Debian/CentOS/Fedora)

```bash
# Tornar script executável
chmod +x installbase.sh

# Executar instalação automatizada
sudo ./installbase.sh
```

**El script hará:**
1. Detectar la distribución de Linux
2. Instalar JDK 21 vía SDKMAN
3. Instalar Maven y Node.js
4. Configurar PostgreSQL y MongoDB
5. Generar la PKI Easy-RSA y configurar el mTLS de PostgreSQL
6. **Instalar las CA de ICP-Brasil y montar el truststore JKS** (MDF-e e CT-e)
7. Clonar dependencias Maven
8. Compilación automática del frontend React

El paso 6 no es opcional. La SVRS presenta un certificado de ICP-Brasil
(`AC SERPRO SSLv1`, vindo da `Raiz Brasileira v10`), e num Debian limpo nenhuma
dessas duas está no bundle de CAs: sem elas o handshake morre com
`PKIX path building failed`. El script deja:

```
/etc/brasil-saas/certs/truststore-sefaz.jks   644   solo CA pública
/etc/brasil-saas/sefaz.env                   644   ruta y UF, sin secreto
/etc/brasil-saas/cert.env                    600   el A1 y las contraseñas
```

El truststore está separado del truststore del sistema **a propósito**: el ERP es Java
y la biblioteca fiscal lee su propio JKS. Sin montarlo, el mismo error vuelve y parece
que las CA nunca entraron.

`cert.env` debe completarse con el A1 del emisor:

```bash
BRASIL_SAAS_MDFE_CERTIFICADO=/caminho/do/a1.pfx
BRASIL_SAAS_MDFE_CERT_PASS=...
```

**El mismo A1 sirve para NFS-e, MDF-e y CT-e**, desde que seja e-CNPJ A1 da
ICP-Brasil. No son certificados distintos: son usos distintos del mismo.

### Scripts de operación

```bash
# Sobe o ambiente de desenvolvimento (Postgres, Mongo, backend 8080, Vite 5173)
./subir-dev.sh --reiniciar
./subir-dev.sh --parar

# Gera o manifesto de hashes das 23 pastas de microservices
python3 scripts/gerar_manifesto.py

# Baixa os repositórios, apaga o .git e VERIFICA o hash de cada arquivo
./scripts/restaurar_microservices.sh --verificar

# Grava a documentação no Mongo e recusa documento com credencial
python3 scripts/salvar_docs_mongo.py
```

O `restaurar_microservices.sh` importa SHA-256 dos 16.299 arquivos das pastas
de referência. **Só 3 das 23 mantêm o `.git` com remote** — das outras 20 o
histórico se perdió, y un `clone` trae el HEAD de hoy, que puede ser otra versión
do material com que o ERP foi testado. O hash é o que diz se o que baixou é o
mesmo, e o script **diz que não bate** em vez de deixar material errado entrar
em silêncio.

### 🪟 Instalação no Windows

```powershell
# Executar PowerShell como Administrador
.\install-sysfluxo.ps1
```

**El script hará:**
1. Verificar permissões de administrador
2. Instalar JDK 21 Oracle via winget/choco
3. Instalar Node.js, Maven e PostgreSQL
4. Configurar firewall nas portas necessárias
5. Build automático do frontend

### 📝 Configuração Manual (Passo a Passo)

#### 1️⃣ Clonar o Repositório

```bash
git clone https://github.com/euripedesdark/BRASIL-SAAS-ERP.git
cd BRASIL-SAAS-ERP
```

#### 2️⃣ Configurar Banco de Dados

```sql
-- Criar banco no PostgreSQL
CREATE DATABASE "brasil-saas" WITH OWNER = postgres ENCODING = 'UTF8';

-- Criar schema
CREATE SCHEMA brasil_saas;
```

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/brasil-saas?currentSchema=brasil_saas
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA

# MongoDB
spring.data.mongodb.uri=mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil_saas?authSource=admin
```

#### 3️⃣ Build do Frontend

```bash
cd src/main/resources/static/react
npm install
npm run build
cd ../../../../
```

#### 4️⃣ Build do Backend

```bash
mvn clean package -DskipTests
```

#### 5️⃣ Executar a Aplicação

```bash
# Modo desenvolvimento
mvn spring-boot:run

# Ou executar JAR
java -jar target/BRASIL-SAAS-ERP-web-1.0.0.jar

# Modo produção
java -jar target/BRASIL-SAAS-ERP-web-1.0.0.jar --spring.profiles.active=prod
```

---

## 🌐 Accediendo al Sistema

| URL | Descripción |
|-----|-----------|
| http://localhost:8080 | URL Principal |
| http://localhost:8080/api/* | API REST |
| http://localhost:8080/swagger-ui.html | Documentação Swagger |
| http://localhost:8080/actuator/health | Health Check |
| http://localhost:8080/actuator/metrics | Métricas |

### 🔑 Credenciais Padrão

| Usuário | Senha | Perfil |
|---------|-------|--------|
| sysdba | masterkey | SuperAdmin |

> ⚠️ **Importante**: Alterar após o primeiro acesso!

---

## 📊 Porcentaje de finalización de lo fiscal

**Critério usado**, para o número não ser opinião. Cada documento fiscal precisa
de 6 entregáveis:

| # | Entregável |
|---|-----------|
| 1 | **Conexión con la SEFAZ/ayuntamiento** respondiendo |
| 2 | **Montagem do documento** (o XML: emitente, veiculo, motorista, LAC) |
| 3 | **Assinatura** com A1 ICP-Brasil |
| 4 | **Envio e protocolo** |
| 5 | **Consulta / cancelamento** |
| 6 | **Pantalla** con la respuesta del ayuntamiento |

| Módulo | 1 | 2 | 3 | 4 | 5 | 6 | % |
|--------|---|---|---|---|---|---|---|
| **NFS-e São Paulo** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **100%** |
| **SPED EFD ICMS/IPI** | — | ✅ | ⚪ | — | — | ⚪ | **40%** |
| **MDF-e** | ✅ | ❌ | ⚪ | ❌ | ⚪ | ❌ | **25%** |
| **CT-e** | ✅ | ❌ | ⚪ | ❌ | ❌ | ❌ | **17%** |
| **NF-e / NFC-e** | ❌ | ❌ | ❌ | ❌ | ❌ | ⚪ | **0%** |
| **SPED EFD Contribuições** | — | ❌ | ⚪ | — | — | ⚪ | **20%** |
| **Consulta/DistDFe** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **100%** |

`✅` pronto e verificado · `⚪` existe mas não verificado · `❌` não existe
`—` não se aplica (EFD não se envia a ninguém: é arquivo, não web service)

**Lendo os números:**

- **MDF-e y CT-e están en 25% y 17%, no en 100%.** La conexión funciona y la
  SEFAZ responde `cStat 107` com o seu A1 — isso é o entregável 1, o mais
  difícil, porque eram 4 camadas entre o ERP e a resposta (A1, CAs da
  ICP-Brasil, truststore JKS, mutual TLS). **lo que falta es el entregable 2**, el
  montagem do documento, e ele é o trabalho maior. `cStat 107` é o *status do
  servicio*: no dice nada sobre el XML, porque nunca se armó ningún XML.
- **SPED EFD ICMS/IPI em 40%** e não em 100% porque gera arquivo válido, com
  os contadores conferidos, mas lê o cabeçalho do corpo da requisição em vez
  del banco, y no tiene pantalla. Faltan los entregables 2 y 5.
- **NF-e em 0%.** Não é exagero: os 3 métodos da interface `NFeService` estão
  como `TODO` em `NFeServiceImpl`. A interface existe, a implementação não.
- **Consulta/DistDFe em 100%** de 6 em 6.

El % es del **alcance fiscal**, no del sistema. Los demás módulos tienen su propia medición,
abaixo.

## 📊 Demás módulos — estructura y qué responde

### Por que não há um % único aqui

**Contar archivos no mide la madurez.** Medí los 12 módulos con los mismos 6
entregáveis do fiscal e **onze deram 100%** — porque todos têm model, repository,
service, controller e tela. Isso não significa que o cadastro de cliente abre, que
o título baixa ou que a folha calcula. Significa que os arquivos existem.

O que mede é executar. Então há duas colunas, e elas discordam:

| Módulo | Tabela | Entidade | Repo | Service | Controller | Tela | Estrutural | Responde |
|--------|:------:|:--------:|:----:|:-------:|:----------:|:----:|:----------:|:--------:|
| Cadastro | 36 | 21 | 21 | 28 | 12 | 11 | 6/6 | ✅ |
| Financeiro | 32 | 32 | 22 | 12 | 12 | 15 | 6/6 | ✅ |
| Estoque | 13 | 12 | 12 | **0** | 9 | 9 | 5/6 | ✅ |
| Fiscal | 29 | 29 | 26 | 6 | 12 | 10 | 6/6 | ⚠️ parcial |
| Vendas | 11 | 5 | 5 | 2 | 2 | 2 | 6/6 | ✅ |
| Compras | 11 | 10 | 8 | 4 | 4 | 4 | 6/6 | ✅ |
| Produção | 6 | 6 | 5 | 11 | 4 | 4 | 6/6 | ✅ |
| RH | 4 | 4 | 3 | 2 | 4 | 4 | 6/6 | ❌ |
| Serviços | 3 | 3 | 3 | 2 | 1 | 1 | 6/6 | ✅ |
| BI | 8 | 17 | 9 | 14 | 6 | 4 | 6/6 | ✅ |
| Core | 15 | 9 | 7 | 14 | 10 | 6 | 6/6 | ✅ |
| IA | 10 | 10 | 10 | 18 | 9 | **1** | 6/6 | ⚠️ parcial |

**Estrutural** = os 6 entregáveis existem como arquivo.
**Responde** = a API respondeu num smoke test com token válido.

⚠️ **Nenhum destes foi validado funcionalmente.** Abri a tela, não. Criei
registro, não. O que o smoke test prova é que a **rota responde**; não prova
que o dado devolvido esteja certo. As duas coisas são diferentes, e confundir
elas é como se descobre que "o módulo está pronto" e a tela mostra valor errado.

### O que o smoke test achou

28 rotas chamada, uma por módulo:

```
16  2xx   respondem
 1  500   QUEBROU   <- o que interessa
 4  404   rota não existe no caminho testado
 6  400   parâmetro obrigatório faltando
```

**O 500 é um bug real e reproduzível:**

```
GET /api/rh/funcionarios?page=0&size=1   ->  500

HttpMessageNotWritableException: Could not write JSON:
  could not initialize proxy [br.com.brasil_saas.rh.model.Cargo#1]
Caused by: LazyInitializationException: no Session
```

`Funcionario` tem `@ManyToOne(fetch = FetchType.LAZY)` para `Cargo`. A sessão
do Hibernate fecha quando a transação do controller termina, e o Jackson tenta
serializar o proxy **depois** — sem sessão. O `GET /api/rh/cargos` funciona
(200); o de funcionários não.

Correção: buscar o funcionário com `join fetch` no `Cargo`, ou marcar o
relacionamento como `EAGER`, ou devolver um DTO em vez da entidade. A
recomendada é o `join fetch`, porque `EAGER` em `ManyToOne` resolve o problema
trocando por N+1 em toda listagem.

### Os 404 e 400 são meus, não do sistema

Chamei caminhos que não existem. `rh/funcionarios` só tem `GET /{id}`, sem
listagem — o `500` acima veio de `GET /{id}` com página. `core/empresas` e
`core/usuarios` não têm rota de listagem nesse caminho. Não são bugs; são URLs
que eu chutei.

**A lição:** as porcentagens estruturais daqui são confiáveis — os arquivos
existem. A coluna "Responde" é de uma amostra de **uma rota por módulo**, o que
no cubre el módulo. Medirlo de verdad exige una suite de pruebas, y el ERP **no tiene
teste automatizado**.

### Estoque não tem camada de service

9 controllers, 12 repositories, **0 services**. La regla de negocio está dentro
del controller, hablando directo al repository. Funciona, pero no hay dónde
probar y no aísla la regla. En los demás módulos la separación existe — es una
inconsistência a decidir, não um acidente.

## ✅ Qué Está Corriendo

Verificado el **26/09/2026**. Los ocho servicios levantados y respondiendo.

| Servicio | Puerto | Estado | Cómo se verificó |
|---------|-------|--------|---------------------|
| **PostgreSQL** | 5432 | 🟢 `active/enabled` | `select 'ok'` retorna `ok` |
| **MongoDB** | 27017 | 🟢 `active/enabled` | conecta; 4 colecciones |
| **RabbitMQ** | 5672 / 15672 | 🟢 `active/enabled` | el panel responde |
| **MinIO** | 9000 / 9001 | 🟢 `active/enabled` | `/minio/health/live` → 200 |
| **Redis** | 6379 | 🟢 `active/enabled` | `PING` → `PONG` |
| **ERP (Spring Boot)** | 8080 | 🟢 de pé | responde; 500 con token falso es el comportamiento normal |
| **Vite (frontend)** | 5173 | 🟢 `HTTP 200` | página servida |
| **API NFS-e** | 4567 | 🟢 `active/enabled` | `sucesso=true`, certificado y contraseña cargados |

### Apagados a propósito

| Servicio | Por qué |
|---------|---------|
| `nfse-sp-bridge` (Ruby) | **Una sola API, sin fallback automático.** Un fallback que cambia de implementación cuando la llamada expira emite una nota duplicada — se pierde la respuesta, el ERP reemite y el ayuntamiento acaba con dos. El bridge sigue en disco; el contrato `contrato_nfse` es el mismo de los dos lados. |
| `nfse-watchdog` | Usaba `consulta-cnpj` como health check, que va al ayuntamiento y tarda 1,27 s. Tiró abajo la API Java **3 veces** mientras el ERP emitía por ella. No volver a encenderlo como estaba. |
| `nginx` | Reemplazado por la API única. El proxy leía el cuerpo HTTP del socket y solo sabía leerlo con `Content-Length`; el ERP manda `Transfer-Encoding: chunked`, así que el cuerpo se descartaba. |

### Lo que el ERP valida por su cuenta

| Verificación | Resultado |
|-------------|-----------|
| `GET /api/fiscal/nfse/status` (via 4567) | `sucesso=true` |
| `POST /api/fiscal/mdfe/status` → SVRS | **`cStat 107`** "Servicio en Operación" *(literal devuelto por la SEFAZ)* |
| `GET /api/fiscal/cte/status` → SEFAZ de SP | **`cStat 107`** "Servicio en Operación." *(literal devuelto por la SEFAZ)* |
| `GET /api/fiscal/sped/efd/exemplo` | archivo válido, 19 líneas, contadores verificados |

O MDF-e é atendido pela **SVRS** (`ufAtendente: RS`) e o CT-e pelo **portal de
SP** (`ufAtendente: SP`), com o **mesmo A1**. São autorizadores diferentes, não
certificados distintos.

### Lo que aún no está listo

| Ítem | Situación |
|------|----------|
| **NF-e / NFC-e** | `NFeServiceImpl` tem 89 linhas e **3 TODOs**. A interface promete `emitirNFe`, `cancelarNFe` e `consultarSituacao`; nenhuma das três é implementada. |
| **IA (Spring AI)** | Módulo presente e conectado, com embeddings, chat e classifications. Não foi validado nesta rodada. |
| **Emisión de MDF-e** | La conexión con la SVRS está lista y probada (`cStat 107`), y el certificado firma. **A montagem do documento não existe ainda**: `MdfeEmissaoService` tem só `statusServico` e `consultarRecibo`. Falta construir o XML com emitente, veículo, motorista, LAC, municípios e CIOT. |
| **Emissão de CT-e** | Idem. Só `statusServico`. |
| **SPED EFD Contribuciones** | La biblioteca resuelve en `pom.xml`, pero **no hay endpoint** — solo ICMS/IPI lo tiene. |
| **Producción (SEFAZ)** | Nunca probada. La biblioteca devuelve `HOMOLOGACAO` fijo; la configuración lo sobrescribe, y sobrescribirlo no es lo mismo que funcione. |
| **Pestaña de retorno en la pantalla** | Los datos están en Mongo y el endpoint existe; falta la pestaña en `Nfse.jsx`. |
| **Validación del CIOT** | `cStat 684` entra en producción en la SEFAZ el **23/11/2026**. `infCIOT` ya está en el XSD; falta la validación en el emisor. |

**Por que o MDF-e e o CT-e não estão na lista de "prontos" apesar de
respondan `cStat 107`:** el `cStat 107` es el *estado del servicio*, y prueba que la
cadena TLS, el certificado del cliente y el sobre SOAP están correctos. **No
prova que o XML do documento esteja**, porque nenhum documento foi montado nem
enviado. São duas coisas diferentes, e o README separa por isso.

---

## 📡 Endpoints de la API

### Municípios

```
GET    /api/municipios/paginado?page=0&size=20&sort=nome,asc
GET    /api/municipios/buscar?termo={codigo_ou_nome}
POST   /api/municipios
PUT    /api/municipios/{id}
DELETE /api/municipios/{id}
```

### Financeiro

```
GET    /api/financeiro/titulos/paginado?page=0&size=20
GET    /api/financeiro/lancamentos/paginado?page=0&size=20
GET    /api/financeiro/resumo?inicio=YYYY-MM-DD&fim=YYYY-MM-DD
POST   /api/financeiro/titulos
POST   /api/financeiro/lancamentos
PUT    /api/financeiro/titulos/{id}/baixar
DELETE /api/financeiro/titulos/{id}/estornar
GET    /api/financeiro/extrato/{idConta}
GET    /api/financeiro/conciliacao
GET    /api/financeiro/fluxo-caixa
```

### Ordem de Serviço

```
GET    /api/servicos/ordens/paginado
POST   /api/servicos/ordens
PUT    /api/servicos/ordens/{id}
POST   /api/servicos/ordens/{id}/emitir-nota
POST   /api/servicos/ordens/{id}/fechar
GET    /api/servicos/ordens/{id}/pdf
```

### Relatórios

```
GET    /api/relatorios/financeiro-pdf?inicio=YYYY-MM-DD&fim=YYYY-MM-DD
GET    /api/relatorios/os-pdf/{id}
GET    /api/relatorios/dre?competencia=YYYY-MM
```

### Fiscal — NFS-e São Paulo

La API de emisión es un servicio separado, en el puerto **4567**. El ERP habla con ella por
`brasil-saas.fiscal.nfse.url`. É **uma API só, sem fallback automático** — o
bridge Ruby e o nginx estão desligados de propósito, porque um fallback que
troca de implementação em produção emite nota duplicada quando a resposta se
perde.

```
POST   /api/fiscal/nfse/emitir
POST   /api/fiscal/nfse/{id}/cancelar
GET    /api/fiscal/nfse/{id}/xml
GET    /api/fiscal/nfse/{id}/pdf
GET    /api/fiscal/nfse/retornos                   conversas com a prefeitura
GET    /api/fiscal/nfse/retornos/recusas           só as recusadas
GET    /api/fiscal/nfse/retornos/para-conferir     as que não deu para saber
```

El motivo del rechazo del ayuntamiento se graba **antes** de que la excepción suba, con el
`cStat` e o corpo bruto no Mongo. A tela de erro é uma parede; o registro não.

### Fiscal — MDF-e e CT-e

```
GET    /api/fiscal/mdfe/status                     cStat 107 se a SVRS estiver de pé
GET    /api/fiscal/mdfe/recibo?numero={recibo}     2ª chamada: do recibo para a chave
GET    /api/fiscal/cte/status                      cStat 107 se a SEFAZ estiver de pé
```

Os dois usam o mesmo contrato de resposta, com os **três estados** de
`sucesso`: `true` (autorizado), `false` (recusado, com `cStat` e motivo) e
`null` (**não deu para saber**). O terceiro estado existe para não duplicar
documento: a SVRS rejeita chave natural repetida, e reemitir no escuro é
exatamente o que cria a duplicidade.

O MDF-e devolve **recibo** na primeira chamada e **protocolo** na segunda.
Ler a chave na resposta do envio dá NPE — o método não existe.

### Fiscal — SPED EFD ICMS/IPI

```
POST   /api/fiscal/sped/efd/gerar                  gera o arquivo EFD
GET    /api/fiscal/sped/efd/exemplo                gera o de exemplo, para conferir o formato
```

**EFD não se envia para ninguém.** O arquivo é gerado, assinado e guardado; quem
busca depois é a SEFAZ ou a Receita. Sem web service, sem protocolo, sem fila.

### Fiscal — tabelas e cadastros

```
GET    /api/fiscal/cest?busca={termo}              busca por código, descrição ou NCM
GET    /api/fiscal/cfop?tipoOperacao=ENTRADA|SAIDA
GET    /api/fiscal/ncm
GET    /api/fiscal/issqn
GET    /api/fiscal/impostos
GET    /api/fiscal/certificados
```

`GET /api/cadastro/servicos?nome={termo}` alimenta o autocomplete do nome do
servicio, que sugiere un servicio ya cadastrado mientras la persona escribe. Dos
cadastros com nome parecido e código municipal diferente é a causa mais comum
de rechazo en el ayuntamiento.

---

## 🎯 Funcionalidades Especiales

### 🔍 Busca Dinâmica de Municípios
Digite o código IBGE ou nome e o sistema localiza instantaneamente.

### 📄 Paginación Inteligente
Todas as telas exibem máx. 20 linhas, com navegação entre páginas.

### 🌳 Lazy Loading
Dados carregados sob demanda, reduzindo consumo de memória.

### 📊 Dashboard Financeiro
Visualize totais por status (Aberto, Pago, Atrasado) em tempo real.

### 🖨️ Informes Profesionales
PDFs com layout moderno, cores diferenciadas e formatação empresarial.

### 🔐 Hierarquia de Permissões

| Perfil | Acesso |
|--------|--------|
| **SuperAdmin** | Completo, incluindo gerenciamento de administradores |
| **Diretor** | Todas funcionalidades exceto gerenciar administradores |
| **Gerente** | Todas funcionalidades operacionais |
| **Usuário** | Limitado às permissões específicas |

---

## ⚙️ Configuración Avanzada

### Alterar Porta do Servidor

```properties
# application.properties
server.port=8080  # Altere para a porta desejada
```

### Modo Produção

```bash
mvn clean package
java -jar target/BRASIL-SAAS-ERP-web-1.0.0.jar --spring.profiles.active=prod
```

### Logs Detalhados

```properties
logging.level.br.com.brasil_saas=DEBUG
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### Variáveis de Ambiente

```bash
# PostgreSQL
export DATABASE_URL="jdbc:postgresql://localhost:5432/brasil-saas?currentSchema=brasil_saas"
export DATABASE_USERNAME="sa"
export DATABASE_PASSWORD="<sua-senha>"

# MongoDB
export MONGODB_URI="mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil_saas?authSource=admin"

# JWT
export JWT_SECRET="ALTERE_ME_32_BYTES_OU_MAIS"

# Imagens
export IMAGENS_ENTRADA_DIR="/path/para/imagens"
```

---

## 🧪 Scripts de Prueba y Validación

| Script | Finalidade |
|--------|------------|
| `test_db_connection.sh` | Prueba la conexión PostgreSQL |
| `test_database.sh` | Valida schema e migrations |
| `test_erp_operations.sh` | Smoke test de operações ERP |
| `check_mongodb.sh` | Verifica la conexión y la colección MongoDB |

---

## 🔒 Seguridad

| Recurso | Descripción |
|---------|-----------|
| 🔐 **Senhas Criptografadas** | BCrypt no banco de dados |
| 🛡️ **Filtro de Autenticação** | Em todas as rotas API e web |
| 🔑 **Tokens JWT** | Com expiração configurável |
| 🚫 **SQL Injection** | Protegido via JPA/Hibernate |
| 🌐 **CORS Configurado** | Para APIs externas |
| 📁 **Upload Seguro** | Validação de tipo e tamanho (10MB) |
| 🔐 **mTLS** | Conexão PostgreSQL criptografada com PKI |

---

## 🐛 Resolución de Problemas

### Erro: "Port 8080 already in use"

```bash
# Linux
sudo lsof -i :8080
sudo kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Erro: "Database not found"

Verifique se o PostgreSQL está rodando:

```bash
sudo systemctl status postgresql
psql -h localhost -p 5432 -U sa -d brasil-saas
```

### Erro: "npm not found"

Instale Node.js 22+ LTS:

```bash
# Linux
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs

# Windows (PowerShell)
winget install OpenJS.NodeJS.LTS
```

### Frontend não carrega ou aparece em branco

**Verifique se o build foi executado:**

```bash
cd src/main/resources/static/react
npm install && npm run build
```

**Status do Build:**
- ✅ La compilación ya se ejecutó (2,2 MB en `static/dist/`)
- ✅ 25 componentes React compilados
- ✅ Assets otimizados (JS, CSS, fonts, icons)

**Se precisar rebuild:**
```bash
# Limpar e rebuild
rm -rf ../dist
npm run build

# Verificar output
ls -lh ../dist/assets/
```

**Problemas comuns:**
1. **Cache do navegador** - Ctrl+F5 para hard refresh
2. **CORS errors** - Verificar backend rodando na porta 8080
3. **Token expirado** - Fazer login novamente
4. **Erro 404 em assets** - Confirmar path correto no index.html

### Erro: "No plugin found for prefix 'lint'"

Este erro ocorre ao executar `mvn lint`. O projeto não usa o plugin `maven-lint-plugin`. Use:

```bash
# Para compilar
mvn clean compile

# Para verificar código
mvn spotbugs:check
# ou
mvn pmd:check
```

---

## 📂 Archivos No Referenciados / Backup

Os seguintes arquivos e diretórios são backups ou não estão em uso ativo:

| Tipo | Arquivo/Diretório | Descripción |
|------|-------------------|-----------|
| 📁 Backup | `old-controller-backup/` | Controllers antigos (backup) |
| 📁 Backup | `old-service-backup/` | Services antigos (backup) |
| 📁 Backup | `old-java-backup/` | Classes Java antigas (backup) |
| 📁 Backup | `bkp/` | Diretório de backups gerais |
| 📁 Backup | `sysfluxo - bkp/` | Backup do sistema legado SysFluxo |
| 📄 Backup | `pom.xml2` | Versão alternativa do POM |
| 🐍 Script | `compilar.py` | Script Python de compilação (legado) |
| 🐍 Script | `importa_ncm.py` | Importação NCM (uso pontual) |
| 🐍 Script | `exporta.py` | Exportação de dados |
| 🐍 Script | `exporta2.py` | Exportação de dados (v2) |
| 🐍 Script | `corrige.py` | Script de correções |

---

---

## 📄 Licencia

Este proyecto está bajo la **GNU Affero General Public License v3.0 (AGPLv3)**.

El texto completo, sin cambios, está en [`LICENSE.md`](LICENSE.md).

La AGPLv3 exige que el código fuente se ofrezca a quien use el programa, incluso
cuando el uso es **por red** — de ahí el "Affero". Para un ERP accesible por navegador,
esa cláusula es la que importa: quien apunte su navegador a este sistema tiene derecho
al código.

Los componentes de terceros distribuidos en `src/main/resources/microservices/`
conservan sus propias licencias (MIT, Apache-2.0 y BSD), todas compatibles con la
AGPLv3. Los archivos `LICENSE` originales de cada componente permanecen donde están y
no son sustituidos por este.

© 2026 Brasil SaaS ERP

---

---

---

## ❤️ Apoya el Proyecto

Brasil SaaS ERP es un ERP de código abierto mantenido por un único desarrollador.

Se este proyecto te ayudó, a tu empresa o a tu equipo, considera apoyar su desarrollo.

**PIX:**

```
24adc62c-b073-4587-974d-03fe35f6733f
```

Una donación de cualquier cantidad paga el servidor, el certificado digital y las tasas
de emisión del ayuntamiento. No compra una fecha de entrega en ningún issue.

### 💳 Transferencia internacional

La clave PIX no funciona fuera de Brasil. Para donar desde el exterior, usa una transferencia bancaria.

**Si envías desde un banco de Estados Unidos**, puedes usar estos datos para una
transferencia doméstica. **Si envías desde cualquier otro lugar**, haz una transferencia
internacional Swift.

| | |
|---|---|
| **Nombre** | Euripedes Batista de Paiva Junior |
| **Tipo de cuenta** | Checking |
| **Routing number** (para wire y ACH) | `101019628` |
| **Número de cuenta** | `215822927677` |
| **Nombre y dirección del banco** | Wise US Inc, 108 W 13th St, Wilmington, DE, 19801, United States |
| **SWIFT/BIC** | `TRWIUS35XXX` |

El routing number solo se usa cuando el dinero sale de Estados Unidos. En cualquier otro
lugar, el campo a usar es `SWIFT/BIC`.

---

## 📬 Suporte

Para dudas, sugerencias o reportes de errores:

- 📧 **Email**: euripedesdark@gmail.com
- 🔗 **GitHub Issues**: https://github.com/euripedesdark/BrasilCloudERP/issues
- 📖 **Documentação**: `/docs/`

---

## 🤝 Como Contribuir

El proyecto está bajo AGPLv3 y acepta contribuciones.

1. **Abra uma issue** antes de escrever código, descrevendo o problema ou a melhoria.
   Sem issue beforehand, a mudança pode ir para um caminho que ninguém usa.
2. **Crie uma branch** com nome descritivo: `minha-melhoria`.
3. **Commit por tema.** Un commit hace una cosa; un commit con seis cosas no puede
   ser revertido sozinho.
4. **Rode os testes** antes de abrir o PR: `mvn test`.
5. **Abra o Pull Request** contra a `main` e descreva o que muda e por quê.

```bash
git clone https://github.com/euripedesdark/BrasilCloudERP.git
cd BrasilCloudERP
mvn test
```

### O que não mexer sem issue

| Área | Por qué |
|---|---|
| Base de datos | Un `ALTER` equivocado en producción no tiene vuelta atrás |
| Active Directory | Grupo errado dá acesso a quem não deveria |
| `SQL_AUTHORITIES` | Define a autorização; mudar é decisão de negócio |
| `pg_hba.conf`, `pg_ident.conf` | Controlam quem entra no Postgres |

---

## ❓ Perguntas Frequentes

**O ERP emite nota fiscal hoje?**
Emite NFS-e de São Paulo pela API única na porta 4567. NF-e e NFC-e **não**: o
`NFeServiceImpl` tem 89 linhas e 3 `TODO`s, e nenhuma das três promised operations
está implementada.

**MDF-e e CT-e estão prontos?**
No, aunque respondan `cStat 107`. Ese código es el *estado del servicio*: prueba que
el TLS, el certificado del cliente y el sobre SOAP están correctos. No prueba que el XML
do documento existe, porque nenhum documento foi montado.

**Qual a diferença entre estar em produção e responder?**
Servicios como SVRS y SEFAZ responden a una llamada de estado. Responder no es emitir.
O README separa as duas coisas por isso.

**¿Necesito un certificado A1 para ejecutar esto?**
Para a integração com SEFAZ, sim, um A1 e-CNPJ da ICP-Brasil. O mesmo A1 vale para
NFS-e, MDF-e y CT-e: son usos distintos del mismo certificado, no certificados distintos
diferentes.

**Como a autenticação funciona?**
O ERP autentica contra o Active Directory pelo Auth Service, e a autorização vem dos
grupos del directorio. No hay contraseña local en el ERP: entra quien el AD reconoce.

**Qual banco de dados usa?**
PostgreSQL 18 para dados relacionais, MongoDB para binários e imagens, MinIO para
arquivos grandes.

**Posso mudar a licença?**
A AGPLv3 é a licença atual. Alterar isso é decisão do titular do projeto, e precisa
passar por todos que já receberam o código sob AGPLv3.

**Por que a tela de cadastro de pessoas aparece vazia?**
Isso é frontend, não permissão. A tela abre o formulário e os campos não são
preenchidos; nenhuma configuração de grupo AD corrige.

## 🎉 Contribuidores

| Nombre | Função |
|------|--------|
| **Eurípedes Batista de Paiva Junior** | Desenvolvedor Principal |

---

## 🏆 Comparativa con Grandes ERPs

| Funcionalidad | SAP | Sankya | **Brasil SaaS ERP** |
|----------------|-----|--------|---------------------|
| Multiempresa Nativo | ✅ | ✅ | ✅ |
| Fiscal Brasileiro | ⚠️ Complexo | ✅ | ✅ **Mais ágil** |
| IA Integrada | ❌ | ❌ | ✅ **Spring AI** |
| Código Aberto | ❌ | ❌ | ⚠️ Privado |
| Customização | ⚠️ Cara | ⚠️ Limitada | ✅ **Total** |
| Preço | $$$$ | $$$ | **$$** |
| Suporte Local | ⚠️ Terceiros | ✅ | ✅ **Direto** |
| Cloud-Native | ⚠️ Adaptação | ⚠️ Adaptação | ✅ **Nativo** |
| Observabilidad | ✅ | ⚠️ | ✅ **Completa** |

---

<div align="center">

**Feito com ❤️ usando Spring Boot + React + PrimeReact**

🚀 **Enterprise Ready - Fiscalmente Conforme - Observável**

</div>


---

## 🔐 Autenticación y Autorización

O Brasil SaaS ERP possui dois mecanismos de autenticação:

- **Usuarios ERP**: username + contraseña validada con BCrypt.
- **PostgreSQL SUPERUSER**: username + contraseña autenticados directamente en PostgreSQL. El role debe tener rolsuper=true.

Un role de PostgreSQL común no sustituye la contraseña BCrypt del usuario ERP.

### PostgreSQL SUPERUSER

Cualquier PostgreSQL SUPERUSER puede autenticarse en el ERP con su propia credencial. Si todavía no existe un usuariorio ERP correspondente, o sistema o provisiona automaticamente, garante o perfil ADMIN e emite as authorities:

- ROLE_ADMIN
- ROLE_SUPERADMIN

La contraseña de PostgreSQL nunca se guarda en el ERP.

La conexión normal de la aplicación usa la identidad técnica configurada en el datasource. La autenticación de un SUPERUSER usa uma conexão PostgreSQL temporária com a identidade informada pelo usuário e SSL, sem reutilizar o certificado de cliente da role técnica sa.

### Usuários com perfil GERENTE/DIRETORIA/USUARIO

El perfil define **autorización**, no autenticación. Estos usuarios siguen dependiendo de la contraseña BCrypt registradaa no ERP. Criar uma role PostgreSQL comum com o mesmo username não concede automaticamente acesso.

### Fluxo resumido

    POST /api/auth/login
            |
            +--> usuario ERP + BCrypt ----------------> JWT
            |
            +--> PostgreSQL SUPERUSER ----------------> ADMIN + SUPERADMIN + JWT

Documentação completa: docs/autenticacao.md

Arquitetura: docs/arquitetura.md

### Dependencias del Ecosistema

## Autenticación Corporativa

Brasil SaaS ERP utiliza BrasilCloud Auth Service para la autenticación centralizada.

Auth Service:

https://github.com/euripedesdark/auth-service

Licencia:

AGPLv3

---

## Requisitos del Auth Service

El Auth Service fue diseñado para operar con:

- LDAP
- LDAPS
- Active Directory

Entornos recomendados:

### Linux

Samba Active Directory

Guía completa:

https://medium.com/meetcyber/full-guide-deploying-samba-active-directory-on-fedora-and-rocky-with-hybrid-dns-and-gpo-support-30f47fab1e90

### Windows

Windows Server Active Directory con LDAPS habilitado.

---

## Observación

BrasilCloudERP puede ser evaluado y ejecutado independientemente de la implantación completa del entorno corporativo.

Para producción, se recomienda utilizar el Auth Service integrado a un directorio LDAP/LDAPS.
