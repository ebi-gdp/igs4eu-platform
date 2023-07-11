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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.status;
import static reactor.core.publisher.Mono.just;

//@RestControllerAdvice
public class ExceptionControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @ExceptionHandler(ClientException.class)
    protected ResponseEntity<Mono<ErrorMessage>> serviceExceptionHandler(final ClientException ce) {
        LOGGER.error(ce.getMessage(), ce);
        return status(ce.getHttpStatus())
                .contentType(APPLICATION_JSON)
                .body(just(new ErrorMessage(ce.getMessage())));
    }

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<Mono<String>> serverExceptionHandler(final Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
        return status(INTERNAL_SERVER_ERROR)
                .contentType(APPLICATION_JSON)
                .body(just("{\"message\": \"Unable to process request at this moment\"}"));
    }

    public record ErrorMessage(String errorMessage) {
    }
}
