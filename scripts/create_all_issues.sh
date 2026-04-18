#!/usr/bin/env bash

# Pour que le script s'arrête en cas d'erreur
set -e
# Désactive le pager pour gh
export GH_PAGER=""

# ====================================
# CONFIGURATION
# ====================================
# Si tu es déjà dans le repo Git, ça récupère automatiquement OWNER/REPO
OWNER_REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
if [ -z "$OWNER_REPO" ]; then
  echo " ❌ Impossible de détecter OWNER/REPO. Sois dans le repo GitHub ou définis OWNER/REPO manuellement."
  exit 1
fi

# ====================================
# Définition of Done (DoD)
# ====================================
DOD=$(cat docs/definition-of-done.md)

# ====================================
# Fonction utilitaire
# ====================================
create_label () {
  LABEL="$1"
  COLOR="$2"
  if ! gh label list --json name --jq '.[].name' | grep -q "^$LABEL$"; then
    gh label create "$LABEL" --color "$COLOR"
    echo " ✅ Label créé: $LABEL"
  else
    echo " ⏩ Label existant: $LABEL"
  fi
}

create_milestone () {
  TITLE="$1"
  DESC="$2"
  # Vérifie si le milestone existe déjà
  if ! gh api repos/$OWNER_REPO/milestones --jq '.[].name' | grep -q "^$TITLE$"; then
    gh api --method POST repos/$OWNER_REPO/milestones -f name="$TITLE" -f description="$DESC" > /dev/null
    echo " ✅ Milestone créé: $TITLE"
  else
    echo " ⏩ Milestone existant: $TITLE"
  fi
}

create_issue () {
  TITLE="$1"
  LABEL="$2"
  MILESTONE="$3"
  BODY="$4"

  # Idempotence : ne recrée pas si issue existe déjà
  if gh issue list --search "$TITLE" --json name --jq '.[] | .name' | grep -q "^$TITLE$"; then
    echo " ⏩ Issue déjà existante: $TITLE"
  else
    gh issue create \
      --name "$TITLE" \
      --body "$BODY

---

$DOD" \
      --label "$LABEL" \
      --milestone "$MILESTONE"
    echo " ✅ Issue créée: $TITLE"
  fi
}

# ====================================
# 1. Création des labels
# ====================================
declare -A labels
labels=(
  [mvp]=0052cc
  [security]=d73a4a
  [payments]=0e8a16
  [backoffice]=fbca04
  [innovation]=c5def5
  [architecture]=5319e7
  [devops]=24292e
  [legal]=bfdadc
)

for l in "${!labels[@]}"; do
  create_label "$l" "${labels[$l]}"
done

# ====================================
# 2. Création des milestones
# ====================================
declare -A milestones
milestones=(
  [MVP]="Fonctionnalités cœur"
  [SECURITY]="Sécurité et accès"
  [PAYMENTS]="Paiements et monétisation"
  [BACKOFFICE]="Back-office et administration"
  [INNOVATION]="Fonctionnalités différenciantes"
  [ARCHITECTURE]="Architecture technique"
  [DEVOPS]="CI/CD et exploitation"
  [LEGAL]="Conformité légale"
)

for m in "${!milestones[@]}"; do
  create_milestone "$m" "${milestones[$m]}"
done

# ====================================
# 3. Création des issues
# ====================================

# ======= MVP =======
create_issue "Créer un évènement" mvp MVP "Création d’un évènement."
create_issue "Modifier un évènement" mvp MVP "Modification d’un évènement."
create_issue "Supprimer un évènement" mvp MVP "Suppression d’un évènement."
create_issue "Gestion des statuts d’évènement" mvp MVP "DRAFT / PUBLISHED / SOLD_OUT / CANCELLED."
create_issue "Gestion des billets" mvp MVP "Création et gestion des types de billets."
create_issue "QR Code billet" mvp MVP "QR Code unique par billet."
create_issue "Check-in par QR Code" mvp MVP "Validation des entrées."

# ======= SECURITY =======
create_issue "Authentification email/mot de passe" security SECURITY "Login sécurisé."
create_issue "JWT + Refresh Token" security SECURITY "Gestion des tokens."
create_issue "RBAC utilisateurs" security SECURITY "Rôles et permissions."
create_issue "Rate limiting API" security SECURITY "Limiter les abus."
create_issue "Audit log sécurité" security SECURITY "Traçabilité des actions."

# ======= PAYMENTS =======
create_issue "Intégration Stripe" payments PAYMENTS "Paiement via Stripe."
create_issue "Webhooks Stripe sécurisés" payments PAYMENTS "Validation signature."
create_issue "Facturation automatique" payments PAYMENTS "Génération de factures."

# ======= BACKOFFICE =======
create_issue "Dashboard administrateur" backoffice BACKOFFICE "Vue globale."
create_issue "Gestion utilisateurs" backoffice BACKOFFICE "Blocage, suspension."

# ======= INNOVATION =======
create_issue "Recommandation d’évènements" innovation INNOVATION "Moteur de reco."
create_issue "Networking participants" innovation INNOVATION "Mise en relation."

# ======= ARCHITECTURE =======
create_issue "Architecture clean / hexagonale" architecture ARCHITECTURE "Structuration du backend."
create_issue "Découpage par modules métiers" architecture ARCHITECTURE "Modularisation."

# ======= DEVOPS =======
create_issue "Pipeline CI PR" devops DEVOPS "Validation automatique."
create_issue "Pipeline de release" devops DEVOPS "Release versionnée."
create_issue "Dockerisation" devops DEVOPS "Dockerfile + compose."

# ======= LEGAL =======
create_issue "Conformité RGPD" legal LEGAL "Données personnelles."
create_issue "Mentions légales et CGU" legal LEGAL "Pages légales."

echo "🎉 Script terminé. Toutes les labels, milestones et issues sont en place."
