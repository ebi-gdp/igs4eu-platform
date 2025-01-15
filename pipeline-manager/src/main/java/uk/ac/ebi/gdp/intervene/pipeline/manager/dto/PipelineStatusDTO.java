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
    private Byte traceExit;

    private PipelineStatusDTO() {
    }

    private PipelineStatusDTO(final Builder builder) {
        this.runName = builder.runName;
        this.status = builder.status;
        this.utcTime = builder.utcTime;
        this.traceName = builder.traceName;
        this.traceExit = builder.traceExit;
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

    public Byte getTraceExit() {
        return traceExit;
    }

    public static class Builder {
        private final String runName;
        private final String status;
        private final LocalDateTime utcTime;
        private String traceName;
        private Byte traceExit;

        public Builder(final String runName,
                       final String status,
                       final LocalDateTime utcTime) {
            if (runName == null || status == null || utcTime == null) {
                throw new IllegalArgumentException("runName, status, and utcTime are required fields and cannot be null.");
            }
            this.runName = runName;
            this.status = status;
            this.utcTime = utcTime;
        }

        public Builder traceName(final String traceName) {
            this.traceName = traceName;
            return this;
        }

        public Builder traceExit(final Byte traceExit) {
            this.traceExit = traceExit;
            return this;
        }

        public PipelineStatusDTO build() {
            return new PipelineStatusDTO(this);
        }
    }
}

