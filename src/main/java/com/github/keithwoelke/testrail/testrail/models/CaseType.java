package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CaseType {
    API_FE_AUTOMATED(1),
    API_AUTOMATED(9),
    FE_AUTOMATED(8),
    LOAD(7),
    MANUAL(2),
    OTHER(6),
    PERFORMANCE(3),
    REGRESSION(4),
    USABILITY(5);

    private final int typeId;

    CaseType(int typeId) {
        this.typeId = typeId;
    }

    @JsonValue
    public int statusId() {
        return typeId;
    }
}
