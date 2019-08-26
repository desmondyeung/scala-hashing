# Scala-Hashing

[![Build Status](https://api.travis-ci.com/desmondyeung/scala-hashing.svg)](https://travis-ci.com/desmondyeung/scala-hashing)
[![codecov.io](http://codecov.io/github/desmondyeung/scala-hashing/coverage.svg?branch=master)](http://codecov.io/github/desmondyeung/scala-hashing?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.desmondyeung.hashing/scala-hashing_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.desmondyeung.hashing/scala-hashing_2.13)

## Overview
Fast non-cryptographic hash functions for Scala. This library provides APIs for computing 32-bit and 64-bit hashes.

Currently implemented hash functions
* [MurmurHash3](https://github.com/aappleby/smhasher) (32-bit)
* [XxHash](https://github.com/Cyan4973/xxHash) (32-bit and 64-bit)

Hash functions in this library can be access via either a standard API for hashing primitives, byte arrays, or Java ByteBuffers (direct and non-direct), or a streaming API for hashing stream-like objects such as InputStreams, Java NIO Channels, or Akka Streams. Hash functions should produce consistent output regardless of platform or endianness.

This library uses the `sun.misc.Unsafe` API internally. I might explore using the `VarHandle` API introduced in Java 9 in the future, but am currently still supporting Java 8.

## Performance

Benchmarked against various other open-source implementations
* [Guava](https://github.com/google/guava) (MurmurHash3)
* [LZ4 Java](https://github.com/lz4/lz4-java) (XxHash32 and XxHash64 - Includes JNI binding, pure Java, and Java+Unsafe implementations)
* [Scala](https://github.com/scala/scala) (Scala's built-in `scala.util.hashing.MurmurHash3`)
* [Zero-Allocation-Hashing](https://github.com/OpenHFT/Zero-Allocation-Hashing) (XxHash64)

### MurmurHash3_32
![MurmurHash3_32](https://github.com/desmondyeung/scala-hashing/blob/master/bench/src/main/resource/results/MurmurHash3_32.png)

### XxHash32
![XxHash32](https://github.com/desmondyeung/scala-hashing/blob/master/bench/src/main/resource/results/XxHash32.png)

### XxHash64
![XxHash64](https://github.com/desmondyeung/scala-hashing/blob/master/bench/src/main/resource/results/XxHash64.png)


### Running Locally

Benchmarks are located in the `bench` subproject and can be run using the [sbt-jmh](https://github.com/ktoso/sbt-jmh) plugin.

To run all benchmarks with default settings
```sbt
bench/jmh:run
```
To run a specific benchmark with custom settings
```sbt
bench/jmh:run -f 2 -wi 5 -i 5 XxHash64Bench
```

## Getting Started

```scala
libraryDependencies += "com.desmondyeung.hashing" %% "scala-hashing" % "0.1.0"
```

### Examples

This library defines the interfaces `Hash32` and `StreamingHash32` for computing 32-bit hashes and `Hash64` and `StreamingHash64` for computing 64-bit hashes. Classes extending `StreamingHash32` or `StreamingHash64` are not thread-safe.

The public API for `Hash64` and `StreamingHash64` can be seen below
```scala
trait Hash64 {
  def hashByte(input: Byte, seed: Long): Long
  def hashInt(input: Int, seed: Long): Long
  def hashLong(input: Long, seed: Long): Long
  def hashByteArray(input: Array[Byte], seed: Long): Long
  def hashByteArray(input: Array[Byte], offset: Int, length: Int, seed: Long): Long
  def hashByteBuffer(input: ByteBuffer, seed: Long): Long
  def hashByteBuffer(input: ByteBuffer, offset: Int, length: Int, seed: Long): Long
}

trait StreamingHash64 {
  def reset(): Unit
  def value: Long
  def updateByteArray(input: Array[Byte], offset: Int, length: Int): Unit
  def updateByteBuffer(input: ByteBuffer, offset: Int, length: Int): Unit
}
```

Using the standard API
```scala
import com.desmondyeung.hashing.XxHash64
import java.nio.ByteBuffer

// hash a long
val hash = XxHash64.hashLong(123, seed = 0)

// hash a Array[Byte]
val hash = XxHash64.hashByteArray(Array[Byte](123), seed = 0)

// hash a ByteBuffer
val hash = XxHash64.hashByteBuffer(ByteBuffer.wrap(Array[Byte](123)), seed = 0)
```

Using the streaming API
```scala
import com.desmondyeung.hashing.StreamingXxHash64
import java.nio.ByteBuffer
import java.io.FileInputStream

val checksum = StreamingXxHash64(seed = 0)
val channel  = new FileInputStream("/path/to/file.txt").getChannel
val chunk    = ByteBuffer.allocate(1024)

var bytesRead = channel.read(chunk)
while (bytesRead > 0) {
  checksum.updateByteBuffer(chunk, 0, bytesRead)
  chunk.rewind
  bytesRead = channel.read(chunk)
}

val hash = checksum.value
```

## License

Licensed under the Apache License, Version 2.0 (the "License").
