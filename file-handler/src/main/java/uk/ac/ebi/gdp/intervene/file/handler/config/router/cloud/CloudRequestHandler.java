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
package uk.ac.ebi.gdp.intervene.file.handler.config.router.cloud;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.file.handler.cloud.ICloudStorage;

import static java.util.stream.Collectors.toSet;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;

/**
 * Cloud request handler, defines handlers for router function.
 */
public class CloudRequestHandler {
    private static final String BUCKET_NAME = "bucketName";
    private static final String PATH_PREFIX = "pathPrefix";
    private static final String PATH = "path";
    private final ICloudStorage cloudStorage;

    public CloudRequestHandler(final ICloudStorage cloudStorage) {
        this.cloudStorage = cloudStorage;
    }

    /**
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return set of file names
     */
    public Mono<ServerResponse> listFiles(final ServerRequest serverRequest) {
        final String bucketName = getQueryParamValue(serverRequest, BUCKET_NAME);
        final String pathPrefix = getQueryParamValue(serverRequest, PATH_PREFIX);
        return cloudStorage
                .listFiles(bucketName, pathPrefix)
                .map(streamOfFiles -> streamOfFiles.collect(toSet()))
                .flatMap(setOfFiles -> ok()
                        .bodyValue(setOfFiles));
    }

    /**
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return input stream source of file to download
     */
    public Mono<ServerResponse> streamFileFromBucket(final ServerRequest serverRequest) {
        final String bucketName = getQueryParamValue(serverRequest, BUCKET_NAME);
        final String path = getQueryParamValue(serverRequest, PATH);
        return cloudStorage
                .streamFileFromBucket(bucketName, path)
                .flatMap(inputStreamSource -> ok()
                        .contentType(APPLICATION_OCTET_STREAM)
                        .bodyValue(inputStreamSource));
    }

    private String getQueryParamValue(final ServerRequest serverRequest,
                                      final String paramName) {
        return serverRequest.queryParam(paramName)
                .orElseThrow(() -> badRequest("Query param '%s' is missing!".formatted(paramName)));
    }
}
