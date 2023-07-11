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
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;

public class CustomPipelineDetailsRepositoryImpl implements CustomPipelineDetailsRepository {
    private final DatabaseClient databaseClient;
    private final PipelineDetailsMapper pipelineDetailsMapper;

    public CustomPipelineDetailsRepositoryImpl(final DatabaseClient databaseClient,
                                               final PipelineDetailsMapper pipelineDetailsMapper) {
        this.databaseClient = databaseClient;
        this.pipelineDetailsMapper = pipelineDetailsMapper;
    }

    @Override
    public Mono<PipelineDetails> findRecentFullPipelineDetails(final String userId) {
        //@formatter:off
        final String query = "SELECT " +
                             " p.pipeline_id," +
                             " p.pipeline_uid," +
                             " p.user_id," +
                             " p.dataset_id," +
                             " p.status," +
                             " d.dataset_name," +
                             " d.genome_build," +
                             " d.fileset_id," +
                             " d.fileset_type," +
                             " g.globus_username," +
                             " g.guest_collection_id," +
                             " g.dir_path_on_guest_collection," +
                             " u.globus_user_uid " +
                             "FROM " +
                             " pipeline_details p " +
                             "INNER JOIN" +
                             " dataset_details d " +
                             " ON p.dataset_id = d.dataset_id " +
                             "INNER JOIN" +
                             " globus_guest_collection_files_details g" +
                             " ON d.fileset_id = g.fileset_id " +
                             "INNER JOIN" +
                             " globus_user_details u" +
                             " ON u.globus_username = g.globus_username " +
                             "WHERE" +
//                           " p.pipeline_id = :pipelineId " +
//                           "AND " +
                             " p.user_id = :userId " +
                             "ORDER BY " +
                             " p.pipeline_id DESC " +
                             "LIMIT 1";
        //@formatter:on
        return databaseClient
                .sql(query)
//                .bind("pipelineId", pipelineId)
                .bind("userId", userId)
                .map(pipelineDetailsMapper::apply)
                .one();
    }
}
