**Scenario 1: Récupération de la liste des pays (public)**
**Given** que des pays sont configurés
**When** un utilisateur (authentifié ou non) récupère la liste des pays
**Then** la liste complète des pays est retournée
**And** aucune autorisation spécifique n’est requise

**Scenario 2: Création d’un pays par un administrateur**
**Given** que je suis connecté avec un profil administrateur
**When** je crée un pays à partir d’une valeur valide du PaysEnum
**Then** le pays est enregistré en base
**And** il devient disponible pour les formulaires

**Scenario 3: Création d’un pays avec une valeur invalide**
**Given** que je suis administrateur
**When** je tente de créer un pays avec une valeur absente du PaysEnum
**Then** la création est refusée
**And** un message d’erreur explicite est retourné

**Scenario 4: Modification d’un pays**
**Given** qu’un pays existe
**And** que je suis administrateur
**When** je modifie les informations du pays
**Then** les modifications sont sauvegardées
**And** les entités liées conservent la référence au pays

**Scenario 5: Suppression d’un pays non utilisé**
**Given** qu’un pays existe
**And** qu’il n’est référencé par aucune autre entité
**When** je supprime le pays
**Then** le pays est supprimé définitivement

**Scenario 6: Suppression d’un pays utilisé**
**Given** qu’un pays est référencé par des utilisateurs ou des évènements
**When** je tente de le supprimer
**Then** la suppression est refusée
**And** un message indique que le pays est utilisé

**Scenario 7: Accès refusé aux actions admin**
**Given** que je suis connecté avec un profil non administrateur
**When** je tente de créer, modifier ou supprimer un pays
**Then** l’accès est refusé
**And** une erreur d’autorisation est retournée
