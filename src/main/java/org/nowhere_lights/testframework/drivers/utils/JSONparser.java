package org.nowhere_lights.testframework.drivers.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSONparser {
    public static boolean getPrivilege(String nodeName, String fileName, String fieldName) {
        String path = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedReader br =
                     new BufferedReader(new FileReader(path + "/src/main/resources/json/" + fileName))) {
            JsonNode node = mapper.readTree(br);
            return node.findPath(nodeName).get(fieldName).asBoolean();
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to load JSON");
        }
        return false;
    }
}
