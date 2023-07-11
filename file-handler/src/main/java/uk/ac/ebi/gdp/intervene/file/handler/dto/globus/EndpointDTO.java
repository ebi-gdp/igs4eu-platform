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
package uk.ac.ebi.gdp.intervene.file.handler.dto.globus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.DataType;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PermissionType;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PrincipalType;

public interface EndpointDTO {
    record Mkdir(@JsonIgnore DataType dataType,
                 String path) {

        @JsonGetter("DATA_TYPE")
        public String getDataType() {
            return dataType.getDataType();
        }
    }

    record Access(@JsonIgnore DataType dataType,
                  @JsonIgnore PrincipalType principalType,
                  String principal,
                  String path,
                  @JsonIgnore PermissionType permissionType,
                  @JsonProperty("notify_email") String notifyEmailId) {
        @JsonGetter("DATA_TYPE")
        public String getDataType() {
            return dataType.getDataType();
        }

        @JsonGetter("principal_type")
        public String getPrincipalType() {
            return principalType.getPrincipalType();
        }

        public String getPrincipal() {
            return principal;
        }

        public String getPath() {
            return path;
        }

        @JsonGetter("permissions")
        public String getPermissionType() {
            return permissionType.getAccess();
        }
    }
}
