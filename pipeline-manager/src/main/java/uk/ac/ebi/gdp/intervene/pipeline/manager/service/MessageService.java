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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.TriggerPipelineEvent;

public class MessageService {

    private final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private final KafkaTemplate<String, TriggerPipelineEvent> triggerPipelineEventKT;
    private final String startTopicName;

    public MessageService(final KafkaTemplate<String, TriggerPipelineEvent> triggerPipelineEventKT,
                          final String startTopicName) {
        this.triggerPipelineEventKT = triggerPipelineEventKT;
        this.startTopicName = startTopicName;
    }

    public void sendMessage(final String key, final TriggerPipelineEvent message) {
        triggerPipelineEventKT.send(startTopicName, key, message);
        LOGGER.info("Message has been sent, Key: {}, topic: {}", key, startTopicName);
    }
}
