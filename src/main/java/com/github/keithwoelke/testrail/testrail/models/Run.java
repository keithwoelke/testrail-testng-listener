package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Run {

    private Integer id;
    private Integer suiteId;
    private Integer projectId;
    private Long createdOn;
    private String url;

    @Builder
    @JsonCreator
    public Run(
            @JsonProperty("id") Integer id,
            @JsonProperty("suite_id") Integer suiteId,
            @JsonProperty("project_id") Integer projectId,
            @JsonProperty("created_on") Long createdOn, @JsonProperty("url") String url) {
        this.id = id;
        this.suiteId = suiteId;
        this.projectId = projectId;
        this.createdOn = createdOn;
        this.url = url;
    }
}
