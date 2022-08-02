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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.entity;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 666820436008855294L;
    @Id
    @NotNull(message = "User account id can't be null")
    @Column(name = "user_id")
    private String userId;

    @Column
    private String givenName;

    @Column
    private String familyName;

    @Column
    private String emailId;

    @Enumerated(EnumType.STRING)
    @Type(type = "uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.PGEnumUserType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.UserAccountStatus")})
    @Column
    private UserAccountStatus status;

    @OneToOne(mappedBy = "userAccount", fetch = FetchType.LAZY)
    private UserAccountDetails userAccountDetails;

    @CreatedDate
    @Column
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedOn;

    protected UserAccount() {
    }

    private UserAccount(final String userId,
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

    public UserAccountDetails getUserAccountDetails() {
        return userAccountDetails;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public static UserAccount newAccount(final String userId,
                                         final String firstName,
                                         final String lastName,
                                         final String emailId) {
        return new UserAccount(
                userId,
                firstName,
                lastName,
                emailId,
                UserAccountStatus.ACTIVE
        );
    }
}
