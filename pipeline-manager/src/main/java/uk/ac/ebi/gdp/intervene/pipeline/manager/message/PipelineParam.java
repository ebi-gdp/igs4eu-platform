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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public record PipelineParam(@JsonInclude(NON_EMPTY) Collection<Map<String, Object>> targetGenomes,
                            NXFParamsFile nxfParamsFile,
                            String id) {

    @JsonInclude(NON_EMPTY)
    public static class NXFParamsFile {
        @JsonProperty("pgs_id")
        String pgsIds;

        @JsonProperty("trait_efo")
        String traitIds;

        @JsonProperty("pgp_id")
        String publicationIds;

        @JsonIgnore
        FormatType formatType;

        @JsonProperty("target_build")
        String targetBuild;

        private NXFParamsFile() {
        }

        private NXFParamsFile(final String pgsIds,
                              final String traitIds,
                              final String publicationIds,
                              final FormatType formatType,
                              final String targetBuild) {
            this.pgsIds = pgsIds;
            this.traitIds = traitIds;
            this.publicationIds = publicationIds;
            this.formatType = formatType;
            this.targetBuild = targetBuild;
        }

        public static NXFParamsFile createWithPgsIds(final String pgsIds,
                                                     final FormatType formatType,
                                                     final String targetBuild) {
            return new NXFParamsFile(pgsIds, null, null, formatType, targetBuild);
        }

        public static NXFParamsFile createWithTraitIds(final String traitIds,
                                                       final FormatType formatType,
                                                       final String targetBuild) {
            return new NXFParamsFile(null, traitIds, null, formatType, targetBuild);
        }

        public static NXFParamsFile createWithPublicationIds(final String publicationIds,
                                                             final FormatType formatType,
                                                             final String targetBuild) {
            return new NXFParamsFile(null, null, publicationIds, formatType, targetBuild);
        }

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
