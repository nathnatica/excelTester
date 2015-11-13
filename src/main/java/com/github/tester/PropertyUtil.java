package com.github.tester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
        logger.info("load db.properties file");
        InputStream stream = loader.getResourceAsStream("db.properties");
        prop.load(stream);
        stream.close();
        
        Properties prop2 = new Properties();
        logger.info("load application.properties file");
        stream = loader.getResourceAsStream("application.properties");
        prop2.load(stream);
        prop.putAll(prop2);
        stream.close();

        File f = new File("C:\\application.properties");
        if (f.canRead()) {
            logger.info("load external application.properties");
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            prop.load(is);
            is.close();
        }

        instance = prop;
    }
}
