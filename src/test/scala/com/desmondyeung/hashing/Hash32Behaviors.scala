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
import java.nio.{ByteBuffer, ByteOrder}
import org.scalatest.FunSpec

trait Hash32Behaviors extends HashSpecUtils { this: FunSpec =>

  val seed = Random.nextInt

  def hash32(underTest: Hash32, referenceImpl: (Array[Byte], Int) => Int) = {
    describe("when hashing primitives") {
      it("should correctly hash a Byte") {
        val input = byteBufferOfSize(1)

        val expected = referenceImpl(input.array, seed)
        val computed = underTest.hashByte(input.get(0), seed)

        assert(expected === computed)
      }

      it("should correctly hash an Int") {
        val input = byteBufferOfSize(4)

        val expected = referenceImpl(input.array, seed)
        val computed = underTest.hashInt(input.getInt(0), seed)

        assert(expected === computed)
      }

      it("should correctly hash a Long") {
        val input = byteBufferOfSize(8)

        val expected = referenceImpl(input.array, seed)
        val computed = underTest.hashLong(input.getLong(0), seed)

        assert(expected === computed)
      }
    }

    describe("when hashing a byte array") {
      it("should correctly hash an empty byte array") {
        val input = Array[Byte]()

        val expected = referenceImpl(input, seed)
        val computed = underTest.hashByteArray(input, seed)

        assert(expected === computed)
      }

      it("should correctly hash a byte array") {
        val input = byteBufferOfSize(Random.nextInt(1024)).array

        val expected = referenceImpl(input, seed)
        val computed = underTest.hashByteArray(input, seed)

        assert(expected === computed)
      }

      it("should correctly hash a byte array slice") {
        val input = byteBufferOfSize(Random.nextInt(1024)).array

        val expected = referenceImpl(input, seed)
        val computed = underTest.hashByteArray(input, 0, input.length, seed)

        assert(expected === computed)
      }

      it("should throw IndexOutOfBoundsException if offset is < 0") {
        val input = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          underTest.hashByteArray(input, -1, input.length, seed)
        }
      }

      it("should throw IndexOutOfBoundsException if length is < 0") {
        val input = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          underTest.hashByteArray(input, 0, -1, seed)
        }
      }

      it("should throw IndexOutOfBoundsException if offset > input length") {
        val input = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          underTest.hashByteArray(input, input.length + 1, input.length, seed)
        }
      }

      it("should throw IndexOutOfBoundsException if length > input length") {
        val input = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          underTest.hashByteArray(input, 0, input.length + 1, seed)
        }
      }
    }

    describe("when hashing a ByteBuffer") {
      it("should correctly hash a non-direct byte buffer") {
        val input = byteBufferOfSize(Random.nextInt(1024))

        val expected = referenceImpl(input.array, seed)
        val computed = underTest.hashByteBuffer(input, seed)

        assert(expected === computed)
      }

      it("should correctly hash a non-direct ByteBuffer slice") {
        val input = byteBufferOfSize(Random.nextInt(1024))

        val expected = referenceImpl(input.array, seed)
        val computed = underTest.hashByteBuffer(input, 0, input.capacity, seed)

        assert(expected === computed)
      }

      it("should correctly hash a direct ByteBuffer") {
        val input = byteBufferOfSize(Random.nextInt(1024), direct = true)
        val array = new Array[Byte](input.capacity)
        input.get(array)

        val expected = referenceImpl(array, seed)
        val computed = underTest.hashByteBuffer(input, seed)

        assert(expected === computed)
      }

      it("should correctly hash a direct ByteBuffer slice") {
        val input = byteBufferOfSize(Random.nextInt(1024), direct = true)
        val array = new Array[Byte](input.capacity)
        input.get(array)

        val expected = referenceImpl(array, seed)
        val computed = underTest.hashByteBuffer(input, 0, input.capacity, seed)

        assert(expected === computed)
      }

      it("should throw IndexOutOfBoundsException if offset is < 0") {
        intercept[IndexOutOfBoundsException] {
          val input = byteBufferOfSize(1)
          underTest.hashByteBuffer(input, -1, input.capacity, seed)
        }
      }

      it("should throw IndexOutOfBoundsException if length is < 0") {
        intercept[IndexOutOfBoundsException] {
          val input = byteBufferOfSize(1)
          underTest.hashByteBuffer(input, 0, -1, seed)
        }
      }

      it("should throw IndexOutOfBoundsException if offset > input length") {
        intercept[IndexOutOfBoundsException] {
          val input = byteBufferOfSize(1)
          underTest.hashByteBuffer(input, input.capacity + 1, input.capacity, seed)
        }
      }

      it("should throw IndexOutOfBoundsException if length > input length") {
        intercept[IndexOutOfBoundsException] {
          val input = byteBufferOfSize(1)
          underTest.hashByteBuffer(input, 0, input.capacity + 1, seed)
        }
      }
    }
  }
}
