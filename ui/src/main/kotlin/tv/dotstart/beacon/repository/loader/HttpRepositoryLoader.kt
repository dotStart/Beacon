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

import org.apache.http.client.HttpResponseException
import org.apache.http.client.fluent.Request
import tv.dotstart.beacon.BeaconMetadata
import tv.dotstart.beacon.repository.error.NoSuchRepositoryException
import tv.dotstart.beacon.repository.error.RepositoryAvailabilityException
import java.io.IOException
import java.net.URI
import java.nio.file.Path

/**
 * Provides a basic HTTP(S) backed implementation.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object HttpRepositoryLoader : RepositoryLoader {

  override val schemes = listOf("http", "https")

  override fun invoke(uri: URI, target: Path) {
    try {
      Request.Get(uri)
          .setHeader("User-Agent", BeaconMetadata.userAgent)
          .execute()
          .saveContent(target.toFile())
    } catch (ex: HttpResponseException) {
      throw when (ex.statusCode) {
        404 -> NoSuchRepositoryException("Cannot locate repository", ex)
        else -> RepositoryAvailabilityException("Cannot fetch repository", ex)
      }
    } catch (ex: IOException) {
      throw RepositoryAvailabilityException("Cannot fetch or store repository", ex)
    }
  }
}
