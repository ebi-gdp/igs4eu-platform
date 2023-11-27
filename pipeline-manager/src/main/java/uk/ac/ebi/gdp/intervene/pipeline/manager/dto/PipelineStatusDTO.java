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
package uk.ac.ebi.gdp.intervene.pipeline.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class PipelineStatusDTO {
    @JsonProperty("run_name")
    private String runName;

    @JsonProperty("event")
    private String status;

    @JsonProperty("utc_time")
    private LocalDateTime utcTime;

    @JsonProperty("trace_name")
    private String traceName;

    @JsonProperty("trace_exit")
    private byte traceExit;

    private PipelineStatusDTO() {
    }

    public String getRunName() {
        return runName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUtcTime() {
        return utcTime;
    }

    public String getTraceName() {
        return traceName;
    }

    public byte getTraceExit() {
        return traceExit;
    }
}
