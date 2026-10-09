# Stripe por empresa

La integración de Stripe es **multiempresa**. Las credenciales de Stripe pertenecen al tenant de `brasil_saas.bc_core_empresa`; ya no existe una `STRIPE_SECRET_KEY` global compartida por todas las empresas.

## Quién puede configurarla

Solo pueden configurarla estas autoridades:

- `ROLE_DIRETORIA`
- `ROLE_GESTOR`
- `ROLE_SUPERUSER`

El backend es la autoridad final. Ocultar la sección en React no sustituye a `@PreAuthorize`.

## Configuración de la empresa

La pantalla **Configurar empresa** permite:

- Secret API Key de Stripe;
- Webhook Signing Secret;
- activar o desactivar Stripe;
- probar la conexión.

Ambas credenciales se almacenan cifradas con AES-GCM. La clave maestra de cifrado nunca se guarda en la base de datos ni en el frontend.

El servidor debe definir:

```bash
export APP_SECURITY_SECRET_ENCRYPTION_KEY='una-clave-maestra-larga-y-aleatoria'
```

No añadas esta variable a Git.

## Prueba

Después de guardar la Secret API Key:

1. Haz clic en **Probar conexión con Stripe**.
2. El backend llama a `GET /v1/account` usando la clave de esa empresa.
3. El ID de la cuenta de Stripe se guarda en `bc_core_empresa.stripe_account_id`.
4. La pantalla muestra únicamente el ID y la clave enmascarada.

La clave secreta nunca se devuelve a React.

## Webhook

Cada empresa tiene su propio endpoint:

```
POST /api/financeiro/stripe/webhook/{empresaId}
```

El signing secret se resuelve mediante el `empresaId` de la URL. El evento también incluye `empresaId` en los metadatos y el backend lo rechaza si ambos valores no coinciden.

Configura en Stripe estos eventos utilizados por el ERP:

- `checkout.session.completed`
- `checkout.session.async_payment_succeeded`
- `checkout.session.async_payment_failed`
- `invoice.paid`
- `invoice.payment_failed`
- `payment_intent.payment_failed`

## Flujo

```
Empresa
  |
  +-- Secret API Key (cifrada)
  +-- Webhook Secret (cifrado)
  +-- Stripe Account ID
  +-- ¿Stripe habilitada?
          |
          v
El backend resuelve empresa_id
          |
          v
StripeClient (clave de esa empresa)
          |
          +--> Checkout / Invoice
          |
          +--> Webhook /webhook/{empresaId}
                    |
                    +--> valida la firma
                    +--> valida metadata.empresaId
                    +--> liquida el título del mismo tenant
```

Esto impide utilizar las credenciales Stripe de una empresa para cobrar o consultar otra.

## Producción

Antes de activar Stripe para una empresa:

- definir `APP_SECURITY_SECRET_ENCRYPTION_KEY`;
- registrar la Secret API Key de la propia cuenta Stripe de esa empresa;
- registrar el Webhook Signing Secret correspondiente al endpoint;
- guardar la configuración;
- ejecutar la prueba de conexión;
- solo entonces habilitar Stripe para la empresa.

La clave Stripe expuesta anteriormente en el chat no debe reutilizarse. Genera o rota una nueva credencial antes de registrarla en el ERP.

## Métodos de pago

El diseño actual no requiere un terminal físico para tarjetas.

- **Tarjeta:** Checkout alojado por Stripe; el cliente puede pagar desde su móvil mediante un enlace o código QR.
- **PIX:** Checkout lo ofrece cuando el método está habilitado y la cuenta Stripe de la empresa cumple los requisitos.
- **Boleto:** disponible mediante el flujo de cobro/Invoice, según la disponibilidad de la cuenta y la configuración de métodos de pago.
- **Confirmación:** el ERP solo marca el título como pagado después de recibir la confirmación por webhook; la URL de retorno de Checkout no liquida el título por sí sola.
- **Invoice:** el backend finaliza la Invoice cuando es necesario para garantizar que exista la Hosted Invoice Page antes de devolver el enlace al frontend.

El ERP no almacena números de tarjeta, CVV ni datos de tarjeta sin procesar.

## NFC / Tap to Pay — futuro

**NFC/Tap to Pay no forma parte del módulo web actual.** Se reserva para el futuro **módulo App de BrasilCloud ERP**, donde un teléfono compatible podrá actuar como terminal de pago, manteniendo la misma integración financiera y conciliación mediante webhook.

El módulo App deberá reutilizar el motor de liquidación/conciliación financiera; no debe crear una segunda regla de liquidación.
