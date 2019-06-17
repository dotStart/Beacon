/*
 * Copyright (C) 2019 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
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
package tv.dotstart.beacon.util

import com.sangupta.murmur.Murmur3
import tv.dotstart.beacon.BeaconCli
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

/**
 * Provides a caching solution for arbitrary blobs.
 *
 * This solution is primarily used in order to speed up the retrieval of repositories and extraction
 * of icons into a JavaFX compatible directory layout.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object Cache {

  private val logger = Cache::class.logger

  private val base = OperatingSystem.current.storage.resolve("cache")

  init {
    Files.createDirectories(base)
  }

  /**
   * Retrieves the path to a previously cached object.
   *
   * When the cached file does not yet exist or has exceeded the maximum caching duration, the
   * specified generator will be invoked instead.
   */
  operator fun invoke(key: String, generator: (Path) -> Unit): Path {
    val path = base.resolve(
        hash(key))
    if (Files.exists(path) && !BeaconCli.disableCache) {
      val modificationTime = Files.getLastModifiedTime(path).toInstant()
      if (modificationTime.plus(BeaconCli.cacheDuration).isAfter(Instant.now())) {
        logger.debug("""Cache for key "$key" is valid - Returning cached value""")
        return path
      }
    }

    logger.debug("""Cache for key "$key" does not exist or has been invalidated - Regenerating""")
    generator(path)
    return path
  }

  /**
   * Generates a unique hash value for the given cache key.
   */
  private fun hash(key: String) = buildString {
    val data = key.toByteArray(StandardCharsets.UTF_8)
    val hash = Murmur3.hash_x64_128(data, data.size, 424242L)

    hash.forEach {
      append("%016X", it)
    }
  }
}
