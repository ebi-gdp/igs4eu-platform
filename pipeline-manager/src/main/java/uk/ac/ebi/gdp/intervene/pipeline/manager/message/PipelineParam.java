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
package uk.ac.ebi.gdp.intervene.pipeline.manager.message;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Map;

public record PipelineParam(
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Collection<Map<String, String>> targetGenomes,
        NXFParamsFile nxfParamsFile,
        String nxfWork, String id) {

    public record NXFParamsFile(
            @JsonProperty("pgs_id") String pgsIds,
            @JsonIgnore FormatType formatType,
            String targetBuild
    ) {
        @JsonGetter("format")
        public String getFormatType() {
            return formatType.getFormatType();
        }
    }

    public enum FormatType {
        JSON("json");

        private final String formatType;

        FormatType(final String formatType) {
            this.formatType = formatType;
        }

        public String getFormatType() {
            return formatType;
        }
    }
}
