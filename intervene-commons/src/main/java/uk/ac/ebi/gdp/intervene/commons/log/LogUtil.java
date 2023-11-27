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
package uk.ac.ebi.gdp.intervene.commons.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LogUtil {
    Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);
    String REQUEST_ID_HEADER = "Request-Id";

    static HandlerFilterFunction<ServerResponse, ServerResponse> buildUniqueRequestId(final Logger logger) {
        return (request, next) -> next
                .handle(request)
                .contextWrite(contextView -> {
                    final String uniqueRequestId = UUID.randomUUID().toString();
                    logger.info("Request-Id generated for this request: {}", uniqueRequestId);
                    return contextView.put(REQUEST_ID_HEADER, uniqueRequestId);
                });
    }

    static ExchangeFilterFunction propagateRequestId(final Logger logger) {
        return (request, next) -> Mono.deferContextual(contextView -> {
            final String valueFromContext = contextView.get("Request-Id");
            logger.debug("Request-Id retrieved: {}", valueFromContext);
            // Continue with the request
            return next.exchange(ClientRequest.from(request)
                    .headers(headers -> headers.add(REQUEST_ID_HEADER, valueFromContext))
                    .build());
        });
    }

    static HandlerFilterFunction<ServerResponse, ServerResponse> logRequestIdHeader(final Logger logger) {
        return (request, next) -> {
            final HttpHeaders headers = request.headers().asHttpHeaders();
            if (headers.containsKey(REQUEST_ID_HEADER)) {
                logger.info("Request-Id header: {}", headers.get(REQUEST_ID_HEADER).get(0));
            } else {
                logger.warn("Request-Id header not found!");
            }
            return next.handle(request);
        };
    }
}
