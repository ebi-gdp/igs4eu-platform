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
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class AuthUserAccount implements Serializable {

    private static final long serialVersionUID = 2228204252453363833L;

    @Id
    @NotNull(message = "Auth user account id can't be null")
    @Column(name = "auth_user_id")
    private String authUserId;

    @OneToOne
    @NotNull(message = "User account id can't be null")
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_USER_ACCOUNT_ID"))
    private UserAccount userAccount;

    @Enumerated(EnumType.STRING)
    @Type(type = "uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.PGEnumUserType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.AuthProvider")})
    @Column
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Type(type = "uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.PGEnumUserType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.AuthUserAccountStatus")})
    @Column
    private AuthUserAccountStatus status;

    @CreatedDate
    @Column
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedOn;

    protected AuthUserAccount() {
    }

    private AuthUserAccount(final String authUserId,
                            final UserAccount userAccount,
                            final AuthProvider authProvider,
                            final AuthUserAccountStatus status) {
        this.authUserId = authUserId;
        this.userAccount = userAccount;
        this.authProvider = authProvider;
        this.status = status;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public AuthUserAccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public static AuthUserAccount newAuthUserAccount(final String authUserId,
                                                     final UserAccount userAccount,
                                                     final AuthProvider authProvider) {
        return new AuthUserAccount(
                authUserId,
                userAccount,
                authProvider,
                AuthUserAccountStatus.ENABLED
        );
    }
}
