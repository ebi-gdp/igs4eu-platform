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
package uk.ac.ebi.gdp.intervene.key.handler.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.commons.dpa.DPAConsentCheck;

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static uk.ac.ebi.gdp.intervene.commons.log.LogUtil.buildUniqueRequestId;
import static uk.ac.ebi.gdp.intervene.commons.log.LogUtil.logRequestIdHeader;

/**
 * Configuration class for setting up RouterFunction beans to handle web requests.
 */
@Configuration
public class KeyHandlerRouterConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyHandlerRouterConfig.class);
    private static final Path keys = get("/key");
    private static final String KEYS_URI = "{keyId}/version/{versionId}";

    /**
     * Defines a RouterFunction bean that routes HTTP requests.
     *
     * @param keyHandler the handler function that processes requests.
     * @param dpaConsentCheck service to check DPA.
     *
     * @return a RouterFunction that routes requests to the appropriate handler.
     */
    @Bean
    public RouterFunction<ServerResponse> keyHandlerRoutes(final KeyRequestHandler keyHandler,
                                                           final DPAConsentCheck dpaConsentCheck) {
        return route()
                .filter(logRequestIdHeader(LOGGER))
                .filter(dpaConsentCheck.hasUserGivenConsent())
                .POST(keys.toString(), serverRequest -> keyHandler.generateKeys())
                .DELETE(keys.toString(), keyHandler::deleteSecret)
                .build();
    }

    /**
     * Defines a RouterFunction to access secret keys.
     *
     * @param keyHandler {@link KeyRequestHandler}
     *
     * @return a RouterFunction that routes requests to the appropriate handler.
     */
    @Bean
    public RouterFunction<ServerResponse> pipelineRoutesBasicAuth(final KeyRequestHandler keyHandler) {
        return route()
                .filter(buildUniqueRequestId(LOGGER))
                //Make sure this path is secured under basic auth
                .GET(keys.resolve(KEYS_URI).toString(), keyHandler::retrievePrivateKey)
                .build();
    }
}
