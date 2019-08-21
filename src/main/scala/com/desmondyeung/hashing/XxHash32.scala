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
 * Scala implementation of Yann Collet's XxHash32 algoritm.
 * See https://github.com/Cyan4973/xxHash
 */
object XxHash32 extends Hash32 {
  val Prime1 = -1640531535
  val Prime2 = -2048144777
  val Prime3 = -1028477379
  val Prime4 = 668265263
  val Prime5 = 374761393

  final def hashByte(input: Byte, seed: Int): Int =
    avalanche(processByte(seed + Prime5 + 1, input & 0xFF))

  final def hashInt(input: Int, seed: Int): Int =
    avalanche(processInt(seed + Prime5 + 4, input))

  final def hashLong(input: Long, seed: Int): Int =
    avalanche(processInt(processInt(seed + Prime5 + 8, input.asInstanceOf[Int]), (input >> 32).asInstanceOf[Int]))

  private[hashing] final def round(acc: Int, input: Int): Int =
    rotl32(acc + input * Prime2, 13) * Prime1

  private[hashing] final def finalize(hash: Int, input: Array[Byte], offset: Long, length: Int): Int = {
    var h           = hash
    var off         = offset
    var unprocessed = length

    while (unprocessed >= 4) {
      h = processInt(h, UnsafeUtil.getInt(input, off))
      off += 4
      unprocessed -= 4
    }

    while (unprocessed > 0) {
      h = processByte(h, UnsafeUtil.getUnsignedByte(input, off))
      off += 1
      unprocessed -= 1
    }

    avalanche(h)
  }

  private[hashing] final def hashBytes(input: Array[Byte], offset: Long, length: Int, seed: Int): Int = {
    var hash        = 0
    var off         = offset
    var unprocessed = length

    if (length >= 16) {
      var v1 = seed + Prime1 + Prime2
      var v2 = seed + Prime2
      var v3 = seed
      var v4 = seed - Prime1

      do {
        v1 = round(v1, UnsafeUtil.getInt(input, off))
        v2 = round(v2, UnsafeUtil.getInt(input, off + 4L))
        v3 = round(v3, UnsafeUtil.getInt(input, off + 8L))
        v4 = round(v4, UnsafeUtil.getInt(input, off + 12L))

        off += 16
        unprocessed -= 16
      } while (unprocessed >= 16)

      hash = rotl32(v1, 1) + rotl32(v2, 7) + rotl32(v3, 12) + rotl32(v4, 18)

    } else {
      hash = seed + Prime5
    }

    hash += length

    finalize(hash, input, off, unprocessed)
  }

  private final def processByte(hash: Int, input: Int): Int =
    rotl32(hash + input * Prime5, 11) * Prime1

  private final def processInt(hash: Int, input: Int): Int =
    rotl32(hash + input * Prime3, 17) * Prime4

  private final def avalanche(hash: Int): Int = {
    val k1 = (hash ^ (hash >>> 15)) * Prime2
    val k2 = (k1 ^ (k1 >>> 13)) * Prime3
    k2 ^ (k2 >>> 16)
  }
}
