import NextAuth from "next-auth";
import Keycloak from "next-auth/providers/keycloak";
import Credentials from "next-auth/providers/credentials";

export const {
  handlers,
  signIn,
  signOut,
  auth,
} = NextAuth({
  providers: [
    Keycloak({
      id: "keycloak",
      clientId: process.env.KEYCLOAK_CLIENT_ID!,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      issuer: process.env.KEYCLOAK_ISSUER!,
    }),
    Keycloak({
      id: "keycloak-google",
      clientId: process.env.KEYCLOAK_CLIENT_ID!,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      issuer: process.env.KEYCLOAK_ISSUER!,
      authorization: {
        params: {
          kc_idp: "google",
        },
      },
    }),
    Credentials({
      name: "Email",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) return null;

        const keycloakUrl = process.env.KEYCLOAK_ISSUER!;
        const tokenEndpoint = `${keycloakUrl}/protocol/openid-connect/token`;

        const body = new URLSearchParams({
          grant_type: "password",
          client_id: process.env.KEYCLOAK_CLIENT_ID!,
          client_secret: process.env.KEYCLOAK_CLIENT_SECRET!,
          username: credentials.email as string,
          password: credentials.password as string,
          scope: "openid email profile",
        });

        const res = await fetch(tokenEndpoint, {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: body.toString(),
        });

        const data = await res.json();

        if (!res.ok || !data.access_token) {
          return null;
        }

        // Decode access_token to get user info
        const payload = JSON.parse(
          Buffer.from(data.access_token.split(".")[1], "base64url").toString()
        );

        return {
          id: payload.sub,
          name: payload.name || payload.preferred_username,
          email: payload.email,
          image: payload.picture,
          accessToken: data.access_token,
          idToken: data.id_token,
        };
      },
    }),
  ],
  callbacks: {
    async jwt({ token, account, user }) {
      // Credentials provider
      if (user) {
        token.accessToken = (user as any).accessToken;
        token.idToken = (user as any).idToken;
        token.provider = "credentials";
      }
      // OAuth provider (Keycloak)
      if (account && (account.provider === "keycloak" || account.provider === "keycloak-google")) {
        token.accessToken = account.access_token;
        token.idToken = account.id_token;
        token.provider = "keycloak";
      }
      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken as string;
      session.idToken = token.idToken as string;
      session.user.id = token.sub as string;
      session.provider = token.provider as string;
      return session;
    },
  },
  pages: {
    signIn: "/auth/signin",
    error: "/auth/error",
  },
});

declare module "next-auth" {
  interface Session {
    accessToken?: string;
    idToken?: string;
    provider?: string;
    user: {
      id?: string;
      name?: string | null;
      email?: string | null;
      image?: string | null;
    };
  }
}
