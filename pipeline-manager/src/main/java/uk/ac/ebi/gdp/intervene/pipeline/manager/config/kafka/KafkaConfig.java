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
package uk.ac.ebi.gdp.intervene.pipeline.manager.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.mail.javamail.JavaMailSender;
import uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.listener.PipelineEventListener;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.CSCPipelineHandler;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.EmailSender;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

@EnableKafka
@Configuration
public class KafkaConfig extends DefaultKafkaConfig {

    public KafkaConfig(final KafkaProperties kafkaProperties,
                       @Value("${kafka.group.instance-id}") final String groupInstanceId,
                       @Value("${kafka.listener.max.poll.interval.ms}") final int maxPollIntervalMs,
                       @Value("${kafka.listener.default.poll.timeout.ms}") final long defaultPollTimeout) {
        super(kafkaProperties, groupInstanceId, maxPollIntervalMs, defaultPollTimeout);
    }

    @Bean
    public KafkaTemplate<String, TriggerPipelineEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, TriggerPipelineEvent> producerFactory() {
        return defaultProducerFactory();
    }

    @Bean("kafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        return defaultKafkaListenerContainerFactory();
    }

    @Bean
    public IEmailSender emailService(final JavaMailSender mailSender,
                                     final @Value("${spring.mail.username}") String emailFrom) {
        return new EmailSender(mailSender, emailFrom);
    }

    @Bean
    public PipelineEventListener pipelineEventListener(final CSCPipelineHandler cscPipelineHandler,
                                                       final IPipelinePersistence pipelinePersistence) {
        return new PipelineEventListener(cscPipelineHandler, pipelinePersistence);
    }
}
