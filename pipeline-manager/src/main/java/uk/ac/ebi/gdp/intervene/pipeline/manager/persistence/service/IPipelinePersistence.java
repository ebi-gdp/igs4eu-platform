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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineStatusDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineResult;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;

public interface IPipelinePersistence {
    Mono<PipelineDetails> createPipeline(String userId, String datasetId);

    Mono<PipelineExecutionStatus> updatePipelineStatus(String pipelineId, PipelineStatus pipelineStatus);

    Mono<PipelineExecutionStatus> updatePipelineStatus(String pipelineId, PipelineStatusDTO pipelineStatusDTO);

    Mono<PipelineResult> persistPipelineResult(PipelineResultEvent pipelineResultEvent);

    Mono<PipelineResult> getPipelineResultRecent(String userId);

    Mono<PipelineResult> getPipelineResult(String userId, String pipelineId);

    Mono<PipelineDetails> getPipeline(String pipelineId, String userId);

    Mono<PipelineDetails> getPipelineFull(String pipelineId, String userId);

    Flux<PipelineDetails> getPipelines(String userId, int offset, int limit);

    Mono<Long> getPipelinesCount(String userId);

    Mono<PipelineDetails> getPipelineFullRecent(String userId);

    Mono<PipelineDetails> save(PipelineDetails pipelineDetails);

    Mono<PipelineExecutionStatus> getPipelineExecutionStatus(String pipelineId);

    Mono<PipelineExecutionStatus> savePipelineExecutionStatus(PipelineExecutionStatus pipelineExecutionStatus);
}
