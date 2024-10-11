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
package uk.ac.ebi.gdp.intervene.pipeline.manager.router;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.IGlobusFileDetailsWrapper;
import uk.ac.ebi.gdp.intervene.commons.dto.usermanager.GlobusUserDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.GlobusDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.validation.CreateDirDTOValidator;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.GlobusUserDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusManagerService;

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static java.util.UUID.randomUUID;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.UserAccountUtil.userAccount;

/**
 * Request handler for Globus related operations.
 */
public class GlobusRequestHandler {
    private static final Logger LOGGER = getLogger(GlobusRequestHandler.class);
    private final GlobusManagerService globusManagerService;
    private final GlobusUserDetailsMapper globusUserDetailsMapper;
    private final FileValidations<IGlobusFileDetailsWrapper> fileValidations;
    private final CreateDirDTOValidator createDirDTOValidator;

    public GlobusRequestHandler(final GlobusManagerService globusManagerService,
                                final GlobusUserDetailsMapper globusUserDetailsMapper,
                                final FileValidations<IGlobusFileDetailsWrapper> fileValidations,
                                final CreateDirDTOValidator createDirDTOValidator) {
        this.globusManagerService = globusManagerService;
        this.globusUserDetailsMapper = globusUserDetailsMapper;
        this.fileValidations = fileValidations;
        this.createDirDTOValidator = createDirDTOValidator;
    }

    /**
     * Maps Globus user id if doesn't exists or else returns existing.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Globus user details represented by {@link GlobusUserDetailsDTO}
     */
    @Transactional
    public Mono<ServerResponse> mapGlobusUserId(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(String.class)
                .flatMap(username -> globusManagerService
                        .getUserDetails(username)
                        .flatMap(globusUserDetails -> mappingFound(globusUserDetails, username))
                        .switchIfEmpty(defer(() -> createMapping(username, serverRequest))));
    }

    private Mono<ServerResponse> mappingFound(final GlobusUserDetails globusUserDetails,
                                              final String username) {
        return status(OK)
                .bodyValue(globusUserDetailsMapper.toDTO(globusUserDetails))
                .doOnNext(serverResponse -> LOGGER.info("Globus mapping found for {}", username));
    }

    private Mono<ServerResponse> createMapping(final String username,
                                               final ServerRequest serverRequest) {
        LOGGER.info("Globus mapping not found for {}, mapping is being created!", username);
        return userAccount(serverRequest)
                .flatMap(userAccountDTO -> globusManagerService
                        .getUserDetails(username, userAccountDTO.accountId()))
                .flatMap(globusManagerService::save)
                .map(globusUserDetailsMapper::toDTO)
                .flatMap(globusUserDetailsDTO -> status(CREATED)
                        .bodyValue(globusUserDetailsDTO))
                .doOnNext(serverResponse -> LOGGER.info("Globus mapping has been successfully created"));
    }

    /**
     * Creates directory on guest collection.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Globus details represented by {@link GlobusDetailsDTO}, expected https status 200(Ok)/201(Created)
     * @see HttpStatus
     */
    public Mono<ServerResponse> createDirectoryOnGuestCollection(final ServerRequest serverRequest) {
        return createDirDTOValidator
                .handleRequest(serverRequest)
                .flatMap(createDirDTO -> {
                    LOGGER.info("Creating directory on Globus guest collection");
                    final Path directoryName = get(buildUniqueFolderName(createDirDTO.datasetName()));
                    return createDirectory(directoryName, createDirDTO.globusUsername())
                            .flatMap(globusDetailsDTO -> createOrUpdateGlobusRecord(
                                    globusDetailsDTO, createDirDTO.globusUsername(), directoryName));
                });
    }

    private String buildUniqueFolderName(final String datasetName) {
        return datasetName.concat("-").concat(randomUUID().toString().substring(0, 8));
    }

    private Mono<GlobusDetailsDTO> createDirectory(final Path directoryName,
                                                   final String globusUsername) {
        return globusManagerService
                .createDirectoryOnGuestCollection(
                        directoryName,
                        globusUsername)
                .doOnNext(globusDetailsDTO -> LOGGER.info("Directory has been created"));
    }

    private Mono<ServerResponse> createOrUpdateGlobusRecord(final GlobusDetailsDTO globusDetailsDTO,
                                                            final String globusUsername,
                                                            final Path directoryName) {
        return globusManagerService
                .createOrUpdateGlobusRecord(
                        globusUsername,
                        globusDetailsDTO.getGuestCollectionId(),
                        directoryName)
                .flatMap(globusDetails -> {
                    globusDetailsDTO.setFilesetId(globusDetails.getFilesetId());
                    if (globusDetails.isNew()) {
                        LOGGER.info("Globus details have been created");
                        return status(CREATED).bodyValue(globusDetailsDTO);
                    } else {
                        LOGGER.info("Globus details have been updated");
                        return status(OK).bodyValue(globusDetailsDTO);
                    }
                });
    }

    /**
     * Validate files uploaded on Globus.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return empty body with appropriate http status code, 200(Ok) for 400(Bad request)
     * @see HttpStatus
     */
    public Mono<ServerResponse> validateFiles(final ServerRequest serverRequest) {
        LOGGER.info("Validating globus files");
        return serverRequest
                .queryParam("path")
                .map(filePath -> globusManagerService
                        .listFilesOnGuestCollection(get(filePath))
                        .doOnNext(globusFileDetailsWrapperDTO -> LOGGER.info("Globus files have been listed"))
                        .filter(globusFileDetailsWrapperDTO -> fileValidations
                                .validations()
                                .stream()
                                .allMatch(predicate -> predicate.test(globusFileDetailsWrapperDTO)))
                        .flatMap(ignoreData -> ok().build()))
                .orElse(error(badRequest("Query param 'path' is missing or empty!")));
    }
}
