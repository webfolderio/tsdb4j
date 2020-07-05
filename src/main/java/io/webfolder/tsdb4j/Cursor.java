package io.webfolder.tsdb4j;

import static io.webfolder.tsdb4j.TimeUtils.fromEpoch;
import static java.lang.Double.NaN;
import static java.util.Collections.emptyList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Cursor implements Iterable<String>,
                               Iterator<String>,
                               AutoCloseable,
                               SimpleCursor,
                               AggregateCursor,
                               CompoundCursor,
                               GroupAggregateCursor {

    private long cursor;

    private native String _next(long cursor);

    private native boolean _done(long cursor);

    private native void _close(long cursor);

    private double[] values;

    private String series;

    private long timestamp;

    private String metric;

    private List<String> metrics;

    private List<Tag> tags;

    private List<AggregateFunction> aggregateFunctions;

    private AggregateFunction aggregateFunction;

    public Cursor(long cursor) {
        this.cursor = cursor;
    }

    @Override
    public boolean hasNext() {
        return !_done(cursor);
    }

    @Override
    public String next() {
        series = _next(cursor);
        metric = null;
        tags = null;
        metrics = null;
        aggregateFunctions = null;
        return series;
    }

    @Override
    public void close() {
        _close(cursor);
        cursor = 0;
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Instant getTimestampAsInstant() {
        return fromEpoch(timestamp);
    }

    public double getValue() {
        return values != null ? values[0] : NaN;
    }

    public double[] getValues() {
        return values;
    }

    public String getSeries() {
        return series;
    }

    public String getMetric() {
        if (metric != null) {
            return metric;
        }
        int i;
        int startAggregate = 0;
        int startTag = 0;
        for (i = 0; i < series.length(); i++) {
            char c = series.charAt(i);
            if (c == ' ') {
                startTag = i;
                break;
            }
            if (c == '|') {
                break;
            }
            if (c == ':') {
                startAggregate = i;
            }
        }
        if (startAggregate > 0) {
            aggregateFunction = AggregateFunction.valueOf(series.substring(startAggregate + 1, i));
        }
        if (startAggregate == 0 && startTag == 0) {
            startTag = i;
        }
        return metric = series.substring(0, startAggregate > 0 ? startAggregate : startTag);
    }

    public List<String> getMetrics() {
        if (series == null) {
            return emptyList();
        }
        if (metrics != null) {
            return metrics;
        }
        int len = values != null ? values.length : 0;
        metrics = new ArrayList<>(len);
        aggregateFunctions = new ArrayList<>(len);
        int start = 0;
        int aggregateStart = 0;
        for (int i = 0; i < series.length(); i++) {
            char c = series.charAt(i);
            if (c == ':') {
                aggregateStart = i;
                continue;
            } else if (c == '|' || c == ' ') {
                if (aggregateStart > 0) {
                    String aggregate = series.substring(aggregateStart + 1, i);
                    AggregateFunction aggregateFunction = AggregateFunction.valueOf(aggregate);
                    aggregateFunctions.add(aggregateFunction);
                }
                String next = series.substring(start, aggregateStart > 0 ? aggregateStart : i);
                metrics.add(next);
                if (c == ' ') {
                    break;
                }
                start = i + 1;
            }
        }
        return metrics;
    }

    public double getValue(AggregateFunction aggregate) {
        if (aggregateFunctions != null) {
            int index = aggregateFunctions.indexOf(aggregate);
            if (index < 0) {
                return NaN;
            }
            return index < values.length ? values[index] : NaN;
        }
        return NaN;
    }

    public List<Tag> getTags() {
        if (series == null) {
            return emptyList();
        }
        if (tags != null) {
            return tags;
        }
        String[] list = series.split(" ");
        tags = list.length <= 1 ? emptyList() : new ArrayList<>(list.length);
        for (int i = 0; i < list.length; i++) {
            String next = list[i];
            int tagLen = next.length();
            for (int j = 0; j < tagLen; j++) {
                char c = next.charAt(j);
                if (c == '=') {
                    Tag tag = new Tag(next.substring(0, j), next.substring(j + 1, tagLen));
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    public List<AggregateFunction> getAggregateFunctions() {
        if (series == null) {
            return emptyList();
        }
        if (aggregateFunctions != null) {
            return aggregateFunctions;
        }
        getMetrics();
        if (aggregateFunctions == null) {
            aggregateFunctions = emptyList();
        }
        return aggregateFunctions;
    }

    public AggregateFunction getAggregateFunction() {
        return aggregateFunction;
    }

    @Override
    public String toString() {
        return "Cursor [cursor=" + cursor + "]";
    }
}
