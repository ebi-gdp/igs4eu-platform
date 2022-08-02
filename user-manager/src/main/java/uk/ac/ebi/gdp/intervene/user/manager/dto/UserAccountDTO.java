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
package uk.ac.ebi.gdp.intervene.user.manager.dto;

public class UserAccountDTO {
    private final String accountId;
    private final String givenName;
    private final String familyName;
    private final String emailId;

    public UserAccountDTO(final String accountId,
                          final String givenName,
                          final String familyName,
                          final String emailId) {
        this.accountId = accountId;
        this.givenName = givenName;
        this.familyName = familyName;
        this.emailId = emailId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getEmailId() {
        return emailId;
    }
}
