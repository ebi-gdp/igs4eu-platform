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

    @Column("created_on")
    private LocalDateTime createdOn;

    @Column("updated_on")
    private LocalDateTime updatedOn;

    protected UserAccount() {
    }

    public UserAccount(final String userId,
                       final String firstName,
                       final String lastName,
                       final String emailId,
                       final UserAccountStatus userAccountStatus) {
        this.userId = userId;
        this.givenName = firstName;
        this.familyName = lastName;
        this.emailId = emailId;
        this.status = userAccountStatus;
    }

    private UserAccount(final String userId,
                        final String firstName,
                        final String lastName,
                        final String emailId,
                        final UserAccountStatus userAccountStatus,
                        final boolean newUserAccount) {
        this(userId, firstName, lastName, emailId, userAccountStatus);
        this.newUserAccount = newUserAccount;
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

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public static UserAccount newUserAccount(final String userId,
                                             final String firstName,
                                             final String lastName,
                                             final String emailId) {
        return new UserAccount(
                userId,
                firstName,
                lastName,
                emailId,
                UserAccountStatus.ACTIVE,
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
