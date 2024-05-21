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

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.Default;

import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;

@Table("dataset_details")
public class DatasetDetails implements Persistable<String> {
    @Id
    private String datasetId;

    private String datasetName;

    private GenomeBuild genomeBuild;

    private String filesetId;

    private FilesetType filesetType;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdOn;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedOn;

    @Transient
    private GlobusDetails globusDetails;

    @Transient
    private boolean isNew;

    protected DatasetDetails() {
    }

    private DatasetDetails(final String datasetId,
                           final String datasetName,
                           final GenomeBuild genomeBuild,
                           final FilesetType filesetType,
                           final String createdBy,
                           final String updatedBy) {
        this.datasetId = datasetId;
        this.datasetName = datasetName;
        this.genomeBuild = genomeBuild;
        this.filesetType = filesetType;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    private DatasetDetails(final String datasetId,
                           final String datasetName,
                           final GenomeBuild genomeBuild,
                           final FilesetType filesetType,
                           final String createdBy,
                           final LocalDateTime createdOn,
                           final String updatedBy,
                           final LocalDateTime updatedOn) {
        this(datasetId, datasetName, genomeBuild, filesetType,
                createdBy, updatedBy);
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    private DatasetDetails(final String datasetId,
                           final String datasetName,
                           final GenomeBuild genomeBuild,
                           final FilesetType filesetType,
                           final GlobusDetails globusDetails,
                           final String createdBy,
                           final LocalDateTime createdOn,
                           final String updatedBy,
                           final LocalDateTime updatedOn) {
        this(datasetId, datasetName, genomeBuild, filesetType, createdBy,
                createdOn, updatedBy, updatedOn);
        this.globusDetails = globusDetails;
    }

    /**
     * Default constructor for creating new dataset details
     * entity via mapper.
     *
     * @param datasetId dataset id
     * @param datasetName dataset name
     * @param genomeBuild {link GenomeBuild}
     * @param filesetId fileset id
     * @param filesetType {link FilesetType}
     * @param createdBy user id
     * @param updatedBy user id
     */
    @Default
    public DatasetDetails(final String datasetId,
                          final String datasetName,
                          final GenomeBuild genomeBuild,
                          final String filesetId,
                          final FilesetType filesetType,
                          final String createdBy,
                          final String updatedBy) {
        this(datasetId, datasetName, genomeBuild, filesetType,
                createdBy, updatedBy);
        this.filesetId = filesetId;
        isNew = true;
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
        return isNew || !hasText(datasetId);
    }

    /**
     * Load dataset details.
     *
     * @param datasetId dataset id
     * @param datasetName dataset name
     * @param genomeBuild {@link GenomeBuild}
     * @param filesetType {@link FilesetType}
     * @param createdBy user id
     * @param createdOn {@link LocalDateTime}
     * @param updatedBy user id
     * @param updatedOn {@link LocalDateTime}
     *
     * @return {@link DatasetDetails}
     */
    public static DatasetDetails load(final String datasetId,
                                      final String datasetName,
                                      final GenomeBuild genomeBuild,
                                      final FilesetType filesetType,
                                      final String createdBy,
                                      final LocalDateTime createdOn,
                                      final String updatedBy,
                                      final LocalDateTime updatedOn) {
        return new DatasetDetails(datasetId, datasetName, genomeBuild,
                filesetType, createdBy, createdOn, updatedBy, updatedOn);
    }

    /**
     * Load dataset details.
     *
     * @param datasetId dataset id
     * @param datasetName dataset name
     * @param genomeBuild {@link GenomeBuild}
     * @param filesetType {@link FilesetType}
     * @param globusDetails {@link GlobusDetails}
     * @param createdBy user id
     * @param createdOn {@link LocalDateTime}
     * @param updatedBy user id
     * @param updatedOn {@link LocalDateTime}
     *
     * @return {@link DatasetDetails}
     */
    public static DatasetDetails load(final String datasetId,
                                      final String datasetName,
                                      final GenomeBuild genomeBuild,
                                      final FilesetType filesetType,
                                      final GlobusDetails globusDetails,
                                      final String createdBy,
                                      final LocalDateTime createdOn,
                                      final String updatedBy,
                                      final LocalDateTime updatedOn) {
        return new DatasetDetails(datasetId, datasetName, genomeBuild, filesetType,
                globusDetails, createdBy, createdOn, updatedBy, updatedOn);
    }

    /**
     * This method only introduced to load Dataset name.
     * Required for downloading pipeline result from bucket.
     *
     * @param datasetId dataset Id
     * @param datasetName name of dataset name
     *
     * @return {@link DatasetDetails}
     */
    public static DatasetDetails loadDatasetIdAndNameOnly(final String datasetId,
                                                          final String datasetName) {
        final DatasetDetails datasetDetails = new DatasetDetails();
        datasetDetails.datasetId = datasetId;
        datasetDetails.datasetName = datasetName;
        return datasetDetails;
    }
}
