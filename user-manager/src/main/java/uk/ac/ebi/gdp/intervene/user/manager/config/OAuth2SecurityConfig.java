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
package uk.ac.ebi.gdp.intervene.user.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.intervene.commons.security.GenericOAuth2SecurityConfig;
import uk.ac.ebi.gdp.intervene.user.manager.auth.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.ElixirAuthenticationService;

import static java.net.URI.create;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.web.reactive.function.client.WebClient.builder;

/**
 * OAuth2 security config, extends {@link GenericOAuth2SecurityConfig}.
 *
 * @see SecurityWebFilterChain
 * @see ReactiveUserDetailsService
 * @see ElixirAuthenticationService
 * @see AuthenticationContext
 * @see WebClient
 */
@Configuration
@EnableWebFluxSecurity
public class OAuth2SecurityConfig extends GenericOAuth2SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(final ServerHttpSecurity http,
                                                            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") final String jwkSetURI) {
        return securityFilterChain(http, jwkSetURI);
    }

    @Order(HIGHEST_PRECEDENCE)
    @Bean
    public SecurityWebFilterChain securityFilterChainBasicAuth(final ServerHttpSecurity http) {
        return super.securityFilterChainBasicAuth(http, "/user/account/{accountId}");
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(@Value("${basic.auth.username}") final String username,
                                                         @Value("${basic.auth.password}") final String password) {
        return super.userDetailsService(username, password);
    }

    @Bean
    public ElixirAuthenticationService elixirAuthenticationService(final WebClient webClient,
                                                                   @Value("${oidc.user-info.uri}") final String userInfoURI) {
        return new ElixirAuthenticationService(
                webClient,
                create(userInfoURI));
    }

    @Bean
    public AuthenticationContext authenticationContext(final ElixirAuthenticationService elixirAuthenticationService) {
        return new AuthenticationContext(
                elixirAuthenticationService
        );
    }

    @Bean
    public WebClient elixirWebClient(@Value("${elixir.oidc.url}") final String elixirOidcUrl) {
        return webClient(elixirOidcUrl);
    }

    private WebClient webClient(final String baseURL) {
        return builder()
                .baseUrl(baseURL)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(new ServerBearerExchangeFilterFunction());
                    exchangeFilterFunctions.add(errorHandler());
                })
                .filter(new ServerBearerExchangeFilterFunction())
                .build();
    }
}
