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
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PGSIdsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PaginationDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.PipelineDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;

public class PipelineRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineRequestHandler.class);
    private final PipelineManagerService pipelineManagerService;
    private final IPipelinePersistence pipelinePersistence;
    private final UserManagerService userManagerService;
    private final PipelineDetailsMapper pipelineDetailsMapper;
    private final StringRedisTemplate redisTemplate;

    public PipelineRequestHandler(final PipelineManagerService pipelineManagerService,
                                  final IPipelinePersistence pipelinePersistence,
                                  final UserManagerService userManagerService,
                                  final PipelineDetailsMapper pipelineDetailsMapper,
                                  final StringRedisTemplate redisTemplate) {
        this.pipelineManagerService = pipelineManagerService;
        this.pipelinePersistence = pipelinePersistence;
        this.userManagerService = userManagerService;
        this.pipelineDetailsMapper = pipelineDetailsMapper;
        this.redisTemplate = redisTemplate;
    }

    public Mono<ServerResponse> createPipeline(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(String.class)
                .flatMap(datasetId -> userManagerService
                        .getUserAccountDetails()
                        .flatMap(userAccountDTO -> pipelinePersistence.createPipeline(userAccountDTO.accountId(), datasetId)))
                .map(pipelineDetails -> new PipelineDetailsDTO(pipelineDetails.getPipelineId(), pipelineDetails.getPipelineExecutionStatus().getStatus()))
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    public Mono<ServerResponse> getPipeline(final ServerRequest serverRequest) {
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence
                        .getPipelineFull(serverRequest.pathVariable("pipelineId"),
                                userAccountDTO.accountId())
                        .doOnNext(ignoreData -> LOGGER.info("Pipeline details are retrieved for {}", serverRequest.pathVariable("pipelineId"))))
                .switchIfEmpty(error(resourceNotFound("Pipeline details not found for the given pipeline id!")))
                .map(pipelineDetailsMapper::toDTO)
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    public Mono<ServerResponse> getPipelines(final ServerRequest serverRequest) {
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence
                        .getPipelinesCount(userAccountDTO.accountId())
                        .flatMap(count -> doGetPipelines(serverRequest, userAccountDTO.accountId())
                                .map(pipelineDetailsDTOS -> new PaginationDTO<>(pipelineDetailsDTOS, count)))
                        .flatMap(paginationDTO -> ok().bodyValue(paginationDTO)));
    }

    private Mono<List<PipelineDetailsDTO>> doGetPipelines(final ServerRequest serverRequest,
                                                          final String accountId) {
        final int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        final int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        return pipelinePersistence
                .getPipelines(accountId, size, page * size)
                .collectList()
                .map(this::buildPipelineList);
    }

    private List<PipelineDetailsDTO> buildPipelineList(final List<PipelineDetails> pipelineDetailsMono) {
        return pipelineDetailsMono
                .parallelStream()
                .map(pipelineDetailsMapper::toDTO)
                .collect(toList());
    }

    public Mono<ServerResponse> getPipelineRecent() {
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence.getPipelineFullRecent(userAccountDTO.accountId()))
                .map(pipelineDetailsMapper::toDTO)
                .switchIfEmpty(error(resourceNotFound("No recent submission found!")))
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    public Mono<ServerResponse> updateDatasetId(final ServerRequest serverRequest) {
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccount ->
                        pipelinePersistence
                                .getPipeline(serverRequest.pathVariable("pipelineId"),
                                        userAccount.accountId()))
                .flatMap(pipelineDetails -> serverRequest
                        .bodyToMono(String.class)
                        .map(datasetId -> {
                            pipelineDetails.updateDatasetId(datasetId);
                            return pipelineDetails;
                        }))
                .flatMap(pipelinePersistence::save)
                .flatMap(pipelineDetails -> status(OK).build());
    }

    public Mono<ServerResponse> executePipeline(final ServerRequest serverRequest) {
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO ->
                        pipelinePersistence
                                .getPipelineFull(serverRequest.pathVariable("pipelineId"), userAccountDTO.accountId()))
                .flatMap(pipelineDetails ->
                        serverRequest
                                .bodyToMono(String.class)
                                .flatMap(polygenicScoreIds -> pipelineManagerService
                                        .triggerGeneticScoringPipeline(
                                                pipelineDetails,
                                                polygenicScoreIds))
                                .thenReturn(pipelineDetails))
                .flatMap(pipelineDetails -> pipelinePersistence.getPipelineExecutionStatus(pipelineDetails.getPipelineId()))
                .map(pipelineExecutionStatus -> {
                    pipelineExecutionStatus.pending();
                    return pipelineExecutionStatus;
                })
                .flatMap(pipelinePersistence::savePipelineExecutionStatus)
                .flatMap(pipelineDetails -> status(ACCEPTED).bodyValue("Request received!"));
    }

    public Mono<ServerResponse> validatePGSIds(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(PGSIdsDTO.class)
                .mapNotNull(pgsIdsDTO -> redisTemplate
                        .opsForSet()
                        .isMember("pgs_ids_set", pgsIdsDTO.getPgsIds().toArray()))
                .filter(objectBooleanMap -> objectBooleanMap
                        .entrySet()
                        .parallelStream()
                        .anyMatch(objectBooleanEntry -> !objectBooleanEntry.getValue()))
                .flatMap(pgsIdMap -> badRequest()
                        .bodyValue(pgsIdMap))
                .switchIfEmpty(ok().build());
    }
}
