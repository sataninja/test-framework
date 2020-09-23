package org.nowhere_lights.testframework.drivers;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.nowhere_lights.testframework.drivers.vars.Browser;
import org.nowhere_lights.testframework.testutils.allure.AllureSelenide;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.nowhere_lights.testframework.drivers.BrowserstackDriver.isBrowserStack;


public class WebDriverFactory {

    public static final int DEFAULT_TIMEOUT = 15;
    private static final Logger _logger = LogManager.getLogger(WebDriverFactory.class.getSimpleName());
    private static final PropertiesContext propertiesContext = PropertiesContext.getInstance();
    private static Browser browser = Browser.toEnum(propertiesContext.getProperty("browser"));
    private WebDriver webDriver;

    public void setWebDriver() throws Exception {
        if (isBrowserStack()) {
            webDriver = BrowserstackDriver.createBrowserStackDriver();
        } else if (propertiesContext.getProperty("selenoid.run").equalsIgnoreCase("true") &&
                propertiesContext.getProperty("selenoid.url") != null) {
            _logger.info("Starting selenoid driver...");
//            webDriver = SelenoidRemoteDriverDesktop.createRemoteWebDriver();
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            if (PropertiesContext.getInstance().getProperty("browser") != null)
                desiredCapabilities.setBrowserName(PropertiesContext.getInstance().getProperty("browser"));
            else desiredCapabilities.setBrowserName("chrome");
            if (PropertiesContext.getInstance().getProperty("browser.version") != null)
                desiredCapabilities.setVersion(PropertiesContext.getInstance().getProperty("browser.version"));
            desiredCapabilities.setCapability("enableVNC", true);
            desiredCapabilities.setCapability("enableVideo", false);
            webDriver = new SelenoidRemoteDriverDesktop().createDriver(desiredCapabilities);
        } else {
            _logger.info("SETTING DESKTOP DRIVER!");
            if (browser == Browser.CHROME) {
                _logger.info("Setting Chrome driver...");
                WebDriverManager.chromedriver().setup();
                webDriver = new ChromeDriver(ChromeDriverDesktop.getChromeOptions());
            } else if (browser == Browser.FIREFOX) {
                _logger.info("Setting Firefox driver...");
                WebDriverManager.firefoxdriver().setup();
                webDriver = new FirefoxDriver();
                System.setProperty("firefoxprofile.dom.webnotifications.serviceworker.enabled", "false");
                System.setProperty("firefoxprofile.dom.webnotifications.enabled", "false");
                System.setProperty("firefoxprofile.geo.enabled", "false");
                //set proxy here
//                ffoptions.setProxy(ClientUtil.createSeleniumProxy(proxy));
            } else if (browser == Browser.IE) {
                _logger.info("Setting Internet Explorer driver...");
                WebDriverManager.iedriver().setup();
                webDriver = new InternetExplorerDriver();
            } else if (browser == Browser.EDGE) {
                _logger.info("Setting Edge driver...");
                WebDriverManager.edgedriver().setup();
                webDriver = new EdgeDriver();
            } else {
                _logger.warn("No driver property found, using default driver (Chrome)");
                WebDriverManager.chromedriver().setup();
                webDriver = new ChromeDriver(ChromeDriverDesktop.getChromeOptions());
            }
        }
        if (webDriver != null) WebDriverRunner.setWebDriver(webDriver);
        else _logger.error("No WebDriver has been set! Check configuration! (or may be you are using IE/Edge)");
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    public void closeWebDriver() {
        if (webDriver != null) webDriver.close();
    }
}
