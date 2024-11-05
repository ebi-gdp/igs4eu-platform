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
package uk.ac.ebi.gdp.intervene.pipeline.manager.converter;

import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.EnumWriteSupport;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetStatusType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;

public interface EnumConverter {

    @WritingConverter
    class PipelineStatusTypeConverter extends EnumWriteSupport<PipelineStatus> {
    }

    @WritingConverter
    class GenomeBuildConverter extends EnumWriteSupport<GenomeBuild> {
    }

    @WritingConverter
    class FilesetTypeConverter extends EnumWriteSupport<FilesetType> {
    }

    @WritingConverter
    class DatasetStatusTypeConverter extends EnumWriteSupport<DatasetStatusType> {
    }
}
