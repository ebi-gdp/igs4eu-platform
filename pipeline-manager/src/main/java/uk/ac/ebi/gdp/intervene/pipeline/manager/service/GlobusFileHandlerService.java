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
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.DeleteDirResponseDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusUserIdentityDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirReqDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.GlobusDetailsDTO;

import java.net.URI;
import java.nio.file.Path;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Globus file handler service, define methods to interact with
 * File Handler service to access Globus APIs
 */
public class GlobusFileHandlerService {
    private final WebClient fileHandlerWebClient;
    private final URI globusUserURI;
    private final URI globusListDirFilesURI;
    private final URI globusCreateDirURI;
    private final URI globusDeleteDirURI;

    /**
     * Constructs a {@code GlobusFileHandlerService} instance.
     * This service provides operations for handling files with Globus, including user interactions,
     * listing directory files, and creating directories.
     *
     * @param fileHandlerWebClient the WebClient used for making HTTP requests to Globus services.
     * @param globusUserURI the URI for accessing Globus user-related operations.
     * @param globusListDirFilesURI the URI for listing files in a Globus directory.
     * @param globusCreateDirURI the URI for creating a directory in Globus.
     */
    public GlobusFileHandlerService(final WebClient fileHandlerWebClient,
                                    final URI globusUserURI,
                                    final URI globusListDirFilesURI,
                                    final URI globusCreateDirURI,
                                    final URI globusDeleteDirURI) {
        this.fileHandlerWebClient = fileHandlerWebClient;
        this.globusUserURI = globusUserURI;
        this.globusListDirFilesURI = globusListDirFilesURI;
        this.globusCreateDirURI = globusCreateDirURI;
        this.globusDeleteDirURI = globusDeleteDirURI;
    }

    /**
     * Retrieves Globus user details.
     *
     * @param username globus username e.g. email id
     *
     * @return Globus user details represented by {@link GlobusUserIdentityDetailsWrapperDTO}
     */
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

    /**
     * Lists files uploaded to specified Dir on Guest collection.
     *
     * @param dirPath files to list on the dir path
     *
     * @return List of files represented by {@link GlobusFileDetailsWrapperDTO}
     */
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

    /**
     * Creates directory on Guest collection.
     *
     * @param guestCollectionDirReqDTO request data.
     *
     * @return Dir details represented by {@link GlobusDetailsDTO}.
     */
    public Mono<GlobusDetailsDTO> createDirectoryOnGuestCollection(final GuestCollectionDirReqDTO guestCollectionDirReqDTO) {
        return fileHandlerWebClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(globusCreateDirURI.getPath())
                        .build())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(guestCollectionDirReqDTO)
                .retrieve()
                .bodyToMono(GlobusDetailsDTO.class);
    }

    /**
     * @param path directory path to be deleted on Globus guest collection.
     *
     * @return Directory deleted details represented {@link DeleteDirResponseDTO}.
     */
    public Mono<DeleteDirResponseDTO> deleteDirectoryOnGuestCollection(final String path) {
        return fileHandlerWebClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(globusDeleteDirURI.getPath())
                        .queryParam("path", path)
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DeleteDirResponseDTO.class);
    }
}
