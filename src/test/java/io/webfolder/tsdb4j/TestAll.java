package io.webfolder.tsdb4j;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    CreateDeleteTest.class,
    InsertTest.class,
    MetaDataTest.class,
    CriteriaTest.class
})
public class TestAll {

}
