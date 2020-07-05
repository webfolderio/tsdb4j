package io.webfolder.tsdb4j;

public interface SimpleCursor extends BaseCursor {

    String getMetric();

    double getValue();
}
