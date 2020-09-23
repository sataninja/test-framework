package org.nowhere_lights.testframework.pages;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;

public abstract class BasePage {

    protected static final Logger _logger = LogManager.getLogger(BasePage.class.getSimpleName());
    protected static PropertiesContext context = PropertiesContext.getInstance();

}
