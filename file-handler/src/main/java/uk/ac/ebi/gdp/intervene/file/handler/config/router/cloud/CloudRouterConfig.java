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
package uk.ac.ebi.gdp.intervene.file.handler.config.router.cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.commons.dpa.DPAConsentCheck;
import uk.ac.ebi.gdp.intervene.file.handler.cloud.ICloudStorage;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Cloud router config.
 */
@Configuration
public class CloudRouterConfig {
    /**
     * @param dpaConsentCheck DPA consent check implementation
     *
     * @return router function
     */
    @Bean
    public RouterFunction<ServerResponse> cloudRoutes(final DPAConsentCheck dpaConsentCheck,
                                                      final CloudRequestHandler cloudRequestHandler) {
        return route()
                .filter(dpaConsentCheck.hasUserGivenConsent())
                .path("/cloud", gb -> gb
                        .path("/storage", gcb -> gcb
                                .GET("/files", cloudRequestHandler::listFiles)
                                .GET("/stream-file", cloudRequestHandler::streamFileFromBucket)))
                .build();
    }

    /**
     * @param cloudStorage cloud service.
     *
     * @return {@link CloudRequestHandler}
     */
    @Bean
    public CloudRequestHandler cloudRequestHandler(final ICloudStorage cloudStorage) {
        return new CloudRequestHandler(cloudStorage);
    }
}
