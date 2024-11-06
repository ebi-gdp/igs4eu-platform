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
package uk.ac.ebi.gdp.intervene.file.handler.service.globus.collection;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.DeleteDirResponseDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.file.handler.dto.globus.DeleteDirRequestDTO;
import uk.ac.ebi.gdp.intervene.file.handler.dto.globus.SubmissionIdDTO;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PermissionType;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.ac.ebi.gdp.intervene.file.handler.dto.globus.EndpointDTO.Access;
import static uk.ac.ebi.gdp.intervene.file.handler.dto.globus.EndpointDTO.Mkdir;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.DataType.ACCESS;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.DataType.DELETE;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.DataType.MKDIR;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PrincipalType.IDENTITY;

/**
 * Implements {@link IFileOperationService}, provides implementation to access/execute Globus APIs.
 */
public class FileOperationService implements IFileOperationService {
    private final WebClient webClient;
    private final String collectionEndpointId;
    private final Path guestCollectionHomePath;
    private final URI mkDirEndpointURI;
    private final URI dirAccessURI;
    private final URI listFilesURI;
    private final URI submissionIdURI;
    private final URI deleteDirURI;

    public FileOperationService(final WebClient webClient,
                                final String collectionEndpointId,
                                final Path guestCollectionHomePath,
                                final URI mkDirEndpointURI,
                                final URI dirAccessURI,
                                final URI listFilesURI,
                                final URI submissionIdURI,
                                final URI deleteDirURI) {
        this.webClient = webClient;
        this.collectionEndpointId = collectionEndpointId;
        this.guestCollectionHomePath = guestCollectionHomePath;
        this.mkDirEndpointURI = mkDirEndpointURI;
        this.dirAccessURI = dirAccessURI;
        this.listFilesURI = listFilesURI;
        this.submissionIdURI = submissionIdURI;
        this.deleteDirURI = deleteDirURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> createDirectory(final Path dirPath) {
        return webClient
                .post()
                .uri(mkDirEndpointURI.getPath())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(new Mkdir(MKDIR, guestCollectionHomePath.resolve(dirPath).toString()))
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<GlobusFileDetailsWrapperDTO> listFiles(final Path dirPath) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(listFilesURI.getPath())
                        .queryParam("path", guestCollectionHomePath.resolve(dirPath).toString())
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GlobusFileDetailsWrapperDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> grantDirectoryPermission(final String principal,
                                                 final PermissionType permissionType,
                                                 final String notifyEmail,
                                                 final Path dirPath) {
        return webClient
                .post()
                .uri(dirAccessURI.getPath())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(buildAccessRequestBody(
                        principal,
                        permissionType,
                        notifyEmail,
                        dirPath))
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DeleteDirResponseDTO> deleteDirectoryOnGuestCollection(final String submissionId,
                                                                       final Path dirPathToDelete) {
        return webClient
                .post()
                .uri(deleteDirURI.getPath())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(buildDeleteDirRequestBody(
                        submissionId,
                        collectionEndpointId,
                        dirPathToDelete.toString()
                ))
                .retrieve()
                .bodyToMono(DeleteDirResponseDTO.class);
    }

    private Access buildAccessRequestBody(final String principal,
                                          final PermissionType permissionType,
                                          final String notifyEmail,
                                          final Path dirPath) {
        return new Access(
                ACCESS,
                IDENTITY,
                principal,
                "/" + dirPath + "/",//TODO: revisit logic
                permissionType,
                notifyEmail
        );
    }

    private DeleteDirRequestDTO buildDeleteDirRequestBody(final String submissionId,
                                                          final String collectionEndpointId,
                                                          final String dirPathToDelete) {
        final String completeDirPathToDelete = Paths.get(guestCollectionHomePath.toString(),
                dirPathToDelete).toString();
        return new DeleteDirRequestDTO(
                DELETE.getDataType(),
                submissionId,
                true,
                collectionEndpointId,
                List.of(new DeleteDirRequestDTO.Data(completeDirPathToDelete))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<SubmissionIdDTO> getSubmissionId() {
        return webClient
                .get()
                .uri(submissionIdURI.getPath())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SubmissionIdDTO.class);
    }
}
