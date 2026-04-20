# Configuration Guide

## Initial Setup

### 1. Database Configuration

Create a PostgreSQL database:
```bash
createdb events
```

Default credentials (change in production):
- Username: `postgres`
- Password: `postgres`
- Database: `events`
- Port: `5432`

### 2. Application Properties

Copy the example configuration file:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then fill in the following secrets:

#### JWT Secrets
Generate strong secrets (at least 64 characters):
```bash
# Linux/Mac
openssl rand -base64 64

# Or use a password generator
```

Replace in `application.properties`:
- `jwt.access-token.secret`
- `jwt.refresh-token.secret`

#### Google OAuth2
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google`
   - Add your production URL when deploying

Replace in `application.properties`:
- `spring.security.oauth2.client.registration.google.client-id`
- `spring.security.oauth2.client.registration.google.client-secret`

#### Gmail SMTP
1. Enable 2-factor authentication on your Gmail account
2. Generate an App Password: [Google App Passwords](https://myaccount.google.com/apppasswords)
3. Use the generated 16-character password

Replace in `application.properties`:
- `spring.mail.username` (your Gmail address)
- `spring.mail.password` (the 16-character app password)

#### Supabase Storage
1. Create a Supabase project at [supabase.com](https://supabase.com)
2. Create a storage bucket named `eventsimagebucket`
3. Set bucket to public if you want public image access
4. Get your credentials from Project Settings > API

Replace in `application.properties`:
- `supabase.project-url` (format: https://xxxxx.supabase.co)
- `supabase.api-key` (anon/public key)
- `supabase.service-role-key` (service role key - keep secret!)

### 3. Environment-Specific Configuration

For different environments (dev, staging, prod), you can use Spring profiles:

**application-dev.properties** (already ignored by git):
```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

**application-prod.properties**:
```properties
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
logging.level.org.hibernate.SQL=WARN
```

Run with profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Security Best Practices

### ⚠️ NEVER commit these files to git:
- `application.properties` (already in .gitignore)
- `application-*.properties` with real secrets
- Any file containing API keys, passwords, or tokens

### ✅ DO commit:
- `application.properties.example` (template without secrets)
- `application-*.properties.example` (templates for different environments)

### Production Deployment

For production, use environment variables instead of application.properties:

```bash
# Example using environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-host:5432/events
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
export JWT_ACCESS_TOKEN_SECRET=${JWT_SECRET}
# ... etc
```

Or use a secrets management service:
- AWS Secrets Manager
- Azure Key Vault
- HashiCorp Vault
- Google Secret Manager

## Verifying Configuration

Run the application:
```bash
./mvnw spring-boot:run
```

Check that:
- Application starts without errors
- Database connection is successful
- OAuth2 login works at `http://localhost:8080/oauth2/authorization/google`
- Email sending works (check logs)

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running: `pg_isready`
- Check credentials in application.properties
- Ensure database exists: `psql -l`

### OAuth2 Issues
- Verify redirect URIs match exactly
- Check client ID and secret are correct
- Ensure Google+ API is enabled

### Email Issues
- Verify 2FA is enabled on Gmail
- Use App Password, not regular password
- Check firewall allows port 587

### Supabase Issues
- Verify project URL format
- Check bucket name matches exactly
- Ensure bucket has correct permissions (public/private)
