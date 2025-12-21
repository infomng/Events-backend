# Documentation du script `create_all_issues.sh`

## Objectif

Ce script a pour but d'initialiser un projet sur GitHub en créant automatiquement un ensemble de labels, de milestones et d'issues prédéfinis. Il est conçu pour être idempotent, ce qui signifie qu'il peut être exécuté plusieurs fois sans créer de doublons : il ne créera que les éléments (labels, milestones, issues) qui n'existent pas déjà.

## Prérequis

Avant d'exécuter ce script, vous devez vous assurer que :

1.  **L'interface de ligne de commande de GitHub (`gh`) est installée** sur votre machine. Si ce n'est pas le cas, suivez les [instructions d'installation officielles](https://github.com/cli/cli#installation).
2.  **Vous êtes authentifié auprès de GitHub** via la CLI. Vous pouvez vous connecter avec la commande `gh auth login`.
3.  **Vous exécutez le script depuis la racine d'un dépôt Git** qui est lié à un dépôt distant sur GitHub. Le script détecte automatiquement le nom du dépôt (`OWNER/REPO`).

## Utilisation

Pour exécuter le script, placez-vous à la racine de votre projet et lancez la commande suivante dans votre terminal :

```bash
./scripts/create_all_issues.sh
```

## Fonctionnement détaillé

Le script exécute les actions suivantes dans l'ordre :

### 1. Configuration initiale

-   **Détection du dépôt** : Le script utilise `gh repo view` pour récupérer automatiquement le nom du dépôt distant (ex: `Gedeon31/Events-backend`).
-   **Chargement de la "Definition of Done"** : Le contenu du fichier `docs/definition-of-done.md` est chargé en mémoire pour être ajouté à la description de chaque issue créée.

### 2. Création des Labels

Le script s'assure que les labels suivants existent dans le dépôt. S'ils n'existent pas, ils sont créés avec une couleur spécifique.

-   `mvp`
-   `security`
-   `payments`
-   `backoffice`
-   `innovation`
-   `architecture`
-   `devops`
-   `legal`

### 3. Création des Milestones

De la même manière, le script vérifie et crée les milestones (jalons) suivants s'ils sont absents :

-   **MVP**: Fonctionnalités cœur
-   **SECURITY**: Sécurité et accès
-   **PAYMENTS**: Paiements et monétisation
-   **BACKOFFICE**: Back-office et administration
-   **INNOVATION**: Fonctionnalités différenciantes
-   **ARCHITECTURE**: Architecture technique
-   **DEVOPS**: CI/CD et exploitation
-   **LEGAL**: Conformité légale

### 4. Création des Issues

Enfin, le script crée une série d'issues prédéfinies. Pour chaque issue, il vérifie d'abord si une issue avec le même titre existe déjà. Si ce n'est pas le cas, il la crée en lui assignant :

-   Un titre
-   Un corps (une brève description suivie du contenu de la "Definition of Done")
-   Le label correspondant
-   Le milestone correspondant

Les issues sont créées pour chaque catégorie définie dans les milestones (MVP, Sécurité, Paiements, etc.).

---

Ce script est un excellent outil pour standardiser le démarrage d'un projet et s'assurer que toutes les tâches initiales importantes sont bien présentes dans le tracker d'issues de GitHub.
