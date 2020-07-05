package io.webfolder.tsdb4j;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

public interface BaseCursor extends Iterable<String>, Iterator<String>, AutoCloseable {

    String getSeries();

    long getTimestamp();

    Instant getTimestampAsInstant();

    List<Tag> getTags();

    void close();
}
