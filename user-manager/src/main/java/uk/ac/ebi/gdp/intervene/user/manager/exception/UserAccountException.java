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
package uk.ac.ebi.gdp.intervene.user.manager.exception;

import org.springframework.http.HttpStatus;
import uk.ac.ebi.gdp.intervene.commons.exception.ClientException;

import java.io.Serial;

import static org.springframework.http.HttpStatus.CONFLICT;

public class UserAccountException extends ClientException {
    @Serial
    private static final long serialVersionUID = 8760042428916614428L;

    public UserAccountException(final HttpStatus httpStatus, final String message) {
        super(httpStatus, message);
        System.out.println("Inside UserAccountException");
    }

    public static UserAccountException accountAlreadyExists(final String accountId) {
        return new UserAccountException(CONFLICT, "User account %s already exists!".formatted(accountId));
    }
}
