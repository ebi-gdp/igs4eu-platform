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
import uk.ac.ebi.gdp.intervene.user.manager.model.IUserInfo;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountDetails;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountRepository;

import static uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType.ELIXIR;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount.create;

public class UserAccountPersistenceService implements IUserAccountPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountPersistenceService.class);
    private final UserAccountRepository userAccountRepository;
    private final UserAccountDetailsRepository userAccountDetailsRepository;
    private final AuthUserAccountRepository authUserAccountRepository;

    public UserAccountPersistenceService(final UserAccountRepository userAccountRepository,
                                         final UserAccountDetailsRepository userAccountDetailsRepository,
                                         final AuthUserAccountRepository authUserAccountRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountDetailsRepository = userAccountDetailsRepository;
        this.authUserAccountRepository = authUserAccountRepository;
    }

    /**
     * Build & persist all user account related entities
     * Rollbacks transaction on runtime exception thrown.
     * {@inheritDoc}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<UserAccount> createAccount(final String authUserAccountId,
                                           final IUserInfo userInfo) {
        //Get next user account id
        LOGGER.info("Creating a new user account!");
        return userAccountRepository
                .getNextUserAccount()
                .doOnNext(nextUserAccountId -> LOGGER.info("Generated new user account Id"))
                .doOnNext(nextUserAccountId -> LOGGER.debug("Next new user account Id: {}", nextUserAccountId))
                .map(nextUserAccountId -> buildUserAccount(nextUserAccountId, userInfo))
                .flatMap(userAccountRepository::save)
                .doOnNext(userAccount -> LOGGER.info("Persisted new user account"))
                .flatMap(userAccount -> {
                    // Build User Account Details
                    return userAccountDetailsRepository
                            .save(UserAccountDetails.create(userAccount.getUserId(),
                                    userAccount.getUserId()))
                            .doOnNext(userAccountDetails -> LOGGER.info("Persisted new user account details"))
                            .thenReturn(userAccount);
                })
                .flatMap(userAccount -> {
                    // Build Auth User Account
                    return authUserAccountRepository
                            .save(create(
                                    authUserAccountId,
                                    userAccount.getUserId(),
                                    ELIXIR,
                                    userAccount.getUserId()
                            ))
                            .doOnNext(authUserAccount -> LOGGER.info("Persisted new auth user account"))
                            .thenReturn(userAccount);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<UserAccount> getUserAccountById(final String platformUserAccountId) {
        return userAccountRepository.findUserAccountByUserId(platformUserAccountId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<UserAccount> getUserAccountByEmail(final String email) {
        return userAccountRepository.findByEmailId(email);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<UserAccount> updateAuthUserAccount(final String authUserAccountId, final UserAccount userAccount) {
        return authUserAccountRepository
                .deleteByUserIdAndAuthProviderType(userAccount.getUserId(), ELIXIR)
                .doOnSuccess(unused -> LOGGER.info("Removed old auth user account for user {}", userAccount.getUserId()))
                .then(
                        authUserAccountRepository.save(create(authUserAccountId,
                        userAccount.getUserId(),
                        ELIXIR,
                        userAccount.getUserId()
                ))
                .doOnNext(authUserAccount -> LOGGER.info("Updated auth user account with new sub"))
                .thenReturn(userAccount));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AuthUserAccount> getUserAccountByAuthUserAccountId(final String authUserAccountId) {
        return authUserAccountRepository.findAuthUserAccount(authUserAccountId);
    }

    private UserAccount buildUserAccount(final String nextUserAccountId,
                                         final IUserInfo userInfo) {
        return UserAccount.create(
                nextUserAccountId,
                userInfo.getGivenName(),
                userInfo.getFamilyName(),
                userInfo.getEmailId(),
                nextUserAccountId
        );
    }
}
