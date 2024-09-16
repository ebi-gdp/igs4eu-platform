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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.mapper;

import io.r2dbc.spi.Row;
import uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentType;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus;

import java.util.function.BiFunction;

import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentType.NOT_GIVEN;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.query.QueryUtil.getLocalDateTime;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.query.QueryUtil.getString;

/**
 * Auth user account mapper to build object from database record.
 */
public interface AuthUserAccountEntityMapper {
    static BiFunction<Row, Object, AuthUserAccount> fullMap() {
        return (row, object) -> {
            final String consentType = getString("consent_type", row);
            final UserAccount userAccount = UserAccount.load(
                    getString("user_id", row),
                    getString("given_name", row),
                    getString("family_name", row),
                    getString("email_id", row),
                    UserAccountStatus.valueOf(getString("status", row)),
                    UserDPAConsentType.valueOf("".equals(consentType) ? NOT_GIVEN.name() : consentType),
                    getString("created_by", row),
                    getLocalDateTime("created_on", row),
                    getString("updated_by", row),
                    getLocalDateTime("updated_on", row)
            );
            return AuthUserAccount.load(
                    getString("auth_user_id", row),
                    AuthProviderType.valueOf(getString("auth_provider", row)),
                    AuthUserAccountStatus.valueOf(getString("auth_user_status", row)),
                    getString("auth_user_created_by", row),
                    getLocalDateTime("auth_user_created_on", row),
                    getString("auth_user_updated_by", row),
                    getLocalDateTime("auth_user_updated_on", row),
                    userAccount
            );
        };
    }
}
