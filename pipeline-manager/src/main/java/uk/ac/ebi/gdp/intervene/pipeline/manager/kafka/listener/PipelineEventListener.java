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
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.invocation.MethodArgumentResolutionException;
import org.springframework.retry.annotation.Backoff;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.exception.ClientException;
import uk.ac.ebi.gdp.intervene.commons.exception.ServerException;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineStatusDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.exception.MailException;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.PipelineStatusHandler;

import static org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

/**
 * Kafka event listener to listen pipeline statuses
 */
public class PipelineEventListener {
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineEventListener.class);
    private final PipelineStatusHandler pipelineStatusHandler;

    public PipelineEventListener(final PipelineStatusHandler pipelineStatusHandler) {
        this.pipelineStatusHandler = pipelineStatusHandler;
    }

    @RetryableTopic(
            attempts = "2",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),  // Delay and backoff multiplier
            kafkaTemplate = "retryableTopicKafkaTemplate",
            dltStrategy = FAIL_ON_ERROR,
            exclude = {DeserializationException.class,
                    MessageConversionException.class,
                    ConversionException.class,
                    MethodArgumentResolutionException.class,
                    NoSuchMethodException.class,
                    ClassCastException.class,
                    ServerException.class})
    @KafkaListener(
            topics = "${kafka.pipeline-status.topic}",
            clientIdPrefix = "${kafka.group.instance-id}",
            groupId = "${kafka.group.instance-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenPipelineResultQueue(final PipelineStatusDTO pipelineStatusDTO,
                                          final Acknowledgment acknowledgment) {
        LOGGER.info("Pipeline status is being updated to : {} for Pipeline Id: {}", pipelineStatusDTO.getStatus(), pipelineStatusDTO.getRunName());
        updatePipelineStatus(pipelineStatusDTO, acknowledgment)
                .subscribe();
    }

    private Mono<Void> updatePipelineStatus(final PipelineStatusDTO pipelineStatusDTO,
                                            final Acknowledgment acknowledgment) {
        return pipelineStatusHandler
                .updatePipelineStatus(pipelineStatusDTO.getRunName(), pipelineStatusDTO)
                .doOnSuccess(unused -> {
                    acknowledgment.acknowledge();
                    LOGGER.info("Pipeline status has been updated for Pipeline Id: {}", pipelineStatusDTO.getRunName());
                })
                .doOnError(throwable -> {
                    if (throwable instanceof ClientException || throwable instanceof MailException) {
                        acknowledgment.acknowledge();
                    }
                    LOGGER.error("Error while updating status for Pipeline Id: {}", pipelineStatusDTO.getRunName());
                });
    }

    @DltHandler
    public void handleDltPipelineStatus(@Header(RECEIVED_TOPIC) String topic,
                                        final String pipelineStatusDTO) {
        LOGGER.info("Event on dlt topic={}, payload={}", topic, pipelineStatusDTO);
    }
}
