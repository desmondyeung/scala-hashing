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
import scala.util.Random
import org.scalatest._

trait HashTest extends FunSpecLike {
  val seed32 = Random.nextInt
  val seed64 = Random.nextLong

  def byteBufferOfSize(size: Int, direct: Boolean = false): ByteBuffer = {
    val array = new Array[Byte](size)
    Random.nextBytes(array)

    val bb = if (direct) {
      ByteBuffer.allocateDirect(size)
    } else {
      ByteBuffer.allocate(size)
    }
    bb.order(ByteOrder.nativeOrder)
    bb.put(array)
    bb.rewind
    bb
  }
}
