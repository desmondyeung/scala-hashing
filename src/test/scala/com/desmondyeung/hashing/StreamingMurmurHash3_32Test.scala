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

import scala.util.Random

class StreamingMurmurHash3_32Test extends HashTest {

  def referenceImpl(input: Array[Byte], seed: Int): Int =
    scala.util.hashing.MurmurHash3.bytesHash(input, seed)

  describe("StreamingMurmurHash3_32") {
    it("should hash a Array[Byte]") {
      val mm3   = StreamingMurmurHash3_32(seed32)
      val array = byteBufferOfSize(Random.nextInt(1024)).array

      val expected = referenceImpl(array, seed32)
      val computed = {
        var offset = 0
        while (offset < array.length) {
          val chunkLength = Random.nextInt((array.length - offset) + 1)
          mm3.updateByteArray(array, offset, chunkLength)
          offset += chunkLength
        }
        mm3.value
      }

      assert(expected === computed)
    }

    it("should hash a heap ByteBuffer") {
      val mm3 = StreamingMurmurHash3_32(seed32)
      val bb  = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed32)
      val computed = {
        var offset = 0
        while (offset < bb.capacity) {
          val chunkLength = Random.nextInt((bb.capacity - offset) + 1)
          mm3.updateByteBuffer(bb, offset, chunkLength)
          offset += chunkLength
        }
        mm3.value
      }

      assert(expected === computed)
    }

    it("should hash a direct ByteBuffer") {
      val mm3   = StreamingMurmurHash3_32(seed32)
      val bb    = byteBufferOfSize(Random.nextInt(1024), direct = true)
      val array = new Array[Byte](bb.capacity)
      bb.get(array)

      val expected = referenceImpl(array, seed32)
      val computed = {
        var offset = 0
        while (offset < bb.capacity) {
          val chunkLength = Random.nextInt((bb.capacity - offset) + 1)
          mm3.updateByteBuffer(bb, offset, chunkLength)
          offset += chunkLength
        }
        mm3.value
      }

      assert(expected === computed)
    }
  }
}
