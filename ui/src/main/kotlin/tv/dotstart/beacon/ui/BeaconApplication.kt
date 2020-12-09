/*
 * Copyright 2020 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.beacon.ui

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tv.dotstart.beacon.ui.config.Configuration
import tv.dotstart.beacon.ui.controller.SplashController
import tv.dotstart.beacon.core.util.OperatingSystem
import tv.dotstart.beacon.ui.preload.Preloader
import tv.dotstart.beacon.ui.util.logger
import tv.dotstart.beacon.ui.util.splashWindow
import java.nio.file.Files


/**
 * JavaFX Entry Point
 *
 * This type handles the bootstrapping of the JavaFX components (e.g. the splash screen stage) as
 * well as the backing services (such as game definition updaters, UPnP client instances, etc).
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
@KoinApiExtension
class BeaconApplication : Application(), KoinComponent {

  private val configuration by inject<Configuration>()
  private val preloader by inject<Preloader>()

  companion object {

    private val logger = BeaconApplication::class.logger

    private const val iconPath = "image/logo.png"
    val icon: Image by lazy {
      Thread.currentThread().contextClassLoader.getResourceAsStream(iconPath)
          .use(::Image)
    }
  }

  override fun start(stage: Stage) {
    // disable implicit application exit as the tray icon would otherwise cause the toolkit to
    // shut down once the main window is hidden from view as Stage#close and Stage#hide are
    // synonymous
    Platform.setImplicitExit(false)

    try {
      stage.icons.add(icon)
    } catch (ex: Throwable) {
      logger.warn("Failed to load application icon", ex)
    }

    if (Files.notExists(OperatingSystem.current.storageDirectory)) {
      logger.info("Creating persistence directory")
      Files.createDirectories(OperatingSystem.current.storageDirectory)
    }

    this.configuration.load()

    try {
      logger.info("Displaying splash screen")
      stage.splashWindow<SplashController>("splash.fxml")
      stage.show()
    } catch (ex: Throwable) {
      logger.error("Application startup failed due to unknown error", ex)
      throw ex
    }
  }

  override fun stop() {
    this.preloader.shutdown()

    super.stop()
  }
}
