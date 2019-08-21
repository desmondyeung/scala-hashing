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
import com.desmondyeung.hashing.MurmurHash3_32
import com.google.common.hash.Hashing
import scala.util.hashing.MurmurHash3

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.Throughput))
@Fork(1)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
class MurmurHash3_32Bench {

  var input: Array[Byte] = _

  @Param(Array("8", "128", "512", "1024", "1536", "2048"))
  var inputSize: Int = _

  @Setup
  def prepare: Unit = {
    input = new Array[Byte](inputSize)
    scala.util.Random.nextBytes(input)
  }

  val guava = Hashing.murmur3_32(0)

  @Benchmark
  def com_desmondyeung_hashing: Int = MurmurHash3_32.hashByteArray(input, 0)

  @Benchmark
  def com_google_common_hash: Int = guava.hashBytes(input).asInt

  @Benchmark
  def scala_util_hashing(): Int = MurmurHash3.bytesHash(input, 0)
}
