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

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

/**
 * Service class to handle cloud files.
 */
public class CloudFileHandlerService {
    private final WebClient fileHandlerWebClient;
    private final URI listFilesOnCloudURI;
    private final URI streamFileFromCloudURI;
    private static final String BUCKET_NAME = "bucketName";
    private static final String PATH_PREFIX = "pathPrefix";
    private static final String PATH = "path";

    public CloudFileHandlerService(final WebClient fileHandlerWebClient,
                                   final URI listFilesOnCloudURI,
                                   final URI streamFileFromCloudURI) {
        this.fileHandlerWebClient = fileHandlerWebClient;
        this.listFilesOnCloudURI = listFilesOnCloudURI;
        this.streamFileFromCloudURI = streamFileFromCloudURI;
    }

    /**
     * List files on specified bucket & path
     *
     * @param bucketName bucket name
     * @param pathPrefix path prefix
     *
     * @return set of file names
     */
    public Mono<Set<String>> listFiles(final String bucketName,
                                       final String pathPrefix) {
        return fileHandlerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(listFilesOnCloudURI.getPath())
                        .queryParam(BUCKET_NAME, bucketName)
                        .queryParam(PATH_PREFIX, pathPrefix)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Stream files from bucket
     *
     * @param bucketName bucket name
     * @param path file path on bucket
     *
     * @return Flux of {@link DataBuffer}
     */
    public Flux<DataBuffer> streamFileFromBucket(final String bucketName,
                                                 final String path) {
        return fileHandlerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(streamFileFromCloudURI.getPath())
                        .queryParam(BUCKET_NAME, bucketName)
                        .queryParam(PATH, path)
                        .build())
                .retrieve()
                .bodyToFlux(DataBuffer.class);
    }
}
