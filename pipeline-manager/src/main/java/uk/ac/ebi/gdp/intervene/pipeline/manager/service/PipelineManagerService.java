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
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirReqDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirResDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.GlobusDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.MessageService;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.Paths.get;
import static java.util.List.of;
import static java.util.stream.Collectors.toMap;
import static reactor.core.publisher.Mono.defer;
import static uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO.GlobusFileDetails;
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

    public Mono<GlobusDetails> createOrUpdateGlobusRecord(final String globusUsername,
                                                          final String guestCollectionId,
                                                          final Path dirPathOnGuestCollection) {
        return globusDetailsRepository
                .findByDirPathOnGuestCollectionEndsWith(dirPathOnGuestCollection.toString())
                .flatMap(globusDetails -> {
                    globusDetails.updateGlobusUsername(globusUsername);
                    return globusDetailsRepository.save(globusDetails);
                })
                .switchIfEmpty(defer(() -> globusDetailsRepository
                        .getNextFilesetId()
                        .map(nextFilesetId ->
                                newRecord(
                                        nextFilesetId,
                                        globusUsername,
                                        guestCollectionId,
                                        dirPathOnGuestCollection
                                ))
                        .flatMap(globusDetailsRepository::save))
                );
    }

    public Mono<Void> triggerGeneticScoringPipeline(final PipelineDetails pipelineDetails,
                                                    final String polygenicScoreIds) {
        final Set<FileDetails> files = new HashSet<>();
        return globusFileHandlerService
                .listFilesOnGuestCollection(get(pipelineDetails.getDatasetDetails().getGlobusDetails().getGlobusUsername()
                        + pipelineDetails.getDatasetDetails().getGlobusDetails().getDirPathOnGuestCollection()))
                .map(globusFileDetailsWrapperDTO -> globusFileDetailsWrapperDTO
                        .getFileDetailsList()
                        .stream()
                        .peek(globusFileDetails -> files
                                .add(new FileDetails(globusFileDetails.getFileName(),
                                        globusFileDetails.getSize())))
                        .collect(toMap(GlobusFileDetails::getProperty, GlobusFileDetails::getFileName)))
                .map(stringStringMap -> {
                    stringStringMap.put("sampleset", pipelineDetails.getDatasetDetails().getDatasetName());
                    stringStringMap.put("chrom", null);
                    return of(stringStringMap);
                })
                .flatMap(targetGenomes -> messageService
                        .sendMessage(pipelineDetails.getPipelineId(), buildTriggerPipelineEvent(pipelineDetails, polygenicScoreIds, targetGenomes, files))
                        .doOnRequest(unused -> LOGGER.info("Message is being sent! : {}", pipelineDetails.getPipelineId()))
                        .doOnSuccess(unused -> LOGGER.info("Message sent! : {}", pipelineDetails.getPipelineId()))
                );
    }

    private TriggerPipelineEvent buildTriggerPipelineEvent(final PipelineDetails pipelineDetails,
                                                           final String polygenicScoreIds,
                                                           final Collection<Map<String, String>> targetGenomes,
                                                           final Set<FileDetails> files) {
        return new TriggerPipelineEvent(
                buildPipelineParam(pipelineDetails, polygenicScoreIds, targetGenomes),
                new GuestCollectionDirResDTO(
                        pipelineDetails.getDatasetDetails().getGlobusDetails().getGuestCollectionId(),
                        pipelineDetails.getDatasetDetails().getGlobusDetails().getGlobusUsername()
                                + pipelineDetails.getDatasetDetails().getGlobusDetails().getDirPathOnGuestCollection(),
                        files
                )
        );
    }

    private PipelineParam buildPipelineParam(final PipelineDetails pipelineDetails,
                                             final String polygenicScoreIds,
                                             final Collection<Map<String, String>> targetGenomes) {
        final NXFParamsFile nxfParamsFile = new NXFParamsFile(
                polygenicScoreIds,
                FormatType.JSON,
                pipelineDetails.getDatasetDetails().getGenomeBuild().getGenomeBuildValue());
        return new PipelineParam(
                targetGenomes,
                nxfParamsFile,
                "/workspace/work/",
                pipelineDetails.getPipelineId()
        );
    }
}
