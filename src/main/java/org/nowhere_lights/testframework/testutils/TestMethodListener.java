package org.nowhere_lights.testframework.testutils;


import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestClass;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

public class TestMethodListener implements IInvokedMethodListener {
    @Override
    public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
    }

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        ITestClass invokingClass = method.getTestMethod().getTestClass();
        Object[] classInstance = invokingClass.getInstances(true);
        if (method.isTestMethod()) {
            if (classInstance[0] instanceof BaseTest) {
                BaseTest testCase = (BaseTest) classInstance[0];
                SoftAssert soft = testCase.getSoftAssert();
                if (soft == null) {
                    return; //softAssert will be null in case driver initialisation failed or something went
                            // wrong in @BeforeMethod so just in case avoiding NPE here
                }
                try {
                    soft.assertAll(testResult);
                } catch (AssertionError e) {
                    testResult.setStatus(TestResult.FAILURE); //make test fail if there are soft assert fails
                    testResult.setThrowable(e);
                }
            }
        }
    }
}
