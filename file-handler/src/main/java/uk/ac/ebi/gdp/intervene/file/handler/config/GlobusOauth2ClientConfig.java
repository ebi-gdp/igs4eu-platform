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
package uk.ac.ebi.gdp.intervene.file.handler.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;

import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

@Primary
@Configuration
public class GlobusOauth2ClientConfig extends Oauth2ClientConfig {

    private static final String GLOBUS_RESOURCE_ID = "GLOBUS_RESOURCE_ID";

    @Bean("globusReactiveClientRegistrationRepository")
    public ReactiveClientRegistrationRepository getRegistration(
            @Value("${globus.aai.access-token.uri}") final String tokenUri,
            @Value("${globus.aai.client-id}") final String clientId,
            @Value("${globus.aai.client-secret}") final String clientSecret,
            @Value("#{'${globus.aai.scopes}'.split(',')}") final List<String> scopes) {
        return getRegistration(
                tokenUri,
                clientId,
                clientSecret,
                scopes,
                CLIENT_CREDENTIALS,
                GLOBUS_RESOURCE_ID
        );
    }

    @Bean("globusReactiveOAuth2AuthorizedClientService")
    public ReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientService(@Qualifier("globusReactiveClientRegistrationRepository") final ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return reactiveOAuth2AuthorizedClientServiceInMemory(clientRegistrationRepository);
    }

    @Bean("globusReactiveOAuth2AuthorizedClientManager")
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            @Qualifier("globusReactiveClientRegistrationRepository") final ReactiveClientRegistrationRepository clientRegistrationRepository,
            @Qualifier("globusReactiveOAuth2AuthorizedClientService") final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        return reactiveO2ACMClientCredentials(
                clientRegistrationRepository,
                authorizedClientService
        );
    }

    @Bean("globusWebClient")
    public WebClient globusWebClient(@Qualifier("globusReactiveOAuth2AuthorizedClientManager") final ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
                                     final WebClientProperties webClientProperties,
                                     @Value("${globus.data-api.url}") final String baseURL) {
        return webClient(
                authorizedClientManager,
                webClientProperties,
                baseURL,
                GLOBUS_RESOURCE_ID
        );
    }
}
