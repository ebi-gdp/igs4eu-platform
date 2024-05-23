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
package uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation;

import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.IGlobusFileDetailsWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO.GlobusFileDetails;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.BED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.BIM;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.BIM_ZST;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.FAM;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.INVALID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PGEN;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PSAM;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PVAR;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PVAR_ZST;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.VCF_PATH_GZ;

/**
 * Defines file validations to be performed on the files
 * to be used for Pipeline execution.
 *
 * @param <DTO> generic type, subclass of {@link IGlobusFileDetailsWrapper}
 */
public class FileValidations<DTO extends IGlobusFileDetailsWrapper> {
    private List<Predicate<DTO>> validations;

    public FileValidations() {
        this.validations = new ArrayList<>(2);
        addValidations();
    }

    private void addValidations() {
        validations.add(globusFileDetailsWrapperDTO -> validateFileCount(globusFileDetailsWrapperDTO.getTotalNoOfFiles()));
        validations.add(globusFileDetailsWrapperDTO -> validateFileExtensionAndGroupCount(globusFileDetailsWrapperDTO.getFileDetailsList()));
        this.validations = unmodifiableList(this.validations);
    }

    /**
     * Returns list of validations.
     *
     * @return predicate implementation of {@link Predicate}
     */
    public List<Predicate<DTO>> validations() {
        return validations;
    }

    private boolean validateFileCount(final int fileCount) {
        if (fileCount == 0) {
            throw resourceNotFound("Empty! No file(s) have been uploaded!");
        } else {
            return true;
        }
    }

    private boolean validateFileExtensionAndGroupCount(final List<GlobusFileDetails> globusFileDetailsList) {
        final Map<FileExtension, Integer> extensionMap = buildFileExtensionMap(globusFileDetailsList);
        final boolean groupOneEmptyFiles = validateGroupOneEmptyFiles(extensionMap);
        final boolean groupTwoEmptyFiles = validateGroupTwoEmptyFiles(extensionMap);
        final boolean groupThreeEmptyFiles = validateGroupThreeEmptyFiles(extensionMap);

        if (((validateGroupOne(extensionMap) && groupTwoEmptyFiles && groupThreeEmptyFiles)
                || (groupOneEmptyFiles && validateGroupTwo(extensionMap) && groupThreeEmptyFiles)
                || (groupOneEmptyFiles && groupTwoEmptyFiles && validateGroupThree(extensionMap)))
                && extensionMap.get(INVALID) == 0) {
            return true;
        } else {
            throw badRequest("Correct file(s) haven't been uploaded!");
        }
    }

    private Map<FileExtension, Integer> buildFileExtensionMap(final List<GlobusFileDetails> globusFileDetailsList) {
        final Map<FileExtension, Integer> fileExtensionMap = new HashMap<>(10);
        fileExtensionMap.put(BIM_ZST, 0);
        fileExtensionMap.put(BIM, 0);
        fileExtensionMap.put(BED, 0);
        fileExtensionMap.put(FAM, 0);
        fileExtensionMap.put(PVAR_ZST, 0);
        fileExtensionMap.put(PVAR, 0);
        fileExtensionMap.put(PSAM, 0);
        fileExtensionMap.put(PGEN, 0);
        fileExtensionMap.put(VCF_PATH_GZ, 0);
        fileExtensionMap.put(INVALID, 0);

        globusFileDetailsList
                .parallelStream()
                .forEach(globusFileDetails -> FileExtension
                        .getFileExtension(globusFileDetails.getFileName())
                        .ifPresentOrElse(fileExtension -> updateFileCount(fileExtensionMap, fileExtension),
                                () -> updateFileCount(fileExtensionMap, INVALID)));
        return fileExtensionMap;
    }

    private void updateFileCount(final Map<FileExtension, Integer> fileExtensionMap,
                                 final FileExtension fileExtension) {
        fileExtensionMap.compute(fileExtension, (key, count) -> count + 1);
    }

    private boolean validateGroupOne(final Map<FileExtension, Integer> extensionMap) {
        return validate(extensionMap, BIM_ZST, BIM)
                && validateSingleCount(extensionMap, BED)
                && validateSingleCount(extensionMap, FAM);
    }

    private boolean validateGroupTwo(final Map<FileExtension, Integer> extensionMap) {
        return validate(extensionMap, PVAR_ZST, PVAR)
                && validateSingleCount(extensionMap, PSAM)
                && validateSingleCount(extensionMap, PGEN);
    }

    private boolean validateGroupThree(final Map<FileExtension, Integer> extensionMap) {
        return validateSingleCount(extensionMap, VCF_PATH_GZ);
    }

    private boolean validate(final Map<FileExtension, Integer> extensionMap,
                             final FileExtension fileExtensionOne,
                             final FileExtension fileExtensionTwo) {
        return (validateSingleCount(extensionMap, fileExtensionOne) && validateZeroCount(extensionMap, fileExtensionTwo))
                || (validateZeroCount(extensionMap, fileExtensionOne) && validateSingleCount(extensionMap, fileExtensionTwo));
    }

    private boolean validateGroupOneEmptyFiles(final Map<FileExtension, Integer> extensionMap) {
        return validateZeroCount(extensionMap, BIM_ZST)
                && validateZeroCount(extensionMap, BIM)
                && validateZeroCount(extensionMap, BED)
                && validateZeroCount(extensionMap, FAM);
    }

    private boolean validateGroupTwoEmptyFiles(final Map<FileExtension, Integer> extensionMap) {
        return validateZeroCount(extensionMap, PVAR_ZST)
                && validateZeroCount(extensionMap, PVAR)
                && validateZeroCount(extensionMap, PSAM)
                && validateZeroCount(extensionMap, PGEN);
    }

    private boolean validateZeroCount(final Map<FileExtension, Integer> extensionMap,
                                      final FileExtension fileExtension) {
        return extensionMap.get(fileExtension) == 0;
    }

    private boolean validateSingleCount(final Map<FileExtension, Integer> extensionMap,
                                        final FileExtension fileExtension) {
        return extensionMap.get(fileExtension) == 1;
    }

    private boolean validateGroupThreeEmptyFiles(final Map<FileExtension, Integer> extensionMap) {
        return validateZeroCount(extensionMap, VCF_PATH_GZ);
    }

    /**
     * Enum constants of File extensions to support.
     */
    public enum FileExtension {
        BIM("bim"),
        BIM_ZST("bim.zst"),
        BED("bed"),
        FAM("fam"),
        PVAR("pvar"),
        PVAR_ZST("pvar.zst"),
        PGEN("pgen"),
        PSAM("psam"),
        VCF_PATH_GZ("vcf.gz"),
        INVALID("invalid.ext");

        private final String fileExtension;

        FileExtension(final String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public static Optional<FileExtension> getFileExtension(final String fileExtensionToBeChecked) {
            for (final FileExtension fileExtensionType : FileExtension.values()) {
                if (fileExtensionToBeChecked.toLowerCase().endsWith(fileExtensionType.getFileExtension())) {
                    return of(fileExtensionType);
                }
            }
            return empty();
        }
    }
}
