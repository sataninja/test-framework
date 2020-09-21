package org.nowhere_lights.testframework.pages;


import org.nowhere_lights.testframework.drivers.WaitDriver;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.nowhere_lights.testframework.drivers.utils.Wrappers;
import org.openqa.selenium.WebDriver;

public abstract class BasePage extends Wrappers implements AbstractUiElement {

    private static PropertiesContext context = PropertiesContext.getInstance();
//    protected WebDriver driver;
//    protected WaitDriver waitHelper;
//
//    public BasePage(WebDriver driver) {
//        this.driver = driver;
//        this.waitHelper = WaitDriver.getInstance(driver);
//    }
//
//    @Override
//    public WebDriver getDriver() {
//        return driver;
//    }
}
