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
package tv.dotstart.beacon.config

import javafx.beans.InvalidationListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tv.dotstart.beacon.config.storage.Config
import tv.dotstart.beacon.util.OperatingSystem
import tv.dotstart.beacon.util.logger
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Represents the current user configuration.
 *
 * This object provides a more accessible wrapper for the configuration representation provided in
 * the spec package.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object Configuration {

  private val logger = Configuration::class.logger

  /**
   * Identifies the location of the configuration file which will persistently store all data
   * managed by this type.
   */
  val file = OperatingSystem.current.storage.resolve("config.dat")

  /**
   * Exposes an index of user specified repository URLs which are to be pulled upon application
   * initialization (or manual repository refresh).
   */
  val userRepositoryIndex: ObservableList<URL> = FXCollections.observableArrayList<URL>()

  /**
   * Constructs a persistable representation of the current configuration state.
   */
  private val persistable: Config.UserConfiguration
    get() = Config.UserConfiguration.newBuilder()
        .setVersion(Config.Version.V1_0)
        .addAllRepository(this.userRepositoryIndex
            .map { it.toString() }
            .toList())
        .build()

  init {
    this.userRepositoryIndex.addListener(InvalidationListener {
      this.persist()
    })
  }

  /**
   * Loads a configuration file from disk.
   */
  fun load() {
    logger.info("Loading user configuration file")

    if (!Files.exists(this.file)) {
      logger.info("No prior configuration file located - Assuming defaults")
      return
    }

    val serialized = Files.newInputStream(this.file).use {
      Config.UserConfiguration.parseFrom(it)
    }

    if (serialized.version == Config.Version.UNRECOGNIZED) {
      logger.warn("Incompatible save file - Changes to settings may overwrite existing data")
      return
    }

    this.userRepositoryIndex.setAll(serialized.repositoryList
        .map { URL(it) }
        .toList())

    logger.info("Discovered ${this.userRepositoryIndex.size} user repositories")
  }

  /**
   * Persists the configuration back to disk.
   */
  fun persist() {
    logger.info("Persisting user configuration to disk")

    val serialized = this.persistable
    Files.newOutputStream(this.file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING).use {
      serialized.writeTo(it)
    }
  }
}
