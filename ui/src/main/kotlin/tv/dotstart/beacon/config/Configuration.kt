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
import tv.dotstart.beacon.BeaconUiMetadata
import tv.dotstart.beacon.config.storage.Config
import tv.dotstart.beacon.core.delegate.logManager
import java.io.OutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Represents the current user configuration.
 *
 * This object provides a more accessible wrapper for the configuration representation provided in
 * the spec package.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class Configuration(root: Path) {

  /**
   * Identifies the location of the configuration file which will persistently store all data
   * managed by this type.
   */
  private val file = root.resolve("config.dat")

  /**
   * Identifies whether a different version of the application was running prior to this execution
   * thus requiring migration in some modules.
   */
  var migration: Boolean = false
    private set

  /**
   * Exposes an index of user specified repository URLs which are to be pulled upon application
   * initialization (or manual repository refresh).
   */
  val userRepositoryIndex: ObservableList<URI> = FXCollections.observableArrayList<URI>()

  /**
   * Constructs a persistable representation of the current configuration state.
   */
  private val persistable: Config.UserConfiguration
    get() = Config.UserConfiguration.newBuilder()
        .setVersion(Config.Version.V1_0)
        .setApplicationVersion(BeaconUiMetadata.version)
        .addAllRepository(this.userRepositoryIndex
                              .map(URI::toString)
                              .toList())
        .build()

  companion object {

    private val logger by logManager()
  }

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
      this.persist()
      return
    }

    val serialized = Files.newInputStream(this.file).use {
      Config.UserConfiguration.parseFrom(it)
    }

    if (serialized.version == Config.Version.UNRECOGNIZED) {
      logger.warn("Incompatible save file - Changes to settings may overwrite existing data")
      return
    }

    if (serialized.applicationVersion != BeaconUiMetadata.version) {
      logger.warn("Migration from version ${serialized.applicationVersion} in progress")
      this.migration = true
    }

    this.userRepositoryIndex.setAll(serialized.repositoryList
                                        .map { URI.create(it) }
                                        .toList())

    logger.info("Discovered ${this.userRepositoryIndex.size} user repositories")

    if (this.migration) {
      this.persist()
    }
  }

  /**
   * Persists the configuration back to disk.
   */
  fun persist() {
    logger.info("Persisting user configuration to disk")

    val serialized = this.persistable
    Files.newOutputStream(this.file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                          StandardOpenOption.TRUNCATE_EXISTING)
        .use<OutputStream?, Unit>(serialized::writeTo)
  }
}
