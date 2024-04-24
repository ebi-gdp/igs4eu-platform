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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;

import java.net.URI;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class HttpMessageService implements MessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessageService.class);

    private final WebClient webClient;
    private final URI pipelineRequestURI;

    public HttpMessageService(final WebClient webClient,
                              final URI pipelineRequestURI) {
        this.webClient = webClient;
        this.pipelineRequestURI = pipelineRequestURI;
    }

    @Override
    public Mono<Void> sendMessage(final String key,
                                  final TriggerPipelineEvent message) {
        return webClient
                .post()
                .uri(pipelineRequestURI.getPath())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .toBodilessEntity()
                .doOnNext(voidResponseEntity -> {
                    if (voidResponseEntity.getStatusCode() == HttpStatusCode.valueOf(CREATED.value())) {
                        LOGGER.info("Pipeline request has been successfully submitted!");
                    } else {
                        LOGGER.error("Pipeline request has failed!");
                    }
                })
                .then();
    }
}
