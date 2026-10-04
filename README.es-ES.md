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
| **Migrations** | Flyway | Latest | Migrations versionadas (V109…V126) |
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
- ✅ 132 componentes implementados (41.338 líneas)
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

El sistema tiene **22 módulos de negocio** sobre un núcleo común
(`core`), todos con controller, service, repository y pantalla.
Números verificados el **03/10/2026** en el código:

| | |
|---|---|
| Clases Java | **762** |
| Repositorios Spring Data | **174** |
| Services | **147** |
| Endpoints REST | **451** |
| Tablas en PostgreSQL (schema `brasil_saas`) | **204** |
| Componentes React | **123** |
| Migrations Flyway en el repo | **15** (`V109`…`V126`) |

### 💰 Financeiro

> **Status**: ✅ implementado y respondiendo (`/api/financeiro/*` → 200)

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Cuentas por Pagar/Cobrar** | Títulos, parcelas, flujo de aprobación multinivel, alertas de vencimiento |
| ✅ **Baixas** | Con descuentos, intereses, multas y estornos |
| ✅ **Tesorería** | Caja, proyección de flujo de caja, préstamos y aplicaciones financieras |
| ✅ **Contabilidad General** | Asientos y partidas contables, plan de cuentas, centros de costo |
| ✅ **Bancos** | Cuentas bancarias, extracto, conciliación bancaria, remesas y retornos (CNAB/OFX) |
| ✅ **Presupuestos** | Presupuestado vs realizado |
| ✅ **Comisiones** | Reglas de comisión sobre ventas y OS |
| ✅ **Renegociación** | Renegociación de títulos |
| ✅ **Provisión PDD** | Provisión para deudores dudosos |
| ✅ **Análisis de Rentabilidad** | Por centro de costo, plan de cuentas y período |

**Endpoints**: `/api/financeiro/titulos`, `/lancamentos`, `/orcamentos`,
`/emprestimos`, `/planos-contas`, `/centros-custo`, `/contas-bancarias`,
`/caixas`, `/condicoes-pagamento`, `/tipos-pagamento`, `/conciliacao`,
`/extrato`, `/comissoes`, `/renegociacao`, `/remessas`, `/retornos` …
(55 endpoints en el módulo)

---

### 🏙️ Cadastros

| Registro | Funcionalidades |
|----------|-----------------|
| ✅ **Municipios** | Tabla oficial IBGE, búsqueda por código o nombre, CEP |
| ✅ **Personas** | Registro único PF/PJ, contactos, direcciones |
| ✅ **Clientes / Proveedores** | Límite de crédito, logo, evaluación, histórico |
| ✅ **Productos** | Variaciones, kits, NCM, imágenes (MongoDB), e-commerce |
| ✅ **Servicios** | Para NFS-e, integración con OS |
| ✅ **Categorías / Marcas / Unidades de Medida / Transportadoras** | Apoyo al registro de productos y fletes |

**Endpoints**: `/api/cadastro/produtos`, `/clientes`, `/fornecedores`,
`/pessoas`, `/servicos`, `/categorias`, `/marcas`, `/unidades-medida`,
`/transportadoras` + `/api/municipios` (31 endpoints en el módulo)

---

### 📋 Serviços e Vendas

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Orden de Servicio** | Emisión, ítems, apuntes, PDF, emisión de NFS-e integrada |
| ✅ **Pedidos de Venta** | Integración con inventario, finanzas y fiscal |
| ✅ **PDV** | Pantalla de punto de venta |
| ✅ **Tablas de Precio** | Ítems por producto |
| ✅ **Bonificaciones y Devoluciones** | Con ítems |
| ✅ **Contratos de Venta** | Con ítems y reglas de comisión |

**Endpoints**: `/api/servicos/os`, `/api/vendas/pedidos`,
`/api/vendas/tabelas-preco` … (15 endpoints en los dos módulos)

---

### 🛒 Compras y Supply Chain

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Pedidos de Compra** | Con ítems, integración con proveedores |
| ✅ **Recepciones** | Verificación de ítems |
| ✅ **Verificación de Facturas** | Validación de factura contra lo recibido |
| ✅ **Supply Chain** | Solicitudes de compra y cotizaciones con mapa de proveedores |
| ✅ **Contratos de Compra** | Con ítems |

**Endpoints**: `/api/compras/pedidos`, `/recebimentos`,
`/supply-chain/solicitacoes`, `/supply-chain/cotacoes` … (12 endpoints)

---

### 📦 Estoque

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Depósitos y Direcciones** | Ocupación por dirección |
| ✅ **Saldos, Lotes y Series** | Trazabilidad |
| ✅ **Movimientos** | Entradas, salidas, ajustes |
| ✅ **Reservas y Transferencias** | Entre depósitos |
| ✅ **Inventarios** | Con ítems |
| ✅ **Expediciones** | Con ítems |

**Endpoints**: `/api/estoque/depositos`, `/saldos`, `/movimentacoes`,
`/lotes`, `/reservas`, `/transferencias`, `/inventarios`, `/expedicoes` …
(17 endpoints)

---

### 🏭 Produção Industrial (PCP)

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Órdenes de Producción** | Flujo completo: insumos → proceso → producto final |
| ✅ **Estructura de Producto (BOM)** | Por producto padre |
| ✅ **Rutas y Operaciones** | Secuencia de operaciones |
| ✅ **Centros de Trabajo y Capacidad** | Programación por capacidad |
| ✅ **Apuntes** | Por producción, funcionario, período y estado; estadísticas |
| ✅ **Albaranes** | Con ítems |
| ✅ **MRP** | Planeación de necesidades |

**Endpoints**: `/api/producao/estruturas`, `/roteiros`, `/centros-trabalho`,
`/capacidade`, `/apontamentos`, `/romaneios`, `/mrp` … (30 endpoints)

---

### 📊 Contabilidad

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Asientos y Partidas** | Contabilidad en partidas dobles |
| ✅ **Balancete, Balance, DRE, Razón** | Informes contables |
| ✅ **Cierres** | Cierre de período |

**Endpoints**: `/api/contabilidade/lancamentos`, `/balancete`, `/balanco`,
`/dre`, `/razao`, `/fechamentos` (15 endpoints)

---

### 🏢 Activos (Gestión de Activos)

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Activo Fijo** | Registro de bienes |
| ✅ **Mantenimientos** | Historial de mantenimiento de los activos |

**Endpoints**: `/api/ativos/manutencoes` (4 endpoints)

---

### 📁 DMS (Gestión de Documentos)

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Documentos** | Con contenido, versiones y aprobaciones |
| ✅ **Retención** | Política de retención |

**Endpoints**: `/api/dms/documentos`, `/retencao`, `/versoes/{id}/download` (9 endpoints)

---

### ✅ Calidad

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Planes de Inspección** | Planes por producto/proceso |
| ✅ **Inspecciones** | Registro de inspección |
| ✅ **No conformidades** | Tratamiento de NC |

**Endpoints**: `/api/qualidade/planos`, `/inspecoes`, `/nao-conformidades` (7 endpoints)

---

### 📈 Proyectos

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Proyectos** | Resumen, etapas, movimientos |
| ✅ **Facturación** | Facturación por proyecto |
| ✅ **Riesgos y Cambios** | Registro de riesgos y cambios |

**Endpoints**: `/api/projetos`, `/projetos/{id}/resumo`, `/etapas`,
`/movimentos`, `/faturamentos`, `/riscos`, `/mudancas` (14 endpoints)

---

### 🏗️ WMS (Warehouse)

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Ondas de Picking** | Con ítems |
| ✅ **Volúmenes** | Con ítems |
| ✅ **Putaway** | Ubicación de carga |

**Endpoints**: `/api/wms/ondas`, `/volumes`, `/putaway` (14 endpoints)

---

### 🔄 Workflow

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Definiciones** | Con etapas |
| ✅ **Instancias y Tareas** | Tareas pendientes por usuario |

**Endpoints**: `/api/workflow/definitions`, `/instances`, `/tasks/pendentes` (10 endpoints)

---

### 🌐 Portales

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Accesos** | Control de acceso a portales |
| ✅ **Validación Pública** | Validación y "mi cuenta" sin login del ERP |

**Endpoints**: `/api/portais/acessos`, `/publico/validar`, `/publico/minha-conta` (5 endpoints)

---

### 👥 RH

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Funcionarios** | Vínculo con persona, cargo, foto |
| ✅ **Cargos** | Estructura de cargos |
| ✅ **Nómina** | Con ítems |

**Endpoints**: `/api/rh/funcionarios`, `/cargos`, `/folhas` (14 endpoints)

---

### 🤝 CRM

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Leads y Oportunidades** | Pipeline de ventas |
| ✅ **Actividades y Tareas** | Seguimiento |
| ✅ **Forecast** | Previsión de ventas |

**Endpoints**: `/api/crm/leads`, `/pipeline`, `/forecast`, `/atividades` (10 endpoints)

---

### 📊 BI (Inteligencia de Negocios)

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Dashboards** | Públicos, por tipo y por usuario, con widgets |
| ✅ **KPIs e Indicadores** | Cálculo de KPI por tipo |
| ✅ **Informes** | Por categoría, exportación **PDF, Excel y CSV** |
| ✅ **Informes Agendados** | Por frecuencia, cola de pendientes |
| ✅ **Reports con Parámetros** | Informes parametrizados |

**Endpoints**: `/api/bi/dashboards`, `/kpis`, `/indicadores`, `/relatorios`,
`/relatorios-agendados`, `/reports` … (46 endpoints — el módulo más grande)

---

### 🤖 IA Asistiva

| Recurso | Status |
|---------|--------|
| ✅ **Chat con IA** | Sesiones y mensajes persistidos |
| ✅ **Asistente ERP** | Con auditoría de uso |
| ✅ **Embeddings** | Búsqueda semántica por entidad |
| ✅ **Clasificaciones y Análisis Predictivos** | Modelos entrenables |
| ✅ **Prompts y Templates** | Biblioteca reutilizable |
| ⚠️ **Proveedor** | Configurable (`app.ia.provider`, por defecto `openai`); necesita clave válida |

**Endpoints**: `/api/ia/config`, `/sessoes`, `/mensagens`, `/prompts`,
`/prompt-templates`, `/embeddings`, `/classificacoes`, `/analises` …
(74 endpoints)

---

### 🔐 Core, Auth y Superadmin

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Autenticación** | JWT, login en base de datos, login AD (SPNEGO), refresh token |
| ✅ **Usuarios, Perfiles y Permisos** | Autorización por permiso (`@PreAuthorize`) |
| ✅ **Multiempresa** | Usuario ↔ empresa(s), aislamiento por empresa |
| ✅ **Superadmin** | Catálogo SQL, módulos por usuario, perfiles disponibles |
| ✅ **Auditoría** | Log de acceso, notificaciones, sesiones |

**Endpoints**: `/api/auth/login`, `/login/database`, `/login/ad`, `/refresh`,
`/me` + `/api/core/perfil`, `/core/minha-empresa` + `/api/superadmin/usuarios`,
`/superadmin/sql/catalogo` … (33 endpoints)

---

### 🖨️ Informes

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Informes por Tipo** | HTML y PDF (`/api/relatorios/{tipo}`, `/api/relatorios/pdf/{tipo}`) |

---

### 📄 Documentos (shared)

| Funcionalidad | Descripción |
|----------------|-----------|
| ✅ **Contenido de Documento** | `/api/documentos/{id}/conteudo` — archivos guardados en MongoDB |

---

## 🗂️ Estructura del Proyecto

```
BRASIL-SAAS-ERP/
├── src/main/java/br/com/brasil_saas/     # Monólito principal (762 classes Java)
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

**Contar archivos no mide la preparación.** Medí los módulos por los mismos
6 entregables del fiscal y casi todos dan 100% estructural — porque
todos tienen model, repository,
service, controller e pantalla. Eso no
significa que el registro de cliente abre, que el título baja o que la
nómina calcula. Significa que los archivos existen.

Lo que mide es ejecutar. Entonces hay dos columnas:

| Módulo | Tabelas | Endpoints | Pantalla | Estructural | Responde (03/10/2026) |
|--------|:-------:|:---------:|:--------:|:----------:|:----------------------:|
| Cadastro | 21 | 31 | ✅ | 6/6 | ✅ |
| Financeiro | 32 | 55 | ✅ | 6/6 | ✅ |
| Estoque | 13 | 17 | ✅ | 5/6 | ✅ |
| Fiscal | 30 | 28 | ✅ | 6/6 | ⚠️ parcial (ver % fiscal) |
| Vendas | 11 | 7 | ✅ | 6/6 | ✅ |
| Compras | 11 | 12 | ✅ | 6/6 | ✅ |
| Produção | 10 | 30 | ✅ | 6/6 | ✅ |
| RH | 4 | 14 | ✅ | 6/6 | ✅ |
| Serviços | 3 | 8 | ✅ | 6/6 | ✅ |
| BI | 8 | 46 | ✅ | 6/6 | ✅ |
| Core/Auth | 18 | 33 | ✅ | 6/6 | ✅ |
| IA | 11 | 74 | ⚠️ 1 pantalla | 6/6 | ⚠️ parcial |
| Contabilidade | 3 | 15 | ✅ | 6/6 | ✅ |
| CRM | 4 | 10 | ✅ | 6/6 | ✅ |
| Ativos | 2 | 4 | ✅ | 6/6 | ✅ |
| DMS | 3 | 9 | ✅ | 6/6 | ✅ |
| Qualidade | 3 | 7 | ✅ | 6/6 | ✅ |
| Projetos | 6 | 14 | ✅ | 6/6 | ✅ |
| WMS | 4 | 14 | ✅ | 6/6 | ✅ |
| Workflow | 4 | 10 | ✅ | 6/6 | ✅ |
| Portais | 1 | 5 | ✅ | 6/6 | ✅ |
| Relatórios | — | 2 | ✅ | 6/6 | ✅ |

**Estrutural** = os 6 entregáveis existem como arquivo.
**Responde** = a API respondeu num smoke test com token válido.

⚠️ **Nenhum destes foi validado funcionalmente.** Abri a tela, não. Criei
registro, não. O que o smoke test prova é que a **rota responde**; não prova
que o dado devolvido esteja certo. As duas coisas são diferentes, e confundir
elas é como se descobre que "o módulo está pronto" e a tela mostra valor errado.

### Lo que encontró el smoke test (03/10/2026)

67 rutas llamadas, una o más por módulo:

```
62  2xx   responden
 5  400   parámetro obligatorio faltante (la ruta existe)
 0  500   ningún error
```

Los 400 son el comportamiento esperado de validación
(`/api/bi/indicadores/dashboard`, `/api/ia/config`, `/api/ia/sessoes`,
`/api/producao/apontamentos/por-status`, `/api/producao/apontamentos/por-periodo`,
`/api/contabilidade/balancete`, `/api/fiscal/sefaz/status`,
`/api/wms/putaway`) — piden un parámetro que el test no pasó.

### El bug de RH fue corregido

El smoke test de 26/09/2026 encontró un **500** real:
`GET /api/rh/funcionarios` rompía con
`LazyInitializationException` — `Funcionario` tiene
`@ManyToOne(fetch = LAZY)` para `Cargo`, y el Jackson serializaba el
proxy después de que la sesión de Hibernate cerraba.

**En 03/10/2026 está corregido:** `/api/rh/funcionarios/pessoas`,
`/api/rh/cargos` y `/api/rh/folhas` responden **200**. La corrección
fue buscar con `join fetch` en `Cargo` (o DTO), lo que evita que el
serializer toque el proxy fuera de la sesión.

### Estoque no tiene capa de service

9 controllers, 12 repositories, **0 services**. La regla de negocio
está dentro del controller, hablando directo al repository. Funciona,
pero no tiene dónde testear y no aísla la regla. En los otros módulos
la separación existe — es una inconsistencia a decidir, no un accidente.

## ✅ Qué Está Corriendo

Verificado en **03/10/2026** vía `systemctl` y `ss`.

| Servicio (systemd) | Puerto | Estado |
|-------------------|-------|--------|
| `brasil-saas-erp.service` (Spring Boot, user `brasilsaas`) | 8080 | 🟢 running |
| `nfse-sp-api.service` (Java, primaria) | 4568 | 🟢 running |
| `nfse-sp-bridge.service` (Ruby, fallback) | 4569 | 🟢 running |
| `brasil_saas-watchdog.service` (verificación de contrato) | — | 🟢 running |
| `brasil_saas-minio.service` | 9000 / 9001 | 🟢 running |
| `auth-service.service` | 8081/8082 | 🟢 running |
| PostgreSQL (schema `brasil_saas`) | 5432 | 🟢 up |
| MongoDB | 27017 | 🟢 up (4 colecciones) |
| RabbitMQ | 5672 / 15672 | 🟢 up |
| Redis | 6379 | 🟢 up |
| nginx (proxy del dominio) | 80 / 443 | 🟢 up |

El ERP corre del jar
`/opt/brasil-saas-erp/brasil-saas-erp-1.0.0-SNAPSHOT.jar`
(unit `brasil-saas-erp.service`). Los microservicios de NF-e corren de
los jars dentro de `src/main/resources/microservices/` — **atención**:
en el servidor, los jars activos están en
`/home/euripedes/BrasilCloudERP/src/main/resources/microservices/`
(otro checkout), no en este repo.

### ⚠️ El proxy failover de NFS-e (4567) está caído

El ERP llama la emisión de NFS-e en
`brasil-saas.fiscal.nfse.url`, que por defecto es
`http://127.0.0.1:4567/api/nfse-sp` — el **proxy failover**. En
03/10/2026 **nada escucha en 4567** (conexión rechazada), aunque las
dos implementaciones (4568 Java, 4569 Ruby) y el watchdog estén de
pie.

Consecuencia: a menos que `/etc/brasil-saas/erp.env` (archivo del
usuario `brasilsaas`, ilegible sin sudo) sobrescriba la URL hacia la
4568, **la emisión de NFS-e está rota ahora**. Evidencia: la última
NFS-e en la base es de **26/09/2026** (nota 33), con 4 registros
`FALHA_EMISSAO` en ese día y ningún intento desde entonces.

Acción: levantar el proxy de la 4567 (o apuntar
`brasil-saas.fiscal.nfse.url` directamente a
`http://127.0.0.1:4568/api/nfse-sp`), y registrar en `erp.env` qué
URL está en uso.

### Lo que el ERP valida solo

| Verificación | Resultado (03/10/2026) |
|-------------|------------------------|
| `GET /api/fiscal/mdfe/status` → SVRS | **200** (`cStat 107`) |
| `GET /api/fiscal/cte/status` → SEFAZ SP | **200** (`cStat 107`) |
| `GET /api/fiscal/sped/efd/exemplo` | **200** (archivo válido) |
| `GET /api/fiscal/nfse/retornos/recusas` y `/para-conferir` | **200** |

El MDF-e es atendido por la **SVRS** (`ufAtendente: RS`) y el CT-e por el
**portal de SP** (`ufAtendente: SP`), con el **mismo A1**. Son
autorizadores diferentes, no certificados diferentes.

### Lo que aún no está listo

| Item | Situación |
|------|----------|
| **Proxy failover 4567** | Caído; URL por defecto del ERP. Ver arriba. |
| **NF-e / NFC-e** | `NFeServiceImpl` tiene **3 TODOs**. La interfaz promete `emitirNFe`, `cancelarNFe` y `consultarSituacao`; ninguna de las tres está implementada. |
| **Emisión de MDF-e** | La conexión con la SVRS está lista (`cStat 107`) y el certificado firma. **El montaje del documento no existe**: `MdfeEmissaoService` tiene solo `statusServico` y `consultarRecibo`. |
| **Emisión de CT-e** | Ídem. `CteEmissaoService` tiene solo `statusServico`. |
| **SPED EFD Contribuciones** | La biblioteca resuelve en el `pom.xml`, pero **no hay endpoint** — solo el ICMS/IPI tiene. |
| **IA (Spring AI)** | Módulo presente (74 endpoints), con embeddings, chat y classifications. No fue validado funcionalmente; necesita clave de API válida. |
| **Pantalla de la IA** | 1 pantalla solo (`IaAssistWidget`); los 74 endpoints no tienen pantalla equivalente. |
| **Validación del CIOT** | `cStat 684` entra en producción en la SEFAZ en **23/11/2026**. El `infCIOT` ya está en el XSD; falta la validación en el emisor. |

**Por qué el MDF-e y el CT-e no están en la lista de "listos" a pesar de
responder `cStat 107`:** el `cStat 107` es el *status del servicio*, y
prueba que la cadena TLS, el certificado del cliente y el envelope SOAP
están correctos. **No prueba que el XML del documento esté**, porque
ningún documento fue montado ni enviado. Son dos cosas diferentes, y
el README las separa por eso.

---

## 📡 Endpoints de la API

El backend expone **451 endpoints REST** (`/api/**`). La lista
completa está en [`docs/INDICE.md`](docs/INDICE.md) y en
el Swagger UI: <http://localhost:8080/swagger-ui.html>.

### Municípios

```
GET    /api/municipios/paginado?page=0&size=20&sort=nome,asc
GET    /api/municipios/buscar?termo={codigo_ou_nome}
GET    /api/municipios/codigo/{codigoIbge}
GET    /api/municipios/{id}
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
GET    /api/financeiro/orcamentos
GET    /api/financeiro/emprestimos
GET    /api/financeiro/planos-contas
GET    /api/financeiro/centros-custo
GET    /api/financeiro/contas-bancarias
GET    /api/financeiro/caixas
GET    /api/financeiro/condicoes-pagamento
GET    /api/financeiro/tipos-pagamento
GET    /api/financeiro/remessas
GET    /api/financeiro/retornos
```

### Orden de Servicio

```
GET    /api/servicos/os/{id}
GET    /api/servicos/os/{id}/itens
GET    /api/servicos/os/{id}/pdf
POST   /api/servicos/os
PUT    /api/servicos/os/{id}
POST   /api/servicos/os/{id}/emitir-nota
POST   /api/servicos/os/{id}/fechar
```

### Relatórios

```
GET    /api/relatorios/{tipo}
GET    /api/relatorios/pdf/{tipo}
GET    /api/relatorios/financeiro?inicio=YYYY-MM-DD&fim=YYYY-MM-DD
GET    /api/relatorios/os-pdf/{id}
GET    /api/relatorios/dre?competencia=YYYY-MM
```

### BI

```
GET    /api/bi/dashboards                      /dashboards/publicos   /dashboards/tipo/{tipo}
GET    /api/bi/kpis/{id}/calculate             /bi/kpis/type/{kpiType}
GET    /api/indicadores                     /indicadores/categoria/{categoria}
GET    /api/relatorios/{id}                 /relatorios/{id}/pdf  /excel  /csv
GET    /api/relatorios-agendados            /relatorios-agendados/pendentes
GET    /api/reports/{id}                    /reports/category/{category}
```

### Fiscal — NFS-e São Paulo

La emisión la hace una API separada. El ERP llama
`brasil-saas.fiscal.nfse.url` (por defecto
`http://127.0.0.1:4567/api/nfse-sp`, el proxy failover —
ver el aviso en "Qué Está Corriendo"). Las implementaciones
activas son la **Java en la 4568** (primaria) y la **Ruby en la 4569**
(fallback), vigiladas por el `brasil_saas-watchdog`, que chequea el
**contrato** de respuesta, no solo el puerto.

```
POST   /api/fiscal/nfse/emitir
POST   /api/fiscal/nfse/{id}/cancelar
GET    /api/fiscal/nfse
GET    /api/fiscal/nfse/{id}/xml
GET    /api/fiscal/nfse/{id}/pdf
GET    /api/fiscal/nfse/{id}/retornos
GET    /api/fiscal/nfse/{id}/retornos/{retornoId}/bruto
GET    /api/fiscal/nfse/retornos/recusas           solo las recusadas
GET    /api/fiscal/nfse/retornos/para-conferir     las que no se pudo saber
```

El motivo del rechazo de la prefectura se graba **antes** de que la
excepción suba, con el `cStat` y el cuerpo bruto en Mongo. La
pantalla de error es una pared; el registro no.

### Fiscal — MDF-e y CT-e

```
GET    /api/fiscal/mdfe/status                     cStat 107 si la SVRS está de pie
GET    /api/fiscal/mdfe/recibo?numero={recibo}     2ª llamada: del recibo a la clave
GET    /api/fiscal/cte/status                      cStat 107 si la SEFAZ está de pie
```

Los dos usan el mismo contrato de respuesta, con los **tres
estados** de `sucesso`: `true` (autorizado), `false`
(recusado, con `cStat` y motivo) y `null` (**no se pudo
saber**). El tercer estado existe para no duplicar
documento: la SVRS rechaza clave natural repetida, y reemitir
a ciegas es exactamente lo que crea la duplicidad.

El MDF-e devuelve **recibo** en la primera llamada y
**protocolo** en la segunda. Leer la clave en la respuesta del
envío da NPE — el método no existe.

### Fiscal — SPED EFD ICMS/IPI

```
POST   /api/fiscal/sped/efd/gerar                  genera el archivo EFD
GET    /api/fiscal/sped/efd/exemplo                genera el de ejemplo, para conferir el formato
```

**EFD no se envía a nadie.** El archivo se genera,
firma y guarda; quien busca después es la SEFAZ o la
Receita. Sin web service, sin protocolo, sin fila.

### Fiscal — consulta y distribución

```
GET    /api/fiscal/sefaz/status
GET    /api/fiscal/sefaz/consultar
GET    /api/fiscal/sefaz/distribuicao
```

### Fiscal — tablas y registros

```
GET    /api/fiscal/cest?busca={termo}              búsqueda por código, descripción o NCM
GET    /api/fiscal/cfop?tipoOperacao=ENTRADA|SAIDA
GET    /api/fiscal/ncm
GET    /api/fiscal/ncm/{codigo}
GET    /api/fiscal/issqn
GET    /api/fiscal/impostos
GET    /api/fiscal/certificados
```

`GET /api/cadastro/servicos?nome={termo}` alimenta el
autocomplete del nombre del servicio, que sugiere servicio ya
registrado mientras la persona escribe. Dos registros con nombre
parecido y código municipal diferente es la causa más común de
rechazo en la prefectura.

### Demais módulos (raíces)

```
GET    /api/cadastro/produtos      /clientes   /fornecedores   /pessoas   /servicos
GET    /api/estoque/depositos      /saldos     /movimentacoes  /lotes     /reservas
       /estoque/transferencias     /inventarios /expedicoes
GET    /api/vendas/pedidos         /vendas/tabelas-preco
GET    /api/compras/pedidos        /compras/recebimentos
       /compras/supply-chain/solicitacoes
GET    /api/producao/estruturas    /producao/roteiros  /producao/centros-trabalho
       /producao/apontamentos      /producao/romaneios
GET    /api/contabilidade/lancamentos /contabilidade/balancete /contabilidade/dre
GET    /api/rh/funcionarios/pessoas /rh/cargos /rh/folhas
GET    /api/crm/leads              /crm/pipeline   /crm/forecast  /crm/atividades
GET    /api/ativos/manutencoes
GET    /api/dms/documentos         /dms/retencao
GET    /api/qualidade/planos       /qualidade/inspecoes /qualidade/nao-conformidades
GET    /api/projetos               /projetos/{id}/resumo /projetos/{id}/etapas
GET    /api/wms/ondas              /wms/putaway
GET    /api/workflow/definitions   /workflow/instances /workflow/tasks/pendentes
GET    /api/portais/acessos        /portais/publico/validar
GET    /api/ia/config              /ia/sessoes   /ia/prompts   /ia/embeddings
GET    /api/core/perfil            /core/minha-empresa  /core/recent/updates
GET    /api/superadmin/usuarios    /superadmin/sql/catalogo
```

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
