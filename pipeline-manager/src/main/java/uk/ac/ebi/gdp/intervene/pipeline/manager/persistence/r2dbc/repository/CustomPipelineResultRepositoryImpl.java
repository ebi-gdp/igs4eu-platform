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
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineResult;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.COMPLETED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.PipelineResultEntityMapper.map;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineResultQueries.FIND_BY_STATUS_USER_ID_CONDITION_DATA_LIMIT_QUERY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineResultQueries.FIND_BY_STATUS_USER_ID_CONDITION_PIPELINE_ID_CONDITION;

public class CustomPipelineResultRepositoryImpl implements CustomPipelineResultRepository {
    private final DatabaseClient databaseClient;

    public CustomPipelineResultRepositoryImpl(final DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<PipelineResult> findCompletedRecent(final String userId) {
        return databaseClient
                .sql(FIND_BY_STATUS_USER_ID_CONDITION_DATA_LIMIT_QUERY)
                .bind("status", COMPLETED)
                .bind("userId", userId)
                .map(map()::apply)
                .one();
    }

    @Override
    public Mono<PipelineResult> findCompleted(final String pipelineId,
                                              final String userId) {
        return databaseClient
                .sql(FIND_BY_STATUS_USER_ID_CONDITION_PIPELINE_ID_CONDITION)
                .bind("status", COMPLETED)
                .bind("pipelineId", pipelineId)
                .bind("userId", userId)
                .map(map()::apply)
                .one();
    }
}
