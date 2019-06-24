package org.nowhere_lights.testframework.drivers.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.internal.WrapsDriver;
import org.testng.Reporter;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ByJquery extends By implements Serializable {
    private final String query;

    public ByJquery(String query) {
        checkNotNull(query, "Cannot find elements with a null JQuery expression.");
        this.query = query;
    }

    private static WebDriver getWebDriverFromSearchContext(SearchContext context) {
        if (context instanceof WebDriver) {
            return (WebDriver) context;
        }
        if (context instanceof WrapsDriver) {
            return ((WrapsDriver) context).getWrappedDriver();
        }
        throw new IllegalStateException("Can't access a WebDriver instance from the current search context.");
    }

    private static boolean isJQueryOnThisPage(WebDriver driver) {
        try {
            return (Boolean) ((JavascriptExecutor) driver).executeScript("var result = true; " +
                    "try {  result = (typeof jQuery != 'undefined') ? jQuery.active == 0 : true } " +
                    "catch (e) {}; return result;");
        } catch (Exception e) {
            Reporter.log("Something went wrong during js execution!");
            return true;
        }
    }

    private static void injectJQuery(WebDriver driver) {
        // TODO Load JQuery from a file, inject it into a page via JS.
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        WebDriver driver = getWebDriverFromSearchContext(context);

        if (!isJQueryOnThisPage(driver)) {
            injectJQuery(driver);
        }

        return new ByJavaScript("return $(\"" + query + "\")").findElements(context);
    }

    @Override
    public String toString() {
        return "By.jQuery: \"$(" + query + ")\"";
    }
}


