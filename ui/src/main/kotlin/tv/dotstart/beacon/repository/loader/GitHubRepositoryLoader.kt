/*
 * Copyright 2019 Johannes Donath <johannesd@torchmind.com>
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
package tv.dotstart.beacon.repository.loader

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.http.client.fluent.Request
import tv.dotstart.beacon.BeaconMetadata
import tv.dotstart.beacon.repository.error.IllegalRepositorySpecificationException
import tv.dotstart.beacon.repository.error.RepositoryAvailabilityException
import tv.dotstart.beacon.util.Cache
import tv.dotstart.beacon.util.logger
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.file.Path

/**
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object GitHubRepositoryLoader : RepositoryLoader {

  private val logger = GitHubRepositoryLoader::class.logger

  override val schemes = listOf("github")

  override fun invoke(uri: URI, target: Path) {
    val owner = uri.host
    val repository = uri.path.substring(1)
    val asset = uri.fragment

    if (owner.isEmpty()) {
      throw IllegalRepositorySpecificationException(
          "Invalid GitHub asset identifier: Owner cannot be empty")
    }
    if (repository.isEmpty()) {
      throw IllegalRepositorySpecificationException(
          "Invalid GitHub asset identifier: Repository cannot be empty")
    }
    if (asset.isEmpty()) {
      throw IllegalRepositorySpecificationException(
          "Invalid GitHub asset identifier: Asset cannot be empty")
    }

    val apiUri = "https://api.github.com/repos/$owner/$repository/releases"
    val releaseFile = Cache(apiUri) {
      logger.debug("Fetching release information for $owner:$repository ($asset)")

      try {
        Request.Get(apiUri)
            .setHeader("User-Agent", BeaconMetadata.userAgent)
            .setHeader("Accept", "application/vnd.github.v3+json")
            .execute()
            .saveContent(it.toFile())
      } catch (ex: IOException) {
        throw RepositoryAvailabilityException("Failed to establish communication with GitHub", ex)
      }
    }

    val (release, file) = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue<List<Release>>(releaseFile.toFile())
        .flatMap { release ->
          release.assets
              .map { release to it }
        }
        .find { it.second.name == asset }
        ?: throw RepositoryAvailabilityException(
            "Cannot find release with asset \"$asset\" in $owner/$repository")

    logger.debug(
        "Fetching release asset ${file.id} from release ${release.tagName} (${release.id})")
    Request.Get(file.url.toURI())
        .setHeader("User-Agent", BeaconMetadata.userAgent)
        .execute()
        .saveContent(target.toFile())
  }

  data class Release(
      val id: String,
      @JsonProperty("tag_name")
      val tagName: String,
      val assets: List<Asset>)

  data class Asset(
      val id: String,
      val name: String,
      @JsonProperty("browser_download_url")
      val url: URL)
}
