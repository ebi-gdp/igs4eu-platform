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

import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getBoolean;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getLocalDateTime;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

public interface DatasetDetailsEntityMapper {
    static BiFunction<Row, Object, DatasetDetails> fullMap() {
        return (row, object) -> {
            final DatasetCryptographyDetails datasetCryptographyDetails = DatasetCryptographyDetails.load(
                    getString("dataset_id", row),
                    getString("public_key", row),
                    getString("secret_id", row),
                    getString("secret_id_version", row),
                    getBoolean("c_is_deleted", row),
                    getString("created_by", row),
                    getLocalDateTime("created_on", row),
                    getString("updated_by", row),
                    getLocalDateTime("updated_on", row));
            final GlobusDetails globusDetails = GlobusDetails.load(
                    getString("fileset_id", row),
                    getString("globus_username", row),
                    getString("guest_collection_id", row),
                    Path.of(getString("dir_path_on_guest_collection", row)),
                    DatasetStatusType.valueOf(getString("status", row)),
                    getString("created_by", row),
                    getLocalDateTime("created_on", row),
                    getString("updated_by", row),
                    getLocalDateTime("updated_on", row));
            return DatasetDetails.load(
                    getString("dataset_id", row),
                    getString("dataset_name", row),
                    GenomeBuild.valueOf(getString("genome_build", row)),
                    FilesetType.valueOf(getString("fileset_type", row)),
                    getLocalDateTime("expires_at", row),
                    getBoolean("is_deleted", row),
                    datasetCryptographyDetails,
                    globusDetails,
                    getString("gc_created_by", row),
                    getLocalDateTime("gc_created_on", row),
                    getString("gc_updated_by", row),
                    getLocalDateTime("gc_updated_on", row));
        };
    }

    static BiFunction<Row, Object, DatasetDetails> expiredDatasetMap() {
        return (row, object) -> {
            final GlobusDetails globusDetails = GlobusDetails.load(
                    getString("fileset_id", row),
                    getString("globus_username", row),
                    getString("guest_collection_id", row),
                    Path.of(getString("dir_path_on_guest_collection", row)),
                    DatasetStatusType.valueOf(getString("status", row)),
                    getString("created_by", row),
                    getLocalDateTime("created_on", row),
                    getString("updated_by", row),
                    getLocalDateTime("updated_on", row));
            return DatasetDetails.load(
                    getString("dataset_id", row),
                    getString("dataset_name", row),
                    GenomeBuild.valueOf(getString("genome_build", row)),
                    FilesetType.valueOf(getString("fileset_type", row)),
                    getLocalDateTime("expires_at", row),
                    getBoolean("is_deleted", row),
                    globusDetails,
                    getString("gc_created_by", row),
                    getLocalDateTime("gc_created_on", row),
                    getString("gc_updated_by", row),
                    getLocalDateTime("gc_updated_on", row));
        };
    }
}
