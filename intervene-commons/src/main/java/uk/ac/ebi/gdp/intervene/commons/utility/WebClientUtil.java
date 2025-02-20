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
package uk.ac.ebi.gdp.intervene.commons.utility;

import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.clientException;
import static uk.ac.ebi.gdp.intervene.commons.exception.ServerException.serverException;
import static uk.ac.ebi.gdp.intervene.commons.utility.CommonUtil.getJsonObjectMapper;

/**
 * Web client utility abstract class. Define utility methods to support web client functions.
 * This is an abstract class in order to support private methods & variables if needed.
 */
public abstract class WebClientUtil {
    /**
     * Configures jackson encoder & decoder.
     *
     * @return {@link ExchangeStrategies}
     */
    public static ExchangeStrategies jsonExchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer
                            .defaultCodecs()
                            .jackson2JsonDecoder(
                                    new Jackson2JsonDecoder(getJsonObjectMapper()));
                    configurer
                            .defaultCodecs()
                            .jackson2JsonEncoder(
                                    new Jackson2JsonEncoder(getJsonObjectMapper()));
                })
                .build();
    }

    /**
     * Default web client utility method.
     *
     * @param baseURL base URL of a service to interact with.
     *
     * @return {@link WebClient} default instance to support necessary functionality.
     */
    public static WebClient webClient(final WebClient.Builder builder,
                                      final String baseURL) {
        return builder
                .baseUrl(baseURL)
                .filter(errorHandler())
                .filter(new ServerBearerExchangeFilterFunction())
                .build();
    }

    /**
     * Error handler.
     *
     * @return {@link ClientResponse}
     */
    public static ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> error(clientException(clientResponse.statusCode().value(), errorBody)));
            } else if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> error(serverException(clientResponse.statusCode().value(), errorBody)));
            } else {
                return just(clientResponse);
            }
        });
    }
}
