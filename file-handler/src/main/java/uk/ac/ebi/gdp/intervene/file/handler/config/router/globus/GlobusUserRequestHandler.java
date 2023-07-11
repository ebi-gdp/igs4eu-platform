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

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.AuthService;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;

public class GlobusUserRequestHandler {
    private final AuthService authService;

    public GlobusUserRequestHandler(final AuthService authService) {
        this.authService = authService;
    }

    public Mono<ServerResponse> getUserIdDetails(final ServerRequest serverRequest) {
        return serverRequest
                .queryParam("username")
                .map(username -> authService
                        .getUserIdentityDetails(username)
                        .flatMap(globusUserIdentityDetailsWrapperDTO -> ok().bodyValue(globusUserIdentityDetailsWrapperDTO)))
                .orElse(error(badRequest("Query param 'username' is missing or empty!")));
    }
}
