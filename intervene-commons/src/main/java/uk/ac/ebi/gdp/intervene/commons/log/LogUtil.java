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

import io.micrometer.context.ContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Hooks;
import reactor.util.context.Context;

import java.util.concurrent.ThreadLocalRandom;

public abstract class LogUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);
    public static final ThreadLocal<Long> CORRELATION_ID_THREAD_LOCAL = new ThreadLocal<>();
    public static final String CORRELATION_ID = "CORRELATION_ID";

    public static void registerContext() {
        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(CORRELATION_ID,
                        CORRELATION_ID_THREAD_LOCAL::get,
                        CORRELATION_ID_THREAD_LOCAL::set,
                        CORRELATION_ID_THREAD_LOCAL::remove);

        Hooks.enableAutomaticContextPropagation();
    }

    public static void debugLog(final String message) {
        final String threadName = Thread.currentThread().getName();
        final String threadNameTail = getThreadNameTail(threadName);
        LOGGER.info("[{}][{}] {}", threadNameTail, CORRELATION_ID_THREAD_LOCAL.get(), message);
    }

    public static long correlationId() {
        return Math.abs(ThreadLocalRandom.current().nextLong());
    }

    public static Context initializeContext() {
        return Context.of(CORRELATION_ID, correlationId());
    }

    private static String getThreadNameTail(final String threadName) {
        return threadName.substring(Math.max(0, threadName.length() - 10));
    }
}
