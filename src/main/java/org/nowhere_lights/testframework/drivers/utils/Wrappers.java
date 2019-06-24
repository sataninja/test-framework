package org.nowhere_lights.testframework.drivers.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class Wrappers {

    public abstract WebDriver getDriver();

    protected By findById(String id) {
        return By.id(id);
    }

    protected By findByClass(String className) {
        return By.className(className);
    }


    protected By findByCss(String cssSelector) {
        return By.cssSelector(cssSelector);
    }

    protected By findByXPath(String xpath) {
        return By.xpath(xpath);
    }

    protected void type(By field, String value) {
        getDriver().findElement(field).sendKeys(value);
    }

    protected void clearAndType(By field, String value) {
        getDriver().findElement(field).clear();
        getDriver().findElement(field).sendKeys(value);
    }

    protected void click(By by) {
        getDriver().findElement(by).click();
    }

    protected void click(WebElement element) {
        element.click();
    }

    protected void pressEnter(By element) {
        getDriver().findElement(element).sendKeys(Keys.ENTER);
    }

    private Boolean isSelected(By locator) {
        return getDriver().findElement(locator).isSelected();
    }

    protected void interactWithCheckbox(By locator, Boolean condition) {
        WebElement checkbox = getDriver().findElement(locator);
        if (condition) {
            if (!isSelected(locator))
                getDriver().findElement(locator).click();
        } else {
            if (isSelected(locator))
                getDriver().findElement(locator).click();
        }
    }

    protected String getText(By element) {
        return getDriver().findElement(element).getText();
    }
}
