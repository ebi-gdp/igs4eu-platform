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
package uk.ac.ebi.gdp.intervene.commons.dto.filehandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

import static java.util.Set.copyOf;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuestCollectionDirResDTO {
    private String guestCollectionId;
    private String dirPathOnGuestCollection;
    private Set<FileDetails> files;

    protected GuestCollectionDirResDTO() {
    }

    public GuestCollectionDirResDTO(final String guestCollectionId,
                                    final String dirPathOnGuestCollection) {
        this.guestCollectionId = guestCollectionId;
        this.dirPathOnGuestCollection = dirPathOnGuestCollection;
    }

    public GuestCollectionDirResDTO(final String guestCollectionId,
                                    final String dirPathOnGuestCollection,
                                    final Set<FileDetails> files) {
        this(guestCollectionId, dirPathOnGuestCollection);
        this.files = copyOf(files);
    }

    public GuestCollectionDirResDTO(final String dirPathOnGuestCollection,
                                    final Set<FileDetails> files) {
        this(null, dirPathOnGuestCollection, files);
    }

    public String getGuestCollectionId() {
        return guestCollectionId;
    }

    public void setGuestCollectionId(String guestCollectionId) {
        this.guestCollectionId = guestCollectionId;
    }

    public String getDirPathOnGuestCollection() {
        return dirPathOnGuestCollection;
    }

    public void setDirPathOnGuestCollection(String dirPathOnGuestCollection) {
        this.dirPathOnGuestCollection = dirPathOnGuestCollection;
    }

    public Set<FileDetails> getFiles() {
        return files;
    }

    public record FileDetails(String filename, long size) {
    }
}
