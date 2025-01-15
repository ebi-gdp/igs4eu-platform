/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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

public interface FieldNames {
    String DATASET_ID = "dataset_id";
    String DATASET_NAME = "dataset_name";
    String GENOME_BUILD = "genome_build";
    String FILESET_TYPE = "fileset_type";
    String EXPIRES_AT = "expires_at";
    String IS_DELETED = "is_deleted";
    String CREATED_BY = "created_by";
    String CREATED_ON = "created_on";
    String UPDATED_BY = "updated_by";
    String UPDATED_ON = "updated_on";
    String PUBLIC_KEY = "public_key";
    String SECRET_ID = "secret_id";
    String SECRET_ID_VERSION = "secret_id_version";
    String FILESET_ID = "fileset_id";
    String GLOBUS_USERNAME = "globus_username";
    String GLOBUS_USER_UID = "globus_user_uid";
    String GUEST_COLLECTION_ID = "guest_collection_id";
    String DIR_PATH_ON_GUEST_COLLECTION = "dir_path_on_guest_collection";
    String STATUS = "status";
    String USER_ID = "user_id";
    String GC_CREATED_BY = "gc_created_by";
    String GC_CREATED_ON = "gc_created_on";
    String GC_UPDATED_BY = "gc_updated_by";
    String GC_UPDATED_ON = "gc_updated_on";
    String GU_CREATED_BY = "gu_created_by";
    String GU_CREATED_ON = "gu_created_on";
    String GU_UPDATED_BY = "gu_updated_by";
    String GU_UPDATED_ON = "gu_updated_on";
    String GLOBUS_CREATED_BY = "globus_created_by";
    String GLOBUS_CREATED_ON = "globus_created_on";
    String GLOBUS_UPDATED_BY = "globus_updated_by";
    String GLOBUS_UPDATED_ON = "globus_updated_on";
    String CRYPTOGRAPHY_CREATED_BY = "cryptography_created_by";
    String CRYPTOGRAPHY_CREATED_ON = "cryptography_created_on";
    String CRYPTOGRAPHY_UPDATED_BY = "cryptography_updated_by";
    String CRYPTOGRAPHY_UPDATED_ON = "cryptography_updated_on";
    String DATASET_CREATED_BY = "dataset_created_by";
    String DATASET_CREATED_ON = "dataset_created_on";
    String DATASET_UPDATED_BY = "dataset_updated_by";
    String DATASET_UPDATED_ON = "dataset_updated_on";
    String PIPELINE_ID = "pipeline_id";
    String PIPELINE_UID = "pipeline_uid";
    String TRACE_NAME = "trace_name";
    String TRACE_EXIT = "trace_exit";
    String SUBMITTED_ON = "submitted_on";
    String STARTED_ON = "started_on";
    String ENDED_ON = "ended_on";
    String PES_CREATED_BY = "pes_created_by";
    String PES_CREATED_ON = "pes_created_on";
    String PES_UPDATED_BY = "pes_updated_by";
    String PES_UPDATED_ON = "pes_updated_on";
    String FILE_DOWNLOAD_PATH = "file_download_path";
    String C_IS_DELETED = "c_is_deleted";
    String G_STATUS = "g_status";
}
