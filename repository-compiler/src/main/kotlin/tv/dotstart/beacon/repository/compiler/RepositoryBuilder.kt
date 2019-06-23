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
package tv.dotstart.beacon.repository.compiler

import org.apache.commons.compress.compressors.CompressorStreamFactory
import tv.dotstart.beacon.repository.Model
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Provides a factory for arbitrary service lists.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class RepositoryBuilder {

  private val builder = Model.Repository.newBuilder()

  var displayName: String?
    get() = this.builder.displayName
    set(value) {
      if (value == null) {
        this.builder.clearDisplayName()
        return
      }

      this.builder.displayName = value
    }

  var revision: Long
    get() = this.builder.revision
    set(value) {
      this.builder.revision = value
    }

  companion object {

    /**
     * Constructs a new repository based on programmatically defined service definitions.
     */
    operator fun invoke(block: RepositoryBuilder.() -> Unit) {
      val builder = RepositoryBuilder()
      builder.block()
    }
  }

  init {
    this.builder.revision = 0
  }

  /**
   * Configures a service with the given identifier, title and other programmatically defined
   * attributes.
   */
  fun withService(id: String, title: String, block: ServiceDefinitionBuilder.() -> Unit) {
    val builder = ServiceDefinitionBuilder(id, title)
    builder.block()
    this.builder.addService(builder())
  }

  /**
   * Writes a serialized version of the repository to the given location.
   */
  fun writeTo(target: Path) {
    val repository = this.builder.build()
    Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING).use { out ->
      CompressorStreamFactory()
          .createCompressorOutputStream(CompressorStreamFactory.XZ, out)
          .use {
            repository.writeTo(it)
          }
    }
  }
}
