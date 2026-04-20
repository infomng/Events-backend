# 🚨 SECURITY INCIDENT - IMMEDIATE ACTION REQUIRED

**Date**: 2026-04-15
**Severity**: CRITICAL
**Repository**: https://github.com/infomng/Events-backend.git

## Situation

Your `application.properties` file containing ALL secrets has been committed to git history and pushed to GitHub.

### Exposed Secrets
1. **Supabase Service Role Key** - Full admin access to your Supabase database!
2. **JWT Secrets** - Can forge authentication tokens
3. **Google OAuth2 Client Secret** - Can impersonate your OAuth app
4. **Gmail App Password** - Can send emails from your account
5. **Database credentials**

## IMMEDIATE ACTIONS (Do Now!)

### 1. Rotate Supabase Keys (URGENT - 15 minutes)
```
1. Login to https://supabase.com
2. Go to Project Settings > API
3. Click "Reveal" on Service Role Key
4. Click "Generate New Key" (if available) OR
5. Create a new project and migrate data
6. Update your local application.properties
```

### 2. Regenerate Google OAuth2 Credentials (10 minutes)
```
1. Go to https://console.cloud.google.com/
2. APIs & Services > Credentials
3. Delete the compromised OAuth 2.0 Client
4. Create New OAuth 2.0 Client ID
5. Update application.properties with new credentials
```

### 3. Change Gmail App Password (5 minutes)
```
1. Go to https://myaccount.google.com/apppasswords
2. Revoke the current app password
3. Generate a new one
4. Update application.properties
```

### 4. Generate New JWT Secrets (2 minutes)
```bash
# Generate new secrets (run twice for access + refresh)
openssl rand -base64 64

# Update in application.properties:
# jwt.access-token.secret=NEW_SECRET_1
# jwt.refresh-token.secret=NEW_SECRET_2
```

### 5. Check Database Access Logs
```sql
-- Check for unauthorized access (PostgreSQL)
SELECT * FROM pg_stat_activity;
SELECT * FROM pg_stat_statements ORDER BY calls DESC LIMIT 20;
```

## MEDIUM PRIORITY (Do Today)

### 6. Remove Secrets from Git History

**Option A: BFG Repo-Cleaner (Recommended)**
```bash
# Download BFG
wget https://repo1.maven.org/maven2/com/madgag/bfg/1.14.0/bfg-1.14.0.jar

# Backup your repo first
cp -r Events-backend Events-backend-backup

# Remove the file from history
java -jar bfg-1.14.0.jar --delete-files application.properties Events-backend

# Clean up
cd Events-backend
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push (WARNING: coordinate with team)
git push origin --force --all
git push origin --force --tags
```

**Option B: Git Filter-Repo (Alternative)**
```bash
# Install git-filter-repo
pip install git-filter-repo

# Remove file from history
git filter-repo --path src/main/resources/application.properties --invert-paths

# Force push
git push origin --force --all
```

### 7. Notify Your Team
- Inform all developers about the incident
- Share new credentials securely (NOT via git/email)
- Update CI/CD pipelines with new secrets

### 8. Update GitHub Repository Settings
```
1. Go to repository Settings > Security
2. Enable "Secret scanning"
3. Enable "Dependabot alerts"
4. Consider making repo private if currently public
```

## VERIFICATION CHECKLIST

After rotating all secrets:

- [ ] Supabase service-role-key regenerated
- [ ] Google OAuth2 credentials regenerated
- [ ] Gmail app password changed
- [ ] JWT secrets changed
- [ ] Database credentials changed (if exposed)
- [ ] Local application.properties updated
- [ ] Application tested with new credentials
- [ ] Secrets removed from git history
- [ ] Force pushed to remote
- [ ] Team notified
- [ ] CI/CD updated
- [ ] GitHub secret scanning enabled

## PREVENTION (Future)

### Install pre-commit hook
```bash
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
if git diff --cached --name-only | grep -E "application.*\.properties$" | grep -v "\.example$"; then
  echo "❌ ERROR: Attempting to commit application.properties!"
  echo "This file contains secrets and should NEVER be committed."
  echo "Use application.properties.example instead."
  exit 1
fi
EOF

chmod +x .git/hooks/pre-commit
```

### Use git-secrets
```bash
# Install
brew install git-secrets  # macOS
# OR
sudo apt install git-secrets  # Linux

# Configure
git secrets --install
git secrets --register-aws
git secrets --add 'supabase.*key'
git secrets --add 'jwt.*secret'
```

### Use Environment Variables in Production
```bash
# Instead of application.properties
export SUPABASE_SERVICE_ROLE_KEY="..."
export JWT_ACCESS_TOKEN_SECRET="..."
export SPRING_MAIL_PASSWORD="..."
```

## Resources

- [GitHub Secret Scanning](https://docs.github.com/en/code-security/secret-scanning)
- [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/)
- [Git Secrets](https://github.com/awslabs/git-secrets)
- [Removing Sensitive Data from Git](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)

## Timeline

| Time | Action | Status |
|------|--------|--------|
| T+0 (Now) | Rotate Supabase keys | ⏳ Pending |
| T+15min | Rotate OAuth2 credentials | ⏳ Pending |
| T+30min | Change Gmail password | ⏳ Pending |
| T+1hr | Remove from git history | ⏳ Pending |
| T+2hr | Force push to remote | ⏳ Pending |
| T+4hr | Verify no unauthorized access | ⏳ Pending |

## Contact

If you need help:
1. GitHub Security: https://github.com/security
2. Supabase Support: https://supabase.com/support
3. Google Cloud Support: https://cloud.google.com/support

---

**REMEMBER**: Even after removing from git, secrets may be cached by GitHub or other services.
**The ONLY safe action is to ROTATE all secrets immediately.**
