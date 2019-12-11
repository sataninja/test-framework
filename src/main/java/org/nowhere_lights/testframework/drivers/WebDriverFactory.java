package org.nowhere_lights.testframework.drivers;

import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.nowhere_lights.testframework.drivers.vars.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;


public class WebDriverFactory {

    public static final int DEFAULT_TIMEOUT = 15;
    private static final Logger _logger = LogManager.getLogger(WebDriverFactory.class.getSimpleName());
    private static Browser browser = Browser.toEnum(PropertiesContext.getInstance().getProperty("browser"));

    public void setWebDriver() {
        if (browser == Browser.CHROME) {
            WebDriverManager.chromedriver().setup();
            Configuration.browser = ChromeDriverDesktop.class.getName();
        } else if (browser == Browser.FIREFOX) {
            WebDriverManager.firefoxdriver().setup();
            Configuration.browser = "firefox";
            System.setProperty("firefoxprofile.dom.webnotifications.serviceworker.enabled", "false");
            System.setProperty("firefoxprofile.dom.webnotifications.enabled", "false");
            System.setProperty("firefoxprofile.geo.enabled", "false");
        } else {
            _logger.warn("No driver property found, using default browser");
            WebDriverManager.chromedriver().setup();
            Configuration.browser = ChromeDriverDesktop.class.getName();
        }
        Configuration.startMaximized = false;
        Configuration.timeout = 10000;
        String remote = System.getenv("BROWSER_URL");
        if (remote != null) {
            Configuration.remote = remote;
            DesiredCapabilities chromeDesiredCapabilities = new DesiredCapabilities();
            chromeDesiredCapabilities.setCapability("enableVNC", true);
            Configuration.browserCapabilities = chromeDesiredCapabilities;
            if (browser == Browser.FIREFOX) {
                Configuration.browser = "firefox";
            } else {
                Configuration.browser = "chrome";
            }
        } else {
            _logger.warn("Remote driver is not set.");
        }
    }
}
