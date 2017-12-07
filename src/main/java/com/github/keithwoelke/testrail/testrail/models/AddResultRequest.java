package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AddResultRequest {

    @JsonProperty("status_id")
    private ResultStatus statusId;
    private String comment;
    private String elapsed;
    private List<String> defects;
    private String version;

    @Builder
    @JsonCreator
    public AddResultRequest(
            @JsonProperty("status_id") ResultStatus statusId,
            @JsonProperty("comment") String comment,
            @JsonProperty("elapsed") String elapsed, @Singular @JsonProperty("defects") List<String> defects,
            @JsonProperty("version") String version) {
        this.statusId = statusId;
        this.comment = comment;
        this.elapsed = elapsed;
        this.defects = defects;
        this.version = version;
    }

    @JsonGetter("defects")
    public String getDefects() {
        return defects.stream().collect(Collectors.joining());
    }
}
