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
import uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PipelineResult;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.repository.PipelineDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.repository.PipelineResultRepository;

import java.util.Optional;
import java.util.UUID;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PipelineStatus.NEW;

public class PipelinePersistence implements IPipelinePersistence {

    private final PipelineDetailsRepository pipelineDetailsRepository;
    private final PipelineResultRepository pipelineResultRepository;

    public PipelinePersistence(final PipelineDetailsRepository pipelineDetailsRepository,
                               final PipelineResultRepository pipelineResultRepository) {
        this.pipelineDetailsRepository = pipelineDetailsRepository;
        this.pipelineResultRepository = pipelineResultRepository;
    }

    @Transactional(transactionManager = "pipelineManagerTransactionManager")
    @Override
    public PipelineDetails createPipeline(final String userId) {
        final String randomUUID = "id-" + UUID.randomUUID();
        final PipelineDetails pipelineDetails = new PipelineDetails(
                randomUUID,
                userId,
                NEW
        );
        return pipelineDetailsRepository.save(pipelineDetails);
    }

    @Transactional(transactionManager = "pipelineManagerTransactionManager")
    @Override
    public void updatePipelineDetailsStatus(final String pipelineId,
                                            final PipelineStatus pipelineStatus) {
        pipelineDetailsRepository
                .findById(pipelineId)
                .ifPresent((pipelineDetails) -> pipelineDetails.setStatus(pipelineStatus));
    }

    @Transactional(transactionManager = "pipelineManagerTransactionManager")
    @Override
    public void persistPipelineResult(final PipelineResultEvent pipelineResultEvent) {
        final PipelineResult pipelineResult = new PipelineResult(
                pipelineResultEvent.getUid(),
                pipelineResultEvent.getOutdir()
        );
        pipelineResultRepository.save(pipelineResult);
    }

    @Override
    public Optional<PipelineResult> getLatestPipelineResult(final String userId) {
        final Optional<PipelineDetails> pipelineDetailsOptional = pipelineDetailsRepository
                .findTopByUserIdAndStatusOrderByUpdatedOnDesc(userId, PipelineStatus.COMPLETED);
        return pipelineDetailsOptional
                .flatMap(pipelineDetails -> pipelineResultRepository.findById(pipelineDetailsOptional.get().getPipelineId()));
    }

    @Override
    public Optional<PipelineResult> pipelineResult(final String pipelineId) {
        return pipelineResultRepository.findById(pipelineId);
    }

    @Override
    public Optional<PipelineDetails> pipelineDetails(final String pipelineId) {
        return pipelineDetailsRepository.findById(pipelineId);
    }
}
