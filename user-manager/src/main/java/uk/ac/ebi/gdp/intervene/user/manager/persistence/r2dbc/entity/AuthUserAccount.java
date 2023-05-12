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

    @Column("created_on")
    private LocalDateTime createdOn;

    @Column("updated_on")
    private LocalDateTime updatedOn;

    protected AuthUserAccount() {
    }

    private AuthUserAccount(final String authUserId,
                            final String userId,
                            final AuthProviderType authProviderType,
                            final AuthUserAccountStatus status,
                            final boolean newAuthUserAccount) {
        this.authUserId = authUserId;
        this.userId = userId;
        this.authProviderType = authProviderType;
        this.status = status;
        this.newAuthUserAccount = newAuthUserAccount;
    }

    public AuthUserAccount(final String authUserId,
                           final AuthProviderType authProviderType,
                           final AuthUserAccountStatus status,
                           final UserAccount userAccount) {
        this(authUserId, userAccount.getUserId(), authProviderType, status, false);
        this.userAccount = userAccount;
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

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public static AuthUserAccount newAuthUserAccount(final String authUserId,
                                                     final String userId,
                                                     final AuthProviderType authProviderType) {
        return new AuthUserAccount(
                authUserId,
                userId,
                authProviderType,
                AuthUserAccountStatus.ENABLED,
                true
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
