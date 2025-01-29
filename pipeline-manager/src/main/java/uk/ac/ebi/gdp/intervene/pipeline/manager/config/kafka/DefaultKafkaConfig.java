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

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_INSTANCE_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static uk.ac.ebi.gdp.intervene.commons.utility.CommonUtil.getJsonObjectMapper;

/**
 * Default kafka config, defines most common properties to integrate with kafka.
 */
public class DefaultKafkaConfig {
    private final KafkaProperties kafkaProperties;
    private final String groupInstanceId;
    private final int maxPollIntervalMs;
    private final long defaultPollTimeout;

    public DefaultKafkaConfig(final KafkaProperties kafkaProperties,
                              final String groupInstanceId,
                              final int maxPollIntervalMs,
                              final long defaultPollTimeout) {
        this.kafkaProperties = kafkaProperties;
        this.groupInstanceId = groupInstanceId;
        this.maxPollIntervalMs = maxPollIntervalMs;
        this.defaultPollTimeout = defaultPollTimeout;
    }

    protected <KEY, MESSAGE> ProducerFactory<KEY, MESSAGE> defaultProducerFactory() {
        final DefaultKafkaProducerFactory<KEY, MESSAGE> factory =
                new DefaultKafkaProducerFactory<>(producerConfigs());
        factory.setValueSerializer(new JsonSerializer<>(getJsonObjectMapper()));
        return factory;
    }

    protected Map<String, Object> producerConfigs() {
        final Map<String, Object> properties = kafkaProperties.buildProducerProperties(null);
        properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //  properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);//TMP
        return properties;
    }

    //Consumer config

    protected <KEY, MESSAGE> KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<KEY, MESSAGE>> defaultKafkaListenerContainerFactory() {
        final ConcurrentKafkaListenerContainerFactory<KEY, MESSAGE> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(1);
        factory.setConsumerFactory(defaultConsumerFactory());
        factory.getContainerProperties().setPollTimeout(defaultPollTimeout);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setRecordMessageConverter(new StringJsonMessageConverter(getJsonObjectMapper()));
        return factory;
    }

    protected <KEY, MESSAGE> ConsumerFactory<KEY, MESSAGE> defaultConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(defaultConsumerConfigs());
    }

    protected Map<String, Object> defaultConsumerConfigs() {
        final Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        properties.put(GROUP_INSTANCE_ID_CONFIG, groupInstanceId);
        properties.put(ENABLE_AUTO_COMMIT_CONFIG, false);
        return properties;
    }
}
