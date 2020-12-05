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
package tv.dotstart.beacon.core.artifact.loader

import tv.dotstart.beacon.core.artifact.error.ArtifactAvailabilityException
import tv.dotstart.beacon.core.artifact.error.ArtifactException
import tv.dotstart.beacon.core.artifact.error.ArtifactSpecificationException
import tv.dotstart.beacon.core.artifact.error.NoSuchArtifactException
import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.core.cache.NoopCacheProvider
import java.net.URI
import java.util.*

/**
 * Provides an abstraction capable of retrieving arbitrary artifacts.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
interface ArtifactLoader {

  /**
   * Instructs this particular loader to retrieve a given artifact and return its contents.
   *
   * Implementations may choose to cache aspects of their location logic but are expected to always
   * return a fresh copy of the desired artifact even if it has previously been fetched. Caching
   * of artifacts is to be handled by the caller.
   *
   * @throws ArtifactAvailabilityException when the artifact or remote machine is not available.
   * @throws ArtifactSpecificationException when the given artifact URI is invalid.
   * @throws NoSuchArtifactException when the given artifact does not exist.
   * @throws ArtifactException when an unknown loader error occurs.
   */
  fun retrieve(uri: URI): ByteArray

  companion object {

    /**
     * Discovers all artifact loader implementations within a given Class-Path.
     */
    fun discover(
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader): List<ArtifactLoader.Factory> =
        ServiceLoader.load(ArtifactLoader.Factory::class.java, classLoader)
            .toList()
  }

  /**
   * Constructs arbitrary artifact loader instances for a given implementation.
   */
  interface Factory {

    /**
     * Identifies one or more URI schemes which are recognized and supported by this particular
     * loader implementation.
     */
    val schemes: List<String>

    /**
     * Constructs a new artifact loader with a given cache provider.
     */
    fun create(cache: CacheProvider = NoopCacheProvider): ArtifactLoader
  }
}
