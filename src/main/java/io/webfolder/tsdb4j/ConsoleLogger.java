package io.webfolder.tsdb4j;

public class ConsoleLogger {

    public enum Level {
        error,
        info,
        trace
    }

    public static Level LEVEL = Level.error;

    public void error(String message) {
        System.err.println("[tsdb4j][error] " + message);
    }

    public void info(String message) {
        if (LEVEL == Level.info || LEVEL == Level.trace) {
            System.out.println("[tsdb4j][info] " + message);
        }
    }

    public void trace(String message) {
        if (LEVEL == Level.trace) {
            System.out.println("[tsdb4j][trace] " + message);
        }
    }
}
