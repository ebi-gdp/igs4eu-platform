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
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineResult;

/**
 * Database operations layer.
 */
public interface IPipelinePersistence {
    /**
     * Creates pipeline.
     *
     * @param userId platform user id
     * @param datasetId dataset id
     *
     * @return pipeline details represented by {@link PipelineDetails}
     */
    Mono<PipelineDetails> createPipeline(String userId, String datasetId);

    /**
     * Update pipeline status.
     *
     * @param pipelineId pipeline id
     * @param pipelineStatusDTO {@link PipelineStatusDTO}
     *
     * @return pipeline execution status represented by {@link PipelineExecutionStatus}
     */
    Mono<PipelineExecutionStatus> updatePipelineStatus(String pipelineId, PipelineStatusDTO pipelineStatusDTO);

    /**
     * Persist pipeline result.
     *
     * @param pipelineResultEvent {@link PipelineResultEvent}
     *
     * @return pipeline result represented by {@link PipelineResult}
     */
    Mono<PipelineResult> createPipelineResult(PipelineResultEvent pipelineResultEvent);

    /**
     * Retrieves recent pipeline result.
     *
     * @param userId Intervene platform user id
     *
     * @return pipeline result represented by {@link PipelineResult}
     */
    Mono<PipelineResult> getPipelineResultRecent(String userId);

    /**
     * Retrieves pipeline result for pipeline id.
     *
     * @param pipelineId pipeline id
     * @param userId Intervene platform user id
     *
     * @return pipeline result represented by {@link PipelineResult}
     */
    Mono<PipelineResult> getPipelineResult(String pipelineId, String userId);

    /**
     * Retrieves pipeline details for pipeline id.
     *
     * @param pipelineId pipeline id
     * @param userId Intervene platform user id
     *
     * @return pipeline details represented by {@link PipelineDetails}
     */
    Mono<PipelineDetails> getPipeline(String pipelineId, String userId);

    /**
     * Retrieves complete pipeline details for pipeline id,
     * includes all dependent entities.
     *
     * @param pipelineId pipeline id
     * @param userId Intervene platform user id
     *
     * @return pipeline details represented by {@link PipelineDetails}
     */
    Mono<PipelineDetails> getPipelineFull(String pipelineId, String userId);

    /**
     * Retrieves pipeline details without dependent related entities.
     *
     * @param userId Intervene platform user id
     * @param offset db record offset
     * @param limit no of records to limit
     *
     * @return pipeline details represented by {@link PipelineDetails}
     */
    Flux<PipelineDetails> getPipelines(String userId, int offset, int limit);

    /**
     * Counts no. of pipelines.
     *
     * @param userId Intervene platform user id
     *
     * @return no. of records represented by {@link Long}
     */
    Mono<Long> getPipelinesCount(String userId);

    /**
     * Retrieves complete recent pipeline details,
     * includes all dependent entities.
     *
     * @param userId Intervene platform user id
     *
     * @return pipeline details represented by {@link PipelineDetails}
     */
    Mono<PipelineDetails> getPipelineFullRecent(String userId);

    /**
     * Persist pipeline details.
     *
     * @param pipelineDetails {link PipelineDetails}
     *
     * @return pipeline details represented by {@link PipelineDetails}
     */
    Mono<PipelineDetails> save(PipelineDetails pipelineDetails);

    /**
     * Retrieves pipeline execution status.
     *
     * @param pipelineId pipeline id
     *
     * @return pipeline execution status represented by {@link PipelineExecutionStatus}
     */
    Mono<PipelineExecutionStatus> getPipelineExecutionStatus(String pipelineId);

    /**
     * Persist pipeline execution status.
     *
     * @param pipelineExecutionStatus {@link PipelineExecutionStatus}
     *
     * @return pipeline execution status represented by {@link PipelineExecutionStatus}
     */
    Mono<PipelineExecutionStatus> savePipelineExecutionStatus(PipelineExecutionStatus pipelineExecutionStatus);

    /**
     * Get dataset name for given pipeline id.
     *
     * @param pipelineId pipeline id
     *
     * @return dataset details (contains only id & name) represented by {@link DatasetDetails}
     */
    Mono<DatasetDetails> getDatasetName(String pipelineId);

    /**
     * Get pipelines for current day.
     *
     * @param userId user id
     *
     * @return {@link Flux} of {@link PipelineDetails}
     */
    Flux<PipelineDetails> getPipelinesForToday(String userId);
}
