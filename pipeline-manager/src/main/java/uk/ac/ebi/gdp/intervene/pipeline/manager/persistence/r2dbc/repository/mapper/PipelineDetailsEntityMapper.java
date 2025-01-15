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
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;

import java.nio.file.Paths;
import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails.load;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CRYPTOGRAPHY_CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CRYPTOGRAPHY_CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CRYPTOGRAPHY_UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.CRYPTOGRAPHY_UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.C_IS_DELETED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_NAME;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DATASET_UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.DIR_PATH_ON_GUEST_COLLECTION;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.ENDED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.EXPIRES_AT;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.FILESET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.FILESET_TYPE;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GENOME_BUILD;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GLOBUS_CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GLOBUS_CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GLOBUS_UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GLOBUS_UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GLOBUS_USERNAME;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GLOBUS_USER_UID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GUEST_COLLECTION_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GU_CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GU_CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GU_UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.GU_UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.G_STATUS;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.IS_DELETED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PES_CREATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PES_CREATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PES_UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PES_UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PIPELINE_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PIPELINE_UID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.PUBLIC_KEY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.SECRET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.SECRET_ID_VERSION;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.STARTED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.STATUS;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.SUBMITTED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.TRACE_EXIT;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.TRACE_NAME;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.UPDATED_BY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.UPDATED_ON;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.mapper.FieldNames.USER_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getBoolean;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getByte;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getLocalDateTime;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface PipelineDetailsEntityMapper {
    static BiFunction<Row, Object, PipelineDetails> fullMap() {
        return (row, object) -> {
            final GlobusUserDetails globusUserDetails = GlobusUserDetails.load(
                    getString(GLOBUS_USERNAME, row),
                    getString(GLOBUS_USER_UID, row),
                    getString(USER_ID, row),
                    getString(GU_CREATED_BY, row),
                    getLocalDateTime(GU_CREATED_ON, row),
                    getString(GU_UPDATED_BY, row),
                    getLocalDateTime(GU_UPDATED_ON, row));
            final GlobusDetails globusDetails = GlobusDetails.load(
                    getString(FILESET_ID, row),
                    getString(GLOBUS_USERNAME, row),
                    getString(GUEST_COLLECTION_ID, row),
                    Paths.get(getString(DIR_PATH_ON_GUEST_COLLECTION, row)),
                    DatasetStatusType.valueOf(getString(G_STATUS, row)),
                    getString(GLOBUS_CREATED_BY, row),
                    getLocalDateTime(GLOBUS_CREATED_ON, row),
                    getString(GLOBUS_UPDATED_BY, row),
                    getLocalDateTime(GLOBUS_UPDATED_ON, row));
            globusDetails.setGlobusUserDetails(globusUserDetails);
            final DatasetCryptographyDetails cryptographyDetails = DatasetCryptographyDetails.load(
                    getString(DATASET_ID, row),
                    getString(PUBLIC_KEY, row),
                    getString(SECRET_ID, row),
                    getString(SECRET_ID_VERSION, row),
                    getBoolean(C_IS_DELETED, row),
                    getString(CRYPTOGRAPHY_CREATED_BY, row),
                    getLocalDateTime(CRYPTOGRAPHY_CREATED_ON, row),
                    getString(CRYPTOGRAPHY_UPDATED_BY, row),
                    getLocalDateTime(CRYPTOGRAPHY_UPDATED_ON, row));
            final DatasetDetails datasetDetails = DatasetDetails.load(
                    getString(DATASET_ID, row),
                    getString(DATASET_NAME, row),
                    GenomeBuild.valueOf(getString(GENOME_BUILD, row)),
                    FilesetType.valueOf(getString(FILESET_TYPE, row)),
                    getLocalDateTime(EXPIRES_AT, row),
                    getBoolean(IS_DELETED, row),
                    cryptographyDetails,
                    globusDetails,
                    getString(DATASET_CREATED_BY, row),
                    getLocalDateTime(DATASET_CREATED_ON, row),
                    getString(DATASET_UPDATED_BY, row),
                    getLocalDateTime(DATASET_UPDATED_ON, row));
            final PipelineExecutionStatus pipelineExecutionStatus = getPipelineExecutionStatus(row);
            return getPipelineDetails(row, pipelineExecutionStatus, datasetDetails);
        };
    }

    static BiFunction<Row, Object, PipelineDetails> map() {
        return (row, object) -> {
            final PipelineExecutionStatus pipelineExecutionStatus = getPipelineExecutionStatus(row);
            final DatasetDetails datasetDetails = DatasetDetails.load(
                    getString(DATASET_ID, row),
                    getString(DATASET_NAME, row),
                    GenomeBuild.valueOf(getString(GENOME_BUILD, row)),
                    FilesetType.valueOf(getString(FILESET_TYPE, row)),
                    getLocalDateTime(EXPIRES_AT, row),
                    getBoolean(IS_DELETED, row),
                    getString(DATASET_CREATED_BY, row),
                    getLocalDateTime(DATASET_CREATED_ON, row),
                    getString(DATASET_UPDATED_BY, row),
                    getLocalDateTime(DATASET_UPDATED_ON, row)
            );
            return getPipelineDetails(row, pipelineExecutionStatus, datasetDetails);
        };
    }

    static BiFunction<Row, Object, DatasetDetails> tinyMap() {
        return (row, object) -> DatasetDetails.loadDatasetIdAndNameOnly(getString(DATASET_ID, row),
                getString(DATASET_NAME, row));
    }

    static PipelineDetails getPipelineDetails(final Row row,
                                              final PipelineExecutionStatus pipelineExecutionStatus,
                                              final DatasetDetails datasetDetails) {
        return load(
                getString(PIPELINE_ID, row),
                getString(PIPELINE_UID, row),
                getString(USER_ID, row),
                getString(DATASET_ID, row),
                getString(CREATED_BY, row),
                getLocalDateTime(CREATED_ON, row),
                getString(UPDATED_BY, row),
                getLocalDateTime(UPDATED_ON, row),
                pipelineExecutionStatus,
                datasetDetails);
    }

    static PipelineExecutionStatus getPipelineExecutionStatus(final Row row) {
        return PipelineExecutionStatus.load(
                getString(PIPELINE_ID, row),
                PipelineStatus.valueOf(getString(STATUS, row)),
                getString(TRACE_NAME, row),
                getByte(TRACE_EXIT, row),
                getLocalDateTime(SUBMITTED_ON, row),
                getLocalDateTime(STARTED_ON, row),
                getLocalDateTime(ENDED_ON, row),
                getString(PES_CREATED_BY, row),
                getLocalDateTime(PES_CREATED_ON, row),
                getString(PES_UPDATED_BY, row),
                getLocalDateTime(PES_UPDATED_ON, row));
    }
}
