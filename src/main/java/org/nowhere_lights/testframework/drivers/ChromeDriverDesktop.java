package org.nowhere_lights.testframework.drivers;

import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

public class ChromeDriverDesktop {

//    @SuppressWarnings("deprecation")
//    @Override
//    public WebDriver createDriver(DesiredCapabilities desiredCapabilities) {
//        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, getChromeOptions());
//        desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
//        desiredCapabilities.setCapability(CapabilityType.PROXY, ProxyProvider.getSeleniumProxy());
//        return new ChromeDriver(desiredCapabilities);
//    }

    public synchronized static ChromeOptions getChromeOptions() {
        String downloadFilepath = System.getProperty("user.dir") + "/downloadedFiles";
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--disable-extensions");
        chromeOptions.addArguments("--disable-web-security");
//        chromeOptions.addArguments("disable-native-notifications");
        chromeOptions.addArguments("--disable-infobars"); //does not works properly with chrome 65+, replaced with 'enable-automation' option below
//        chromeOptions.addArguments("--start-fullscreen");
//        chromeOptions.addArguments("--window-size=1920,1080");
        chromeOptions.addArguments("--disable-background-networking");
        chromeOptions.addArguments("--disable-popup-blocking");
        chromeOptions.addArguments("--enable-push-api-background-mode");
        chromeOptions.addArguments("--enable-site-settings");
//        chromeOptions.addArguments("--proxy-server=" + ProxyProvider.getSeleniumProxy().getHttpProxy());
//        chromeOptions.addArguments("--ignore-certificate-errors", "--user-data-dir=build/proxy-cache");

        Map<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_setting_values.geolocation", 2);
        chromePrefs.put("profile.default_content_setting_values.notifications", 2);
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("credentials_enable_service", false); //blocks popup with Save Password
        chromePrefs.put("profile.password_manager_enabled", false);
        chromePrefs.put("download.default_directory", downloadFilepath);
        chromePrefs.put("webkit.webprefs.javascript_enabled", 0);

        chromeOptions.setExperimentalOption("prefs", chromePrefs);
        chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        return chromeOptions;
    }

    public static DesiredCapabilities getChromeDesiredCapabilities() {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, getChromeOptions());
        desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        desiredCapabilities.setCapability(CapabilityType.PROXY, ProxyProvider.getSeleniumProxy());
        return desiredCapabilities;
    }

}
