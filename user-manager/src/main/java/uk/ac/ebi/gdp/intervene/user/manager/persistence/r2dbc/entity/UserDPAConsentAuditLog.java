/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("user_dpa_consent_audit_logs")
public class UserDPAConsentAuditLog implements Persistable<String> {
    private String userId;
    private String consentId;

    @Column("consent_type")
    private UserDPAConsentType userDPAConsentType;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdOn;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedOn;

    protected UserDPAConsentAuditLog() {
    }

    public UserDPAConsentAuditLog(final String userId,
                                  final String consentId,
                                  final UserDPAConsentType userDPAConsentType) {
        this.userId = userId;
        this.consentId = consentId;
        this.userDPAConsentType = userDPAConsentType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public UserDPAConsentType getConsentType() {
        return userDPAConsentType;
    }

    public void setConsentType(UserDPAConsentType userDPAConsentType) {
        this.userDPAConsentType = userDPAConsentType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    @Override
    public String getId() {
        return userId + "-" + consentId;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
