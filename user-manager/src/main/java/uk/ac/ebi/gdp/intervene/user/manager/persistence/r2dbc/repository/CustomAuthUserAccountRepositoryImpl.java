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

import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.mapper.AuthUserAccountEntityMapper.fullMap;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.query.AuthUserAccountQueries.FIND_BY_AUTH_USER_ID;

/**
 * Custom implementation for accessing data from {@link AuthUserAccountRepository}
 * Currently there is no support for relationship in R2DBC, this repo implementation
 * serves the purpose.
 */
public class CustomAuthUserAccountRepositoryImpl implements CustomAuthUserAccountRepository {
    private final DatabaseClient databaseClient;

    public CustomAuthUserAccountRepositoryImpl(final DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AuthUserAccount> findAuthUserAccount(final String authUserId) {
        return databaseClient
                .sql(FIND_BY_AUTH_USER_ID)
                .bind("authUserId", authUserId)
                .map(fullMap()::apply)
                .one();
    }
}
