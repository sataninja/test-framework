package org.nowhere_lights.testframework.drivers.utils;


import org.testng.Reporter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesContext {

    private static final String CONFIG_PROPERTIES = "config";
    private static final String ENV_PROPERTIES = "env";
    private static final String PAGES_PROPERTIES = "pages";
    private static final String EMAIL_PROPERTIES = "email";
    private static final String USER_PROPERTIES = "user";
    private static final String ALLURE_PROPERTIES = "allure";
    private static final String BROWSERSTACK_PROPERTIES = "browserstack";
    private static PropertiesContext instance = new PropertiesContext();
    private Properties generalMap = new Properties();
    private Properties configMap = new Properties();
    private Properties emailMap = new Properties();
    private Properties pagesMap = new Properties();
    private Properties gradleMap = new Properties();

    private PropertiesContext() {
        init();
    }

    public static PropertiesContext getInstance() {
        if (instance == null) {
            instance = new PropertiesContext();
        }
        return instance;
    }

    public void init() {
        loadPropertiesFromClasspath(configMap, CONFIG_PROPERTIES);
        loadPropertiesFromClasspath(configMap, ENV_PROPERTIES);
        loadPropertiesFromClasspath(configMap, USER_PROPERTIES);
        loadPropertiesFromClasspath(configMap, ALLURE_PROPERTIES);
        loadPropertiesFromClasspath(configMap, PAGES_PROPERTIES);
        loadPropertiesFromClasspath(configMap, EMAIL_PROPERTIES);
        loadPropertiesFromClasspath(configMap, BROWSERSTACK_PROPERTIES);
//        loadGradleProperties(gradleMap);

        generalMap.putAll(configMap);
        generalMap.putAll(pagesMap);
        if (System.getProperty("browser") != null) {
            generalMap.setProperty("browser", System.getProperty("browser"));
        }
        if (System.getProperty("env") != null) {
            generalMap.setProperty("env", System.getProperty("env"));
        }
        if (System.getProperty("urltest") != null) {
            generalMap.setProperty("urltest", System.getProperty("urltest"));
        }
        if (System.getProperty("urlstg") != null) {
            generalMap.setProperty("urlstg", System.getProperty("urlstg"));
        }
        //email props
        if (System.getProperty("email.address") != null) {
            generalMap.setProperty("email.address", System.getProperty("email.address"));
        }
        if (System.getProperty("email.password") != null) {
            generalMap.setProperty("email.password", System.getProperty("email.password"));
        }
        //user test run
        if (System.getProperty("user.email.admin") != null) {
            generalMap.setProperty("user.email.admin", System.getProperty("user.email.admin"));
        }
        if (System.getProperty("user.password.admin") != null) {
            generalMap.setProperty("user.password.admin", System.getProperty("user.password.admin"));
        }
        if (System.getProperty("user.email.member") != null) {
            generalMap.setProperty("user.email.member", System.getProperty("user.email.member"));
        }
        if (System.getProperty("user.password.member") != null) {
            generalMap.setProperty("user.password.member", System.getProperty("user.password.member"));
        }
    }

    public String getProperty(String key) {
        String result = (String) generalMap.get(key);
        if (result != null) {
            return result;
        } else {
            Reporter.log("Property " + key + " was not found (null)");
            return null;
        }
    }

    public void setProperty(String key, String value) {
        if (value == null)
            Reporter.log("Value is null");
        if (key != null)
            generalMap.setProperty(key, value);
        else Reporter.log("Key is null");
    }

    public void clear() {
        generalMap.clear();
    }

    private String getFullFileName(String fileName) {
        return fileName + ".properties";
    }

    private void loadGradleProperties(Properties props) {
        try {
            String fileName = "gradle.properties";
            String path = System.getProperty("user.dir");
            Reader resource = new FileReader(new File(path + "/" + fileName));
            props.load(resource);
        } catch (IOException e) {
            Reporter.log("Missing or corrupt property file", true);
        }
    }

    private void loadPropertiesFromClasspath(Properties props, String fileName) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(getFullFileName(fileName));

            if (resourceAsStream != null) {
                props.load(resourceAsStream);
            }
        } catch (IOException e) {
            Reporter.log("Missing or corrupt property file", true);
        }
    }

    public Map<String, String> getPagesNames(Properties properties, String filename) {
        Map<String, String> pages = new HashMap<>();
//        Properties pagesFromProperties = new Properties();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(getFullFileName(filename));

            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
                for (String name : properties.stringPropertyNames())
                    pages.put(name, properties.getProperty(name));
            }
        } catch (IOException e) {
            Reporter.log("Missing or corrupt property file", true);
        }
        return pages;
    }

    public Properties getPagesMap() {
        return pagesMap;
    }

}
