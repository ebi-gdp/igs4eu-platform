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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PGSTraitWrapperDTO;

import java.net.URI;

/**
 * PGS Catalog service, defines method to interact PGS catalog APIs.
 */
public class PGSCatalogService {
    private final WebClient webClient;
    private final URI pgsTraitSearchURI;

    /**
     * Constructs a {@code PGSCatalogService} instance.
     * This service provides operations for interacting with the PGS (Polygenic Score) Catalog,
     * including searching for traits.
     *
     * @param webClient the WebClient used for making HTTP requests to the PGS Catalog service.
     * @param pgsTraitSearchURI the URI for accessing the trait search functionality in the PGS Catalog.
     */
    public PGSCatalogService(final WebClient webClient,
                             final URI pgsTraitSearchURI) {
        this.webClient = webClient;
        this.pgsTraitSearchURI = pgsTraitSearchURI;
    }

    /**
     * Retrieves PGS Ids according to search traits.
     *
     * @param searchTerm string to search for
     *
     * @return Search result represented by {@link PGSTraitWrapperDTO}
     */
    public Mono<PGSTraitWrapperDTO> searchPGSIdsByTraits(final String searchTerm) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(pgsTraitSearchURI.getPath())
                        .queryParam("term", searchTerm)
                        .queryParam("exact", 0)
                        .queryParam("include_children=", 0)
                        .build())
                .retrieve()
                .bodyToMono(PGSTraitWrapperDTO.class);
    }
}
