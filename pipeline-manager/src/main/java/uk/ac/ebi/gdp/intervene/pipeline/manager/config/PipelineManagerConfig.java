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
package uk.ac.ebi.gdp.intervene.pipeline.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.TriggerPipelineEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.repository.PipelineDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.repository.PipelineResultRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.PipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.MessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;

@Configuration
public class PipelineManagerConfig {

    @Bean
    public IPipelinePersistence pipelinePersistence(final PipelineDetailsRepository pipelineDetailsRepository,
                                                    final PipelineResultRepository pipelineResultRepository) {
        return new PipelinePersistence(
                pipelineDetailsRepository,
                pipelineResultRepository
        );
    }

    @Bean
    public MessageService messageService(final KafkaTemplate<String, TriggerPipelineEvent> triggerPipelineEventKT,
                                         @Value("${kafka.pipeline-trigger.topic}") final String pipelineTriggerTopic) {
        return new MessageService(triggerPipelineEventKT, pipelineTriggerTopic);
    }

    @Bean
    public PipelineManagerService pipelineManagerService(final MessageService messageService) {
        return new PipelineManagerService(messageService);
    }
}
