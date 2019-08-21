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
 * Streaming Scala implementation of Austin Appleby's MurmurHash2_x86_32 algorithm.
 * See https://github.com/aappleby/smhasher
 */
object StreamingMurmurHash3_32 {
  def apply(seed: Int) = new StreamingMurmurHash3_32(seed)
}

class StreamingMurmurHash3_32(seed: Int) extends StreamingHash32 {

  private[this] final val buffer      = new Array[Byte](4)
  private[this] final var hash        = seed
  private[this] final var totalLength = 0
  private[this] final var bufferSize  = 0

  final def reset(): Unit = {
    totalLength = 0
    bufferSize = 0
    hash = seed
  }

  final def value: Int = {
    var h = hash
    if (bufferSize > 0) {
      var k1 = 0
      if (bufferSize == 3) {
        k1 ^= UnsafeUtil.getUnsignedByte(buffer, 18L) << 16
      }
      if (bufferSize >= 2) {
        k1 ^= UnsafeUtil.getUnsignedByte(buffer, 17L) << 8
      }
      if (bufferSize >= 1) {
        k1 ^= UnsafeUtil.getUnsignedByte(buffer, 16L)
        h = MurmurHash3_32.fmix(h, k1)
      }
    }

    MurmurHash3_32.avalance(h ^ totalLength)
  }

  private[hashing] final def update(input: Array[Byte], offset: Long, length: Int): Unit = {
    totalLength += length
    val newBuffSize = bufferSize + length
    if (newBuffSize < 4) {
      UnsafeUtil.copyMemory(input, offset, buffer, bufferSize + UnsafeUtil.ByteArrayBase, length)
      bufferSize = newBuffSize
    } else {
      var off         = offset
      var unprocessed = length
      if (bufferSize > 0) {
        val remaining = 4 - bufferSize
        UnsafeUtil.copyMemory(input, offset, buffer, bufferSize + UnsafeUtil.ByteArrayBase, remaining)
        hash = MurmurHash3_32.mix(hash, UnsafeUtil.getInt(buffer, UnsafeUtil.ByteArrayBase))
        off += remaining
        unprocessed -= remaining
        bufferSize = 0
      }

      if (unprocessed >= 4) {
        do {
          hash = MurmurHash3_32.mix(hash, UnsafeUtil.getInt(input, off))
          off += 4
          unprocessed -= 4
        } while (unprocessed >= 4)
      }

      if (unprocessed > 0) {
        UnsafeUtil.copyMemory(input, off, buffer, UnsafeUtil.ByteArrayBase, unprocessed)
        bufferSize = unprocessed
      }
    }
  }
}
