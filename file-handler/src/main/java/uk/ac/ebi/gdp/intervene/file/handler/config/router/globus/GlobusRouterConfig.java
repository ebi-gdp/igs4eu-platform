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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.commons.dpa.DPAConsentCheck;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.collection.IFileOperationService;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.AuthService;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static uk.ac.ebi.gdp.intervene.commons.log.LogUtil.logRequestIdHeader;

/**
 * Globus router config.
 */
@Configuration
public class GlobusRouterConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobusRouterConfig.class);

    @Bean
    public RouterFunction<ServerResponse> globusRoutes(final GlobusRequestHandler globusRequestHandler,
                                                       final GlobusUserRequestHandler globusUserRequestHandler,
                                                       final DPAConsentCheck dpaConsentCheck) {
        return route()
                .filter(logRequestIdHeader(LOGGER))
                .filter(dpaConsentCheck.hasUserGivenConsent())
                .path("/globus", gb -> gb
                        .path("/guest-collection", gcb -> gcb
                                .POST(globusRequestHandler::createDirectoryOnGuestCollection)
                                .GET(globusRequestHandler::listFilesOnGuestCollectionDirectory))
                        .path("/user", gub -> gub
                                .GET(globusUserRequestHandler::getUserIdDetails)))
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
