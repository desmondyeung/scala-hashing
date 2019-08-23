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

trait StreamingHash32Behaviors extends HashSpecUtils { this: FunSpec =>

  val seed = Random.nextInt

  def streamingHash32(underTest: (Int) => StreamingHash32, referenceImpl: (Array[Byte], Int) => Int) = {
    describe("when hashing a byte array") {
      it("should correctly hash a empty byte array") {
        val checksum = underTest(seed)
        val array    = byteBufferOfSize(0).array

        val expected = referenceImpl(array, seed)

        assert(expected === checksum.value)
      }

      it("should correctly hash a byte array in chunks") {
        val checksum = underTest(seed)
        val array    = byteBufferOfSize(Random.nextInt(1024)).array

        val expected = referenceImpl(array, seed)
        val computed = {
          var offset = 0
          while (offset < array.length) {
            val chunkLength = Random.nextInt((array.length - offset) + 1)
            checksum.updateByteArray(array, offset, chunkLength)
            offset += chunkLength
          }
          checksum.value
        }

        assert(expected === computed)
      }

      it("should throw IndexOutOfBoundsException if offset is < 0") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteArray(input, -1, input.length)
        }
      }

      it("should throw IndexOutOfBoundsException if length is < 0") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteArray(input, 0, -1)
        }
      }

      it("should throw IndexOutOfBoundsException if offset > input length") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteArray(input, input.length + 1, input.length)
        }
      }

      it("should throw IndexOutOfBoundsException if length > input length") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1).array
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteArray(input, 0, input.length + 1)
        }
      }
    }

    describe("when hashing a ByteBuffer") {
      it("should correctly hash a non-direct ByteBuffer in chunks") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(Random.nextInt(1024))

        val expected = referenceImpl(input.array, seed)
        val computed = {
          var offset = 0
          while (offset < input.capacity) {
            val chunkLength = Random.nextInt((input.capacity - offset) + 1)
            checksum.updateByteBuffer(input, offset, chunkLength)
            offset += chunkLength
          }
          checksum.value
        }

        assert(expected === computed)
      }

      it("should correctly hash a direct ByteBuffer in chunks") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(Random.nextInt(1024), direct = true)
        val array    = new Array[Byte](input.capacity)
        input.get(array)

        val expected = referenceImpl(array, seed)
        val computed = {
          var offset = 0
          while (offset < input.capacity) {
            val chunkLength = Random.nextInt((input.capacity - offset) + 1)
            checksum.updateByteBuffer(input, offset, chunkLength)
            offset += chunkLength
          }
          checksum.value
        }

        assert(expected === computed)
      }

      it("should throw IndexOutOfBoundsException if offset is < 0") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1)
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteBuffer(input, -1, input.capacity)
        }
      }

      it("should throw IndexOutOfBoundsException if length is < 0") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1)
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteBuffer(input, 0, -1)
        }
      }

      it("should throw IndexOutOfBoundsException if offset > input length") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1)
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteBuffer(input, input.capacity + 1, input.capacity)
        }
      }

      it("should throw IndexOutOfBoundsException if length > input length") {
        val checksum = underTest(seed)
        val input    = byteBufferOfSize(1)
        intercept[IndexOutOfBoundsException] {
          checksum.updateByteBuffer(input, 0, input.capacity + 1)
        }
      }
    }

    it("should be idempotent when calculating the final value") {
      val checksum = underTest(seed)
      val array    = byteBufferOfSize(Random.nextInt(1024)).array

      var offset = 0
      while (offset < array.length) {
        val chunkLength = Random.nextInt((array.length - offset) + 1)
        checksum.updateByteArray(array, offset, chunkLength)
        offset += chunkLength
      }

      assert(checksum.value === checksum.value)
    }

    it("should allow state to be reset") {
      val checksum = underTest(seed)
      val array    = byteBufferOfSize(Random.nextInt(1024)).array

      var expected = referenceImpl(array, seed)
      var computed = {
        var offset = 0
        while (offset < array.length) {
          val chunkLength = Random.nextInt((array.length - offset) + 1)
          checksum.updateByteArray(array, offset, chunkLength)
          offset += chunkLength
        }
        checksum.value
      }

      assert(expected === computed)

      checksum.reset()

      Random.nextBytes(array)

      expected = referenceImpl(array, seed)
      computed = {
        var offset = 0
        while (offset < array.length) {
          val chunkLength = Random.nextInt((array.length - offset) + 1)
          checksum.updateByteArray(array, offset, chunkLength)
          offset += chunkLength
        }
        checksum.value
      }

      assert(expected === computed)
    }
  }
}
