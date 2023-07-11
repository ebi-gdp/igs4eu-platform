/*
 *
 * Copyright 2023 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service.message;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.config.kafka.DefaultKafkaConfig;
import uk.ac.ebi.gdp.intervene.pipeline.manager.exception.S3Exception;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;

import java.io.ByteArrayInputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AllasMessageService implements MessageService {
    private final AmazonS3 s3ClientAllas;
    private final String bucketName;

    public AllasMessageService(final AmazonS3 s3ClientAllas,
                               final String bucketName) {
        this.s3ClientAllas = s3ClientAllas;
        this.bucketName = bucketName;
    }

    public Mono<Void> sendMessage(final String key, final TriggerPipelineEvent message) {
        if (!s3ClientAllas.doesBucketExistV2(bucketName)) {
            s3ClientAllas.createBucket(bucketName);
        }
        try {
            s3ClientAllas.putObject(buildObjectRequest(key, message));
        } catch (Exception e) {
            return Mono.error(new S3Exception("Unable to upload file to Allas: " + e.getMessage(), e));
        }
        return Mono.empty();
    }

    private PutObjectRequest buildObjectRequest(final String key,
                                                final TriggerPipelineEvent message) throws JsonProcessingException {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(APPLICATION_JSON_VALUE);
        metadata.addUserMetadata("title", "JSON file for %s pipeline".formatted(key));

        final byte[] messageByteArray = DefaultKafkaConfig
                .getObjectMapper()
                .writeValueAsBytes(message);

        return new PutObjectRequest(
                bucketName,
                "job-queue/%s.json".formatted(key),
                new ByteArrayInputStream(messageByteArray),
                metadata
        );
    }
}
