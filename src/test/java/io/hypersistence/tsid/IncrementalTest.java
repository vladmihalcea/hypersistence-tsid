package io.hypersistence.tsid;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class IncrementalTest {

	private static final int TEN_SECONDS = 10_000;
	
    private static class ClockMock extends Clock {

        private Instant instant = Instant.ofEpochSecond(0);

        @Override
        public ZoneId getZone() {
            throw new UnsupportedOperationException("getZone");
        }

        @Override
        public Clock withZone(ZoneId zoneid) {
            throw new UnsupportedOperationException("withZone");
        }

        @Override
        public Instant instant() {
            return instant;
        }

        public void incrementMillis(int millis) {
            this.instant = this.instant.plusMillis(millis);
        }

        public void decrementMillis(int millis) {
            this.instant = this.instant.minusMillis(millis);
        }
    }

    @Test
    public void shouldGenerateIncrementalValuesInCaseOfBackwardAGlitch() {
        ClockMock clock = new ClockMock();

        TSID.Factory factory = TSID.Factory.builder()
                .withNodeBits(20)
                .withNode(0)
                .withClock(clock)
                .build();

        TSID prev = factory.generate();

        clock.decrementMillis(TEN_SECONDS - 1);

        TSID next = factory.generate();

        assertIncremental(prev, next);
    }

    @Test
    public void shouldGenerateIncrementalValuesInCaseOfForwardAGlitch() {
        ClockMock clock = new ClockMock();

        TSID.Factory factory = TSID.Factory.builder()
                .withNodeBits(20)
                .withNode(0)
                .withClock(clock)
                .build();

        TSID prev = factory.generate();

        clock.incrementMillis(TEN_SECONDS - 1);

        TSID next = factory.generate();

        assertIncremental(prev, next);
    }

    @Test
    public void shouldManageAGlitch() {
    	
        TSID.Factory factory = TSID.Factory.builder()
                .withRandomFunction(() -> 0)
                .withClock(new ClockMock())
                .withNodeBits(20)
                .withNode(0)
                .build();

        final int advanceTimeUpTODriftTolerance = TEN_SECONDS * 4 - 1;

        long last = Long.MIN_VALUE;
        for (int i=0; i < advanceTimeUpTODriftTolerance; i++) {
            long tsid = factory.generate().toLong();
            assertTrue(last < tsid);
            last = tsid;
        }
        
        TSID prev = factory.generate();
        assertTrue(last < prev.toLong());
        
        TSID next = factory.generate();
        assertIncremental(prev, next);
    }

     @Test
    public void shouldAlwaysBeIncremental() {
        TSID.Factory factory = TSID.Factory.builder()
                .withNodeBits(20)
                .withNode(0)
                .build();

        long last = 0;
        for (int i=0; i<1_000_000; i++) {
            long tsid = factory.generate().toLong();
            if (last != 0 && tsid < last) {
                fail(String.format("generated TSID value is less that the previous one:\n" +
                     "   iteration: %d\n" +
                     "   previous: %s  long= %d\n" +
                     "   actual  : %s  long= %d\n", i,
                                     TSID.from(last).toString(), last,
                                     TSID.from(tsid).toString(), tsid));
            }
            last = tsid;
        }
    }

    private void assertIncremental(TSID prev, TSID next) {
        assertTrue(
                String.format("generated TSID value is less that the previous one:\n" +
                "   previous: %s  long= %d\n" +
                "   actual  : %s  long= %d\n",
                prev, prev.toLong(),next, next.toLong()),
                prev.toLong() < next.toLong());

    }
}
