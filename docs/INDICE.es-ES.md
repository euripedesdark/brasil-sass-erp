# Índice de documentación — Brasil SaaS ERP

> Actualizado el 04/10/2026 — inventario contrastado con el código, la base de datos y la prueba smoke.

La raíz del repositorio contiene `README.md`, que enlaza a las cuatro versiones lingüísticas del README principal. El resto de la documentación se organiza aquí por tema.

## Documentación principal (cuatro idiomas)

| Idioma | Archivo |
|---|---|
| 🇧🇷 Portugués (Brasil) | [README.pt-BR.md](./i18n/README.pt-BR.md) |
| 🇺🇸 Inglés | [README.en-US.md](./i18n/README.en-US.md) |
| 🇪🇸 Español | [README.es-ES.md](./i18n/README.es-ES.md) |
| 🇫🇷 Francés | [README.fr-FR.md](./i18n/README.fr-FR.md) |

## Documentación activa (estado actual del sistema)

| Documento | Contenido |
|---|---|
| [Módulos](modulos/) | Un archivo por módulo con endpoints, tablas y funciones |
| [Índice de módulos](modulos/README.md) | Resumen de módulos |
| [Navegación](navegacao.md) | Rutas del frontend y estructura de menús |
| [Arquitectura](arquitetura/) | Arquitectura del sistema |
| [Modelo de datos](dados/modelo-dados.md) | Modelo de datos (204 tablas, según el documento fuente) |
| [Autenticación](dados/autenticacao.md) | Autenticación y autorización |
| [Datos maestros](dados/cadastro.md) | Módulo de datos maestros |
| [Instrucciones del propietario](ia/INSTRUCOES-DO-DONO.md) | Instrucciones para el propietario del sistema |
| [Infraestructura](infra/) | Compilación, despliegue, restauración y microservicios |
| [Guía de módulos](infra/README_MODULES.md) | Módulos del sistema |
| [Guía de microservicios](infra/README_MICROSSERVICOS.md) | Microservicios fiscales |
| [Guía de compilación](infra/BUILD.md) | Cómo compilar |
| [Guía de migraciones](infra/MIGRATION.md) | Migraciones Flyway |
| [Guía de restauración](infra/RESTAURAR.md) | Restauración de la base de datos |
| [Perfiles de acceso](perfis/) | Reglas de perfiles |
| [Registro de cambios](guia/CHANGELOG.md) | Historial de cambios |

## Documentos de trabajo de desarrollo

> ⚠️ Estos archivos son registros de sesiones de desarrollo generados con ayuda de IA. No son documentación oficial del producto.

| Documento | Contenido |
|---|---|
| [Documentos de trabajo de IA](ia/) | Notas e investigaciones de desarrollo |

## Referencias de arquitectura y datos

| Área | Contenido |
|---|---|
| [Arquitectura](arquitetura/) | Arquitectura del sistema |
| [Datos](dados/) | Autenticación, datos maestros y modelo de datos |

## Auditorías e informes (instantáneas históricas)

> ⚠️ Estos documentos describen el sistema en la fecha indicada en el nombre del archivo y pueden no reflejar el estado actual. Para consultar el estado actual, use los README y la documentación activa anteriores.

| Documento | Contenido |
|---|---|
| [Auditorías](auditorias/) | Auditorías y mapas de cobertura |
| [Informes](relatorios/) | Informes de pruebas y diagnósticos |
| [Incidentes](incidentes/) | Registros de incidentes y coordinación |
| [Auditoría de autenticación](ia/AUDITORIA_AUTENTICACAO_FRONTEND_BACKEND_24-09-2026.md) | Auditoría de autenticación del 24/09/2026 |
| [Informe de paridad funcional](ia/RELATORIO_PARIDADE_FUNCIONAL_ERP_25-09-2026.md) | Paridad funcional del 25/09/2026 |

## Referencias de investigación

| Documento | Contenido |
|---|---|
| [Directorio de investigación](pesquisa/) | Investigación técnica y análisis |
| [Manual de API fiscal](pesquisa/MEGA-MANUAL-API-FISCAL.md) | Referencia de la API fiscal |
| [APIs de transporte, SPED, MDF-e y CT-e](pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md) | APIs de transporte y documentos fiscales |
| [Importación de NF-e por XML](pesquisa/IMPORTACAO-NFE-POR-XML.md) | Importar NF-e desde XML |
| [Registro municipal](pesquisa/REGISTRO-da-prefeitura-NFSE.md) | Registro municipal de facturas de servicios |
| [Contrato unificado de NFS-e](pesquisa/contrato-nfse-unico.md) | Contrato unificado de respuesta |

## Decisiones de arquitectura

| Documento | Contenido |
|---|---|
| [Arquitectura de identidad](ia/ARQUITETURA-IDENTIDADE.md) | Arquitectura de identidad |
| [Identidad con Active Directory](ia/IDENTIDADE-AD.md) | Integración con Active Directory |
| [Mapa de microservicios fiscales](ia/MAPA-MICROSERVICOS-FISCAIS.md) | Microservicios fiscales |
| [Mapa de NFC-e](ia/MAPA-NFCE.md) | Factura electrónica de consumo |

## Informes pendientes

| Documento | Contenido |
|---|---|
| [Informe de NCM pendientes](ia/RELATORIO-NCM-PENDENTES.md) | Clasificaciones NCM pendientes |
| [PDF de ISSQN pendientes](ia/ISSQN-PDFS-PENDENTE.md) | Archivos PDF de ISSQN pendientes |
| [Elemento de failover pendiente](ia/FAILOVER-4567-PENDENTE.md) | Elemento de failover 4567 |

## Internacionalización de la documentación

[Plan de internacionalización Markdown](i18n/MARKDOWN_I18N_COMPLETO_2026-10-04.md) — alcance y reglas para traducir la documentación Markdown propia del producto.
