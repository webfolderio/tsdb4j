package io.webfolder.tsdb4j;

public class Logger {

    public static ConsoleLogger DEFAULT_LOGGER = new ConsoleLogger();

    public void error(String message) {
        DEFAULT_LOGGER.error(message);
    }

    public void info(String message) {
        DEFAULT_LOGGER.info(message);
    }

    public void trace(String message) {
        DEFAULT_LOGGER.trace(message);
    }
}
