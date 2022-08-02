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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class PipelineDetails {

    @Id
    @NotNull(message = "Pipeline id can't be null")
    @Column(name = "pipeline_id")
    private String pipelineId;

    @NotNull(message = "User id can't be null")
    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Type(type = "uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.entity.PGEnumUserType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "uk.ac.ebi.gdp.pipeline.manager.persistence.entity.PipelineStatus")})
    @Column
    private PipelineStatus status;

    @CreatedDate
    @Column
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedOn;

    protected PipelineDetails() {
    }

    public PipelineDetails(final String pipelineId,
                           final String userId,
                           final PipelineStatus status) {
        this.pipelineId = pipelineId;
        this.userId = userId;
        this.status = status;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
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
}
