/*
 *
 * Copyright 2023 EMBL - European Bioinformatics Institute
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType;
import uk.ac.ebi.gdp.intervene.user.manager.auth.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.UserAccountPersistenceService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserAccountPersistenceServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAccountDetailsRepository userAccountDetailsRepository;

    @Mock
    private AuthUserAccountRepository authUserAccountRepository;

    @Mock
    private AuthenticationContext authenticationContext;

    @InjectMocks
    private UserAccountPersistenceService userAccountPersistenceService;

    @Test
    public void verifyCreateUserAccount() {

        final String authUserId = "d45ae07cfe294cfd134f19d93e79c272c43992ff@elixir-europe.org";

        final AuthUserAccount authUserAccount = new AuthUserAccount(
                authUserId,
                AuthProviderType.ELIXIR,
                AuthUserAccountStatus.ENABLED,
                new UserAccount(
                        "INTU00000000001",
                        "Ashutosh",
                        "Shimpi",
                        "ashutosh@ebi.ac.uk",
                        UserAccountStatus.ACTIVE
                )
        );

        when(authUserAccountRepository
                .findAuthUserAccount(authUserId))
                .thenReturn(Mono.just(authUserAccount));

        final Mono<AuthUserAccount> userAccount = userAccountPersistenceService.getUserAccountByAuthUserAccountId(authUserId);

        StepVerifier
                .create(userAccount)
                .consumeNextWith(resultAuthUserAccount -> assertEquals(authUserId, resultAuthUserAccount.getAuthUserId()))
                .verifyComplete();
    }
}
