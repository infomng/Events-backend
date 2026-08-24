En tant qu’organisateur, je veux annuler un évènement afin d’informer les participants.



Scenario 1: Annulation réussie
Given que je suis l’organisateur
And que l’évènement est publié
When j’annule l’évènement
Then le statut passe à "Annulé"
And tous les participants sont notifiés



Scenario 2: Annulation avec billets vendus
Given que des billets ont été vendus
When j’annule l’évènement
Then les remboursements sont déclenchés automatiquement

