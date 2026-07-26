# TASK: SPI Dynamic Configuration Audit & Refactoring Plan

Analyze the newly created Keycloak JWE Access Token SPI codebase. Currently, there might be hardcoded values that violate enterprise production readiness. You are running in PLAN MODE. Do NOT modify the code yet. Your task is to identify static values and propose a Keycloak-native dynamic configuration plan.

## 1. Audit Requirements
Scan the SPI source code (Provider, Factory, and any utility/cache classes) and identify if any of the following are hardcoded:
- The target client ID (e.g., "archcore-client").
- The Resource Server JWKS URL (e.g., "http://host.docker.internal:8081/.well-known/jwks.json").
- Encryption algorithms (RSA-OAEP, A256GCM).
- Cache Expiration/TTL limits for the JWKS fetching mechanism.

## 2. Dynamic Implementation Strategy (Keycloak Native)
Do not use Spring annotations (like @Value) since this runs inside the Keycloak container. You must propose a plan utilizing one or both of the following Keycloak standards:
- **Client Attributes:** E.g., fetching `ClientModel.getAttribute("jwe.jwks.url")` or `ClientModel.getAttribute("jwe.enabled")` to determine if the token should be encrypted and where to get the key dynamically per client.
- **Provider Configuration:** Utilizing `ProviderFactory.init(Config.Scope config)` to read system-wide defaults from Keycloak environment variables (`keycloak.conf`).

## 3. Global Rules Compliance
Ensure your proposed plan strictly adheres to our `RULES.md`:
- Java 25 & Lombok usage for any new configuration wrapper classes.
- VCS rules: No `.env` or IDE files should be tracked. Any new files must be added to VCS immediately.

## Output Expectation
Provide a step-by-step markdown report outlining:
1. The static values you found in the current codebase.
2. Your proposed architectural approach to making them dynamic (Client Attributes vs. Environment Variables, and why).
3. The specific classes and methods that will need to be refactored.
   Ask for my approval on this plan before executing any code changes.