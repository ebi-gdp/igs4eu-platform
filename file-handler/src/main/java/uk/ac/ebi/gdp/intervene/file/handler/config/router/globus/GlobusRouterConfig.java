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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.collection.IFileOperationService;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.AuthService;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class GlobusRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> globusRoutes(final GlobusRequestHandler globusRequestHandler) {
        final String guestCollectionURI = "/globus/guest-collection";
        return route()
                .POST(guestCollectionURI, accept(APPLICATION_JSON), globusRequestHandler::createDirectoryOnGuestCollection)
                .GET(guestCollectionURI, globusRequestHandler::listFilesOnGuestCollectionDirectory)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> globusUserRoutes(final GlobusUserRequestHandler globusUserRequestHandler) {
        final String guestCollectionURI = "/globus/user";
        return route()
                .GET(guestCollectionURI, globusUserRequestHandler::getUserIdDetails)
                .build();
    }

    @Bean
    public GlobusRequestHandler globusRequestHandler(final IFileOperationService fileOperationService,
                                                     @Value("${globus.guest-collection.endpoint-id}") final String guestCollectionId) {
        return new GlobusRequestHandler(fileOperationService, guestCollectionId);
    }

    @Bean
    public GlobusUserRequestHandler globusUserRequestHandler(final AuthService authService) {
        return new GlobusUserRequestHandler(authService);
    }
}
