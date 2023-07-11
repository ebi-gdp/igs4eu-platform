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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;

import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.PASSWORD;

@Configuration
public class EgaOauth2ClientConfig extends Oauth2ClientConfig {

    private static final String EGA_RESOURCE_ID = "EGA_RESOURCE_ID";

    @ConfigurationProperties(prefix = "webclient.connection")
    @Bean
    public WebClientProperties webClientProperties() {
        return new WebClientProperties();
    }

    @Primary
    @Bean("egaReactiveClientRegistrationRepository")
    public ReactiveClientRegistrationRepository getRegistration(
            @Value("${ega.aai.access-token.uri}") final String tokenUri,
            @Value("${ega.aai.client-id}") final String clientId,
            @Value("${ega.aai.client-secret}") final String clientSecret,
            @Value("${ega.aai.scopes}") final String scope) {
        return getRegistration(
                tokenUri,
                clientId,
                clientSecret,
                List.of(scope),
                PASSWORD,
                EGA_RESOURCE_ID
        );
    }

    @Primary
    @Bean("egaReactiveOAuth2AuthorizedClientService")
    public ReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientService(@Qualifier("egaReactiveClientRegistrationRepository") final ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return reactiveOAuth2AuthorizedClientServiceInMemory(clientRegistrationRepository);
    }

    @Primary
    @Bean("egaReactiveOAuth2AuthorizedClientManager")
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            @Qualifier("egaReactiveClientRegistrationRepository") final ReactiveClientRegistrationRepository clientRegistrationRepository,
            @Qualifier("egaReactiveOAuth2AuthorizedClientService") final ReactiveOAuth2AuthorizedClientService authorizedClientService,
            @Value("${ega.aai.username}") final String username,
            @Value("${ega.aai.password}") final String password) {
        return reactiveO2ACMPassword(
                clientRegistrationRepository,
                authorizedClientService,
                username,
                password
        );
    }

    @Bean("egaWebClient")
    public WebClient egaWebClient(@Qualifier("egaReactiveOAuth2AuthorizedClientManager") final ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
                                  final WebClientProperties webClientProperties,
                                  @Value("${ega.data-api.url}") final String baseURL) {

        return webClient(
                authorizedClientManager,
                webClientProperties,
                baseURL,
                EGA_RESOURCE_ID
        );
    }
}
