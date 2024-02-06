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
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.exception.ClientException;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PGSIdsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PGSTraitWrapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PaginationDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.PipelineDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PGSCatalogService;
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

/**
 * Request handler for Pipeline related operations.
 */
public class PipelineRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineRequestHandler.class);
    private final PipelineManagerService pipelineManagerService;
    private final IPipelinePersistence pipelinePersistence;
    private final UserManagerService userManagerService;
    private final PipelineDetailsMapper pipelineDetailsMapper;
    private final StringRedisTemplate redisTemplate;
    private final PGSCatalogService pgsCatalogService;

    public PipelineRequestHandler(final PipelineManagerService pipelineManagerService,
                                  final IPipelinePersistence pipelinePersistence,
                                  final UserManagerService userManagerService,
                                  final PipelineDetailsMapper pipelineDetailsMapper,
                                  final StringRedisTemplate redisTemplate,
                                  final PGSCatalogService pgsCatalogService) {
        this.pipelineManagerService = pipelineManagerService;
        this.pipelinePersistence = pipelinePersistence;
        this.userManagerService = userManagerService;
        this.pipelineDetailsMapper = pipelineDetailsMapper;
        this.redisTemplate = redisTemplate;
        this.pgsCatalogService = pgsCatalogService;
    }

    /**
     * Creates pipeline.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Pipeline details represented by {@link PipelineDetailsDTO}
     */
    public Mono<ServerResponse> createPipeline(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(String.class)
                .flatMap(datasetId -> userManagerService
                        .getUserAccountDetails()
                        .doOnNext(userAccountDTO -> LOGGER.info("Creating pipeline instance for Dataset Id: {}", datasetId))
                        .flatMap(userAccountDTO -> pipelinePersistence
                                .createPipeline(userAccountDTO.accountId(), datasetId)
                                .doOnNext(pipelineDetails -> LOGGER.info("Pipeline instance has been created, Pipeline Id: {}", pipelineDetails.getPipelineId()))))
                .map(pipelineDetails -> new PipelineDetailsDTO(pipelineDetails.getPipelineId(), pipelineDetails.getPipelineExecutionStatus().getStatus()))
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    /**
     * Retrieves pipeline.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Pipeline details represented by {@link PipelineDetailsDTO}
     */
    public Mono<ServerResponse> getPipeline(final ServerRequest serverRequest) {
        final String pipelineId = serverRequest.pathVariable("pipelineId");
        return userManagerService
                .getUserAccountDetails()
                .doOnNext(userAccountDTO -> LOGGER.info("Retrieving pipeline details for {}", pipelineId))
                .flatMap(userAccountDTO -> pipelinePersistence
                        .getPipelineFull(pipelineId,
                                userAccountDTO.accountId())
                        .doOnNext(ignoreData -> LOGGER.info("Pipeline details are retrieved for {}", serverRequest.pathVariable("pipelineId"))))
                .switchIfEmpty(error(resourceNotFound("Pipeline details not found for the given pipeline id!")))
                .map(pipelineDetailsMapper::toDTO)
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO));
    }

    /**
     * List pipelines for a user.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Paginated list of pipelines represented by {@link PaginationDTO}
     */
    public Mono<ServerResponse> getPipelines(final ServerRequest serverRequest) {
        return userManagerService
                .getUserAccountDetails()
                .doOnNext(userAccountDTO -> LOGGER.info("Retrieving all pipelines"))
                .flatMap(userAccountDTO -> pipelinePersistence
                        .getPipelinesCount(userAccountDTO.accountId())
                        .flatMap(count -> doGetPipelines(serverRequest, userAccountDTO.accountId())
                                .map(pipelineDetailsDTOS -> new PaginationDTO<>(pipelineDetailsDTOS, count)))
                        .flatMap(paginationDTO -> ok().bodyValue(paginationDTO))
                        .doOnNext(serverResponse -> LOGGER.info("Retrieved all pipelines")));
    }

    private Mono<List<PipelineDetailsDTO>> doGetPipelines(final ServerRequest serverRequest,
                                                          final String accountId) {
        final int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        final int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        LOGGER.debug("Page number: {}, size: {}", page, size);
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

    /**
     * Retrieves recent pipeline details.
     *
     * @return Pipeline details represented by {@link PipelineDetailsDTO}
     */
    public Mono<ServerResponse> getPipelineRecent() {
        return userManagerService
                .getUserAccountDetails()
                .doOnNext(userAccountDTO -> LOGGER.info("Retrieving recent pipeline details"))
                .flatMap(userAccountDTO -> pipelinePersistence.getPipelineFullRecent(userAccountDTO.accountId()))
                .map(pipelineDetailsMapper::toDTO)
                .switchIfEmpty(error(resourceNotFound("No recent submission found!")))
                .flatMap(pipelineDetailsDTO -> ok().bodyValue(pipelineDetailsDTO))
                .doOnNext(userAccountDTO -> LOGGER.info("Retrieved recent pipeline details"));
    }

    /**
     * Updates dataset id in pipeline details.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Empty response with 200(Ok) https status
     * @see HttpStatus
     */
    public Mono<ServerResponse> updateDatasetId(final ServerRequest serverRequest) {
        final String pipelineId = serverRequest.pathVariable("pipelineId");
        return userManagerService
                .getUserAccountDetails()
                .doOnNext(userAccountDTO -> LOGGER.info("Updating dataset id for pipeline details"))
                .flatMap(userAccount -> {
                    LOGGER.info("Retrieving pipeline details: {}", pipelineId);
                    return pipelinePersistence
                            .getPipeline(pipelineId,
                                    userAccount.accountId());
                })
                .flatMap(pipelineDetails -> serverRequest
                        .bodyToMono(String.class)
                        .map(datasetId -> {
                            LOGGER.info("Updating dataset id: {}", datasetId);
                            pipelineDetails.updateDatasetId(datasetId);
                            return pipelineDetails;
                        }))
                .flatMap(pipelinePersistence::save)
                .flatMap(pipelineDetails -> status(OK).build())
                .doOnNext(userAccountDTO -> LOGGER.info("Successfully updated dataset id"));
    }

    /**
     * Executes pipeline for PGS Ids.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Http status 202(Accepted)
     * @see HttpStatus
     */
    public Mono<ServerResponse> executePipelineForPgsIds(final ServerRequest serverRequest) {
        LOGGER.info("Executing pipeline for PGS Ids");
        return getPipelineDetails(serverRequest)
                .flatMap(pipelineDetails -> serverRequest
                        .bodyToMono(String.class)
                        .flatMap(polygenicScoreIds /*TODO change polygenicScoreIds from string to list of ids */ -> pipelineManagerService
                                .triggerGeneticScoringPipelineWithPgsIds(
                                        pipelineDetails,
                                        polygenicScoreIds)
                                .doOnSuccess(unused -> LOGGER.info("Pipeline execution request has been submitted!")))
                        .thenReturn(pipelineDetails))
                .flatMap(this::updatePipelineExecutionStatus);
    }

    /**
     * Executes pipeline for Trait Ids.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Http status 202(Accepted)
     * @see HttpStatus
     */
    public Mono<ServerResponse> executePipelineForTraitIds(final ServerRequest serverRequest) {
        LOGGER.info("Executing pipeline for Trait Ids");
        return getPipelineDetails(serverRequest)
                .flatMap(pipelineDetails -> serverRequest
                        .bodyToMono(String.class)
                        .flatMap(polygenicTraitIds -> pipelineManagerService
                                .triggerGeneticScoringPipelineWithTraitIds(
                                        pipelineDetails,
                                        polygenicTraitIds)
                                .doOnSuccess(unused -> LOGGER.info("Pipeline execution request has been submitted!")))
                        .thenReturn(pipelineDetails))
                .flatMap(this::updatePipelineExecutionStatus);
    }

    private Mono<PipelineDetails> getPipelineDetails(final ServerRequest serverRequest) {
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence
                        .getPipelineFull(serverRequest.pathVariable("pipelineId"), userAccountDTO.accountId()));
    }

    private Mono<ServerResponse> updatePipelineExecutionStatus(final PipelineDetails pipelineDetails) {
        return pipelinePersistence
                .getPipelineExecutionStatus(pipelineDetails.getPipelineId())
                .map(pipelineExecutionStatus -> {
                    pipelineExecutionStatus.pending();
                    return pipelineExecutionStatus;
                })
                .flatMap(pipelinePersistence::savePipelineExecutionStatus)
                .flatMap(pipelineExecutionStatus -> status(ACCEPTED).bodyValue("Request received!"));
    }

    /**
     * Validates PGS Ids.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Https status 200(Ok) or 400(BadRequest)
     * @see HttpStatus
     */
    public Mono<ServerResponse> validatePGSIds(final ServerRequest serverRequest) {
        LOGGER.info("Validating PGS Ids");
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
                .switchIfEmpty(ok().build())
                .doOnNext(serverResponse -> LOGGER.info("PGS Ids have been successfully validated!"));
    }

    /**
     * Retrieves PGS Ids traits.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return PGS trait according to search term, represented by {@link PGSTraitWrapper} Or http status 404(NotFound)
     * @see HttpStatus
     */
    public Mono<ServerResponse> getPGSIdsByTraits(final ServerRequest serverRequest) {
        LOGGER.info("Retrieving Trait Ids from PGS Catalog API");
        final String searchTerm = serverRequest
                .queryParam("searchTerm")
                .orElseThrow(() -> ClientException.badRequest("Query parameter 'searchTerm' has an issue!"));
        return pgsCatalogService
                .searchPGSIdsByTraits(searchTerm)
                .filter(pgsTraitWrapper -> pgsTraitWrapper.results().size() > 0)
                .flatMap(pgsTraitWrapper -> ok()
                        .bodyValue(pgsTraitWrapper))
                .doOnNext(serverResponse -> LOGGER.info("Retrieved Trait Ids from PGS Catalog API"))
                .switchIfEmpty(error(resourceNotFound("Traits not found!")));
    }
}
