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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.joinQuery;

public interface PipelineQueries {
    //@formatter:off
    String FIND_BY_USER_ID = "SELECT " +
                             " p.pipeline_id," +
                             " p.pipeline_uid," +
                             " p.user_id," +
                             " p.dataset_id," +
                             " p.created_by," +
                             " p.created_on," +
                             " p.updated_by," +
                             " p.updated_on," +
                             " s.status," +
                             " s.submitted_on," +
                             " s.started_on," +
                             " s.ended_on," +
                             " d.dataset_id," +
                             " d.dataset_name," +
                             " d.genome_build " +
                             "FROM " +
                             " pipeline_details p " +
                             "INNER JOIN" +
                             " pipeline_execution_status s " +
                             " ON p.pipeline_id = s.pipeline_id " +
                             "INNER JOIN" +
                             " dataset_details d " +
                             " ON p.dataset_id = d.dataset_id " +
                             "WHERE" +
                             " p.user_id = :userId";

    String FIND_BY_USER_ID_FULL_DETAILS = "SELECT " +
                                          " p.pipeline_id," +
                                          " p.pipeline_uid," +
                                          " p.user_id," +
                                          " p.dataset_id," +
                                          " s.status," +
                                          " s.trace_name," +
                                          " s.trace_exit," +
                                          " s.submitted_on," +
                                          " s.started_on," +
                                          " s.ended_on," +
                                          " s.created_by," +
                                          " s.created_on," +
                                          " s.updated_by," +
                                          " s.updated_on," +
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
                                          " pipeline_execution_status s " +
                                          " ON p.pipeline_id = s.pipeline_id " +
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
                                          " p.user_id = :userId";

    String PIPELINE_ID_CONDITION = "AND " +
                                   "p.pipeline_id = :pipelineId";

    String ORDER_BY_LIMIT_OFFSET = "ORDER BY" +
                                   " p.pipeline_id DESC " +
                                   "LIMIT :limit OFFSET :offset";

    String ORDER_BY_LIMIT = "ORDER BY" +
                            " p.pipeline_id DESC " +
                            "LIMIT 1";

    String FIND_BY_USER_ID_FULL_DETAILS_ORDER_BY_LIMIT = joinQuery(FIND_BY_USER_ID_FULL_DETAILS, ORDER_BY_LIMIT);

    String FIND_BY_USER_ID_FULL_DETAILS_PIPELINE_ID_CONDITION = joinQuery(FIND_BY_USER_ID_FULL_DETAILS, PIPELINE_ID_CONDITION);

    String FIND_BY_USER_ID_ORDER_BY_LIMIT_OFFSET = joinQuery(FIND_BY_USER_ID, ORDER_BY_LIMIT_OFFSET);

    String FIND_BY_USER_ID_PIPELINE_ID_CONDITION = joinQuery(FIND_BY_USER_ID, PIPELINE_ID_CONDITION);
    //@formatter:on
}
