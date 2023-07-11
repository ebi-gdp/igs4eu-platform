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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository;

import io.r2dbc.spi.Row;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.BiFunction;

public class PipelineDetailsMapper implements BiFunction<Row, Object, PipelineDetails> {
    @Override
    public PipelineDetails apply(final Row row, final Object o) {
        final GlobusUserDetails globusUserDetails = new GlobusUserDetails(
                row.get("globus_username", String.class),
                row.get("globus_user_uid", String.class),
                row.get("user_id", String.class)
        );

        final GlobusDetails globusDetails = GlobusDetails.existingRecord(
                row.get("fileset_id", String.class),
                row.get("globus_username", String.class),
                row.get("guest_collection_id", String.class),
                Paths.get(Objects.requireNonNull(row.get("dir_path_on_guest_collection", String.class))));
        globusDetails.setGlobusUserDetails(globusUserDetails);

        final DatasetDetails datasetDetails = new DatasetDetails(
                row.get("dataset_id", String.class),
                row.get("dataset_name", String.class),
                GenomeBuild.valueOf(row.get("genome_build", String.class)),
                FilesetType.valueOf(row.get("fileset_type", String.class)),
                globusDetails
        );

        return new PipelineDetails(
                row.get("pipeline_id", String.class),
                row.get("pipeline_uid", String.class),
                row.get("user_id", String.class),
                PipelineStatus.valueOf(row.get("status", String.class)),
                datasetDetails
        );
    }
}
