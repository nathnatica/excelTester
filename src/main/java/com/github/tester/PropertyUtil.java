package com.github.tester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {

    final static Logger logger = LoggerFactory.getLogger(PropertyUtil.class);

    private void PropertyUtil() {}
    private static Properties instance = null;

    public static String getProperty(String key) {
        if (instance == null) {
            try {
                loadProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance.getProperty(key);
    }

    private static void loadProperties() throws IOException {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("application.properties");
        prop.load(stream);

        Properties prop2 = new Properties();
        stream = loader.getResourceAsStream("db.properties");
        prop2.load(stream);
               
        prop.putAll(prop2);
                
        instance = prop;
    }
}
