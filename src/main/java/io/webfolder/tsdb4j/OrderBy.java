package io.webfolder.tsdb4j;

public enum OrderBy {
    series,
    time;

    @Override
    public String toString() {
        return name();
    }
}
