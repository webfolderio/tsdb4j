package io.webfolder.tsdb4j;

import static io.webfolder.tsdb4j.TimeUtils.toEpoch;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.grack.nanojson.JsonStringWriter;
import com.grack.nanojson.JsonWriter;

/**
 * This query is used to downsample time-series data.
 * It divides all data-points into a series of equally sized bins and
 * computes a single value for every bin if the bin is not empty.
 * 
 * If the bin is empty it doesn't produce any value.
 * The same aggregation functions that can be used with aggregate query can be used with group-aggregate.
 * The difference between the {@link AggregateCriteria} and {@link GroupAggregateCriteria} queries is that the aggregate
 * produces only one value for every series but the {@link GroupAggregateCriteria} can produce a time-series with fixed step.
 * Also, more than one aggregation function can be used with group-aggregate query.
 * 
 * Both {@link Builder#from(long)} and {@link Builder#to(long)} fields are mandatory for the select criteria.
 * 
 * If {@link Builder#from(long)} is less than {@link Builder#to(long)} the time-series data points
 * will be returned in ascending order (from old to new).
 * 
 * If {@link Builder#from(long)} is greater than {@link Builder#to(long)} then the time-series data points
 * will be returned in descending order (from recent to old).
 */
public class GroupAggregateCriteria implements Criteria {

    private List<String> metrics;

    private Duration step;

    private EnumSet<AggregateFunction> functions;

    private long from;

    private long to;

    private String tagName;

    private List<String> tagValues;

    private List<String> groupByTag;

    private List<String> pivotByTag;

    private OrderBy orderBy;

    private String criteria;

    private Filter filter1;

    private Filter filter2;

    private int limit = -1;

    private int offset = -1;

    public static class Builder {

        private GroupAggregateCriteria criteria = new GroupAggregateCriteria();

        private Builder() {
            // no op
        }

        /**
         * Query specific parameters
         * 
         * These fields are required to make a group-aggregate query.
         */
        public Builder groupAggregate(List<String> metrics, Duration step, EnumSet<AggregateFunction> functions) {
            criteria.metrics = unmodifiableList(metrics.stream().distinct().collect(toList()));
            criteria.step = step;
            criteria.functions = functions;
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

        /**
         * 
         * Order of the data-points in the result set
         * 
         * This method can be used to control the order of the data-points in the query output.
         * If order-by is {@link OrderBy#series} the results will be ordered by series name first and then by timestamp.
         * If order-by is {@link OrderBy#time} then data points will be ordered by timestamp first and then by series name.
         */
        public Builder orderBy(OrderBy orderBy) {
            criteria.orderBy = orderBy;
            return this;
        }

        /**
         * Value based filtering
         * 
         * Filter field can be used to filter data-points by value.
         * 
         * The use of filter field can speed up query execution if the number of returned values is small.
         * In this case the query engine won't read all the data from disk but only those pages that have the data the query needs.
         */
        public Builder filter(Predicate predicate, double value) {
            if (criteria.filter1 != null) {
                throw new IllegalStateException();
            }
            criteria.filter1 = new Filter(null, predicate, value);
            return this;
        }

        /**
         * Value based filtering
         * 
         * Filter field can be used to filter data-points by value.
         * 
         * This method is used for combine two predicates if you want to read values that fit some range.
         * 
         * The use of filter field can speed up query execution if the number of returned values is small.
         * In this case the query engine won't read all the data from disk but only those pages that have the data the query needs.
         */
        public Builder filter(Predicate predicate1, double value1, Predicate predicate2, double value2) {
            if (criteria.filter1 != null && criteria.filter2 != null) {
                throw new IllegalStateException();
            }
            criteria.filter1 = new Filter(null, predicate1, value1);
            criteria.filter2 = new Filter(null, predicate2, value2);
            return this;
        }

        /**
         * Limit on output size
         * 
         * You can use this method to limit the number of returned tuples and to skip some tuples at the beginning of the query output.
         * This field works the same as LIMIT clause in SQL.
         * 
         * Don't use this fields if you need to read all the data in chunks.
         * Database executes queries lazily. To read data in chunks,
         * you can issue a normal query (without limit and offset) and read the cursor.
         * When you done with the first row you can read the next one, and so on.
         * The query will be executed as far as you read data through cursor.
         */
        public Builder limit(int limit) {
            criteria.limit = limit;
            return this;
        }

        /**
         * Offset of the query output
         * 
         * You can use this method to limit the number of returned tuples and to skip some tuples at the beginning of the query output.
         * This field works the same as OFFSET clause in SQL.
         * 
         * Don't use this fields if you need to read all the data in chunks.
         * Database executes queries lazily. To read data in chunks,
         * you can issue a normal query (without limit and offset) and read the cursor.
         * When you done with the first row you can read the next one, and so on.
         * The query will be executed as far as you read data through cursor.
         */
        public Builder offset(int offset) {
            criteria.offset = offset;
            return this;
        }

        public GroupAggregateCriteria build() {
            if (criteria.metrics == null || criteria.metrics.isEmpty()) {
                throw new IllegalStateException("[metrics] parameter is required to make a group-aggregate query");
            }
            if (criteria.functions == null || criteria.functions.isEmpty()) {
                throw new IllegalStateException("[functions] parameter is required to make a group-aggregate query");
            }
            if (criteria.from <= 0 && criteria.to <= 0) {
                throw new InvalidIntervalException("[from] and [to] are mandatory parameters");
            }
            return criteria;
        }
    }

    private GroupAggregateCriteria() {
        // no op
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public Duration getStep() {
        return step;
    }

    public EnumSet<AggregateFunction> getFunctions() {
        return functions;
    }

    @Override
    public long getFrom() {
        return from;
    }

    @Override
    public long getTo() {
        return to;
    }

    public String getTagName() {
        return tagName;
    }

    public List<String> getTagValues() {
        return tagValues;
    }

    public List<String> getGroupByTag() {
        return groupByTag;
    }

    public List<String> getPivotByTag() {
        return pivotByTag;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public String getCriteria() {
        return criteria;
    }

    public Filter getFilter1() {
        return filter1;
    }

    public Filter getFilter2() {
        return filter2;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toJson() {
        if (criteria != null) {
            return criteria;
        }
        JsonStringWriter json = JsonWriter.string();
        JsonStringWriter jroot = json.object();
        if (metrics != null && step != null && functions != null) {
            List<String> funcs = new ArrayList<>(functions.size());
            for (AggregateFunction next : functions) {
                funcs.add(next.name());
            }
            jroot.object("group-aggregate")
                        .array("metric", metrics)
                        .value("step", step.toNanos())
                        .array("func", funcs)
                    .end();
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
            if (tagName != null && tagValues != null) {
                jroot.object("where")
                            .array(tagName, tagValues)
                        .end();
            }
            if (groupByTag != null && !groupByTag.isEmpty()) {
                jroot.array("group-by-tag", groupByTag);
            }
            if (pivotByTag != null && !pivotByTag.isEmpty()) {
                jroot.array("pivot-by-tag", pivotByTag);
            }
            if (orderBy != null) {
                jroot.value("order-by", orderBy.name());
            }
            if (filter1 != null && filter2 == null) {
                jroot.object("filter")
                        .value(filter1.getPredicate().value, filter1.getValue())
                    .end();
            } else if (filter1 != null && filter2 != null) {
                jroot.object("filter")
                        .value(filter1.getPredicate().value, filter1.getValue())
                        .value(filter2.getPredicate().value, filter2.getValue())
                    .end();
            }
            if (limit >= 0) {
                jroot.value("limit", limit);
            }
            if (offset >= 0) {
                jroot.value("offset", offset);
            }
        }
        jroot.end();
        return criteria = json.done();
    }
}
