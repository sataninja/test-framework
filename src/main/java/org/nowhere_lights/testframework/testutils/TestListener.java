package org.nowhere_lights.testframework.testutils;


import com.codeborne.selenide.testng.ScreenShooter;
import org.nowhere_lights.testframework.drivers.utils.ReportURL;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.*;
import org.testng.annotations.Listeners;

import java.util.Set;
import java.util.logging.Level;

import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

@Listeners({ScreenShooter.class})
public class TestListener extends TestListenerAdapter {
    private static final String SNAPSHOT_HEIGHT = "480px";
    private static final String SNAPSHOT_WIDTH = "848px";

    @Override
    public void onTestStart(ITestResult result) {
        Reporter.log("Starting tests " + result.getName() + "\n", true);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Reporter.log("Test " + result.getName() + " has passed!!!", true);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        for (LogEntry entry : getWebDriver().manage().logs().get(LogType.BROWSER).filter(Level.SEVERE)) {
            Reporter.log("<br>***** Severe JS error " + entry.getMessage() + " *****", true);
        }
        for (LogEntry entry : getWebDriver().manage().logs().get(LogType.BROWSER).filter(Level.WARNING)) {
            Reporter.log("<br>***** Warning JS error " + entry.getMessage() + " *****", true);
        }
        Reporter.log("<br>***** Error " + result.getName() + " test has failed *****", true);
        Reporter.log("<br>***** Error message: " + result.getThrowable().getMessage() + " *****", true);
        Reporter.log("<br>***** Caused by: " + result.getThrowable().getCause() + " *****", true);
        String methodName = result.getName().trim();
        String filePath = Screenshooter.takeScreenShot(methodName, getWebDriver());
        Reporter.setCurrentTestResult(result);
        Reporter.log("<br>" + filePath, 1, true);
        Reporter.log("<br><img src=\"" + ReportURL.build(filePath)
                + "\" height=\"" + SNAPSHOT_HEIGHT
                + "\" width=\"" + SNAPSHOT_WIDTH + "\">", 1, true);
        Reporter.setCurrentTestResult(null);
        closeWebDriver();
    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onFinish(ITestContext context) {
        Set<ITestResult> failedTests = context.getFailedTests().getAllResults();
        Set<ITestResult> skippedTests = context.getSkippedTests().getAllResults();
        for (ITestResult temp : failedTests) {
            ITestNGMethod method = temp.getMethod();
            if (context.getFailedTests().getResults(method).size() > 1) {
                failedTests.remove(temp);
            } else {
                if (context.getPassedTests().getResults(method).size() > 0) {
                    failedTests.remove(temp);
                }
            }
        }
        for (ITestResult temp : skippedTests) {
            ITestNGMethod method = temp.getMethod();
            if (context.getSkippedTests().getResults(method).size() > 1) {
                skippedTests.remove(temp);
            } else {
                if (context.getPassedTests().getResults(method).size() > 0) {
                    skippedTests.remove(temp);
                }
            }
        }
        closeWebDriver();
    }
}
