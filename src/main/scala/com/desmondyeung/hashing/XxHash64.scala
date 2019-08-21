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

import java.lang.Long.{rotateLeft => rotl64}

/*
 * Scala implementation of Yann Collet's XxHash64 algorithm.
 * See https://github.com/Cyan4973/xxHash
 */
object XxHash64 extends Hash64 {
  val Prime1 = -7046029288634856825L
  val Prime2 = -4417276706812531889L
  val Prime3 = 1609587929392839161L
  val Prime4 = -8796714831421723037L
  val Prime5 = 2870177450012600261L

  final def hashByte(input: Byte, seed: Long): Long =
    avalanche(processByte(seed + Prime5 + 1L, input & 0xFF))

  final def hashInt(input: Int, seed: Long): Long =
    avalanche(processInt(seed + Prime5 + 4L, input & 0xFFFFFFFFL))

  final def hashLong(input: Long, seed: Long): Long =
    avalanche(processLong(seed + Prime5 + 8L, input))

  private[hashing] final def round(acc: Long, input: Long): Long =
    rotl64(acc + input * Prime2, 31) * Prime1

  private[hashing] final def mergeRound(acc: Long, v: Long): Long =
    (acc ^ round(0L, v)) * Prime1 + Prime4

  private[hashing] final def finalize(hash: Long, input: Array[Byte], offset: Long, length: Int): Long = {
    var h           = hash
    var off         = offset
    var unprocessed = length
    while (unprocessed >= 8) {
      h = processLong(h, UnsafeUtil.getLong(input, off))
      off += 8
      unprocessed -= 8
    }

    if (unprocessed >= 4) {
      h = processInt(h, UnsafeUtil.getUnsignedInt(input, off))
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

  private[hashing] final def hashBytes(input: Array[Byte], offset: Long, length: Int, seed: Long): Long = {
    var hash        = 0L
    var off         = offset
    var unprocessed = length

    if (length >= 32) {
      var v1 = seed + Prime1 + Prime2
      var v2 = seed + Prime2
      var v3 = seed
      var v4 = seed - Prime1

      do {
        v1 = round(v1, UnsafeUtil.getLong(input, off))
        v2 = round(v2, UnsafeUtil.getLong(input, off + 8L))
        v3 = round(v3, UnsafeUtil.getLong(input, off + 16L))
        v4 = round(v4, UnsafeUtil.getLong(input, off + 24L))

        off += 32
        unprocessed -= 32
      } while (unprocessed >= 32)

      hash = rotl64(v1, 1) + rotl64(v2, 7) + rotl64(v3, 12) + rotl64(v4, 18)
      hash = mergeRound(hash, v1)
      hash = mergeRound(hash, v2)
      hash = mergeRound(hash, v3)
      hash = mergeRound(hash, v4)
    } else {
      hash = seed + Prime5
    }

    hash += length

    finalize(hash, input, off, unprocessed)
  }

  private final def processByte(hash: Long, input: Int): Long =
    rotl64(hash ^ input * Prime5, 11) * Prime1

  private final def processInt(hash: Long, input: Long): Long =
    rotl64(hash ^ input * Prime1, 23) * Prime2 + Prime3

  private final def processLong(hash: Long, input: Long): Long =
    rotl64(hash ^ round(0, input), 27) * Prime1 + Prime4

  private final def avalanche(hash: Long): Long = {
    val k1 = (hash ^ (hash >>> 33)) * Prime2
    val k2 = (k1 ^ (k1 >>> 29)) * Prime3
    k2 ^ (k2 >>> 32)
  }
}
