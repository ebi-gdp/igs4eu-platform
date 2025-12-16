/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.gdp.intervene.commons.security;

import java.net.URI;

public enum AuthProviderType {
    ELIXIR("@lifescience-ri.eu"),

    KEYCLOAK(null);

    private final String authProvider;

    AuthProviderType(final String authProvider) {
        this.authProvider = authProvider;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public static AuthProviderType getAuthProviderByDescription(final String authProvider) {
        for (final AuthProviderType provider : AuthProviderType.values()) {
            if (authProvider.endsWith(provider.getAuthProvider())) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No matching AuthProvider for value " + authProvider);
    }

    public static AuthProviderType getAuthProviderByIssuer(final String issuer) {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("No matching AuthProvider for empty issuer");
        }

        final String normalized = normalizeIssuer(issuer);
        if (normalized.contains("/realms/")) {
            return KEYCLOAK;
        }
        if (normalized.contains("lifescience")) {
            return ELIXIR;
        }

        throw new IllegalArgumentException("No matching AuthProvider for issuer " + issuer);
    }

    private static String normalizeIssuer(final String issuer) {
        // Normalize so "https://host/realms/x" and "https://host/realms/x/" match consistently.
        try {
            final URI uri = URI.create(issuer.trim());
            final String normalizedPath = (uri.getPath() == null) ? "" : uri.getPath().replaceAll("/+$", "");
            return new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    normalizedPath,
                    null,
                    null
            ).toString();
        } catch (final Exception ex) {
            // Fall back to a trimmed string if it's not a clean URI for any reason.
            return issuer.trim().replaceAll("/+$", "");
        }
    }
}
