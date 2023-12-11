/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

public class PipelineDetails implements Persistable<String> {
    @Id
    @Column("pipeline_id")
    private String pipelineId;

    @Column("pipeline_uid")
    private String pipelineUID;

    @Column("user_id")
    private String userId;

    @Column("dataset_id")
    private String datasetId;

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
    private boolean newPipelineDetails;

    @Transient
    private DatasetDetails datasetDetails;

    @Transient
    private PipelineExecutionStatus pipelineExecutionStatus;

    protected PipelineDetails() {
    }

    private PipelineDetails(final String pipelineId,
                            final String userId,
                            final String datasetId) {
        this.pipelineId = pipelineId;
        this.pipelineUID = UUID.randomUUID().toString();
        this.userId = userId;
        this.datasetId = datasetId;
        this.newPipelineDetails = true;
        this.createdBy = userId;
        this.updatedBy = userId;
        this.pipelineExecutionStatus = new PipelineExecutionStatus(pipelineId, userId);
    }

    public PipelineDetails(final String pipelineId,
                           final String pipelineUID,
                           final String userId,
                           final String datasetId,
                           final String createdBy,
                           final LocalDateTime createdOn,
                           final String updatedBy,
                           final LocalDateTime updatedOn) {
        this.pipelineId = pipelineId;
        this.pipelineUID = pipelineUID;
        this.userId = userId;
        this.datasetId = datasetId;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }

    private PipelineDetails(final String pipelineId,
                            final String pipelineUID,
                            final String userId,
                            final String datasetId,
                            final String createdBy,
                            final LocalDateTime createdOn,
                            final String updatedBy,
                            final LocalDateTime updatedOn,
                            final PipelineExecutionStatus pipelineExecutionStatus) {
        this(pipelineId, pipelineUID, userId, datasetId, createdBy, createdOn,
                updatedBy, updatedOn);
        this.pipelineExecutionStatus = pipelineExecutionStatus;
    }

    private PipelineDetails(final String pipelineId,
                            final String pipelineUID,
                            final String userId,
                            final String datasetId,
                            final String createdBy,
                            final LocalDateTime createdOn,
                            final String updatedBy,
                            final LocalDateTime updatedOn,
                            final PipelineExecutionStatus pipelineExecutionStatus,
                            final DatasetDetails datasetDetails) {
        this(pipelineId, pipelineUID, userId, datasetId, createdBy, createdOn,
                updatedBy, updatedOn, pipelineExecutionStatus);
        this.datasetDetails = datasetDetails;
    }

    public static PipelineDetails create(final String pipelineId,
                                         final String userId,
                                         final String datasetId) {
        return new PipelineDetails(pipelineId, userId, datasetId);
    }

    public static PipelineDetails pipelineDetailsWithStatusAndDataset(final String pipelineId,
                                                                      final String pipelineUID,
                                                                      final String userId,
                                                                      final String datasetId,
                                                                      final String createdBy,
                                                                      final LocalDateTime createdOn,
                                                                      final String updatedBy,
                                                                      final LocalDateTime updatedOn,
                                                                      final PipelineExecutionStatus pipelineExecutionStatus,
                                                                      final DatasetDetails datasetDetails) {
        return new PipelineDetails(pipelineId, pipelineUID, userId, datasetId, createdBy,
                createdOn, updatedBy, updatedOn, pipelineExecutionStatus, datasetDetails);
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getPipelineUID() {
        return pipelineUID;
    }

    public void setPipelineUID(String pipelineUID) {
        this.pipelineUID = pipelineUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void updateDatasetId(final String datasetId) {
        this.datasetId = datasetId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public DatasetDetails getDatasetDetails() {
        return datasetDetails;
    }

    public PipelineExecutionStatus getPipelineExecutionStatus() {
        return pipelineExecutionStatus;
    }

    @Override
    public String getId() {
        return pipelineId;
    }

    @Override
    public boolean isNew() {
        return newPipelineDetails || !hasText(pipelineId);
    }
}
