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

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getDataTime;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface DatasetDetailsModelMapper {
    static BiFunction<Row, Object, DatasetDetails> mapFullDatasetDetails() {
        return (row, object) -> {
            final GlobusDetails globusDetails = GlobusDetails.loadRecord(
                    getString(row, "fileset_id"),
                    getString(row, "globus_username"),
                    getString(row, "guest_collection_id"),
                    Path.of(getString(row, "dir_path_on_guest_collection")),
                    getDataTime(row, "created_on"),
                    getDataTime(row, "updated_on")
            );
            return new DatasetDetails(
                    row.get("dataset_id", String.class),
                    row.get("dataset_name", String.class),
                    GenomeBuild.valueOf(row.get("genome_build", String.class)),
                    FilesetType.valueOf(row.get("fileset_type", String.class)),
                    globusDetails,
                    row.get("created_on", LocalDateTime.class),
                    row.get("updated_on", LocalDateTime.class)
            );
        };
    }
}
