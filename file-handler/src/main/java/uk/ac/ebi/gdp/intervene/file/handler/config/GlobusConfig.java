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
package uk.ac.ebi.gdp.intervene.file.handler.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.collection.FileOperationService;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.collection.IFileOperationService;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.AuthService;

import java.net.URI;
import java.nio.file.Paths;

@Configuration
public class GlobusConfig {

    @Bean
    public AuthService authService(@Qualifier("globusAuthWebClient") final WebClient webClient) {
        return new AuthService(
                webClient
        );
    }

    @Bean("globusAuthWebClient")
    public WebClient webClient(@Value("${globus.auth-api.url}") final String authAPIBaseURL,
                               @Value("${globus.auth-api.credentials}") final String credentials) {
        return WebClient.builder()
                .baseUrl(authAPIBaseURL)
                .defaultHeader("Authorization", "Basic " + credentials)
                .build();
    }


    @Bean
    public IFileOperationService fileOperationService(@Qualifier("globusWebClient") final WebClient webClient,
                                                      @Value("${globus.guest-collection.home-path}") final String guestCollectionHomePath,
                                                      @Value("${globus.endpoint.mkdir.uri}") final URI mkDirEndpointURI,
                                                      @Value("${globus.endpoint.access.uri}") final URI dirAccessURI,
                                                      @Value("${globus.endpoint.list-files.uri}") final URI listFilesURI) {
        return new FileOperationService(
                webClient,
                Paths.get(guestCollectionHomePath),
                mkDirEndpointURI,
                dirAccessURI,
                listFilesURI
        );
    }
}
