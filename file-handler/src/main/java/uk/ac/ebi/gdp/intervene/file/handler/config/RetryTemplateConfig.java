/*
 *
 * Copyright 2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.file.handler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import uk.ac.ebi.gdp.file.handler.core.exception.ServerException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines {@link RetryTemplate} config, specifies two strategies Fixed & Exponential.
 *
 * @see RetryTemplate
 * @see SimpleRetryPolicy
 * @see FixedBackOffPolicy
 * @see ExponentialBackOffPolicy
 *
 */
@Configuration
public class RetryTemplateConfig {

    @ConditionalOnProperty(name = "file.download.retry.strategy", havingValue = "FIXED")
    @Bean("fixedDelayRetryTemplate")
    public RetryTemplate fixedDelayRetryTemplate(@Value("${file.download.retry.attempts.back-off-period}") final long backOffPeriod,
                                                 @Value("${file.download.retry.attempts.max}") final int maxAttempts) {
        return initFixedDelayRetryTemplate(backOffPeriod, maxAttempts);
    }

    @ConditionalOnProperty(name = "file.download.retry.strategy", havingValue = "EXPONENTIAL")
    @Bean("exponentialDelayRetryTemplate")
    public RetryTemplate exponentialDelayRetryTemplate(@Value("${file.download.retry.attempts.delay}") final int delay,
                                                       @Value("${file.download.retry.attempts.maxDelay}") final int maxDelay,
                                                       @Value("${file.download.retry.attempts.multiplier}") final double multiplier,
                                                       @Value("${file.download.retry.attempts.max}") final int maxAttempts) {
        return initExponentialDelayRetryTemplate(
                delay, maxDelay, multiplier, maxAttempts
        );
    }

    private RetryTemplate initFixedDelayRetryTemplate(final long backOffPeriod,
                                                      final int maxAttempts) {
        final FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(backOffPeriod);
        return retryTemplate(maxAttempts, backOffPolicy);
    }

    private RetryTemplate initExponentialDelayRetryTemplate(final int delay,
                                                            final int maxDelay,
                                                            final double multiplier,
                                                            final int maxAttempts) {
        final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(delay);
        backOffPolicy.setMaxInterval(maxDelay);
        backOffPolicy.setMultiplier(multiplier);
        return retryTemplate(maxAttempts, backOffPolicy);
    }

    private RetryTemplate retryTemplate(final int maxAttempts,
                                        final BackOffPolicy backOffPolicy) {
        final RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy(maxAttempts));
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    private SimpleRetryPolicy simpleRetryPolicy(final int maxAttempts) {
        final Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>(1);
        retryableExceptions.put(IOException.class, true);
        retryableExceptions.put(ServerException.class, true);
        return new SimpleRetryPolicy(maxAttempts, retryableExceptions);
    }
}
