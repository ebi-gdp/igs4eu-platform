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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.PipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.nio.file.Path;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/*@RestController
@RequestMapping*/
public class FileDownloadController {

    private final UserManagerService userManagerService;
    private final PipelinePersistence pipelinePersistence;
    private final String s3Bucket;
    private final AmazonS3 s3Client;

    public FileDownloadController(final UserManagerService userManagerService,
                                  final PipelinePersistence pipelinePersistence,
                                  final String s3Bucket,
                                  AmazonS3 s3Client) {
        this.userManagerService = userManagerService;
        this.pipelinePersistence = pipelinePersistence;
        this.s3Bucket = s3Bucket;
        this.s3Client = s3Client;
    }

    /*public Mono<ResponseEntity<InputStreamResource>> streamFileFromS3(*//*final ServerRequest serverRequest*//*) {
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
                    return Mono.just(ResponseEntity.ok()
                            .contentType(APPLICATION_OCTET_STREAM)
                            .header(CONTENT_DISPOSITION, "attachment;filename=" + Path.of(path).getFileName())
                            .body(inputStreamResource));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(NOT_FOUND).build()));
    }*/
}
