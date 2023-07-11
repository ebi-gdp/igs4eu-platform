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
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.S3ObjectDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

public class PipelineResultHandler {
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

    public Mono<ServerResponse> listResultFiles() {
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence.getLatestPipelineResult(userAccountDTO.getAccountId()))
                .flatMap(pipelineResult -> {
                    final ListObjectsRequest listObjects = new ListObjectsRequest();
                    listObjects.setBucketName(s3Bucket);
                    listObjects.setPrefix(pipelineResult.getPipelineId());
                    final ObjectListing objectListing = s3Client.listObjects(listObjects);
                    final List<String> files = objectListing
                            .getObjectSummaries()
                            .stream()
                            .map(S3ObjectSummary::getKey)
                            .filter(fileName -> fileName.endsWith("log.csv.gz") || fileName.endsWith("scores.txt.gz") || fileName.endsWith("report.html"))
                            .collect(toList());
                    return ok().bodyValue(new S3ObjectDTO(pipelineResult.getPipelineId(), files));
                })
                .switchIfEmpty(status(NOT_FOUND).build());
    }

    public Mono<ServerResponse> streamFileFromS3(final ServerRequest serverRequest) {
        final String path = serverRequest
                .queryParam("path")
                .orElseThrow(RuntimeException::new);
        return userManagerService
                .getUserAccountDetails()
                .flatMap(userAccountDTO -> pipelinePersistence.getPipelineDetails(serverRequest.pathVariable("pipelineId"), userAccountDTO.getAccountId()))
                .flatMap(latestPipelineResult -> {
                    final GetObjectRequest objectRequest = new GetObjectRequest(s3Bucket, path);
                    final InputStreamResource inputStreamResource = new InputStreamResource(s3Client
                            .getObject(objectRequest)
                            .getObjectContent());
                    return ok()
                            .contentType(APPLICATION_OCTET_STREAM)
                            .bodyValue(inputStreamResource);
                })
                .switchIfEmpty(status(NOT_FOUND).build());
    }
}
