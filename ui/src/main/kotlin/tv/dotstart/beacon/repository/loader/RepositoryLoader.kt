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
package tv.dotstart.beacon.repository.loader

import java.net.URI
import java.nio.file.Path

/**
 * Provides the logic necessary for retrieving a repository through a specific interface.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
interface RepositoryLoader {

  /**
   * Identifies one or more URI schemes which are recognized and supported by this particular
   * loader implementation.
   */
  val schemes: List<String>

  /**
   * Instructs this particular loader to retrieve a given repository index and store its contents at
   * the given location.
   */
  operator fun invoke(uri: URI, target: Path)

  companion object {
    private val loaders = mutableMapOf<String, RepositoryLoader>()

    /**
     * Registers a new loader with this implementation.
     */
    operator fun plusAssign(loader: RepositoryLoader) {
      loader.schemes.forEach {
        this.loaders[it] = loader
      }
    }

    /**
     * Retrieves the repository loader for a given URI.
     */
    operator fun get(location: URI) = this.loaders[location.scheme]
        ?: throw IllegalArgumentException("Unsupported URI scheme: ${location.scheme}")

    /**
     * Executes a compatible loader for the given URI.
     */
    operator fun invoke(location: URI, target: Path) {
      this[location](location, target)
    }
  }
}
