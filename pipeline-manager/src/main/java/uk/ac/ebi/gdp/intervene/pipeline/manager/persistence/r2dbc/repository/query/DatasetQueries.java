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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query;

public interface DatasetQueries {
    //@formatter:off
    String FIND_BY_DATASET_ID_AND_CREATED_BY = "SELECT " +
                                               " d.dataset_id," +
                                               " d.dataset_name," +
                                               " d.genome_build," +
                                               " d.fileset_type," +
                                               " d.expires_at," +
                                               " d.is_deleted," +
                                               " d.created_by," +
                                               " d.created_on," +
                                               " d.updated_by," +
                                               " d.updated_on," +
                                               " c.public_key," +
                                               " c.secret_id," +
                                               " c.secret_id_version," +
                                               " c.is_deleted as c_is_deleted," +
                                               " c.created_by," +
                                               " c.created_on," +
                                               " c.updated_by," +
                                               " c.updated_on," +
                                               " f.fileset_id," +
                                               " f.globus_username," +
                                               " f.guest_collection_id," +
                                               " f.dir_path_on_guest_collection," +
                                               " f.status," +
                                               " f.created_by as gc_created_by," +
                                               " f.created_on as gc_created_on," +
                                               " f.updated_by as gc_updated_by," +
                                               " f.updated_on as gc_updated_on " +
                                               "FROM" +
                                               " dataset_details d " +
                                               "INNER JOIN" +
                                               " dataset_cryptography_details c" +
                                               " ON d.dataset_id = c.dataset_id " +
                                               "INNER JOIN" +
                                               " globus_guest_collection_files_details f" +
                                               " ON d.fileset_id = f.fileset_id " +
                                               "WHERE" +
                                               " d.dataset_id = :datasetId " +
                                               "AND" +
                                               " d.created_by = :createdBy ";

    String FIND_EXPIRED_DATASETS = "SELECT " +
                                   " d.dataset_id," +
                                   " d.dataset_name," +
                                   " d.genome_build," +
                                   " d.fileset_type," +
                                   " d.expires_at," +
                                   " d.is_deleted," +
                                   " d.created_by," +
                                   " d.created_on," +
                                   " d.updated_by," +
                                   " d.updated_on," +
                                   " f.fileset_id," +
                                   " f.globus_username," +
                                   " f.guest_collection_id," +
                                   " f.dir_path_on_guest_collection," +
                                   " f.status," +
                                   " f.created_by as gc_created_by," +
                                   " f.created_on as gc_created_on," +
                                   " f.updated_by as gc_updated_by," +
                                   " f.updated_on as gc_updated_on " +
                                   "FROM" +
                                   " dataset_details d " +
                                   "INNER JOIN" +
                                   " globus_guest_collection_files_details f" +
                                   " ON d.fileset_id = f.fileset_id " +
                                   "WHERE" +
                                   " d.expires_at < CURRENT_TIMESTAMP " +
                                   "AND" +
                                   " d.is_deleted = false " +
                                   "AND" +
                                   " f.status = 'ACTIVE' " +
                                   "LIMIT :limit";
    //@formatter:on
}
