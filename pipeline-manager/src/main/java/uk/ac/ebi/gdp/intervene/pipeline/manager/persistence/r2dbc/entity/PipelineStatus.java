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

public enum PipelineStatus {
    NEW("New"), PENDING("Pending"), COMPLETED("Succeeded"), STARTED("Deployed"), ERROR("Failed");

    private final String pipelineStatus;

    PipelineStatus(final String pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public String getPipelineStatus() {
        return pipelineStatus;
    }

    public static PipelineStatus getPipelineStatusByDescription(final String pipelineStatusArg) {
        for (final PipelineStatus pipelineStatus : values()) {
            if (pipelineStatus.getPipelineStatus().equalsIgnoreCase(pipelineStatusArg)) {
                return pipelineStatus;
            }
        }
        throw new IllegalArgumentException("No matching Status for value " + pipelineStatusArg);
    }
}
