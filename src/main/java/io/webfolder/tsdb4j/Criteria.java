package io.webfolder.tsdb4j;

public interface Criteria {

    String toJson();

    long getFrom();

    long getTo();
}
