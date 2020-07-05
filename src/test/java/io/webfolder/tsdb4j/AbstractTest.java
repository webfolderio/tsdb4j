package io.webfolder.tsdb4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class AbstractTest {

    protected Database createTempDb() {
        return createTempDb(createTempPath());
    }

    protected Database createTempDb(Path tempPath) {
        Database database = new Database(tempPath, "test");
        database.create(2, 1024 * 1024, true);
        return database;
    }

    protected Path createTempPath() {
        try {
            return Files.createTempDirectory("tsdb4j");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected long now() {
        return TimeUtils.toEpoch(Instant.now());
    }
}
