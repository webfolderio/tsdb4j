package io.webfolder.tsdb4j;

import org.junit.Assert;
import org.junit.Test;

public class MetaDataTest extends AbstractTest {

    @Test
    public void t01_query() {
        Database db = createTempDb();
        db.open();
        Session session = db.createSession();
        session.add(now(), "mem server=prod location=unkown", 20.20);
        try (MetaDataCursor cursor = session.metadata()) {
            for (String next : cursor) {
                Assert.assertNotNull(next);
                Assert.assertEquals("mem", cursor.getMetric());
                Assert.assertEquals(2, cursor.getTags().size());
                Assert.assertEquals("location", cursor.getTags().get(0).getName());
                Assert.assertEquals("unkown", cursor.getTags().get(0).getValue());
                Assert.assertEquals("server", cursor.getTags().get(1).getName());
                Assert.assertEquals("prod", cursor.getTags().get(1).getValue());
            }
        }
        session.close();
        db.close();
        db.delete();
        deleteIfExists(db.getPath());
    }
}
