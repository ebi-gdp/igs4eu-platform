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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;

@Table("dataset_cryptography_details")
public class DatasetCryptographyDetails implements Persistable<String> {
    @Id
    private String datasetId;
    private String publicKey;
    private String secretId;
    private String secretIdVersion;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdOn;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedOn;

    @Transient
    private boolean isNew;

    protected DatasetCryptographyDetails() {
    }

    private DatasetCryptographyDetails(final String datasetId,
                                       final String publicKey,
                                       final String secretId,
                                       final String secretIdVersion,
                                       final boolean isNew) {
        this.datasetId = datasetId;
        this.publicKey = publicKey;
        this.secretId = secretId;
        this.secretIdVersion = secretIdVersion;
        this.isNew = isNew;
    }

    private DatasetCryptographyDetails(final String datasetId,
                                       final String publicKey,
                                       final String secretId,
                                       final String secretIdVersion,
                                       final String createdBy,
                                       final LocalDateTime createdOn,
                                       final String updatedBy,
                                       final LocalDateTime updatedOn) {
        this(datasetId, publicKey, secretId, secretIdVersion, false);
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }

    public static DatasetCryptographyDetails create(final String datasetId,
                                                    final String publicKey,
                                                    final String secretId,
                                                    final String secretIdVersion) {
        return new DatasetCryptographyDetails(
                datasetId,
                publicKey,
                secretId,
                secretIdVersion,
                true);
    }

    public static DatasetCryptographyDetails load(final String datasetId,
                                                  final String publicKey,
                                                  final String secretId,
                                                  final String secretIdVersion,
                                                  final String createdBy,
                                                  final LocalDateTime createdOn,
                                                  final String updatedBy,
                                                  final LocalDateTime updatedOn) {
        return new DatasetCryptographyDetails(
                datasetId,
                publicKey,
                secretId,
                secretIdVersion,
                createdBy,
                createdOn,
                updatedBy,
                updatedOn);
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretIdVersion() {
        return secretIdVersion;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    @Override
    public String getId() {
        return datasetId;
    }

    @Override
    public boolean isNew() {
        return isNew || !hasText(datasetId);
    }
}
