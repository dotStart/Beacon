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

import okhttp3.OkHttpClient
import tv.dotstart.beacon.core.BeaconCoreMetadata
import tv.dotstart.beacon.core.artifact.error.ArtifactAvailabilityException
import tv.dotstart.beacon.core.artifact.error.ArtifactSchemeException
import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.github.interceptor.UserAgentInterceptor
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI

/**
 * Retrieves artifacts via HTTP.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object HttpArtifactLoader : ArtifactLoader {

  private val client = OkHttpClient.Builder()
      .addInterceptor(UserAgentInterceptor(BeaconCoreMetadata.userAgent))
      .build()

  override fun retrieve(uri: URI): ByteArray {
    val url = try {
      uri.toURL()
    } catch (ex: MalformedURLException) {
      throw ArtifactSchemeException("Malformed HTTP(S) artifact URI", ex)
    }

    val request = okhttp3.Request.Builder()
        .url(url)
        .header("X-Beacon-Version", BeaconCoreMetadata.version)
        .build()

    val response = try {
      this.client.newCall(request).execute()
    } catch (ex: IOException) {
      throw ArtifactAvailabilityException("Failed to retrieve artifact", ex)
    }

    if (!response.isSuccessful) {
      throw ArtifactAvailabilityException(
          "Upstream server responded with unexpected error code: ${response.code}")
    }

    return try {
      response.body
          ?.bytes()
          ?: throw ArtifactAvailabilityException("Upstream server responded with empty body")
    } catch (ex: IOException) {
      throw ArtifactAvailabilityException("Failed to retrieve artifact", ex)
    }
  }

  class Factory : ArtifactLoader.Factory {

    override val schemes = listOf("http")

    override fun create(cache: CacheProvider) = HttpArtifactLoader
  }
}
