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
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirResDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.MessageService;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.nio.file.Paths.get;
import static java.util.List.of;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO.GlobusFileDetails;
import static uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirResDTO.FileDetails;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.TargetGenomeFileType.GENO;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.TargetGenomeFileType.PHENO;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.TargetGenomeFileType.VARIANTS;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.TargetGenomeFileType.VCF_GZ;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.TargetGenomeFileType.getPropertyName;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam.FormatType.JSON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam.NXFParamsFile;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam.NXFParamsFile.createWithPgsIds;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam.NXFParamsFile.createWithPublicationIds;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineParam.NXFParamsFile.createWithTraitIds;

/**
 * Pipeline manager service, core service to perform Pipeline operations.
 */
public class PipelineManagerService {
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineManagerService.class);
    private final MessageService messageService;
    private final GlobusManagerService globusManagerService;

    public PipelineManagerService(final MessageService messageService,
                                  final GlobusManagerService globusManagerService) {
        this.messageService = messageService;
        this.globusManagerService = globusManagerService;
    }

    /**
     * Triggers pipeline with PGS Ids.
     *
     * @param pipelineDetails {@link PipelineDetails}
     * @param pgsIds polygenic score ids
     *
     * @return {@link Void}
     */
    public Mono<Void> triggerGeneticScoringPipelineWithPgsIds(final PipelineDetails pipelineDetails,
                                                              final Set<String> pgsIds) {
        final NXFParamsFile nxfParamsFile = buildWithPgsIds(
                String.join(",", pgsIds),
                pipelineDetails.getDatasetDetails().getGenomeBuild());
        return triggerGeneticScoringPipeline(pipelineDetails, nxfParamsFile);
    }

    /**
     * Triggers pipeline with PGS Trait Ids.
     *
     * @param pipelineDetails {@link PipelineDetails}
     * @param traitIds trait ids
     *
     * @return {@link Void}
     */
    public Mono<Void> triggerGeneticScoringPipelineWithTraitIds(final PipelineDetails pipelineDetails,
                                                                final Set<String> traitIds) {
        final NXFParamsFile nxfParamsFile = buildWithTraitIds(
                String.join(",", traitIds),
                pipelineDetails.getDatasetDetails().getGenomeBuild());
        return triggerGeneticScoringPipeline(pipelineDetails, nxfParamsFile);
    }

    /**
     * Triggers pipeline with Publication Ids.
     *
     * @param pipelineDetails {@link PipelineDetails}
     * @param publicationIds publication ids
     *
     * @return {@link Void}
     */
    public Mono<Void> triggerGeneticScoringPipelineWithPublicationIds(final PipelineDetails pipelineDetails,
                                                                      final Set<String> publicationIds) {
        final NXFParamsFile nxfParamsFile = buildWithPublicationIds(
                String.join(",", publicationIds),
                pipelineDetails.getDatasetDetails().getGenomeBuild());
        return triggerGeneticScoringPipeline(pipelineDetails, nxfParamsFile);
    }

    private Mono<Void> triggerGeneticScoringPipeline(final PipelineDetails pipelineDetails,
                                                     final NXFParamsFile nxfParamsFile) {
        final Set<FileDetails> files = new HashSet<>();
        return globusManagerService
                .listFilesOnGuestCollection(buildDirPath(pipelineDetails))
                .map(globusFileDetailsWrapperDTO -> buildMapFromGlobusFiles(globusFileDetailsWrapperDTO, files))
                .map(propertiesMap -> {
                    addProperties(propertiesMap, pipelineDetails.getDatasetDetails().getDatasetName());
                    return of(propertiesMap);
                })
                .flatMap(targetGenomes -> submitPipelineRequest(pipelineDetails, nxfParamsFile, targetGenomes, files));
    }

    private Path buildDirPath(final PipelineDetails pipelineDetails) {
        return get(pipelineDetails
                .getDatasetDetails()
                .getGlobusDetails()
                .getGlobusUsername() + pipelineDetails
                .getDatasetDetails()
                .getGlobusDetails()
                .getDirPathOnGuestCollection());
    }

    private Map<String, Object> buildMapFromGlobusFiles(final GlobusFileDetailsWrapperDTO globusFileDetailsWrapperDTO,
                                                        final Set<FileDetails> files) {
        final Stream<GlobusFileDetails> globusFileDetailsStream = collectGlobusFiles(globusFileDetailsWrapperDTO.getFileDetailsList(), files);
        final String globusFileName = globusFileDetailsWrapperDTO.getFileDetailsList().get(0).getFileName();
        if (globusFileName.endsWith(VCF_GZ)) {
            globusFileDetailsStream
                    .forEach(element -> {
                    });
            return buildVcfMap(globusFileName);
        } else {
            return globusFileDetailsStream
                    .collect(toMap(globusFileDetails -> getPropertyName(globusFileDetails.getFileName()), GlobusFileDetails::getFileName));
        }
    }

    private void addProperties(final Map<String, Object> propertiesMap,
                               final String datasetName) {
        propertiesMap.put("sampleset", datasetName);
        propertiesMap.put("chrom", null);
        propertiesMap.put("vcf_import_dosage", FALSE);
        propertiesMap.put("format", getFileFormat(getExtension(propertiesMap.get("geno").toString())));
    }

    private String getFileFormat(final String genoFileNameExtension) {
        if (genoFileNameExtension.startsWith("p")) {
            return "pfile";
        } else if (genoFileNameExtension.startsWith("b")) {
            return "bfile";
        } else {
            return "vcf";
        }
    }

    private Stream<GlobusFileDetails> collectGlobusFiles(final List<GlobusFileDetails> globusFileDetailsList,
                                                         final Set<FileDetails> files) {
        return globusFileDetailsList
                .stream()
                .peek(globusFileDetails -> files
                        .add(new FileDetails(globusFileDetails.getFileName(),
                                globusFileDetails.getSize())));
    }

    private Map<String, Object> buildVcfMap(final String globusFileName) {
        final Map<String, Object> globusFilesMap = new HashMap<>(3);
        globusFilesMap.put(GENO, globusFileName);
        globusFilesMap.put(PHENO, globusFileName);
        globusFilesMap.put(VARIANTS, globusFileName);
        return globusFilesMap;
    }

    private Mono<Void> submitPipelineRequest(final PipelineDetails pipelineDetails,
                                             final NXFParamsFile nxfParamsFile,
                                             final Collection<Map<String, Object>> targetGenomes,
                                             final Set<FileDetails> files) {
        final PipelineParam pipelineParam = buildPipelineParam(pipelineDetails.getPipelineId(), nxfParamsFile, targetGenomes);
        return messageService
                .sendMessage(pipelineDetails.getPipelineId(), buildTriggerPipelineEvent(pipelineDetails, pipelineParam, files))
                .doOnRequest(unused -> LOGGER.info("Message is being sent! : {}", pipelineDetails.getPipelineId()))
                .doOnSuccess(unused -> LOGGER.info("Message sent! : {}", pipelineDetails.getPipelineId()));
    }

    private TriggerPipelineEvent buildTriggerPipelineEvent(final PipelineDetails pipelineDetails,
                                                           PipelineParam pipelineParam,
                                                           final Set<FileDetails> files) {
        return new TriggerPipelineEvent(
                pipelineParam,
                new GuestCollectionDirResDTO(
                        pipelineDetails.getDatasetDetails().getGlobusDetails().getGlobusUsername()
                                + pipelineDetails.getDatasetDetails().getGlobusDetails().getDirPathOnGuestCollection(),
                        files
                )
        );
    }

    private PipelineParam buildPipelineParam(final String pipelineId,
                                             final NXFParamsFile nxfParamsFile,
                                             final Collection<Map<String, Object>> targetGenomes) {
        return new PipelineParam(
                targetGenomes,
                nxfParamsFile,
                pipelineId
        );
    }

    private NXFParamsFile buildWithPgsIds(final String polygenicScoreIds,
                                          final GenomeBuild genomeBuild) {
        return createWithPgsIds(
                polygenicScoreIds,
                JSON,
                genomeBuild.getGenomeBuildValue());
    }

    private NXFParamsFile buildWithTraitIds(final String polygenicTraitIds,
                                            final GenomeBuild genomeBuild) {
        return createWithTraitIds(
                polygenicTraitIds,
                JSON,
                genomeBuild.getGenomeBuildValue());
    }

    private NXFParamsFile buildWithPublicationIds(final String publicationIds,
                                                  final GenomeBuild genomeBuild) {
        return createWithPublicationIds(
                publicationIds,
                JSON,
                genomeBuild.getGenomeBuildValue());
    }
}
