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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.entity;

public enum AuthProvider {
    ELIXIR("@elixir-europe.org");

    private final String authProvider;

    AuthProvider(final String authProvider) {
        this.authProvider = authProvider;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public static AuthProvider getAuthProviderByDescription(final String authProvider) {
        for (final AuthProvider provider : AuthProvider.values()) {
            if (provider.getAuthProvider().endsWith(authProvider)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No matching AuthProvider for value " + authProvider);
    }
}
