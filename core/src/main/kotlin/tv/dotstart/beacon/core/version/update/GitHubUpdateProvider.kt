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
package tv.dotstart.beacon.core.version.update

import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.core.version.InstabilityType
import tv.dotstart.beacon.core.version.Version
import tv.dotstart.beacon.github.operations.RepositoryOperations
import java.net.MalformedURLException
import java.net.URL

/**
 * Provides update check logic for libraries and applications via GitHub.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 12/12/2020
 */
class GitHubUpdateProvider(
    override val current: Version,
    override val channel: InstabilityType,
    val repository: RepositoryOperations) : UpdateProvider {

  companion object {

    private val logger by logManager()
  }

  override fun check(): Update? {
    return try {
      this.repository.listReleases()
          .mapNotNull {
            try {
              Version.parse(it.tagName.removePrefix("v")) to URL(it.url)
            } catch (ex: IllegalArgumentException) {
              logger.debug("Ignoring invalid release: ${it.tagName}", ex)
              null
            } catch (ex: MalformedURLException) {
              logger.debug("Ignoring invalid release: ${it.tagName}", ex)
              null
            }
          }
          .filter { (version, _) -> version.instabilityType >= this.channel }
          .sortedByDescending { (version, _) -> version }
          .find { (version, _) -> version > this.current }
          ?.let { (version, releaseUrl) ->
            Update(version, releaseUrl)
          }
    } catch (ex: Throwable) {
      logger.error("Failed to perform update check", ex)
      null
    }
  }
}
