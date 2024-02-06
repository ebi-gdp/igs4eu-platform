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

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static org.springframework.util.StringUtils.hasText;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.COMPLETED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.ERROR;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.NEW;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.PENDING;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.STARTED;

public class PipelineExecutionStatus implements Persistable<String> {
    @Id
    private String pipelineId;

    private PipelineStatus status;

    private String traceName;

    private byte traceExit;

    @Transient
    private PipelineDetails pipelineDetails;

    private LocalDateTime submittedOn;

    private LocalDateTime startedOn;

    private LocalDateTime endedOn;

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

    private PipelineExecutionStatus(final String pipelineId) {
        this.pipelineId = pipelineId;
        this.status = NEW;
        this.isNew = true;
    }

    private PipelineExecutionStatus(final String pipelineId,
                                    final PipelineStatus pipelineStatus,
                                    final String traceName,
                                    final byte traceExit,
                                    final LocalDateTime submittedOn,
                                    final LocalDateTime startedOn,
                                    final LocalDateTime endedOn,
                                    final String createdBy,
                                    final LocalDateTime createdOn,
                                    final String updatedBy,
                                    final LocalDateTime updatedOn) {
        this.pipelineId = pipelineId;
        this.status = pipelineStatus;
        this.traceName = traceName;
        this.traceExit = traceExit;
        this.submittedOn = submittedOn;
        this.startedOn = startedOn;
        this.endedOn = endedOn;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }

    private PipelineExecutionStatus(final String pipelineId,
                                    final PipelineStatus pipelineStatus,
                                    final String traceName,
                                    final byte traceExit,
                                    final LocalDateTime submittedOn,
                                    final LocalDateTime startedOn,
                                    final LocalDateTime endedOn,
                                    final String createdBy,
                                    final LocalDateTime createdOn,
                                    final String updatedBy,
                                    final LocalDateTime updatedOn,
                                    final PipelineDetails pipelineDetails) {
        this(pipelineId, pipelineStatus, traceName, traceExit, submittedOn,
                startedOn, endedOn, createdBy, createdOn, updatedBy, updatedOn);
        this.pipelineDetails = pipelineDetails;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public String getTraceName() {
        return traceName;
    }

    public byte getTraceExit() {
        return traceExit;
    }

    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    public LocalDateTime getStartedOn() {
        return startedOn;
    }

    public LocalDateTime getEndedOn() {
        return endedOn;
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

    public PipelineDetails getPipelineDetails() {
        return pipelineDetails;
    }

    @Override
    public String getId() {
        return pipelineId;
    }

    @Override
    public boolean isNew() {
        return isNew || !hasText(pipelineId);
    }

    public static PipelineExecutionStatus create(final String pipelineId) {
        return new PipelineExecutionStatus(pipelineId);
    }

    public static PipelineExecutionStatus load(final String pipelineId,
                                               final PipelineStatus pipelineStatus,
                                               final String traceName,
                                               final byte traceExit,
                                               final LocalDateTime submittedOn,
                                               final LocalDateTime startedOn,
                                               final LocalDateTime endedOn,
                                               final String createdBy,
                                               final LocalDateTime createdOn,
                                               final String updatedBy,
                                               final LocalDateTime updatedOn) {
        return new PipelineExecutionStatus(pipelineId, pipelineStatus, traceName, traceExit,
                submittedOn, startedOn, endedOn, createdBy, createdOn, updatedBy, updatedOn);
    }

    public static PipelineExecutionStatus load(final String pipelineId,
                                               final PipelineStatus pipelineStatus,
                                               final String traceName,
                                               final byte traceExit,
                                               final LocalDateTime submittedOn,
                                               final LocalDateTime startedOn,
                                               final LocalDateTime endedOn,
                                               final String createdBy,
                                               final LocalDateTime createdOn,
                                               final String updatedBy,
                                               final LocalDateTime updatedOn,
                                               final PipelineDetails pipelineDetails) {
        return new PipelineExecutionStatus(pipelineId, pipelineStatus, traceName, traceExit,
                submittedOn, startedOn, endedOn, createdBy, createdOn, updatedBy, updatedOn,
                pipelineDetails);
    }

    public void pending() {
        this.status = PENDING;
        this.submittedOn = now();
    }

    public void started(final LocalDateTime startedOn) {
        this.status = STARTED;
        this.startedOn = startedOn;
    }

    public void completed(final LocalDateTime endedOn) {
        this.status = COMPLETED;
        this.endedOn = endedOn;
    }

    public void error(final LocalDateTime endedOn,
                      final String traceName,
                      final byte traceExit) {
        this.status = ERROR;
        this.endedOn = endedOn;
        this.traceName = traceName;
        this.traceExit = traceExit;
    }
}
