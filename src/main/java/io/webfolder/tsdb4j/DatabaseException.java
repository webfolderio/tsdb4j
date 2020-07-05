package io.webfolder.tsdb4j;

public class DatabaseException extends TsdbException {

    private static final long serialVersionUID = 8123799053357164238L;

    private final Status status;

    DatabaseException(Status status) {
        super(status.message);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "DatabaseException [status=" + status + "]";
    }
}
