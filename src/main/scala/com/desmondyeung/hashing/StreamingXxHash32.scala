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
 * Streaming Scala implementation of Yann Collet's XxHash32 algorithm.
 * See https://github.com/Cyan4973/xxHash
 */
object StreamingXxHash32 {
  def apply(seed: Int) = new StreamingXxHash32(seed)
}

final class StreamingXxHash32(seed: Int) extends StreamingHash32 {

  private[this] final val buffer      = new Array[Byte](16)
  private[this] final var v1          = seed + XxHash32.Prime1 + XxHash32.Prime2
  private[this] final var v2          = seed + XxHash32.Prime2
  private[this] final var v3          = seed
  private[this] final var v4          = seed - XxHash32.Prime1
  private[this] final var totalLength = 0
  private[this] final var bufferSize  = 0

  final def reset(): Unit = {
    v1 = seed + XxHash32.Prime1 + XxHash32.Prime2
    v2 = seed + XxHash32.Prime2
    v3 = seed
    v4 = seed - XxHash32.Prime1
    totalLength = 0
    bufferSize = 0
  }

  final def value: Int = {
    var hash = 0
    if (totalLength >= 16) {
      hash = rotl32(v1, 1) + rotl32(v2, 7) + rotl32(v3, 12) + rotl32(v4, 18)
    } else {
      hash = seed + XxHash32.Prime5
    }

    hash += totalLength

    XxHash32.finalize(hash, buffer, UnsafeUtil.ByteArrayBase, bufferSize)
  }

  private[hashing] final def update(input: Array[Byte], offset: Long, length: Int): Unit = {
    totalLength += length
    val newBuffSize = bufferSize + length
    if (newBuffSize < 16) {
      UnsafeUtil.copyMemory(input, offset, buffer, bufferSize + UnsafeUtil.ByteArrayBase, length)
      bufferSize = newBuffSize
    } else {
      var off         = offset
      var unprocessed = length
      if (bufferSize > 0) {
        val remaining = 16 - bufferSize
        UnsafeUtil.copyMemory(input, offset, buffer, bufferSize + UnsafeUtil.ByteArrayBase, remaining)

        v1 = XxHash32.round(v1, UnsafeUtil.getInt(buffer, UnsafeUtil.ByteArrayBase))
        v2 = XxHash32.round(v2, UnsafeUtil.getInt(buffer, UnsafeUtil.ByteArrayBase + 4L))
        v3 = XxHash32.round(v3, UnsafeUtil.getInt(buffer, UnsafeUtil.ByteArrayBase + 8L))
        v4 = XxHash32.round(v4, UnsafeUtil.getInt(buffer, UnsafeUtil.ByteArrayBase + 12L))

        off += remaining
        unprocessed -= remaining
        bufferSize = 0
      }

      if (unprocessed >= 16) {
        do {
          v1 = XxHash32.round(v1, UnsafeUtil.getInt(input, off))
          v2 = XxHash32.round(v2, UnsafeUtil.getInt(input, off + 4L))
          v3 = XxHash32.round(v3, UnsafeUtil.getInt(input, off + 8L))
          v4 = XxHash32.round(v4, UnsafeUtil.getInt(input, off + 12L))

          off += 16
          unprocessed -= 16
        } while (unprocessed >= 16)
      }

      if (unprocessed > 0) {
        UnsafeUtil.copyMemory(input, off, buffer, UnsafeUtil.ByteArrayBase, unprocessed)
        bufferSize = unprocessed
      }
    }
  }
}
