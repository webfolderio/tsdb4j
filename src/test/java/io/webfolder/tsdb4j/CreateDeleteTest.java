package io.webfolder.tsdb4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class CreateDeleteTest extends AbstractTest {

    @Test
    public void t01_testOpenClose() {
        Database db = createTempDb();
        boolean open = db.open();
        Assert.assertTrue(open);
        db.close();
        db.delete();
        deleteIfExists(db.getPath());
    }

    @Test
    public void t02_testDelete() throws IOException {
        Path path = createTempPath();

        Database database = createTempDb(path);

        Path volFile = path.resolve("test_0.vol");
        Path dbFile = path.resolve("test.akumuli");

        Assert.assertTrue(Files.exists(dbFile));
        Assert.assertTrue(Files.exists(volFile));
        Assert.assertTrue(Files.size(dbFile) >= 1024L);
        Assert.assertTrue(Files.size(volFile) >= 1024L * 1024L);

        database.delete();

        database.close();

        deleteIfExists(path);
    }
}
