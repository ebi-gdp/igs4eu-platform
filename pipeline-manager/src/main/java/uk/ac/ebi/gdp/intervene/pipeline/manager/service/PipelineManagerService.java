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
import uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.TriggerPipelineEvent;

import java.util.List;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.TriggerPipelineEvent.FormatType;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.TriggerPipelineEvent.NXFParamsFile;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message.TriggerPipelineEvent.TargetGenomes;

public class PipelineManagerService {
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineManagerService.class);

    private final MessageService messageService;

    public PipelineManagerService(final MessageService messageService) {
        this.messageService = messageService;
    }

    public void triggerGeneticScoringPipeline(final String pipelineId) {
        LOGGER.info("Message is being sent to Kafka! : {}", pipelineId);

        messageService.sendMessage(
                pipelineId,
                buildTriggerPipelineEvent(pipelineId)
        );
        LOGGER.info("Message sent to Kafka! : {}", pipelineId);
    }

    private TriggerPipelineEvent buildTriggerPipelineEvent(final String pipelineId) {
        final TriggerPipelineEvent.TargetGenomes targetGenomes = new TargetGenomes(
                "cineca_synthetic_subset",
                "/workspace/data/Synthetic_ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz",
                22
        );
        final NXFParamsFile nxfParamsFile = new NXFParamsFile(
                "/workspace/data/PGS001229_22.txt",
                0.01f,
                "4.GB",
                FormatType.JSON,
                "/workspace/results/" + pipelineId);
        return new TriggerPipelineEvent(
                List.of(targetGenomes),
                nxfParamsFile,
                "/workspace/" + pipelineId + "/",
                pipelineId
        );
    }
}
