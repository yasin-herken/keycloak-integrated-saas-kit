# Keycloak Integrated SaaS Kit

Ready-to-use development environment that boots Keycloak and PostgreSQL infrastructure with a single command.

## Quick Start

### 1. Create the `.env` file

```bash
cp .env.example .env
```

### 2. Edit the `.env` file

```env
PROJECT_NAME=myproject              # Realm names are derived from this (myproject-dev, myproject)
DB_NAME=keycloak
DB_USER=keycloak
DB_PASSWORD=yourPassword            # Enter a strong password
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASS=yourPassword    # Enter a strong password
```

### 3. Generate realm configurations

```bash
./generate-realm-configs.sh
```

### 4. Start the containers

```bash
docker compose up -d
```

### 5. Verify it's running

```bash
docker ps
```

You should see `myproject-postgres` and `myproject-keycloak` containers in running state.

## Access

| Service | URL |
|---------|-----|
| Keycloak Admin Panel | http://localhost:8080 |
| PostgreSQL | localhost:5433 |

**Keycloak login:** Use the `KEYCLOAK_ADMIN_USER` and `KEYCLOAK_ADMIN_PASS` defined in your `.env` file.

## Generated Realms

With `PROJECT_NAME=myproject`:

| Realm | Purpose |
|-------|---------|
| `myproject-dev` | Development environment (open registration, SSL not required) |
| `myproject` | Production environment (registration closed, SSL required) |

## File Structure

```
├── .env.example                          # Template env file
├── .env                                  # Actual values (not committed to git)
├── docker-compose.yml                    # Container definitions
├── generate-realm-configs.sh             # Template → JSON converter
├── infrastructure/keycloak/
│   ├── templates/
│   │   ├── realm-dev.json.template       # Dev realm template
│   │   └── realm-prod.json.template      # Prod realm template
│   └── import/
│       ├── realm-dev.json                # Generated (not committed to git)
│       └── realm-prod.json               # Generated (not committed to git)
```

## Common Commands

```bash
# Stop containers
docker compose down

# Stop containers and delete data
docker compose down -v

# Show logs
docker logs myproject-keycloak
docker logs myproject-postgres

# Shell into container
docker exec -it myproject-keycloak bash

# Regenerate realm configs (after editing templates)
./generate-realm-configs.sh && docker compose restart keycloak
```

## Troubleshooting

**Port conflict:** The `docker-compose.yml` uses `5433:5432`. Change it if you need a different port.

**Container not starting — check logs:**
```bash
docker logs myproject-keycloak
docker logs myproject-postgres
```

**Reset database:**
```bash
docker compose down -v
./generate-realm-configs.sh
docker compose up -d
```
