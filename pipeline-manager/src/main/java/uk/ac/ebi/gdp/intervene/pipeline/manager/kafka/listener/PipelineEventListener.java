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
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineStatusDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.PipelineStatusHandler;

/**
 * Kafka event listener to listen pipeline statuses
 */
public class PipelineEventListener {
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineEventListener.class);
    private final PipelineStatusHandler pipelineStatusHandler;

    public PipelineEventListener(final PipelineStatusHandler pipelineStatusHandler) {
        this.pipelineStatusHandler = pipelineStatusHandler;
    }

    @Transactional
    @KafkaListener(
            topics = "${kafka.pipeline-status.topic}",
            clientIdPrefix = "${kafka.group.instance-id}",
            groupId = "${kafka.group.instance-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenPipelineResultQueue(@Header(KafkaHeaders.KEY) String pipelineIdAsKey,
                                          final PipelineStatusDTO pipelineStatusDTO,
                                          final Acknowledgment acknowledgment) {
        LOGGER.info("Pipeline status is being updated to : {} for Pipeline Id: {}", pipelineStatusDTO.getStatus(), pipelineIdAsKey);
        pipelineStatusHandler
                .updatePipelineStatus(pipelineIdAsKey, pipelineStatusDTO)
                .doOnSuccess(unused -> {
                    acknowledgment.acknowledge();
                    LOGGER.info("Pipeline status has been updated for Pipeline Id: {}", pipelineIdAsKey);
                })
                .block();
    }
}
