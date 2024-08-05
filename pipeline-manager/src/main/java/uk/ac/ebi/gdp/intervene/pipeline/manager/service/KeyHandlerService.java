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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.keyhandler.DatasetCryptographyDetailsDTO;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Service class for handling key management operations by interacting with an external key handler service.
 * This class uses a {@link WebClient} to communicate with the external service specified by the URI.
 */
public class KeyHandlerService {
    private final WebClient keyHandlerServiceWebClient;
    private final URI keyHandlerURI;

    /**
     * Constructs a {@code KeyHandlerService} instance.
     *
     * @param keyHandlerServiceWebClient the {@link WebClient} instance to use for making HTTP requests to the key handler service.
     * @param keyHandlerURI the {@link URI} of the external key handler service.
     */
    public KeyHandlerService(final WebClient keyHandlerServiceWebClient,
                             final URI keyHandlerURI) {
        this.keyHandlerServiceWebClient = keyHandlerServiceWebClient;
        this.keyHandlerURI = keyHandlerURI;
    }

    /**
     * Generates a new pair of cryptographic keys.
     * This method initiates the key generation process and returns the details of the generated public key.
     *
     * @return a {@link Mono} that emits the details of the generated public key encapsulated in a {@link DatasetCryptographyDetailsDTO} object.
     */
    public Mono<DatasetCryptographyDetailsDTO> generateKeys() {
        return keyHandlerServiceWebClient
                .post()
                .uri(keyHandlerURI.getPath())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DatasetCryptographyDetailsDTO.class);
    }
}
