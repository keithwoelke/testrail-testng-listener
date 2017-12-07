package com.github.keithwoelke.testrail.testrail;

import com.github.keithwoelke.assertion.TestNGAssertionListener;
import lombok.extern.slf4j.Slf4j;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * This TestNG listener can be used to trigger test failures on expectation failure where a test would otherwise pass,
 * provide useful stack traces both both expectations and assertions, and provide reporting/metrics on a per test case
 * basis as well as at the completion of a test run. Additionally, it will report its results to TestRail if the test
 * cases are annotated with the TestRailCase annotation.
 *
 * @author wkwoelke
 * @see TestNGTestRailListener
 * @see TestNGAssertionListener
 * @see TestRailCase
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Slf4j
public class TestNGAssertionTestRailListener implements ITestListener, ISuiteListener {

    private TestNGTestRailListener testNGTestRailListener = new TestNGTestRailListener();
    private TestNGAssertionListener testNGAssertionListener = new TestNGAssertionListener();

    @Override
    public void onTestStart(ITestResult result) {
        testNGAssertionListener.onTestStart(result);
        testNGTestRailListener.onTestStart(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        testNGAssertionListener.onTestSuccess(result);
        testNGTestRailListener.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        testNGAssertionListener.onTestFailure(result);
        testNGTestRailListener.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        testNGAssertionListener.onTestSkipped(result);
        testNGTestRailListener.onTestSkipped(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        testNGAssertionListener.onTestFailedButWithinSuccessPercentage(result);
        testNGTestRailListener.onTestFailedButWithinSuccessPercentage(result);
    }

    @Override
    public void onStart(ITestContext context) {
        testNGAssertionListener.onStart(context);
        testNGTestRailListener.onStart(context);
    }

    @Override
    public void onFinish(ITestContext context) {
        testNGAssertionListener.onFinish(context);
        testNGTestRailListener.onFinish(context);
    }

    @Override
    public void onStart(ISuite suite) {
        testNGTestRailListener.onStart(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        testNGTestRailListener.onFinish(suite);
    }
}