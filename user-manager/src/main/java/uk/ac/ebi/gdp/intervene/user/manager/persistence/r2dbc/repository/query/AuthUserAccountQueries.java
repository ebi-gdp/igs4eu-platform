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
package uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.query;

import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;

/**
 * {@link UserAccount} {@link AuthUserAccount} related sql queries.
 */
public interface AuthUserAccountQueries {
    //@formatter:off
    String FIND_BY_AUTH_USER_ID = "SELECT" +
                                  " u.user_id," +
                                  " u.given_name," +
                                  " u.family_name," +
                                  " u.email_id," +
                                  " u.status," +
                                  " u.created_by," +
                                  " u.created_on," +
                                  " u.updated_by," +
                                  " u.updated_on," +
                                  " (SELECT " +
                                  "   consent_type " +
                                  "  FROM " +
                                  "   user_dpa_consent_audit_logs c " +
                                  "  WHERE " +
                                  "   c.user_id = a.user_id " +
                                  "  ORDER BY " +
                                  "   c.updated_on DESC " +
                                  "  LIMIT 1) AS consent_type," +
                                  " a.auth_user_id," +
                                  " a.auth_provider," +
                                  " a.status as auth_user_status," +
                                  " a.created_by as auth_user_created_by," +
                                  " a.created_on as auth_user_created_on," +
                                  " a.updated_by as auth_user_updated_by," +
                                  " a.updated_on as auth_user_updated_on " +
                                  "FROM" +
                                  " user_account u INNER JOIN auth_user_account a" +
                                  " ON u.user_id = a.user_id " +
                                  "WHERE" +
                                  " a.auth_user_id = :authUserId";
    //@formatter:on
}
