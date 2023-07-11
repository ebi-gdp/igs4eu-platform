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
package uk.ac.ebi.gdp.intervene.file.handler.service.ega;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.gdp.file.handler.core.listener.ProgressListener;
import uk.ac.ebi.gdp.file.handler.core.stream.ProgressListenerOutputStream;
import uk.ac.ebi.gdp.file.handler.core.stream.RetryInputStream;
import uk.ac.ebi.gdp.intervene.file.handler.dto.EGAFileDetailsDTO;
import uk.ac.ebi.gdp.intervene.file.handler.exception.MD5ChecksumException;
import uk.ac.ebi.gdp.intervene.file.handler.model.FileDetails;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static uk.ac.ebi.gdp.file.handler.core.exception.ExceptionHandler.throwClientError;
import static uk.ac.ebi.gdp.file.handler.core.exception.ExceptionHandler.throwServerError;
import static uk.ac.ebi.gdp.file.handler.core.utils.Checksum.getMD5MessageDigest;
import static uk.ac.ebi.gdp.file.handler.core.utils.Checksum.normalize;

public class EGAFileService {

    private static final String UNDERSCORE = "_";

    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final String storagePath;
    private final int pipeSize;
    private final int IV;

    public EGAFileService(final WebClient webClient,
                          final RetryTemplate retryTemplate,
                          final String storagePath,
                          final int pipeSize,
                          final int initializationVector) {
        this.webClient = webClient;
        this.retryTemplate = retryTemplate;
        this.storagePath = storagePath;
        this.pipeSize = pipeSize;
        this.IV = initializationVector;
    }

    public FileDetails downloadToStorage(final String egaFileId,
                                         final ProgressListener progressListener) throws IOException, MD5ChecksumException, NoSuchAlgorithmException {
        final FileDetails fileDetails = getFileDetails(egaFileId);
        final File outputFile = outputFile(egaFileId, fileDetails.getFileName());
        final MessageDigest messageDigest = getMD5MessageDigest();

        try (final OutputStream os = new DigestOutputStream(
                new ProgressListenerOutputStream(
                        new FileOutputStream(outputFile), progressListener
                ), messageDigest);
             final InputStream in = doGetDownloadInputStream(egaFileId, fileDetails.getFileSize())) {
            IOUtils.copyLarge(in, os);
        }

        final String downloadedFileMD5 = normalize(messageDigest);

        assertMD5Checksum(fileDetails.getMd5(), downloadedFileMD5);
        return null;//Return downloaded file details
    }

    private InputStream doGetDownloadInputStream(final String egaFileId,
                                                 final long fileSize) throws IOException {
        final String downloadRequestURI = UriComponentsBuilder
                .fromPath("/elixir/data/files/{egaFileId}")
                .build(egaFileId)
                .getRawPath();

        final int startRange = 0;
        final long endRange = fileSize - IV;

        return new RetryInputStream(
                webClient,
                retryTemplate,
                downloadRequestURI,
                startRange,
                endRange,
                pipeSize
        );
    }

    public FileDetails getFileDetails(final String egaFileId) {
        final ResponseEntity<EGAFileDetailsDTO> responseEntity = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/elixir/data/metadata/files/{egaFileId}")
                        .build(egaFileId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, throwClientError())
                .onStatus(HttpStatusCode::is5xxServerError, throwServerError())
                .toEntity(EGAFileDetailsDTO.class)
                .block();

        final EGAFileDetailsDTO egaFileDetailsDTO = responseEntity.getBody();
        return new FileDetails(
                egaFileDetailsDTO.getDisplayFileName(),
                egaFileDetailsDTO.getFileSize(),
                egaFileDetailsDTO.getUnencryptedChecksum()
        );
    }

    private File outputFile(final String fileId, final String fileName) {
        return new File(storagePath + fileId + UNDERSCORE + UUID.randomUUID() + UNDERSCORE + fileName);
    }

    private void assertMD5Checksum(final String expectedMD5, final String actualMD5) throws MD5ChecksumException {
        if (!expectedMD5.equals(actualMD5)) {
            throw MD5ChecksumException.md5MismatchException(expectedMD5, actualMD5);
        }
    }
}
