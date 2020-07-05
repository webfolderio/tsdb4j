package io.webfolder.tsdb4j;

public enum Predicate {
    GreaterThan("gt"),
    GreaterOrEqual("ge"),
    LessThan("lt"),
    lessThanOrEqual("le");

    public final String value;

    private Predicate(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
