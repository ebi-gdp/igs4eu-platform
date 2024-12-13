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
package uk.ac.ebi.gdp.intervene.file.handler.cloud;

import org.springframework.core.io.InputStreamSource;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

/**
 * Cloud storage interface to provide feature to interact with cloud storage
 * e.g. S3, Google Cloud Storage.
 */
public interface ICloudStorage {
    /**
     * List files on cloud storage.
     *
     * @param bucketName name of the bucket
     * @param pathPrefix file path prefix
     *
     * @return {@link Stream} of {@link String}, contains filenames
     */
    Mono<Stream<String>> listFiles(String bucketName, String pathPrefix);

    /**
     * @param bucketName bucket name on cloud storage
     * @param path bucket path
     *
     * @return {@link InputStreamSource}, contains file contents
     */
    Mono<InputStreamSource> streamFileFromBucket(String bucketName, String path);

    static boolean isValidFileForDownload(final String fileName) {
        return fileName.endsWith("log.csv.gz") || fileName.endsWith("scores.txt.gz") || fileName.endsWith("report.html");
    }
}
