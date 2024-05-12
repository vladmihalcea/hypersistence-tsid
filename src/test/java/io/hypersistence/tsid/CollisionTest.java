package io.hypersistence.tsid;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CollisionTest {

	private TSID.Factory newFactory(int nodeBits) {
		return TSID.Factory.builder()
			.withRandomFunction(TSID.Factory.THREAD_LOCAL_RANDOM_FUNCTION)
			.withNodeBits(nodeBits) // 8 bits: 256 nodes; 10 bits: 1024 nodes...
			.build();
	}

	@Test
	public void testCollisionOneThreadPerNode() throws InterruptedException {

		int threadCount = 16;
		int iterationCount = 100_000;

		AtomicInteger clashes = new AtomicInteger();
		CountDownLatch endLatch = new CountDownLatch(threadCount);
		ConcurrentMap<Long, Integer> tsidMap = new ConcurrentHashMap<>();

		for (int i = 0; i < threadCount; i++) {

			final int threadId = i;

			new Thread(() -> {
				TSID.Factory factory = TSID.Factory.builder()
					.withNode(threadId)
					.build();

				for (int j = 0; j < iterationCount; j++) {
					Long tsid = factory.generate().toLong();
					if (Objects.nonNull(tsidMap.put(tsid, (threadId * iterationCount) + j))) {
						clashes.incrementAndGet();
						break;
					}
				}

				endLatch.countDown();
			}).start();
		}
		endLatch.await();

		assertEquals("Collisions detected!", 0, clashes.intValue());
	}

	@Test
	public void testCollisionMultipleThreadsPerNode() throws InterruptedException {

		int nodeBits = 1;
		int threadCount = 16;
		int iterationCount = 200_000;

		AtomicInteger clashes = new AtomicInteger();
		CountDownLatch endLatch = new CountDownLatch(threadCount);
		ConcurrentMap<Long, Integer> tsidMap = new ConcurrentHashMap<>();
        final TSID.Factory  factory = TSID.Factory.builder()
                                           .withNodeBits(nodeBits)
                                           .withRandomFunction(TSID.Factory.THREAD_LOCAL_RANDOM_FUNCTION)
                                           .build();

		for (int i = 0; i < threadCount; i++) {

			final int threadId = i;

			new Thread(() -> {

				for (int j = 0; j < iterationCount; j++) {
					Long tsid = factory.generate().toLong();
					if (Objects.nonNull(tsidMap.put(tsid, (threadId * iterationCount) + j))) {
						clashes.incrementAndGet();
						break;
					}
				}

				endLatch.countDown();
			}).start();
		}
		endLatch.await();

		assertEquals("Collisions detected!", 0, clashes.intValue());
	}
}