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
import uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType;

import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccountStatus.ENABLED;

@Table("auth_user_account")
public class AuthUserAccount implements Persistable<String> {

    @Id
    @Column("auth_user_id")
    private String authUserId;

    @Column("user_id")
    private String userId;

    @Column("auth_provider")
    private AuthProviderType authProviderType;

    @Column("status")
    private AuthUserAccountStatus status;

    @Transient
    private UserAccount userAccount;

    @Transient
    private boolean newAuthUserAccount;

    private String createdBy;

    @Column("created_on")
    private LocalDateTime createdOn;

    private String updatedBy;

    @Column("updated_on")
    private LocalDateTime updatedOn;

    protected AuthUserAccount() {
    }

    /**
     * Constructor to support creation of new Auth user account
     * also to support initialize default members for retrieval of
     * exiting object.
     *
     * @param authUserId auth user id
     * @param userId user account id
     * @param authProviderType auth provider type {@link AuthProviderType}
     * @param status account status of type {@link AuthUserAccountStatus}
     * @param newAuthUserAccount whether account is new or existing
     * @param createdBy user id
     * @param updatedBy user id
     */
    private AuthUserAccount(final String authUserId,
                            final String userId,
                            final AuthProviderType authProviderType,
                            final AuthUserAccountStatus status,
                            final boolean newAuthUserAccount,
                            final String createdBy,
                            final String updatedBy) {
        this.authUserId = authUserId;
        this.userId = userId;
        this.authProviderType = authProviderType;
        this.status = status;
        this.newAuthUserAccount = newAuthUserAccount;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    /**
     * Public constructor to build existing auth user account.
     *
     * @param authUserId auth user id
     * @param authProviderType auth provider type {@link AuthProviderType}
     * @param status account status of type {@link AuthUserAccountStatus}
     * @param createdBy user id
     * @param createdOn object creation time
     * @param updatedBy user id
     * @param updatedOn object updated time
     * @param userAccount {@link UserAccount}
     */
    public AuthUserAccount(final String authUserId,
                           final AuthProviderType authProviderType,
                           final AuthUserAccountStatus status,
                           final String createdBy,
                           final LocalDateTime createdOn,
                           final String updatedBy,
                           final LocalDateTime updatedOn,
                           final UserAccount userAccount) {
        this(authUserId, userAccount.getUserId(), authProviderType, status, false, createdBy, updatedBy);
        this.userAccount = userAccount;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public String getUserId() {
        return userId;
    }

    public AuthProviderType getAuthProvider() {
        return authProviderType;
    }

    public AuthUserAccountStatus getStatus() {
        return status;
    }

    public UserAccount getUserAccount() {
        return userAccount;
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
     * Static method to support creation if new auth user account.
     *
     * @param authUserId auth user id
     * @param userId user account id
     * @param authProviderType auth provider type {@link AuthProviderType}
     * @param createdBy user id
     * @param updatedBy user id
     *
     * @return newly created object {@link AuthUserAccount}
     */
    public static AuthUserAccount newAuthUserAccount(final String authUserId,
                                                     final String userId,
                                                     final AuthProviderType authProviderType,
                                                     final String createdBy,
                                                     final String updatedBy) {
        return new AuthUserAccount(
                authUserId,
                userId,
                authProviderType,
                ENABLED,
                true,
                createdBy,
                updatedBy
        );
    }

    @Override
    public String getId() {
        return authUserId;
    }

    @Override
    public boolean isNew() {
        return newAuthUserAccount || !hasText(authUserId);
    }
}
