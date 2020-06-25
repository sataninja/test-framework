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

    public static synchronized WebDriver createRemoteWebDriver() {
        String url;
        if (System.getProperty("selenoid.url") != null || System.getenv("selenoid.url") != null)
            url = "http://" + PropertiesContext.getInstance().getProperty("selenoid.url") + ":4444";
        else if (PropertiesContext.getInstance().getProperty("selenoid.url").equals("localhost"))
            url = "http://" + PropertiesContext.getInstance().getProperty("selenoid.url") + ":4444/wd/hub";
        else throw new NullPointerException("selenoid.url is NULL!");

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        if (PropertiesContext.getInstance().getProperty("browser") != null)
            desiredCapabilities.setBrowserName(PropertiesContext.getInstance().getProperty("browser"));
        else desiredCapabilities.setBrowserName("chrome");
        if (PropertiesContext.getInstance().getProperty("browser.version") != null)
            desiredCapabilities.setVersion(PropertiesContext.getInstance().getProperty("browser.version"));
        desiredCapabilities.setCapability("enableVNC", true);
        desiredCapabilities.setCapability("enableVideo", false);
//        desiredCapabilities.setCapability(CapabilityType.PROXY, ProxyProvider.getSeleniumProxy());

        try {
            RemoteWebDriver remoteDriver = new RemoteWebDriver(
                    URI.create(url).toURL(),
                    desiredCapabilities
            );
            remoteDriver.manage().window().setSize(new Dimension(1920, 1080));
            remoteDriver.setFileDetector(new LocalFileDetector());
            return remoteDriver;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
