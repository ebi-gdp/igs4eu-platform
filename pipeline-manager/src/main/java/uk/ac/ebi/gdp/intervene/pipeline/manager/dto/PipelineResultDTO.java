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

public class PipelineResultDTO {
    private String pipelineId;
    private String fileName;
    private String status;

    private PipelineResultDTO() {
    }

    public PipelineResultDTO(final String pipelineId,
                             final String fileName,
                             final String status) {
        this.pipelineId = pipelineId;
        this.fileName = fileName;
        this.status = status;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(final String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
