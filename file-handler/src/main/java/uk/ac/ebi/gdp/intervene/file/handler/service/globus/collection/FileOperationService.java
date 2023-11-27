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
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PermissionType;

import java.net.URI;
import java.nio.file.Path;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.ac.ebi.gdp.intervene.file.handler.dto.globus.EndpointDTO.Access;
import static uk.ac.ebi.gdp.intervene.file.handler.dto.globus.EndpointDTO.Mkdir;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.DataType.ACCESS;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.DataType.MKDIR;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PrincipalType.IDENTITY;

/**
 * Implements {@link IFileOperationService}, provides implementation to access/execute Globus APIs.
 */
public class FileOperationService implements IFileOperationService {
    private final WebClient webClient;
    private final Path guestCollectionHomePath;
    private final URI mkDirEndpointURI;
    private final URI dirAccessURI;
    private final URI listFilesURI;

    public FileOperationService(final WebClient webClient,
                                final Path guestCollectionHomePath,
                                final URI mkDirEndpointURI,
                                final URI dirAccessURI,
                                final URI listFilesURI) {
        this.webClient = webClient;
        this.guestCollectionHomePath = guestCollectionHomePath;
        this.mkDirEndpointURI = mkDirEndpointURI;
        this.dirAccessURI = dirAccessURI;
        this.listFilesURI = listFilesURI;
    }

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
}
