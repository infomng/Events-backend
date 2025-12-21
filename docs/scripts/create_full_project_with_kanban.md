# Documentation du script `create_full_project_with_kanban.sh`

## Objectif

Ce script a pour but d'initialiser un projet sur GitHub en créant automatiquement un ensemble complet d'éléments pour la gestion de projet. Il configure :
-   Des **labels** pour catégoriser les issues.
-   Des **milestones** (jalons) pour regrouper les issues par grandes étapes.
-   Un **projet GitHub (Project V2)** de type Kanban.
-   Des **issues** prédéfinies pour chaque catégorie, directement liées aux labels et milestones correspondants.
-   L'ajout de toutes les issues créées au projet Kanban.

Comme le script `create_all_issues.sh`, il est conçu pour être **idempotent** : il peut être exécuté plusieurs fois sans créer de doublons. Il ne créera que les éléments qui n'existent pas déjà.

## Prérequis

Avant d'exécuter ce script, vous devez vous assurer que :

1.  **L'interface de ligne de commande de GitHub (`gh`) est installée** sur votre machine. Si ce n'est pas le cas, suivez les [instructions d'installation officielles](https://github.com/cli/cli#installation).
2.  **Vous êtes authentifié auprès de GitHub** via la CLI avec des droits suffisants pour créer des projets, des labels, des milestones et des issues. Vous pouvez vous connecter avec la commande `gh auth login`.
3.  **Vous exécutez le script depuis la racine d'un dépôt Git** qui est lié à un dépôt distant sur GitHub. Le script détecte automatiquement le nom du dépôt (`OWNER/REPO`).

## Utilisation

Pour exécuter le script, placez-vous à la racine de votre projet et lancez la commande suivante dans votre terminal :

```bash
./scripts/create_full_project_with_kanban.sh
```

## Fonctionnement détaillé

Le script exécute les actions suivantes dans l'ordre :

### 1. Configuration initiale

-   **Détection du dépôt** : Le script utilise `gh repo view` pour récupérer automatiquement le nom du dépôt distant (ex: `Gedeon31/Events-backend`).
-   **Chargement de la "Definition of Done"** : Le contenu du fichier `docs/definition-of-done.md` est chargé pour être ajouté au corps de chaque issue.

### 2. Création des Labels

Le script s'assure que les labels suivants existent. S'ils sont absents, ils sont créés :
`mvp`, `security`, `payments`, `backoffice`, `innovation`, `architecture`, `devops`, `legal`.

### 3. Création des Milestones

Le script vérifie et crée les milestones (jalons) suivants s'ils n'existent pas :
**MVP**, **SECURITY**, **PAYMENTS**, **BACKOFFICE**, **INNOVATION**, **ARCHITECTURE**, **DEVOPS**, **LEGAL**.

### 4. Création du Projet (Project V2)

-   Il vérifie si un projet nommé **"Event App Roadmap"** existe déjà pour ce dépôt.
-   Si ce n'est pas le cas, il crée un nouveau projet (Project V2) lié au dépôt.

### 5. Création des Colonnes du Projet

Le script s'assure que le projet Kanban possède des champs (qui peuvent être vus comme des colonnes ou des catégories) correspondant à chaque milestone.

### 6. Création et Ajout des Issues au Projet

-   Pour chaque tâche prédéfinie (ex: "Créer un évènement"), le script :
    1.  Vérifie si une issue avec le même titre existe déjà.
    2.  Si non, il **crée l'issue** en lui assignant le titre, un corps (incluant la "Definition of Done"), le label et le milestone appropriés.
    3.  Il récupère l'identifiant de l'issue (nouvellement créée ou existante).
    4.  Il vérifie si cette issue est déjà dans le projet "Event App Roadmap".
    5.  Si non, il **ajoute l'issue au projet**.

Ce processus est répété pour toutes les tâches listées dans le script, couvrant l'ensemble des catégories (MVP, Sécurité, Paiements, etc.).

---

Ce script est un outil puissant pour initialiser de manière complète et standardisée un environnement de gestion de projet sur GitHub, en s'assurant que toutes les tâches initiales sont non seulement créées mais aussi organisées dans un tableau Kanban.
