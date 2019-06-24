package org.nowhere_lights.testframework.testutils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int tryCounter = 0;
    private static final int TRY_MAX = 2;

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (!iTestResult.isSuccess()) {
            if (tryCounter < TRY_MAX) {
                iTestResult.setStatus(iTestResult.SKIP);
                iTestResult.getTestContext().getFailedTests().removeResult(iTestResult);
                iTestResult.getTestContext().getSkippedTests().removeResult(iTestResult);
                tryCounter++;
                return true;
            }
            iTestResult.setStatus(iTestResult.FAILURE);
            return false;
        }
        iTestResult.setStatus(iTestResult.SUCCESS);
        return false;
    }
}
