#!/usr/bin/env bash

# Pour que le script s'arrête en cas d'erreur
set -e
# Désactive le pager pour gh
export GH_PAGER=""

# ====================================
# CONFIGURATION
# ====================================
OWNER_REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
if [ -z "$OWNER_REPO" ]; then
  echo " ❌ Impossible de détecter OWNER/REPO. Sois dans le repo GitHub ou définis OWNER/REPO manuellement."
  exit 1
fi

PROJECT_NAME="Event App Roadmap"
PROJECT_BODY="Kanban complet des fonctionnalités de l'application Event App"

# ====================================
# Définition of Done (DoD)
# ====================================
DOD=$(cat docs/definition-of-done.md)

# ====================================
# 1. Fonction utilitaire
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
  if ! gh api repos/$OWNER_REPO/milestones --jq '.[].name' | grep -q "^$TITLE$"; then
    gh api --method POST repos/$OWNER_REPO/milestones -f name="$TITLE" -f description="$DESC" > /dev/null
    echo " ✅ Milestone créé: $TITLE"
  else
    echo " ⏩ Milestone existant: $TITLE"
  fi
}

# ====================================
# 2. Labels
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
# 3. Milestones
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
# 4. Création du Project V2
# ====================================
PROJECT_ID=$(gh api graphql -f query='
query($owner: String!, $name: String!) {
  repository(owner:$owner, name:$name) {
    projectsV2(first: 100) { nodes { id name } }
  }
}' -F owner="${OWNER_REPO%%/*}" -F name="${OWNER_REPO##*/}" | jq -r --arg PN "$PROJECT_NAME" '.data.repository.projectsV2.nodes[] | select(.name==$PN) | .id')

if [ -z "$PROJECT_ID" ]; then
  REPO_ID=$(gh api graphql -f query='query($owner:String!,$name:String!){repository(owner:$owner,name:$name){id}}' \
            -F owner="${OWNER_REPO%%/*}" -F name="${OWNER_REPO##*/}" | jq -r '.data.repository.id')
  PROJECT_ID=$(gh api graphql -f query='mutation($repoId:ID!,$name:String!){createProjectV2(input:{repositoryId:$repoId,name:$name}){projectV2{id}}}' \
            -F repoId="$REPO_ID" -F name="$PROJECT_NAME" | jq -r '.data.createProjectV2.projectV2.id')
  echo " ✅ Project V2 créé: $PROJECT_NAME"
else
  echo " ⏩ Project V2 existant: $PROJECT_NAME"
fi

# ====================================
# 5. Créer colonnes pour chaque milestone (single select fields)
# ====================================
declare -A COLUMN_IDS
for m in "${!milestones[@]}"; do
  FIELD_ID=$(gh api graphql -f query='
query($projectId:ID!){ node(id:$projectId){... on ProjectV2{fields(first:100){nodes{id name}}}}}' -F projectId="$PROJECT_ID" | jq -r '.data.node.fields.nodes[] | select(.name=="'"$m"'") | .id')
  if [ -z "$FIELD_ID" ]; then
    FIELD_ID=$(gh api graphql -f query='mutation($projectId:ID!,$name:String!){addProjectV2Field(input:{projectId:$projectId,name:$name,type:SINGLE_SELECT}){field{id}}}' \
              -F projectId="$PROJECT_ID" -F name="$m" | jq -r '.data.addProjectV2Field.field.id')
    echo " ✅ Colonne créée pour milestone: $m"
  else
    echo " ⏩ Colonne existante pour milestone: $m"
  fi
  COLUMN_IDS[$m]=$FIELD_ID
done

# ====================================
# 6. Créer les issues + ajout au Project
# ====================================
create_issue () {
  TITLE="$1"
  LABEL="$2"
  MILESTONE="$3"
  BODY="$4"

  ISSUE_NUMBER=$(gh issue list --search "$TITLE" --json number,name --jq '.[] | select(.name=="'"$TITLE"'") | .number')
  if [ -z "$ISSUE_NUMBER" ]; then
    ISSUE_NUMBER=$(gh issue create --name "$TITLE" --body "$BODY

---

$DOD" --label "$LABEL" --milestone "$MILESTONE" --json number | jq -r '.number')
    echo " ✅ Issue créée: $TITLE (#$ISSUE_NUMBER)"
  else
    echo " ⏩ Issue existante: $TITLE (#$ISSUE_NUMBER)"
  fi

  # Récupérer l'ID de l'issue pour ProjectV2
  ISSUE_NODE_ID=$(gh api graphql -f query='
query($owner:String!,$repo:String!,$issueNumber:Int!){
  repository(owner:$owner,name:$repo){issue(number:$issueNumber){id}}
}' -F owner="${OWNER_REPO%%/*}" -F repo="${OWNER_REPO##*/}" -F issueNumber="$ISSUE_NUMBER" | jq -r '.data.repository.issue.id')

  # Ajouter à Project V2 si pas déjà présent
  EXISTS=$(gh api graphql -f query='
query($projectId:ID!){
  node(id:$projectId){... on ProjectV2{items(first:100){nodes{content{id}}}}}
}' -F projectId="$PROJECT_ID" | jq -r '.data.node.items.nodes[]?.content.id' | grep -w "$ISSUE_NODE_ID" || true)

  if [ -z "$EXISTS" ]; then
    gh api graphql -f query='mutation($projectId:ID!,$contentId:ID!){addProjectV2ItemById(input:{projectId:$projectId,contentId:$contentId}){item{id}}}' \
      -F projectId="$PROJECT_ID" -F contentId="$ISSUE_NODE_ID" >/dev/null
    echo " ➡️ Issue ajoutée au Project V2: $TITLE"
  else
    echo " ⏩ Issue déjà dans le Project: $TITLE"
  fi
}

# ====================================
# 7. Liste complète des issues
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

echo " 🎉 Script terminé. Toutes les labels, milestones, issues et Project V2 Kanban sont en place."
