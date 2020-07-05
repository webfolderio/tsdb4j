package io.webfolder.tsdb4j.example;

import static com.jakewharton.fliptables.FlipTable.of;
import static java.lang.String.valueOf;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.deleteIfExists;
import static java.time.Instant.now;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.webfolder.tsdb4j.Database;
import io.webfolder.tsdb4j.SelectCriteria;
import io.webfolder.tsdb4j.Session;
import io.webfolder.tsdb4j.SimpleCursor;
import io.webfolder.tsdb4j.Tag;

public class Select {

    public static void main(String[] args) throws IOException {
        Path path = createTempDirectory("tsdb4j");
        Database database = new Database(path, "example");
        // create & open database
        database.create(1, Database.VOLUME_MIN_SIZE, true);
        if (!database.open()) {
            database.close();
            throw new RuntimeException("db open failed!");
        }
        Instant now = now();
        try (Session session = database.createSession()) {
            // insert dummy data
            session.add(now.plusSeconds(1), "cpu.usage location=Tallinn", 20.10D);
            session.add(now.plusSeconds(2), "cpu.usage location=Tallinn", 21.20D);
            session.add(now.plusSeconds(3), "cpu.usage location=Tallinn", 22.30D);
            session.add(now.plusSeconds(4), "cpu.usage location=Tallinn", 23.40D);
            // build select criteria
            SelectCriteria select = SelectCriteria.builder()
                                                        .select("cpu.usage")
                                                        .from(now)
                                                        .to(now.plusSeconds(5))
                                                    .build();
            List<String[]> list = new ArrayList<>();
            // query data
            try (SimpleCursor cursor = session.query(select)) {
                while (cursor.hasNext()) {
                    String series = cursor.next();
                    Tag tag = cursor.getTags().get(0);
                    list.add(new String[] {
                            series,
                            cursor.getMetric(),
                            valueOf(cursor.getValue()),
                            tag.getName(),
                            tag.getValue(),
                            cursor.getTimestampAsInstant().toString()
                    });
                }
            }
            System.out.println(of(new String[] {
                                        "Series", "Metric", "Value", "Tag Name", "Tag Value", "Timestamp"
                            }, list.toArray(new String[][] { })));
        }
        database.close();
        database.delete();
        deleteIfExists(path);
    }
}
