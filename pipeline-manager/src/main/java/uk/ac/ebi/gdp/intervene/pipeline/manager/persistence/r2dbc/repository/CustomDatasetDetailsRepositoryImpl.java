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
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.DatasetDetailsModelMapper.mapFullDatasetDetails;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.DatasetQueries.FIND_BY_DATASET_ID_AND_CREATED_BY;

public class CustomDatasetDetailsRepositoryImpl implements CustomDatasetDetailsRepository {
    private final DatabaseClient databaseClient;

    public CustomDatasetDetailsRepositoryImpl(final DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<DatasetDetails> findByDatasetIdAndCreatedBy(final String datasetId,
                                                            final String createdBy) {
        return databaseClient
                .sql(FIND_BY_DATASET_ID_AND_CREATED_BY)
                .bind("datasetId", datasetId)
                .map(mapFullDatasetDetails()::apply)
                .one();
    }
}
