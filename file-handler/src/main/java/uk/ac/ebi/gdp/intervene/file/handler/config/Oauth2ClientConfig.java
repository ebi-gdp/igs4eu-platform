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

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;
import uk.ac.ebi.gdp.intervene.commons.security.GenericOAuth2SecurityConfig;
import uk.ac.ebi.gdp.intervene.file.handler.config.oauth2.GlobusOAuth2BodyExtractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static org.springframework.security.oauth2.client.OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME;
import static org.springframework.security.oauth2.client.OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME;
import static reactor.core.publisher.Mono.just;
import static uk.ac.ebi.gdp.intervene.commons.utility.WebClientUtil.errorHandler;
import static uk.ac.ebi.gdp.intervene.commons.utility.WebClientUtil.jsonExchangeStrategies;

/**
 * Common/generic OAuth2 client config
 * extends {@link GenericOAuth2SecurityConfig} to supply arguments Or override logic.
 *
 * @see ReactiveClientRegistrationRepository
 * @see AuthorizationGrantType
 * @see ReactiveOAuth2AuthorizedClientManager
 * @see ReactiveOAuth2AuthorizedClientService
 * @see AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
 */
class Oauth2ClientConfig extends GenericOAuth2SecurityConfig {
    protected ReactiveClientRegistrationRepository getRegistration(final String tokenUri,
                                                                   final String clientId,
                                                                   final String clientSecret,
                                                                   final List<String> scopes,
                                                                   final AuthorizationGrantType authorizationGrantType,
                                                                   final String registrationId) {
        final ClientRegistration registration = ClientRegistration
                .withRegistrationId(registrationId)
                .tokenUri(tokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(authorizationGrantType)
                .scope(scopes)
                .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    /**
     * @see ReactiveOAuth2AuthorizedClientProvider
     * @see ReactiveOAuth2AuthorizedClientProviderBuilder
     */
    protected ReactiveOAuth2AuthorizedClientManager reactiveO2ACMPassword(final ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                                          final ReactiveOAuth2AuthorizedClientService authorizedClientService,
                                                                          final String username,
                                                                          final String password) {
        final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .password()
                        .build();

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                reactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        authorizedClientManager.setContextAttributesMapper(contextAttributesMapper(username, password));
        return authorizedClientManager;
    }

    /**
     * @see WebClientReactiveClientCredentialsTokenResponseClient
     */
    protected ReactiveOAuth2AuthorizedClientManager reactiveO2ACMClientCredentials(final ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                                                   final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        final WebClientReactiveClientCredentialsTokenResponseClient reactiveResponseClient = new WebClientReactiveClientCredentialsTokenResponseClient();
        reactiveResponseClient.setBodyExtractor(GlobusOAuth2BodyExtractors.oauth2AccessTokenResponse());

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                reactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(clientCredentialsGrantBuilder -> clientCredentialsGrantBuilder.accessTokenResponseClient(reactiveResponseClient))
                .build());
        return authorizedClientManager;
    }

    /**
     * @see InMemoryReactiveOAuth2AuthorizedClientService
     */
    protected ReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientServiceInMemory(final ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * @see HttpClient
     * @see ServerOAuth2AuthorizedClientExchangeFilterFunction
     * @see WebClient
     */
    protected WebClient webClient(final ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
                                  final WebClient.Builder builder,
                                  final WebClientProperties webClientProperties,
                                  final String baseURL,
                                  final String registrationId) {
        final HttpClient httpClient = HttpClient.create()
                .option(CONNECT_TIMEOUT_MILLIS, webClientProperties.getConnectionTimeout() * 1000)
                .doOnConnected(connection -> connection
                        .addHandlerFirst(new ReadTimeoutHandler(webClientProperties.getReadWriteTimeout()))
                        .addHandlerFirst(new WriteTimeoutHandler(webClientProperties.getReadWriteTimeout())));

        final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        oauth.setDefaultClientRegistrationId(registrationId);
        return builder
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(oauth);
                    exchangeFilterFunctions.add(errorHandler());
                })
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(jsonExchangeStrategies())
                .baseUrl(baseURL)
                .build();
    }

    private AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(final ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                                                                               final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        return new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
    }

    /**
     * @see OAuth2AuthorizeRequest
     */
    private Function<OAuth2AuthorizeRequest, Mono<Map<String, Object>>> contextAttributesMapper(final String username,
                                                                                                final String password) {
        return authorizeRequest -> {
            final Map<String, Object> contextAttributes = new HashMap<>();
            contextAttributes.put(USERNAME_ATTRIBUTE_NAME, username);
            contextAttributes.put(PASSWORD_ATTRIBUTE_NAME, password);
            return just(contextAttributes);
        };
    }
}
