package io.webfolder.tsdb4j;

import java.io.Serializable;

public class Filter implements Serializable {

    private static final long serialVersionUID = 216276235051807400L;

    private final String metric;

    private final Predicate predicate;

    private final double value;

    public Filter(String metric, Predicate predicate, double value) {
        this.metric = metric;
        this.predicate = predicate;
        this.value = value;
    }

    public String getMetric() {
        return metric;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metric == null) ? 0 : metric.hashCode());
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Filter other = (Filter) obj;
        if (metric == null) {
            if (other.metric != null)
                return false;
        } else if (!metric.equals(other.metric))
            return false;
        if (predicate != other.predicate)
            return false;
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Filter [metric=" + metric + ", predicate=" + predicate + ", value=" + value + "]";
    }
}
