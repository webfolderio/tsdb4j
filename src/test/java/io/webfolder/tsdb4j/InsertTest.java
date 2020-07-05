package io.webfolder.tsdb4j;

import org.junit.Test;

public class InsertTest extends AbstractTest {

    @Test
    public void t01_insert() {
        Database db = createTempDb();
        db.open();
        Session session = db.createSession();
        session.add(now(), "mem server=1", 20.20);
        session.close();
        db.close();
        db.delete();
        deleteIfExists(db.getPath());
    }
}
