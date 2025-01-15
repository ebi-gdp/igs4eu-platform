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
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetCryptographyDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetStatusType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusDetails;

import java.nio.file.Path;
import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.C_IS_DELETED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_NAME;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DIR_PATH_ON_GUEST_COLLECTION;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.EXPIRES_AT;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.FILESET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.FILESET_TYPE;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GC_CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GC_CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GC_UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GC_UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GENOME_BUILD;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GLOBUS_USERNAME;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GUEST_COLLECTION_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.IS_DELETED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PUBLIC_KEY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.SECRET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.SECRET_ID_VERSION;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.STATUS;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getBoolean;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getLocalDateTime;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface DatasetDetailsEntityMapper {
    static BiFunction<Row, Object, DatasetDetails> fullMap() {
        return (row, object) -> {
            final DatasetCryptographyDetails datasetCryptographyDetails = DatasetCryptographyDetails.load(
                    getString(DATASET_ID, row),
                    getString(PUBLIC_KEY, row),
                    getString(SECRET_ID, row),
                    getString(SECRET_ID_VERSION, row),
                    getBoolean(C_IS_DELETED, row),
                    getString(CREATED_BY, row),
                    getLocalDateTime(CREATED_ON, row),
                    getString(UPDATED_BY, row),
                    getLocalDateTime(UPDATED_ON, row));
            final GlobusDetails globusDetails = GlobusDetails.load(
                    getString(FILESET_ID, row),
                    getString(GLOBUS_USERNAME, row),
                    getString(GUEST_COLLECTION_ID, row),
                    Path.of(getString(DIR_PATH_ON_GUEST_COLLECTION, row)),
                    DatasetStatusType.valueOf(getString(STATUS, row)),
                    getString(CREATED_BY, row),
                    getLocalDateTime(CREATED_ON, row),
                    getString(UPDATED_BY, row),
                    getLocalDateTime(UPDATED_ON, row));
            return DatasetDetails.load(
                    getString(DATASET_ID, row),
                    getString(DATASET_NAME, row),
                    GenomeBuild.valueOf(getString(GENOME_BUILD, row)),
                    FilesetType.valueOf(getString(FILESET_TYPE, row)),
                    getLocalDateTime(EXPIRES_AT, row),
                    getBoolean(IS_DELETED, row),
                    datasetCryptographyDetails,
                    globusDetails,
                    getString(GC_CREATED_BY, row),
                    getLocalDateTime(GC_CREATED_ON, row),
                    getString(GC_UPDATED_BY, row),
                    getLocalDateTime(GC_UPDATED_ON, row));
        };
    }

    static BiFunction<Row, Object, DatasetDetails> expiredDatasetMap() {
        return (row, object) -> {
            final GlobusDetails globusDetails = GlobusDetails.load(
                    getString(FILESET_ID, row),
                    getString(GLOBUS_USERNAME, row),
                    getString(GUEST_COLLECTION_ID, row),
                    Path.of(getString(DIR_PATH_ON_GUEST_COLLECTION, row)),
                    DatasetStatusType.valueOf(getString(STATUS, row)),
                    getString(CREATED_BY, row),
                    getLocalDateTime(CREATED_ON, row),
                    getString(UPDATED_BY, row),
                    getLocalDateTime(UPDATED_ON, row));
            return DatasetDetails.load(
                    getString(DATASET_ID, row),
                    getString(DATASET_NAME, row),
                    GenomeBuild.valueOf(getString(GENOME_BUILD, row)),
                    FilesetType.valueOf(getString(FILESET_TYPE, row)),
                    getLocalDateTime(EXPIRES_AT, row),
                    getBoolean(IS_DELETED, row),
                    globusDetails,
                    getString(GC_CREATED_BY, row),
                    getLocalDateTime(GC_CREATED_ON, row),
                    getString(GC_UPDATED_BY, row),
                    getLocalDateTime(GC_UPDATED_ON, row));
        };
    }
}
