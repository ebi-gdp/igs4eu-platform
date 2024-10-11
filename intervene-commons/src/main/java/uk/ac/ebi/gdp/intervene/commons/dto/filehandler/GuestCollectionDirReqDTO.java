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
package uk.ac.ebi.gdp.intervene.commons.dto.filehandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuestCollectionDirReqDTO {
    @Size(max = 4092, message = "Directory name can't be more than 4092 characters!")
    @NotEmpty
    private String directoryName;

    @Size(max = 512, message = "Globus user id can't be more than 512 characters!")
    @NotEmpty
    private String globusUserUID;

    @Size(max = 512, message = "Email id can't be more than 512 characters!")
    @NotEmpty
    private String notifyEmail;

    private GuestCollectionDirReqDTO() {
    }

    public GuestCollectionDirReqDTO(final String directoryName,
                                    final String globusUserUID,
                                    final String notifyEmail) {
        this.directoryName = directoryName;
        this.globusUserUID = globusUserUID;
        this.notifyEmail = notifyEmail;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getGlobusUserUID() {
        return globusUserUID;
    }

    public String getNotifyEmail() {
        return notifyEmail;
    }
}
