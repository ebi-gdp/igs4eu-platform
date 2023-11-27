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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.Default;

import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;

@Table("dataset_details")
public class DatasetDetails implements Persistable<String> {
    @Id
    @Column("dataset_id")
    private String datasetId;

    @Column("dataset_name")
    private String datasetName;

    @Column("genome_build")
    private GenomeBuild genomeBuild;

    @Column("fileset_id")
    private String filesetId;

    @Column("fileset_type")
    private FilesetType filesetType;

    @Column("created_by")
    private String createdBy;

    @CreatedDate
    @Column
    private LocalDateTime createdOn;

    @Column("updated_by")
    private String updatedBy;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedOn;

    @Transient
    private boolean newDatasetDetails;

    @Transient
    private GlobusDetails globusDetails;

    protected DatasetDetails() {
    }

    @Default
    public DatasetDetails(final String datasetId,
                          final String datasetName,
                          final GenomeBuild genomeBuild,
                          final String filesetId,
                          final FilesetType filesetType,
                          final String createdBy,
                          final String updatedBy) {
        this.datasetId = datasetId;
        this.datasetName = datasetName;
        this.genomeBuild = genomeBuild;
        this.filesetId = filesetId;
        this.filesetType = filesetType;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        newDatasetDetails = true;
    }

    public DatasetDetails(final String datasetId,
                          final String datasetName,
                          final GenomeBuild genomeBuild,
                          final FilesetType filesetType,
                          final GlobusDetails globusDetails,
                          final LocalDateTime createdOn,
                          final LocalDateTime updatedOn) {
        this.datasetId = datasetId;
        this.datasetName = datasetName;
        this.genomeBuild = genomeBuild;
        this.filesetType = filesetType;
        this.globusDetails = globusDetails;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public DatasetDetails(final String datasetId,
                          final String datasetName,
                          final GenomeBuild genomeBuild) {
        this.datasetId = datasetId;
        this.datasetName = datasetName;
        this.genomeBuild = genomeBuild;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public GenomeBuild getGenomeBuild() {
        return genomeBuild;
    }

    public String getFilesetId() {
        return filesetId;
    }

    public FilesetType getFilesetType() {
        return filesetType;
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

    public GlobusDetails getGlobusDetails() {
        return globusDetails;
    }

    public void updateGenomeBuild(GenomeBuild genomeBuild) {
        this.genomeBuild = genomeBuild;
    }

    @Override
    public String getId() {
        return datasetId;
    }

    @Override
    public boolean isNew() {
        return newDatasetDetails || !hasText(datasetId);
    }
}
