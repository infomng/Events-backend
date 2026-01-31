En tant qu’ organisateur d’événement
Je veux pouvoir gérer des catégories (par défaut et personnalisées) pour mon événement, avec un prix associé à chaque catégorie
Afin de proposer différentes offres aux participants et adapter ma billetterie à mes besoins.

📌 Description fonctionnelle

Lors de la création d’un événement :

Le système propose au minimum 3 catégories par défaut.

Chaque catégorie possède :

un nom

un prix

L’organisateur peut :

utiliser uniquement les catégories par défaut

modifier le nom et le prix des catégories par défaut

créer des catégories personnalisées

définir un prix pour chaque catégorie

Les catégories créées sont propres à l’événement (pas globales).

✅ Critères d’acceptation (Acceptance Criteria)

🟢 Scénario 1 : Catégories par défaut proposées

Given un utilisateur crée un nouvel événement
When il accède à l’étape de configuration des catégories
Then le système affiche au moins 3 catégories par défaut
And chaque catégorie possède un champ nom et prix

🟢 Scénario 2 : Modification des catégories par défaut

Given les catégories par défaut sont affichées
When l’utilisateur modifie le nom ou le prix d’une catégorie
Then les modifications sont enregistrées pour l’événement
And les catégories restent associées uniquement à cet événement

🟢 Scénario 3 : Création de catégories personnalisées

Given l’utilisateur est sur la configuration des catégories
When il ajoute une nouvelle catégorie personnalisée
Then il peut saisir un nom et un prix
And la catégorie est ajoutée à la liste des catégories de l’événement

🟢 Scénario 4 : Utilisation sans création de catégories personnalisées

Given l’utilisateur ne souhaite pas créer de nouvelles catégories
When il valide la création de l’événement
Then l’événement est créé avec uniquement les 3 catégories par défaut

🟢 Scénario 5 : Validation des prix

Given un utilisateur saisit un prix pour une catégorie
When le prix est inférieur à zéro ou vide
Then le système affiche un message d’erreur
And empêche la validation de l’événement

🧩 Règles métier (Business Rules)

Un événement doit avoir au moins une catégorie.

Une catégorie doit toujours avoir :

un nom non vide

un prix ≥ 0

Les catégories sont liées à un événement, pas réutilisables globalement (sauf évolution future).

