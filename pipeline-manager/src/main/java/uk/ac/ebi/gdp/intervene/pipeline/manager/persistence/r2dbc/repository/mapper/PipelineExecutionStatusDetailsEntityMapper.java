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

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.ENDED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PIPELINE_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PIPELINE_UID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.STARTED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.STATUS;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.SUBMITTED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.TRACE_EXIT;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.TRACE_NAME;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.USER_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getByte;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getLocalDateTime;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface PipelineExecutionStatusDetailsEntityMapper {
    static BiFunction<Row, Object, PipelineExecutionStatus> map() {
        return (row, object) -> PipelineExecutionStatus.load(
                getString(PIPELINE_ID, row),
                PipelineStatus.valueOf(getString(STATUS, row)),
                getString(TRACE_NAME, row),
                getByte(TRACE_EXIT, row),
                getLocalDateTime(SUBMITTED_ON, row),
                getLocalDateTime(STARTED_ON, row),
                getLocalDateTime(ENDED_ON, row),
                getString(CREATED_BY, row),
                getLocalDateTime(CREATED_ON, row),
                getString(UPDATED_BY, row),
                getLocalDateTime(UPDATED_ON, row),
                PipelineDetails.load(
                        getString(PIPELINE_ID, row),
                        getString(PIPELINE_UID, row),
                        getString(USER_ID, row),
                        getString(DATASET_ID, row),
                        getString(CREATED_BY, row),
                        getLocalDateTime(CREATED_ON, row),
                        getString(UPDATED_BY, row),
                        getLocalDateTime(UPDATED_ON, row)
                )
        );
    }
}

