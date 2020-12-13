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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

/**
 * Provides a Jackson based serializer implementation.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 13/12/2020
 */
class JsonSerializer<T : Any>(
    private val valueType: KClass<T>,
    private val mapper: ObjectMapper) : Serializer<T> {

  override fun encode(value: T): ByteArray = this.mapper.writeValueAsBytes(value)

  override fun decode(value: ByteArray): T = this.mapper.readValue(value, this.valueType.java)
}

/**
 * Creates a new JSON serializer for a given target type.
 *
 * When no object mapper is given, a standard mapper is constructed using the modules present within
 * the application classpath.
 *
 * Note: The resulting object mapper is configured to ignore unknown properties in order to
 * facilitate support with differently structured models (even though the cache is typically purged
 * upon version migration).
 */
inline fun <reified T : Any> jsonSerializer(mapper: ObjectMapper? = null): JsonSerializer<T> {
  val actualMapper = mapper
      ?: jacksonObjectMapper()
          .findAndRegisterModules()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  return JsonSerializer(T::class, actualMapper)
}
