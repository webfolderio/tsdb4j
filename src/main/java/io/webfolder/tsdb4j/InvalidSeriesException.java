package io.webfolder.tsdb4j;

import static io.webfolder.tsdb4j.Status.fromCode;

public class InvalidSeriesException extends DatabaseException {

    private static final long serialVersionUID = -8868600577720936498L;

    private final String series;

    InvalidSeriesException(int status, String series) {
        super(fromCode(status));
        this.series = series;
    }

    public String getSeries() {
        return series;
    }

    @Override
    public String toString() {
        return "InvalidSeriesException [series=" + series + ", status=" + getStatus() + "]";
    }
}
