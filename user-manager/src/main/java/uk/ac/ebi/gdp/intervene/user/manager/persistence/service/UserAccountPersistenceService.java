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

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.gdp.intervene.user.manager.exception.UserAccountCreationException;
import uk.ac.ebi.gdp.intervene.user.manager.model.IUserInfo;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.AuthProvider;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.UserAccountDetails;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.repository.UserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.IAuthenticationService;

import java.util.Optional;

public class UserAccountPersistenceService implements IUserAccountPersistenceService {

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

    @Override
    public Optional<AuthUserAccount> getUserAccount(final String authUserAccountId) {
        return authUserAccountRepository.findById(authUserAccountId);
    }

    @Transactional(
            transactionManager = "userManagerTransactionManager",
            rollbackFor = Exception.class)
    @Override
    public UserAccount createAccount(final String authUserAccountId) throws UserAccountCreationException {
        try {
            //Get next user account id
            final String nextUserAccount = userAccountRepository.getNextUserAccount();

            // Fetch user details, call /userinfo from elixir. TODO Decide which auth service to call if multiple present
            final IAuthenticationService authenticationService = authenticationContext.getAuthenticationService();
            final IUserInfo userInfo = authenticationService.userInfo();

            // Build User Account
            final UserAccount userAccount = UserAccount.newAccount(
                    nextUserAccount,
                    userInfo.getGivenName(),
                    userInfo.getFamilyName(),
                    userInfo.getEmailId()
            );

            // Build User Account Details
            final UserAccountDetails userAccountDetails = new UserAccountDetails(
                    userAccount
            );
            userAccountDetailsRepository.save(userAccountDetails);
            userAccountRepository.save(userAccount);

            // Build Auth User Account
            final AuthUserAccount authUserAccount = AuthUserAccount.newAuthUserAccount(
                    authUserAccountId,
                    userAccount,
                    AuthProvider.ELIXIR
            );
            authUserAccountRepository.save(authUserAccount);
            return userAccount;
        } catch (Exception e) {
            throw new UserAccountCreationException(e.getMessage(), e);
        }
    }
}
