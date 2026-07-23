# JWT Encryption SPI - Session Summary

**Date:** 2026-01-27
**Task:** 007 - JWT Encryption SPI Implementation

---

## Objective

Develop a custom Keycloak 26.0.8 SPI that intercepts Access Token generation for "archcore-client" and encrypts the token into a 5-part JWE (RSA-OAEP-256 / A256GCM) using the Resource Server's public key fetched from a JWKS endpoint.

---

## Tech Stack

- **Java:** 25 (Temurin 25.0.3)
- **Spring Boot:** 4
- **Keycloak:** 26.0.8
- **Database:** PostgreSQL

---

## Environment Paths

- **JDK Path:** `/Users/yasinherken/Library/Java/JavaVirtualMachines/temurin-25.0.3/Contents/Home`
- **Maven Settings:** `.mvn/settings.xml`

---

## Key Technical Decisions

### Package Naming
All packages must start with `com.archcore`.

### SPI Type Discovery
- `TokenPostProcessor` SPI does **NOT** exist in Keycloak 26.0.8 (it's from a newer version)
- **Correct approach:** Protocol Mapper implementing `OIDCAccessTokenResponseMapper`
- Invoked by `TokenManager.transformAccessTokenResponse`

### Encryption Algorithm
- **JWE Algorithm:** `RSA-OAEP-256` (aligned with backend's `JwtDecoderConfig`)
- **JWE Encryption:** `A256GCM`

### JWKS URL
- `http://host.docker.internal:8081/.well-known/jwks.json`

### Keycloak JWE Classes
- Located in `keycloak-core`: `org.keycloak.jose.jwe.*`
- Key classes: `JWE`, `JWEHeader`, `JWEConstants`

### Nimbus JOSE Classes
- `com.nimbusds.jose.crypto.RSAEncrypter` (not `RSAESOAEPkwEncrypter`)
- `com.nimbusds.jose.Payload` (not `com.nimbusds.jose.payload.JWSPayload`)

### Mockito + Java 25 Compatibility
- Mockito 5.14.2 needs ByteBuddy 1.17.5 override to work with Java 25

---

## Completed Work

### 1. Maven Project Configuration
**File:** `keycloak-jwe-spi/pom.xml`

Dependencies:
- keycloak-parent 26.0.8 BOM
- keycloak-core (provided)
- keycloak-services (provided)
- keycloak-server-spi (provided)
- nimbus-jose-jwt 9.37.3
- JUnit 5.11.4
- Mockito 5.14.2
- ByteBuddy 1.17.5 (override)

### 2. Core SPI Implementation
**File:** `keycloak-jwe-spi/src/main/java/com/archcore/keycloak/spi/jwe/JweAccessTokenResponseMapper.java`

- Extends `AbstractOIDCProtocolMapper`
- Implements `OIDCAccessTokenResponseMapper`
- Encrypts access token via Nimbus `RSAEncrypter` + `JWEObject` (RSA-OAEP-256 / A256GCM)
- Skips encryption for non-target clients
- Configurable JWKS URL via `ConfigProperty`

### 3. JWKS Client
**File:** `keycloak-jwe-spi/src/main/java/com/archcore/keycloak/spi/jwe/JwksClient.java`

- JWKS fetcher with `AtomicReference<CachedJwks>` TTL cache
- Stale-on-failure fallback
- Uses `java.net.http.HttpClient`

### 4. SPI Service Registration
**File:** `keycloak-jwe-spi/src/main/resources/META-INF/services/org.keycloak.protocol.ProtocolMapper`

Content: `com.archcore.keycloak.spi.jwe.JweAccessTokenResponseMapper`

### 5. Unit Tests
**File:** `keycloak-jwe-spi/src/test/java/com/archcore/keycloak/spi/jwe/JweAccessTokenResponseMapperTest.java`
- 8 tests: JWE 5-part validation, skip for non-target client, null/empty token handling, config properties, createMapper factory method

**File:** `keycloak-jwe-spi/src/test/java/com/archcore/keycloak/spi/jwe/JwksClientTest.java`
- 5 tests: fetch, cache, stale fallback, 404 error, empty JWKS

### 6. Gitignore & VCS
**File:** `.gitignore`

Added entries:
- `*.pem`, `*.key`, `*.p12`, `*.pfx`, `*.jks`
- `target/`, `out/`, `build/`
- `.vscode/`, `*.swp`, `*.swo`
- `.env.*`

All new files added to Git VCS via `git add -A`.

### 7. Compilation Status
```bash
cd keycloak-jwe-spi && JAVA_HOME=/Users/yasinherken/Library/Java/JavaVirtualMachines/temurin-25.0.3/Contents/Home mvn clean compile -s ../.mvn/settings.xml
```
**Result:** ✅ Passes

---

## Current State

### Unit Tests
- **Total:** 15 tests
- **Passing:** 15 tests ✅
- **Failing:** 0 tests

### Package Structure
```
keycloak-jwe-spi/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/archcore/keycloak/spi/jwe/
│   │   │   ├── JweAccessTokenResponseMapper.java
│   │   │   └── JwksClient.java
│   │   └── resources/META-INF/services/
│   │       └── org.keycloak.protocol.ProtocolMapper
│   └── test/
│       └── java/com/archcore/keycloak/spi/jwe/
│           ├── JweAccessTokenResponseMapperTest.java
│           └── JwksClientTest.java
```

---

## Next Steps

1. ~~Re-run tests~~ - ✅ All 15 tests passing
2. **Package the JAR:** `mvn clean package -s ../.mvn/settings.xml`
3. **Provide deployment instructions** for Keycloak Docker container:
   - Mount JAR to `/opt/keycloak/providers/`
   - Run `kc.sh build`
   - Restart Keycloak

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `keycloak-jwe-spi/pom.xml` | Maven project definition for the SPI |
| `keycloak-jwe-spi/src/main/java/com/archcore/keycloak/spi/jwe/JweAccessTokenResponseMapper.java` | Core SPI: Protocol Mapper that encrypts access tokens to JWE |
| `keycloak-jwe-spi/src/main/java/com/archcore/keycloak/spi/jwe/JwksClient.java` | JWKS fetcher with caching |
| `keycloak-jwe-spi/src/main/resources/META-INF/services/org.keycloak.protocol.ProtocolMapper` | SPI service registration |
| `keycloak-jwe-spi/src/test/java/com/archcore/keycloak/spi/jwe/JweAccessTokenResponseMapperTest.java` | Mapper unit tests |
| `keycloak-jwe-spi/src/test/java/com/archcore/keycloak/spi/jwe/JwksClientTest.java` | JWKS client unit tests |
| `.gitignore` | Secrets/certs/build outputs ignore rules |
| `.opencode/RULES.md` | Global agent rules |
| `.tasks/007-jwt-enctrpytion-spi.md` | Task specification |
| `backend/archcore-security/src/main/java/com/archcore/security/config/JwtDecoderConfig.java` | Backend JWE decoder (uses RSA-OAEP-256) |
| `backend/archcore-security/src/main/java/com/archcore/security/config/JwksController.java` | JWKS endpoint serving public key (RSA-OAEP) |
| `infrastructure/keycloak/import/realm-dev.json` | Keycloak realm config with archcore-client |
| `docker-compose.yml` | Keycloak 26.0 + PostgreSQL setup |

---

## Important Notes

1. **Keycloak 26.0.8 does NOT have `TokenPostProcessor` SPI** - Use Protocol Mapper instead
2. **Encryption algorithm is `RSA-OAEP-256`** - Aligned with backend's `JwtDecoderConfig`
3. **JWKS endpoint** serves RSA-OAEP-256 public key at `http://host.docker.internal:8081/.well-known/jwks.json`
4. **All new files must be added to Git VCS** - Enforced by `.opencode/RULES.md`
5. **Build command:** `JAVA_HOME=/Users/yasinherken/Library/Java/JavaVirtualMachines/temurin-25.0.3/Contents/Home mvn clean compile -s ../.mvn/settings.xml`
