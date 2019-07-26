/*
 * Copyright 2019 Johannes Donath <johannesd@torchmind.com>
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
package tv.dotstart.beacon.controller

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import tv.dotstart.beacon.BeaconCli
import tv.dotstart.beacon.BeaconMetadata
import tv.dotstart.beacon.util.OperatingSystem
import java.awt.Desktop
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * Provides a controller component for the about dialogue.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class AboutController : Initializable {

  @FXML
  private lateinit var versionLabel: Label

  override fun initialize(path: URL, resources: ResourceBundle?) {
    this.versionLabel.text = BeaconMetadata.version
  }

  @FXML
  private fun onShowLicenses(actionEvent: ActionEvent) {
    val classLoader = Thread.currentThread().contextClassLoader
    val targetFile = OperatingSystem.current.storage.resolve("THIRD-PARTY.txt")

    classLoader.getResourceAsStream("THIRD-PARTY.txt").use {
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
