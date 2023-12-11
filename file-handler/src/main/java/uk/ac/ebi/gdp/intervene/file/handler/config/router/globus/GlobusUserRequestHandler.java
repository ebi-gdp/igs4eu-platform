/*
 *
 * Copyright 2023 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.file.handler.config.router.globus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusUserIdentityDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.IAuthService;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;

/**
 * Globus user request handler. Defines handlers for router function.
 */
public class GlobusUserRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobusUserRequestHandler.class);
    private final IAuthService authService;

    public GlobusUserRequestHandler(final IAuthService authService) {
        this.authService = authService;
    }

    /**
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return user details represented by {@link GlobusUserIdentityDetailsWrapperDTO}
     */
    public Mono<ServerResponse> getUserIdDetails(final ServerRequest serverRequest) {
        LOGGER.info("Retrieving users details from Globus");
        return serverRequest
                .queryParam("username")
                .map(username -> {
                    LOGGER.info("Fetching Globus user details from the Globus API for {} ", username);
                    return authService
                            .getUserIdentityDetails(username)
                            .doOnNext(ignoreData -> LOGGER.info("Response received for Globus user details from the Globus API"))
                            .flatMap(globusUserIdentityDetailsWrapperDTO -> ok().bodyValue(globusUserIdentityDetailsWrapperDTO))
                            .doOnNext(serverResponse -> LOGGER.info("Successfully retrieved user details"));
                })
                .orElse(error(badRequest("Query param 'username' is missing or empty")));
    }
}
