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

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class CommonUtil {

    private static final Pattern EMAIL_NAME_EXTRACTION_PATTERN = Pattern.compile("([a-zA-Z]+)[^a-zA-Z@]*(@.*)?");
    private static final ObjectMapper jsonObjectMapper = new ObjectMapper();

    public static String getNameFromEmail(final String email) {
        return EMAIL_NAME_EXTRACTION_PATTERN
                .matcher(email).results()
                .map(result -> result.group(1))
                .collect(Collectors.joining(" "));
    }

    public static ObjectMapper getJsonObjectMapper() {
        return jsonObjectMapper;
    }
}
