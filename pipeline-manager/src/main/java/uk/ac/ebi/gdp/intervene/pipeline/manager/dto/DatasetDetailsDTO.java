package uk.ac.ebi.gdp.intervene.pipeline.manager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatasetDetailsDTO {
    private String datasetId;

    @Size(max = 512, message = "Dataset name can't be more than 512 characters!")
    @NotEmpty
    private String datasetName;

    @NotNull
    private GenomeBuild genomeBuild;

    private LocalDateTime expiresAt;

    @Size(max = 15, message = "Fileset Id can't be more than 15 characters!")
    @NotEmpty
    private String filesetId;

    @Size(max = 256, message = "Public Key can't be more than 256 characters!")
    private String publicKey;

    private boolean isDeleted;

    @Valid
    private GlobusDetailsDTO globusDetails;

    public DatasetDetailsDTO() {
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getGenomeBuild() {
        return genomeBuild.getGenomeBuildValue();
    }

    public void setGenomeBuild(GenomeBuild genomeBuild) {
        this.genomeBuild = genomeBuild;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @JsonProperty
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getFilesetId() {
        return filesetId;
    }

    public void setFilesetId(String filesetId) {
        this.filesetId = filesetId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public GlobusDetailsDTO getGlobusDetails() {
        return globusDetails;
    }

    public void setGlobusDetails(GlobusDetailsDTO globusDetails) {
        this.globusDetails = globusDetails;
    }
}
