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
package uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.PipelinePersistence;

public class PipelineEventListener {

    private final Logger LOGGER = LoggerFactory.getLogger(PipelineEventListener.class);

    private final PipelinePersistence pipelinePersistence;

    public PipelineEventListener(final PipelinePersistence pipelinePersistence) {
        this.pipelinePersistence = pipelinePersistence;
    }

    @Transactional(transactionManager = "pipelineManagerTransactionManager")
    @KafkaListener(
            topics = "${kafka.pipeline-status.topic}",
            clientIdPrefix = "${kafka.group.instance-id}",
            groupId = "${kafka.group.instance-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenPipelineResultQueue(final PipelineResultEvent pipelineResultEvent,
                                          final Acknowledgment acknowledgment) {
        switch (PipelineStatus.getPipelineStatusByDescription(pipelineResultEvent.getStatus())) {
            case COMPLETED:
                updatePipelineStatus(pipelineResultEvent);
                pipelinePersistence.persistPipelineResult(pipelineResultEvent);
                LOGGER.info("Pipeline completed, id: {}", pipelineResultEvent.getUid());
                break;
            case STARTED:
                updatePipelineStatus(pipelineResultEvent);
            default:
                LOGGER.info("Pipeline id: {}, status: {}", pipelineResultEvent.getUid(), pipelineResultEvent.getStatus());
        }
        acknowledgment.acknowledge();
        LOGGER.info("Pipeline result has been saved!");
    }

    private void updatePipelineStatus(final PipelineResultEvent pipelineResultEvent) {
        pipelinePersistence.updatePipelineDetailsStatus(
                pipelineResultEvent.getUid(),
                PipelineStatus.valueOf(pipelineResultEvent.getStatus().toUpperCase())
        );
    }
}
