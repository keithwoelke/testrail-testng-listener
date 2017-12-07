package com.github.keithwoelke.testrail.testrail;

import java.lang.annotation.*;

@Repeatable(TestRailCases.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TestRailCase {

    int DEFAULT_PROJECT_ID = -1;
    int DEFAULT_SUITE_ID = -1;

    int projectId() default -1;

    int suiteId() default -1;

    int[] caseId() default {};
}
