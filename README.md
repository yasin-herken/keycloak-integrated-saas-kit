# Keycloak Integrated SaaS Kit

Ready-to-use development environment that boots Keycloak and PostgreSQL infrastructure with a single command.

## Quick Start

### 1. Create the configuration file

```bash
cp saas-init.yml.example saas-init.yml
```

### 2. Edit `saas-init.yml`

```yaml
project:
  name: archcore  # or your project name (e.g., myproject)

features:
  forgotPassword:
    enabled: true
  googleLogin:
    enabled: true
  smtp:
    enabled: true

db:
  name: keycloak
  user: keycloak
  password: yourPassword

keycloak:
  admin:
    user: admin
    pass: yourPassword
  smtp:
    host: smtp.gmail.com
    port: 587
    user: your-email@gmail.com
    password: your-app-password
    from: noreply@yourdomain.com
  google:
    clientId: your-google-client-id.apps.googleusercontent.com
    clientSecret: your-google-client-secret

frontend:
  url: http://localhost:3000
```

### 3. Run the deployment script

```bash
./deploy.sh
```

This generates:
- `.env` file for docker-compose
- `docker-compose.yml` with feature-specific environment variables
- Keycloak realm JSON files (`realm-dev.json`, `realm-prod.json`)

### 4. Start the containers

```bash
docker compose up -d
```

### 5. Verify it's running

```bash
docker ps
```

You should see `archcore-postgres` and `archcore-keycloak` containers in running state.

## Feature Flags

Control optional features via `saas-init.yml`:

| Feature | Description | Env Vars Required |
|---------|-------------|-------------------|
| `forgotPassword` | Shows "Forgot Password" link on login | SMTP config |
| `googleLogin` | Shows "Login with Google" button | Google OAuth config |
| `smtp` | Configures email server for notifications | SMTP config |

### Examples

**Full features (Google + SMTP + Forgot Password):**
```yaml
features:
  forgotPassword:
    enabled: true
  googleLogin:
    enabled: true
  smtp:
    enabled: true
```

**Only password login (no Google, no email):**
```yaml
features:
  forgotPassword:
    enabled: false
  googleLogin:
    enabled: false
  smtp:
    enabled: false
```

**Password + Forgot Password (requires SMTP):**
```yaml
features:
  forgotPassword:
    enabled: true
  googleLogin:
    enabled: false
  smtp:
    enabled: true
```

**Password + Google (no email notifications):**
```yaml
features:
  forgotPassword:
    enabled: false
  googleLogin:
    enabled: true
  smtp:
    enabled: false
```

## Access

| Service | URL |
|---------|-----|
| Keycloak Admin Panel | http://localhost:8080 |
| PostgreSQL | localhost:5433 |

**Keycloak login:** Use the `KEYCLOAK_ADMIN_USER` and `KEYCLOAK_ADMIN_PASS` defined in your `saas-init.yml` file.

## Generated Realms

With `PROJECT_NAME=archcore` (or `myproject` if you used that):

| Realm | Purpose |
|-------|---------|
| `archcore-dev` | Development environment (open registration, SSL not required) |
| `archcore` | Production environment (registration closed, SSL required) |

## File Structure

```
├── saas-init.yml                          # Master config (single source of truth)
├── saas-init.yml.example                  # Template config file
├── deploy.sh                              # Deployment script (generates all configs)
├── .env                                   # Generated env vars (not committed)
├── docker-compose.yml                     # Generated container definitions (not committed)
├── infrastructure/keycloak/
│   ├── Dockerfile                         # Custom Keycloak image with JWE SPI
│   ├── templates/
│   │   ├── realm-dev.json.template        # Dev realm template
│   │   ├── realm-prod.json.template       # Prod realm template
│   │   ├── realm-dev.json                 # Generated (not committed)
│   │   └── realm-prod.json                # Generated (not committed)
│   └── import/
│       └── (legacy import files)
```

## How It Works

```
saas-init.yml  →  deploy.sh  →  .env  →  docker compose
                     ↓
               .template → .json (Keycloak import)
                     ↓
               docker-compose.yml (dynamic generation)
```

1. `deploy.sh` parses `saas-init.yml` and flattens keys to env vars
2. `.template` files are processed with `envsubst`
3. `jq` post-processes realm JSON based on feature flags
4. `docker-compose.yml` is generated with only required env vars

## Common Commands

```bash
# Stop containers
docker compose down

# Stop containers and delete data
docker compose down -v

# Show logs
docker logs archcore-keycloak
docker logs archcore-postgres

# Shell into container
docker exec -it archcore-keycloak bash

# Regenerate configs (after editing saas-init.yml)
./deploy.sh && docker compose up -d
```

## Troubleshooting

**Port conflict:** The `docker-compose.yml` uses `5433:5432`. Change it if you need a different port.

**Container not starting — check logs:**
```bash
docker logs archcore-keycloak
docker logs archcore-postgres
```

**Reset database:**
```bash
docker compose down -v
./deploy.sh
docker compose up -d
```

**Missing features in Keycloak:**
- Check `saas-init.yml` feature flags are enabled
- Re-run `./deploy.sh` to regenerate configs
- Restart Keycloak: `docker compose restart keycloak`
