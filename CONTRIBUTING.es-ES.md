# Contribuyendo a Brasil SaaS ERP

Gracias por su interés en contribuir a Brasil SaaS ERP. Este documento
proporciona directrices para contribuir al proyecto.

## Código de Conducta

Este proyecto y todos los participantes se rigen por el
[Código de Conducta](CODE_OF_CONDUCT.md). Al participar, se espera que
respete este código.

## ¿Cómo Puedo Contribuir?

### Reportando Bugs

Antes de crear un informe de bug, verifique los issues existentes para ver
si el problema ya fue reportado. Al crear un informe de bug, incluya el
máximo de detalles posible:

* Un título claro y descriptivo
* Los pasos exactos para reproducir el problema
* El comportamiento observado después de seguir los pasos
* El comportamiento esperado
* Capturas de pantalla, si aplica
* Su entorno (SO, versión de Java, navegador, etc.)

### Sugeriendo Mejoras

Las sugerencias de mejoras se rastrean como issues de GitHub. Al crear una
sugerencia, incluya:

* Un título claro y descriptivo
* Una descripción detallada de la mejora propuesta
* Cualquier ejemplo o mockup relevante
* La motivación para la mejora

### Pull Requests

1. Haga un fork del repositorio y cree su rama desde `main`.
2. Si ha añadido código que debe ser probado, añada pruebas.
3. Si ha cambiado APIs, actualice la documentación.
4. Asegúrese de que la suite de pruebas pasa.
5. Asegúrese de que su código sigue el estilo de código existente.
6. Cree un pull request con título y descripción claros.

## Configuración de Desarrollo

### Prerrequisitos

* Java 21 (Oracle JDK o OpenJDK)
* Maven 3.9+
* Node.js 20+
* PostgreSQL 18
* MongoDB 7+
* Redis 7+
* RabbitMQ 3.13+

### Build

```bash
mvn clean package -DskipTests
```

### Ejecutando Pruebas

```bash
mvn test
```

### Ejecutando Localmente

```bash
# Inicie la infraestructura (PostgreSQL, MongoDB, Redis, RabbitMQ)
docker-compose up -d

# Ejecute la aplicación
mvn spring-boot:run
```

## Estructura del Proyecto

```
src/main/java/br/com/brasil_saas/
├── core/           # Autenticación, autorización, multi-tenant
├── cadastro/       # Datos maestros (clientes, proveedores, productos, servicios)
├── financeiro/     # Gestión financiera
├── fiscal/         # Documentos fiscales (NFS-e, NF-e, MDF-e, CT-e, SPED)
├── estoque/        # Gestión de inventario
├── vendas/         # Ventas
├── compras/        # Compras
├── producao/      # Planificación y control de producción
├── rh/             # Recursos humanos
├── crm/            # Gestión de relaciones con el cliente
├── bi/             # Inteligencia de negocios
├── contabilidad/  # Contabilidad
├── ativos/         # Activos fijos
├── dms/            # Gestión de documentos
├── calidad/       # Gestión de calidad
├── projetos/       # Gestión de proyectos
├── wms/            # Gestión de almacén
├── workflow/       # Motor de workflow
├── portais/        # Portales de cliente/proveedor
├── ia/             # Asistente de IA
├── servicos/       # Órdenes de servicio
└── relatorios/     # Informes
```

## Estándares de Código

* Siga el estilo de código existente (Spring Boot, Lombok, MapStruct).
* Use nombres de variables y métodos significativos.
* Escriba mensajes de commit claros.
* Añada Javadoc para métodos públicos.
* Mantenga métodos pequeños y enfocados en una sola responsabilidad.

## Licencia

Al contribuir, usted acepta que sus contribuciones serán licenciadas bajo la
GNU Affero General Public License v3.0 (AGPLv3).
