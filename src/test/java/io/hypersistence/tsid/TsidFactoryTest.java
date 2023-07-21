package io.hypersistence.tsid;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static io.hypersistence.tsid.TSID.Factory.Settings;
import static io.hypersistence.tsid.TSID.Factory.Settings.NODE;
import static io.hypersistence.tsid.TSID.Factory.Settings.NODE_COUNT;

public class TsidFactoryTest {

	private static final int LOOP_MAX = 1_000;

	@Before
	public void before() {
		// clear properties
		System.clearProperty(NODE);
		System.clearProperty(NODE_COUNT);
	}

	@After
	public void after() {
		// clear properties
		System.clearProperty(NODE);
		System.clearProperty(NODE_COUNT);
	}

	@Test
	public void testGetInstant() {

		Instant start = Instant.now();
		TSID tsid = TSID.Factory.getTsid();
		Instant middle = tsid.getInstant();
		Instant end = Instant.now();

		assertTrue(start.toEpochMilli() <= middle.toEpochMilli());
		assertTrue(middle.toEpochMilli() <= end.toEpochMilli() + 1);
	}

	@Test
	public void testGetUnixMilliseconds() {

		long start = System.currentTimeMillis();
		TSID tsid = (new TSID.Factory()).generate();
		long middle = tsid.getUnixMilliseconds();
		long end = System.currentTimeMillis();

		assertTrue(start <= middle);
		assertTrue(middle <= end + 1);
	}

	@Test
	public void testGetInstantWithClock() {

		final long bound = (long) Math.pow(2, 42);

		for (int i = 0; i < LOOP_MAX; i++) {

			// instantiate a factory with a Clock that returns a fixed value
			final long random = ThreadLocalRandom.current().nextLong(bound);
			final long millis = random + TSID.TSID_EPOCH; // avoid dates before 2020
			Clock clock = Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC); // simulate a frozen clock
			IntFunction<byte[]> randomFunction = x -> new byte[x]; // force to reinitialize the counter to ZERO
			TSID.Factory factory = TSID.Factory.builder().withClock(clock).withRandomFunction(randomFunction).build();

			long result = factory.generate().getInstant().toEpochMilli();
			assertEquals("The current instant is incorrect", millis, result);
		}
	}

	@Test
	public void testGetUnixMillisecondsWithClock() {

		final long bound = (long) Math.pow(2, 42);

		for (int i = 0; i < LOOP_MAX; i++) {

			// instantiate a factory with a Clock that returns a fixed value
			final long random = ThreadLocalRandom.current().nextLong(bound);
			final long millis = random + TSID.TSID_EPOCH; // avoid dates before 2020
			Clock clock = Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC); // simulate a frozen clock
			IntFunction<byte[]> randomFunction = x -> new byte[x]; // force to reinitialize the counter to ZERO
			TSID.Factory factory = TSID.Factory.builder().withClock(clock).withRandomFunction(randomFunction).build();

			long result = factory.generate().getUnixMilliseconds();
			assertEquals("The current millisecond is incorrect", millis, result);
		}
	}

	@Test
	public void testGetInstantWithCustomEpoch() {

		Instant customEpoch = Instant.parse("2015-10-23T00:00:00Z");

		Instant start = Instant.now();
		TSID tsid = TSID.Factory.builder().withCustomEpoch(customEpoch).build().generate();
		Instant middle = tsid.getInstant(customEpoch);
		Instant end = Instant.now();

		assertTrue(start.toEpochMilli() <= middle.toEpochMilli());
		assertTrue(middle.toEpochMilli() <= end.toEpochMilli());
	}

	@Test
	public void testGetUnixMillisecondsWithCustomEpoch() {

		Instant customEpoch = Instant.parse("1984-01-01T00:00:00Z");

		long start = System.currentTimeMillis();
		TSID tsid = TSID.Factory.builder().withCustomEpoch(customEpoch).build().generate();
		long middle = tsid.getInstant(customEpoch).toEpochMilli();
		long end = System.currentTimeMillis();

		assertTrue(start <= middle);
		assertTrue(middle <= end);
	}

	@Test
	public void testWithNode() {
		{
			for (int i = 0; i <= 20; i++) {
				int bits = TSID.Factory.NODE_BITS_1024;
				int shif = TSID.RANDOM_BITS - bits;
				int mask = ((1 << bits) - 1);
				int node = ThreadLocalRandom.current().nextInt() & mask;
				TSID.Factory factory = new TSID.Factory(node);
				assertEquals(node, (factory.generate().getRandom() >>> shif) & mask);
			}
		}
		{
			for (int i = 0; i <= 20; i++) {
				int bits = TSID.Factory.NODE_BITS_1024;
				int shif = TSID.RANDOM_BITS - bits;
				int mask = ((1 << bits) - 1);
				int node = ThreadLocalRandom.current().nextInt() & mask;
				System.setProperty(NODE, String.valueOf(node));
				TSID.Factory factory = new TSID.Factory();
				assertEquals(node, (factory.generate().getRandom() >>> shif) & mask);
			}
		}
		{
			for (int i = 0; i <= 20; i++) {
				int bits = TSID.Factory.NODE_BITS_1024;
				int shif = TSID.RANDOM_BITS - bits;
				int mask = ((1 << bits) - 1);
				int node = ThreadLocalRandom.current().nextInt() & mask;
				TSID.Factory factory = TSID.Factory.builder().withNode(node).build();
				assertEquals(node, (factory.generate().getRandom() >>> shif) & mask);
			}
		}
		{
			for (int i = 0; i <= 20; i++) {
				int bits = TSID.Factory.NODE_BITS_256;
				int shif = TSID.RANDOM_BITS - bits;
				int mask = ((1 << bits) - 1);
				int node = ThreadLocalRandom.current().nextInt() & mask;
				TSID.Factory factory = TSID.Factory.newInstance256(node);
				assertEquals(node, (factory.generate().getRandom() >>> shif) & mask);
			}
		}
		{
			for (int i = 0; i <= 20; i++) {
				int bits = TSID.Factory.NODE_BITS_1024;
				int shif = TSID.RANDOM_BITS - bits;
				int mask = ((1 << bits) - 1);
				int node = ThreadLocalRandom.current().nextInt() & mask;
				TSID.Factory factory = TSID.Factory.newInstance1024(node);
				assertEquals(node, (factory.generate().getRandom() >>> shif) & mask);
			}
		}
		{
			for (int i = 0; i <= 20; i++) {
				int bits = TSID.Factory.NODE_BITS_4096;
				int shif = TSID.RANDOM_BITS - bits;
				int mask = ((1 << bits) - 1);
				int node = ThreadLocalRandom.current().nextInt() & mask;
				TSID.Factory factory = TSID.Factory.newInstance4096(node);
				assertEquals(node, (factory.generate().getRandom() >>> shif) & mask);
			}
		}
	}

	@Test
	public void testWithNodeBits() {
		final int randomBits = 22;
		// test all allowed values of node bits
		for (int i = 0; i <= 20; i++) {
			final int nodeBits = i;
			final int counterBits = randomBits - nodeBits;
			final int node = (1 << nodeBits) - 1; // max: 2^nodeBits - 1
			TSID tsid = TSID.Factory.builder().withNodeBits(nodeBits).withNode(node).build().generate();
			int actual = (int) tsid.getRandom() >>> counterBits;
			assertEquals(node, actual);
		}
	}

	@Test
	public void testWithNodeCount() {
		final int randomBits = 22;
		// test all allowed values of node bits
		for (int i = 0; i <= 20; i++) {
			final int nodeBits = i;
			final int counterBits = randomBits - nodeBits;
			final int node = (1 << nodeBits) - 1; // max: 2^nodeBits - 1
			final int nodeCount = (int) Math.pow(2, nodeBits);
			System.setProperty(NODE_COUNT, String.valueOf(nodeCount));
			TSID tsid = TSID.Factory.builder().withNode(node).build().generate();
			int actual = (int) tsid.getRandom() >>> counterBits;
			assertEquals(node, actual);
		}
	}

	@Test
	public void testWithRandom() {
		Random random = new Random();
		TSID.Factory factory = TSID.Factory.builder().withRandom(random).build();
		assertNotNull(factory.generate());
	}

	@Test
	public void testWithRandomNull() {
		TSID.Factory factory = TSID.Factory.builder().withRandom(null).build();
		assertNotNull(factory.generate());
	}

	@Test
	public void testWithRandomFunction() {
		{
			SplittableRandom random = new SplittableRandom();
			IntSupplier function = () -> random.nextInt();
			TSID.Factory factory = TSID.Factory.builder().withRandomFunction(function).build();
			assertNotNull(factory.generate());
		}
		{
			IntFunction<byte[]> function = (length) -> {
				byte[] bytes = new byte[length];
				ThreadLocalRandom.current().nextBytes(bytes);
				return bytes;
			};
			TSID.Factory factory = TSID.Factory.builder().withRandomFunction(function).build();
			assertNotNull(factory.generate());
		}
	}

	@Test
	public void testWithRandomFunctionNull() {
		{
			TSID.Factory factory = TSID.Factory.builder().withRandomFunction((IntSupplier) null).build();
			assertNotNull(factory.generate());
		}
		{
			TSID.Factory factory = TSID.Factory.builder().withRandomFunction((IntFunction<byte[]>) null).build();
			assertNotNull(factory.generate());
		}
	}

	@Test
	@Ignore
	public void testWithRandomFunctionReturningZero() throws InterruptedException {

		// a random function that returns a fixed array filled with ZEROS
		IntFunction<byte[]> randomFunction = (x) -> new byte[x];

		TSID.Factory factory = TSID.Factory.builder().withRandomFunction(randomFunction).build();

		final long mask = 0b111111111111; // counter bits: 12

		// test it 5 times, waiting 1ms each time
		for (int i = 0; i < 5; i++) {
			Thread.sleep(1); // wait 1ms
			long expected = 0;
			long counter = factory.generate().getRandom() & mask;
			assertEquals("The counter should be equal to ZERO when the ms changes", expected, counter);
		}
	}

	@Test
	@Ignore
	public void testWithRandomFunctionReturningNonZero() throws InterruptedException {

		// a random function that returns a fixed array
		byte[] fixed = { 0, 0, 0, 0, 0, 0, 0, 127 };
		IntFunction<byte[]> randomFunction = (x) -> fixed;

		TSID.Factory factory = TSID.Factory.builder().withRandomFunction(randomFunction).build();

		final long mask = 0b111111111111; // counter bits: 12

		// test it 5 times, waiting 1ms each time
		for (int i = 0; i < 5; i++) {
			Thread.sleep(1); // wait 1ms
			long expected = fixed[2];
			long counter = factory.generate().getRandom() & mask;
			assertEquals("The counter should be equal to a fixed value when the ms changes", expected, counter);
		}
	}

	@Test
	public void testMonotonicityAfterClockDrift() throws InterruptedException {

		long diff = 10_000;
		long time = Instant.parse("2021-12-31T23:59:59.000Z").toEpochMilli();
		long times[] = { -1, time, time + 0, time + 1, time + 2, time + 3 - diff, time + 4 - diff, time + 5 };

		Clock clock = new Clock() {
			private int i;

			@Override
			public long millis() {
				return times[i++ % times.length];
			}

			@Override
			public ZoneId getZone() {
				return null;
			}

			@Override
			public Clock withZone(ZoneId zone) {
				return null;
			}

			@Override
			public Instant instant() {
				return null;
			}
		};

		// a function that forces the clock to restart to ZERO
		IntFunction<byte[]> randomFunction = x -> new byte[x];

		TSID.Factory factory = TSID.Factory.builder().withClock(clock).withRandomFunction(randomFunction).build();

		long ms1 = factory.generate().getUnixMilliseconds(); // time
		long ms2 = factory.generate().getUnixMilliseconds(); // time + 0
		long ms3 = factory.generate().getUnixMilliseconds(); // time + 1
		long ms4 = factory.generate().getUnixMilliseconds(); // time + 2
		long ms5 = factory.generate().getUnixMilliseconds(); // time + 3 - 10000 (CLOCK DRIFT)
		long ms6 = factory.generate().getUnixMilliseconds(); // time + 4 - 10000 (CLOCK DRIFT)
		long ms7 = factory.generate().getUnixMilliseconds(); // time + 5
		assertEquals(ms1 + 0, ms2); // clock repeats.
		assertEquals(ms1 + 1, ms3); // clock advanced.
		assertEquals(ms1 + 2, ms4); // clock advanced.
		assertEquals(ms1 + 2, ms5); // CLOCK DRIFT! DON'T MOVE BACKWARDS!
		assertEquals(ms1 + 2, ms6); // CLOCK DRIFT! DON'T MOVE BACKWARDS!
		assertEquals(ms1 + 5, ms7); // clock advanced.
	}

	@Test
	public void testMonotonicityAfterLeapSecond() throws InterruptedException {

		long second = Instant.parse("2021-12-31T23:59:59.000Z").getEpochSecond();
		long leapSecond = second - 1; // simulate a leap second
		long times[] = { second, leapSecond };

		Clock clock = new Clock() {
			private int i;

			@Override
			public long millis() {
				return times[i++ % times.length] * 1000;
			}

			@Override
			public ZoneId getZone() {
				return null;
			}

			@Override
			public Clock withZone(ZoneId zone) {
				return null;
			}

			@Override
			public Instant instant() {
				return null;
			}
		};

		// a function that forces the clock to restart to ZERO
		IntFunction<byte[]> randomFunction = x -> new byte[x];

		TSID.Factory factory = TSID.Factory.builder().withClock(clock).withRandomFunction(randomFunction).build();

		long ms1 = factory.generate().getUnixMilliseconds(); // second
		long ms2 = factory.generate().getUnixMilliseconds(); // leap second

		assertEquals(ms1, ms2); // LEAP SECOND! DON'T MOVE BACKWARDS!
	}

	@Test
	public void testByteRandomNextInt() {

		for (int i = 0; i < 10; i++) {
			byte[] bytes = new byte[Integer.BYTES];
			(new Random()).nextBytes(bytes);
			int number = ByteBuffer.wrap(bytes).getInt();
			TSID.Factory.IRandom random = new TSID.Factory.ByteRandom((x) -> bytes);
			assertEquals(number, random.nextInt());
		}

		for (int i = 0; i < 10; i++) {

			int ints = 10;
			int size = Integer.BYTES * ints;

			byte[] bytes = new byte[size];
			(new Random()).nextBytes(bytes);
			ByteBuffer buffer1 = ByteBuffer.wrap(bytes);
			ByteBuffer buffer2 = ByteBuffer.wrap(bytes);

			TSID.Factory.IRandom random = new TSID.Factory.ByteRandom((x) -> {
				byte[] octects = new byte[x];
				buffer1.get(octects);
				return octects;
			});

			for (int j = 0; j < ints; j++) {
				assertEquals(buffer2.getInt(), random.nextInt());
			}
		}
	}

	@Test
	public void testByteRandomNextBytes() {

		for (int i = 0; i < 10; i++) {
			byte[] bytes = new byte[Integer.BYTES];
			(new Random()).nextBytes(bytes);
			TSID.Factory.IRandom random = new TSID.Factory.ByteRandom((x) -> bytes);
			assertEquals(Arrays.toString(bytes), Arrays.toString(random.nextBytes(Integer.BYTES)));
		}

		for (int i = 0; i < 10; i++) {

			int ints = 10;
			int size = Integer.BYTES * ints;

			byte[] bytes = new byte[size];
			(new Random()).nextBytes(bytes);
			ByteBuffer buffer1 = ByteBuffer.wrap(bytes);
			ByteBuffer buffer2 = ByteBuffer.wrap(bytes);

			TSID.Factory.IRandom random = new TSID.Factory.ByteRandom((x) -> {
				byte[] octects = new byte[x];
				buffer1.get(octects);
				return octects;
			});

			for (int j = 0; j < ints; j++) {
				byte[] octects = new byte[Integer.BYTES];
				buffer2.get(octects);
				assertEquals(Arrays.toString(octects), Arrays.toString(random.nextBytes(Integer.BYTES)));
			}
		}
	}

	@Test
	public void testLogRandomNextInt() {

		for (int i = 0; i < 10; i++) {
			byte[] bytes = new byte[Integer.BYTES];
			(new Random()).nextBytes(bytes);
			int number = ByteBuffer.wrap(bytes).getInt();
			TSID.Factory.IRandom random = new TSID.Factory.IntRandom(() -> number);
			assertEquals(number, random.nextInt());
		}

		for (int i = 0; i < 10; i++) {

			int ints = 10;
			int size = Integer.BYTES * ints;

			byte[] bytes = new byte[size];
			(new Random()).nextBytes(bytes);
			ByteBuffer buffer1 = ByteBuffer.wrap(bytes);
			ByteBuffer buffer2 = ByteBuffer.wrap(bytes);

			TSID.Factory.IRandom random = new TSID.Factory.IntRandom(() -> buffer1.getInt());

			for (int j = 0; j < ints; j++) {
				assertEquals(buffer2.getInt(), random.nextInt());
			}
		}
	}

	@Test
	public void testLogRandomNextBytes() {

		for (int i = 0; i < 10; i++) {
			byte[] bytes = new byte[Integer.BYTES];
			(new Random()).nextBytes(bytes);
			int number = ByteBuffer.wrap(bytes).getInt();
			TSID.Factory.IRandom random = new TSID.Factory.IntRandom(() -> number);
			assertEquals(Arrays.toString(bytes), Arrays.toString(random.nextBytes(Integer.BYTES)));
		}

		for (int i = 0; i < 10; i++) {

			int ints = 10;
			int size = Integer.BYTES * ints;

			byte[] bytes = new byte[size];
			(new Random()).nextBytes(bytes);
			ByteBuffer buffer1 = ByteBuffer.wrap(bytes);
			ByteBuffer buffer2 = ByteBuffer.wrap(bytes);

			TSID.Factory.IRandom random = new TSID.Factory.IntRandom(() -> buffer1.getInt());

			for (int j = 0; j < ints; j++) {
				byte[] octects = new byte[Integer.BYTES];
				buffer2.get(octects);
				assertEquals(Arrays.toString(octects), Arrays.toString(random.nextBytes(Integer.BYTES)));
			}
		}
	}

	@Test
	public void testSettingsGetNode() {
		for (int i = 0; i < 100; i++) {
			int number = ThreadLocalRandom.current().nextInt();
			System.setProperty(NODE, String.valueOf(number));
			long result = Settings.getNode();
			assertEquals(number, result);
		}
	}

	@Test
	public void testSettingsGetNodeCount() {
		for (int i = 0; i < 100; i++) {
			int number = ThreadLocalRandom.current().nextInt();
			System.setProperty(NODE_COUNT, String.valueOf(number));
			long result = Settings.getNodeCount();
			assertEquals(number, result);
		}
	}

	@Test
	public void testSettingsGetNodeInvalid() {
		String string = "0xx11223344"; // typo
		System.setProperty(NODE, string);
		Integer result = Settings.getNode();
		assertNull(result);

		string = " 0x11223344"; // space
		System.setProperty(NODE, string);
		result = Settings.getNode();
		assertNull(result);

		string = "0x112233zz"; // non hexadecimal
		System.setProperty(NODE, string);
		result = Settings.getNode();
		assertNull(result);

		string = ""; // empty
		System.setProperty(NODE, string);
		result = Settings.getNode();
		assertNull(result);

		string = " "; // blank
		System.setProperty(NODE, string);
		result = Settings.getNode();
		assertNull(result);
	}

	@Test
	public void testSettingsGetNodeCountInvalid() {
		String string = "0xx11223344"; // typo
		System.setProperty(NODE_COUNT, string);
		Integer result = Settings.getNodeCount();
		assertNull(result);

		string = " 0x11223344"; // space
		System.setProperty(NODE_COUNT, string);
		result = Settings.getNodeCount();
		assertNull(result);

		string = "0x112233zz"; // non hexadecimal
		System.setProperty(NODE_COUNT, string);
		result = Settings.getNodeCount();
		assertNull(result);

		string = ""; // empty
		System.setProperty(NODE_COUNT, string);
		result = Settings.getNodeCount();
		assertNull(result);

		string = " "; // blank
		System.setProperty(NODE_COUNT, string);
		result = Settings.getNodeCount();
		assertNull(result);
	}
}
