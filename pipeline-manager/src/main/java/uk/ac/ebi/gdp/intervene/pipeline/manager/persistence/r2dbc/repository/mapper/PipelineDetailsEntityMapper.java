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
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;

import java.nio.file.Paths;
import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails.load;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getLocalDateTime;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface PipelineDetailsEntityMapper {
    static BiFunction<Row, Object, PipelineDetails> fullMap() {
        return (row, object) -> {
            final GlobusUserDetails globusUserDetails = GlobusUserDetails.load(
                    getString("globus_username", row),
                    getString("globus_user_uid", row),
                    getString("user_id", row),
                    getString("gu_created_by", row),
                    getLocalDateTime("gu_created_on", row),
                    getString("gu_updated_by", row),
                    getLocalDateTime("gu_updated_on", row)
            );

            final GlobusDetails globusDetails = GlobusDetails.loadRecord(
                    getString("fileset_id", row),
                    getString("globus_username", row),
                    getString("guest_collection_id", row),
                    Paths.get(getString("dir_path_on_guest_collection", row)),
                    getString("globus_created_by", row),
                    getLocalDateTime("globus_created_on", row),
                    getString("globus_updated_by", row),
                    getLocalDateTime("globus_updated_on", row));
            globusDetails.setGlobusUserDetails(globusUserDetails);

            final DatasetDetails datasetDetails = DatasetDetails.load(
                    getString("dataset_id", row),
                    getString("dataset_name", row),
                    GenomeBuild.valueOf(getString("genome_build", row)),
                    FilesetType.valueOf(getString("fileset_type", row)),
                    globusDetails,
                    getString("dataset_created_by", row),
                    getLocalDateTime("dataset_created_on", row),
                    getString("dataset_updated_by", row),
                    getLocalDateTime("dataset_updated_on", row)
            );
            final PipelineExecutionStatus pipelineExecutionStatus = getPipelineExecutionStatus(row);
            return getPipelineDetails(row, pipelineExecutionStatus, datasetDetails);
        };
    }

    static BiFunction<Row, Object, PipelineDetails> map() {
        return (row, object) -> {
            final PipelineExecutionStatus pipelineExecutionStatus = getPipelineExecutionStatus(row);
            final DatasetDetails datasetDetails = DatasetDetails.load(
                    getString("dataset_id", row),
                    getString("dataset_name", row),
                    GenomeBuild.valueOf(getString("genome_build", row)),
                    FilesetType.valueOf(getString("fileset_type", row)),
                    getString("dataset_created_by", row),
                    getLocalDateTime("dataset_created_on", row),
                    getString("dataset_updated_by", row),
                    getLocalDateTime("dataset_updated_on", row)
            );
            return getPipelineDetails(row, pipelineExecutionStatus, datasetDetails);
        };
    }

    static PipelineDetails getPipelineDetails(final Row row,
                                              final PipelineExecutionStatus pipelineExecutionStatus,
                                              final DatasetDetails datasetDetails) {
        return load(
                getString("pipeline_id", row),
                getString("pipeline_uid", row),
                getString("user_id", row),
                getString("dataset_id", row),
                getString("created_by", row),
                getLocalDateTime("created_on", row),
                getString("updated_by", row),
                getLocalDateTime("updated_on", row),
                pipelineExecutionStatus,
                datasetDetails);
    }

    static PipelineExecutionStatus getPipelineExecutionStatus(final Row row) {
        return PipelineExecutionStatus.load(
                getString("pipeline_id", row),
                PipelineStatus.valueOf(getString("status", row)),
                getString("trace_name", row),
                row.get("trace_exit") != null ? row.get("trace_exit", Byte.class) : (byte) 0,
                getLocalDateTime("submitted_on", row),
                getLocalDateTime("started_on", row),
                getLocalDateTime("ended_on", row),
                getString("pes_created_by", row),
                getLocalDateTime("pes_created_on", row),
                getString("pes_updated_by", row),
                getLocalDateTime("pes_updated_on", row));
    }
}
