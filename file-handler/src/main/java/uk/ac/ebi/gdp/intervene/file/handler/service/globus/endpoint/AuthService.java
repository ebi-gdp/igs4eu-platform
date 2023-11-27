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
package uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusUserIdentityDetailsWrapperDTO;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Auth service to get user identity details from Globus.
 *
 * @see WebClient
 */
public class AuthService implements IAuthService {
    private final WebClient webClient;

    public AuthService(final WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * {@inheritDoc}
     *
     * retrieves user's Globus identity details
     */
    public Mono<GlobusUserIdentityDetailsWrapperDTO> getUserIdentityDetails(final String username) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/identities")
                        .queryParam("usernames", username)
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GlobusUserIdentityDetailsWrapperDTO.class);
    }
}
