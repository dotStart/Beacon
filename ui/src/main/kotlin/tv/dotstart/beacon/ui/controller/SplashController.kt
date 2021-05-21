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
package tv.dotstart.beacon.ui.controller

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.stage.Stage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tv.dotstart.beacon.ui.BeaconApplication
import tv.dotstart.beacon.ui.BeaconUiMetadata
import tv.dotstart.beacon.ui.config.Configuration
import tv.dotstart.beacon.ui.preload.Preloader
import tv.dotstart.beacon.ui.tray.TrayIconProvider
import tv.dotstart.beacon.ui.util.Localization
import tv.dotstart.beacon.ui.util.logger
import tv.dotstart.beacon.ui.util.window
import java.net.URL
import java.util.*
import java.util.concurrent.Callable

/**
 * Handles the application startup logic.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class SplashController : Initializable, KoinComponent {

  private val configuration by inject<Configuration>()
  private val preloader by inject<Preloader>()
  private val trayIconProvider by inject<TrayIconProvider>()

  @FXML
  lateinit var statusLabel: Label

  @FXML
  lateinit var versionLabel: Label

  @FXML
  lateinit var progressBar: ProgressBar

  companion object {

    private val logger = SplashController::class.logger
  }

  override fun initialize(resourceURL: URL, resourceBundle: ResourceBundle?) {
    this.statusLabel.textProperty().bind(
        Bindings.createStringBinding(
            Callable {
              val key = this.preloader.description
              if (key.isEmpty()) {
                return@Callable ""
              }

              Localization("preload.$key")
            },
            this.preloader.descriptionProperty
        )
    )
    this.versionLabel.text = "v${BeaconUiMetadata.version}"
    this.progressBar.progressProperty().bind(this.preloader.percentageProperty)

    logger.info("Starting preloading process")
    this.preloader.preload {
      logger.info("Completed application pre-loading")

      val stage = Stage()
      stage.title = "Beacon v${BeaconUiMetadata.version}"
      stage.icons += BeaconApplication.icon
      stage.window<MainController>("main.fxml", maximizable = false)

      this.trayIconProvider.registerCallback {
        stage.show()
        stage.isIconified = false

        stage.requestFocus()
      }

      stage.iconifiedProperty().addListener { _, _, newValue ->
        if (this.configuration.iconifyToTray && newValue) {
          stage.hide()
        }
      }

      this.configuration.iconifyToTrayProperty.addListener { _, _, enabled ->
        this.updateTrayIconRegistration(stage, enabled)
      }
      this.updateTrayIconRegistration(stage, this.configuration.iconifyToTray)

      stage.show()

      // we're always spawned in primary
      (this.statusLabel.scene.window as Stage).close()

      logger.info("Switched to main window")
    }
  }

  private fun updateTrayIconRegistration(stage: Stage, enabled: Boolean) {
    if (!enabled) {
      this.trayIconProvider.visibleProperty.unbind()
      this.trayIconProvider.hide()
      return
    }

    this.trayIconProvider.visibleProperty.bind(stage.showingProperty().not())
    stage.setOnCloseRequest {
      this.trayIconProvider.hide()
      this.trayIconProvider.visibleProperty.unbind()

      Platform.exit()
    }
  }
}
