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
package uk.ac.ebi.gdp.intervene.commons.dto.validation;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.exception.ClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract base class for validating request bodies of a specific type {@code T}
 * using a provided {@code Validator} of type {@code U}. This class handles the validation logic
 * and provides a mechanism to handle errors or pass the validated object further.
 *
 * @param <T> the type of object to be validated
 * @param <U> the type of {@code Validator} used to perform the validation
 */
public abstract class AbstractValidator<T, U extends Validator> {
    private final Class<T> validationClass;
    private final U validator;

    /**
     * Constructs an instance of {@code AbstractValidator}.
     *
     * @param clazz the class type of the object that will be validated
     * @param validator the validator used to validate objects of type {@code T}
     */
    protected AbstractValidator(final Class<T> clazz,
                                final U validator) {
        this.validationClass = clazz;
        this.validator = validator;
    }

    /**
     * Handles the validation of the request body. It reads the body of the request,
     * validates it using the provided validator, and either returns the validated object
     * or processes validation errors.
     * If validation fails, this method calls {@link #onValidationErrors(Map)}.
     * If validation succeeds, the validated body of type {@code T} is returned in a {@code Mono}.
     *
     * @param request the {@code ServerRequest} containing the body to be validated
     *
     * @return a {@code Mono<T>} containing the validated object or an error if validation fails
     */
    public final Mono<T> handleRequest(final ServerRequest request) {
        return request.bodyToMono(this.validationClass)
                .flatMap(body -> {
                    final Errors errors = new BeanPropertyBindingResult(body, this.validationClass.getName());
                    this.validator.validate(body, errors);

                    if (!errors.getAllErrors().isEmpty()) {
                        final Map<String, String> errorsMap = new HashMap<>();
                        errors.getAllErrors().forEach(error -> {
                            final String fieldName = ((FieldError) error).getField();
                            final String errorMessage = error.getDefaultMessage();
                            errorsMap.put(fieldName, errorMessage);
                        });
                        return onValidationErrors(errorsMap);
                    }
                    return Mono.just(body);
                });
    }

    /**
     * Handles validation errors. This method is called when validation fails, and a map of
     * field errors is provided. Subclasses can override this method to customize error handling.
     * By default, this method throws a {@code ClientException} with a bad request message.
     *
     * @param errorsMap a map containing field names and their corresponding validation error messages
     *
     * @return a {@code Mono<T>} representing the error scenario
     * @throws ClientException always thrown to indicate a validation failure
     */
    protected Mono<T> onValidationErrors(final Map<String, String> errorsMap) {
        throw ClientException.badRequest("Invalid data received! %s".formatted(errorsMap));
    }
}
