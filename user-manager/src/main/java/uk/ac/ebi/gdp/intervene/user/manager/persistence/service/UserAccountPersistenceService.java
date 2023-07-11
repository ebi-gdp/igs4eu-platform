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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.auth.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.model.IUserInfo;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountDetails;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.IAuthenticationService;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType.ELIXIR;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount.newAuthUserAccount;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount.newUserAccount;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountDetails.newUserAccountDetailsR2DBC;

public class UserAccountPersistenceService implements IUserAccountPersistenceService {
    private final Logger LOGGER = getLogger(UserAccountPersistenceService.class);
    private final UserAccountRepository userAccountRepository;
    private final UserAccountDetailsRepository userAccountDetailsRepository;
    private final AuthUserAccountRepository authUserAccountRepository;
    private final AuthenticationContext authenticationContext;

    public UserAccountPersistenceService(final UserAccountRepository userAccountRepository,
                                         final UserAccountDetailsRepository userAccountDetailsRepository,
                                         final AuthUserAccountRepository authUserAccountRepository,
                                         final AuthenticationContext authenticationContext) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountDetailsRepository = userAccountDetailsRepository;
        this.authUserAccountRepository = authUserAccountRepository;
        this.authenticationContext = authenticationContext;
    }

    @Transactional(
            transactionManager = "reactiveTransactionManager",
            rollbackFor = Exception.class)
    @Override
    public Mono<UserAccount> createAccount(final String authUserAccountId) {
        //Get next user account id
        LOGGER.info("Creating new User Account");
        return userAccountRepository
                .getNextUserAccount()
                .doOnNext(nextUserAccountId -> LOGGER.debug("Next User Account Id: {}", nextUserAccountId))
                .flatMap(nextUserAccountId -> {
                    // Fetch user details
                    return authenticationContext
                            .getAuthenticationService()
                            .flatMap(IAuthenticationService::userInfo)
                            .map(userInfo -> buildUserAccount(nextUserAccountId, userInfo));
                })
                .flatMap(userAccountRepository::save)
                .flatMap(userAccountR2DBC -> {
                    // Build User Account Details
                    final UserAccountDetails userAccountDetails = newUserAccountDetailsR2DBC(userAccountR2DBC.getUserId());
                    return userAccountDetailsRepository
                            .save(userAccountDetails)
                            .thenReturn(userAccountR2DBC);
                })
                .flatMap(userAccountR2DBC -> {
                    // Build Auth User Account
                    final AuthUserAccount authUserAccount = newAuthUserAccount(
                            authUserAccountId,
                            userAccountR2DBC.getUserId(),
                            ELIXIR
                    );
                    return authUserAccountRepository
                            .save(authUserAccount)
                            .thenReturn(userAccountR2DBC);
                });
    }

    @Override
    public Mono<UserAccount> getUserAccountById(final String platformUserAccountId) {
        return userAccountRepository.findUserAccountByUserId(platformUserAccountId);
    }

    @Override
    public Mono<AuthUserAccount> getUserAccountByAuthUserAccountId(final String authUserAccountId) {
        LOGGER.debug("Fetching User Account details for {}", authUserAccountId);
        return authUserAccountRepository.findAuthUserAccount(authUserAccountId);
    }

    private UserAccount buildUserAccount(final String nextUserAccountId,
                                         final IUserInfo userInfo) {
        return newUserAccount(
                nextUserAccountId,
                userInfo.getGivenName(),
                userInfo.getFamilyName(),
                userInfo.getEmailId()
        );
    }
}
