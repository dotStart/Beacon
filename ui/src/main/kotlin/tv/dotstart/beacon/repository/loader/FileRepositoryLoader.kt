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

import tv.dotstart.beacon.repository.error.RepositoryAvailabilityException
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Provides a simple file based loader which access a file from the local file system and copies it
 * to the desired cache location.
 *
 * Note that this loader implementation is most useful when the caching system has been explicitly
 * disabled via the command line.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object FileRepositoryLoader : RepositoryLoader {

  override val schemes = listOf("file")

  override fun invoke(uri: URI, target: Path) {
    try {
      val source = Paths.get(uri)
      Files.copy(source, target)
    } catch (ex: IOException) {
      throw RepositoryAvailabilityException("Failed to copy repository", ex)
    }
  }
}
