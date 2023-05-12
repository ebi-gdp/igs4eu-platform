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
package uk.ac.ebi.gdp.intervene.commons.dto.filehandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

public class GlobusUserIdentityDetailsWrapperDTO {

    private List<GlobusUserIdentityDetails> identities;

    public GlobusUserIdentityDetailsWrapperDTO() {
    }

    public List<GlobusUserIdentityDetails> getIdentities() {
        return identities;
    }

    public void setIdentities(final List<GlobusUserIdentityDetails> identities) {
        this.identities = identities;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GlobusUserIdentityDetails {
        private String uid;
        private String username;

        public GlobusUserIdentityDetails() {
        }

        public GlobusUserIdentityDetails(final String uid,
                                         final String username) {
            this.uid = uid;
            this.username = username;
        }

        public String getUid() {
            return uid;
        }

        @JsonSetter("id")
        public void setUid(final String uid) {
            this.uid = uid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }
    }
}
