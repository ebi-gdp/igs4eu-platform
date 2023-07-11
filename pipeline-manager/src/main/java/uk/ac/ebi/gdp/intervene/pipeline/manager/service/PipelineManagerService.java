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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirReqDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirResDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.GlobusDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineExecutionDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.MessageService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.List.of;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirResDTO.FileDetails;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam.FormatType;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam.NXFParamsFile;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusDetails.newRecord;

public class PipelineManagerService {
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineManagerService.class);
    private final MessageService messageService;
    private final GlobusFileHandlerService globusFileHandlerService;
    private final GlobusDetailsRepository globusDetailsRepository;
    private final GlobusUserRepository globusUserRepository;

    public PipelineManagerService(final MessageService messageService,
                                  final GlobusFileHandlerService globusFileHandlerService,
                                  final GlobusDetailsRepository globusDetailsRepository,
                                  final GlobusUserRepository globusUserRepository) {
        this.messageService = messageService;
        this.globusFileHandlerService = globusFileHandlerService;
        this.globusDetailsRepository = globusDetailsRepository;
        this.globusUserRepository = globusUserRepository;
    }

    public Mono<GlobusDetailsDTO> createDirectoryOnGuestCollection(final Path directoryName,
                                                                   final String username) {//TODO: handle exceptions
        return globusUserRepository
                .findById(username)//TODO: handle 404
                .map(globusUserDetails -> new GuestCollectionDirReqDTO(
                        directoryName.toString(),
                        globusUserDetails.getUserUID(),
                        globusUserDetails.getUsername()))
                .flatMap(globusFileHandlerService::createDirectoryOnGuestCollection);
    }

    public Mono<GlobusDetails> createGlobusRecord(final String globusUsername,
                                                  final String guestCollectionId,
                                                  final Path dirPathOnGuestCollection) {
        return globusDetailsRepository
                .getNextFilesetId()
                .map(nextFilesetId ->
                        newRecord(
                                nextFilesetId,
                                globusUsername,
                                guestCollectionId,
                                dirPathOnGuestCollection
                        ))
                .flatMap(globusDetailsRepository::save);
    }

    public Mono<Void> triggerGeneticScoringPipeline(final String pipelineId,
                                                    final PipelineExecutionDTO pipelineExecutionDTO) {
        final Set<FileDetails> files = new HashSet<>();
        return globusFileHandlerService
                .listFilesOnGuestCollection(Paths.get(pipelineExecutionDTO.globusDetails().getDirPathOnGuestCollection()))
                .map(globusFileDetailsWrapperDTO -> globusFileDetailsWrapperDTO
                        .getData()
                        .stream()
                        .peek(globusFileDetails -> files
                                .add(new FileDetails(globusFileDetails.getFileName(),
                                        globusFileDetails.getSize())))
                        .collect(toMap(GlobusFileDetailsWrapperDTO.GlobusFileDetails::getProperty, GlobusFileDetailsWrapperDTO.GlobusFileDetails::getFileName)))
                .map(stringStringMap -> {
                    stringStringMap.put("sampleset", pipelineExecutionDTO.sampleSetName());
                    stringStringMap.put("chrom", null);
                    return of(stringStringMap);
                })
                .flatMap(targetGenomes -> messageService
                        .sendMessage(pipelineId, buildTriggerPipelineEvent(targetGenomes, files, pipelineExecutionDTO, pipelineId))
                        .doOnRequest(unused -> LOGGER.info("Message is being sent! : {}", pipelineId))
                        .doOnSuccess(unused -> LOGGER.info("Message sent! : {}", pipelineId))
                );
    }

    private TriggerPipelineEvent buildTriggerPipelineEvent(final Collection<Map<String, String>> targetGenomes,
                                                           final Set<FileDetails> files,
                                                           final PipelineExecutionDTO pipelineExecutionDTO,
                                                           final String pipelineId) {
        return new TriggerPipelineEvent(
                buildPipelineParam(targetGenomes, pipelineExecutionDTO, pipelineId),
                new GuestCollectionDirResDTO(
                        pipelineExecutionDTO.globusDetails().getGuestCollectionId(),
                        pipelineExecutionDTO.globusDetails().getDirPathOnGuestCollection(),
                        files
                )
        );
    }

    private PipelineParam buildPipelineParam(final Collection<Map<String, String>> targetGenomes,
                                             final PipelineExecutionDTO pipelineExecutionDTO,
                                             final String pipelineId) {
        final NXFParamsFile nxfParamsFile = new NXFParamsFile(
                pipelineExecutionDTO.polygenicScoreIds(),
                FormatType.JSON,
                GenomeBuild.valueOf(pipelineExecutionDTO.genomeBuild().toUpperCase()).getGenomeBuildValue());
        return new PipelineParam(
                targetGenomes,
                nxfParamsFile,
                "/workspace/work/",
                pipelineId
        );
    }
}
