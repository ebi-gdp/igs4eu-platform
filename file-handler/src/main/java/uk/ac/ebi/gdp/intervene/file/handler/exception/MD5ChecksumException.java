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
package uk.ac.ebi.gdp.intervene.file.handler.exception;

import java.io.Serial;

import static java.lang.String.format;

/**
 * Exception represents error related to MD5 checksum validation.
 */
public class MD5ChecksumException extends Exception {
    @Serial
    private static final long serialVersionUID = -6208032664686322248L;

    private MD5ChecksumException(final String message) {
        super(message);
    }

    public static MD5ChecksumException md5MismatchException(final String expectedMd5,
                                                            final String actualMd5) {
        return new MD5ChecksumException(format("Downloaded file MD5 mismatch: Expected MD5: %s, Actual MD5: %s", expectedMd5, actualMd5));
    }
}
