package io.webfolder.tsdb4j;

import static io.webfolder.tsdb4j.TimeUtils.toEpoch;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.List;

import com.grack.nanojson.JsonStringWriter;
import com.grack.nanojson.JsonWriter;

/**
 * This query can be used to calculate aggregates over time-series.
 * The query returns only one result for every time-series.
 *
 * The aggregate query object computes aggregate only for values inside the specified time-range.
 * 
 * If {@link Builder#from(long)} is less than {@link Builder#to(long)} the time-series data points
 * will be returned in ascending order (from old to new).
 * 
 * If {@link Builder#from(long)} is greater than {@link Builder#to(long)} then the time-series data points
 * will be returned in descending order (from recent to old).
 */
public class AggregateCriteria implements Criteria {

    private String aggregateMetricName;

    private AggregateFunction aggregateFunction;

    private long from;

    private long to;

    private List<String> groupByTag;

    private List<String> pivotByTag;

    private String tagName;

    private List<String> tagValues;

    private String criteria;

    private AggregateCriteria() {
        // no op
    }

    public static class Builder {

        private AggregateCriteria criteria = new AggregateCriteria();

        private Builder() {
            // no op
        }

        /**
         * Metric name and aggregation function
         * 
         * This method is required to create an aggregate query.
         * 
         * At least one metric-name and {@link AggregateFunction} pair should be provided.
         */
        public Builder aggregate(String aggregateMetricName, AggregateFunction aggregateFunction) {
            criteria.aggregateMetricName = aggregateMetricName;
            criteria.aggregateFunction = aggregateFunction;
            return this;
        }

        /**
         * Time range
         * 
         * Range field denotes the time interval that query should fetch.
         */
        public Builder from(long from) {
            criteria.from = from;
            return this;
        }

        /**
         * Time range
         * 
         * Range field denotes the time interval that query should fetch.
         */
        public Builder from(Instant from) {
            criteria.from = toEpoch(from);
            return this;
        }

        /**
         * Time range
         * 
         * Range field denotes the time interval that query should fetch.
         */
        public Builder to(long to) {
            criteria.to = to;
            return this;
        }

        /**
         * Time range
         * 
         * Range field denotes the time interval that query should fetch.
         */
        public Builder to(Instant to) {
            criteria.to = toEpoch(to);
            return this;
        }

        /**
         * Tag filter
         * 
         * Where field is used to limit number of series returned by the query.
         * You can specify many tags in one where field.
         * This data in conjunction with metric name (or names) will form be used to search series inside the index.
         * 
         * Note that the timestamps and values are the same. Only series names are different.
         */
        public Builder where(String tagName, List<String> tagValues) {
            criteria.tagName = tagName;
            criteria.tagValues = unmodifiableList(tagValues.stream().distinct().collect(toList()));
            return this;
        }

        /**
         * Merge series by tag
         * 
         * This paramaeter query processor to remove listed tags from series name.
         * After that all series that have matching tags are considered equal and merged together. 
         * 
         * This parameter is the opposite of {{@link #pivotByTag(List)}}.
         */
        public Builder groupByTag(List<String> groupByTag) {
            criteria.groupByTag = unmodifiableList(groupByTag.stream().distinct().collect(toList()));
            return this;
        }

        /**
         * Merge series by tag
         * 
         * In a nutshell, this methods tells query processor to remove all tags from series name except the ones that was listed.
         * After that all series that have matching tags are considered equal and merged together. 
         * 
         * For instance, if pivot-by-tag field was used to specify a single tag name,
         * all series with this tag with the same value will collapse into one.
         * All data points from that series will be joined together.
         * The resulting time-series will contain all data-points from the original series.
         * The series name will contain only the specified tag. It's also possible to specify more than one tag. 
         */
        public Builder pivotByTag(List<String> pivotByTag) {
            criteria.pivotByTag = unmodifiableList(pivotByTag.stream().distinct().collect(toList()));
            return this;
        }

        public AggregateCriteria build() {
            if (criteria.aggregateMetricName == null || criteria.aggregateMetricName.trim().isEmpty()) {
                throw new IllegalStateException("[aggregateMetricName] parameter is required to make a aggregate query");
            }
            if (criteria.aggregateFunction == null) {
                throw new IllegalStateException("[aggregateFunction] parameter is required to make a aggregate query");
            }
            return criteria;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toJson() {
        if (criteria != null) {
            return criteria;
        }
        JsonStringWriter json = JsonWriter.string();
        JsonStringWriter jroot = json.object();
        if (aggregateMetricName != null && aggregateFunction != null) {
            jroot.object("aggregate")
                        .value(aggregateMetricName, aggregateFunction.name())
                    .end();
        }
        if (from > 0 && to > 0) {
            jroot.object("range")
                    .value("from", from)
                    .value("to", to)
                .end();
        } else if (from > 0 && to <= 0) {
            jroot.object("range")
                    .value("from", from)
                .end();
        }
        if (groupByTag != null && !groupByTag.isEmpty()) {
            jroot.array("group-by-tag", groupByTag);
        }
        if (pivotByTag != null && !pivotByTag.isEmpty()) {
            jroot.array("pivot-by-tag", pivotByTag);
        }
        if (tagName != null && tagValues != null) {
            jroot.object("where")
                        .array(tagName, tagValues)
                    .end();
        }
        jroot.end();
        return criteria = json.done();
    }
    
    public String getAggregateMetricName() {
        return aggregateMetricName;
    }

    public AggregateFunction getAggregateFunction() {
        return aggregateFunction;
    }

    @Override
    public long getFrom() {
        return from;
    }

    @Override
    public long getTo() {
        return to;
    }

    public List<String> getGroupByTag() {
        return groupByTag;
    }

    public List<String> getPivotByTag() {
        return pivotByTag;
    }

    public String getTagName() {
        return tagName;
    }

    public List<String> getTagValues() {
        return tagValues;
    }

    public String getCriteria() {
        return criteria;
    }

    @Override
    public String toString() {
        return "AggregateCriteria [criteria=" + criteria + "]";
    }
}
