# Checklist : ce qui manque pour une exploitation réelle

État établi à partir du code au 2026-11-26 (branche `develop`, après la PR #41).
Contrairement aux autres fichiers de ce dossier, cette liste part de ce qui existe réellement dans le code.

Priorités :
- **P0** : bloque toute mise en production
- **P1** : attendu par de vrais clients
- **P2** : valeur ajoutée

---

## P0 : parcours d'achat de bout en bout

Le panier et le paiement existent, mais rien ne les relie.

- [ ] Passer commande depuis le panier (`POST /checkout`) : création de la `Booking` à partir du panier
- [ ] Bloquer les places pendant le paiement, avec une durée limite
- [ ] Job d'expiration des réservations non payées, qui libère les places
- [ ] Anti-survente : verrou ou version sur le stock, contrôle du stock aussi pour un nouvel article du panier
- [ ] Génération des tickets après paiement (`TicketService.generateTicketsForBooking` n'est appelé nulle part)
- [ ] Vider le panier après une commande payée
- [ ] Email de confirmation avec les billets (QR code ou PDF)
- [ ] Annulation des tickets quand une commande est remboursée

## P0 : contrôle d'accès le jour J

- [ ] Endpoint de scan : validation du billet, passage en `USED`, refus d'un second passage
- [ ] QR code signé (HMAC ou JWT), infalsifiable
- [ ] Rôle STAFF sur un événement, autorisé à scanner
- [ ] Interface de scan mobile (PWA avec caméra, mode hors ligne si possible)
- [ ] `getTicketById` : vérifier le propriétaire du billet

## P0 : espace organisateur

- [ ] Rôle ORGANIZER réellement appliqué (qui peut créer, publier, annuler)
- [ ] Annulation d'un événement en cascade : remboursements et notification des participants
- [ ] Liste des participants et export CSV
- [ ] Tableau de bord des ventes : billets vendus, chiffre d'affaires, taux de remplissage
- [ ] Versement des recettes aux organisateurs (Stripe Connect) et commission de la plateforme

## P0 : sécurité et exploitation

- [ ] Changer les secrets exposés et nettoyer l'historique git
- [ ] Protéger `POST /data-generation` (ADMIN uniquement, ou désactivé en prod)
- [ ] CORS configurable (aujourd'hui codée en dur sur `localhost:3000`)
- [ ] Migrations de base de données (Flyway ou Liquibase) au lieu de `ddl-auto=update`
- [ ] Dockerfile de l'API et pipeline de déploiement
- [ ] Supervision : Actuator, health checks, logs centralisés, alertes d'erreurs
- [ ] Sauvegardes automatiques de la base

## P0 : frontend

- [ ] Panier et paiement Stripe.js (Payment Element)
- [ ] Page « Mes billets » avec les QR codes
- [ ] Recherche et filtres reliés à l'API
- [ ] Espace organisateur complet : édition, publication, annulation, ventes, participants
- [ ] Une seule méthode d'authentification (aujourd'hui : cookie, en-tête Bearer et NextAuth)

---

## P1 : attendu par de vrais utilisateurs

- [ ] Profil modifiable, changement de mot de passe, suppression de compte (RGPD)
- [ ] Historique des commandes, factures et reçus
- [ ] Billets gratuits et limite de billets par personne
- [ ] Codes promo et tarifs early bird
- [ ] Billets nominatifs et transfert d'un billet
- [ ] Rappels par email (J-1) et notification en cas de modification d'un événement
- [ ] Remboursement demandé par le client, selon une politique par événement
- [ ] Liste d'attente quand un événement est complet
- [ ] Fuseaux horaires et plusieurs devises
- [ ] Paiement mobile money (Orange, MTN, Wave)
- [ ] Back-office admin : modération, utilisateurs, litiges

## P1 : obligations légales

- [ ] CGU et CGV, politique de confidentialité, mentions légales
- [ ] Consentement aux cookies, export et suppression des données
- [ ] Facturation conforme (TVA, numérotation des factures)

---

## P2 : valeur ajoutée

- [ ] Plan de salle interactif (choix du siège)
- [ ] Billets dans Apple Wallet et Google Wallet
- [ ] Avis et notes après l'événement
- [ ] Recommandations, partage sur les réseaux, SEO des pages événement
- [ ] Application mobile ou PWA
- [ ] Authentification à deux facteurs, autres connexions OAuth (Microsoft, Apple)

---

## Ordre recommandé

1. Parcours d'achat (commande → paiement → tickets → email)
2. Scan des billets le jour J
3. Espace organisateur et annulation en cascade
4. Sécurité et exploitation, puis frontend
