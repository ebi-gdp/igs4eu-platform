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

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static org.springframework.security.oauth2.client.OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME;
import static org.springframework.security.oauth2.client.OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.PASSWORD;

@Configuration
public class Oauth2ClientConfig {

    private static final String EGA_RESOURCE_ID = "EGA_RESOURCE_ID";

    @ConfigurationProperties(prefix = "webclient.connection")
    @Bean
    public WebClientProperties webClientProperties() {
        return new WebClientProperties();
    }

    @Bean
    public ReactiveClientRegistrationRepository getRegistration(
            @Value("${ega.aai.access-token.uri}") final String tokenUri,
            @Value("${ega.aai.client-id}") final String clientId,
            @Value("${ega.aai.client-secret}") final String clientSecret,
            @Value("${ega.aai.scopes}") final String scope) {
        final ClientRegistration registration = ClientRegistration
                .withRegistrationId(EGA_RESOURCE_ID)
                .tokenUri(tokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(PASSWORD)
                .scope(scope)
                .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            final ReactiveClientRegistrationRepository clientRegistrationRepository,
            final ReactiveOAuth2AuthorizedClientService authorizedClientService,
            @Value("${ega.aai.username}") final String username,
            @Value("${ega.aai.password}") final String password) {

        final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .password()
                        .build();

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        authorizedClientManager.setContextAttributesMapper(contextAttributesMapper(username, password));

        return authorizedClientManager;
    }

    private Function<OAuth2AuthorizeRequest, Mono<Map<String, Object>>> contextAttributesMapper(final String username,
                                                                                                final String password) {
        return authorizeRequest -> {
            final Map<String, Object> contextAttributes = new HashMap<>();
            contextAttributes.put(USERNAME_ATTRIBUTE_NAME, username);
            contextAttributes.put(PASSWORD_ATTRIBUTE_NAME, password);
            return Mono.just(contextAttributes);
        };
    }

    @Bean
    public WebClient webClient(final ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
                               final WebClientProperties webClientProperties,
                               @Value("${ega.data-api.url}") final String baseURL) {
        final HttpClient httpClient = HttpClient.create()
                .option(CONNECT_TIMEOUT_MILLIS, webClientProperties.getConnectionTimeout() * 1000)
                .doOnConnected(connection -> connection
                        .addHandler(new ReadTimeoutHandler(webClientProperties.getReadWriteTimeout()))
                        .addHandlerLast(new WriteTimeoutHandler(webClientProperties.getReadWriteTimeout())));

        final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId(EGA_RESOURCE_ID);
        return WebClient.builder()
                .filter(oauth)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseURL)
                .build();
    }

    @Bean
    public InMemoryReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientService(final ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
}
