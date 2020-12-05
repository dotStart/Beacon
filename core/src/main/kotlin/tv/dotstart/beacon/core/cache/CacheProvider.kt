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

import tv.dotstart.beacon.core.cache.error.CacheException
import tv.dotstart.beacon.core.cache.serialize.Serializer
import java.time.Duration

/**
 * Provides a storage agnostic
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 04/12/2020
 */
interface CacheProvider {

  /**
   * Attempts to retrieve a cached value with a given key.
   *
   * When no value with the given key is present within the cache provider, null is returned
   * instead.
   *
   * Callers may optionally include a custom expiration duration. When no expiration duration is
   * given, the provider default is chosen.
   *
   * @throws CacheException when a key cannot be stored.
   */
  fun get(key: String, lifespan: Duration? = null): ByteArray?

  /**
   * Attempts to retrieve a cached value with a given key.
   *
   * When no value with the given key is present within the cache provider, null is returned
   * instead.
   *
   * Callers may optionally include a custom expiration duration. When no expiration duration is
   * given, the provider default is chosen.
   *
   * @throws CacheException when a key cannot be retrieved potentially preventing future operations.
   */
  fun <V : Any> get(key: String,
                    serializer: Serializer<V>,
                    lifespan: Duration? = null): V? = get(key, lifespan)
      ?.let(serializer::decode)

  /**
   * Stores a value with a given key within this cache provider.
   *
   * @throws CacheException when a key cannot be stored.
   */
  fun store(key: String, value: ByteArray)

  /**
   * Stores a value with a given key within this cache provider.
   *
   * @throws CacheException when a key cannot be stored.
   */
  fun <V : Any> store(key: String, serializer: Serializer<V>, value: V) =
      store(key, serializer.encode(value))

  /**
   * Purges a given key from this cache provider.
   *
   * @throws CacheException when a key cannot be stored.
   */
  fun purge(key: String)

  /**
   * Attempts to retrieve a cached value with a given key or populates its contents when it is not
   * present.
   *
   * @throws CacheException when a key cannot be stored or its retrieval fails in a way that would
   * prevent future storage operations.
   */
  fun <V : Any> getOrPopulate(key: String, serializer: Serializer<V>,
                              lifespan: Duration? = null, provider: () -> V): V {
    val cached = this.get(key, serializer, lifespan)
    if (cached != null) {
      return cached
    }

    val populated = provider()
    this.store(key, serializer, populated)

    return populated
  }
}
