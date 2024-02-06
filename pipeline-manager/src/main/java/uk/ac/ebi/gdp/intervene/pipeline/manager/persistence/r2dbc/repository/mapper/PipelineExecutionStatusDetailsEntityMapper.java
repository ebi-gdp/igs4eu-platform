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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper;

import io.r2dbc.spi.Row;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;

import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getLocalDateTime;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface PipelineExecutionStatusDetailsEntityMapper {
    static BiFunction<Row, Object, PipelineExecutionStatus> map() {
        return (row, object) -> PipelineExecutionStatus.load(
                getString("pipeline_id", row),
                PipelineStatus.valueOf(getString("status", row)),
                getString("trace_name", row),
                row.get("trace_exit") != null ? row.get("trace_exit", Byte.class) : (byte) 0,
                getLocalDateTime("submitted_on", row),
                getLocalDateTime("started_on", row),
                getLocalDateTime("ended_on", row),
                getString("created_by", row),
                getLocalDateTime("created_on", row),
                getString("updated_by", row),
                getLocalDateTime("updated_on", row),
                PipelineDetails.load(
                        getString("pipeline_id", row),
                        getString("pipeline_uid", row),
                        getString("user_id", row),
                        getString("dataset_id", row),
                        getString("created_by", row),
                        getLocalDateTime("created_on", row),
                        getString("updated_by", row),
                        getLocalDateTime("updated_on", row)
                )
        );
    }
}

