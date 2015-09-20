package com.system.util;

import org.apache.log4j.*;
/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class SingleLogger {
    
    private static SingleLogger singleLogger;
    private Logger logger;
    private static final String LOGGER_NAME = "systemLogger";
    private static final String FILE_NAME = "ModelCheckingSystem.log";
    private static final String PATTERN_LAYOUT = "%d %-5p [%c{1}] %m%n";
    
    private SingleLogger() {
        configure();
    }
    
    private void configure() {
        FileAppender fileAppender = new FileAppender();
        fileAppender.setName(LOGGER_NAME);
        fileAppender.setFile(FILE_NAME);
        fileAppender.setLayout(new PatternLayout(PATTERN_LAYOUT));
        fileAppender.setThreshold(Level.ALL);
        fileAppender.setAppend(true);
        fileAppender.activateOptions();
        BasicConfigurator.configure(fileAppender);
        logger = Logger.getLogger(LOGGER_NAME);
    }
    
    /**
     *
     * @return instance of log4j logger
     */
    public static Logger getLogger() {
        if (singleLogger == null) {
            singleLogger = new SingleLogger();
        }
        return singleLogger.logger;
    }
}
