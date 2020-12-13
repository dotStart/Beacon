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

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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
import tv.dotstart.beacon.github.interceptor.UserAgentInterceptor
import tv.dotstart.beacon.github.model.repository.Release
import java.io.IOException
import java.net.URI

/**
 * Retrieves artifacts from GitHub.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class GitHubArtifactLoader(
    private val cache: CacheProvider,
    private val api: GitHub,
    private val http: OkHttpClient) : ArtifactLoader {

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

    val request = okhttp3.Request.Builder()
        .url(upstreamAsset.url)
        .header("Accept", upstreamAsset.contentType)
        .build()

    val response = try {
      this.http.newCall(request).execute()
    } catch (ex: IOException) {
      throw ArtifactAvailabilityException("Failed to retrieve artifact from GitHub", ex)
    }

    if (!response.isSuccessful) {
      throw ArtifactAvailabilityException(
          "GitHub responded with an unexpected error code: ${response.code}")
    }

    return response.body
        ?.let(ResponseBody::bytes)
        ?: throw ArtifactAvailabilityException("GitHub responded with an empty response body")
  }

  class Factory : ArtifactLoader.Factory {

    override val schemes = listOf("github")

    override fun create(cache: CacheProvider) = GitHubArtifactLoader(
        cache,
        GitHub.create {
          userAgent = BeaconCoreMetadata.userAgent
        },
        OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor(BeaconCoreMetadata.userAgent))
            .build())
  }
}
