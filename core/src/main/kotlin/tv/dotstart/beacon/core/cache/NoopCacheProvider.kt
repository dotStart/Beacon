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
package tv.dotstart.beacon.core.cache

import tv.dotstart.beacon.core.cache.serialize.Serializer
import java.time.Duration

/**
 * Provides a cache provider which does not store or retrieve any values.
 *
 * @author Johannes Donath
 * @date 05/12/2020
 */
object NoopCacheProvider : CacheProvider {

  override fun <V : Any> get(key: String, serializer: Serializer<V>,
                             expirationPeriod: Duration?): V? = null

  override fun <V : Any> store(key: String, serializer: Serializer<V>, value: V) = Unit

  override fun purge(key: String) = Unit
}
