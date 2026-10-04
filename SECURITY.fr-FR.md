# Politique de Sécurité

Nous prenons la sécurité de Brasil SaaS ERP au sérieux. Si vous pensez avoir
trouvé une vulnérabilité de sécurité, veuillez la signaler de manière responsable.

## Comment Signaler

**N'ouvrez pas d'issues publiques pour les vulnérabilités de sécurité.**
Envoyez plutôt un e-mail à :

**euripedesdark@gmail.com**

Incluez les informations suivantes :

* Une description de la vulnérabilité
* Les étapes pour reproduire le problème
* L'impact potentiel
* Toute suggestion de correction (si disponible)

## À Quoi S'Attendre

* Nous accuserons réception de votre signalement sous 48 heures.
* Nous enquêterons sur le problème et fournirons un calendrier pour la correction.
* Nous vous créditerons dans les notes de version (sauf si vous préférez rester anonyme).
* Nous publierons une correction dès que possible et vous informerons lorsqu'elle est disponible.

## Portée

Cette politique de sécurité s'applique à :

* Le code de l'application Brasil SaaS ERP
* Les endpoints de l'API
* Le schéma de la base de données et les migrations
* Les scripts de déploiement et les configurations

## Hors Portée

* Les bibliothèques tierces (signalez les vulnérabilités aux projets respectifs)
* Les problèmes dans la documentation
* Les questions générales sur les meilleures pratiques de sécurité

## Mesures de Sécurité

Le projet met en œuvre les mesures de sécurité suivantes :

* **Authentification :** Authentification basée sur JWT avec refresh tokens
* **Autorisation :** Contrôle d'accès basé sur les rôles (RBAC) avec permissions
* **Multi-tenant :** Isolation des données entre entreprises
* **Chiffrement :** TLS pour toutes les communications, mTLS pour les connexions à la base de données
* **Validation des entrées :** Bean Validation sur tous les endpoints de l'API
* **Prévention des injections SQL :** Requêtes paramétrées via JPA
* **Prévention XSS :** Échappement intégré de React
* **Protection CSRF :** Conception d'API stateless
* **Audit :** Journaux d'accès et pistes d'audit

## Politique de Divulgation

Nous suivons une politique de divulgation coordonnée. Nous vous demandons de :

* Nous laisser un délai raisonnable pour corriger le problème avant de le divulguer publiquement.
* Ne pas exploiter la vulnérabilité au-delà de ce nécessaire pour la démontrer.
* Ne pas accéder ni modifier les données appartenant à d'autres utilisateurs.
