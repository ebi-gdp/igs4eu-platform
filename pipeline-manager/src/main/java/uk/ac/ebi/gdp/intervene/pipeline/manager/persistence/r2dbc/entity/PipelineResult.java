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

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;

public class PipelineResult implements Persistable<String> {
    @Id
    @Column("pipeline_id")
    private String pipelineId;

    @Column("file_download_path")
    private String fileDownloadPath;

    @CreatedBy
    private String createdBy;

    @Column
    private LocalDateTime createdOn;

    @LastModifiedBy
    private String updatedBy;

    @Column
    private LocalDateTime updatedOn;

    @Transient
    private boolean isNew;

    protected PipelineResult() {
    }

    private PipelineResult(final String pipelineId,
                           final String fileDownloadPath) {
        this.pipelineId = pipelineId;
        this.fileDownloadPath = fileDownloadPath;
        this.isNew = true;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getFileDownloadPath() {
        return fileDownloadPath;
    }

    public void setFileDownloadPath(String fileDownloadPath) {
        this.fileDownloadPath = fileDownloadPath;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String getId() {
        return pipelineId;
    }

    @Override
    public boolean isNew() {
        return isNew || !hasText(pipelineId);
    }

    /**
     * Static method to create new pipeline result.
     *
     * @param pipelineId pipeline id
     * @param fileDownloadPath file download path
     *
     * @return new pipeline result {@link PipelineResult}
     */
    public static PipelineResult create(final String pipelineId,
                                        final String fileDownloadPath) {
        return new PipelineResult(
                pipelineId,
                fileDownloadPath);
    }
}
