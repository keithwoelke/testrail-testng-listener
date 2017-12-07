package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AddCaseRequest {

    private String title;
    @JsonProperty("type_id")
    private CaseType typeId;

    @Builder
    @JsonCreator
    public AddCaseRequest(
            @JsonProperty("title") String title,
            @JsonProperty("type_id") CaseType typeId) {
        this.title = title;
        this.typeId = typeId;
    }
}
