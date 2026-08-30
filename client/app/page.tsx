import { auth } from "@/lib/auth";
import { Header } from "./components/Header";
import { Footer } from "./components/Footer";
import Link from "next/link";

export default async function Home() {
  const session = await auth();

  return (
    <div className="min-h-screen bg-white">
      <Header session={session} />

      {/* Hero Section */}
      <section className="pt-24 pb-16 bg-gradient-to-b from-gray-50 to-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 leading-tight">
                Enterprise SaaS Kit with{" "}
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-500 to-purple-600">
                  SSO Integration
                </span>
              </h1>
              <p className="mt-6 text-lg text-gray-600 max-w-lg">
                Production-ready Java SaaS boilerplate with Keycloak SSO, JWE encryption, 
                zero-touch infrastructure, and built-in billing.
              </p>
              <div className="mt-8 flex flex-col sm:flex-row gap-4">
                <Link
                  href="/auth/signup"
                  className="px-8 py-4 text-lg font-semibold text-white bg-gray-900 rounded-xl hover:bg-gray-800 transition-all shadow-lg hover:shadow-xl text-center"
                >
                  Get Started Now
                </Link>
                <Link
                  href="/#features"
                  className="px-8 py-4 text-lg font-semibold text-gray-900 bg-white border-2 border-gray-200 rounded-xl hover:border-gray-300 transition-all text-center"
                >
                  Learn More
                </Link>
              </div>
              <div className="mt-8 flex items-center space-x-6">
                <div className="flex -space-x-2">
                  {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 border-2 border-white flex items-center justify-center">
                      <span className="text-white text-xs font-medium">{i}</span>
                    </div>
                  ))}
                </div>
                <p className="text-sm text-gray-600">
                  <span className="font-semibold text-gray-900">500+</span> teams already using
                </p>
              </div>
            </div>
            <div className="relative">
              <div className="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-3xl p-8 shadow-2xl">
                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center space-x-3 mb-4">
                    <div className="w-3 h-3 rounded-full bg-red-400"></div>
                    <div className="w-3 h-3 rounded-full bg-yellow-400"></div>
                    <div className="w-3 h-3 rounded-full bg-green-400"></div>
                  </div>
                  <pre className="text-sm text-gray-800 font-mono">
{`// Keycloak SSO Integration
const session = await auth();
if (session) {
  // User is authenticated via SSO
  return <Dashboard user={session.user} />;
}
// Redirect to Keycloak login
return signIn("keycloak");`}
                  </pre>
                </div>
              </div>
              <div className="absolute -bottom-6 -left-6 w-32 h-32 bg-yellow-400 rounded-2xl opacity-20 blur-xl"></div>
              <div className="absolute -top-6 -right-6 w-32 h-32 bg-indigo-400 rounded-2xl opacity-20 blur-xl"></div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900">
              Cutting-edge Features
            </h2>
            <p className="mt-4 text-lg text-gray-600 max-w-2xl mx-auto">
              Everything you need to build and scale your SaaS application
            </p>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[
              {
                icon: "🔐",
                title: "Keycloak SSO",
                description: "Single Sign-On with Keycloak, supporting OIDC, SAML, and social logins out of the box."
              },
              {
                icon: "🛡️",
                title: "JWE Encryption",
                description: "JWT Web Encryption with custom SPI for secure token handling and validation."
              },
              {
                icon: "⚡",
                title: "Spring Boot 4",
                description: "Built on the latest Spring Boot with Java 25 for maximum performance and modern features."
              },
              {
                icon: "🗄️",
                title: "PostgreSQL 18",
                description: "Production-ready database setup with automatic migrations and connection pooling."
              },
              {
                icon: "💰",
                title: "Stripe Billing",
                description: "Complete billing integration with Stripe for subscriptions and payment processing."
              },
              {
                icon: "🚀",
                title: "One-Click Deploy",
                description: "Docker-based deployment with automated infrastructure provisioning and scaling."
              }
            ].map((feature, index) => (
              <div
                key={index}
                className="p-8 bg-gray-50 rounded-2xl hover:bg-white hover:shadow-xl transition-all duration-300 border border-transparent hover:border-gray-100"
              >
                <div className="w-14 h-14 bg-white rounded-xl flex items-center justify-center text-2xl shadow-md mb-6">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-3">{feature.title}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-16 items-center">
            <div>
              <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-6">
                Why Choose ArchCore?
              </h2>
              <p className="text-lg text-gray-600 mb-8">
                Built for modern SaaS applications, ArchCore provides everything you need 
                to launch your product faster with enterprise-grade security.
              </p>
              <ul className="space-y-4">
                {[
                  "Enterprise-grade security with JWE encryption",
                  "Single Sign-On with Keycloak integration",
                  "Production-ready infrastructure",
                  "Built-in billing with Stripe",
                  "Comprehensive audit logging",
                  "Rate limiting and abuse protection"
                ].map((item, index) => (
                  <li key={index} className="flex items-start">
                    <svg className="w-6 h-6 text-green-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <span className="ml-3 text-gray-700">{item}</span>
                  </li>
                ))}
              </ul>
            </div>
            <div className="relative">
              <div className="bg-white rounded-3xl shadow-xl p-8">
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                    <span className="text-sm font-medium text-gray-700">Authentication</span>
                    <span className="text-sm font-semibold text-green-600">Keycloak SSO</span>
                  </div>
                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                    <span className="text-sm font-medium text-gray-700">Token Security</span>
                    <span className="text-sm font-semibold text-green-600">JWE Encrypted</span>
                  </div>
                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                    <span className="text-sm font-medium text-gray-700">Backend</span>
                    <span className="text-sm font-semibold text-green-600">Spring Boot 4</span>
                  </div>
                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                    <span className="text-sm font-medium text-gray-700">Database</span>
                    <span className="text-sm font-semibold text-green-600">PostgreSQL 18</span>
                  </div>
                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                    <span className="text-sm font-medium text-gray-700">Billing</span>
                    <span className="text-sm font-semibold text-green-600">Stripe</span>
                  </div>
                </div>
              </div>
              <div className="absolute -top-4 -right-4 w-full h-full bg-gradient-to-br from-indigo-500 to-purple-600 rounded-3xl -z-10 opacity-20"></div>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900">
              Simple, Transparent Pricing
            </h2>
            <p className="mt-4 text-lg text-gray-600 max-w-2xl mx-auto">
              Choose the plan that fits your needs. All plans include core features.
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            {[
              {
                name: "Starter",
                price: "29",
                features: ["5 Projects", "10GB Storage", "Email Support", "Basic Analytics"]
              },
              {
                name: "Growth",
                price: "79",
                popular: true,
                features: ["25 Projects", "100GB Storage", "Priority Support", "Advanced Analytics", "Custom Domains"]
              },
              {
                name: "Enterprise",
                price: "199",
                features: ["Unlimited Projects", "1TB Storage", "24/7 Support", "Custom Integrations", "SLA Guarantee"]
              }
            ].map((plan, index) => (
              <div
                key={index}
                className={`relative p-8 rounded-2xl ${
                  plan.popular
                    ? "bg-gray-900 text-white ring-4 ring-indigo-500"
                    : "bg-gray-50 text-gray-900"
                }`}
              >
                {plan.popular && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1 bg-indigo-500 text-white text-sm font-semibold rounded-full">
                    Most Popular
                  </div>
                )}
                <h3 className="text-xl font-semibold mb-2">{plan.name}</h3>
                <div className="mb-6">
                  <span className="text-4xl font-bold">${plan.price}</span>
                  <span className={plan.popular ? "text-gray-300" : "text-gray-500"}>/month</span>
                </div>
                <ul className="space-y-3 mb-8">
                  {plan.features.map((feature, i) => (
                    <li key={i} className="flex items-center">
                      <svg className={`w-5 h-5 mr-3 ${plan.popular ? "text-indigo-400" : "text-green-500"}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      {feature}
                    </li>
                  ))}
                </ul>
                <Link
                  href="/auth/signup"
                  className={`block w-full py-3 px-6 text-center font-semibold rounded-xl transition-colors ${
                    plan.popular
                      ? "bg-white text-gray-900 hover:bg-gray-100"
                      : "bg-gray-900 text-white hover:bg-gray-800"
                  }`}
                >
                  Get Started
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-indigo-600 to-purple-600">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-6">
            Ready to Build Your SaaS?
          </h2>
          <p className="text-xl text-indigo-100 max-w-2xl mx-auto mb-8">
            Start building your enterprise application today with ArchCore&apos;s 
            complete SSO integration and security features.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              href="/auth/signup"
              className="px-8 py-4 text-lg font-semibold text-indigo-600 bg-white rounded-xl hover:bg-gray-50 transition-all shadow-lg"
            >
              Get Started Free
            </Link>
            <Link
              href="/#features"
              className="px-8 py-4 text-lg font-semibold text-white border-2 border-white/30 rounded-xl hover:bg-white/10 transition-all"
            >
              View Demo
            </Link>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
