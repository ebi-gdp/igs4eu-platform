/*
 *
 * Copyright 2025 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.UserAccountUtil;

import java.util.function.Function;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Pipeline submission rate limiter filter, executes before pipeline creation to validate
 * whether user has reached the pipeline submission limit.
 */
public class PipelineSubmissionRateLimiterFilter {
    private final IPipelineSubmissionRateLimiter pipelineSubmissionRateLimiter;

    public PipelineSubmissionRateLimiterFilter(final IPipelineSubmissionRateLimiter pipelineSubmissionRateLimiter) {
        this.pipelineSubmissionRateLimiter = pipelineSubmissionRateLimiter;
    }

    /**
     * @param request {@link ServerRequest}
     * @param next implementation of Function<ServerRequest, Mono<ServerResponse>>, allows to continue
     * request processing.
     *
     * @return Mono containing {@link ServerResponse}
     */
    public Mono<ServerResponse> isUserAllowedToSubmitPipeline(final ServerRequest request,
                                                              final Function<ServerRequest, Mono<ServerResponse>> next) {
        return UserAccountUtil.userAccount(request)
                .flatMap(userAccountDTO ->
                        pipelineSubmissionRateLimiter.isUserAllowedToSubmitPipeline(userAccountDTO.accountId())
                                .flatMap(isAllowed -> isAllowed
                                        ? next.apply(request)  // Allowed: Proceed with request
                                        : Mono.defer(() -> ServerResponse.status(TOO_MANY_REQUESTS)
                                        .contentType(APPLICATION_JSON)
                                        .bodyValue("{\"message\": \"Exhausted per day limit of pipeline submission!\"}"))));
    }
}
