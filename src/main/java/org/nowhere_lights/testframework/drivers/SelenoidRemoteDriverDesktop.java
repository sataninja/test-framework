package org.nowhere_lights.testframework.drivers;

import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;

public class SelenoidRemoteDriverDesktop {

    public static synchronized WebDriver createRemoteWebDriver() {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(PropertiesContext.getInstance().getProperty("browser"));
//        desiredCapabilities.setVersion("76.0");
        desiredCapabilities.setCapability("enableVNC", true);
        desiredCapabilities.setCapability("enableVideo", false);
//        desiredCapabilities.setCapability(CapabilityType.PROXY, ProxyProvider.getSeleniumProxy());

        try {
            RemoteWebDriver remoteDriver = new RemoteWebDriver(
                    URI.create("http://" + PropertiesContext.getInstance().getProperty("selenoid.url") + ":4444/wd/hub").toURL(),
                    desiredCapabilities
            );
            remoteDriver.manage().window().setSize(new Dimension(1920, 1080));
            return remoteDriver;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
