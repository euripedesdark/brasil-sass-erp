# Contribuer à Brasil SaaS ERP

Merci de votre intérêt pour contribuer à Brasil SaaS ERP. Ce document
fournit des directives pour contribuer au projet.

## Code de Conduite

Ce projet et tous les participants sont régis par le
[Code de Conduite](../../CODE_OF_CONDUCT.md). En participant, vous êtes tenu de
respecter ce code.

## Comment Contribuer ?

### Signalement des Bugs

Avant de créer un rapport de bug, vérifiez les issues existantes pour voir
si le problème a déjà été signalé. Lors de la création d'un rapport de bug,
incluez autant de détails que possible :

* Un titre clair et descriptif
* Les étapes exactes pour reproduire le problème
* Le comportement observé après avoir suivi les étapes
* Le comportement attendu
* Captures d'écran, si applicable
* Votre environnement (OS, version Java, navigateur, etc.)

### Suggestion d'Améliorations

Les suggestions d'améliorations sont suivies comme des issues GitHub. Lors
de la création d'une suggestion, incluez :

* Un titre clair et descriptif
* Une description détaillée de l'amélioration proposée
* Tout exemple ou mockup pertinent
* La motivation pour l'amélioration

### Pull Requests

1. Forkez le dépôt et créez votre branche depuis `main`.
2. Si vous avez ajouté du code à tester, ajoutez des tests.
3. Si vous avez modifié des API, mettez à jour la documentation.
4. Assurez-vous que la suite de tests passe.
5. Assurez-vous que votre code suit le style de code existant.
6. Créez un pull request avec titre et description clairs.

## Configuration de Développement

### Pré-requis

* Java 21 (Oracle JDK ou OpenJDK)
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

### Lancer les Tests

```bash
mvn test
```

### Lancer en Local

```bash
# Démarrez l'infrastructure (PostgreSQL, MongoDB, Redis, RabbitMQ)
docker-compose up -d

# Lancez l'application
mvn spring-boot:run
```

## Structure du Projet

```
src/main/java/br/com/brasil_saas/
├── core/           # Authentification, autorisation, multi-tenant
├── cadastro/       # Données maîtres (clients, fournisseurs, produits, services)
├── financeiro/     # Gestion financière
├── fiscal/         # Documents fiscaux (NFS-e, NF-e, MDF-e, CT-e, SPED)
├── estoque/        # Gestion de stock
├── vendas/         # Ventes
├── compras/        # Achats
├── producao/      # Planification et contrôle de production
├── rh/             # Ressources humaines
├── crm/            # Gestion de la relation client
├── bi/             # Intelligence d'affaires
├── contabilidade/  # Comptabilité
├── ativos/         # Immobilisations
├── dms/            # Gestion de documents
├── qualité/       # Gestion de la qualité
├── projetos/       # Gestion de projets
├── wms/            # Gestion d'entrepôt
├── workflow/       # Moteur de workflow
├── portais/        # Portails client/fournisseur
├── ia/             # Assistant IA
├── servicos/       # Ordres de service
└── relatorios/     # Rapports
```

## Standards de Code

* Suivez le style de code existant (Spring Boot, Lombok, MapStruct).
* Utilisez des noms de variables et de méthodes significatifs.
* Rédigez des messages de commit clairs.
* Ajoutez Javadoc pour les méthodes publiques.
* Gardez les méthodes petites et axées sur une seule responsabilité.

## Licence

En contribuant, vous acceptez que vos contributions soient licenciées sous
la GNU Affero General Public License v3.0 (AGPLv3).
