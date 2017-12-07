package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Result {

    private Integer id;
    @JsonProperty("test_id")
    private Integer testId;

    @Builder
    @JsonCreator
    public Result(
            @JsonProperty("id") Integer id, @JsonProperty("test_id") Integer testId) {
        this.id = id;
        this.testId = testId;
    }
}
