package com.github.keithwoelke.testrail.testrail.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResultStatus {
    PASSED(1),
    BLOCKED(2),
    UNTESTED(3),
    RETEST(4),
    FAILED(5);

    private final int statusId;

    ResultStatus(int statusId) {
        this.statusId = statusId;
    }

    @JsonValue
    public int statusId() {
        return statusId;
    }
}
