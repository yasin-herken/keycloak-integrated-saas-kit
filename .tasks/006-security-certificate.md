---
id: 004
title: Spring Security 7 Resource Server with Static JWE Keys
agent: security-agent
status: pending
---

## Task Description
Configure the `archcore-security` module as an OAuth2 Resource Server. The system must support JWE (Encrypted JWT) using a static RSA key pair to support a multi-microservice MVP architecture. The application must expose its Public Key via a standard JWKS endpoint so Keycloak can fetch it automatically for token encryption.

## Hard Constraints
* **Framework:** Spring Security 7.x (Context 7). No deprecated classes.
* **Token Type:** JWE (Encrypted JWT). 
* **Session:** Strictly `STATELESS`.

## Requirements
1. **Key Management:** Instead of generating keys in-memory, load the RSA key pair from the classpath (`classpath:test-public.pem` and `classpath:test-private.pem`). You can use Spring Security's `RsaKeyConverters` or standard Java NIO/KeyFactory to parse the PEM files into `RSAPublicKey` and `RSAPrivateKey`.
2. **JWE Decoder:** Configure a custom `JwtDecoder` bean using `NimbusJwtDecoder`. It must use the loaded `RSAPrivateKey` to decrypt incoming JWE tokens. After decryption, it must validate the signature against Keycloak's issuer URI.
3. **JWKS Endpoint:** Create a `@RestController` mapped to `/.well-known/jwks.json`. It must return the loaded `RSAPublicKey` wrapped in a standard JSON Web Key Set (JWKS) format. The key must have `"use": "enc"` and `"alg": "RSA-OAEP"`.
4. **Security Filter Chain:** 
   * Configure `oauth2ResourceServer` to use the custom JWE `JwtDecoder`.
   * Implement a `JwtAuthenticationConverter` to extract Keycloak's `realm_access.roles` and map them to Spring Security authorities (prefix `ROLE_`).
   * Explicitly set `permitAll()` for the `/.well-known/jwks.json` endpoint so Keycloak can read it without authentication.
   * Require authentication for all other requests.

## Acceptance Criteria
* The app successfully loads the static `.pem` files on startup.
* `GET /.well-known/jwks.json` returns a valid JWKS public key payload.
* The `JwtDecoder` is correctly wired and can decrypt incoming JWE tokens.