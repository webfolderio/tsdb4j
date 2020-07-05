package io.webfolder.tsdb4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

public class CriteriaTest extends AbstractTest {

    @Test
    public void t01_select() {
        Database db = createTempDb();
        db.open();
        Session session = db.createSession();
        long now = now();
        session.add(now, "mem server=1", 20.20D);
        SelectCriteria criteria = SelectCriteria.builder()
                                        .select("mem")
                                        .from(now)
                                        .to(now + 1)
                                    .build();
        SimpleCursor cursor = session.query(criteria);
        Assert.assertTrue(cursor.hasNext());
        String series = cursor.next();
        Assert.assertEquals("mem server=1", series);
        Assert.assertEquals("mem", cursor.getMetric());
        Assert.assertEquals(20.20D, cursor.getValue(), 0);
        Assert.assertEquals(1, cursor.getTags().size());
        Assert.assertEquals(new Tag("server", "1"), cursor.getTags().get(0));
        cursor.close();
        session.close();
        db.close();
        db.delete();
        deleteIfExists(db.getPath());
    }

    @Test
    public void t02_select() {
        Database db = createTempDb();
        db.open();
        Session session = db.createSession();
        Instant now = Instant.now();
        for (int i = 0; i < 1_000_000; i++) {
            session.add(now.plusNanos(1), "mem server=1 server=2", i);
        }
        long from = TimeUtils.toEpoch(Instant.now().minusSeconds(60));
        long to = TimeUtils.toEpoch(Instant.now().plusSeconds(60));
        SelectCriteria criteria = SelectCriteria.builder()
                                        .select("mem")
                                        .from(from)
                                        .to(to)
                                    .build();
        SimpleCursor cursor = session.query(criteria);

        long start = System.currentTimeMillis();

        int counter = 0;
        while (cursor.hasNext()) {
            String series = cursor.next();
            Assert.assertEquals("mem server=1 server=2", series);
            Assert.assertEquals(counter, cursor.getValue(), 0);
            counter += 1;
        }

        long end = System.currentTimeMillis();
        
        Assert.assertTrue(end - start < 2000);

        Assert.assertEquals(1_000_000, counter);
        Assert.assertFalse(cursor.hasNext());

        AggregateCriteria aggregateCriteria = AggregateCriteria.builder()
                                                    .aggregate("mem", AggregateFunction.count)
                                                    .from(from)
                                                    .to(to)
                                                .build();

        start = System.currentTimeMillis();
        double count = 0;
        try (AggregateCursor cursor2 = session.query(aggregateCriteria)) {
            if (cursor2.hasNext()) {
                cursor2.next();
                count = cursor2.getValue();
            }
        }

        Assert.assertEquals(1_000_000D, count, 0);
        end = System.currentTimeMillis();
        
        Assert.assertTrue(end - start < 100);
        
        cursor.close();
        session.close();
        db.close();
        db.delete();
        deleteIfExists(db.getPath());
    }

    @Test
    public void t03_aggregate() throws Exception {
        Database db = createTempDb();
        db.open();
        Session session = db.createSession();
        Instant now = Instant.now();
        session.add(now.plusMillis(10), "mem server=1 server=2", 40);
        session.add(now.plusMillis(10), "mem server=1 server=2", 20);
        Instant maxTimestamp = now.plusMillis(10);
        session.add(maxTimestamp, "mem server=1 server=2", 80);
        session.add(now.plusMillis(10), "mem server=1 server=2", 10);
        AggregateCriteria criteria = AggregateCriteria.builder()
                                        .aggregate("mem", AggregateFunction.max)
                                    .build();
        AggregateCursor cursor = session.query(criteria);
        Assert.assertTrue(cursor.hasNext());
        String series = cursor.next();
        Assert.assertEquals("mem:max server=1 server=2", series);
        Assert.assertEquals("mem", cursor.getMetric());
        Assert.assertEquals(80, cursor.getValue(), 0);
        Assert.assertEquals(2, cursor.getTags().size());
        Assert.assertEquals(AggregateFunction.max, cursor.getAggregateFunction());
        Assert.assertEquals(new Tag("server", "1"), cursor.getTags().get(0));
        Assert.assertEquals(new Tag("server", "2"), cursor.getTags().get(1));
        Assert.assertEquals(maxTimestamp, cursor.getTimestampAsInstant());
        cursor.close();
        session.close();
        db.close();
        db.delete();
        deleteIfExists(db.getPath());        
    }

    @Test
    public void t04_aggregateGroup() throws Exception {
        Database db = createTempDb();
        db.open();
        Session session = db.createSession();
        Instant now = Instant.now();
        session.add(now, "server.mem server=1", 40);
        session.add(now.plusSeconds(1), "server.mem server=1", 20);
        session.add(now.plusSeconds(4), "cpu.time server=1", 400);
        session.add(now.plusSeconds(5), "cpu.time server=1", 200);
        GroupAggregateCriteria criteria = GroupAggregateCriteria.builder()
                                                .groupAggregate(Arrays.asList("server.mem", "cpu.time"), Duration.ofSeconds(60), EnumSet.of(AggregateFunction.sum, AggregateFunction.max, AggregateFunction.min, AggregateFunction.count))
                                                .from(now.minusSeconds(10))
                                                .to(now.plusSeconds(10))
                                            .build();
        session.close();
        session = db.createSession();
        GroupAggregateCursor cursor = session.query(criteria);
        Assert.assertTrue(cursor.hasNext());
        String series = cursor.next();
        Assert.assertEquals("server.mem:count|server.mem:max|server.mem:min|server.mem:sum server=1", series);
        Assert.assertEquals(2, cursor.getValues()[0], 0);
        Assert.assertEquals(40, cursor.getValues()[1], 0);
        Assert.assertEquals(20, cursor.getValues()[2], 0);
        Assert.assertEquals(60, cursor.getValues()[3], 0);
        
        Assert.assertEquals(4, cursor.getAggregateFunctions().size());
        Assert.assertEquals(AggregateFunction.count, cursor.getAggregateFunctions().get(0));
        Assert.assertEquals(AggregateFunction.max, cursor.getAggregateFunctions().get(1));
        Assert.assertEquals(AggregateFunction.min, cursor.getAggregateFunctions().get(2));
        Assert.assertEquals(AggregateFunction.sum, cursor.getAggregateFunctions().get(3));

        Assert.assertEquals(2, cursor.getValue(AggregateFunction.count), 0);
        Assert.assertEquals(40, cursor.getValue(AggregateFunction.max), 0);
        Assert.assertEquals(20, cursor.getValue(AggregateFunction.min), 0);
        Assert.assertEquals(60, cursor.getValue(AggregateFunction.sum), 0);
        Assert.assertEquals(Double.NaN, cursor.getValue(AggregateFunction.last), 0);

        Assert.assertTrue(cursor.hasNext());
        Assert.assertEquals(4, cursor.getMetrics().size());
        Assert.assertEquals("server.mem", cursor.getMetrics().get(0));
        cursor.next();
        Assert.assertFalse(cursor.hasNext());
        Assert.assertEquals(2, cursor.getValues()[0], 0);
        Assert.assertEquals(400, cursor.getValues()[1], 0);
        Assert.assertEquals(200, cursor.getValues()[2], 0);
        Assert.assertEquals(600, cursor.getValues()[3], 0);
        Assert.assertEquals("cpu.time:count|cpu.time:max|cpu.time:min|cpu.time:sum server=1", cursor.getSeries());
        Assert.assertFalse(cursor.hasNext());
        cursor.close();
        session.close();
        db.close();
        db.delete();
        deleteIfExists(db.getPath());
    }

    @Test
    public void t05_join() throws Exception {
        Database db = createTempDb();
        db.open();
        Session session = db.createSession();
        Instant now = Instant.now();
        session.add(now.plusSeconds(1), "hdd.usage location=xyz", 10);
        session.add(now.plusSeconds(1), "hdd.usage location=xyz", 11);
        session.add(now.plusSeconds(1), "cpu.usage location=xyz", 12);
        session.add(now.plusSeconds(1), "cpu.usage location=xyz", 13);
        session.add(now.plusSeconds(1), "mem.usage location=xyz", 14);
        session.add(now.plusSeconds(1), "mem.usage location=xyz", 15);
        JoinCriteria criteria = JoinCriteria.builder()
                                        .join(Arrays.asList("hdd.usage", "cpu.usage", "mem.usage"))
                                        .from(TimeUtils.toEpoch(Instant.now().minusSeconds(60 * 60 * 24)))
                                        .to(TimeUtils.toEpoch(Instant.now().plusSeconds(60 * 60 * 24)))
                                    .build();
        CompoundCursor cursor = session.query(criteria);
        Assert.assertTrue(cursor.hasNext());
        String next = cursor.next();
        Assert.assertEquals("hdd.usage|cpu.usage|mem.usage location=xyz", next);
        Assert.assertEquals(3, cursor.getMetrics().size());
        Assert.assertEquals("hdd.usage", cursor.getMetrics().get(0));
        Assert.assertEquals("cpu.usage", cursor.getMetrics().get(1));
        Assert.assertEquals("mem.usage", cursor.getMetrics().get(2));
        Assert.assertEquals(1, cursor.getTags().size());
        Assert.assertEquals(new Tag("location", "xyz"), cursor.getTags().get(0));
        double[] values = cursor.getValues();
        Assert.assertEquals(3, values.length);
        Assert.assertEquals(10, values[0], 0);
        Assert.assertEquals(12, values[1], 0);
        Assert.assertEquals(14, values[2], 0);

        long timestamp = cursor.getTimestamp();
        Assert.assertTrue(timestamp > 0);
        
        Assert.assertTrue(cursor.hasNext());
        
        next = cursor.next();
        Assert.assertEquals("hdd.usage|cpu.usage|mem.usage location=xyz", next);
        Assert.assertEquals(3, cursor.getMetrics().size());
        Assert.assertEquals("hdd.usage", cursor.getMetrics().get(0));
        Assert.assertEquals("cpu.usage", cursor.getMetrics().get(1));
        Assert.assertEquals("mem.usage", cursor.getMetrics().get(2));
        Assert.assertEquals(1, cursor.getTags().size());
        Assert.assertEquals(new Tag("location", "xyz"), cursor.getTags().get(0));

        values = cursor.getValues();
        Assert.assertEquals(11, values[0], 0);
        Assert.assertEquals(13, values[1], 0);
        Assert.assertEquals(15, values[2], 0);

        timestamp = cursor.getTimestamp();
        Assert.assertTrue(timestamp > 0);

        Assert.assertFalse(cursor.hasNext());
        cursor.close();
        session.close();
        db.close();
        db.delete();
        deleteIfExists(db.getPath());
    }
}
