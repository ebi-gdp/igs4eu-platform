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

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.IGlobusFileDetailsWrapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.CreateDirDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.GlobusUserDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;

public class GlobusRequestHandler {
    private final PipelineManagerService pipelineManagerService;
    private final UserManagerService userManagerService;
    private final GlobusFileHandlerService globusFileHandlerService;
    private final GlobusUserRepository globusUserRepository;
    private final GlobusUserDetailsMapper globusUserDetailsMapper;
    private final FileValidations<IGlobusFileDetailsWrapper> fileValidations;

    public GlobusRequestHandler(final PipelineManagerService pipelineManagerService,
                                final UserManagerService userManagerService,
                                final GlobusFileHandlerService globusFileHandlerService,
                                final GlobusUserRepository globusUserRepository,
                                final GlobusUserDetailsMapper globusUserDetailsMapper,
                                final FileValidations<IGlobusFileDetailsWrapper> fileValidations) {
        this.pipelineManagerService = pipelineManagerService;
        this.userManagerService = userManagerService;
        this.globusFileHandlerService = globusFileHandlerService;
        this.globusUserRepository = globusUserRepository;
        this.globusUserDetailsMapper = globusUserDetailsMapper;
        this.fileValidations = fileValidations;
    }

    public Mono<ServerResponse> mapGlobusUserId(final ServerRequest serverRequest) {//TODO: handle exceptions
        return serverRequest
                .bodyToMono(String.class)
                .flatMap(username -> globusUserRepository
                        .findById(username)
                        .flatMap(globusUserDetails -> status(OK)
                                .bodyValue(globusUserDetailsMapper.toDTO(globusUserDetails)))
                        .switchIfEmpty(defer(() -> userManagerService
                                .getUserAccountDetails()
                                .flatMap(userAccountDTO -> globusFileHandlerService
                                        .getGlobusUserDetails(username)
                                        .map(globusUserIDWDto -> new GlobusUserDetails(
                                                username,
                                                globusUserIDWDto.getIdentities().get(0).getUid(),
                                                userAccountDTO.accountId())))
                                .flatMap(globusUserRepository::save)
                                .map(globusUserDetailsMapper::toDTO)
                                .flatMap(globusUserDetailsDTO -> status(CREATED)
                                        .bodyValue(globusUserDetailsDTO)))));
    }

    public Mono<ServerResponse> createDirectoryOnGuestCollection(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(CreateDirDTO.class)//TODO: handle exceptions
                .flatMap(createDirDTO -> {
                    final Path directoryName = get(createDirDTO.datasetName());
                    return pipelineManagerService
                            .createDirectoryOnGuestCollection(
                                    directoryName,
                                    createDirDTO.globusUsername())
                            .flatMap(globusDetailsDTO -> pipelineManagerService
                                    .createOrUpdateGlobusRecord(
                                            createDirDTO.globusUsername(),
                                            globusDetailsDTO.getGuestCollectionId(),
                                            directoryName)
                                    .flatMap(globusDetails -> {
                                        globusDetailsDTO.setFilesetId(globusDetails.getFilesetId());
                                        if (globusDetails.isNew()) {
                                            return status(CREATED).bodyValue(globusDetailsDTO);
                                        } else {
                                            return status(OK).bodyValue(globusDetailsDTO);
                                        }
                                    })
                            );
                });
    }

    public Mono<ServerResponse> validateFiles(final ServerRequest serverRequest) {
        return serverRequest
                .queryParam("path")
                .map(filePath -> globusFileHandlerService
                        .listFilesOnGuestCollection(get(filePath))
                        .filter(globusFileDetailsWrapperDTO -> fileValidations
                                .validations()
                                .stream()
                                .allMatch(predicate -> predicate.test(globusFileDetailsWrapperDTO)))
                        .flatMap(ignoreData -> ok().build()))
                .orElse(error(badRequest("Query param 'path' is missing or empty!")));
    }
}
