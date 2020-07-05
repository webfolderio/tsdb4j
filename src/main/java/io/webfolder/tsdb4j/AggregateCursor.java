package io.webfolder.tsdb4j;

public interface AggregateCursor extends SimpleCursor {

    AggregateFunction getAggregateFunction();
}
