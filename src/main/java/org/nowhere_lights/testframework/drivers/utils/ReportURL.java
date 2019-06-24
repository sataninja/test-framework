package org.nowhere_lights.testframework.drivers.utils;

import org.nowhere_lights.testframework.testutils.SoftAssert;

import java.util.Map;

/**
 * Create url suitable for team city usage
 */

public class ReportURL {

    // repository/download/{env.BUILD_ID}/{env.BUILD_NUMBER}/{project_dir}/{report_dir}/{snapshots_path}
    private static final String TC_RESOURCE_PATTERN = "/repository/download/%s/%s/%s/%s/%s";
    // {project_dir}/{report_dir}/{snapshots_path}
    private static final String LOCAL_RESOURCE_PATTERN = "%s/%s/%s";

    private static final String TC_BUILD_ID_PARAM = "BUILD_ID";
    private static final String TC_BUILD_NUMBER_PARAM = "BUILD_NUMBER";

    private ReportURL() {
    }

    public static String build(String relativeFilePath) {
        String projectDir = PropertiesContext.getInstance().getProperty(SoftAssert.PROJECT_DIRECTORY);
        String reportDir = PropertiesContext.getInstance().getProperty(SoftAssert.SNAPSHOT_DIRECTORY);
        Map<String, String> envVars = System.getenv();

        if (envVars.containsKey(TC_BUILD_ID_PARAM) && envVars.containsKey(TC_BUILD_NUMBER_PARAM)) {
            return String.format(TC_RESOURCE_PATTERN,
                    envVars.get(TC_BUILD_ID_PARAM),
                    envVars.get(TC_BUILD_NUMBER_PARAM),
                    projectDir,
                    reportDir,
                    relativeFilePath);
        } else {
            return String.format(LOCAL_RESOURCE_PATTERN,
                    projectDir,
                    reportDir,
                    relativeFilePath);
        }
    }
}
