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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static org.springframework.util.StringUtils.hasText;

@Table("globus_user_details")
public class GlobusUserDetails implements Persistable<String> {
    @Id
    @Column("globus_username")
    private String username;

    @Column("globus_user_uid")
    private String userUID;

    @Column("intervene_user_id")
    private String interveneUserId;

    @Transient
    private boolean newGlobusUserDetails;

    protected GlobusUserDetails() {
    }

    public GlobusUserDetails(final String username,
                             final String userUID,
                             final String interveneUserId) {
        this.username = username;
        this.userUID = userUID;
        this.interveneUserId = interveneUserId;
        this.newGlobusUserDetails = true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getInterveneUserId() {
        return interveneUserId;
    }

    public void setInterveneUserId(String interveneUserId) {
        this.interveneUserId = interveneUserId;
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public boolean isNew() {
        return newGlobusUserDetails || !hasText(username);
    }
}
