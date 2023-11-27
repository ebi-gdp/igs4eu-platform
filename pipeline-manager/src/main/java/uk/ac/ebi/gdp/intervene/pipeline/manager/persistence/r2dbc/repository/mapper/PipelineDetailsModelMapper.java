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
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails.pipelineDetailsWithStatusAndDataset;

public interface PipelineDetailsModelMapper {
    static BiFunction<Row, Object, PipelineDetails> mapFullDetails() {
        return (row, object) -> {
            final GlobusUserDetails globusUserDetails = new GlobusUserDetails(
                    row.get("globus_username", String.class),
                    row.get("globus_user_uid", String.class),
                    row.get("user_id", String.class)
            );

            final GlobusDetails globusDetails = GlobusDetails.loadRecord(
                    row.get("fileset_id", String.class),
                    row.get("globus_username", String.class),
                    row.get("guest_collection_id", String.class),
                    Paths.get(Objects.requireNonNull(row.get("dir_path_on_guest_collection", String.class))),
                    row.get("created_on", LocalDateTime.class),
                    row.get("updated_on", LocalDateTime.class));
            globusDetails.setGlobusUserDetails(globusUserDetails);

            final DatasetDetails datasetDetails = new DatasetDetails(
                    row.get("dataset_id", String.class),
                    row.get("dataset_name", String.class),
                    GenomeBuild.valueOf(row.get("genome_build", String.class)),
                    FilesetType.valueOf(row.get("fileset_type", String.class)),
                    globusDetails
            );

            final PipelineExecutionStatus pipelineExecutionStatus = new PipelineExecutionStatus(
                    row.get("pipeline_id", String.class),
                    PipelineStatus.valueOf(row.get("status", String.class)),
                    row.get("trace_name", String.class),
                    row.get("trace_exit") != null ? row.get("trace_exit", Byte.class) : (byte) 0,
                    row.get("submitted_on", LocalDateTime.class),
                    row.get("started_on", LocalDateTime.class),
                    row.get("ended_on", LocalDateTime.class),
                    row.get("created_by", String.class),
                    row.get("created_on", LocalDateTime.class),
                    row.get("updated_by", String.class),
                    row.get("updated_on", LocalDateTime.class)
            );
            return pipelineDetailsWithStatusAndDataset(
                    row.get("pipeline_id", String.class),
                    row.get("pipeline_uid", String.class),
                    row.get("user_id", String.class),
                    row.get("dataset_id", String.class),
                    row.get("created_by", String.class),
                    row.get("created_on", LocalDateTime.class),
                    row.get("updated_by", String.class),
                    row.get("updated_on", LocalDateTime.class),
                    pipelineExecutionStatus,
                    datasetDetails
            );
        };
    }

    static BiFunction<Row, Object, PipelineDetails> mapDetails() {
        return (row, object) -> {
            final PipelineExecutionStatus pipelineExecutionStatus = new PipelineExecutionStatus(
                    row.get("pipeline_id", String.class),
                    PipelineStatus.valueOf(row.get("status", String.class)),
                    row.get("submitted_on", LocalDateTime.class),
                    row.get("started_on", LocalDateTime.class),
                    row.get("ended_on", LocalDateTime.class));

            final DatasetDetails datasetDetails = new DatasetDetails(
                    row.get("dataset_id", String.class),
                    row.get("dataset_name", String.class),
                    GenomeBuild.valueOf(row.get("genome_build", String.class))
            );
            return pipelineDetailsWithStatusAndDataset(
                    row.get("pipeline_id", String.class),
                    row.get("pipeline_uid", String.class),
                    row.get("user_id", String.class),
                    row.get("dataset_id", String.class),
                    row.get("created_by", String.class),
                    row.get("created_on", LocalDateTime.class),
                    row.get("updated_by", String.class),
                    row.get("updated_on", LocalDateTime.class),
                    pipelineExecutionStatus,
                    datasetDetails);
        };
    }
}
