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

import java.lang.Long.{reverseBytes => swap64}
import java.lang.Integer.{reverseBytes => swap32}
import java.lang.reflect.Field
import java.nio.ByteOrder
import sun.misc.Unsafe

private[hashing] object UnsafeUtil {
  private[this] final val theUnsafe = {
    val field: Field = classOf[Unsafe].getDeclaredField("theUnsafe")
    field.setAccessible(true)
    field.get(null).asInstanceOf[Unsafe]
  }

  private[this] final val isLittleEndian = ByteOrder.nativeOrder == ByteOrder.LITTLE_ENDIAN

  final val ByteArrayBase: Long = theUnsafe.arrayBaseOffset(Array[Byte]().getClass)

  final def getByte(input: Array[Byte], offset: Long): Byte =
    theUnsafe.getByte(input, offset)

  final def getInt(input: Array[Byte], offset: Long): Int =
    if (isLittleEndian) {
      theUnsafe.getInt(input, offset)
    } else {
      swap32(theUnsafe.getInt(input, offset))
    }

  final def getLong(input: Array[Byte], offset: Long): Long =
    if (isLittleEndian) {
      theUnsafe.getLong(input, offset)
    } else {
      swap64(theUnsafe.getLong(input, offset))
    }

  final def getUnsignedByte(input: Array[Byte], offset: Long): Int =
    theUnsafe.getByte(input, offset) & 0xFF

  final def getUnsignedInt(input: Array[Byte], offset: Long): Long =
    if (isLittleEndian) {
      theUnsafe.getInt(input, offset) & 0xFFFFFFFFL
    } else {
      swap32(theUnsafe.getInt(input, offset)) & 0xFFFFFFFFL
    }

  final def copyMemory(src: Array[Byte], srcOffset: Long, dest: Array[Byte], destOffset: Long, length: Int): Unit =
    theUnsafe.copyMemory(src, srcOffset, dest, destOffset, length)
}
