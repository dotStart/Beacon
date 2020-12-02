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
package tv.dotstart.beacon

import javafx.application.Application
import javafx.scene.image.Image
import javafx.stage.Stage
import tv.dotstart.beacon.config.Configuration
import tv.dotstart.beacon.preload.Preloader
import tv.dotstart.beacon.util.OperatingSystem
import tv.dotstart.beacon.util.logger
import tv.dotstart.beacon.util.splashWindow
import java.nio.file.Files


/**
 * JavaFX Entry Point
 *
 * This type handles the bootstrapping of the JavaFX components (e.g. the splash screen stage) as
 * well as the backing services (such as game definition updaters, UPnP client instances, etc).
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class Beacon : Application() {

  companion object {

    private val logger = Beacon::class.logger

    private const val iconPath = "image/logo.png"
    val icon: Image by lazy {
      Thread.currentThread().contextClassLoader.getResourceAsStream(iconPath)
          .use(::Image)
    }
  }

  override fun start(stage: Stage) {
    try {
      stage.icons.add(icon)
    } catch (ex: Throwable) {
      logger.warn("Failed to load application icon", ex)
    }

    if (Files.notExists(OperatingSystem.current.storage)) {
      logger.info("Creating persistence directory")
      Files.createDirectories(OperatingSystem.current.storage)
    }

    Configuration.load()

    try {
      logger.info("Displaying splash screen")
      stage.splashWindow("splash.fxml")
      stage.show()
    } catch (ex: Throwable) {
      logger.error("Application startup failed due to unknown error", ex)
      throw ex
    }
  }

  override fun stop() {
    Preloader.shutdown()

    super.stop()
  }
}
