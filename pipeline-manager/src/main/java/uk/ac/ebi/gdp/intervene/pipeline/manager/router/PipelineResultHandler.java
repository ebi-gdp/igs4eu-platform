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
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.ICloudStorage;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;

/**
 * Request handler for Pipeline related operations.
 */
public class PipelineResultHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineResultHandler.class);
    private final IPipelinePersistence pipelinePersistence;
    private final UserManagerService userManagerService;
    private final ICloudStorage cloudStorage;
    private final String bucketNameFormat;
    private final String bucketFilePrefix;

    public PipelineResultHandler(final IPipelinePersistence pipelinePersistence,
                                 final UserManagerService userManagerService,
                                 final ICloudStorage cloudStorage,
                                 final String bucketNameFormat,
                                 final String bucketFilePrefix) {
        this.pipelinePersistence = pipelinePersistence;
        this.userManagerService = userManagerService;
        this.cloudStorage = cloudStorage;
        this.bucketNameFormat = bucketNameFormat;
        this.bucketFilePrefix = bucketFilePrefix;
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
                .flatMap(pipelineResult -> pipelinePersistence
                        .getDatasetName(pipelineResult.getPipelineId())
                        .flatMap(datasetDetails -> storageFiles(bucketNameFormat.formatted(pipelineResult.getPipelineId()),
                                bucketFilePrefix.formatted(datasetDetails.getDatasetName()))
                                .flatMap(files -> ok().bodyValue(new S3ObjectDTO(pipelineResult.getPipelineId(), files)))
                                .doOnNext(serverResponse -> LOGGER.info("Result file(s) have been successfully retrieved from S3 object storage"))))
                .switchIfEmpty(status(NOT_FOUND)
                        .build()
                        .doOnNext(serverResponse -> LOGGER.info("File(s) not found!")));
    }

    private Mono<Set<String>> storageFiles(final String bucketName,
                                           final String pathPrefix) {
        return cloudStorage
                .listFiles(bucketName, pathPrefix)
                .map(stringStream -> stringStream.collect(toSet()));
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

    /**
     * Streams file to be downloaded, retrieves file from S3 object storage.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return {@link InputStreamResource} Or http status 404(NotFound)
     * @see HttpStatus
     */
    public Mono<ServerResponse> streamFileFromCloudStorage(final ServerRequest serverRequest) {
        LOGGER.info("Streaming file from Cloud object storage");
        final String path = serverRequest
                .queryParam("path")
                .orElseThrow(() -> badRequest("Query param 'Path' is missing!"));
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence
                        .getPipeline(serverRequest.pathVariable("pipelineId"), userAccountDTO.accountId())
                        .doOnNext(ignorePipelineDetails -> LOGGER.info("Retrieved pipeline details")))
                .flatMap(ignorePipelineResult -> cloudStorage
                        .streamFileFromBucket(bucketNameFormat
                                .formatted(ignorePipelineResult.getPipelineId()), path))
                .flatMap(inputStreamSource -> ok()
                        .contentType(APPLICATION_OCTET_STREAM)
                        .bodyValue(inputStreamSource))
                .doOnNext(serverResponse -> LOGGER.info("File found on Cloud object storage! Now being streamed"))
                .switchIfEmpty(status(NOT_FOUND)
                        .build()
                        .doOnNext(serverResponse -> LOGGER.info("File not found on S3 object storage!")));
    }
}
