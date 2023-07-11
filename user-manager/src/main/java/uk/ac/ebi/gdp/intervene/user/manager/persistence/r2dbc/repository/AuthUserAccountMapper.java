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

import io.r2dbc.spi.Row;
import uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus;

import java.util.function.BiFunction;

public class AuthUserAccountMapper implements BiFunction<Row, Object, AuthUserAccount> {

    @Override
    public AuthUserAccount apply(final Row row, final Object o) {
        final UserAccount userAccount = new UserAccount(
                row.get("user_id", String.class),
                row.get("given_name", String.class),
                row.get("family_name", String.class),
                row.get("email_id", String.class),
                UserAccountStatus.valueOf(row.get("status", String.class))
        );
        return new AuthUserAccount(
                row.get("auth_user_id", String.class),
                AuthProviderType.valueOf(row.get("auth_provider", String.class)),
                AuthUserAccountStatus.valueOf(row.get("auth_user_status", String.class)),
                userAccount
        );
    }
}
