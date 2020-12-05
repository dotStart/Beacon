/*
 * Copyright 2020 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.beacon.core.cache.serialize

import com.google.protobuf.Message

/**
 * Provides a cache serializer for protobuf messages.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 05/12/2020
 */
class ProtobufSerializer<M : Message>(private val template: M) : Serializer<M> {

  override fun encode(value: M): ByteArray = value.toByteArray()

  @Suppress("UNCHECKED_CAST")
  override fun decode(value: ByteArray): M = this.template.parserForType.parseFrom(value) as M
}
