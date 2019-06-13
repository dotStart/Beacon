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
package tv.dotstart.beacon.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import tv.dotstart.beacon.BeaconMetadata
import tv.dotstart.beacon.preload.Preloader
import tv.dotstart.beacon.util.logger
import java.net.URL
import java.util.*

/**
 * Handles the application startup logic.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class SplashController : Initializable {

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
    this.statusLabel.textProperty().bind(Preloader.descriptionProperty)
    this.versionLabel.text = "v${BeaconMetadata.version}"
    this.progressBar.progressProperty().bind(Preloader.percentageProperty)

    logger.info("Starting preloading process")
    Preloader {
      logger.info("Completed application pre-loading")
    }
  }
}
