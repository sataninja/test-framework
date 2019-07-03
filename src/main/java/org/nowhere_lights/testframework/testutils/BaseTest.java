package org.nowhere_lights.testframework.testutils;

import com.codeborne.selenide.testng.GlobalTextReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.WebDriverFactory;
import org.nowhere_lights.testframework.drivers.utils.EmailUtils;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.nowhere_lights.testframework.drivers.vars.Environment;
import org.nowhere_lights.testframework.drivers.utils.Wrappers;
import org.nowhere_lights.testframework.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

@Listeners({TestMethodListener.class, GlobalTextReport.class, TestListener.class})
public class BaseTest extends Wrappers {

    private static final Logger _logger = LogManager.getLogger(BaseTest.class.getSimpleName());

    private static PropertiesContext propertiesContext = PropertiesContext.getInstance();
    private static final String DEFAULT_URL = null;
    private static final String URL_TEST = propertiesContext.getProperty("urltest");
    private static final String URL_STG = propertiesContext.getProperty("urlstg");
    private static final String EMAIL_USERNAME = propertiesContext.getProperty("email.address");
    private static final String EMAIL_PASSWORD = propertiesContext.getProperty("email.password");
    private static final String EMAIL_SMTP_HOST = propertiesContext.getProperty("mail.smtp.host");
    protected static final String USER_EMAIL_ADMIN = propertiesContext.getProperty("user.email.admin");
    protected static final String USER_PASSWORD_ADMIN = propertiesContext.getProperty("user.password.admin");
    protected static final String USER_EMAIL_MEMBER = propertiesContext.getProperty("user.email.member");
    protected static final String USER_PASS_MEMBER = propertiesContext.getProperty("user.password.member");
    protected static final Boolean RETRY_ON = Boolean.valueOf(propertiesContext.getProperty("retry"));
    private static Environment env = Environment.toEnum(propertiesContext.getProperty("env"));
    protected static EmailUtils emailUtils;
    protected SoftAssert softAssert;

    public Long suitStart, suitStop = 0L, elapsedTime;

    private static Map<String, String> pageNames = propertiesContext
            .getPagesNames(propertiesContext.getPagesMap(), "pages");
    private static Map<String, BasePage> pages = new HashMap<>();

    protected static <T> T getPage(String pageName) {
        return (T) pages.get(pageName);
    }

    @BeforeSuite
    public void beforeSuite(final ITestContext context) {
        if (RETRY_ON)
            for (ITestNGMethod method : context.getAllTestMethods())
                method.setRetryAnalyzer(new RetryAnalyzer());
        suitStart = System.currentTimeMillis();
        _logger.info("<br>Setting server: " + env.getValue());
        _logger.info("<br>Setting browser: " + PropertiesContext.getInstance().getProperty("browser"));
    }

    @BeforeClass
    public static void connectToEmail() {
        try {
            if (EMAIL_USERNAME == null || EMAIL_PASSWORD == null || EMAIL_SMTP_HOST == null)
                emailUtils = new EmailUtils(EMAIL_USERNAME, EMAIL_PASSWORD, EMAIL_SMTP_HOST, EmailUtils.EmailFolder.INBOX);
        } catch (Exception e) {
            _logger.warn("No EMAIL Inbox specified");
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(final ITestContext testContext) {
        new WebDriverFactory().setWebDriver();
        softAssert = SoftAssert.getInstance(getWebDriver());

        try {
            for (Map.Entry<String, String> pageEntry : pageNames.entrySet()) {
                Class<?> clazz = Class.forName(pageEntry.getValue());
                Constructor<?> constructor = clazz.getConstructor(WebDriver.class);
                BasePage page = (BasePage) constructor.newInstance(getWebDriver());
                pages.put(pageEntry.getKey(), page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (env == Environment.TESTING) {
            open(URL_TEST);
        } else if (env == Environment.STAGING) {
            open(URL_STG);
        } else {
            _logger.warn("No environment set, tests will run on: " + DEFAULT_URL);
            open(DEFAULT_URL);
        }
        _logger.info(" [I] - Using " + env + " environment.");
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(final ITestContext testContext, ITestResult result) {
        _logger.info("<br>Completed test method " + result.getName());
        _logger.info("<br>****************************************************");
        _logger.info("<br>Time taken by method " + result.getName() + ": " + (result.getEndMillis() - result.getStartMillis()) / 1000 + "sec");
        _logger.info("<br>****************************************************");
    }

    @AfterSuite
    public void afterSuite(final ITestContext ctx) {
        String suitName = ctx.getSuite().getName();
        suitStop = System.currentTimeMillis();
        elapsedTime = (suitStop - suitStart) / 1000;
        _logger.info("<br>Suit  " + suitName + " took " + elapsedTime + " seconds");
    }

    public SoftAssert getSoftAssert() {
        return softAssert;
    }

    @Override
    public WebDriver getDriver() {
        return getWebDriver();
    }
}
