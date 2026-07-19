---
id: 004
title: Spring Security 7 Resource Server with JWE (Encrypted JWT)
agent: security-agent
status: completed
---

## Task Description
Configure the `archcore-security` module as an OAuth2 Resource Server. A critical security requirement is that the JWTs must be encrypted (JWE - JSON Web Encryption) so that the payload is completely opaque to clients and intermediaries. The application must decrypt the incoming JWT before signature validation and role extraction.

## Hard Constraints
* **Framework:** Spring Security 7.x (Context 7 standard). No deprecated `WebSecurityConfigurerAdapter`.
* **Token Type:** JWE (Encrypted JWT). Standard JWS is not sufficient.
* **Statelessness:** Session policy must be strictly `STATELESS`.

## Requirements
1. Add `spring-boot-starter-security` and `spring-boot-starter-oauth2-resource-server` dependencies to `archcore-security`.
2. Create `SecurityConfig.java` with a `SecurityFilterChain` bean. Secure all endpoints, set session creation policy to stateless, and disable CSRF.
3. Configure a custom `JwtDecoder` bean using `NimbusJwtDecoder` to handle JWE:
    * It must use a Private Key (e.g., `RSAPrivateKey`) to decrypt the incoming token.
    * *Note for Agent:* For this step, load a dummy/test RSA Key Pair from the classpath or environment just to establish the decryption pipeline.
    * After decryption, it must validate the issuer and signature against Keycloak's JWKS endpoint.
4. Create `KeycloakJwtAuthenticationConverter` (implementing `Converter<Jwt, AbstractAuthenticationToken>`) to extract the `realm_access.roles` from the *decrypted* JWT and prefix them with `ROLE_`.
5. Wire the custom `JwtDecoder` and the `JwtAuthenticationConverter` into the `oauth2ResourceServer` configuration in the `SecurityFilterChain`.

## Acceptance Criteria
* The application successfully starts with the custom JWE `JwtDecoder` configured.
* Security rules mandate authentication for all API paths.
  * A dummy RSA private key is wired into the decoder pipeline to prove the JWE decryption capability is active.