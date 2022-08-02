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
package uk.ac.ebi.gdp.intervene.pipeline.manager.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineResultDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PipelineResult;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@RestController
public class PipelineController {

    private final PipelineManagerService pipelineManagerService;
    private final IPipelinePersistence pipelinePersistence;

    public PipelineController(final PipelineManagerService pipelineManagerService,
                              final IPipelinePersistence pipelinePersistence) {
        this.pipelineManagerService = pipelineManagerService;
        this.pipelinePersistence = pipelinePersistence;
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> executePipeline(@PathVariable long id,
                                                  @AuthenticationPrincipal Jwt principal) {
        final String userId = principal.getSubject();
        final PipelineDetails pipelineDetails = pipelinePersistence.createPipeline(userId);
        pipelineManagerService.triggerGeneticScoringPipeline(pipelineDetails.getPipelineId());
        return ResponseEntity.ok("Request received!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<PipelineResultDTO> getPipelineResults(@PathVariable String id) {
        final Optional<PipelineDetails> pipelineDetailsOptional = pipelinePersistence.pipelineDetails(id);
        final Optional<PipelineResult> pipelineResultOptional = pipelinePersistence.pipelineResult(id);
        if (pipelineResultOptional.isPresent()) {
            final PipelineDetails pipelineDetails = pipelineDetailsOptional.get();
            final PipelineResult pipelineResult = pipelineResultOptional.get();
            final PipelineResultDTO pipelineResultDTO = new PipelineResultDTO(
                    pipelineResult.getPipelineId(),
                    pipelineResult.getFileDownloadPath(),
                    pipelineDetails.getStatus().getPipelineStatus()
            );
            return ResponseEntity.ok(pipelineResultDTO);
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @GetMapping("/download")
    public ResponseEntity<?> getFile(@AuthenticationPrincipal Jwt principal) throws IOException {
        final String userId = principal.getSubject();
        final Optional<PipelineResult> latestPipelineResult = pipelinePersistence.getLatestPipelineResult(userId);

        if (latestPipelineResult.isPresent()) {
            final String fileDownloadPath = latestPipelineResult.get().getFileDownloadPath() + "/make/report.html";
            final File file = new File(fileDownloadPath);
            final InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    // Content-Disposition
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                    // Content-Type
                    .contentType(MediaType.TEXT_HTML)
                    // Content-Length
                    .contentLength(file.length()) //
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
