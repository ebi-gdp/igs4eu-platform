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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusUserIdentityDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirReqDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.GlobusDetailsDTO;

import java.net.URI;
import java.nio.file.Path;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class GlobusFileHandlerService {
    private final WebClient fileHandlerWebClient;
    private final URI globusUserURI;
    private final URI globusListDirFilesURI;
    private final URI globusCreatDirURI;

    public GlobusFileHandlerService(final WebClient fileHandlerWebClient,
                                    final URI globusUserURI,
                                    final URI globusListDirFilesURI,
                                    final URI globusCreatDirURI) {
        this.fileHandlerWebClient = fileHandlerWebClient;
        this.globusUserURI = globusUserURI;
        this.globusListDirFilesURI = globusListDirFilesURI;
        this.globusCreatDirURI = globusCreatDirURI;
    }

    public Mono<GlobusUserIdentityDetailsWrapperDTO> getGlobusUserDetails(final String username) {
        return fileHandlerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(globusUserURI.getPath())
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(GlobusUserIdentityDetailsWrapperDTO.class);
    }

    public Mono<GlobusFileDetailsWrapperDTO> listFilesOnGuestCollection(final Path dirPath) {
        return fileHandlerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(globusListDirFilesURI.getPath())
                        .queryParam("path", dirPath.toString())
                        .build())
                .retrieve()
                .bodyToMono(GlobusFileDetailsWrapperDTO.class);
    }

    public Mono<GlobusDetailsDTO> createDirectoryOnGuestCollection(final GuestCollectionDirReqDTO guestCollectionDirReqDTO) {
        return fileHandlerWebClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(globusCreatDirURI.getPath())
                        .build())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(guestCollectionDirReqDTO)
                .retrieve()
                .bodyToMono(GlobusDetailsDTO.class);
    }
}
