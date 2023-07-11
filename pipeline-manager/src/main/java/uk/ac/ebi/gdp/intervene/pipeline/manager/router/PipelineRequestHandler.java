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
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineExecutionDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.PipelineDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;

public class PipelineRequestHandler {
    private final PipelineManagerService pipelineManagerService;
    private final IPipelinePersistence pipelinePersistence;
    private final UserManagerService userManagerService;
    private final PipelineDetailsMapper pipelineDetailsMapper;

    public PipelineRequestHandler(final PipelineManagerService pipelineManagerService,
                                  final IPipelinePersistence pipelinePersistence,
                                  final UserManagerService userManagerService,
                                  final PipelineDetailsMapper pipelineDetailsMapper) {
        this.pipelineManagerService = pipelineManagerService;
        this.pipelinePersistence = pipelinePersistence;
        this.userManagerService = userManagerService;
        this.pipelineDetailsMapper = pipelineDetailsMapper;
    }

    public Mono<ServerResponse> createPipeline() {
        return userManagerService.
                getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence.createPipeline(userAccountDTO.getAccountId()))
                .map(pipelineDetails -> new PipelineDetailsDTO(pipelineDetails.getPipelineId(), pipelineDetails.getStatus()))
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    public Mono<ServerResponse> getPipelineDetails(final ServerRequest serverRequest) {
        return userManagerService.
                getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence.getPipelineDetails(
                        serverRequest.pathVariable("pipelineId"),
                        userAccountDTO.getAccountId()))
                .map(pipelineDetailsMapper::toDTO)
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    public Mono<ServerResponse> getPipelineDetailsRecent() {
        return userManagerService.
                getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence.getPipelineDetailsRecent(userAccountDTO.getAccountId()))
                .map(pipelineDetailsMapper::toDTO)
                .switchIfEmpty(error(resourceNotFound("No recent submission found!")))
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    public Mono<ServerResponse> updateDatasetId(final ServerRequest serverRequest) {
        return userManagerService.
                getUserAccountDetails()
                .flatMap(userAccount ->
                        pipelinePersistence
                                .getPipelineDetails(
                                        serverRequest.pathVariable("pipelineId"),
                                        userAccount.getAccountId()))
                .flatMap(pipelineDetails -> serverRequest
                        .bodyToMono(String.class)
                        .map(datasetId -> {
                            pipelineDetails.updateDatasetId(datasetId);
                            return pipelineDetails;
                        }))
                .flatMap(pipelinePersistence::save)
                .flatMap(pipelineDetails -> status(ACCEPTED).build());
    }

    public Mono<ServerResponse> executePipeline(final ServerRequest serverRequest) {
        return userManagerService.
                getUserAccountDetails()
                .flatMap(userAccountDTO ->
                        pipelinePersistence
                                .getPipelineDetails(serverRequest.pathVariable("pipelineId"),
                                        userAccountDTO.getAccountId()))
                .flatMap(pipelineDetails ->
                        serverRequest
                                .bodyToMono(PipelineExecutionDTO.class)
                                .flatMap(pipelineExecutionDTO ->
                                        pipelineManagerService.triggerGeneticScoringPipeline(
                                                pipelineDetails.getPipelineId(),
                                                pipelineExecutionDTO))
                                .thenReturn(pipelineDetails))
                .map(pipelineDetails -> {
                    pipelineDetails.statusPending();
                    return pipelineDetails;
                })
                .flatMap(pipelinePersistence::save)
                .flatMap(pipelineDetails -> status(ACCEPTED).bodyValue("Request received!"));
    }
}
