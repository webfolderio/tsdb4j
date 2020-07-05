package io.webfolder.tsdb4j;

import java.util.List;

public interface CompoundCursor extends BaseCursor {

    List<String> getMetrics();

    double[] getValues();

    double getValue(AggregateFunction aggregate);
}
