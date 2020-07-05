package io.webfolder.tsdb4j;

import static java.time.Instant.ofEpochSecond;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Instant;    

public class TimeUtils {

    public static long toEpoch(Instant timestamp) {
        return SECONDS.toNanos(timestamp.getEpochSecond()) + timestamp.getNano();
    }

    public static Instant fromEpoch(long nanoEpoch) {
        long epochSecond    = NANOSECONDS.toSeconds(nanoEpoch);
        long nanoAdjustment = nanoEpoch - SECONDS.toNanos(epochSecond);
        return ofEpochSecond(epochSecond, nanoAdjustment);
    }
}
