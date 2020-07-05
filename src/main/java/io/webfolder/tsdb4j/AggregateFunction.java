package io.webfolder.tsdb4j;

public enum AggregateFunction {
    count,
    max,
    min,
    mean,
    sum,
    min_timestamp,
    max_timestamp,
    first,
    last;

    @Override
    public String toString() {
        return name();
    }
}
