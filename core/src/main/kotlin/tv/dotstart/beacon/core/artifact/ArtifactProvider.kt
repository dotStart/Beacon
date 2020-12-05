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
package tv.dotstart.beacon.core.artifact

import tv.dotstart.beacon.core.artifact.error.*
import tv.dotstart.beacon.core.artifact.loader.ArtifactLoader
import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.core.cache.NoopCacheProvider
import java.net.URI

/**
 * Provides arbitrary artifacts from loader implementations in accordance with their respective URI
 * scheme.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 05/12/2020
 */
class ArtifactProvider(
    cacheProvider: CacheProvider = NoopCacheProvider,
    loaders: List<ArtifactLoader.Factory>) {

  private val loaderMap = loaders
      .map { it to lazy { it.create(cacheProvider) } }
      .flatMap { (factory, loader) ->
        factory.schemes
            .map { it to loader }
      }
      .toMap()

  companion object {

    /**
     * Creates an artifact provider for the loaders within the application Class-Path.
     */
    fun forDiscoveredLoaders(
        cacheProvider: CacheProvider = NoopCacheProvider,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader) = ArtifactProvider(
        cacheProvider,
        ArtifactLoader.discover(classLoader)
    )
  }

  /**
   * Retrieves a given artifact from one of the known configured artifact loaders.
   *
   * @throws ArtifactAvailabilityException when the given artifact or the remote machine is
   * unavailable.
   * @throws ArtifactSchemeException when the given artifact scheme is unknown.
   * @throws ArtifactSpecificationException when the given artifact URI is malformed.
   * @throws NoSuchArtifactException when the given artifact does not exist.
   * @throws ArtifactException when an unknown loader error occurs.
   */
  fun retrieve(location: URI): ByteArray {
    val loader = this.loaderMap[location.scheme]?.value
        ?: throw ArtifactSchemeException("No such loader: ${location.scheme}")

    return loader.retrieve(location)
  }
}
