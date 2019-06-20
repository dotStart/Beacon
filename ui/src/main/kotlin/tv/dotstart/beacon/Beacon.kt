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

import com.jfoenix.controls.JFXAlert
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialogLayout
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.stage.Stage
import tv.dotstart.beacon.config.Configuration
import tv.dotstart.beacon.exposure.InterfaceChooser
import tv.dotstart.beacon.util.OperatingSystem
import tv.dotstart.beacon.util.logger
import tv.dotstart.beacon.util.splashWindow
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Paths


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
      val resource = Thread.currentThread().contextClassLoader.getResource(iconPath)
          ?.let { Paths.get(it.toURI()) }
          ?: throw NoSuchFileException("No such file or directory: $iconPath")

      Files.newInputStream(resource).use(::Image)
    }
  }

  override fun start(stage: Stage) {
    try {
      stage.icons.add(icon)
    } catch (ex: Throwable) {
      logger.warn("Failed to load application icon", ex)
    }

    val gatewayInterface = InterfaceChooser.gatewayInterface
    if (gatewayInterface == null) {
      logger.warn("Failed to detect gateway interface - Port forwarding may fail")
    } else {
      logger.info("Gateway Interface: $gatewayInterface")
    }

    val recommendedInterface = InterfaceChooser.recommended
    if (recommendedInterface == null) {
      logger.error("Failed to detect viable network interface - Cannot continue")
      val alert = JFXAlert<Nothing>()
      alert.title = "System Error"
      val layout = JFXDialogLayout()
      layout.setHeading(Label("System Error"))
      layout.setBody(Label(
          "Failed to detect at least one compatible network interface. " +
              "Please make sure that your computer is connected to the internet and try again. " +
              "If this issue persists, please report it to the application maintainer."
      ))
      val closeButton = JFXButton("Exit")
      closeButton.onAction = EventHandler { alert.close() }
      layout.setActions(closeButton)
      alert.setContent(layout)
      alert.showAndWait()
      return
    }

    logger.info("Detected Interfaces: ${InterfaceChooser.interfaces.joinToString()}")
    logger.info("Recommended Interface: $recommendedInterface")

    if (Files.notExists(OperatingSystem.current.storage)) {
      logger.info("Creating persistence directory")
      Files.createDirectories(OperatingSystem.current.storage)
    }

    Configuration.load()

    logger.info("Displaying splash screen")
    stage.splashWindow("splash.fxml")
    stage.show()
  }
}
