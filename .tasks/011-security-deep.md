You are a Senior DevSecOps Engineer, Cybersecurity Expert, and Spring Boot Architect.
My Goal: To harden my actively used, production (PROD) Spring Boot (REST API) project and eliminate all known security vulnerabilities (including the OWASP Top 10).

Without breaking the existing business logic, implement the following steps according to Spring Boot 3.x and Spring Security 6.x (Lambda DSL) standards. Provide clean code and brief explanations for each:

STEP 1: Security Headers and Web Vulnerabilities
- Write a strict `SecurityFilterChain` configuration including HSTS (Strict-Transport-Security), X-Frame-Options, X-Content-Type-Options, and a strict CSP (Content-Security-Policy) to protect against XSS, Clickjacking, and MIME-Sniffing attacks.

STEP 2: CORS and CSRF Hardening
- Create a strict CORS configuration suitable for PROD that explicitly allows only specific origins, methods, and headers, preventing dangerous misconfigurations like "allowedOrigins('*')".
- If the API is stateless, disable CSRF securely. If it uses sessions/cookies, integrate cookie-based CSRF protection (HttpOnly, Secure, SameSite=Strict).

STEP 3: Information Disclosure and Error Handling
- Write a global `@ControllerAdvice` / `@ExceptionHandler` class to prevent Spring Boot's default Whitelabel Error Page and Stack Traces from being exposed to the end-user. Errors should be logged internally, but the user should only receive a generic, standardized JSON error message.
- Explain your approach to Log Masking/Sanitization to prevent sensitive data (Passwords, SSN, Credit Card info, etc.) from being written to logs.

STEP 4: Input Validation and Mass Assignment Protection
- Show me how to apply Spring Boot Validation rules (`@NotBlank`, `@Pattern`, `@Size`, etc.) in Request DTO (Data Transfer Object) classes to prevent SQL Injection, NoSQL Injection, and XSS attacks.
- Provide an example enforcing the use of DTOs instead of passing Entities directly to Controllers, in order to prevent Mass Assignment attacks.

STEP 5: Actuator and Configuration Security
- If I am using Spring Boot Actuator, write the `application.yml` configuration that exposes only `/health` and `/info` endpoints for PROD. Hide or secure all other endpoints to prevent information disclosure (like heapdumps or env variables).

STEP 6: Rate Limiting (Optional but Critical)
- Provide the code to implement Rate Limiting using `Bucket4j` or `Resilience4j` on critical endpoints (e.g., /login, /register) to mitigate Brute Force or basic DDoS attacks.

Please provide clean, copy-paste ready code blocks (Java and YAML) for each step, and briefly explain how I can integrate these changes without breaking my existing code.