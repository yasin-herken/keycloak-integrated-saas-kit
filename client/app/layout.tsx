import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "ArchCore - Enterprise SaaS Kit with SSO Integration",
  description: "Production-ready Java SaaS boilerplate with Keycloak SSO, JWE encryption, and built-in billing.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
