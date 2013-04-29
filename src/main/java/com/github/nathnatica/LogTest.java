package com.github.nathnatica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

    final static Logger logger = LoggerFactory.getLogger(LogTest.class);
    public static void main(String[] args) {
        logger.debug("first log {} {} " , "a", "b");
    }
}
