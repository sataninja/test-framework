package org.nowhere_lights.testframework.testutils;

import com.browserstack.local.Local;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.GlobalTextReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.nowhere_lights.testframework.drivers.WaitDriver;
import org.nowhere_lights.testframework.drivers.WebDriverFactory;
import org.nowhere_lights.testframework.drivers.utils.EmailUtils;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.nowhere_lights.testframework.drivers.utils.Wrappers;
import org.nowhere_lights.testframework.drivers.vars.Environment;
import org.nowhere_lights.testframework.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    //browserstack
    private static String username;
    private static String accessKey;
    private static String sessionId;
    private Local local;
    private static final String BROWSERSTACK_PATH = System.getProperty("user.dir") + "/src/main/resources/browserstack/config.json";
    public RemoteWebDriver remoteWebDriver;

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
        if (System.getenv("BROWSERSTACK_USERNAME") == null && System.getenv("BROWSERSTACK_ACCESS_KEY") == null) {
            _logger.info("<br>Setting server: " + env.getValue());
            _logger.info("<br>Setting browser: " + PropertiesContext.getInstance().getProperty("browser"));
        }
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
     * </p>
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters(value = {"environment"})
    public void beforeMethod(final ITestContext testContext, @Optional String environment) throws Exception {
        _logger.info("<br>Starting test: " + testContext.getName());
        _logger.info("<br>****************************************************");
        if (System.getenv("BROWSERSTACK_USERNAME") != null && System.getenv("BROWSERSTACK_ACCESS_KEY") != null) {
            JSONParser jsonParser = new JSONParser();
            JSONObject config = (JSONObject) jsonParser.parse(new FileReader(BROWSERSTACK_PATH));
            JSONObject envs = (JSONObject) config.get("environments");

            username = System.getenv("BROWSERSTACK_USERNAME");
            if (username == null)
                username = PropertiesContext.getInstance().getProperty("browserstackusername");
            accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            if (accessKey == null)
                accessKey = PropertiesContext.getInstance().getProperty("browserstackaccesskey");

            DesiredCapabilities capabilities = new DesiredCapabilities();

            //browserstack config
            Map<String, String> envCapabilities = (Map<String, String>) envs.get(environment);
            Iterator it = envCapabilities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
            }

            Map<String, String> commonCapabilities = (Map<String, String>) config.get("capabilities");
            it = commonCapabilities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (capabilities.getCapability(pair.getKey().toString()) == null) {
                    capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
                }
            }

            if (capabilities.getCapability("browserstack.local") != null && capabilities.getCapability("browserstack.local") == "true") {
                local = new Local();
                Map<String, String> options = new HashMap<>();
                options.put("key", accessKey);
                local.start(options);
            }
            try {
                remoteWebDriver = new RemoteWebDriver(
                        new URL("http://" + username + ":" + accessKey + "@" + config.get("server") + "/wd/hub"), capabilities);
                remoteWebDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                sessionId = remoteWebDriver.getSessionId().toString();
                WebDriverRunner.setWebDriver(remoteWebDriver);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            _logger.info("Using " + environment + " environment.");
        }
        if (System.getenv("BROWSERSTACK_USERNAME") == null && System.getenv("BROWSERSTACK_ACCESS_KEY") == null) {
            _logger.warn("No BrowserStack driver configured, setting desktop driver");
            new WebDriverFactory().setWebDriver();
            softAssert = SoftAssert.getInstance(getWebDriver());
        }
        for (Map.Entry<String, String> pageEntry : pageNames.entrySet()) {
            try {
                Class<?> clazz = Class.forName(pageEntry.getValue());
                Constructor<?> constructor = clazz.getConstructor(WebDriver.class);
                BasePage page = (BasePage) constructor.newInstance(getWebDriver());
                pages.put(pageEntry.getKey(), page);
            } catch (InstantiationException |
                    InvocationTargetException |
                    NoSuchMethodException |
                    IllegalAccessException |
                    ClassNotFoundException e) {
                _logger.warn("Couldn't initialize page: " +
                        pageEntry.getKey(), pageEntry.getValue(), e.getCause());
                e.printStackTrace();
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(final ITestContext testContext, ITestResult iTestResult) throws Exception {
        _logger.info("<br>Completed test method " + iTestResult.getName());
        _logger.info("<br>****************************************************");
        _logger.info("<br>Time taken by method " + iTestResult.getName() + ": " + (iTestResult.getEndMillis() - iTestResult.getStartMillis()) / 1000 + "sec");
        _logger.info("<br>****************************************************");
        if (remoteWebDriver != null)
            remoteWebDriver.quit();
        if (local != null)
            local.stop();
    }

    @AfterSuite
    public void afterSuite(final ITestContext iTestContext) {
        String suitName = iTestContext.getSuite().getName();
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

    public WaitDriver getWaitDriver() {
        return WaitDriver.getInstance(getDriver());
    }
}
