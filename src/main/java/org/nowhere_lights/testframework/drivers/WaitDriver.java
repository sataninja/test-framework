package org.nowhere_lights.testframework.drivers;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class WaitDriver extends WebDriverWait {

    private static Hashtable<WebDriver, WaitDriver> helpersPerDriver = new Hashtable<>();
    private WebDriver driver;

    private WaitDriver(WebDriver driver) {
        super(driver, WebDriverFactory.DEFAULT_TIMEOUT);
        this.driver = driver;
    }

    public static synchronized WaitDriver getInstance(WebDriver driver) {
        if (driver != null && helpersPerDriver.containsKey(driver)) {
            return helpersPerDriver.get(driver);
        } else if (driver != null) {
            WaitDriver wait = new WaitDriver(driver);
//            wait.ignoring(StaleElementReferenceException.class);//Add some annoying exceptions here
            helpersPerDriver.put(driver, wait);
            return wait;
        }
        throw new WebDriverException("No driver was found");
    }

    /**
     * Click using jquery selector without waiting for element to display
     *
     * @param cssSelector used for js script
     */
    public void clickJSWithoutVisibility(String cssSelector) {
        try {
            ((JavascriptExecutor) driver).executeScript("$('" + cssSelector + "').click()");
        } catch (Exception e) {
            Reporter.log("Failed to click usign jquery", true);
        }
    }

    /**
     * Click using js script, waiting for element to display
     *
     * @param element to interact
     */
    public void clickJS(WebElement element) {
        try {
            waitUntilElementLocated(element);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (TimeoutException timeOut) {
            Reporter.log("Can't find an element. Check your selector");
        } catch (NoSuchElementException noElement) {
            Reporter.log("No element found, check your expression");
        } catch (Exception e) {
            Reporter.log("Failed to click using js.click()");
        }
    }

    /**
     * Below method scrolls to element and moves screen a bit down for displaying it (element) on center
     *
     * @param by will be converted to WebElement, or could be replaced with WebElement
     * @return WebElement
     */
    public WebElement scrollToElement(By by) {
        WebElement element = driver.findElement(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-100)");
        return element;
    }

    /**
     * Below method scrolls down a page for 2000 px and tries to reach the bottom of the page
     */
    public void attemptToScrollToBottomOfPage() {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,2000)", "");
    }

    /**
     * Below methods is for waiting some specific actions
     */
    public WebElement waitUntilElementLocated(String xpath) {
        return this.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    public WebElement waitUntilElementLocated(WebElement element) {
        return this.ignoring(StaleElementReferenceException.class).until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitUntilElementClickable(By locator) {
        return this.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean waitForElementToDissapear(By by) {
        try {
            driver.manage().timeouts()
                    .implicitlyWait(1, TimeUnit.SECONDS);

            return new WaitDriver(driver)
                    .ignoring(StaleElementReferenceException.class)
                    .ignoring(NoSuchElementException.class)
                    .until(ExpectedConditions.invisibilityOfElementLocated(by));
        } catch (Exception e) {
            return false;
        } finally {
            driver.manage().timeouts()
                    .implicitlyWait(WebDriverFactory.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        }
    }

    /**
     * Hover over element
     * Please check selector before using hover
     *
     * @param element
     */
    public void hover(WebElement element) {
        Actions action = new Actions(driver);
        action.moveToElement(element).build().perform();
    }

    public List<WebElement> getAllVisibleElements(By by) {
        return until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
    }

    public void clickOnFirstVisibleElement(By by) {
        List<WebElement> list = until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
        for (WebElement element : list) {
            try {
                element.click();
                break;
            } catch (ElementNotVisibleException e) {
                Reporter.log("Moving to next element", true);
            }
        }
    }

    public boolean checkIfElementExists(WebElement element) {
        try {
            driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
            long t = 1;
            new WebDriverWait(driver, t).until(ExpectedConditions.visibilityOf(element));
            return true;
        } catch (WebDriverException e) {
            Reporter.log("Element is not exists: " + element, 5);
            return false;
        } finally {
            driver.manage().timeouts().implicitlyWait(WebDriverFactory.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        }
    }

    /**
     * Returns text of element or null if there are no such element
     *
     * @param element to interact
     * @return text
     */
    public String getTextSafely(WebElement element) {
        try {
            if (this.checkIfElementExists(element)) {
                return waitUntilElementLocated(element).getText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Method for fields with per simbol validation (date input)
     *
     * @param element
     * @param keys
     */
    public void sendKeysOneByOne(WebElement element, String keys) {
        for (char key : keys.toCharArray()) {
            waitUntilElementLocated(element).sendKeys(String.valueOf(key));
        }

    }

    /**
     * @param element input selector
     * @param text    text to set
     */
    public void clearAndType(WebElement element, String text) {
        waitUntilElementLocated(element).clear();
        if (text != null) {
            waitUntilElementLocated(element).sendKeys(text);
        }
    }

    /**
     * Get Random 8 alpha numeric symbols and lorem ipsum string after
     * good for long text fields
     *
     * @param length
     * @return
     */
    public static String getIpsumString(int length) {
        String ipsum = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. " +
                "Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. " +
                "Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. " +
                "Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. " +
                "In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. " +
                "Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. " +
                "Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. " +
                "Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. " +
                "Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. " +
                "Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. " +
                "Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. " +
                "Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem " +
                "neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. " +
                "Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. " +
                "Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. " +
                "Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. " +
                "Sed consequat, leo eget bibendum sodales, augue velit cursus nunc,";
        return (getRandomID(8) + " " + ipsum).substring(0, length);
    }

    /**
     * Return alphanumeric id with length
     *
     * @param length
     * @return
     */
    public static String getRandomID(int length) {
        return new BigInteger(130, new SecureRandom()).toString(32).substring(0, length);
    }

    /**
     * Return sequence of integers with length
     *
     * @param length
     * @return
     */
    public static Integer getRandomIntID(int length) {
        char[] CHARSET_09 = "1234567890".toCharArray();
        Random random = new SecureRandom();
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET_09.length);
            result[i] = CHARSET_09[randomCharIndex];
        }
        return Integer.parseInt(new String(result));
    }

    public static Integer getRandomIntBetween(int low, int high) {
        return new Random().nextInt(high - low) + low;
    }

    /**
     * @param length of string
     * @return string with uppercase in first letter
     */
    public static String randomString(int length) {
        char[] CHARSET_AZ_09 = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        Random random = new SecureRandom();
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET_AZ_09.length);
            result[i] = CHARSET_AZ_09[randomCharIndex];
        }
        result[0] = Character.toUpperCase(result[0]);
        return new String(result);
    }

    /**
     * DANGER!
     */
    public void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
