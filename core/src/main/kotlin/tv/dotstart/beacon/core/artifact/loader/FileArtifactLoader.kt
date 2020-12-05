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

import tv.dotstart.beacon.core.artifact.error.ArtifactAvailabilityException
import tv.dotstart.beacon.core.artifact.error.NoSuchArtifactException
import tv.dotstart.beacon.core.cache.CacheProvider
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Provides a simple file based loader which access a file from the local file system.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object FileArtifactLoader : ArtifactLoader {

  override fun retrieve(uri: URI): ByteArray {
    val source = Paths.get(uri)
    if (!Files.exists(source)) {
      throw NoSuchArtifactException("No such artifact")
    }

    try {
      return Files.readAllBytes(source)
    } catch (ex: IOException) {
      throw ArtifactAvailabilityException("Failed to copy artifact", ex)
    }
  }

  class Factory : ArtifactLoader.Factory {

    override val schemes = listOf("file")

    override fun create(cache: CacheProvider) = FileArtifactLoader
  }
}
