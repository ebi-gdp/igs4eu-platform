package uk.ac.ebi.gdp.intervene.file.handler.dto.globus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DeleteDirRequestDTO(@JsonProperty("DATA_TYPE") String dataType,
                                  @JsonProperty("submission_id") String submissionId,
                                  boolean recursive,
                                  @JsonProperty("endpoint") String endpointId,
                                  @JsonProperty("DATA") List<Data> data) {

    public record Data(String path) {
    }
}
