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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import reactor.core.publisher.Mono;

import static org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext;
import static uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType.getAuthProviderByDescription;

public class SecurityContextDataProvider {
    public static Mono<AuthProviderType> getAuthProvider() {
        return getAuthentication()
                .map(authentication -> getAuthProviderByDescription(authentication.getName()));
    }

    public static Mono<String> currentUserId() {
        return jwt().map(JwtClaimAccessor::getSubject);
    }

    public static Mono<String> getAccessToken() {
        return jwt().map(AbstractOAuth2Token::getTokenValue);
    }

    public static Mono<Jwt> jwt() {
        return getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class);
    }

    public static Mono<Authentication> getAuthentication() {
        return getContext()
                .map(SecurityContext::getAuthentication);
    }
}
