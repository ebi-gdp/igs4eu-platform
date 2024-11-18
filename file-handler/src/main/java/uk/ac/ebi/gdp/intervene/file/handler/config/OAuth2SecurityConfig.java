/*
 *
 * Copyright 2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.file.handler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import uk.ac.ebi.gdp.intervene.commons.security.GenericOAuth2SecurityConfig;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;

/**
 * OAuth2 security config, extends {@link GenericOAuth2SecurityConfig}
 *
 * @see SecurityWebFilterChain
 */
@Configuration
@EnableWebFluxSecurity
public class OAuth2SecurityConfig extends GenericOAuth2SecurityConfig {
    public static final String URI_INCLUDE_UNDER_BASIC_AUTH = "/demon/globus/guest-collection";

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(final ServerHttpSecurity http,
                                                            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") final String jwkSetURI) {
        return securityFilterChain(http, jwkSetURI);
    }

    /**
     * API call that needs to be authenticated by basic auth should be included here.
     * Watch for patternPath value, this should match with router function defined in {@link FileHandlerConfig}
     */
    @Order(HIGHEST_PRECEDENCE)
    @Bean
    public SecurityWebFilterChain securityFilterChainBasicAuth(final ServerHttpSecurity http) {
        return super.securityFilterChainBasicAuth(http, URI_INCLUDE_UNDER_BASIC_AUTH, DELETE);
    }

    /**
     * Provides credentials for basic auth.
     *
     * @param username basic auth username
     * @param password basic auth password
     *
     * @return {@link ReactiveUserDetailsService}
     */
    @Bean
    public ReactiveUserDetailsService userDetailsService(@Value("${basic.auth.username}") final String username,
                                                         @Value("${basic.auth.password}") final String password) {
        return super.userDetailsService(username, password);
    }
}
