package com.github.tester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

public class BeanConfigurator {

    final static Logger logger = LoggerFactory.getLogger(BeanConfigurator.class);
    static ApplicationContext context;
    
    public static Object getBean(String name) {
        return context.getBean(name); 
    }
    
    static {
        System.setProperty("spring.profiles.active", PropertyUtil.getProperty("env"));
        context = new ClassPathXmlApplicationContext("springBeans.xml");
        logger.info("Active spring profiles : {}", Arrays.toString(context.getEnvironment().getActiveProfiles()));
    }
}
