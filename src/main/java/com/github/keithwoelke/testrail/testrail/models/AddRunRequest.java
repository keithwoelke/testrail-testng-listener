package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AddRunRequest {

    @JsonProperty("suite_id")
    private Integer suiteId;
    private String name;
    private String description;
    @JsonProperty("include_all")
    private Boolean includeAll;
    @JsonProperty("case_ids")
    private List<Integer> caseIds;

    @Builder
    @JsonCreator
    public AddRunRequest(
            @JsonProperty("suite_id") Integer suiteId,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("include_all") Boolean includeAll, @JsonProperty("case_ids") List<Integer> caseIds) {
        this.suiteId = suiteId;
        this.name = name;
        this.description = description;
        this.includeAll = includeAll;
        this.caseIds = caseIds;
    }
}
