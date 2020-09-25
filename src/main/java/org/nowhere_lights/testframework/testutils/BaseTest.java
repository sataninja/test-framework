package org.nowhere_lights.testframework.testutils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.BrowserstackDriver;
import org.nowhere_lights.testframework.drivers.ProxyProvider;
import org.nowhere_lights.testframework.drivers.WaitDriver;
import org.nowhere_lights.testframework.drivers.WebDriverFactory;
import org.nowhere_lights.testframework.drivers.utils.EmailUtils;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.nowhere_lights.testframework.drivers.vars.Environment;
import org.nowhere_lights.testframework.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static org.nowhere_lights.testframework.drivers.BrowserstackDriver.isBrowserStack;

@Listeners({TestMethodListener.class, TestListener.class})
public abstract class BaseTest {

    private static PropertiesContext propertiesContext = PropertiesContext.getInstance();
    private static Environment env = Environment.toEnum(propertiesContext.getProperty("env"));
    private static final Logger _logger = LogManager.getLogger(BaseTest.class.getSimpleName());
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
    protected static EmailUtils emailUtils;
    protected WebDriverFactory webDriverFactory = new WebDriverFactory();
    protected ProxyProvider proxyProvider = new ProxyProvider();
    protected SoftAssert softAssert;
    public Long suiteStart, suiteStop = 0L, elapsedTime;

    private static Map<String, String> pageNames = propertiesContext.getPagesNames(
            propertiesContext.getPagesMap(), "pages");
    private static Map<String, BasePage> pages = new HashMap<>();

    protected static <T> T getPage(String pageName) {
        return (T) pages.get(pageName);
    }

    @BeforeSuite
    public void beforeSuite(final ITestContext context) {
        if (RETRY_ON)
            for (ITestNGMethod method : context.getAllTestMethods())
                method.setRetryAnalyzer(new RetryAnalyzer());
        suiteStart = System.currentTimeMillis();
        if (isBrowserStack()) {
            _logger.info("<br>Setting browserstack driver: " + env.getValue());
        } else {
            _logger.info("<br>Setting server: " + env.getValue());
            _logger.info("<br>Setting browser: " + PropertiesContext.getInstance().getProperty("browser"));
        }
//        proxyProvider.setProxy();
//        proxyProvider.getRequestHeaders();
//        proxyProvider.getResponseHeader();
    }

    @BeforeClass
    public static void connectToEmail() {
        try {
            if (EMAIL_USERNAME != null && EMAIL_PASSWORD != null && EMAIL_SMTP_HOST != null)
                emailUtils = new EmailUtils(EMAIL_USERNAME, EMAIL_PASSWORD, EMAIL_SMTP_HOST, EmailUtils.EmailFolder.INBOX);
        } catch (Exception e) {
            _logger.warn("No EMAIL Inbox specified");
        }
    }

    /**
     * WARNING!
     *
     * <p>
     * <p>
     * If one or more pages is broken for some reason, e.g. wrong path, then
     * initializing process will fail causing one of the following exceptions
     *
     *
     * </p>
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(final ITestContext testContext, ITestResult testResult) throws Exception {
        _logger.info("<br>Starting test: " + testContext.getName());
        _logger.info("<br>****************************************************");
        if (isBrowserStack()) {
            try {
                propertiesContext.setProperty("bsname", testResult.getMethod().getDescription());
            } catch (NullPointerException ignored) {
                propertiesContext.setProperty("bsname", testResult.getMethod().getMethodName());
            }
        }
        webDriverFactory.setWebDriver();
        softAssert = SoftAssert.getInstance(getDriver());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(final ITestContext testContext, ITestResult iTestResult) throws Exception {
        _logger.info("<br>Completed test method " + iTestResult.getName());
        _logger.info("<br>****************************************************");
        _logger.info("<br>Time taken by method " + iTestResult.getName() + ": " + (iTestResult.getEndMillis() - iTestResult.getStartMillis()) / 1000 + "sec");
        _logger.info("<br>****************************************************");
        webDriverFactory.closeWebDriver();
        BrowserstackDriver.closeBrowserstack();
    }

    @AfterSuite
    public void afterSuite(final ITestContext iTestContext) {
        String suiteName = iTestContext.getSuite().getName();
        suiteStop = System.currentTimeMillis();
        elapsedTime = (suiteStop - suiteStart) / 1000;
        _logger.info("<br>Suite  " + suiteName + " took " + elapsedTime + " seconds");
//        ProxyProvider.getProxy().stop();
    }

    public SoftAssert getSoftAssert() {
        return softAssert;
    }

    public WebDriver getDriver() {
        return getWebDriver();
    }

    public WaitDriver getWaitDriver() {
        return WaitDriver.getInstance(getDriver());
    }
}
