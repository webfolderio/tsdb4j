package io.webfolder.tsdb4j;

import java.util.List;

public interface GroupAggregateCursor extends CompoundCursor {

    List<AggregateFunction> getAggregateFunctions();
}
