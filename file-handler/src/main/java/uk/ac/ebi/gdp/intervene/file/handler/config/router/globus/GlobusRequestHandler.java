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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.DeleteDirResponseDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
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
 * Globus request handler, defines handlers for router function.
 */
public class GlobusRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobusRequestHandler.class);
    private final IFileOperationService fileOperationService;
    private final String guestCollectionId;
    private static final int defaultListFilesLimit = 1;
    private final int listFilesLimit;

    public GlobusRequestHandler(final IFileOperationService fileOperationService,
                                final String guestCollectionId,
                                final int listFilesLimit) {
        this.fileOperationService = fileOperationService;
        this.guestCollectionId = guestCollectionId;
        this.listFilesLimit = listFilesLimit;
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
        LOGGER.info("Creating directory on Guest collection");
        return serverRequest
                .bodyToMono(GuestCollectionDirReqDTO.class)
                .flatMap(guestCollectionDirReqDTO -> {
                    final Path fullDirPath = get(guestCollectionDirReqDTO.getNotifyEmail(), guestCollectionDirReqDTO.getDirectoryName());
                    return fileOperationService
                            .listFiles(fullDirPath, defaultListFilesLimit)
                            .flatMap(ignoreData -> status(OK)
                                    .bodyValue(new GuestCollectionDirResDTO(
                                            guestCollectionId,
                                            fullDirPath.toString())))
                            .doOnNext(serverResponse -> LOGGER.info("Dir \"{}\" already exists! Returning the details for the same", fullDirPath))
                            .onErrorResume(listFullPathThrowable -> fileOperationService
                                    .listFiles(fullDirPath.getParent(), defaultListFilesLimit)
                                    .doOnNext(globusFileDetailsWrapperDTO -> LOGGER.info("User's home directory \"{}\" exists!", fullDirPath.getParent()))
                                    .flatMap(globusFileDetailsWrapperDTO -> createDirectory(fullDirPath))
                                    .onErrorResume(listParentPathThrowable -> {
                                        if (listParentPathThrowable instanceof ClientException ce && NOT_FOUND.equals(ce.getHttpStatus())) {
                                            return createDirAndGrantPermission(guestCollectionDirReqDTO, fullDirPath);
                                        } else {
                                            return error(listParentPathThrowable);
                                        }
                                    })
                                    .flatMap(ignoreData -> status(CREATED)
                                            .bodyValue(new GuestCollectionDirResDTO(
                                                    guestCollectionId,
                                                    fullDirPath.toString())))
                                    .doOnNext(serverResponse -> LOGGER.info("Required directory has been created on the Guest collection for the user!"))
                            );
                });
    }

    private Mono<String> createDirAndGrantPermission(final GuestCollectionDirReqDTO guestCollectionDirReqDTO,
                                                     final Path fullDirPath) {
        return fileOperationService
                .createDirectory(fullDirPath.getParent())
                .doOnNext(dir -> LOGGER.info("User's home directory has been created: {}", fullDirPath.getParent()))
                .flatMap(ignoreStr -> createDirectory(fullDirPath))
                .flatMap(ignoreResponse -> grantDirectoryPermission(
                        guestCollectionDirReqDTO.getGlobusUserUID(),
                        guestCollectionDirReqDTO.getNotifyEmail(),
                        fullDirPath.getParent()));
    }

    private Mono<String> createDirectory(final Path dirPath) {
        return fileOperationService
                .createDirectory(dirPath)
                .doOnNext(dirName -> LOGGER.info("Directory has been created under user's home directory: {}", dirName));
    }

    /**
     * Returns list of files available on the given path.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return list of files represented by {@link GlobusFileDetailsWrapperDTO}.
     */
    public Mono<ServerResponse> listFilesOnGuestCollectionDirectory(final ServerRequest serverRequest) {
        LOGGER.info("Listing files on Guest collection");
        return getPath(serverRequest)
                .flatMap(path -> fileOperationService
                        .listFiles(get(path), listFilesLimit)
                        .doOnNext(ignoreResponse -> LOGGER.info("Retrieved files at {}", path))
                        .flatMap(globusFileDetailsWrapperDTO -> ok()
                                .bodyValue(globusFileDetailsWrapperDTO))
                        .doOnNext(serverResponse -> LOGGER.info("Successfully listed files at {}", path)));
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

    /**
     * Deletes directory on Guest collection.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return deleted dir details represented by {@link DeleteDirResponseDTO}.
     */
    public Mono<ServerResponse> deleteDirectoryOnGuestCollection(final ServerRequest serverRequest) {
        return getPath(serverRequest)
                .flatMap(path -> fileOperationService
                        .getSubmissionId()
                        .flatMap(submissionIdDTO -> fileOperationService
                                .deleteDirectoryOnGuestCollection(
                                        submissionIdDTO.submissionId(),
                                        Path.of(path)))
                        .flatMap(deleteDirResponseDTO -> ok().bodyValue(deleteDirResponseDTO)));
    }

    private Mono<String> getPath(final ServerRequest serverRequest) {
        return serverRequest
                .queryParam("path")
                .map(Mono::just)
                .orElse(error(badRequest("Query param 'path' is missing or empty!")));
    }
}
