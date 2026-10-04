# Política de Seguridad

Tomamos la seguridad de Brasil SaaS ERP en serio. Si cree que ha encontrado
una vulnerabilidad de seguridad, repórtela de manera responsable.

## Cómo Reportar

**No abra issues públicas para vulnerabilidades de seguridad.**
En su lugar, envíe un correo electrónico a:

**euripedesdark@gmail.com**

Incluya la siguiente información:

* Una descripción de la vulnerabilidad
* Pasos para reproducir el problema
* El impacto potencial
* Cualquier sugerencia de corrección (si está disponible)

## Qué Esperar

* Reconoceremos su informe en un plazo de 48 horas.
* Investigaremos el problema y proporcionaremos un cronograma para la corrección.
* Lo acreditaremos en las notas de lanzamiento (a menos que prefiera permanecer anónimo).
* Lanzaremos una corrección lo antes posible y le notificaremos cuando esté disponible.

## Alcance

Esta política de seguridad se aplica a:

* El código de la aplicación Brasil SaaS ERP
* Los endpoints de la API
* El esquema de la base de datos y las migraciones
* Los scripts de despliegue y configuraciones

## Fuera del Alcance

* Bibliotecas de terceros (reporte vulnerabilidades a los respectivos proyectos)
* Problemas en la documentación
* Preguntas generales sobre mejores prácticas de seguridad

## Medidas de Seguridad

El proyecto implementa las siguientes medidas de seguridad:

* **Autenticación:** Autenticación basada en JWT con refresh tokens
* **Autorización:** Control de acceso basado en roles (RBAC) con permisos
* **Multi-tenant:** Aislamiento de datos entre empresas
* **Cifrado:** TLS para todas las comunicaciones, mTLS para conexiones con base de datos
* **Validación de entrada:** Bean Validation en todos los endpoints de la API
* **Prevención de SQL injection:** Consultas parametrizadas vía JPA
* **Prevención de XSS:** Escape integrado de React
* **Protección CSRF:** Diseño de API stateless
* **Auditoría:** Registros de acceso y pistas de auditoría

## Política de Divulgación

Seguimos una política de divulgación coordinada. Le pedimos que:

* Nos dé un tiempo razonable para corregir el problema antes de divulgarlo públicamente.
* No explote la vulnerabilidad más allá de lo necesario para demostrarla.
* No acceda ni modifique datos pertenecientes a otros usuarios.
