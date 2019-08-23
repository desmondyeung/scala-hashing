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

import org.scalatest.FunSpec

class StreamingXxHash32Spec extends FunSpec with StreamingHash32Behaviors {

  def referenceImpl(input: Array[Byte], seed: Int): Int =
    net.jpountz.xxhash.XXHashFactory.fastestInstance.hash32.hash(input, 0, input.length, seed)

  describe("StreamingXxHash32") {
    it should behave like streamingHash32(StreamingXxHash32.apply, referenceImpl)
  }
}
