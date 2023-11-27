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
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.CSCPipelineHandler;

public class PipelineEventListener {
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineEventListener.class);
    private final CSCPipelineHandler cscPipelineHandler;
    private final IPipelinePersistence pipelinePersistence;

    public PipelineEventListener(final CSCPipelineHandler cscPipelineHandler,
                                 final IPipelinePersistence pipelinePersistence) {
        this.cscPipelineHandler = cscPipelineHandler;
        this.pipelinePersistence = pipelinePersistence;
    }

    //@Transactional
    @KafkaListener(
            topics = "${kafka.pipeline-status.topic}",
            clientIdPrefix = "${kafka.group.instance-id}",
            groupId = "${kafka.group.instance-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenPipelineResultQueue(final PipelineResultEvent pipelineResultEvent,
                                          final Acknowledgment acknowledgment) {
        //TODO: Revisit the logic
        switch (PipelineStatus.getPipelineStatusByDescription(pipelineResultEvent.status())) {
            case COMPLETED:
                /*cscPipelineHandler
                        .handlePipelineOutcome(pipelineResultEvent)
                        .doOnSuccess(unused -> LOGGER.info("Pipeline completed, id: {}", pipelineResultEvent.pipelineId()))
                        .doOnError(throwable -> LOGGER.error(String.format("Error while updating status, pipeline completed id: %s", pipelineResultEvent.pipelineId()), throwable))
                        .subscribe();*/
                LOGGER.warn("This is not handled!");
                break;
            case STARTED:
                pipelinePersistence
                        .updatePipelineStatus(
                                pipelineResultEvent.pipelineId(),
                                PipelineStatus.valueOf(pipelineResultEvent.status().toUpperCase()))
                        .subscribe();
            default:
                LOGGER.info("Pipeline id: {}, status: {}", pipelineResultEvent.pipelineId(), pipelineResultEvent.status());
        }
        acknowledgment.acknowledge();
        LOGGER.info("Pipeline result has been saved!");
    }
}
