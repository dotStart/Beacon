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
package tv.dotstart.beacon.ui.controller

import com.jfoenix.controls.JFXCheckBox
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import tv.dotstart.beacon.ui.BeaconCli
import tv.dotstart.beacon.ui.BeaconUiMetadata
import tv.dotstart.beacon.ui.config.Configuration
import java.awt.Desktop
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * Provides the necessary controller logic for the settings window.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 09/12/2020
 */
@KoinApiExtension
class SettingsController : Initializable, KoinComponent {

  private val storagePath by inject<Path>(named("storagePath"))
  private val configuration by inject<Configuration>()

  @FXML
  private lateinit var generalIconifyToTrayCheckBox: JFXCheckBox

  @FXML
  private lateinit var aboutVersionLabel: Label

  override fun initialize(location: URL?, resources: ResourceBundle?) {
    this.generalIconifyToTrayCheckBox.selectedProperty()
        .bindBidirectional(this.configuration.iconifyToTrayProperty)

    this.aboutVersionLabel.text = BeaconUiMetadata.version
  }

  @FXML
  private fun onShowLicenses(actionEvent: ActionEvent) {
    val classLoader = Thread.currentThread().contextClassLoader
    val targetFile = this.storagePath.resolve("THIRD-PARTY.txt")

    classLoader.getResourceAsStream("THIRD-PARTY.txt")
        .use {
          Channels.newChannel(it!!).use { input ->
            FileChannel.open(targetFile, StandardOpenOption.CREATE,
                             StandardOpenOption.TRUNCATE_EXISTING,
                             StandardOpenOption.WRITE).use {
              it.transferFrom(input, 0, Long.MAX_VALUE)
            }
          }
        }

    Desktop.getDesktop().open(targetFile.toFile())
  }

  @FXML
  private fun onShowLogs(actionEvent: ActionEvent) {
    Desktop.getDesktop().open(BeaconCli.logDirectory.toFile())
  }
}
