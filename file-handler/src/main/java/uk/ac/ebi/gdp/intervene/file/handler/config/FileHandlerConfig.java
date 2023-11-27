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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;
import uk.ac.ebi.gdp.intervene.commons.exception.ReactiveExceptionHandler;
import uk.ac.ebi.gdp.intervene.file.handler.service.ega.EGAFileService;

/**
 * Bean config for file handler service.
 *
 * @see ReactiveExceptionHandler
 * @see WebClient
 * @see RetryTemplate
 * @see WebClientProperties
 * @see EGAFileService
 */
@Import(ReactiveExceptionHandler.class)
@Configuration
public class FileHandlerConfig {

    @Bean
    public EGAFileService egaFileService(@Qualifier("egaWebClient") final WebClient webClient,
                                         final RetryTemplate retryTemplate,
                                         final WebClientProperties webClientProperties,
                                         @Value("${ega.file.storage.path}") final String storagePath) {
        final int IV = 16 + 1;
        return new EGAFileService(
                webClient,
                retryTemplate,
                storagePath,
                webClientProperties.getPipeSize(),
                IV
        );
    }
}
