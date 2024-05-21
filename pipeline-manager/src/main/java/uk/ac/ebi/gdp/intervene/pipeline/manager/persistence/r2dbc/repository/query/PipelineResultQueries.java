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

public interface PipelineResultQueries {
    //@formatter:off
    String FIND_BY_STATUS = "SELECT " +
                            " p.pipeline_id," +
                            " r.file_download_path " +
                            "FROM" +
                            " pipeline_details p " +
                            "INNER JOIN" +
                            " pipeline_result r" +
                            " ON p.pipeline_id = r.pipeline_id " +
                            "INNER JOIN" +
                            " pipeline_execution_status s" +
                            " ON p.pipeline_id = s.pipeline_id " +
                            "WHERE" +
                            " s.status = :status";

    String USER_ID_CONDITION = "AND" +
                               " p.user_id = :userId";

    String PIPELINE_ID_CONDITION = "AND" +
                                   " p.pipeline_id = :pipelineId";

    String DATA_LIMIT_QUERY = "ORDER BY" +
                              " p.pipeline_id DESC " +
                              "LIMIT 1";

    String FIND_BY_STATUS_USER_ID_CONDITION_DATA_LIMIT_QUERY = joinQuery(FIND_BY_STATUS, USER_ID_CONDITION, DATA_LIMIT_QUERY);
    String FIND_BY_STATUS_USER_ID_CONDITION_PIPELINE_ID_CONDITION = joinQuery(FIND_BY_STATUS, USER_ID_CONDITION, PIPELINE_ID_CONDITION);
    //@formatter:on
}
