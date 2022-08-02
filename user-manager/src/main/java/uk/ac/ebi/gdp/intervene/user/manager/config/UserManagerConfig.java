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
package uk.ac.ebi.gdp.intervene.user.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.gdp.intervene.user.manager.handler.RestTemplateErrorResponseHandler;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.repository.UserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.UserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.service.IUserManagerService;
import uk.ac.ebi.gdp.intervene.user.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.AuthenticationContext;

import static java.time.Duration.ofSeconds;

@Configuration
public class UserManagerConfig {

    @Bean
    public IUserAccountPersistenceService userAccountPersistenceService(final UserAccountRepository userAccountRepository,
                                                                        final UserAccountDetailsRepository userAccountDetailsRepository,
                                                                        final AuthUserAccountRepository authUserAccountRepository,
                                                                        final AuthenticationContext authenticationContext) {
        return new UserAccountPersistenceService(
                userAccountRepository,
                userAccountDetailsRepository,
                authUserAccountRepository,
                authenticationContext
        );
    }

    @Bean
    public IUserManagerService userManagerService(final IUserAccountPersistenceService userAccountPersistenceService) {
        return new UserManagerService(userAccountPersistenceService);
    }

    @Bean
    public ResponseErrorHandler initErrorResponseHandler() {
        return new RestTemplateErrorResponseHandler();
    }

    @Bean("elixirRestTemplate")
    public RestTemplate initElixirRestTemplate(final RestTemplateBuilder restTemplateBuilder,
                                               final ResponseErrorHandler responseErrorHandler,
                                               @Value("${elixir.oidc.request.connection.timeout}") final long connectionTimeout,
                                               @Value("${elixir.oidc.request.read.timeout}") final long readTimeout) {
        return initRestTemplate(restTemplateBuilder,
                responseErrorHandler,
                connectionTimeout,
                readTimeout);
    }

    private RestTemplate initRestTemplate(final RestTemplateBuilder restTemplateBuilder,
                                          final ResponseErrorHandler responseErrorHandler,
                                          final long connectionTimeout,
                                          final long readTimeout) {
        return restTemplateBuilder
                .setConnectTimeout(ofSeconds(connectionTimeout))
                .setReadTimeout(ofSeconds(readTimeout))
                .errorHandler(responseErrorHandler)
                .requestFactory(() -> {
                    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    requestFactory.setOutputStreaming(false);
                    return requestFactory;
                })
                .build();
    }
}
