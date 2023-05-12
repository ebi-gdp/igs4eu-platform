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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;

public class CustomAuthUserAccountRepositoryImpl implements CustomAuthUserAccountRepository {

    private final DatabaseClient databaseClient;

    public CustomAuthUserAccountRepositoryImpl(final DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<AuthUserAccount> findAuthUserAccount(final String authUserId) {
        //@formatter:off
        final String query = "SELECT " +
                             " a.user_id," +
                             " a.given_name," +
                             " a.family_name," +
                             " a.email_id," +
                             " a.status," +
                             " a.created_on," +
                             " a.updated_on," +
                             " b.auth_user_id," +
                             " b.auth_provider," +
                             " b.status as auth_user_status " +
                             "FROM " +
                             " user_account a INNER JOIN auth_user_account b ON a.user_id = b.user_id " +
                             "WHERE b.auth_user_id = :authUserId";
        //@formatter:on

        return databaseClient
                .sql(query)
                .bind("authUserId", authUserId)
                .map((row, rowMetadata) -> new AuthUserAccountMapper().apply(row, rowMetadata))
                .one();
    }
}
