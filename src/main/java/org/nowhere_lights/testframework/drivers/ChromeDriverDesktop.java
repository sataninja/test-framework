package org.nowhere_lights.testframework.drivers;

import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

public class ChromeDriverDesktop implements WebDriverProvider {

    @SuppressWarnings("deprecation")
    @Override
    public WebDriver createDriver(DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, getChromeOptions());
        desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        return new ChromeDriver(desiredCapabilities);
    }

    private static ChromeOptions getChromeOptions() {
        String downloadFilepath = System.getProperty("user.dir") + "/downloadedFiles";
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--disable-extensions");
//        chromeOptions.addArguments("disable-native-notifications");
        chromeOptions.addArguments("--disable-infobars");
        chromeOptions.addArguments("--start-fullscreen");
        chromeOptions.addArguments("--disable-background-networking");
        chromeOptions.addArguments("--enable-push-api-background-mode");
        chromeOptions.addArguments("--enable-site-settings");

        Map<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_setting_values.geolocation", 2);
        chromePrefs.put("profile.default_content_setting_values.notifications", 2);

        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadFilepath);
        chromePrefs.put("webkit.webprefs.javascript_enabled", 0);

        chromeOptions.setExperimentalOption("prefs", chromePrefs);
        return chromeOptions;
    }

}
