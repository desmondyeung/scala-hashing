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

class MurmurHash3_32Test extends HashTest {

  def referenceImpl(input: Array[Byte], seed: Int): Int =
    scala.util.hashing.MurmurHash3.bytesHash(input, seed)

  describe("MurmurHash3_32") {
    it("should hash a Byte") {
      val bb = byteBufferOfSize(1)

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashByte(bb.array.head, seed32)

      assert(expected === computed)
    }

    it("should hash an Int") {
      val bb = byteBufferOfSize(4)

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashInt(bb.getInt(0), seed32)

      assert(expected === computed)
    }

    it("should hash a Long") {
      val bb = byteBufferOfSize(8)

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashLong(bb.getLong(0), seed32)

      assert(expected === computed)
    }

    it("should hash a Array[Byte]") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashByteArray(bb.array, seed32)

      assert(expected === computed)
    }

    it("should hash a Array[Byte] slice") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashByteArray(bb.array, 0, bb.capacity, seed32)

      assert(expected === computed)
    }

    it("should hash a heap ByteBuffer") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashByteBuffer(bb, seed32)

      assert(expected === computed)
    }

    it("should hash a non-direct ByteBuffer") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashByteBuffer(bb, seed32)

      assert(expected === computed)
    }

    it("should hash a non-direct ByteBuffer slice") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed32)
      val computed = MurmurHash3_32.hashByteBuffer(bb, 0, bb.capacity, seed32)

      assert(expected === computed)
    }

    it("should hash a direct ByteBuffer") {
      val bb    = byteBufferOfSize(Random.nextInt(1024), direct = true)
      val array = new Array[Byte](bb.capacity)
      bb.get(array)

      val expected = referenceImpl(array, seed32)
      val computed = MurmurHash3_32.hashByteBuffer(bb, seed32)

      assert(expected === computed)
    }

    it("should hash a direct ByteBuffer slice") {
      val bb    = byteBufferOfSize(Random.nextInt(1024), direct = true)
      val array = new Array[Byte](bb.capacity)
      bb.get(array)

      val expected = referenceImpl(array, seed32)
      val computed = MurmurHash3_32.hashByteBuffer(bb, 0, bb.capacity, seed32)

      assert(expected === computed)
    }
  }
}
