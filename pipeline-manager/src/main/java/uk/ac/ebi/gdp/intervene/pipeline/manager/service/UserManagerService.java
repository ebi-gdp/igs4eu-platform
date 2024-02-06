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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.usermanager.UserAccountDTO;

import java.net.URI;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * User manger service, defines methods to interact with User Manager module.
 */
public class UserManagerService {
    private final WebClient userManagerWebClient;
    private final String basicAuth;
    private final URI userAccountURI;

    public UserManagerService(final WebClient userManagerWebClient,
                              final String basicAuth,
                              final URI userAccountURI) {
        this.userManagerWebClient = userManagerWebClient;
        this.basicAuth = basicAuth;
        this.userAccountURI = userAccountURI;
    }

    /**
     * Returns user account details based on access token.
     *
     * @return User account details represented by {@link UserAccountDTO}
     */
    public Mono<UserAccountDTO> getUserAccountDetails() {
        return userManagerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(userAccountURI.getPath())
                        .build())
                .retrieve()
                .bodyToMono(UserAccountDTO.class);
    }

    /**
     * Returns user account details for specified accountId, secured by basic auth.
     *
     * @param accountId user account id
     *
     * @return User account details represented by {@link UserAccountDTO}
     */
    public Mono<UserAccountDTO> getUserAccountDetails(final String accountId) {
        return userManagerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(userAccountURI.getPath() + "/{accountId}")
                        .build(accountId))
                .header(AUTHORIZATION, basicAuth)
                .retrieve()
                .bodyToMono(UserAccountDTO.class);
    }
}
