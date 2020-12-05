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

import org.apache.http.client.HttpResponseException
import org.apache.http.client.fluent.Request
import tv.dotstart.beacon.core.BeaconCoreMetadata
import tv.dotstart.beacon.core.artifact.error.ArtifactAvailabilityException
import tv.dotstart.beacon.core.artifact.error.NoSuchArtifactException
import tv.dotstart.beacon.core.cache.CacheProvider
import java.io.IOException
import java.net.URI

/**
 * Retrieves artifacts via HTTP.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object HttpArtifactLoader : ArtifactLoader {

  override fun retrieve(uri: URI): ByteArray {
    try {
      return Request.Get(uri)
          .setHeader("User-Agent", BeaconCoreMetadata.userAgent)
          .setHeader("X-Beacon-Version", BeaconCoreMetadata.version)
          .execute()
          .returnResponse()
          .let {
            if (it.statusLine.statusCode == 404 || it.statusLine.statusCode == 204) {
              throw NoSuchArtifactException("No such artifact")
            }

            it.entity.content
          }
          .readAllBytes()
    } catch (ex: HttpResponseException) {
      throw when (ex.statusCode) {
        404 -> NoSuchArtifactException("Cannot locate repository", ex)
        else -> ArtifactAvailabilityException("Cannot fetch repository", ex)
      }
    } catch (ex: IOException) {
      throw ArtifactAvailabilityException("Cannot fetch or store repository", ex)
    }
  }

  class Factory : ArtifactLoader.Factory {

    override val schemes = listOf("http")

    override fun create(cache: CacheProvider) = HttpArtifactLoader
  }
}
