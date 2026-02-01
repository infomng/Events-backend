Scenario 1: Création d’une catégorie
Given que je suis connecté avec un profil administrateur
When je crée une nouvelle catégorie avec un nom valide
Then la catégorie est enregistrée
And elle devient disponible lors de la création d’un évènement

Scenario 2: Modification d’une catégorie
Given qu’une catégorie existe
And que je suis administrateur
When je modifie le nom ou la description de la catégorie
Then les modifications sont sauvegardées
And les évènements existants conservent leur association à la catégorie

Scenario 3: Suppression d’une catégorie non utilisée
Given qu’une catégorie existe
And qu’elle n’est associée à aucun évènement
When je supprime la catégorie
Then la catégorie est supprimée définitivement

Scenario 4: Suppression d’une catégorie utilisée
Given qu’une catégorie est associée à des évènements
When je tente de la supprimer
Then la suppression est refusée
And un message indique que la catégorie est utilisée

Scenario 5: Accès refusé à un utilisateur non admin
Given que je suis connecté avec un profil non administrateur
When je tente de créer, modifier ou supprimer une catégorie
Then l’accès est refusé
And une erreur d’autorisation est retournée