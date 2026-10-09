# Stripe par entreprise

L’intégration Stripe est **multi-entreprise**. Les identifiants Stripe appartiennent au tenant de `brasil_saas.bc_core_empresa` ; il n’existe plus de `STRIPE_SECRET_KEY` globale partagée entre toutes les entreprises.

## Qui peut configurer Stripe ?

Seuls les rôles suivants peuvent configurer Stripe :

- `ROLE_DIRETORIA`
- `ROLE_GESTOR`
- `ROLE_SUPERUSER`

Le backend fait autorité. Masquer la section dans React ne remplace pas `@PreAuthorize`.

## Paramètres de l’entreprise

L’écran **Configurer l’entreprise** permet de renseigner :

- la Secret API Key Stripe ;
- le Webhook Signing Secret ;
- l’activation ou la désactivation de Stripe ;
- le test de connexion.

Les deux identifiants sont chiffrés avec AES-GCM. La clé maîtresse de chiffrement n’est jamais stockée dans la base de données ni dans le frontend.

Le serveur doit définir :

```bash
export APP_SECURITY_SECRET_ENCRYPTION_KEY='une-cle-maitresse-longue-et-aleatoire'
```

N’ajoutez pas cette variable à Git.

## Test

Après l’enregistrement de la Secret API Key :

1. Cliquez sur **Tester la connexion Stripe**.
2. Le backend appelle `GET /v1/account` avec la clé de cette entreprise.
3. L’identifiant du compte Stripe est enregistré dans `bc_core_empresa.stripe_account_id`.
4. L’écran n’affiche que l’identifiant du compte et une clé masquée.

La clé secrète n’est jamais renvoyée à React.

## Webhook

Chaque entreprise dispose de son propre endpoint :

```
POST /api/financeiro/stripe/webhook/{empresaId}
```

Le signing secret est résolu à partir du `empresaId` de l’URL. L’événement contient également `empresaId` dans ses métadonnées ; le backend rejette l’événement si les deux valeurs ne correspondent pas.

Configurez dans Stripe les événements utilisés par l’ERP :

- `checkout.session.completed`
- `checkout.session.async_payment_succeeded`
- `checkout.session.async_payment_failed`
- `invoice.paid`
- `invoice.payment_failed`
- `payment_intent.payment_failed`

## Flux

```
Entreprise
  |
  +-- Secret API Key (chiffrée)
  +-- Webhook Secret (chiffré)
  +-- Stripe Account ID
  +-- Stripe activé ?
          |
          v
Le backend résout empresa_id
          |
          v
StripeClient (clé de cette entreprise)
          |
          +--> Checkout / Invoice
          |
          +--> Webhook /webhook/{empresaId}
                    |
                    +--> vérifie la signature
                    +--> vérifie metadata.empresaId
                    +--> règle le titre du même tenant
```

Cela empêche l’utilisation des identifiants Stripe d’une entreprise pour facturer ou interroger une autre entreprise.

## Production

Avant d’activer Stripe pour une entreprise :

- définir `APP_SECURITY_SECRET_ENCRYPTION_KEY` ;
- enregistrer la Secret API Key du propre compte Stripe de l’entreprise ;
- enregistrer le Webhook Signing Secret correspondant à cet endpoint ;
- enregistrer les paramètres ;
- exécuter le test de connexion ;
- activer Stripe pour l’entreprise seulement après ces vérifications.

La clé Stripe précédemment exposée dans la conversation ne doit pas être réutilisée. Générez ou faites tourner un nouvel identifiant avant de l’enregistrer dans l’ERP.

## Moyens de paiement

La conception actuelle ne nécessite pas de terminal physique de paiement par carte.

- **Carte :** Checkout hébergé par Stripe ; le client peut payer depuis son téléphone via un lien ou un QR code.
- **PIX :** Checkout le propose si le moyen de paiement est activé et admissible pour le compte Stripe de l’entreprise.
- **Boleto :** disponible via le flux de facturation/Invoice selon les possibilités du compte et la configuration des moyens de paiement.
- **Confirmation :** l’ERP ne marque le titre comme payé qu’après confirmation reçue par webhook ; l’URL de retour de Checkout ne règle pas le titre à elle seule.
- **Invoice :** le backend finalise l’Invoice si nécessaire afin de garantir la disponibilité de la Hosted Invoice Page avant de renvoyer son lien au frontend.

L’ERP ne stocke ni numéros de carte, ni CVV, ni données brutes de carte.

## NFC / Tap to Pay — futur

**NFC/Tap to Pay ne fait pas partie du module web actuel.** Cette fonctionnalité est réservée au futur **module App de BrasilCloud ERP**, dans lequel un téléphone compatible pourra servir de terminal de paiement tout en réutilisant la même intégration financière et le rapprochement par webhook.

Le module App devra réutiliser le moteur de règlement/rapprochement du module financier et ne devra pas créer une seconde règle de règlement.
