package com.github.keithwoelke.testrail.testrail;

import com.github.keithwoelke.testrail.testrail.models.AddCaseRequest;
import com.github.keithwoelke.testrail.testrail.models.AddResultRequest;
import com.github.keithwoelke.testrail.testrail.models.AddRunRequest;
import com.github.keithwoelke.testrail.testrail.models.Case;
import com.github.keithwoelke.testrail.testrail.models.CaseType;
import com.github.keithwoelke.testrail.testrail.models.Result;
import com.github.keithwoelke.testrail.testrail.models.ResultStatus;
import com.github.keithwoelke.testrail.testrail.models.Run;
import com.github.keithwoelke.testrail.testrail.models.Suite;
import com.google.common.collect.Maps;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.http.HttpStatus;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.rmi.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

@Slf4j
public class TestNGTestRailListener implements ITestListener, ISuiteListener {

    public static final String REASON_KEY = "reason";

    private static final String TEST_RAIL_BASE_URL = "{TEST_RAIL_BASE_URL}";

    private static final String TEST_RAIL_API_PREFIX_URL = TEST_RAIL_BASE_URL + "/api/v2";
    private static final String TEST_RAIL_ADD_RUN_URL = TEST_RAIL_API_PREFIX_URL + "/add_run/%s";
    private static final String TEST_RAIL_READ_SUITE_URL = TEST_RAIL_API_PREFIX_URL + "/get_suite/%s";
    private static final String TEST_RAIL_UPDATE_CASE_URL = TEST_RAIL_API_PREFIX_URL + "/update_case/%s";
    private static final String TEST_RAIL_READ_CASE_URL = TEST_RAIL_API_PREFIX_URL + "/get_case/%s";
    private static final String TEST_RAIL_ADD_RESULT_FOR_CASE_URL = TEST_RAIL_API_PREFIX_URL +
            "/add_result_for_case/%s/%s";
    private static final String TEST_RAIL_USER = "{TEST_RAIL_USER}";
    private static final String TEST_RAIL_PASS = "{TEST_RAIL_PASS}";

    private Map<ProjectSuiteMapping, Run> testRuns = Maps.newConcurrentMap();
    private boolean enabled = false;

    @Override
    public void onStart(ISuite suite) {
        enabled = Boolean.getBoolean(System.getProperty("platform.reporting.testrail"));

        if (!enabled) {
            return;
        }

        try {
            Map<ProjectSuiteMapping, AddRunRequest> testRunsToCreate = Maps.newConcurrentMap();

            validateSuite(suite);

            for (ITestNGMethod testNGMethod : suite.getAllMethods()) {
                TestRailCase[] testRailCases = getMethodAnnotations(testNGMethod);

                for (TestRailCase testRailCase : testRailCases) {
                    if (!isValidTestRailCase(testNGMethod, testRailCase)) {
                        continue;
                    }

                    addTestCaseIds(testRunsToCreate, testNGMethod, testRailCase);
                }
            }

            createRuns(testRunsToCreate);
        } catch (Exception e) {
            if (e instanceof UnknownHostException) {
                log.warn("An error occurred connecting to TestRail");
                enabled = false;
            }
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        if (!enabled) {
            return;
        }

        try {
            if (testRuns == null) {
                return;
            }

            for (Run run : testRuns.values()) {
                log.info(String.format("Test results reported to %s", run.getUrl()));
            }
        } catch (Exception e) {
            log.warn("An error occurred connecting to TestRail");
            enabled = false;
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (!enabled) {
            return;
        }

        try {
            ITestNGMethod method = result.getMethod();
            TestRailCase[] testRailCases = getMethodAnnotations(method);

            for (TestRailCase testRailCase : testRailCases) {
                if (!isValidTestRailCase(method, testRailCase)) {
                    continue;
                }

                List<Integer> caseIds = getCaseIds(method, testRailCase);

                for (int caseId : caseIds) {
                    Response response = getCase(caseId);

                    if (response.getStatusCode() == HttpStatus.SC_OK) {
                        Case testCase = response.as(Case.class);

                        log.info(String.format("Running test case %s", TEST_RAIL_BASE_URL + "/cases/view/" + testCase
                                .getId()));

                        updateTestCaseType(caseId, testCase);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("An error occurred connecting to TestRail");
            enabled = false;
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (!enabled) {
            return;
        }

        try {
            addResult(result);
        } catch (Exception e) {
            log.warn("An error occurred connecting to TestRail");
            enabled = false;
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!enabled) {
            return;
        }

        try {
            addResult(result);
        } catch (Exception e) {
            log.warn("An error occurred connecting to TestRail");
            enabled = false;
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (!enabled) {
            return;
        }

        try {
            addResult(result);
        } catch (Exception e) {
            log.warn("An error occurred connecting to TestRail");
            enabled = false;
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        if (!enabled) {
            return;
        }

        try {
            addResult(result, ResultStatus.PASSED);
        } catch (Exception e) {
            log.warn("An error occurred connecting to TestRail");
            enabled = false;
        }
    }

    @Override
    public void onStart(ITestContext context) {

    }

    @Override
    public void onFinish(ITestContext context) {

    }

    private void addResult(ITestResult testResult, ResultStatus resultStatus) {
        ITestNGMethod method = testResult.getMethod();
        TestRailCase[] testRailCases = getMethodAnnotations(method);
        AddResultRequest.AddResultRequestBuilder addResultRequest = getNewAddResultRequest(testResult, resultStatus);

        for (TestRailCase testRailCase : testRailCases) {
            int projectId = getProjectId(method, testRailCase);
            int suiteId = getSuiteId(method, testRailCase);
            List<Integer> caseIds = getCaseIds(method, testRailCase);

            if (!isValidTestRailCase(method, testRailCase)) {
                continue;
            }

            ProjectSuiteMapping projectSuiteMapping = new ProjectSuiteMapping(projectId, suiteId);
            if (testRuns.containsKey(projectSuiteMapping)) {
                int runId = testRuns.get(projectSuiteMapping).getId();

                for (int caseId : caseIds) {
                    Response response = addResultForCase(runId, caseId, addResultRequest.build());

                    if (response.getStatusCode() == HttpStatus.SC_OK) {
                        Result result = response.as(Result.class);

                        log.info("Adding result for project {}, suite {}, and case {}: {}/tests/view/{}", projectId,
                                suiteId, caseId, TEST_RAIL_BASE_URL, result.getTestId());
                    }
                }
            }
        }
    }

    private void addResult(ITestResult result) {
        ResultStatus resultStatus = testNgStatusToTestRailStatus(result.getStatus());

        addResult(result, resultStatus);
    }

    private Response addResultForCase(int runId, int caseId, AddResultRequest addResultRequest) {
        Response response = getAuthenticatedRequest().body(addResultRequest).post(getAddResultForCaseUrl(runId,
                caseId));

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            log.warn("Failed to update test case result: {}", response.asString());
        }

        return response;
    }

    private void addTestCaseIds(Map<ProjectSuiteMapping, AddRunRequest> testRunsToCreate, ITestNGMethod testNGMethod,
            TestRailCase testRailCase) {
        int projectId = getProjectId(testNGMethod, testRailCase);
        int suiteId = getSuiteId(testNGMethod, testRailCase);
        List<Integer> caseIds = getCaseIds(testNGMethod, testRailCase);

        ProjectSuiteMapping projectSuiteMapping = new ProjectSuiteMapping(projectId, suiteId);
        if (!testRunsToCreate.containsKey(projectSuiteMapping)) {
            testRunsToCreate.put(projectSuiteMapping, getNewTestRunRequest(suiteId));
        }

        List<Integer> testRunCaseIds = testRunsToCreate.get(projectSuiteMapping).getCaseIds();
        for (Integer caseIdToAdd : caseIds) {
            if (!testRunCaseIds.contains(caseIdToAdd)) {
                testRunCaseIds.add(caseIdToAdd);
            }
        }
    }

    private Response createRun(int projectId, AddRunRequest addRunRequest) {
        Response response = getAuthenticatedRequest().body(addRunRequest).post(getAddTestRunUrl(projectId));

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            log.warn("Failed to create test run: {}", response.asString());
        }

        return response;
    }

    private void createRuns(@NonNull Map<ProjectSuiteMapping, AddRunRequest> testRunsToCreate) {
        for (Map.Entry<ProjectSuiteMapping, AddRunRequest> testRunToCreate : testRunsToCreate.entrySet()) {
            AddRunRequest addRunRequest = getNewAddRunRequest(testRunToCreate);

            Response response = createRun(testRunToCreate.getKey().getProjectId(), addRunRequest);

            if (response.getStatusCode() == HttpStatus.SC_OK) {
                Run testRun = response.as(Run.class);
                ProjectSuiteMapping projectSuiteMapping = new ProjectSuiteMapping(testRun.getProjectId(),
                        addRunRequest.getSuiteId());

                testRuns.put(projectSuiteMapping, testRun);
            }
        }
    }

    private String getAddResultForCaseUrl(int runId, int caseId) {
        return String.format(TEST_RAIL_ADD_RESULT_FOR_CASE_URL, runId, caseId);
    }

    private String getAddTestRunUrl(int projectId) {
        return String.format(TEST_RAIL_ADD_RUN_URL, projectId);
    }

    private RequestSpecification getAuthenticatedRequest() {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);

        return given(requestSpecBuilder.build()).auth().preemptive().basic(TEST_RAIL_USER, TEST_RAIL_PASS);
    }

    private Response getCase(int caseId) {
        Response response = getAuthenticatedRequest().get(getReadCaseUrl(caseId));

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            log.warn("Failed to retrieve case {}: {}", caseId, response.asString());
        }

        return response;
    }

    private List<Integer> getCaseIds(ITestNGMethod method, TestRailCase annotation) {
        return getCaseIds(getMethod(method), annotation);
    }

    private List<Integer> getCaseIds(Method method, TestRailCase annotation) {
        TestRailCase[] classAnnotations = getClassAnnotations(method);

        List<Integer> cases = new ArrayList<>();

        if (classAnnotations.length > 0) {
            cases.addAll(Arrays.stream(classAnnotations[0].caseId()).boxed().collect(Collectors.toList()));
        }

        cases.addAll(Arrays.stream(annotation.caseId()).boxed().collect(Collectors.toList()));

        return cases;
    }

    private TestRailCase[] getClassAnnotations(Class<?> clazz) {
        return clazz.getAnnotationsByType(TestRailCase.class);
    }

    private TestRailCase[] getClassAnnotations(Method method) {
        return method.getDeclaringClass().getAnnotationsByType(TestRailCase.class);
    }

    private ZonedDateTime getCurrentDateTimePT() {
        return Instant.now().atZone(ZoneId.of("America/Los_Angeles"));
    }

    private Duration getDuration(ITestResult result) {
        Instant startTime = Instant.ofEpochMilli(result.getStartMillis());
        Instant endTime = Instant.ofEpochMilli(result.getEndMillis());
        Duration duration = Duration.between(startTime, endTime);

        if (duration.getSeconds() == 0) {
            duration = Duration.ofSeconds(1);
        }

        return duration;
    }

    private Method getMethod(ITestNGMethod method) {
        return method.getConstructorOrMethod().getMethod();
    }

    private TestRailCase[] getMethodAnnotations(ITestNGMethod method) {
        return getMethodAnnotations(getMethod(method));
    }

    private TestRailCase[] getMethodAnnotations(Method method) {
        return method.getAnnotationsByType(TestRailCase.class);
    }

    private AddResultRequest.AddResultRequestBuilder getNewAddResultRequest(ITestResult result, ResultStatus
            resultStatus) {
        AddResultRequest.AddResultRequestBuilder addResultRequest = AddResultRequest.builder();
        addResultRequest.statusId(resultStatus);

        Duration duration = getDuration(result);
        addResultRequest.elapsed(DurationFormatUtils.formatDurationWords(duration.toMillis(), true, false));

        addResultRequest.version(String.format("%s environment", System.getProperty("spring.profiles.active")));

        if (result.getAttribute(REASON_KEY) != null) {
            String reason = result.getAttribute(REASON_KEY).toString();
            addResultRequest.comment(reason);
        }

        return addResultRequest;
    }

    private AddRunRequest getNewAddRunRequest(Map.Entry<ProjectSuiteMapping, AddRunRequest> testRunToCreate) {
        AddRunRequest addRunRequest = testRunToCreate.getValue();

        addRunRequest.setDescription(getCurrentDateTimePT().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle
                .FULL)));
        addRunRequest.setIncludeAll(false);
        addRunRequest.setName(String.format("%s - %s", getCurrentDateTimePT().format(DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)), getSuiteName(addRunRequest.getSuiteId())));
        return addRunRequest;
    }

    private AddRunRequest getNewTestRunRequest(@NonNull int suiteId) {
        AddRunRequest.AddRunRequestBuilder addTestRunRequestBuilder = AddRunRequest.builder();
        addTestRunRequestBuilder.suiteId(suiteId);
        addTestRunRequestBuilder.caseIds(new ArrayList<>());

        return addTestRunRequestBuilder.build();
    }

    private int getProjectId(Method method, TestRailCase annotation) {
        TestRailCase[] classAnnotations = getClassAnnotations(method);

        if (annotation.projectId() != TestRailCase.DEFAULT_PROJECT_ID) {
            return annotation.projectId();
        } else if (classAnnotations.length > 0) {
            return classAnnotations[0].projectId();
        } else {
            return TestRailCase.DEFAULT_PROJECT_ID;
        }
    }

    private int getProjectId(ITestNGMethod method, TestRailCase annotation) {
        return getProjectId(getMethod(method), annotation);
    }

    private String getReadCaseUrl(int caseId) {
        return String.format(TEST_RAIL_READ_CASE_URL, caseId);
    }

    private String getReadTestSuiteUrl(int suiteId) {
        return String.format(TEST_RAIL_READ_SUITE_URL, suiteId);
    }

    private Response getSuite(int suiteId) {
        Response response = getAuthenticatedRequest().get(getReadTestSuiteUrl(suiteId));

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            log.warn("Failed to retrieve suite {}: {}", suiteId, response.asString());
        }

        return response;
    }

    private int getSuiteId(Method method, TestRailCase annotation) {
        TestRailCase[] classAnnotations = getClassAnnotations(method);

        if (annotation.suiteId() != TestRailCase.DEFAULT_SUITE_ID) {
            return annotation.suiteId();
        } else if (classAnnotations.length > 0) {
            return classAnnotations[0].suiteId();
        } else {
            return TestRailCase.DEFAULT_SUITE_ID;
        }
    }

    private int getSuiteId(ITestNGMethod method, TestRailCase annotation) {
        return getSuiteId(getMethod(method), annotation);
    }

    private String getSuiteName(int suiteId) {
        Response response = getSuite(suiteId);

        String suiteName = "";
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            Suite testSuite = response.as(Suite.class);
            suiteName = testSuite.getName();
        }

        return suiteName;
    }

    private String getUpdateCaseUrl(int caseId) {
        return String.format(TEST_RAIL_UPDATE_CASE_URL, caseId);
    }

    private boolean isValidTestRailCase(ITestNGMethod method, TestRailCase annotation) {
        int projectId = getProjectId(method, annotation);
        int suiteId = getSuiteId(method, annotation);
        List<Integer> cases = getCaseIds(method, annotation);

        return enabled && projectId != TestRailCase.DEFAULT_PROJECT_ID && suiteId != TestRailCase.DEFAULT_SUITE_ID &&
                !cases.isEmpty();
    }

    private ResultStatus testNgStatusToTestRailStatus(int status) {
        switch (status) {
            case ITestResult.SUCCESS:
                return ResultStatus.PASSED;
            case ITestResult.FAILURE:
                return ResultStatus.FAILED;
            case ITestResult.SKIP:
                return ResultStatus.BLOCKED;
            default:
                return ResultStatus.BLOCKED;
        }
    }

    private Response updateCase(int caseId, AddCaseRequest addCaseRequest) {
        Response response = getAuthenticatedRequest().body(addCaseRequest).post(getUpdateCaseUrl(caseId));

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            log.warn("Failed to update test case {}: {}", caseId, response.asString());
        }

        return response;
    }

    private void updateTestCaseType(int caseId, Case testCase) {
        if (testCase.getTypeId() == CaseType.MANUAL.statusId()) {
            AddCaseRequest.AddCaseRequestBuilder addCaseRequestBuilder = AddCaseRequest.builder();
            addCaseRequestBuilder.typeId(CaseType.API_AUTOMATED);

            updateCase(caseId, addCaseRequestBuilder.build());
        }
    }

    private void validateSuite(ISuite suite) {
        Set<Class> classes = new HashSet<>();
        Set<Method> methods = new HashSet<>();

        for (ITestNGMethod testNGMethod : suite.getAllMethods()) {
            classes.add(testNGMethod.getRealClass());
            methods.add(getMethod(testNGMethod));
        }

        for (Class<?> clazz : classes) {
            TestRailCase[] testClassAnnotations = getClassAnnotations(clazz);

            if (testClassAnnotations.length > 1) {
                log.warn("Only one TestRailCase annotation allowed per test class. Offending class: {}", clazz);
            }
        }

        for (Method method : methods) {
            TestRailCase[] methodAnnotations = getMethodAnnotations(method);

            for (TestRailCase testRailCase : methodAnnotations) {
                int projectId = getProjectId(method, testRailCase);
                int suiteId = getSuiteId(method, testRailCase);
                List<Integer> caseIds = getCaseIds(method, testRailCase);

                if (projectId == TestRailCase.DEFAULT_PROJECT_ID) {
                    log.warn("Project ID is required. Offending method: {}", method);
                }

                if (suiteId == TestRailCase.DEFAULT_SUITE_ID) {
                    log.warn("Suite ID is required. Offending method: {}", method);
                }

                if (caseIds.isEmpty()) {
                    log.warn("Cases are required. Offending method: {}", method);
                }
            }
        }
    }
}
