package org.nowhere_lights.testframework.drivers;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.nowhere_lights.testframework.drivers.vars.Browser;
import org.nowhere_lights.testframework.testutils.allure.AllureSelenide;
import org.openqa.selenium.remote.DesiredCapabilities;


public class WebDriverFactory {

    public static final int DEFAULT_TIMEOUT = 15;
    private static final Logger _logger = LogManager.getLogger(WebDriverFactory.class.getSimpleName());
    private static Browser browser = Browser.toEnum(PropertiesContext.getInstance().getProperty("browser"));
    private BrowserstackDriver browserstackDriver = new BrowserstackDriver();

    public static boolean isBrowserStack() {
        return System.getenv("BROWSERSTACK_USERNAME") != null && System.getenv("BROWSERSTACK_ACCESS_KEY") != null;
    }

    public void setWebDriver() throws Exception {
//        open(PropertiesContext.getInstance().getProperty("urltest"));
        if (isBrowserStack()) {
            browserstackDriver.createBrowserStackDriver();
        } else {
            _logger.warn("Setting desktop driver");
            if (browser == Browser.CHROME) {
                WebDriverManager.chromedriver().setup();
                Configuration.browser = ChromeDriverDesktop.class.getName();
            } else if (browser == Browser.FIREFOX) {
                WebDriverManager.firefoxdriver().setup();
                Configuration.browser = "firefox";
//                FirefoxOptions ffoptions = new FirefoxOptions();
                System.setProperty("firefoxprofile.dom.webnotifications.serviceworker.enabled", "false");
                System.setProperty("firefoxprofile.dom.webnotifications.enabled", "false");
                System.setProperty("firefoxprofile.geo.enabled", "false");
                //set proxy here
//                ffoptions.setProxy(ClientUtil.createSeleniumProxy(proxy));
            } else {
                _logger.warn("No driver property found, using default browser (chrome)");
                WebDriverManager.chromedriver().setup();
                Configuration.browser = ChromeDriverDesktop.class.getName();
            }
//            Configuration.startMaximized = false;
//            Configuration.proxyEnabled = true;
//            Configuration.fileDownload = FileDownloadMode.PROXY;
            Configuration.browserSize = "1600x1400";
            Configuration.timeout = 10000;
            String remote = System.getenv("BROWSER_URL");
            if (remote != null) {
                Configuration.remote = remote;
                DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
                desiredCapabilities.setCapability("enableVNC", true);
                Configuration.browserCapabilities = desiredCapabilities;
                if (browser == Browser.FIREFOX) {
                    Configuration.browser = "firefox";
                } else {
                    Configuration.browser = "chrome";
                }
            } else {
                _logger.warn("Remote driver is not set.");
            }
        }
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
//        open(PropertiesContext.getInstance().getProperty("urltest"));
    }

    public void closeBrowserstack() throws Exception {
        browserstackDriver.close();
    }
}
