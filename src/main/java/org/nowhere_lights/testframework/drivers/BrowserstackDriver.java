package org.nowhere_lights.testframework.drivers;

import com.browserstack.local.Local;
import com.codeborne.selenide.WebDriverRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BrowserstackDriver {

    private static final Logger _logger = LogManager.getLogger(BrowserstackDriver.class.getSimpleName());
    private RemoteWebDriver remoteWebDriver;
    private Local local;
    private String sessionId;

    public void createBrowserStackDriver() throws Exception {
        String username = System.getenv("BROWSERSTACK_USERNAME");
        String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
        String os = PropertiesContext.getInstance().getProperty("bsos");
        String osVersion = PropertiesContext.getInstance().getProperty("bsos_version");
        String browser = PropertiesContext.getInstance().getProperty("bsbrowser");
        String browserVersion = PropertiesContext.getInstance().getProperty("bsbrowser_version");
        String resolution = PropertiesContext.getInstance().getProperty("bsresolution");
        String project = PropertiesContext.getInstance().getProperty("bsproject");
        String build = PropertiesContext.getInstance().getProperty("bsbuild");
        String name = PropertiesContext.getInstance().getProperty("bsname");

        if (System.getenv("BSOS") != null)
            PropertiesContext.getInstance().setProperty("bsos", System.getenv("BSOS"));
        if (System.getenv("BSOS_VERSION") != null)
            PropertiesContext.getInstance().setProperty("bsos_version", System.getenv("BSOS_VERSION"));
        if (System.getenv("BSBROWSER") != null)
            PropertiesContext.getInstance().setProperty("bsbrowser", System.getenv("BSBROWSER"));
        if (System.getenv("BSBROWSER_VERSION") != null)
            PropertiesContext.getInstance().setProperty("bsbrowser_version", System.getenv("BSBROWSER_VERSION"));
        if (System.getenv("BSRESOLUTION") !=null)
            PropertiesContext.getInstance().setProperty("bsresolution", System.getenv("BSRESOLUTION"));
        if (System.getenv("BSPROJECT") != null)
            PropertiesContext.getInstance().setProperty("bsproject", System.getenv("BSPROJECT"));
        if (System.getenv("BSBUILD") != null)
            PropertiesContext.getInstance().setProperty("bsbuild", System.getenv("BSBUILD"));

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("os", os);
        capabilities.setCapability("os_version", osVersion);
        capabilities.setCapability("browser", browser);
        capabilities.setCapability("browser_version", browserVersion);
        capabilities.setCapability("resolution", resolution);
        capabilities.setCapability("project", project);
        capabilities.setCapability("build", build);
        capabilities.setCapability("name", name);
        capabilities.setCapability("browserstack.debug", "true");
        capabilities.setCapability("browserstack.local", "true");
        capabilities.setCapability("browserstack.networkLogs", "true");
        capabilities.setCapability("browserstack.console", "errors");
        capabilities.setCapability("browserstack.ie.enablePopups", "true");
        capabilities.setCapability("credentials_enable_service", "false");

        //setting local
        if (capabilities.getCapability("browserstack.local") != null && capabilities.getCapability("browserstack.local") == "true") {
            local = new Local();
            if (local.isRunning())
                local.stop();
            Map<String, String> options = new HashMap<>();
            options.put("key", accessKey);
            options.put("v", "3");
            local.start(options);
        }
        try {
            remoteWebDriver = new RemoteWebDriver(
                    new URL("http://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub"), capabilities);
            remoteWebDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            sessionId = remoteWebDriver.getSessionId().toString();
            WebDriverRunner.setWebDriver(remoteWebDriver);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        _logger.info("Starting BrowserStack session!");
        _logger.info("Using " + os + " environment");
        _logger.info("Using " + osVersion + " OS version");
        _logger.info("Using " + browser + " browser");
        _logger.info("Using " + browserVersion + " browser version");
        _logger.info("Session (id): " + sessionId);
    }

    public void close() throws Exception {
        if (remoteWebDriver != null)
            remoteWebDriver.quit();
        if (local != null)
            local.stop();
    }
}
