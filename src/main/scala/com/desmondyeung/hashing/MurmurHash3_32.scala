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

package com.desmondyeung.hashing

import java.lang.Integer.{rotateLeft => rotl32}

/*
 * Scala implementation of Austin Appleby's MurmurHash3.
 * See https://github.com/aappleby/smhasher
 */
object MurmurHash3_32 extends Hash32 {
  val C1 = 0xcc9e2d51
  val C2 = 0x1b873593

  final def hashByte(input: Byte, seed: Int): Int =
    avalance(fmix(seed, input & 0xFF) ^ 1)

  final def hashInt(input: Int, seed: Int): Int =
    avalance(mix(seed, input) ^ 4)

  final def hashLong(input: Long, seed: Int): Int =
    avalance(mix(mix(seed, input.asInstanceOf[Int]), (input >> 32).asInstanceOf[Int]) ^ 8)

  private[hashing] final def fmix(hash: Int, k: Int): Int =
    hash ^ rotl32(k * C1, 15) * C2

  private[hashing] final def mix(hash: Int, k: Int): Int =
    rotl32(fmix(hash, k), 13) * 5 + 0xe6546b64

  private[hashing] final def avalance(hash: Int): Int = {
    val k1 = (hash ^ (hash >>> 16)) * 0x85ebca6b
    val k2 = (k1 ^ (k1 >>> 13)) * 0xc2b2ae35
    k2 ^ (k2 >>> 16)
  }

  private[hashing] final def hashBytes(input: Array[Byte], offset: Long, length: Int, seed: Int): Int = {
    var hash        = seed
    var off         = offset
    var unprocessed = length

    while (unprocessed >= 4) {
      hash = mix(hash, UnsafeUtil.getInt(input, off))
      off += 4
      unprocessed -= 4
    }

    if (unprocessed > 0) {
      var k1 = 0
      if (unprocessed == 3) {
        k1 ^= UnsafeUtil.getUnsignedByte(input, off + 2) << 16
      }
      if (unprocessed >= 2) {
        k1 ^= UnsafeUtil.getUnsignedByte(input, off + 1) << 8
      }
      if (unprocessed >= 1) {
        k1 ^= UnsafeUtil.getUnsignedByte(input, off)
        hash = fmix(hash, k1)
      }
    }

    avalance(hash ^ length)
  }
}
