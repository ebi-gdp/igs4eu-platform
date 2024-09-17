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

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;

@Table("globus_guest_collection_files_details")
public class GlobusDetails implements Persistable<String> {
    @Id
    private String filesetId;
    private String globusUsername;
    private String guestCollectionId;
    private String dirPathOnGuestCollection;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdOn;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedOn;

    @Transient
    private GlobusUserDetails globusUserDetails;

    @Transient
    private boolean isNew;

    protected GlobusDetails() {
    }

    private GlobusDetails(final String filesetId,
                          final String globusUsername,
                          final String guestCollectionId,
                          final String dirPathOnGuestCollection,
                          final boolean isNew) {
        this.filesetId = filesetId;
        this.globusUsername = globusUsername;
        this.guestCollectionId = guestCollectionId;
        this.dirPathOnGuestCollection = dirPathOnGuestCollection;
        this.isNew = isNew;
    }

    private GlobusDetails(final String filesetId,
                          final String globusUsername,
                          final String guestCollectionId,
                          final String dirPathOnGuestCollection,
                          final boolean isNew,
                          final String createdBy,
                          final LocalDateTime createdOn,
                          final String updatedBy,
                          final LocalDateTime updatedOn) {
        this(filesetId, globusUsername, guestCollectionId, dirPathOnGuestCollection, isNew);
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }

    /**
     * Creates new globus details.
     *
     * @param filesetId fileset id.
     * @param globusUsername globus username.
     * @param guestCollectionId guest collection id.
     * @param dirPathOnGuestCollection dir path on guest collection.
     *
     * @return new globus details {@link GlobusDetails}.
     */
    public static GlobusDetails create(final String filesetId,
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

    /**
     * Load globus details.
     *
     * @param filesetId fileset id.
     * @param globusUsername globus username.
     * @param guestCollectionId guest collection id.
     * @param dirPathOnGuestCollection dir path on guest collection.
     * @param createdBy created by user id.
     * @param createdOn created on timestamp
     * @param updatedBy updated by user id.
     * @param updatedOn updated on timestamp.
     *
     * @return {@link GlobusDetails}.
     */
    public static GlobusDetails load(final String filesetId,
                                     final String globusUsername,
                                     final String guestCollectionId,
                                     final Path dirPathOnGuestCollection,
                                     final String createdBy,
                                     final LocalDateTime createdOn,
                                     final String updatedBy,
                                     final LocalDateTime updatedOn) {
        return new GlobusDetails(
                filesetId,
                globusUsername,
                guestCollectionId,
                normalizeDirPath(dirPathOnGuestCollection),
                false,
                createdBy,
                createdOn,
                updatedBy,
                updatedOn
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

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
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
        return isNew || !hasText(filesetId);
    }
}
