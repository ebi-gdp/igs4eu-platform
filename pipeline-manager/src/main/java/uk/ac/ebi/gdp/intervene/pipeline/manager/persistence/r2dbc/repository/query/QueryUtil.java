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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.query;

import io.r2dbc.spi.Row;

import java.time.LocalDateTime;

import static java.lang.String.join;
import static java.time.LocalDateTime.parse;

public interface QueryUtil {
    static String joinQuery(final String... queries) {
        return join(" ", queries);
    }

    static String getString(final String fieldName,
                            final Row row) {
        return row.get(fieldName) != null ?
                row.get(fieldName, String.class) : "";
    }

    static boolean getBoolean(final String fieldName,
                              final Row row) {
        return row.get(fieldName) != null && Boolean.TRUE.equals(row.get(fieldName, Boolean.class));
    }

    static LocalDateTime getLocalDateTime(final String fieldName,
                                          final Row row) {
        return row.get(fieldName) != null ?
                row.get(fieldName, LocalDateTime.class) : parse("0000-01-01T00:00:00");
    }

    static Byte getByte(final String fieldName,
                        final Row row) {
        return row.get(fieldName) != null ? row.get(fieldName, Integer.class).byteValue() : (byte) 0;
    }
}
