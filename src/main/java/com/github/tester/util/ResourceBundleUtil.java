package com.github.tester.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceBundleUtil {

    final static Logger logger = LoggerFactory.getLogger(ResourceBundleUtil.class);

    private void ResourceBundleUtil() {}

    private static ResourceBundle instance = null;
    
    public static String get(String key, boolean regexReplace) {
        if (instance == null) {
            init(null);
        }

        String returnValue = instance.getString(key);
        if (regexReplace) {
            while (isReplacable(returnValue)) {
                returnValue = getReplacedValue(returnValue);
            }
        }
        return returnValue;
    }
    
    public static String get(String key) {
        return get(key, true);
    }
    
    private static boolean isReplacable(String input) {
        Matcher m = Pattern.compile("\\{[0-9a-zA-Z\\.]+\\}").matcher(input);
        return m.find();
    }
    
    private static String getReplacedValue(String value) {
        int idx = 0;
        Matcher m = Pattern.compile("\\{[0-9a-zA-Z\\.]+\\}").matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            sb.append(value.substring(idx, m.start()));
            idx = m.end();
            String sub = get(m.group().replace("{","").replace("}",""), true);
            sb.append(sub);
        }
        sb.append(value.substring(idx));
        return sb.toString();
    }

    private static void loadProperties(String file) throws IOException {
        File f = new File(".");
        File f2 = new File("C:\\");
        URL[] urls = {f.toURI().toURL(), f2.toURI().toURL()};
        ClassLoader loader = new URLClassLoader(urls);

        instance = ResourceBundle.getBundle(file, Locale.ROOT, loader, new UTF8Control());
    }
    
    public static void init(String file) {
        try {
            if (StringUtils.isNotEmpty(file)) {
                loadProperties(file);
            } else {
                loadProperties("application");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
