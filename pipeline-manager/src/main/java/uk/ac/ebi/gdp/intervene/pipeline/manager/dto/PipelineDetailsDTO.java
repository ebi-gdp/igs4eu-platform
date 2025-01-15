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
package uk.ac.ebi.gdp.intervene.pipeline.manager.dto;

import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;

import java.time.LocalDateTime;

public class PipelineDetailsDTO {
    private String pipelineId;
    private PipelineStatus pipelineStatus;
    private String traceName;
    private String traceExit;
    private LocalDateTime submittedOn;
    private LocalDateTime startedOn;
    private LocalDateTime endedOn;
    private DatasetDetailsDTO datasetDetails;
    private GlobusDetailsDTO globusDetails;

    public PipelineDetailsDTO() {
    }

    public PipelineDetailsDTO(final String pipelineId,
                              final PipelineStatus pipelineStatus) {
        this.pipelineId = pipelineId;
        this.pipelineStatus = pipelineStatus;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getPipelineStatus() {
        return getMeaningfulStatus(pipelineStatus);
    }

    public void setPipelineStatus(PipelineStatus pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public DatasetDetailsDTO getDatasetDetails() {
        return datasetDetails;
    }

    public void setDatasetDetails(DatasetDetailsDTO datasetDetails) {
        this.datasetDetails = datasetDetails;
    }

    public GlobusDetailsDTO getGlobusDetails() {
        return globusDetails;
    }

    public void setGlobusDetails(GlobusDetailsDTO globusDetails) {
        this.globusDetails = globusDetails;
    }

    public String getTraceName() {
        return traceName;
    }

    public void setTraceName(String traceName) {
        this.traceName = traceName;
    }

    public String getTraceExit() {
        return traceExit;
    }

    public void setTraceExit(String traceExit) {
        this.traceExit = traceExit;
    }

    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(LocalDateTime submittedOn) {
        this.submittedOn = submittedOn;
    }

    public LocalDateTime getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(LocalDateTime startedOn) {
        this.startedOn = startedOn;
    }

    public LocalDateTime getEndedOn() {
        return endedOn;
    }

    public void setEndedOn(LocalDateTime endedOn) {
        this.endedOn = endedOn;
    }

    private String getMeaningfulStatus(final PipelineStatus pipelineStatus) {
        return switch (pipelineStatus) {
            case NEW -> "Created";
            case PENDING -> "Submitted";
            case STARTED -> "Running";
            case COMPLETED -> "Completed";
            case ERROR -> "Failed";
        };
    }
}
