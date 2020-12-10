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
package tv.dotstart.beacon.core.cache.filesystem

import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.core.cache.error.CacheException
import tv.dotstart.beacon.core.cache.filesystem.path.MessageDigestPathProvider
import tv.dotstart.beacon.core.cache.filesystem.path.PathProvider
import tv.dotstart.beacon.core.delegate.logManager
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.streams.asSequence

/**
 * Provides a filesystem based caching solution.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 05/12/2020
 */
class FileSystemCache(

    /**
     * Defines the location at which cache files are stored.
     *
     * If this directory does not exist, it will be created when the first cache file is written to
     * disk.
     */
    val root: Path,

    /**
     * Identifies the amount of time that cache keys remain valid unless explicitly overriden.
     */
    val expirationPeriod: Duration? = null,

    /**
     * Defines the clock by which cache lifespans are computed.
     */
    val clock: Clock = Clock.systemUTC(),

    /**
     * Defines a path provider which resolves the respective location for each cache key relative
     * to the cache root.
     */
    val pathProvider: PathProvider = MessageDigestPathProvider.sha256) : CacheProvider {

  companion object {

    private val logger by logManager()
  }

  override fun get(key: String, lifespan: Duration?): ByteArray? {
    val location = this.pathProvider.resolve(this.root, key)
    if (Files.notExists(location)) {
      logger.debug("Cache key $key is not present")
      return null
    }

    val keyLifespan = lifespan ?: this.expirationPeriod
    if (keyLifespan != null) {
      val createdAt = Files.getLastModifiedTime(location)
          .toInstant()
      val spentLifetime = Duration.between(createdAt, Instant.now(this.clock))

      if (spentLifetime >= keyLifespan) {
        logger.debug(
            "Cache key $key has expired (lifespan $spentLifetime exceeds desired duration of $keyLifespan)")
        return null
      }
    }

    return try {
      Files.readAllBytes(location)
    } catch (ex: IOException) {
      throw CacheException(
          "Failed to retrieve cache entry with key \"$key\" from storage path $location", ex)
    }
  }

  override fun store(key: String, value: ByteArray) {
    val location = this.pathProvider.resolve(this.root, key)

    try {
      Files.createDirectories(location.parent)
      Files.write(location, value)
    } catch (ex: IOException) {
      throw CacheException(
          "Failed to store cache entry with key \"$key\" in storage path $location", ex)
    }
  }

  override fun purgeAll() {
    Files.walk(this.root).asSequence()
        .sortedWith(Comparator.reverseOrder())
        .forEach { Files.deleteIfExists(it) }
  }

  override fun purge(key: String) {
    val location = this.pathProvider.resolve(this.root, key)

    try {
      Files.deleteIfExists(location)
    } catch (ex: IOException) {
      throw CacheException(
          "Failed to purge cache entry with key \"$key\" in storage path $location", ex)
    }
  }
}
