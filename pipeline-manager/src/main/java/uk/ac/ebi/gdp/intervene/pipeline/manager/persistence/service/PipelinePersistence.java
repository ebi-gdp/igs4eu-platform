/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineStatusDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineResult;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineExecutionStatusRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineResultRepository;

import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails.create;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.getPipelineStatusByDescription;

/**
 * Persistence service layer, clubs different repositories,
 * perform database operations.
 */
public class PipelinePersistence implements IPipelinePersistence {
    private final PipelineDetailsRepository pipelineDetailsRepository;
    private final PipelineExecutionStatusRepository pipelineExecutionStatusRepository;
    private final PipelineResultRepository pipelineResultRepository;

    public PipelinePersistence(final PipelineDetailsRepository pipelineDetailsRepository,
                               final PipelineExecutionStatusRepository pipelineExecutionStatusRepository,
                               final PipelineResultRepository pipelineResultRepository) {
        this.pipelineDetailsRepository = pipelineDetailsRepository;
        this.pipelineExecutionStatusRepository = pipelineExecutionStatusRepository;
        this.pipelineResultRepository = pipelineResultRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Mono<PipelineDetails> createPipeline(final String userId,
                                                final String datasetId) {
        return pipelineDetailsRepository
                .getNextPipelineId()
                .map(nextPipelineId -> create(nextPipelineId, userId, datasetId))
                .flatMap(this::savePipeline);
    }

    private Mono<PipelineDetails> savePipeline(final PipelineDetails pipelineDetailsToBePersisted) {
        return pipelineDetailsRepository
                .save(pipelineDetailsToBePersisted)
                .flatMap(pipelineDetails -> pipelineExecutionStatusRepository
                        .save(pipelineDetails.getPipelineExecutionStatus())
                        .thenReturn(pipelineDetails));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Mono<PipelineExecutionStatus> updatePipelineStatus(final String pipelineId,
                                                              final PipelineStatusDTO pipelineStatusDTO) {
        return getPipelineExecutionStatus(pipelineId)
                .flatMap(pipelineExecutionStatus -> doUpdatePipelineStatus(pipelineStatusDTO, pipelineExecutionStatus)
                        .then(pipelineExecutionStatusRepository.save(pipelineExecutionStatus)));
    }

    private Mono<Void> doUpdatePipelineStatus(final PipelineStatusDTO pipelineStatusDTO,
                                              final PipelineExecutionStatus pipelineExecutionStatus) {
        switch (getPipelineStatusByDescription(pipelineStatusDTO.getStatus())) {
            case PENDING -> pipelineExecutionStatus.pending();
            case STARTED -> pipelineExecutionStatus.started(pipelineStatusDTO.getUtcTime());
            case COMPLETED -> pipelineExecutionStatus.completed(pipelineStatusDTO.getUtcTime());
            case ERROR -> pipelineExecutionStatus.error(
                    pipelineStatusDTO.getUtcTime(),
                    pipelineStatusDTO.getTraceName(),
                    pipelineStatusDTO.getTraceExit()
            );
            default -> {
                return error(() -> badRequest("Invalid status: %s".formatted(pipelineStatusDTO.getStatus())));
            }
        }
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineResult> createPipelineResult(final PipelineResultEvent pipelineResultEvent) {
        return pipelineResultRepository
                .findById(pipelineResultEvent.pipelineId())
                .flatMap(pipelineResult ->
                        Mono.<PipelineResult>error(badRequest("Pipeline result record for Pipeline Id: %s already exists!".formatted(pipelineResult.getPipelineId()))))
                .switchIfEmpty(Mono.defer(() -> {
                    PipelineResult pipelineResult = PipelineResult.create(
                            pipelineResultEvent.pipelineId(),
                            pipelineResultEvent.outputFileLocation()
                    );
                    return pipelineResultRepository.save(pipelineResult);
                }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineResult> getPipelineResultRecent(final String userId) {
        return pipelineResultRepository.findCompletedRecent(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineResult> getPipelineResult(final String pipelineId,
                                                  final String userId) {
        return pipelineResultRepository.findCompleted(pipelineId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineDetails> getPipeline(final String pipelineId,
                                             final String userId) {
        return pipelineDetailsRepository.find(pipelineId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineDetails> getPipelineFull(final String pipelineId,
                                                 final String userId) {
        return pipelineDetailsRepository.findPipelineDetailsFull(pipelineId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<PipelineDetails> getPipelines(final String userId,
                                              final int limit,
                                              final int offset) {
        return pipelineDetailsRepository.findAll(userId, limit, offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Long> getPipelinesCount(final String userId) {
        return pipelineDetailsRepository.countAllByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineDetails> getPipelineFullRecent(final String userId) {
        return pipelineDetailsRepository.findPipelineDetailsFullRecent(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Mono<PipelineDetails> save(final PipelineDetails pipelineDetails) {
        return pipelineDetailsRepository.save(pipelineDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineExecutionStatus> getPipelineExecutionStatus(final String pipelineId) {
        return pipelineExecutionStatusRepository
                .findPipelineExecutionStatus(pipelineId)
                .switchIfEmpty(error(resourceNotFound("Pipeline Id %s not found!".formatted(pipelineId))));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Mono<PipelineExecutionStatus> savePipelineExecutionStatus(final PipelineExecutionStatus pipelineExecutionStatus) {
        return pipelineExecutionStatusRepository.save(pipelineExecutionStatus);
    }

    @Override
    public Mono<DatasetDetails> getDatasetName(final String pipelineId) {
        return pipelineDetailsRepository.findDatasetName(pipelineId);
    }
}
