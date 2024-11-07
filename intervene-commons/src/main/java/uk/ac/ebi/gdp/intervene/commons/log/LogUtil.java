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
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.util.context.Context;

import static java.util.UUID.randomUUID;
import static reactor.core.publisher.Mono.deferContextual;

/**
 * Log util to provide necessary filters to create, log & propagate co-relation request/tracking id.
 */
public abstract class LogUtil {
    public static final String REQUEST_ID_HEADER = "Request-Id";

    /**
     * Creates new Unique Request Id.
     *
     * @param logger {@link Logger} instance.
     */
    public static HandlerFilterFunction<ServerResponse, ServerResponse> buildUniqueRequestId(final Logger logger) {
        return (request, next) -> next
                .handle(request)
                .contextWrite(contextView -> buildContextView(contextView, logger));
    }

    /**
     * Propagates Request Id.
     *
     * @param logger {@link Logger} instance.
     */
    public static ExchangeFilterFunction propagateRequestId(final Logger logger) {
        return (request, next) -> deferContextual(contextView -> {
            final String uniqueRequestId = contextView.getOrDefault(REQUEST_ID_HEADER, generateNewUniqueRequestId(logger));
            logger.debug("{} retrieved: {}", REQUEST_ID_HEADER, uniqueRequestId);
            // Continue with the request
            return next.exchange(ClientRequest.from(request)
                    .headers(headers -> headers.add(REQUEST_ID_HEADER, uniqueRequestId))
                    .build());
        });
    }

    /**
     * Logs Request Id.
     *
     * @param logger {@link Logger} instance.
     */
    public static HandlerFilterFunction<ServerResponse, ServerResponse> logRequestIdHeader(final Logger logger) {
        return (request, next) -> {
            final HttpHeaders headers = request
                    .headers()
                    .asHttpHeaders();
            if (headers.containsKey(REQUEST_ID_HEADER)) {
                final String requestId = headers.get(REQUEST_ID_HEADER).get(0);
                logger.info("{} header: {}", REQUEST_ID_HEADER, requestId);
                return next
                        .handle(request)
                        .contextWrite(contextView -> writeRequestId(contextView, logger, requestId));
            } else {
                logger.warn("{} header not found! Generating new", REQUEST_ID_HEADER);
                return next
                        .handle(request)
                        .contextWrite(contextView -> buildContextView(contextView, logger));
            }
        };
    }

    private static Context buildContextView(final Context contextView,
                                            final Logger logger) {
        final String uniqueRequestId = generateNewUniqueRequestId(logger);
        return contextView.put(REQUEST_ID_HEADER, uniqueRequestId);
    }

    private static Context writeRequestId(final Context contextView,
                                          final Logger logger,
                                          final String requestId) {
        logger.info("{} retrieved & added to context: {}", REQUEST_ID_HEADER, requestId);
        return contextView.put(REQUEST_ID_HEADER, requestId);
    }

    private static String generateNewUniqueRequestId(final Logger logger) {
        final String uniqueRequestId = randomUUID().toString();
        logger.info("{} generated for this request: {}", REQUEST_ID_HEADER, uniqueRequestId);
        return uniqueRequestId;
    }
}
