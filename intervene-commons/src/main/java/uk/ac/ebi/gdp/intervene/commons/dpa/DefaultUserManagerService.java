/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.commons.dpa;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.usermanager.UserAccountDTO;

import java.net.URI;

/**
 * Default User manager service implementation. Interacts with user manager service module.
 */
public class DefaultUserManagerService implements IUserManagerService {
    protected final WebClient userManagerWebClient;
    protected final URI userAccountURI;

    public DefaultUserManagerService(final WebClient userManagerWebClient,
                                     final URI userAccountURI) {
        this.userManagerWebClient = userManagerWebClient;
        this.userAccountURI = userAccountURI;
    }

    /**
     * Returns user account details based on access token.
     *
     * @return User account details represented by {@link UserAccountDTO}
     */
    @Override
    public Mono<UserAccountDTO> getUserAccountDetails() {
        return userManagerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(userAccountURI.getPath())
                        .build())
                .retrieve()
                .bodyToMono(UserAccountDTO.class);
    }
}
