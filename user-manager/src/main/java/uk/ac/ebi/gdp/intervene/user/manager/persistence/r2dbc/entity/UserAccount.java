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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus.ACTIVE;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentType.GIVEN;

@Table("user_account")
public class UserAccount implements Persistable<String> {

    @Id
    private String userId;
    private String givenName;
    private String familyName;
    private String emailId;
    private UserAccountStatus status;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdOn;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedOn;

    @Transient
    private UserDPAConsentType userDPAConsentType;

    @Transient
    private boolean isNew;

    protected UserAccount() {
    }

    private UserAccount(final String userId,
                        final String firstName,
                        final String lastName,
                        final String emailId,
                        final UserAccountStatus userAccountStatus,
                        final UserDPAConsentType userDPAConsentType,
                        final String createdBy,
                        final String updatedBy,
                        final boolean isNew) {
        this.userId = userId;
        this.givenName = firstName;
        this.familyName = lastName;
        this.emailId = emailId;
        this.status = userAccountStatus;
        this.userDPAConsentType = userDPAConsentType;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.isNew = isNew;
    }

    private UserAccount(final String userId,
                        final String firstName,
                        final String lastName,
                        final String emailId,
                        final UserAccountStatus userAccountStatus,
                        final UserDPAConsentType userDPAConsentType,
                        final String createdBy,
                        final LocalDateTime createdOn,
                        final String updatedBy,
                        final LocalDateTime updatedOn) {
        this(userId, firstName, lastName, emailId, userAccountStatus, userDPAConsentType,
                createdBy, updatedBy, false);
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    /**
     * Static method to create new user account.
     *
     * @param userId user id
     * @param firstName firstname
     * @param lastName lastname
     * @param emailId email id
     * @param createdBy user id who creates an account
     *
     * @return new user account {@link UserAccount}
     */
    public static UserAccount create(final String userId,
                                     final String firstName,
                                     final String lastName,
                                     final String emailId,
                                     final String createdBy) {
        return new UserAccount(
                userId,
                firstName,
                lastName,
                emailId,
                ACTIVE,
                GIVEN,
                createdBy,
                createdBy,
                true
        );
    }

    /**
     * Static method to load existing user account.
     *
     * @param userId user id
     * @param firstName firstname
     * @param lastName lastname
     * @param emailId email id
     * @param userAccountStatus account status {@link UserAccountStatus}
     * @param userDPAConsentType {@link UserDPAConsentType}
     * @param createdBy user id
     * @param createdOn object creation timestamp
     * @param updatedBy user id
     * @param updatedOn object update timestamp
     */
    public static UserAccount load(final String userId,
                                   final String firstName,
                                   final String lastName,
                                   final String emailId,
                                   final UserAccountStatus userAccountStatus,
                                   final UserDPAConsentType userDPAConsentType,
                                   final String createdBy,
                                   final LocalDateTime createdOn,
                                   final String updatedBy,
                                   final LocalDateTime updatedOn) {
        return new UserAccount(userId, firstName, lastName, emailId, userAccountStatus,
                userDPAConsentType, createdBy, createdOn, updatedBy, updatedOn);
    }

    public String getUserId() {
        return userId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getEmailId() {
        return emailId;
    }

    public UserAccountStatus getStatus() {
        return status;
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

    public UserDPAConsentType getConsentType() {
        return userDPAConsentType;
    }

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew || !hasText(userId);
    }
}
