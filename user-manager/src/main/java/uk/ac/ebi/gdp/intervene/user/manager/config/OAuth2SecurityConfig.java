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
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.intervene.commons.security.GenericOAuth2SecurityConfig;
import uk.ac.ebi.gdp.intervene.user.manager.auth.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.ElixirAuthenticationService;

import static java.net.URI.create;

@Configuration
@EnableWebFluxSecurity
public class OAuth2SecurityConfig extends GenericOAuth2SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(final ServerHttpSecurity http,
                                                            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") final String jwkSetURI) {
        return securityFilterChain(http, jwkSetURI);
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

    @Bean("elixirWebClient")
    public WebClient elixirWebClient(@Value("${elixir.oidc.url}") final String elixirOidcUrl) {
        return webClient(elixirOidcUrl);
    }

    private WebClient webClient(final String baseURL) {
        return WebClient
                .builder()
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(new ServerBearerExchangeFilterFunction());
                    exchangeFilterFunctions.add(errorHandler());
                })
                .filter(new ServerBearerExchangeFilterFunction())
                .baseUrl(baseURL)
                .build();
    }
}
