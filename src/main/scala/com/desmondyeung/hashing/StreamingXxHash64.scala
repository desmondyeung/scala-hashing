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
 * Streaming Scala implementation of Yann Collet's XxHash64 algorithm.
 * See https://github.com/Cyan4973/xxHash
 */
object StreamingXxHash64 {
  def apply(seed: Long) = new StreamingXxHash64(seed)
}

final class StreamingXxHash64(seed: Long) extends StreamingHash64 {

  private[this] final val buffer      = new Array[Byte](32)
  private[this] final var v1          = seed + XxHash64.Prime1 + XxHash64.Prime2
  private[this] final var v2          = seed + XxHash64.Prime2
  private[this] final var v3          = seed
  private[this] final var v4          = seed - XxHash64.Prime1
  private[this] final var totalLength = 0
  private[this] final var bufferSize  = 0

  final def reset(): Unit = {
    v1 = seed + XxHash64.Prime1 + XxHash64.Prime2
    v2 = seed + XxHash64.Prime2
    v3 = seed
    v4 = seed - XxHash64.Prime1
    totalLength = 0
    bufferSize = 0
  }

  final def value: Long = {
    var hash = 0L
    if (totalLength >= 32) {
      hash = rotl64(v1, 1) + rotl64(v2, 7) + rotl64(v3, 12) + rotl64(v4, 18)
      hash = XxHash64.mergeRound(hash, v1)
      hash = XxHash64.mergeRound(hash, v2)
      hash = XxHash64.mergeRound(hash, v3)
      hash = XxHash64.mergeRound(hash, v4)
    } else {
      hash = seed + XxHash64.Prime5
    }

    hash += totalLength

    XxHash64.finalize(hash, buffer, UnsafeUtil.ByteArrayBase, bufferSize)
  }

  private[hashing] final def update(input: Array[Byte], offset: Long, length: Int): Unit = {
    totalLength += length
    val newBuffSize = bufferSize + length
    if (newBuffSize < 32) {
      UnsafeUtil.copyMemory(input, offset, buffer, bufferSize + UnsafeUtil.ByteArrayBase, length)
      bufferSize = newBuffSize
    } else {
      var off         = offset
      var unprocessed = length
      if (bufferSize > 0) {
        val remaining = 32 - bufferSize
        UnsafeUtil.copyMemory(input, offset, buffer, bufferSize + UnsafeUtil.ByteArrayBase, remaining)

        v1 = XxHash64.round(v1, UnsafeUtil.getLong(buffer, UnsafeUtil.ByteArrayBase))
        v2 = XxHash64.round(v2, UnsafeUtil.getLong(buffer, UnsafeUtil.ByteArrayBase + 8L))
        v3 = XxHash64.round(v3, UnsafeUtil.getLong(buffer, UnsafeUtil.ByteArrayBase + 16L))
        v4 = XxHash64.round(v4, UnsafeUtil.getLong(buffer, UnsafeUtil.ByteArrayBase + 24L))

        off += remaining
        unprocessed -= remaining
        bufferSize = 0
      }

      if (unprocessed >= 32) {
        do {
          v1 = XxHash64.round(v1, UnsafeUtil.getLong(input, off))
          v2 = XxHash64.round(v2, UnsafeUtil.getLong(input, off + 8L))
          v3 = XxHash64.round(v3, UnsafeUtil.getLong(input, off + 16L))
          v4 = XxHash64.round(v4, UnsafeUtil.getLong(input, off + 24L))

          off += 32
          unprocessed -= 32
        } while (unprocessed >= 32)
      }

      if (unprocessed > 0) {
        UnsafeUtil.copyMemory(input, off, buffer, UnsafeUtil.ByteArrayBase, unprocessed)
        bufferSize = unprocessed
      }
    }
  }
}
