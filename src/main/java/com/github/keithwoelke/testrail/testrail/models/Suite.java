package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Suite {

    private String description;
    private Integer id;
    private String name;
    @JsonProperty("project_id")
    private Integer projectId;
    private String url;

    @Builder
    @JsonCreator
    public Suite(
            @JsonProperty("description") String description,
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("project_id") Integer projectId,
            @JsonProperty("created_on") Long createdOn, @JsonProperty("url") String url) {
        this.description = description;
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.url = url;
    }
}
