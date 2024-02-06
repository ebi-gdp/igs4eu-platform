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

public interface PipelineExecutionStatusQueries {
    //@formatter:off
    String FIND_BY_PIPELINE_ID = "SELECT " +
                                 " p.pipeline_id," +
                                 " p.pipeline_uid," +
                                 " p.user_id," +
                                 " p.dataset_id," +
                                 " p.created_by," +
                                 " p.created_on," +
                                 " p.updated_by," +
                                 " p.updated_on," +
                                 " s.status," +
                                 " s.trace_name," +
                                 " s.trace_exit," +
                                 " s.submitted_on," +
                                 " s.started_on," +
                                 " s.ended_on," +
                                 " s.created_by as pes_created_by," +
                                 " s.created_on as pes_created_on," +
                                 " s.updated_by as pes_updated_by," +
                                 " s.updated_on as updated_on " +
                                 "FROM" +
                                 " pipeline_execution_status s " +
                                 "INNER JOIN" +
                                 " pipeline_details p " +
                                 " ON s.pipeline_id = p.pipeline_id " +
                                 "WHERE" +
                                 " p.pipeline_id = :pipelineId";
    //@formatter:on
}
