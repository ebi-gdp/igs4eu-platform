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
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineResult;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineResultRepository;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.NEW;

public class PipelinePersistence implements IPipelinePersistence {
    private final PipelineDetailsRepository pipelineDetailsRepository;
    private final PipelineResultRepository pipelineResultRepository;

    public PipelinePersistence(final PipelineDetailsRepository pipelineDetailsRepository,
                               final PipelineResultRepository pipelineResultRepository) {
        this.pipelineDetailsRepository = pipelineDetailsRepository;
        this.pipelineResultRepository = pipelineResultRepository;
    }

    @Transactional(transactionManager = "reactiveTransactionManager")
    @Override
    public Mono<PipelineDetails> createPipeline(final String userId) {
        //Get next pipeline id
        return pipelineDetailsRepository
                .getNextPipelineId()
                .map(nextPipelineId -> new PipelineDetails(
                        nextPipelineId,
                        userId,
                        NEW))
                .flatMap(pipelineDetailsRepository::save);
    }

    @Transactional(transactionManager = "reactiveTransactionManager")
    @Override
    public Mono<PipelineDetails> updatePipelineDetailsStatus(final String pipelineId,
                                                             final PipelineStatus pipelineStatus) {
        return pipelineDetailsRepository
                .findById(pipelineId)
                .flatMap(pipelineDetails -> {
                    pipelineDetails.setStatus(pipelineStatus);
                    return pipelineDetailsRepository.save(pipelineDetails);
                    //.then();//TODO, work on this. Use Reactive Kafka & then check @Transactional effect
                });
    }

    @Transactional(transactionManager = "reactiveTransactionManager")
    @Override
    public Mono<PipelineResult> persistPipelineResult(final PipelineResultEvent pipelineResultEvent) {
        final PipelineResult pipelineResult = new PipelineResult(
                pipelineResultEvent.pipelineId(),
                pipelineResultEvent.outputFileLocation());
        return pipelineResultRepository.save(pipelineResult);
    }

    @Override
    public Mono<PipelineResult> getLatestPipelineResult(final String userId) {
        return pipelineDetailsRepository
                .findTopByUserIdAndStatusOrderByUpdatedOnDesc(userId, PipelineStatus.COMPLETED)
                .flatMap(pipelineDetails -> pipelineResultRepository
                        .findById(pipelineDetails.getPipelineId()));
    }

    @Override
    public Mono<PipelineResult> getPipelineResult(final String pipelineId) {
        return pipelineResultRepository.findById(pipelineId);
    }

    @Override
    public Mono<PipelineDetails> getPipelineDetails(final String pipelineId,
                                                    final String userId) {
        return pipelineDetailsRepository.findByPipelineIdAndUserId(pipelineId, userId);
    }

    @Override
    public Mono<PipelineDetails> getPipelineDetailsRecent(final String userId) {
        //return pipelineDetailsRepository.findTopByUserIdOrderByUpdatedOnDesc(userId);
        return pipelineDetailsRepository.findRecentFullPipelineDetails(userId);
    }

    @Override
    public Mono<PipelineDetails> save(final PipelineDetails pipelineDetails) {
        return pipelineDetailsRepository.save(pipelineDetails);
    }
}
