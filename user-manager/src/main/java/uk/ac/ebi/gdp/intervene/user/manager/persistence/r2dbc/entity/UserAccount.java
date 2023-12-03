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

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus.ACTIVE;

@Table("user_account")
public class UserAccount implements Persistable<String> {

    @Id
    @Column("user_id")
    private String userId;

    @Column("given_name")
    private String givenName;

    @Column("family_name")
    private String familyName;

    @Column("email_id")
    private String emailId;

    @Column("status")
    private UserAccountStatus status;

    @Transient
    private boolean newUserAccount;

    private String createdBy;

    @Column("created_on")
    private LocalDateTime createdOn;

    private String updatedBy;

    @Column("updated_on")
    private LocalDateTime updatedOn;

    protected UserAccount() {
    }

    /**
     * Constructor to support creation of new user account
     * also to support initialize default members for retrieval of
     * exiting account.
     *
     * @param userId user id
     * @param firstName firstname
     * @param lastName lastname
     * @param emailId email id
     * @param userAccountStatus account status {@link UserAccountStatus}
     * @param createdBy user id
     * @param updatedBy user id
     */
    private UserAccount(final String userId,
                        final String firstName,
                        final String lastName,
                        final String emailId,
                        final UserAccountStatus userAccountStatus,
                        final String createdBy,
                        final String updatedBy) {
        this.userId = userId;
        this.givenName = firstName;
        this.familyName = lastName;
        this.emailId = emailId;
        this.status = userAccountStatus;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    /**
     * Constructor to create new user account.
     *
     * @param userId user id
     * @param firstName firstname
     * @param lastName lastname
     * @param emailId email id
     * @param userAccountStatus account status {@link UserAccountStatus}
     * @param newUserAccount whether account is new or existing
     */
    private UserAccount(final String userId,
                        final String firstName,
                        final String lastName,
                        final String emailId,
                        final UserAccountStatus userAccountStatus,
                        final boolean newUserAccount) {
        this(userId, firstName, lastName, emailId, userAccountStatus, userId, userId);
        this.newUserAccount = newUserAccount;
    }

    /**
     * Public constructor to build existing user account.
     *
     * @param userId user id
     * @param firstName firstname
     * @param lastName lastname
     * @param emailId email id
     * @param userAccountStatus account status {@link UserAccountStatus}
     * @param createdBy user id
     * @param createdOn object creation timestamp
     * @param updatedBy user id
     * @param updatedOn object update timestamp
     */
    public UserAccount(final String userId,
                       final String firstName,
                       final String lastName,
                       final String emailId,
                       final UserAccountStatus userAccountStatus,
                       final String createdBy,
                       final LocalDateTime createdOn,
                       final String updatedBy,
                       final LocalDateTime updatedOn) {
        this(userId, firstName, lastName, emailId, userAccountStatus, createdBy, updatedBy);
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
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

    /**
     * Static method to create new user account.
     *
     * @param userId user id
     * @param firstName firstname
     * @param lastName lastname
     * @param emailId email id
     *
     * @return new user account {@link UserAccount}
     */
    public static UserAccount newUserAccount(final String userId,
                                             final String firstName,
                                             final String lastName,
                                             final String emailId) {
        return new UserAccount(
                userId,
                firstName,
                lastName,
                emailId,
                ACTIVE,
                true
        );
    }

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return newUserAccount || !hasText(userId);
    }
}
