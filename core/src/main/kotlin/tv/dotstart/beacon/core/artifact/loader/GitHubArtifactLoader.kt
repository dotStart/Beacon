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
package tv.dotstart.beacon.core.artifact.loader

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.http.client.fluent.Request
import tv.dotstart.beacon.core.BeaconCoreMetadata
import tv.dotstart.beacon.core.artifact.error.ArtifactSpecificationException
import tv.dotstart.beacon.core.artifact.error.ArtifactAvailabilityException
import tv.dotstart.beacon.core.artifact.error.NoSuchArtifactException
import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.core.cache.serialize.StringSerializer
import tv.dotstart.beacon.core.delegate.logManager
import java.io.IOException
import java.net.URI
import java.net.URL

/**
 * Retrieves artifacts from GitHub.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class GitHubArtifactLoader(private val cache: CacheProvider) : ArtifactLoader {

  companion object {
    private val logger by logManager()

    /**
     * Defines the GitHub API revision which is transmitted along with each respective request.
     */
    private const val apiVersion = "v3"
  }

  override fun retrieve(uri: URI): ByteArray {
    val owner = uri.host
    val repository = uri.path.substring(1)
    val asset = uri.fragment

    if (owner.isEmpty()) {
      throw ArtifactSpecificationException(
          "Invalid GitHub asset identifier: Owner cannot be empty")
    }
    if (repository.isEmpty()) {
      throw ArtifactSpecificationException(
          "Invalid GitHub asset identifier: Repository cannot be empty")
    }
    if (asset.isEmpty()) {
      throw ArtifactSpecificationException(
          "Invalid GitHub asset identifier: Asset cannot be empty")
    }

    val apiUri = "https://api.github.com/repos/$owner/$repository/releases"
    val releaseList = this.cache.getOrPopulate(apiUri, StringSerializer) {
      logger.debug("Fetching release information for $owner:$repository ($asset)")

      try {
        Request.Get(apiUri)
            .setHeader("User-Agent", BeaconCoreMetadata.userAgent)
            .setHeader("Accept", "application/vnd.github.$apiVersion+json")
            .execute()
            .returnResponse()
            .let {
              if (it.statusLine.statusCode == 404 || it.statusLine.statusCode == 204) {
                throw NoSuchArtifactException("No such artifact")
              }

              it.entity.content
            }
            .readAllBytes()
            .toString(Charsets.UTF_8)
      } catch (ex: IOException) {
        throw ArtifactAvailabilityException("Failed to establish communication with GitHub", ex)
      }
    }

    val (release, file) = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue<List<Release>>(releaseList)
        .flatMap { release ->
          release.assets
              .map { release to it }
        }
        .find { it.second.name == asset }
        ?: throw ArtifactAvailabilityException(
            "Cannot find release with asset \"$asset\" in $owner/$repository")

    logger.debug(
        "Fetching release asset ${file.id} from release ${release.tagName} (${release.id})")

    return Request.Get(file.url.toURI())
        .setHeader("User-Agent", BeaconCoreMetadata.userAgent)
        .execute()
        .returnResponse()
        .let {
          if (it.statusLine.statusCode == 404 || it.statusLine.statusCode == 204) {
            throw NoSuchArtifactException("No such artifact")
          }

          it.entity.content
        }
        .readAllBytes()
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

  class Factory : ArtifactLoader.Factory {

    override val schemes = listOf("github")

    override fun create(cache: CacheProvider) = GitHubArtifactLoader(cache)
  }
}
