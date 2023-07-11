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

public class PipelineDetailsDTO {
    private String pipelineId;
    private PipelineStatus pipelineStatus;
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

    public PipelineStatus getPipelineStatus() {
        return pipelineStatus;
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

    /*@JsonIgnoreProperties(ignoreUnknown = true)
    public static class GlobusDetailsDTO extends GuestCollectionDirResDTO {
        private String userAccountUID;
        private String filesetId;

        public String getUserAccountUID() {
            return userAccountUID;
        }

        public void setUserAccountUID(String userAccountUID) {
            this.userAccountUID = userAccountUID;
        }

        public String getFilesetId() {
            return filesetId;
        }

        public void setFilesetId(String filesetId) {
            this.filesetId = filesetId;
        }
    }*/
}
