/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;

@Table("globus_guest_collection_files_details")
public class GlobusDetails implements Persistable<String> {
    @Id
    @Column("fileset_id")
    private String filesetId;

    @Column("globus_username")
    private String globusUsername;

    @Column("guest_collection_id")
    private String guestCollectionId;

    @Column("dir_path_on_guest_collection")
    private String dirPathOnGuestCollection;

    @Transient
    private boolean newGlobusDetails;

    @Column
    private LocalDateTime createdOn;

    @Column
    private LocalDateTime updatedOn;

    @Transient
    private GlobusUserDetails globusUserDetails;

    protected GlobusDetails() {
    }

    private GlobusDetails(final String filesetId,
                          final String globusUsername,
                          final String guestCollectionId,
                          final String dirPathOnGuestCollection,
                          final boolean isNewGlobusDetails) {
        this.filesetId = filesetId;
        this.globusUsername = globusUsername;
        this.guestCollectionId = guestCollectionId;
        this.dirPathOnGuestCollection = dirPathOnGuestCollection;
        this.newGlobusDetails = isNewGlobusDetails;
    }

    public static GlobusDetails newRecord(final String filesetId,
                                          final String globusUsername,
                                          final String guestCollectionId,
                                          final Path dirPathOnGuestCollection) {
        return new GlobusDetails(
                filesetId,
                globusUsername,
                guestCollectionId,
                normalizeDirPath(dirPathOnGuestCollection),
                true
        );
    }

    public static GlobusDetails existingRecord(final String filesetId,
                                               final String globusUsername,
                                               final String guestCollectionId,
                                               final Path dirPathOnGuestCollection) {
        return new GlobusDetails(
                filesetId,
                globusUsername,
                guestCollectionId,
                normalizeDirPath(dirPathOnGuestCollection),
                false
        );
    }

    private static String normalizeDirPath(final Path dirPathOnGuestCollection) {
        return dirPathOnGuestCollection.startsWith("/") ? dirPathOnGuestCollection.toString() : "/" + dirPathOnGuestCollection;
    }

    public String getFilesetId() {
        return filesetId;
    }

    public String getGlobusUsername() {
        return globusUsername;
    }

    public String getGuestCollectionId() {
        return guestCollectionId;
    }

    public String getDirPathOnGuestCollection() {
        return dirPathOnGuestCollection;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public GlobusUserDetails getGlobusUserDetails() {
        return globusUserDetails;
    }

    public void setGlobusUserDetails(GlobusUserDetails globusUserDetails) {
        this.globusUserDetails = globusUserDetails;
    }

    public void updateGlobusUsername(String globusUsername) {
        this.globusUsername = globusUsername;
    }

    @Override
    public String getId() {
        return filesetId;
    }

    @Override
    public boolean isNew() {
        return newGlobusDetails || !hasText(filesetId);
    }
}
