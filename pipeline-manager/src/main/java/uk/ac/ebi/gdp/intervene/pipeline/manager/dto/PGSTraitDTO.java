package uk.ac.ebi.gdp.intervene.pipeline.manager.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

public record PGSTraitDTO(String id,
                          String label,
                          String description,
                          String url,
                          @JsonProperty(value = "associated_pgs_ids", access = WRITE_ONLY)
                       Collection<String> associatedPgsIds,
                          @JsonProperty(value = "child_associated_pgs_ids", access = WRITE_ONLY)
                       Collection<String> childAssociatedPgsIds) {
    @JsonGetter
    public long getPgsIdsSize() {
        return associatedPgsIds.size() + childAssociatedPgsIds.size();
    }
}
