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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;

/**
 * Custom repository to handle {@link PipelineDetails} data
 */
public interface CustomPipelineDetailsRepository {
    /**
     * Returns most recent pipeline details & its dependents
     *
     * @param userId data to be retrieved for
     *
     * @return {@link PipelineDetails}
     */
    Mono<PipelineDetails> findPipelineDetailsFullRecent(String userId);

    /**
     * Returns pipeline details & its dependents
     *
     * @param pipelineId pipeline id
     * @param userId data to be retrieved for
     *
     * @return {@link PipelineDetails}
     */
    Mono<PipelineDetails> findPipelineDetailsFull(String pipelineId, String userId);

    /**
     * Return all records for given user based on input parameters below
     *
     * @param userId for whom data to be retrieved
     * @param limit number of records
     * @param offset record offset
     *
     * @return {@link PipelineDetails}
     */
    Flux<PipelineDetails> findAll(String userId, int limit, int offset);

    /**
     * @param pipelineId pipeline id
     * @param userId data to be retrieved for
     *
     * @return {@link PipelineDetails}
     */
    Mono<PipelineDetails> find(String pipelineId, String userId);
}
