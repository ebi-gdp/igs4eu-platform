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
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.PipelineExecutionStatusDetailsModelMapper.map;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineExecutionStatusQueries.FIND_BY_PIPELINE_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.PipelineExecutionStatusQueries.FIND_BY_PIPELINE_ID_FIND_BY_USER_ID;

public class CustomPipelineExecutionStatusRepositoryImpl implements CustomPipelineExecutionStatusRepository {
    private final DatabaseClient databaseClient;

    public CustomPipelineExecutionStatusRepositoryImpl(final DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<PipelineExecutionStatus> findPipelineExecutionStatus(final String pipelineId) {
        return databaseClient
                .sql(FIND_BY_PIPELINE_ID)
                .bind("pipelineId", pipelineId)
                .map(map()::apply)
                .one();
    }

    @Override
    public Mono<PipelineExecutionStatus> findPipelineExecutionStatus(final String pipelineId,
                                                                     final String userId) {
        return databaseClient
                .sql(FIND_BY_PIPELINE_ID_FIND_BY_USER_ID)
                .bind("pipelineId", pipelineId)
                .bind("userId", userId)
                .map(map()::apply)
                .one();
    }
}
