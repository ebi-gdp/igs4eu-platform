package uk.ac.ebi.gdp.intervene.pipeline.manager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatasetDetailsDTO {
    private String datasetId;
    private String datasetName;
    private GenomeBuild genomeBuild;
    private String filesetId;
    private String publicKey;
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

    public GlobusDetailsDTO getGlobusDetails() {
        return globusDetails;
    }

    public void setGlobusDetails(GlobusDetailsDTO globusDetails) {
        this.globusDetails = globusDetails;
    }
}
