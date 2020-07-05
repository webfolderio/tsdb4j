package io.webfolder.tsdb4j;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MetaDataCursor implements Iterable<String>, Iterator<String>, AutoCloseable {

    private native String _next(long cursor);

    private native boolean _done(long cursor);
    
    private native void _close(long cursor);

    private long cursor;

    private List<Tag> tags;

    private String series;

    private String metric;

    public MetaDataCursor(long cursor) {
        this.cursor = cursor;
    }

    @Override
    public boolean hasNext() {
        return !_done(cursor);
    }

    @Override
    public String next() {
        return series = _next(cursor);
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

    public String getSeries() {
        return series;
    }

    public String getMetric() {
        if (metric != null) {
            return metric;
        }
        int i;
        for (i = 0; i < series.length() && series.charAt(i) != ' '; i++) {
            // no op
        }
        return metric = series.substring(0, i);
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
            int len = next.length();
            for (int j = 0; j < len; j++) {
                char c = next.charAt(j);
                if (c == '=') {
                    Tag tag = new Tag(next.substring(0, j), next.substring(j + 1, len));
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    @Override
    public String toString() {
        return "MetadataCursor [cursor=" + cursor + "]";
    }
}
