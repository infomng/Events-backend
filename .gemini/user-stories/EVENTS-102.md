Résumé

L'utilisateur souhaite publier un évènement qui sera visible par d'autres utilisateurs. Un évènement ne peut être annulé que par son créateur.

Contexte

Ce problème concerne la publication et l'annulation d'évènements par les utilisateurs.

Critères d'acceptation

Scénario 1: L'évènement est au statut DRAFT

Quand l’organisateur appuie sur le bouton annuler l'évènement.

Alors la requête est envoyée à events/{id}/publish.

Et le “controller“ appelle l’event service qui :

Récupère l'évènement.

Vérifie si l'évènement est au statut DRAFT.

Le passe au statut PUBLISHED.

Sinon, il throw une exception : “seul les évènements au statut peuvent être annulés”.

Autres informations

N/A