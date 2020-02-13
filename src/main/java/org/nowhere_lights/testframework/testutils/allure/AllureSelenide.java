package org.nowhere_lights.testframework.testutils.allure;

import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.util.ResultsUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class AllureSelenide implements LogEventListener {

    private final AllureLifecycle lifecycle;

    public AllureSelenide() {
        this(Allure.getLifecycle());
    }

    public AllureSelenide(final AllureLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public static byte[] getScreenShotBytes() {
        return ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.BYTES);
    }

    public static byte[] getPageSourceBytes() {
        return getWebDriver().getPageSource().getBytes(StandardCharsets.UTF_8);
    }

    //actual for selenide 5.7.0 which causes the java.lang.IllegalStateException bug
    @Override
    public void beforeEvent(LogEvent currentLog) {
        lifecycle.getCurrentTestCaseOrStep().ifPresent(parentUuid -> {
            final String uuid = UUID.randomUUID().toString();
            lifecycle.startStep(parentUuid, uuid, new StepResult()
                    .setName(currentLog.toString()));
        });
        lifecycle.updateStep(stepResult -> stepResult.setStart(stepResult.getStart() - currentLog.getDuration()));
    }

    @Override
    public void afterEvent(LogEvent currentLog) {
        if (LogEvent.EventStatus.FAIL.equals(currentLog.getStatus())) {
            lifecycle.addAttachment("Screenshot", "image/png", "png", getScreenShotBytes());
            lifecycle.addAttachment("Page source", "text/html", "html", getPageSourceBytes());
            lifecycle.updateStep(stepResult -> {
                final StatusDetails details = ResultsUtils.getStatusDetails(currentLog.getError())
                        .orElse(new StatusDetails());
                stepResult.setStatus(Status.FAILED);
                stepResult.setStatusDetails(details);
            });
        }
        lifecycle.stopStep();
    }

    //actual for selenide 4
//    @Override
//    public void onEvent(LogEvent currentLog) {
//        lifecycle.getCurrentTestCaseOrStep().ifPresent(parentUuid -> {
//            final String uuid = UUID.randomUUID().toString();
//            lifecycle.startStep(parentUuid, uuid, new StepResult()
//                    .setName(currentLog.toString()));
//        });
//        lifecycle.updateStep(stepResult -> stepResult.setStart(stepResult.getStart() - currentLog.getDuration()));
//        if (LogEvent.EventStatus.FAIL.equals(currentLog.getStatus())) {
//            lifecycle.addAttachment("Screenshot", "image/png", "png", getScreenShotBytes());
//            lifecycle.addAttachment("Page source", "text/html", "html", getPageSourceBytes());
//            lifecycle.updateStep(stepResult -> {
//                final StatusDetails details = ResultsUtils.getStatusDetails(currentLog.getError())
//                        .orElse(new StatusDetails());
//                stepResult.setStatus(Status.FAILED);
//                stepResult.setStatusDetails(details);
//            });
//        }
//        lifecycle.stopStep();
//    }
}
