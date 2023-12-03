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

@Table("user_account_details")
public class UserAccountDetails implements Persistable<String> {

    @Id
    @Column("user_id")
    private String userId;

    @Transient
    private boolean newUserAccountDetails;

    private String createdBy;

    @Column("created_on")
    private LocalDateTime createdOn;

    private String updatedBy;

    @Column("updated_on")
    private LocalDateTime updatedOn;

    protected UserAccountDetails() {
    }

    /**
     * Constructor to support creation of new User account details.
     *
     * @param userId user id
     * @param newUserAccountDetails whether new or existing
     */
    private UserAccountDetails(final String userId,
                               final boolean newUserAccountDetails) {
        this.userId = userId;
        this.createdBy = userId;
        this.updatedBy = userId;
        this.newUserAccountDetails = newUserAccountDetails;
    }

    public String getUserId() {
        return userId;
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
     * Static method to create new user account details.
     *
     * @param userId user id
     *
     * @return new {@link UserAccountDetails}
     */
    public static UserAccountDetails newUserAccountDetails(final String userId) {
        return new UserAccountDetails(userId, true);
    }

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return newUserAccountDetails || !hasText(userId);
    }
}
