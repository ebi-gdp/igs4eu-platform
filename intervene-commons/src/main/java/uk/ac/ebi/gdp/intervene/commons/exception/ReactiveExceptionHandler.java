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
package uk.ac.ebi.gdp.intervene.commons.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import static com.nimbusds.jose.util.JSONStringUtils.toJSONString;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@ControllerAdvice
public class ReactiveExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveExceptionHandler.class);

    @Order(-2)
    @Bean
    public WebExceptionHandler exceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            LOGGER.error(ex.getMessage(), ex);
            exchange.getResponse().getHeaders().setContentType(APPLICATION_JSON);
            if (ex instanceof GenericException ce) {
                exchange
                        .getResponse()
                        .setStatusCode(ce.getHttpStatus());
                return buildResponse(exchange, ce.getMessage());
            } else {
                exchange
                        .getResponse()
                        .setStatusCode(INTERNAL_SERVER_ERROR);
                return buildResponse(exchange, "Unable to process request at this moment!");
            }
        };
    }

    private Mono<Void> buildResponse(final ServerWebExchange exchange, final String message) {
        final DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(buildErrorMsg(message).getBytes());
        return exchange
                .getResponse()
                .writeWith(just(buffer));
    }

    private String buildErrorMsg(final String errorMsg) {
        return toJSONString("{\"errorMessage\": \"%s\"}".formatted(errorMsg));
    }
}
