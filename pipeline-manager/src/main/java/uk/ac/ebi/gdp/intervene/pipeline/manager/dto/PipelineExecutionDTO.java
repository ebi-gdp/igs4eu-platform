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

import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirResDTO;

public class PipelineExecutionDTO {
    private String genomeBuild;
    private String sampleSetName;
    private String polygenicScoreIds;
    private GuestCollectionDirResDTO globusDetails;

    private PipelineExecutionDTO() {
    }

    public String getGenomeBuild() {
        return genomeBuild;
    }

    public void setGenomeBuild(String genomeBuild) {
        this.genomeBuild = genomeBuild;
    }

    public String getSampleSetName() {
        return sampleSetName;
    }

    public void setSampleSetName(String sampleSetName) {
        this.sampleSetName = sampleSetName;
    }

    public String getPolygenicScoreIds() {
        return polygenicScoreIds;
    }

    public void setPolygenicScoreIds(String polygenicScoreIds) {
        this.polygenicScoreIds = polygenicScoreIds;
    }

    public GuestCollectionDirResDTO getGlobusDetails() {
        return globusDetails;
    }

    public void setGlobusDetails(GuestCollectionDirResDTO globusDetails) {
        this.globusDetails = globusDetails;
    }
}
