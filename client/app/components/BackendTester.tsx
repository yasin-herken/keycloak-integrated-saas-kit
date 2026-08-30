"use client";

import { useState } from "react";

interface BackendTesterProps {
  accessToken?: string;
}

export function BackendTester({ accessToken }: BackendTesterProps) {
  const [response, setResponse] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>("");

  const testBackend = async () => {
    setLoading(true);
    setError("");
    setResponse("");

    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8081"}/api/v1/public/ping`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      const data = await res.json();
      setResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to connect to backend");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-xl p-8">
      <h2 className="text-xl font-bold text-gray-900 mb-4">
        Backend Connection Test
      </h2>
      <p className="text-gray-600 mb-6">
        Test the connection to your Spring Boot backend using the JWE token.
      </p>

      <button
        onClick={testBackend}
        disabled={loading || !accessToken}
        className="px-6 py-3 text-sm font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
      >
        {loading ? "Testing..." : "Test Backend Connection"}
      </button>

      {!accessToken && (
        <p className="mt-4 text-sm text-amber-600">
          No access token available. Sign in first.
        </p>
      )}

      {error && (
        <div className="mt-4 p-4 bg-red-50 rounded-lg">
          <p className="text-sm text-red-700 font-medium">Error:</p>
          <pre className="mt-2 text-sm text-red-600 whitespace-pre-wrap">{error}</pre>
        </div>
      )}

      {response && (
        <div className="mt-4 p-4 bg-green-50 rounded-lg">
          <p className="text-sm text-green-700 font-medium">Response:</p>
          <pre className="mt-2 text-sm text-green-600 whitespace-pre-wrap font-mono">{response}</pre>
        </div>
      )}
    </div>
  );
}
