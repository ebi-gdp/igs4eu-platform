/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

/**
 * S3 object storage implementation e.g. Amazon S3 or other S3 providers.
 */
public class S3CloudStorage implements ICloudStorage {
    private final AmazonS3 s3Client;

    public S3CloudStorage(final AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * List files on S3 storage.
     * {@inheritDoc}
     */
    @Override
    public Mono<Stream<String>> listFiles(final String bucketName,
                                          final String pathPrefix) {
        final ListObjectsRequest listObjects = new ListObjectsRequest();
        listObjects.setBucketName(bucketName);
        listObjects.setPrefix(pathPrefix);
        final ObjectListing objectListing = s3Client.listObjects(listObjects);
        return Mono.just(objectListing
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .filter(ICloudStorage::isValidFileForDownload)
                .parallel());
    }

    /**
     * Stream file on S3 storage.
     * {@inheritDoc}
     */
    public Mono<InputStreamSource> streamFileFromBucket(final String bucketName,
                                                        final String path) {
        final GetObjectRequest objectRequest = new GetObjectRequest(bucketName, path);
        return Mono.just(new InputStreamResource(s3Client
                .getObject(objectRequest)
                .getObjectContent()));
    }
}
