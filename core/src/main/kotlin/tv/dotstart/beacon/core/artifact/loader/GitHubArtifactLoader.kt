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

import okhttp3.internal.userAgent
import org.apache.http.client.fluent.Request
import tv.dotstart.beacon.core.BeaconCoreMetadata
import tv.dotstart.beacon.core.artifact.error.ArtifactAvailabilityException
import tv.dotstart.beacon.core.artifact.error.ArtifactSpecificationException
import tv.dotstart.beacon.core.artifact.error.NoSuchArtifactException
import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.core.cache.serialize.jsonSerializer
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.github.GitHub
import tv.dotstart.beacon.github.error.GitHubException
import tv.dotstart.beacon.github.error.repository.NoSuchRepositoryException
import tv.dotstart.beacon.github.model.repository.Release
import java.net.URI

/**
 * Retrieves artifacts from GitHub.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class GitHubArtifactLoader(
    private val cache: CacheProvider,
    private val api: GitHub) : ArtifactLoader {

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

    val (release, upstreamAsset) = this.cache.getOrPopulate(uri.toASCIIString(), jsonSerializer()) {
      logger.debug("Fetching release information for $owner:$repository ($asset)")

      try {
        val repositoryOps = this.api.forRepository(owner, repository)

        repositoryOps.listReleases()
            .filterNot(Release::prerelease)
            .mapNotNull { release ->
              release.assets
                  .find { it.name == asset }
                  ?.let { release to it }
            }
            .firstOrNull()
            ?: throw ArtifactAvailabilityException("No such artifact: $owner/$repository#$asset")
      } catch (ex: NoSuchRepositoryException) {
        throw ArtifactAvailabilityException("No such repository: $owner/$repository", ex)
      } catch (ex: GitHubException) {
        throw ArtifactAvailabilityException("Failed to establish communication with GitHub", ex)
      }
    }

    logger.debug(
        "Fetching release asset ${upstreamAsset.nodeId} from release ${release.tagName} (${release.nodeId})")

    return Request.Get(URI.create(upstreamAsset.url))
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

  class Factory : ArtifactLoader.Factory {

    override val schemes = listOf("github")

    override fun create(cache: CacheProvider) = GitHubArtifactLoader(
        cache, GitHub.Factory()
        .apply {
          userAgent = BeaconCoreMetadata.userAgent
        }
        .build())
  }
}
