Act as a Principal Full-Stack SaaS Architect and UI/UX Expert.

[CONTEXT & ARCHITECTURE]
We are finalizing the frontend and identity layers for our "Zero-Touch Enterprise SaaS Boilerplate".
- Backend: Spring Boot (Port 8081) expecting JWE Bearer tokens.
- Identity Provider: Keycloak (Port 8080).
- Strategy: We provide the end-developer with TWO premium assets:
    1. A custom, highly aesthetic Keycloak Theme (so they don't have to build auth pages).
    2. A lightweight Next.js MVP template configured to talk to Keycloak and the Backend.

[OBJECTIVE]
Generate the code and directory structures for both the Keycloak Theme and the Next.js MVP.

---
PART 1: The Premium Keycloak Theme (Tailwind CSS)
Instead of the ugly default Keycloak UI, we want a minimalist, modern, "Vercel/Stripe-like" login page.
1. Provide the directory structure for a custom Keycloak theme (e.g., `infrastructure/keycloak/themes/archcore/login/`).
2. Generate `theme.properties` (inheriting from keycloak base).
3. Generate a complete `login.ftl` (FreeMarker template) that:
    - Injects Tailwind CSS via CDN (for simplicity in the boilerplate).
    - Features a clean, centered white card on a light-gray background (`bg-gray-50`).
    - Has a placeholder for a dynamic Company Logo and Title.
    - Beautifully styles the username/password inputs, the "Sign In" button (dark mode aesthetic), and the "Forgot Password" link.

---
PART 2: The Next.js MVP Boilerplate
Generate a lightweight Next.js 15 (App Router) + Tailwind + NextAuth.js (Auth.js) template.
1. `env.local.example`: NextAuth and Keycloak credentials.
2. `auth.ts` (or `app/api/auth/[...nextauth]/route.ts`):
    - Configure KeycloakProvider.
    - CRITICAL: Implement the `jwt` and `session` callbacks. Extract the `access_token` (JWE) from Keycloak and inject it into the session object.
3. `app/page.tsx`:
    - A sleek landing page.
    - Unauthenticated state: "Sign in to Dashboard" button (redirects to Keycloak).
    - Authenticated state: Shows user info and a "Sign Out" button.
4. `app/components/BackendTester.tsx`:
    - A Client Component with a "Test Backend Connection" button.
    - Fetches `http://localhost:8081/api/v1/users/me` using `Authorization: Bearer <session.accessToken>`.
    - Displays the JSON response to prove end-to-end JWE integration.

[CONSTRAINTS]
Ensure the code is clean, copy-paste ready, and requires zero manual configuration beyond setting environment variables. The Keycloak FTL should gracefully handle Keycloak's login form variables (e.g., `url.loginAction`, `login.username`).