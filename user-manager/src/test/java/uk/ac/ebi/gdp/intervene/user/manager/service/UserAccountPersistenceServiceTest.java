/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.test.StepVerifier;
import uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter;
import uk.ac.ebi.gdp.intervene.user.manager.model.IUserInfo;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.UserAccountPersistenceService;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType.ELIXIR;

@EnableTransactionManagement
@EnableR2dbcAuditing
@ExtendWith(SpringExtension.class)
@DataR2dbcTest
public class UserAccountPersistenceServiceTest {

    @Autowired
    private IUserAccountPersistenceService userAccountPersistenceService;

    @Autowired
    private IUserInfo userInfo;

    //User 1
    private static final String authUser1AccountId = "d65ae06ftg456cfd789f19d93e79c123c56982ff@lifescience-ri.eu";
    private static final String givenName = "Intervene_User1_Given_Name";
    private static final String familyName = "Intervene_User1_Family_Name";
    private static final String userId1 = "INTU00000000001";
    private static final String emailId = "intervene_user1@ebi.ac.uk";
    private static final String createdBy1 = userId1;

    //User 2
    private static final String authUser2AccountId = "d75vb06ftg123cfd789f19d93e79c123c56982ff@lifescience-ri.eu";
    private static final String userId2 = "INTU00000000002";
    private static final String createdBy2 = userId2;

    @Test
    public void whenCreateAccountWithValidData_thenUserAccountIsCreated() {
        StepVerifier
                .create(userAccountPersistenceService.createAccount(authUser1AccountId, userInfo))
                .consumeNextWith(userAccount -> assertThat(userAccount)
                        .usingComparator(userAccountComparator())
                        .isEqualTo(getUser1AccountDetails()))
                .verifyComplete();
    }

    @Test
    public void whenGetUserAccountByIdWithValidData_thenUserAccountDetailsReturned() {
        StepVerifier
                .create(userAccountPersistenceService.getUserAccountById(userId2))
                .consumeNextWith(userAccountRetrieved ->
                        assertThat(userAccountRetrieved)
                                .usingComparator(userAccountComparator())
                                .isEqualTo(getUser2AccountDetails()))
                .verifyComplete();

    }

    @Test
    public void whenGetUserAccountByAuthUserAccountIdWithValidData_thenUserAccountDetailsReturned() {
        StepVerifier
                .create(userAccountPersistenceService.getUserAccountByAuthUserAccountId(authUser2AccountId))
                .consumeNextWith(userAccountRetrieved ->
                        assertThat(userAccountRetrieved)
                                .usingComparator(authUserAccountComparator())
                                .isEqualTo(getAuthUserAccountDetails()))
                .verifyComplete();

    }

    private UserAccount getUser1AccountDetails() {
        return UserAccount.create(
                userId1,
                givenName,
                familyName,
                emailId,
                createdBy1
        );
    }

    private UserAccount getUser2AccountDetails() {
        return UserAccount.create(
                userId2,
                "Intervene_User2_Given_Name",
                "Intervene_User2_Family_Name",
                "intervene_user2@ebi.ac.uk",
                userId2
        );
    }

    private AuthUserAccount getAuthUserAccountDetails() {
        return AuthUserAccount.create(
                authUser2AccountId,
                userId2,
                ELIXIR,
                createdBy2
        );
    }

    private static Comparator<UserAccount> userAccountComparator() {
        return (userAccount1, userAccount2) -> comparing(UserAccount::getUserId)
                .thenComparing(UserAccount::getGivenName)
                .thenComparing(UserAccount::getFamilyName)
                .thenComparing(UserAccount::getEmailId)
                .thenComparing(UserAccount::getStatus)
                .thenComparing(UserAccount::getCreatedBy)
                .thenComparing(UserAccount::getUpdatedBy)
                .compare(userAccount1, userAccount2);
    }

    private static Comparator<AuthUserAccount> authUserAccountComparator() {
        return (authUserAccount1, authUserAccount2) -> comparing(AuthUserAccount::getId)
                .thenComparing(AuthUserAccount::getUserId)
                .thenComparing(AuthUserAccount::getAuthProvider)
                .thenComparing(AuthUserAccount::getStatus)
                .thenComparing(AuthUserAccount::getCreatedBy)
                .thenComparing(AuthUserAccount::getUpdatedBy)
                .compare(authUserAccount1, authUserAccount2);
    }

    @TestConfiguration
    static class TestConfig extends AbstractR2dbcConfiguration {

        @Bean
        public IUserAccountPersistenceService userAccountPersistenceService(final UserAccountRepository userAccountRepository,
                                                                            final UserAccountDetailsRepository userAccountDetailsRepository,
                                                                            final AuthUserAccountRepository authUserAccountRepository) {
            return new UserAccountPersistenceService(
                    userAccountRepository,
                    userAccountDetailsRepository,
                    authUserAccountRepository
            );
        }

        @Bean
        @Override
        public ConnectionFactory connectionFactory() {
            return ConnectionFactoryBuilder
                    .withUrl("r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE")
                    .build();
        }

        @Override
        protected List<Object> getCustomConverters() {
            return List.of(
                    new EnumConverter.AuthProviderWritingConverter(),
                    new EnumConverter.AuthUserAccountStatusWritingTypeConverter(),
                    new EnumConverter.UserAccountStatusTypeWritingConverter()
            );
        }

        @Bean("r2dbcDatabaseClient")
        public DatabaseClient databaseClient(final ConnectionFactory connectionFactory) {
            return DatabaseClient
                    .builder()
                    .connectionFactory(connectionFactory)
                    .build();
        }

        @Bean("reactiveTransactionManager")
        public ReactiveTransactionManager transactionManager(final ConnectionFactory connectionFactory) {
            return new R2dbcTransactionManager(connectionFactory);
        }

        @Bean
        @Override
        public R2dbcCustomConversions r2dbcCustomConversions() {
            return new R2dbcCustomConversions(getStoreConversions(),
                    List.of(new UserAccountStatusToStringConverter()));
        }

        @Bean
        public IUserInfo userInfo() {
            return new IUserInfo() {

                @Override
                public String getSubject() {
                    return authUser1AccountId;
                }

                @Override
                public String getGivenName() {
                    return givenName;
                }

                @Override
                public String getFamilyName() {
                    return familyName;
                }

                @Override
                public String getEmailId() {
                    return emailId;
                }
            };
        }

        public static class UserAccountStatusToStringConverter implements Converter<UserAccountStatus, String> {
            @Override
            public String convert(UserAccountStatus userAccountStatus) {
                return userAccountStatus.name();
            }
        }
    }
}
