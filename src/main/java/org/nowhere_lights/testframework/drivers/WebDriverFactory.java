package org.nowhere_lights.testframework.drivers;

import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.vars.Browser;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;

import static com.codeborne.selenide.WebDriverRunner.clearBrowserCache;


public class WebDriverFactory {

    private static final Logger _logger = LogManager.getLogger(WebDriverFactory.class.getSimpleName());
    private static Browser browser = Browser.toEnum(PropertiesContext.getInstance().getProperty("browser"));

    public static final int DEFAULT_TIMEOUT = 15;

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
            clearBrowserCache();
            WebDriverManager.chromedriver().setup();
            Configuration.browser = ChromeDriverDesktop.class.getName();
        }
//        Configuration.browserPosition = "790x10";
//        Configuration.browserSize = "375x812"; //iphone xs viewport
        Configuration.startMaximized = false;
        Configuration.timeout = 10000;
    }
}
