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

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.PipelineDetailsEntityMapper.map;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.PipelineDetailsEntityMapper.fullMap;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineQueries.FIND_BY_USER_ID_FULL_DETAILS_ORDER_BY_LIMIT;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineQueries.FIND_BY_USER_ID_FULL_DETAILS_PIPELINE_ID_CONDITION;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineQueries.FIND_BY_USER_ID_ORDER_BY_LIMIT_OFFSET;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineQueries.FIND_BY_USER_ID_PIPELINE_ID_CONDITION;

public class CustomPipelineDetailsRepositoryImpl implements CustomPipelineDetailsRepository {
    private final DatabaseClient databaseClient;

    public CustomPipelineDetailsRepositoryImpl(final DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineDetails> findPipelineDetailsFullRecent(final String userId) {
        return databaseClient
                .sql(FIND_BY_USER_ID_FULL_DETAILS_ORDER_BY_LIMIT)
                .bind("userId", userId)
                .map(fullMap()::apply)
                .one();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineDetails> findPipelineDetailsFull(final String pipelineId,
                                                         final String userId) {
        return databaseClient
                .sql(FIND_BY_USER_ID_FULL_DETAILS_PIPELINE_ID_CONDITION)
                .bind("pipelineId", pipelineId)
                .bind("userId", userId)
                .map(fullMap()::apply)
                .one();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<PipelineDetails> findAll(final String userId,
                                         final int limit,
                                         final int offset) {
        return databaseClient
                .sql(FIND_BY_USER_ID_ORDER_BY_LIMIT_OFFSET)
                .bind("userId", userId)
                .bind("limit", limit)
                .bind("offset", offset)
                .map(map()::apply)
                .all();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PipelineDetails> find(final String pipelineId,
                                      final String userId) {
        return databaseClient
                .sql(FIND_BY_USER_ID_PIPELINE_ID_CONDITION)
                .bind("userId", userId)
                .bind("pipelineId", pipelineId)
                .map(map()::apply)
                .one();
    }
}
