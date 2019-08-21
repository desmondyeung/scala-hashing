/*
 * Copyright 2019 Desmond Yeung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.desmondyeung.bench

import org.openjdk.jmh.annotations._
import com.desmondyeung.hashing.XxHash64
import net.openhft.hashing.LongHashFunction
import net.jpountz.xxhash.XXHashFactory

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.Throughput))
@Fork(1)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
class XxHash64Bench {

  var input: Array[Byte] = _

  @Param(Array("8", "128", "512", "1024", "1536", "2048"))
  var inputSize: Int = _

  @Setup
  def prepare: Unit = {
    input = new Array[Byte](inputSize)
    scala.util.Random.nextBytes(input)
  }

  val jpountzJni    = XXHashFactory.nativeInstance.hash64()
  val jpountzPure   = XXHashFactory.safeInstance.hash64()
  val jpountzUnsafe = XXHashFactory.unsafeInstance.hash64()
  val openhft       = LongHashFunction.xx(0)

  @Benchmark
  def com_desmondyeung_hashing: Long = XxHash64.hashByteArray(input, 0)

  @Benchmark
  def net_jpountz_xxhash_jni: Long = jpountzJni.hash(input, 0, inputSize, 0)

  @Benchmark
  def net_jpountz_xxhash_pure: Long = jpountzPure.hash(input, 0, inputSize, 0)

  @Benchmark
  def net_jpountz_xxhash_unsafe: Long = jpountzUnsafe.hash(input, 0, inputSize, 0)

  @Benchmark
  def net_openhft_hashing: Long = LongHashFunction.xx(0).hashBytes(input)
}
