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
package tv.dotstart.beacon.ui.config

import javafx.beans.InvalidationListener
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tv.dotstart.beacon.ui.BeaconUiMetadata
import tv.dotstart.beacon.config.storage.Config
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.ui.delegate.property
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
   * Loading flag to prevent repeated persistence when loading configuration files.
   */
  private var loading = false

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
  val userRepositoryIndex: ObservableList<URI> = FXCollections.observableArrayList()

  /**
   * Identifies whether the application shall be reduced to a tray icon when iconified instead of
   * staying on the task bar.
   */
  val iconifyToTrayProperty: BooleanProperty = SimpleBooleanProperty(true)

  /**
   * @see iconifyToTrayProperty
   */
  var iconifyToTray by property(iconifyToTrayProperty)

  companion object {

    private val logger by logManager()

    private val currentVersion = Config.Version.V1_1
  }

  init {
    val listener = InvalidationListener { this.persist() }

    this.userRepositoryIndex.addListener(listener)
    this.iconifyToTrayProperty.addListener(listener)
  }

  /**
   * Loads a configuration file from disk.
   */
  fun load() {
    this.loading = true

    try {
      logger.info("Loading user configuration file")

      if (!Files.exists(this.file)) {
        logger.info("No prior configuration file located - Assuming defaults")
        this.persist()
        return
      }

      val serialized = Files.newInputStream(this.file).use {
        Config.UserConfiguration.parseFrom(it)
      }

      logger.info("Configuration version: ${serialized.version}")
      if (serialized.version == Config.Version.UNRECOGNIZED) {
        logger.warn("Incompatible save file - Changes to settings may overwrite existing data")
        return
      }

      this.migration = serialized.applicationVersion != BeaconUiMetadata.version ||
          serialized.version != currentVersion
      if (this.migration) {
        logger.warn(
            "Migration from version ${serialized.applicationVersion} (config ${serialized.version}) in progress")
      }

      this.userRepositoryIndex.setAll(serialized.repositoryList
                                          .map { URI.create(it) }
                                          .toList())
      logger.info("Discovered ${this.userRepositoryIndex.size} user repositories")

      this.iconifyToTray = serialized.iconifyToTray
      logger.info("Iconify to tray: $iconifyToTray")

      when (serialized.version) {
        Config.Version.V1_0 -> {
          logger.warn(
              "Forcefully enabling iconify to tray functionality due to configuration upgrade")
          this.iconifyToTray = true
        }
        else -> logger.info("No configuration parameter migration required")
      }
    } finally {
      this.loading = false
    }

    this.persist()
  }

  /**
   * Persists the configuration back to disk.
   */
  fun persist() {
    if (this.loading) {
      logger.debug("Currently loading configuration file - Ignoring persist request")
      return
    }

    logger.info("Persisting user configuration to disk")

    val serialized = Config.UserConfiguration.newBuilder()
        .setVersion(currentVersion)
        .setApplicationVersion(BeaconUiMetadata.version)
        .addAllRepository(this.userRepositoryIndex
                              .map(URI::toString)
                              .toList())
        .setIconifyToTray(this.iconifyToTray)
        .build()

    Files.newOutputStream(this.file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                          StandardOpenOption.TRUNCATE_EXISTING)
        .use<OutputStream?, Unit>(serialized::writeTo)
  }
}
