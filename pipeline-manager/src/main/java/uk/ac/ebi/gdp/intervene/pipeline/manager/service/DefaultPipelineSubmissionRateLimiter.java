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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;

/**
 * Default implementation for pipeline submission rate limiter.
 */
public class DefaultPipelineSubmissionRateLimiter implements IPipelineSubmissionRateLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPipelineSubmissionRateLimiter.class);
    final IPipelinePersistence pipelinePersistence;
    final int pipelineSubmissionRateLimit;

    public DefaultPipelineSubmissionRateLimiter(final IPipelinePersistence pipelinePersistence,
                                                final int pipelineSubmissionRateLimit) {
        this.pipelinePersistence = pipelinePersistence;
        this.pipelineSubmissionRateLimit = pipelineSubmissionRateLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Boolean> isUserAllowedToSubmitPipeline(final String userId) {
        return pipelinePersistence
                .getPipelinesForToday(userId)
                .count()
                .doOnNext(pipelineCount -> LOGGER.info("No of Pipeline(s): {}, already submitted today for the user: {}", pipelineCount, userId))
                .filter(numberOfPipelines -> numberOfPipelines < pipelineSubmissionRateLimit)
                .hasElement();
    }
}
