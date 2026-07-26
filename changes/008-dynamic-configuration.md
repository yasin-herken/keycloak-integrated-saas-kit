# Task 008: SPI Dynamic Configuration Audit & Refactoring — Execution Log

**Task File:** `.tasks/008-dynamic-configuration.md`
**Date:** 2026-07-24
**Status:** Completed

---

## 1. Task Summary

Audited the Keycloak JWE Access Token SPI codebase for hardcoded values and refactored them into a dynamic configuration system using Keycloak-native `init(Config.Scope)` and `keycloak.conf` environment variable references.

### Static Values Identified (Before)

| Value | Location | Type |
|-------|----------|------|
| `"archcore-client"` | `JweAccessTokenResponseMapper.java:37` | `DEFAULT_TARGET_CLIENT` |
| `"http://host.docker.internal:8081/.well-known/jwks.json"` | `JweAccessTokenResponseMapper.java:36` | `DEFAULT_JWKS_URL` |
| `5 * 60 * 1000L` (5 min) | `JweAccessTokenResponseMapper.java:38` | `DEFAULT_CACHE_TTL_MS` |
| `"RSA-OAEP-256"` | `JweAccessTokenResponseMapper.java:40` | `JWE_ALGORITHM` |
| `"A256GCM"` | `JweAccessTokenResponseMapper.java:41` | `ENCRYPTION_METHOD` |
| `"archcore-enc-key"` | `JweAccessTokenResponseMapper.java:42` | `KEY_ID` |

---

## 2. Applied Design Patterns

### Combined Provider + Factory Pattern (Keycloak ProtocolMapper SPI)
- **Implementation:** `JweAccessTokenResponseMapper` extends `AbstractOIDCProtocolMapper` which implements `ProtocolMapper` — an interface that extends BOTH `Provider` AND `ProviderFactory<ProtocolMapper>`. The mapper IS its own factory. `init(Config.Scope)` is overridden to read configuration at Keycloak bootstrap.
- **Rationale:** This is Keycloak's native SPI design for ProtocolMappers. There is no separate `ProtocolMapperFactory` interface. The mapper class serves dual responsibility, and `init()` is called once at startup before any instances are created. A `static volatile` field stores the config for use by all instances.

### Configuration Object Pattern (`JweProviderConfig`)
- **Implementation:** Lombok `@Data` class encapsulating all JWE configuration properties with a `fromScope(Config.Scope)` factory method.
- **Rationale:** Single source of truth for configuration values. Eliminates scattered `static final` constants. Lombok satisfies RULES.md Rule 1 (mandatory Lombok for configuration classes).

### Strategy Pattern (Per-Client vs. System-Wide)
- **Implementation:** System-wide defaults via `keycloak.conf` env vars (algorithm, encryption method, key ID). Per-mapper overrides via `ProtocolMapperModel` config (target client, JWKS URL, cache TTL).
- **Rationale:** Security-critical values (algorithm, key ID) are system-wide to prevent admin console misconfiguration. Operational values (JWKS URL, cache TTL) are per-mapper for deployment flexibility.

### Null Object Pattern (Config Scope Fallback)
- **Implementation:** `JweProviderConfig.fromScope(null)` returns sensible defaults when no config scope is available (backward compatibility).
- **Rationale:** Prevents NPE during unit testing and allows the mapper to function with defaults when not configured through `init()`.

---

## 3. Architectural Rationale

### Why init(Config.Scope) directly on the mapper (not a separate factory)?
Keycloak's `ProtocolMapper` interface extends both `Provider` and `ProviderFactory<ProtocolMapper>`. There is no separate `ProtocolMapperFactory` interface. The mapper class IS the factory. `AbstractOIDCProtocolMapper` provides default implementations for `init()`, `create()`, `postInit()`, and `close()`. We override `init()` to inject our configuration.

### Why Lombok @Data instead of Java Record?
RULES.md Rule 1 mandates Lombok for configuration classes. While Java records provide immutability and boilerplate elimination, the project convention requires Lombok. `@Data` with `@NoArgsConstructor` and `@AllArgsConstructor` provides the same benefits while maintaining consistency.

### Why split system-wide vs. per-mapper config?
- **System-wide** (algorithm, encryption method, key ID): These are security-critical parameters. If an admin misconfigures the encryption method in the UI, token decryption on the backend would fail silently. Locking these to env vars prevents this.
- **Per-mapper** (target client, JWKS URL, cache TTL): These vary per deployment and per client. Making them configurable in the admin console allows operators to adjust without rebuilding.

---

## 4. Files Changed

### New Files
| File | Description |
|------|-------------|
| `keycloak-jwe-spi/src/main/java/.../JweProviderConfig.java` | Lombok `@Data` configuration wrapper |
| `infrastructure/keycloak/keycloak.conf` | Keycloak config referencing env vars |
| `changes/008-dynamic-configuration.md` | This execution log |

### Modified Files
| File | Changes |
|------|---------|
| `JweAccessTokenResponseMapper.java` | Removed 6 hardcoded constants. Added `init(Config.Scope)` override. Added `static volatile JweProviderConfig`. Updated `encryptToJwe()` and `getJwksClient()` to use config. |
| `JweAccessTokenResponseMapperTest.java` | Updated to inject `JweProviderConfig` via reflection for testing. |
| `pom.xml` | Added Lombok dependency and annotation processor config. |
| `.env.example` | Added `ARCHCORE_JWE_*` environment variables. |
| `docker-compose.yml` | Mounts `keycloak.conf`, passes JWE env vars to Keycloak container. |
| `META-INF/services/org.keycloak.protocol.ProtocolMapper` | Restored — registers mapper directly (Keycloak's native SPI pattern). |

### Removed Files
| File | Reason |
|------|--------|
| `JweAccessTokenResponseMapperFactory.java` | Not needed — `ProtocolMapper` IS the factory (extends `ProviderFactory<ProtocolMapper>`) |
| `JweAccessTokenResponseMapperFactoryTest.java` | Removed with factory |
| `META-INF/services/org.keycloak.protocol.ProtocolMapperFactory` | Interface doesn't exist in Keycloak |

---

## 5. Configuration Flow

```
.env (environment variables)
  └─→ docker-compose.yml (env section)
        └─→ keycloak.conf (${ENV_VAR:default} syntax)
              └─→ AbstractOIDCProtocolMapper.init(Config.Scope) [called by Keycloak at startup]
                    └─→ JweAccessTokenResponseMapper.init(config)
                          └─→ JweProviderConfig.fromScope(config) [stored in static field]
                                └─→ encryptToJwe() / getJwksClient() [per-request use]
```

### Environment Variables (keycloak.conf)

| Variable | Default | Purpose |
|----------|---------|---------|
| `ARCHCORE_JWE_DEFAULT_JWKS_URL` | `http://host.docker.internal:8081/.well-known/jwks.json` | Default JWKS endpoint |
| `ARCHCORE_JWE_CACHE_TTL_MS` | `300000` (5 min) | JWKS cache TTL |
| `ARCHCORE_JWE_ALGORITHM` | `RSA-OAEP-256` | JWE key wrapping algorithm |
| `ARCHCORE_JWE_ENCRYPTION_METHOD` | `A256GCM` | JWE content encryption |
| `ARCHCORE_JWE_KEY_ID` | `archcore-enc-key` | JWK key ID for encryption key |

---

## 6. VCS & Security Audit

- **.gitignore compliance:** `.env` is properly ignored. `keycloak.conf` contains no secrets (only env var references). No `*.pem`, `*.key`, `target/`, or `.idea/` files tracked.
- **Hardcoded secrets:** No passwords, API keys, or private keys found in generated code.
- **New files:** All new Java source files, config files, and this changelog are VCS-tracked.

---

## 7. Documentation Source

- Keycloak Server Developer Guide: `ProtocolMapper` extends `ProviderFactory<ProtocolMapper>` — the mapper IS the factory
- Keycloak `AbstractOIDCProtocolMapper` source: `init(Config.Scope)`, `create(KeycloakSession)` default implementations
- Keycloak Configuration Guide: Environment variable references in `keycloak.conf` via `${ENV_VAR:fallback}` syntax
- Source: https://github.com/keycloak/keycloak/blob/main/docs/documentation/server_development/topics/providers.adoc
