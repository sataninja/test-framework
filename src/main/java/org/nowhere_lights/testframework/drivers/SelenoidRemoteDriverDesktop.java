package org.nowhere_lights.testframework.drivers;

import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;

public class SelenoidRemoteDriverDesktop {

    public static WebDriver createRemoteWebDriver() throws MalformedURLException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        if (PropertiesContext.getInstance().getProperty("browser") != null)
            desiredCapabilities.setBrowserName(PropertiesContext.getInstance().getProperty("browser"));
        else desiredCapabilities.setBrowserName("chrome");
        if (PropertiesContext.getInstance().getProperty("browser.version") != null)
            desiredCapabilities.setVersion(PropertiesContext.getInstance().getProperty("browser.version"));
        desiredCapabilities.setCapability("enableVNC", true);
        desiredCapabilities.setCapability("enableVideo", false);
//        desiredCapabilities.setCapability(CapabilityType.PROXY, ProxyProvider.getSeleniumProxy());

        RemoteWebDriver remoteDriver = null;
        remoteDriver = new RemoteWebDriver(
                URI.create("http://" + PropertiesContext.getInstance().getProperty("selenoid.url") + ":4444/wd/hub").toURL(),
                desiredCapabilities
        );
        remoteDriver.manage().window().setSize(new Dimension(1920, 1080));
        remoteDriver.setFileDetector(new LocalFileDetector());

        return remoteDriver;
    }
}
