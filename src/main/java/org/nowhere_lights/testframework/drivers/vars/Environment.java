package org.nowhere_lights.testframework.drivers.vars;

public enum Environment {
    STAGING("staging"),
    TESTING("testing");

    private String environment;

    Environment(String environment) {
        this.environment = environment;
    }

    public String getValue() {
        return environment;
    }

    @Override
    public String toString() {
        return environment;
    }

    public static Environment toEnum(String value) {
        for (Environment e : values()) {
            if (e.getValue().equalsIgnoreCase(value)) {
                return e;
            }
        }
        return TESTING;
    }

}
