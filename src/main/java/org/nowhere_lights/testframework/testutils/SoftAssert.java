package org.nowhere_lights.testframework.testutils;


import org.nowhere_lights.testframework.drivers.utils.ReportURL;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;
import org.testng.collections.Maps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class SoftAssert extends Assertion {

    public static final String PROJECT_DIRECTORY = "project.directory";
    public static final String SNAPSHOT_DIRECTORY = "snapshot.directory";
    public static final String SNAPSHOTS = "snapshots";
    public static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy__HH_mm_ss");
    private static Hashtable<WebDriver, SoftAssert> assertsPerDriver = new Hashtable<>();
    private Map<AssertionError, IAssert> m_errors = Maps.newLinkedHashMap();
    private WebDriver driver;

    private SoftAssert(WebDriver driver) {
        this.driver = driver;
    }

    public static synchronized SoftAssert getInstance(WebDriver driver) {
        if (driver != null && assertsPerDriver.containsKey(driver)) {
            return assertsPerDriver.get(driver);
        } else if (driver != null) {
            SoftAssert softAssert = new SoftAssert(driver);
            assertsPerDriver.put(driver, softAssert);
            return softAssert;
        }
        throw new NullPointerException("No driver was found");
    }

    @Override
    public void executeAssert(IAssert a) {
        onBeforeAssert(a);
        try {
            a.doAssert();
            onAssertSuccess(a);
        } catch (AssertionError ex) {
            onAssertFailure(a, ex);
            Reporter.log(ex.getMessage(), true);
            String filePath = takeScreenShot(ex);
            m_errors.put(ex, a);
            Reporter.log("<br> " + filePath, 1, true);
            Reporter.log("<br><img src=\"" + ReportURL.build(filePath) + "\"/>", 1, true);
        } finally {
            onAfterAssert(a);
        }
    }

    public String takeScreenShot(AssertionError ex) {
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        StackTraceElement test = null;
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.getClassName().contains(BaseTest.class.getPackage().getName()) && !stackTraceElement.getClassName().contains(BaseTest.class.getName())) {
                test = stackTraceElement;
                break;
            }
        }

        if (test == null) {
            test = stackTraceElements[0];
        }
        String snapshotName = test.getClassName().substring(test.getClassName().lastIndexOf(".") + 1) + "_"
                + test.getMethodName();
        return Screenshooter.takeScreenShot(snapshotName, driver);
    }

    public void assertAll(ITestResult testResult) {
        if (!m_errors.isEmpty()) {
            String mainMessage = "";
            if (!testResult.isSuccess() && testResult.getThrowable() != null) {
                Reporter.log("Printing soft assert stacktrace:\n" + testResult.getThrowable(), true);
                mainMessage = testResult.getThrowable().getMessage();
            }
            StringBuilder sb = new StringBuilder("The following asserts failed:\n");
            boolean first = true;
            for (Map.Entry<AssertionError, IAssert> ae : m_errors.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",\n");
                }
                sb.append(ae.getKey().getMessage());
            }
            sb.append("\n\n").append(mainMessage);
            throw new AssertionError(sb.toString());
        }
    }


    /**
     * Compare two maps by key and value
     * assert fail if keys and values not equal
     *
     * @param expected
     * @param actual
     * @param shouldHaveSameSize
     */
    public void verifyIfMapsAreEqual(Map expected, Map actual, boolean shouldHaveSameSize) {
        if (shouldHaveSameSize)
            assertEquals(expected.size(), actual.size(), "Size of 1st map: " + expected.size() + " is not equal to size of 2nd map " + actual.size());
        try {
            Iterator<Map.Entry<String, String>> i = actual.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> e = i.next();
                String key = e.getKey();
                String value = e.getValue();
                if (value != null) {
                    assertEquals(value, expected.get(key), "Values are different on key: " + key);
                }
            }
        } catch (ClassCastException unused) {
        } catch (NullPointerException unused) {
        }
    }

    /**
     * Verify file in downloads folder has query in its name and delete it
     *
     * @param fileName
     */
    public void verifyFileDownloaded(String fileName) {
        String path = System.getProperty("user.dir");
        File folder = new File(path + "/downloadedFiles");
        File[] listOfFiles = folder.listFiles();
        boolean isFilePresent = false;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(fileName)) {
                isFilePresent = true;
                listOfFiles[i].deleteOnExit();
            }
        }
        assertTrue(isFilePresent, "File " + fileName + " was not downloaded");
    }


}
