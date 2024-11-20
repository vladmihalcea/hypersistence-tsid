[![License](https://img.shields.io/github/license/vladmihalcea/hypersistence-tsid.svg)](https://raw.githubusercontent.com/vladmihalcea/hypersistence-tsid/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.hypersistence/hypersistence-tsid.svg)](https://central.sonatype.com/artifact/io.hypersistence/hypersistence-tsid)
[![JavaDoc](http://javadoc.io/badge/io.hypersistence/hypersistence-tsid.svg)](http://www.javadoc.io/doc/io.hypersistence/hypersistence-tsid)

TSID Generator
======================================================

A Java library for generating Time-Sorted Unique Identifiers (TSID).

This version is a fork of the original [tsid-creator](https://github.com/f4b6a3/tsid-creator/) created by Fabio Lima.

The reason why the project was forked was due to [this comment](https://github.com/f4b6a3/tsid-creator/issues/25) from the original author:

> Take what you need from this project and use it on your own. Go ahead and make a better TSID!
>
> I don't intend to add more features here. From now on, this project will only be maintained. I'm getting tired.
> 
> I wish you good luck!

It brings together ideas from [Twitter's Snowflake](https://github.com/twitter-archive/snowflake/tree/snowflake-2010) and [ULID Spec](https://github.com/ulid/spec).

In summary:

*   Sorted by generation time;
*   It can be stored as an integer of 64 bits;
*   It can be stored as a string of 13 chars;
*   String format is encoded to [Crockford's base32](https://www.crockford.com/base32.html);
*   String format is URL safe, is case insensitive, and has no hyphens;
*   Shorter than UUID, ULID and KSUID.

Recommended readings:

* [The best UUID type for a database Primary Key](https://vladmihalcea.com/uuid-database-primary-key/)
* [The best way to generate a TSID entity identifier with JPA and Hibernate](https://vladmihalcea.com/tsid-identifier-jpa-hibernate/)

Usage
------------------------------------------------------

Create a TSID:

```java
TSID tsid = TSID.Factory.getTsid();
```

Create a TSID as `long`:

```java
long number = TSID.Factory.getTsid().toLong(); // 38352658567418872
```

Create a TSID as `String`:

```java
String string = TSID.Factory.getTsid().toString(); // 01226N0640J7Q
```

The TSID generator is [thread-safe](https://en.wikipedia.org/wiki/Thread_safety).

### Dependency

Add these lines to your `pom.xml`:

```xml
<!-- https://central.sonatype.com/artifact/io.hypersistence/hypersistence-tsid -->
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-tsid</artifactId>
    <version>2.1.2</version>
</dependency>
```

### Modularity

Module and bundle names are the same as the root package name.

*   JPMS module name: `io.hypersistence.tsid`
*   OSGi symbolic name: `io.hypersistence.tsid`

### TSID as Long

The `TSID.toLong()` method simply unwraps the internal `long` value of a TSID.

```java
long tsid = TSID.Factory.getTsid().toLong();
```

Sequence of TSIDs:

```text
38352658567418867
38352658567418868
38352658567418869
38352658567418870
38352658567418871
38352658567418872
38352658567418873
38352658567418874
38352658573940759 < millisecond changed
38352658573940760
38352658573940761
38352658573940762
38352658573940763
38352658573940764
38352658573940765
38352658573940766
         ^      ^ look
                                   
|--------|------|
   time   random
```

### TSID as String

The `TSID.toString()` method encodes a TSID to [Crockford's base 32](https://www.crockford.com/base32.html) encoding. The returned string is 13 characters long.

```java
String tsid = TSID.Factory.getTsid().toString();
```

Sequence of TSID strings:

```text
01226N0640J7K
01226N0640J7M
01226N0640J7N
01226N0640J7P
01226N0640J7Q
01226N0640J7R
01226N0640J7S
01226N0640J7T
01226N0693HDA < millisecond changed
01226N0693HDB
01226N0693HDC
01226N0693HDD
01226N0693HDE
01226N0693HDF
01226N0693HDG
01226N0693HDH
        ^   ^ look
                                   
|-------|---|
   time random
```

The string format can be useful for languages that store numbers in [double-precision 64-bit binary format IEEE 754](https://en.wikipedia.org/wiki/Double-precision_floating-point_format), such as [Javascript](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number).

### TSID Structure

The term TSID stands for (roughly) Time-Sorted ID. A TSID is a number that is formed by the creation time along with random bits.

The TSID has 2 components:

*   Time component (42 bits)
*   Random component (22 bits)

The time component is the count of milliseconds since 2020-01-01 00:00:00 UTC.

The Random component has 2 sub-parts:

*   Node ID (0 to 20 bits)
*   Counter (2 to 22 bits)

The counter bits depend on the node bits. If the node bits are 10, the counter bits are limited to 12. In this example, the maximum node value is 2^10-1 = 1023 and the maximum counter value is 2^12-1 = 4095. So the maximum TSIDs that can be generated per millisecond is 4096.

The node identifier uses 10 bits of the random component by default in the `TSID.Factory`. It's possible to adjust the node bits to a value between 0 and 20. The counter bits are affected by the node bits.

This is the default TSID structure:

```
                                            adjustable
                                           <---------->
|------------------------------------------|----------|------------|
       time (msecs since 2020-01-01)           node       counter
                42 bits                       10 bits     12 bits

- time:    2^42 = ~69 years or ~139 years (with adjustable epoch)
- node:    2^10 = 1,024 (with adjustable bits)
- counter: 2^12 = 4,096 (initially random)

Notes:
The node is adjustable from 0 to 20 bits.
The node bits affect the counter bits.
The time component can be used for ~69 years if stored in a SIGNED 64 bits integer field.
The time component can be used for ~139 years if stored in a UNSIGNED 64 bits integer field.
```

The time component can be 1 ms or more ahead of the system time when necessary to maintain monotonicity and generation speed.

### Node identifier

The simplest way to avoid collisions is to make sure that each generator has its exclusive node ID.

The node ID can be given to `TSID.Factory` by defining the `tsid.node` system property or the `TSID_NODE` environment variable. Otherwise, the node identifier will be chosen randomly.

The total number of nodes can be given to `TSID.Factory` by defining the `tsid.node.count` system property or the `TSID_NODE_COUNT` environment variable. If this property or variable is set, `TSID.Factory` will adjust the amount of bits needed to fit the given node count. For example, if the value 100 is given, the number of bits reserved for the node ID is set to 7, which is the minimum number of bits needed to fit 100 nodes. Otherwise, the default number of bits is set to 10, which can accommodate 1024 nodes.

System properties:

*   `tsid.node`: the node identifier.
*   `tsid.node.count`: the total number of nodes.

Environment variables:

*   `TSID_NODE`: the node identifier.
*   `TSID_NODE_COUNT`: the total number of nodes.

Using system properties:

```bash
// append to VM arguments
-Dtsid.node="755"
```

```bash
// append to VM arguments
-Dtsid.node="42" \
-Dtsid.node.count="100"
```

Using environment variables:

```bash
# append to ~/.profile
export TSID_NODE="492"
```

```bash
# append to ~/.profile
export TSID_NODE="123"
export TSID_NODE_COUNT="200"
```

### More Examples

Create a quick TSID:

```java
TSID tsid = TSID.fast();
```

---

Create a TSID from a canonical string (13 chars):

```java
TSID tsid = TSID.from("0123456789ABC");
```

---

Convert a TSID into a canonical string in lower case:

```java
String string = tsid.toLowerCase(); // 0123456789abc
```

---

Get the creation instant of a TSID:

```java
Instant instant = tsid.getInstant(); // 2020-04-15T22:31:02.458Z
```

---

Encode a TSID to base-62:

```java
String string = tsid.encode(62); // 0T5jFDIkmmy
```

---

Format a TSID to a string starting with a letter, where "K" is the letter and "%S" is a placeholder:

```java
String string = tsid.format("K%S"); // K0AWE5HZP3SKTK
```

---

A key generator that makes substitution easy if necessary:

```java
package com.example;

public class KeyGenerator {
    public static String next() {
        return TSID.Factory.TSID.Factory.getTsid().toString();
    }
}
```
```java
String key = KeyGenerator.next();
```

---

A `TSID.Factory` with a FIXED node identifier:

```java
int node = 256; // max: 2^10
TSID.Factory factory = new TSID.Factory(node);

// use the factory
TSID tsid = factory.generate();
```

---

A `TSID.Factory` with a FIXED node identifier and CUSTOM node bits:

```java
// setup a factory for up to 64 nodes and 65536 ID/ms.
TSID.Factory factory = TSID.Factory.builder()
    .withNodeBits(6)      // max: 20
    .withNode(63)         // max: 2^nodeBits
    .build();

// use the factory
TSID tsid = factory.generate();
```

---

A `TSID.Factory` with a CUSTOM epoch:

```java
// use a CUSTOM epoch that starts from the fall of the Berlin Wall
Instant customEpoch = Instant.parse("1989-11-09T00:00:00Z");
TSID.Factory factory = TSID.Factory.builder().withCustomEpoch(customEpoch).build();

// use the factory
TSID tsid = factory.generate();
```

---

A `TSID.Factory` with `java.util.Random`:

```java
// use a `java.util.Random` instance for fast generation
TSID.Factory factory = TSID.Factory.builder().withRandom(new Random()).build();

// use the factory
TSID tsid = factory.generate();
```

---

A `TSID.Factory` with `RandomGenerator` (JDK 17+):

```java
// use a random function that returns an int value
RandomGenerator random = RandomGenerator.getDefault();
TSID.Factory factory = TSID.Factory.builder()
    .withRandomFunction(() -> random.nextInt())
    .build();

// use the factory
TSID tsid = factory.generate();
```

---

A `TSID.Factory` with `ThreadLocalRandom`:

```java
// use a random function that returns an int value
TSID.Factory factory = TSID.Factory.builder()
    .withRandomFunction(() -> ThreadLocalRandom.current().nextInt())
    .build();

// use the factory
TSID tsid = factory.generate();
```

---

A `TSID.Factory` that creates TSIDs similar to [Twitter Snowflakes](https://github.com/twitter-archive/snowflake):

```java
// Twitter Snowflakes have 5 bits for datacenter ID and 5 bits for worker ID
int datacenter = 1; // max: 2^5-1 = 31
int worker = 1;     // max: 2^5-1 = 31
int node = (datacenter << 5 | worker); // max: 2^10-1 = 1023

// Twitter Epoch is fixed in 1288834974657 (2010-11-04T01:42:54.657Z)
Instant customEpoch = Instant.ofEpochMilli(1288834974657L);

// a function that returns an array with ZEROS, making the factory
// to RESET the counter to ZERO when the millisecond changes
IntFunction<byte[]> randomFunction = (x) -> new byte[x];

// a factory that returns TSIDs similar to Twitter Snowflakes
TSID.Factory factory = TSID.Factory.builder()
		.withRandomFunction(randomFunction)
		.withCustomEpoch(customEpoch)
		.withNode(node)
		.build();

// use the factory
TSID tsid = factory.generate();
```

---

A `TSID.Factory` that creates TSIDs similar to [Discord Snowflakes](https://discord.com/developers/docs/reference#snowflakes):

```java
// Discord Snowflakes have 5 bits for worker ID and 5 bits for process ID
int worker = 1;  // max: 2^5-1 = 31
int process = 1; // max: 2^5-1 = 31
int node = (worker << 5 | process); // max: 2^10-1 = 1023

// Discord Epoch starts in the first millisecond of 2015
Instant customEpoch = Instant.parse("2015-01-01T00:00:00.000Z");

// a factory that returns TSIDs similar to Discord Snowflakes
TSID.Factory factory = TSID.Factory.builder()
		.withCustomEpoch(customEpoch)
		.withNode(node)
		.build();

// use the factory
TSID tsid = factory.generate();
```

---

Benchmark
------------------------------------------------------

This section shows benchmarks comparing `TSID.Factory` to `java.util.UUID`.

```
---------------------------------------------------------------------------
THROUGHPUT (operations/msec)       Mode  Cnt      Score      Error   Units
---------------------------------------------------------------------------
UUID_randomUUID                   thrpt    5   1630,938 ±  183,581  ops/ms
UUID_randomUUID_toString          thrpt    5   1604,916 ±  189,711  ops/ms
-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  - 
Tsid_fast                         thrpt    5  37397,739 ± 1128,756  ops/ms
Tsid_fast_toString                thrpt    5  21144,662 ±  673,939  ops/ms
-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  - 
TSID.Factory_getTsid256            thrpt    5  10727,236 ±  761,920  ops/ms
TSID.Factory_getTsid256_toString   thrpt    5   6813,193 ±  867,041  ops/ms
-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  - 
TSID.Factory_getTsid1024           thrpt    5  12146,561 ± 1533,959  ops/ms
TSID.Factory_getTsid1024_toString  thrpt    5   6507,373 ±  729,444  ops/ms
-  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  - 
TSID.Factory_getTsid4096           thrpt    5  11589,976 ± 1757,076  ops/ms
TSID.Factory_getTsid4096_toString  thrpt    5   6497,042 ± 1339,480  ops/ms
---------------------------------------------------------------------------
Total time: 00:03:22
---------------------------------------------------------------------------
```

Number of threads used in this the benchmark: 4.

System: CPU i7-8565U, 16G RAM, Ubuntu 22.04, JVM 11, rng-tools installed.

To execute the benchmark, run `./benchmark/run.sh`.

Ports and other OSS
------------------------------------------------------

Ports:

| Language | Name |
| -------- | ---- |
| .NET     | [kgkoutis/TSID.Creator.NET](https://github.com/kgkoutis/TSID.Creator.NET) |
| PHP      | [odan/tsid](https://github.com/odan/tsid) |

Other OSS:

| Language | Name |
| -------- | ---- |
| Java     | [fillumina/id-encryptor](https://github.com/fillumina/id-encryptor) |

License
------------------------------------------------------

This library is Open Source software released under the [MIT license](https://opensource.org/licenses/MIT).
