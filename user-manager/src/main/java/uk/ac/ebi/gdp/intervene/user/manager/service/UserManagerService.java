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
package uk.ac.ebi.gdp.intervene.user.manager.service;

import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;

import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.user.manager.exception.UserAccountException.accountAlreadyExists;

public class UserManagerService implements IUserManagerService {

    private final IUserAccountPersistenceService userAccountPersistenceService;

    public UserManagerService(final IUserAccountPersistenceService userAccountPersistenceService) {
        this.userAccountPersistenceService = userAccountPersistenceService;
    }

    @Override
    public Mono<UserAccount> createUserAccount(final String authUserAccountId) {
        return userAccountPersistenceService
                .getUserAccount(authUserAccountId)
                .flatMap(authUserAccountR2DBC -> error(accountAlreadyExists(authUserAccountId)))
                .switchIfEmpty(userAccountPersistenceService.createAccount(authUserAccountId))
                .cast(UserAccount.class);
    }
}
