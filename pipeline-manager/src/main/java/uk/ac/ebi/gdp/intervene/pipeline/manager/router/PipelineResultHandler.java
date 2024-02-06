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
package uk.ac.ebi.gdp.intervene.pipeline.manager.router;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.S3ObjectDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineResult;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

/**
 * Request handler for Pipeline related operations.
 */
public class PipelineResultHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineResultHandler.class);
    private final IPipelinePersistence pipelinePersistence;
    private final UserManagerService userManagerService;
    private final AmazonS3 s3Client;
    private final String s3Bucket;

    public PipelineResultHandler(final IPipelinePersistence pipelinePersistence,
                                 final UserManagerService userManagerService,
                                 final AmazonS3 s3Client,
                                 final String s3Bucket) {
        this.pipelinePersistence = pipelinePersistence;
        this.userManagerService = userManagerService;
        this.s3Client = s3Client;
        this.s3Bucket = s3Bucket;
    }

    /**
     * Retrieves list of results files.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return List of files uploaded on S3 object represented by {@link S3ObjectDTO} Or http status 404(NotFound)
     * @see HttpStatus
     */
    public Mono<ServerResponse> listResultFiles(final ServerRequest serverRequest) {
        LOGGER.info("Listing result files");
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> getPipelineResult(serverRequest, userAccountDTO.accountId()))
                .doOnNext(pipelineResult -> LOGGER.info("Retrieved pipeline result"))
                .flatMap(pipelineResult -> listFilesOnObjectStorage(pipelineResult.getPipelineId())
                        .flatMap(files -> ok().bodyValue(new S3ObjectDTO(pipelineResult.getPipelineId(), files)))
                        .doOnNext(serverResponse -> LOGGER.info("Result file(s) have been successfully retrieved from S3 object storage")))
                .switchIfEmpty(status(NOT_FOUND)
                        .build()
                        .doOnNext(serverResponse -> LOGGER.info("File(s) not found!")));
    }

    private Mono<PipelineResult> getPipelineResult(final ServerRequest serverRequest,
                                                   final String accountId) {
        final Optional<String> pipelineIdOptional = serverRequest.queryParam("pipelineId");
        if (pipelineIdOptional.isPresent()) {
            return pipelinePersistence.getPipelineResult(pipelineIdOptional.get(), accountId);
        } else {
            return pipelinePersistence.getPipelineResultRecent(accountId);
        }
    }

    private Mono<Collection<String>> listFilesOnObjectStorage(final String pipelineId) {
        final ListObjectsRequest listObjects = new ListObjectsRequest();
        listObjects.setBucketName(s3Bucket);
        listObjects.setPrefix(pipelineId);
        final ObjectListing objectListing = s3Client.listObjects(listObjects);
        return Mono.just(objectListing
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .filter(fileName -> fileName.endsWith("log.csv.gz") || fileName.endsWith("scores.txt.gz") || fileName.endsWith("report.html"))
                .collect(toList()));
    }

    /**
     * Streams file to be downloaded, retrieves file from S3 object storage.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return {@link InputStreamResource} Or http status 404(NotFound)
     * @see HttpStatus
     */
    public Mono<ServerResponse> streamFileFromS3(final ServerRequest serverRequest) {
        LOGGER.info("Streaming file from S3 object storage");
        final String path = serverRequest
                .queryParam("path")
                .orElseThrow(RuntimeException::new);
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence
                        .getPipeline(serverRequest.pathVariable("pipelineId"), userAccountDTO.accountId())
                        .doOnNext(ignorePipelineResult -> LOGGER.info("Retrieved pipeline result")))
                .flatMap(ignorePipelineResult -> streamFileFromS3Bucket(path))
                .doOnNext(serverResponse -> LOGGER.info("File found on S3 object storage & is being streamed"))
                .switchIfEmpty(status(NOT_FOUND)
                        .build()
                        .doOnNext(serverResponse -> LOGGER.info("File not found on S3 object storage!")));
    }

    private Mono<ServerResponse> streamFileFromS3Bucket(final String path) {
        final GetObjectRequest objectRequest = new GetObjectRequest(s3Bucket, path);
        final InputStreamResource inputStreamResource = new InputStreamResource(s3Client
                .getObject(objectRequest)
                .getObjectContent());
        return ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .bodyValue(inputStreamResource);
    }
}
