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

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirReqDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirResDTO;
import uk.ac.ebi.gdp.intervene.commons.exception.ClientException;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.collection.IFileOperationService;

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;
import static uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PermissionType.READ_WRITE;

/**
 * Globus request handler. Defines handlers for router function.
 */
public class GlobusRequestHandler {
    private final IFileOperationService fileOperationService;
    private final String guestCollectionId;

    public GlobusRequestHandler(final IFileOperationService fileOperationService,
                                final String guestCollectionId) {
        this.fileOperationService = fileOperationService;
        this.guestCollectionId = guestCollectionId;
    }

    /**
     * First tries to search for requested directory & returns the same, if it doesn't exist then creates
     * directory on Globus under guest collection. After successful creation of a folder grants the permission
     * for a user.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return guest collection directory details represented by {@link GuestCollectionDirResDTO}
     */
    public Mono<ServerResponse> createDirectoryOnGuestCollection(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(GuestCollectionDirReqDTO.class)
                .flatMap(guestCollectionDirReqDTO -> {
                    final Path fullDirPath = get(guestCollectionDirReqDTO.getNotifyEmail(), guestCollectionDirReqDTO.getDirectoryName());
                    return fileOperationService
                            .listFiles(fullDirPath)
                            .flatMap(ignoreData -> status(OK)
                                    .bodyValue(new GuestCollectionDirResDTO(
                                            guestCollectionId,
                                            fullDirPath.toString())))
                            .onErrorResume(listFullPathThrowable -> fileOperationService
                                    .listFiles(fullDirPath.getParent())
                                    .flatMap(globusFileDetailsWrapperDTO -> fileOperationService.createDirectory(fullDirPath))
                                    .onErrorResume(listPArentPathThrowable -> {
                                        if (listPArentPathThrowable instanceof ClientException ce && NOT_FOUND.equals(ce.getHttpStatus())) {
                                            return fileOperationService.createDirectory(fullDirPath.getParent())
                                                    .flatMap(ignoreStr -> fileOperationService
                                                            .createDirectory(fullDirPath))
                                                    .flatMap(ignoreResponse -> grantDirectoryPermission(
                                                            guestCollectionDirReqDTO.getGlobusUserUID(),
                                                            guestCollectionDirReqDTO.getNotifyEmail(),
                                                            fullDirPath.getParent()));
                                        } else {
                                            return error(listPArentPathThrowable);
                                        }
                                    })
                                    .flatMap(ignoreData -> status(CREATED)
                                            .bodyValue(new GuestCollectionDirResDTO(
                                                    guestCollectionId,
                                                    fullDirPath.toString())))
                            );
                });
    }

    /**
     * Returns list of files available on the given path.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return GlobusFileDetailsWrapperDTO
     */
    public Mono<ServerResponse> listFilesOnGuestCollectionDirectory(final ServerRequest serverRequest) {
        return serverRequest
                .queryParam("path")
                .map(path -> fileOperationService
                        .listFiles(get(path))
                        .flatMap(globusFileDetailsWrapperDTO -> ok().bodyValue(globusFileDetailsWrapperDTO)))
                .orElse(error(badRequest("Query param 'path' is missing or empty!")));
    }

    private Mono<String> grantDirectoryPermission(final String principalId,
                                                  final String notifyEmail,
                                                  final Path path) {
        return fileOperationService.grantDirectoryPermission(
                principalId,
                READ_WRITE,
                notifyEmail,
                path);
    }
}
