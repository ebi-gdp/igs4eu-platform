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
package uk.ac.ebi.gdp.intervene.user.manager.service.h2;

/**
 * This class is responsible for providing 'stored procedure' feature on be half of
 * H2 in memory database.
 */
public final class H2StoredProcedure {

    /**
     * As H2 database doesn't exactly support Stored procedure,
     * 'GET_NEXT_INTERVENE_USER_ACCOUNT_ID' stored procedure is being served
     * by this method; it acts as stored procedure.
     *
     * @param ignoreArgument not in use, just added argument to match actual method signature
     *
     * @return Next INTERVENE user id represented by {@link String}
     */
    public static String getNextInterveneUserAccountId(final String ignoreArgument) {
        final String getNextSequenceSQL = "SELECT NEXTVAL('intervene_user_account_id_number')";
        return H2DBUtil
                .getDatabaseClient()
                .sql(getNextSequenceSQL)
                .map((row, metadata) -> "INTU" + String.format("%011d", row.get(0, Long.class)))
                .first()
                .block();
    }
}
