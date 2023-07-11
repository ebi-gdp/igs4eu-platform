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
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.GlobusUserDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.defer;

public class GlobusRequestHandler {
    private final PipelineManagerService pipelineManagerService;
    private final UserManagerService userManagerService;
    private final GlobusFileHandlerService globusFileHandlerService;
    private final GlobusUserRepository globusUserRepository;
    private final GlobusUserDetailsMapper globusUserDetailsMapper;

    public GlobusRequestHandler(final PipelineManagerService pipelineManagerService,
                                final UserManagerService userManagerService,
                                final GlobusFileHandlerService globusFileHandlerService,
                                final GlobusUserRepository globusUserRepository,
                                final GlobusUserDetailsMapper globusUserDetailsMapper) {
        this.pipelineManagerService = pipelineManagerService;
        this.userManagerService = userManagerService;
        this.globusFileHandlerService = globusFileHandlerService;
        this.globusUserRepository = globusUserRepository;
        this.globusUserDetailsMapper = globusUserDetailsMapper;
    }

    public Mono<ServerResponse> mapGlobusUserId(final ServerRequest serverRequest) {//TODO: handle exceptions
        return serverRequest
                .bodyToMono(String.class)
                .flatMap(username -> globusUserRepository
                        .findById(username)
                        .flatMap(globusUserDetails -> status(CONFLICT).build())
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
        final Path directoryName = get(serverRequest.pathVariable("pipelineId"));//TODO: Pipeline id for the time being, this will change in the future
        return serverRequest
                .bodyToMono(String.class)//TODO: handle exceptions
                .flatMap(globusUsername -> pipelineManagerService
                        .createDirectoryOnGuestCollection(
                                directoryName,
                                globusUsername)
                        .flatMap(globusDetailsDTO -> pipelineManagerService
                                .createGlobusRecord(
                                        globusUsername,
                                        globusDetailsDTO.getGuestCollectionId(),
                                        directoryName)
                                .map(globusDetails -> {
                                    // globusDetailsDTO.setUserAccountUID(globusDetails.getGlobusUserDetails().getUserUID());
                                    globusDetailsDTO.setFilesetId(globusDetails.getFilesetId());
                                    return globusDetailsDTO;
                                })))
                .flatMap(globusDetailsDTO -> status(CREATED).bodyValue(globusDetailsDTO));
    }
}
