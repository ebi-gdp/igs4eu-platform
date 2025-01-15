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
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineResult;

import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.FILE_DOWNLOAD_PATH;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PIPELINE_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface PipelineResultEntityMapper {
    static BiFunction<Row, Object, PipelineResult> map() {
        return (row, object) -> PipelineResult.create(
                getString(PIPELINE_ID, row),
                getString(FILE_DOWNLOAD_PATH, row));
    }
}
