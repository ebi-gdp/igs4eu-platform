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
package uk.ac.ebi.gdp.intervene.file.handler.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.gdp.intervene.file.handler.dto.FileInfo;
import uk.ac.ebi.gdp.intervene.file.handler.exception.MD5ChecksumException;
import uk.ac.ebi.gdp.intervene.file.handler.model.FileDetails;
import uk.ac.ebi.gdp.intervene.file.handler.service.ega.EGAFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping("/files")
@RestController
public class FileController {

    private final String storagePath;
    private final EGAFileService egaFileService;

    public FileController(final EGAFileService egaFileService,
                          @Value("${ega.file.storage.path}") final String storagePath) {
        this.egaFileService = egaFileService;
        this.storagePath = storagePath;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<String> downloadFile(@PathVariable final String fileId) throws MD5ChecksumException, IOException, NoSuchAlgorithmException {
        egaFileService.downloadToStorage(fileId, (bytesTransferred -> {
        }));
        return ResponseEntity.ok("File has been downloaded!");
    }


    @GetMapping("/{fileId}/info")
    public ResponseEntity<FileDetails> getFileInfo(@PathVariable final String fileId) {
        return ResponseEntity.ok(egaFileService.getFileDetails(fileId));
    }

    @GetMapping
    public ResponseEntity<Set<FileInfo>> listFiles() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        final Set<FileInfo> fileInfoSet = Stream.of(Objects.requireNonNull(new File(storagePath).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(file -> {
                    FileInfo fileInfo = null;
                    try {
                        BasicFileAttributes attr =
                                Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                        fileInfo = new FileInfo(file.getName(), df.format(attr.creationTime().toMillis()), attr.size());
                    } catch (IOException e) {
                        //Ignore
                    }
                    return fileInfo;
                })
                .collect(Collectors.toSet());
        return ResponseEntity.ok(fileInfoSet);
    }
}
