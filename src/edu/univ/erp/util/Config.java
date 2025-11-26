package edu.univ.erp.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();
    static {
        try (FileInputStream fis = new FileInputStream("config/app.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load config/app.properties: " + e.getMessage());
        }
    }
    public static String get(String key) { return props.getProperty(key, ""); }
}
