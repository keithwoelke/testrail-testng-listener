package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Case {

    private Integer id;
    @JsonProperty("suite_id")
    private Integer suiteId;
    @JsonProperty("section_id")
    private Integer sectionId;
    private String title;
    @JsonProperty("type_id")
    private Integer typeId;

    @Builder
    @JsonCreator
    public Case(
            @JsonProperty("id") Integer id,
            @JsonProperty("suite_id") Integer suiteId,
            @JsonProperty("section_id") Integer sectionId,
            @JsonProperty("title") String title, @JsonProperty("type_id") Integer typeId) {
        this.id = id;
        this.suiteId = suiteId;
        this.sectionId = sectionId;
        this.title = title;
        this.typeId = typeId;
    }
}
