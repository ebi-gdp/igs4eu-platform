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

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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

    @Column("created_by")
    private String createdBy;

    @CreatedDate
    @Column("created_on")
    private LocalDateTime createdOn;

    @Column("updated_by")
    private String updatedBy;

    @LastModifiedDate
    @Column("updated_on")
    private LocalDateTime updatedOn;

    @Transient
    private boolean isNew;

    protected UserAccountDetails() {
    }

    private UserAccountDetails(final String userId,
                               final String createdBy,
                               final String updatedBy) {
        this.userId = userId;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.isNew = true;
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

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew || !hasText(userId);
    }

    /**
     * Static method to create new user account details.
     *
     * @param userId user id
     * @param createdBy user id who creates an account
     *
     * @return new {@link UserAccountDetails}
     */
    public static UserAccountDetails create(final String userId,
                                            final String createdBy) {
        return new UserAccountDetails(
                userId,
                createdBy,
                createdBy);
    }
}
