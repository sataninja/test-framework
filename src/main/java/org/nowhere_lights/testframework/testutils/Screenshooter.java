package org.nowhere_lights.testframework.testutils;


import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

public class Screenshooter {

    private static final String SNAPSHOT_NAME_PATTERN = "%s/%s---%s-%d.png";

    /**
     * @return Relative path to snapshot
     */
    public static String takeScreenShot(String methodName, WebDriver driver) {
        //get the driver
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        if (scrFile.exists()) {
            Reporter.log("*** Stored screenshot in " + scrFile.getAbsolutePath() + " ***");
        }

        String reportDir = PropertiesContext.getInstance().getProperty(SoftAssert.SNAPSHOT_DIRECTORY);
        String filePath = String.format(SNAPSHOT_NAME_PATTERN,
                SoftAssert.SNAPSHOTS,
                methodName,
                SoftAssert.format.format(new Date()),
                (int) (Math.random() * 9999));

        //The below method will save the screen shot in d drive with test method name
        try {
            File destination = new File(reportDir, filePath);
            System.out.println("*** Moving screenshot to " + destination.getAbsolutePath() + " ***");
            destination.getParentFile().mkdirs();
            Files.copy(scrFile.toPath(), destination.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

}
