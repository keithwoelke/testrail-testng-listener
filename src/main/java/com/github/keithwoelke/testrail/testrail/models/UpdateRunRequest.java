package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpdateRunRequest {

    private String name;
    private String description;
    private Boolean includeAll;
    private List<Integer> caseIds;

    @Builder
    @JsonCreator
    public UpdateRunRequest(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("include_all") Boolean includeAll,
            @Singular @JsonProperty("case_ids") List<Integer> caseIds) {
        this.name = name;
        this.description = description;
        this.includeAll = includeAll;
        this.caseIds = caseIds;
    }
}
