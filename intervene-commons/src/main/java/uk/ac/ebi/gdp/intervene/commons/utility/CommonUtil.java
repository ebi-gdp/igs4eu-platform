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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.DEFAULT_VIEW_INCLUSION;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static java.time.format.DateTimeFormatter.ofPattern;

public abstract class CommonUtil {
    private static final ObjectMapper jsonObjectMapper = getObjectMapper();

    public static ObjectMapper getJsonObjectMapper() {
        return jsonObjectMapper;
    }

    private static ObjectMapper getObjectMapper() {
        return JsonMapper
                .builder()
                .addModule(new JavaTimeModule())
                .configure(ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(DEFAULT_VIEW_INCLUSION, false)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .disable(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .propertyNamingStrategy(SNAKE_CASE)
                .build();
    }

    public static Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(final String dateFormat) {
        return builder -> {
            // formatter
            final DateTimeFormatter dateTimeFormatter = ofPattern(dateFormat);

            // deserializers
            //builder.deserializers(new LocalDateTimeDeserializer(dateTimeFormatter));

            // serializers
            builder.serializers(new LocalDateTimeSerializer(dateTimeFormatter));
        };
    }
}
