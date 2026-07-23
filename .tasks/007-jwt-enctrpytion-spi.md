Act as an expert Keycloak Identity and Access Management developer and adhere strictly to the "Enterprise SaaS Kit - Global Agent Rules (RULES.md)". I need to develop a custom Keycloak SPI (Service Provider Interface) for Keycloak version 26.0.8.

Global Project Rules & Execution Constraints:
1. Architecture Stack: Java 25 (Temurin 25.0.3), Spring Boot 4, Keycloak 26+, PostgreSQL (latest).
2. JDK Path: The project strictly uses the JDK located at `/Users/yasinherken/Library/Java/JavaVirtualMachines/temurin-25.0.3/Contents/Home`. Ensure any compiler plugins or environment configurations in `pom.xml` reflect Java 25 targets.
3. Package Naming: All packages MUST start with `com.archcore`.
4. Maven Usage: You must assume the presence of and respect a `.mvn/settings.xml` file for dependencies. Include Lombok to reduce boilerplate.
5. MCP Usage Requirement: Before writing any code or configurations, you MUST use your MCP tools (e.g., doc-fetcher, web-search) to consult the official Keycloak 26 SPI documentation. Do not use memorized information for infrastructure or SPI core classes. You must cite the documentation URL in your output.
6. Execution Limits: Do not modify or create files outside the scope of this specific JWE encryption task.

Task Context:
By default, Keycloak returns Access Tokens as 3-part JWS (Signed JWT). I need a custom SPI that intercepts the Access Token generation process for a specific client ("archcore-client") and encrypts the token, returning a 5-part JWE (JSON Web Encryption) in the OIDC token response.

Technical Requirements:
1. Keycloak Version: 26.0.8
2. Target Client: "archcore-client"
3. Key Management Algorithm (alg): RSA-OAEP
4. Content Encryption Algorithm (enc): A256GCM
5. Public Key Fetching: The SPI needs to fetch the Resource Server's public RSA key. It should try to fetch it from an external JWKS URL (e.g., http://host.docker.internal:8081/.well-known/jwks.json). Implement a caching mechanism for the JWKS to avoid network calls on every token request.
6. SPI Type: Determine the most appropriate SPI interface using Keycloak's internal JOSE/JWE libraries (`org.keycloak.jose.jwe.*`).
7. Testing: The project must include robust unit tests using JUnit 5 and Mockito.

Deliverables:
1. A complete `pom.xml` using Maven, including the correct Keycloak 26 SPI/services dependencies, Lombok, `junit-jupiter`, and `mockito-core`. Target Java release must be 25.
2. The complete Java implementation of the Provider and ProviderFactory classes (under `com.archcore.*`).
3. The required `META-INF/services/` registration files.
4. Comprehensive Unit Tests: Verify the JWE encryption logic. Assert that the token for "archcore-client" is correctly wrapped as a 5-part JWE string.
5. Step-by-step instructions: How to build the JAR and deploy it to a Keycloak 26 Docker container, citing the exact Keycloak docs used for the SPI deployment.

Please write clean, enterprise-grade Java 25 code and provide your response in English.