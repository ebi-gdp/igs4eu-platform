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

import org.springframework.data.annotation.Id;
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

    @Column
    private PipelineStatus status;

    @Column
    private LocalDateTime createdOn;

    @Column
    private LocalDateTime updatedOn;

    @Transient
    private boolean newPipelineDetails;

    @Transient
    private DatasetDetails datasetDetails;

    protected PipelineDetails() {
    }

    public PipelineDetails(final String pipelineId,
                           final String userId,
                           final PipelineStatus status) {
        this.pipelineId = pipelineId;
        this.pipelineUID = UUID.randomUUID().toString();
        this.userId = userId;
        this.datasetId = "INTD00000000000";
        this.status = status;
        this.newPipelineDetails = true;
    }
    public PipelineDetails(final String pipelineId,
                           final String pipelineUID,
                           final String userId,
                           final PipelineStatus status,
                           final DatasetDetails datasetDetails) {
        this.pipelineId = pipelineId;
        this.pipelineUID = pipelineUID;
        this.userId = userId;
        this.status = status;
        this.datasetDetails = datasetDetails;
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

    public PipelineStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
    } //TODO: Check whether needed or not

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

    public void statusPending() {
        this.status = PipelineStatus.PENDING;
    }

    public void statusCompleted() {
        this.status = PipelineStatus.COMPLETED;
    }

    public void updateDatasetId(final String datasetId) {
        this.datasetId = datasetId;
    }

    public DatasetDetails getDatasetDetails() {
        return datasetDetails;
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
