package io.webfolder.tsdb4j;

import static io.webfolder.tsdb4j.Constant.MAX_SERIES_LENGTH;
import static java.util.Collections.emptyList;

import java.time.Instant;
import java.util.List;

import com.grack.nanojson.JsonStringWriter;
import com.grack.nanojson.JsonWriter;

public class Session implements AutoCloseable {

    private long session;

    private native long _open(long db);

    private native void _close(long session);

    private native long _metadata(long session, String query);

    private native long _query(long session, String query);

    private native void _add(long session, long timestamp, String id, double value);

    Session(long db) {
        session = _open(db);
    }

    public void add(long timestamp, String series, double value) {
        if (timestamp < 0) {
            throw new IllegalArgumentException("[timestamp] parameter is required");
        }
        if (series == null) {
            throw new IllegalArgumentException("series");
        }
        if (series.length() > MAX_SERIES_LENGTH) {
            throw new IllegalArgumentException("series length must less than " + MAX_SERIES_LENGTH);
        }
        _add(session, timestamp, series, value);
    }

    public void add(Instant timestamp, String series, double value) {
        add(TimeUtils.toEpoch(timestamp), series, value);
    }

    @Override
    public void close() {
        _close(session);
        session = 0;
    }

    public MetaDataCursor metadata() {
        return queryMetadata("", "", emptyList());
    }

    public MetaDataCursor metadata(String metric) {
        if (metric == null || metric.trim().isEmpty()) {
            throw new IllegalArgumentException("metric");
        }
        return queryMetadata(metric, "", emptyList());
    }

    public MetaDataCursor metadata(String metric, String tag, List<String> tagValues) {
        if (metric == null || metric.trim().isEmpty()) {
            throw new IllegalArgumentException("metric");
        }
        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("tag");
        }
        if (tagValues == null || tagValues.isEmpty()) {
            throw new IllegalArgumentException("tagValues");
        }
        return queryMetadata(metric, tag, tagValues);
    }

    MetaDataCursor queryMetadata(String metric, String tag, List<String> tagValues) {
        JsonStringWriter json = JsonWriter.string().object();
        if (metric == null || metric.trim().isEmpty()) {
            json = json.value("select", "meta:names");
        } else {
            json = json.value("select", "meta:names:" + metric);
        }
        if (tag != null &&
                !tag.trim().isEmpty() &&
                tagValues != null &&
                !tagValues.isEmpty()) {
            json = json.object("where")
                        .array(tag, tagValues)
                    .end();
        }
        String query = json.end().done();
        long peer = _metadata(session,  query);
        if (peer > 0) {
            return new MetaDataCursor(peer);
        } else {
            return null;
        }
    }

    public SimpleCursor query(SelectCriteria criteria) {
        return query((Criteria) criteria);
    }

    public AggregateCursor query(AggregateCriteria criteria) {
        return query((Criteria) criteria);
    }

    public GroupAggregateCursor query(GroupAggregateCriteria criteria) {
        return query((Criteria) criteria);
    }

    public CompoundCursor query(JoinCriteria criteria) {
        return query((Criteria) criteria);
    }

    private Cursor query(Criteria criteria) {
        String json = criteria.toJson();
        if (json == null) {
            return null;
        }
        long peer = _query(session, json);
        if (peer > 0) {
            return new Cursor(peer);
        } else {
            return null;
        }
    }
}
