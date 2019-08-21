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

import java.nio.{ByteBuffer, ByteOrder}
import org.scalatest._
import scala.util.Random

class XxHash64Test extends HashTest {

  def referenceImpl(input: Array[Byte], seed: Long): Long =
    net.jpountz.xxhash.XXHashFactory.fastestInstance.hash64.hash(input, 0, input.length, seed)

  describe("XxHash64") {
    it("should hash a Byte") {
      val bb = byteBufferOfSize(1)

      val expected = referenceImpl(bb.array, seed64)
      val computed = XxHash64.hashByte(bb.array.head, seed64)

      assert(expected === computed)
    }

    it("should hash an Int") {
      val bb = byteBufferOfSize(4)

      val expected = referenceImpl(bb.array, seed64)
      val computed = XxHash64.hashInt(bb.getInt(0), seed64)

      assert(expected === computed)
    }

    it("should hash a Long") {
      val bb = byteBufferOfSize(8)

      val expected = referenceImpl(bb.array, seed64)
      val computed = XxHash64.hashLong(bb.getLong(0), seed64)

      assert(expected === computed)
    }

    it("should hash a Array[Byte]") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed64)
      val computed = XxHash64.hashByteArray(bb.array, seed64)

      assert(expected === computed)
    }

    it("should hash a Array[Byte] slice") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed64)
      val computed = XxHash64.hashByteArray(bb.array, 0, bb.capacity, seed64)

      assert(expected === computed)
    }

    it("should hash a non-direct ByteBuffer") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed64)
      val computed = XxHash64.hashByteBuffer(bb, seed64)

      assert(expected === computed)
    }

    it("should hash a non-direct ByteBuffer slice") {
      val bb = byteBufferOfSize(Random.nextInt(1024))

      val expected = referenceImpl(bb.array, seed64)
      val computed = XxHash64.hashByteBuffer(bb, 0, bb.capacity, seed64)

      assert(expected === computed)
    }

    it("should hash a direct ByteBuffer") {
      val bb    = byteBufferOfSize(Random.nextInt(1024), direct = true)
      val array = new Array[Byte](bb.capacity)
      bb.get(array)

      val expected = referenceImpl(array, seed64)
      val computed = XxHash64.hashByteBuffer(bb, seed64)

      assert(expected === computed)
    }

    it("should hash a direct ByteBuffer slice") {
      val bb    = byteBufferOfSize(Random.nextInt(1024), direct = true)
      val array = new Array[Byte](bb.capacity)
      bb.get(array)

      val expected = referenceImpl(array, seed64)
      val computed = XxHash64.hashByteBuffer(bb, 0, bb.capacity, seed64)

      assert(expected === computed)
    }
  }
}
