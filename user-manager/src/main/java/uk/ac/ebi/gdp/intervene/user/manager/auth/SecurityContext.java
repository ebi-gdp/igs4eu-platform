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
package uk.ac.ebi.gdp.intervene.user.manager.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.AuthProvider;

public class SecurityContext {

    public AuthProvider getAuthProvider() {
        final Authentication authentication = getAuthentication();
        return AuthProvider.getAuthProviderByDescription(authentication.getName());
    }

    public String getAccessToken() {
        return getJwtCredentials().getTokenValue();
    }

    public String getSubject() {
        return getJwtCredentials().getSubject();
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Jwt getJwtCredentials() {
        return (Jwt) getAuthentication().getCredentials();
    }
}
