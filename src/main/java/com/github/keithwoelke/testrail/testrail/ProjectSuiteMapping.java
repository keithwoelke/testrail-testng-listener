package com.github.keithwoelke.testrail.testrail;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class ProjectSuiteMapping {

    private final int projectId;
    private final int suiteId;

    @Builder
    @JsonCreator
    public ProjectSuiteMapping(@JsonProperty("project_id") int projectId, @JsonProperty("suite_ id") int suiteId) {
        this.projectId = projectId;
        this.suiteId = suiteId;
    }
}
