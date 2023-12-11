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

import java.time.LocalDateTime;
import java.util.function.BiFunction;

public interface PipelineExecutionStatusDetailsModelMapper {
    static BiFunction<Row, Object, PipelineExecutionStatus> map() {
        return (row, object) -> new PipelineExecutionStatus(
                row.get("pipeline_id", String.class),
                PipelineStatus.valueOf(row.get("status", String.class)),
                row.get("trace_name", String.class),
                row.get("trace_exit", Byte.class),
                row.get("submitted_on", LocalDateTime.class),
                row.get("started_on", LocalDateTime.class),
                row.get("ended_on", LocalDateTime.class),
                row.get("created_by", String.class),
                row.get("created_on", LocalDateTime.class),
                row.get("updated_by", String.class),
                row.get("updated_on", LocalDateTime.class),
                new PipelineDetails(
                        row.get("pipeline_id", String.class),
                        row.get("pipeline_uid", String.class),
                        row.get("user_id", String.class),
                        row.get("dataset_id", String.class),
                        row.get("created_by", String.class),
                        row.get("created_on", LocalDateTime.class),
                        row.get("updated_by", String.class),
                        row.get("updated_on", LocalDateTime.class)
                )
        );
    }
}

