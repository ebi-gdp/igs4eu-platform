/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.commons.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.valueOf;

public class ClientException extends GenericException {
    @Serial
    private static final long serialVersionUID = -4850629090293414750L;

    protected ClientException(final HttpStatus httpStatus, final String message) {
        super(httpStatus, format("Client error - [%s]: %s", httpStatus, message));
    }

    public static ClientException clientException(final int httpStatusCode, final String message) {
        return new ClientException(valueOf(httpStatusCode), message);
    }

    public static ClientException clientException(final HttpStatus httpStatus, final String message) {
        return new ClientException(httpStatus, message);
    }

    public static ClientException resourceNotFound(final String message) {
        return new ClientException(NOT_FOUND, message);
    }

    public static ClientException badRequest(final String message) {
        return new ClientException(BAD_REQUEST, message);
    }

    public static ClientException dataConflict(final String message) {
        return new ClientException(CONFLICT, message);
    }
}
