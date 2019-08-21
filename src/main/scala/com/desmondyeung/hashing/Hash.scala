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

import java.nio.ByteBuffer
import sun.nio.ch.DirectBuffer

object HashUtil {
  final def checkBounds(inputLength: Int, offset: Int, length: Int) =
    if (offset < 0 || length < 0 || inputLength - offset < length) {
      throw new IndexOutOfBoundsException()
    }
}

trait Hash32 {
  def hashByte(input: Byte, seed: Int): Int
  def hashInt(input: Int, seed: Int): Int
  def hashLong(input: Long, seed: Int): Int

  final def hashByteArray(input: Array[Byte], seed: Int): Int =
    hashBytes(input, UnsafeUtil.ByteArrayBase, input.length, seed)

  final def hashByteArray(input: Array[Byte], offset: Int, length: Int, seed: Int): Int = {
    HashUtil.checkBounds(input.length, offset, length)
    hashBytes(input, UnsafeUtil.ByteArrayBase + offset, length, seed)
  }

  final def hashByteBuffer(input: ByteBuffer, seed: Int): Int =
    if (input.hasArray) {
      hashBytes(input.array, UnsafeUtil.ByteArrayBase + input.arrayOffset, input.capacity, seed)
    } else {
      hashBytes(null, input.asInstanceOf[DirectBuffer].address, input.capacity, seed)
    }

  final def hashByteBuffer(input: ByteBuffer, offset: Int, length: Int, seed: Int): Int = {
    HashUtil.checkBounds(input.capacity, offset, length)
    if (input.hasArray) {
      hashBytes(input.array, UnsafeUtil.ByteArrayBase + input.arrayOffset + offset, length, seed)
    } else {
      hashBytes(null, input.asInstanceOf[DirectBuffer].address + offset, length, seed)
    }
  }

  private[hashing] def hashBytes(input: Array[Byte], offset: Long, length: Int, seed: Int): Int
}

trait Hash64 {
  def hashByte(input: Byte, seed: Long): Long
  def hashInt(input: Int, seed: Long): Long
  def hashLong(input: Long, seed: Long): Long

  final def hashByteArray(input: Array[Byte], seed: Long): Long =
    hashBytes(input, UnsafeUtil.ByteArrayBase, input.length, seed)

  final def hashByteArray(input: Array[Byte], offset: Int, length: Int, seed: Long): Long = {
    HashUtil.checkBounds(input.length, offset, length)
    hashBytes(input, UnsafeUtil.ByteArrayBase + offset, length, seed)
  }

  final def hashByteBuffer(input: ByteBuffer, seed: Long): Long =
    if (input.hasArray) {
      hashBytes(input.array, UnsafeUtil.ByteArrayBase + input.arrayOffset, input.capacity, seed)
    } else {
      hashBytes(null, input.asInstanceOf[DirectBuffer].address, input.capacity, seed)
    }

  final def hashByteBuffer(input: ByteBuffer, offset: Int, length: Int, seed: Long): Long = {
    HashUtil.checkBounds(input.capacity, offset, length)
    if (input.hasArray) {
      hashBytes(input.array, UnsafeUtil.ByteArrayBase + input.arrayOffset + offset, length, seed)
    } else {
      hashBytes(null, input.asInstanceOf[DirectBuffer].address + offset, length, seed)
    }
  }

  private[hashing] def hashBytes(input: Array[Byte], offset: Long, length: Int, seed: Long): Long
}

trait StreamingHash32 {
  def reset(): Unit
  def value: Int

  final def updateByteArray(input: Array[Byte], offset: Int, length: Int): Unit = {
    HashUtil.checkBounds(input.length, offset, length)
    update(input, UnsafeUtil.ByteArrayBase + offset, length)
  }

  final def updateByteBuffer(input: ByteBuffer, offset: Int, length: Int): Unit = {
    HashUtil.checkBounds(input.capacity, offset, length)
    if (input.hasArray) {
      update(input.array, UnsafeUtil.ByteArrayBase + input.arrayOffset + offset, length)
    } else {
      update(null, input.asInstanceOf[DirectBuffer].address + offset, length)
    }
  }

  private[hashing] def update(input: Array[Byte], offset: Long, length: Int): Unit
}

trait StreamingHash64 {
  def reset(): Unit
  def value: Long

  final def updateByteArray(input: Array[Byte], offset: Int, length: Int): Unit = {
    HashUtil.checkBounds(input.length, offset, length)
    update(input, UnsafeUtil.ByteArrayBase + offset, length)
  }

  final def updateByteBuffer(input: ByteBuffer, offset: Int, length: Int): Unit = {
    HashUtil.checkBounds(input.capacity, offset, length)
    if (input.hasArray) {
      update(input.array, UnsafeUtil.ByteArrayBase + input.arrayOffset + offset, length)
    } else {
      update(null, input.asInstanceOf[DirectBuffer].address + offset, length)
    }
  }

  private[hashing] def update(input: Array[Byte], offset: Long, length: Int): Unit
}
