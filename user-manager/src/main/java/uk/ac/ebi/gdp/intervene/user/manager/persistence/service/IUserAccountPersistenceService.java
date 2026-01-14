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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.service;

import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.model.IUserInfo;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;

/**
 * User Account persistence service.
 */
public interface IUserAccountPersistenceService {
    /**
     * @param authUserAccountId OIDC user id
     *
     * @return {@link AuthUserAccount}
     */
    Mono<AuthUserAccount> getUserAccountByAuthUserAccountId(String authUserAccountId);

    /**
     * @param authUserAccountId OIDC user id
     * @param userInfo impl object built from accessing OIDC /userinfo endpoint
     *
     * @return {@link UserAccount}
     */
    Mono<UserAccount> createAccount(String authUserAccountId, IUserInfo userInfo);

    /**
     * @param accountId platform user id
     *
     * @return {@link UserAccount}
     */
    Mono<UserAccount> getUserAccountById(String accountId);

    /**
     * @param email email id
     *
     * @return {@link UserAccount}
     */
    Mono<UserAccount> getUserAccountByEmail(String email);

    /**
     * @param authUserAccountId auth user id (sub)
     * @param userAccount user account
     *
     * @return {@link UserAccount}
     */
    Mono<UserAccount> updateAuthUserAccount(String authUserAccountId, UserAccount userAccount);
}
