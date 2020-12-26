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

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.stage.Modality
import javafx.stage.Stage
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.core.gateway.InternetGatewayDevice
import tv.dotstart.beacon.ui.BeaconCli
import tv.dotstart.beacon.ui.BeaconUiMetadata
import tv.dotstart.beacon.ui.config.Configuration
import tv.dotstart.beacon.ui.debugCookie
import tv.dotstart.beacon.ui.delegate.property
import tv.dotstart.beacon.ui.exposure.PortExposureProvider
import tv.dotstart.beacon.ui.util.window
import java.awt.Desktop
import java.net.URI
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
  private val cache by inject<CacheProvider>()
  private val configuration by inject<Configuration>()
  private val portExposureProvider by inject<PortExposureProvider>()

  @FXML
  private lateinit var generalIconifyToTrayCheckBox: JFXCheckBox

  @FXML
  private lateinit var generalUserRepositoryListView: ListView<URI>

  @FXML
  private lateinit var generalUserRepositoryRemoveButton: JFXButton

  @FXML
  private lateinit var troubleshootingDeviceNameTextField: JFXTextField

  @FXML
  private lateinit var troubleshootingDeviceModelNameTextField: JFXTextField

  @FXML
  private lateinit var troubleshootingDeviceVendorUrlButton: JFXButton

  @FXML
  private lateinit var troubleshootingDeviceManufacturerTextField: JFXTextField

  @FXML
  private lateinit var troubleshootingDebugLogging: JFXCheckBox

  @FXML
  private lateinit var aboutVersionLabel: Label

  private val troubleshootingInternetGatewayDeviceProperty: ObjectProperty<InternetGatewayDevice> =
      SimpleObjectProperty()
  private val troubleshootingInternetGatewayDevice by property(
      troubleshootingInternetGatewayDeviceProperty)

  private val generalSelectedUserRepositoryProperty: ObjectProperty<URI> = SimpleObjectProperty()
  private val generalSelectedUserRepository by property(generalSelectedUserRepositoryProperty)

  companion object {

    private val logger by logManager()
  }

  override fun initialize(location: URL?, resources: ResourceBundle?) {
    this.generalIconifyToTrayCheckBox.selectedProperty()
        .bindBidirectional(this.configuration.iconifyToTrayProperty)
    Bindings.bindContentBidirectional(this.generalUserRepositoryListView.items,
                                      this.configuration.userRepositoryIndex)

    val generalSelectedUserRepositoryBinding = Bindings.select<URI>(
        this.generalUserRepositoryListView, "selectionModel", "selectedItem")
    this.generalSelectedUserRepositoryProperty.bind(generalSelectedUserRepositoryBinding)

    this.generalUserRepositoryRemoveButton.disableProperty()
        .bind(generalSelectedUserRepositoryBinding.isNull)

    val deviceBinding = Bindings.select<InternetGatewayDevice>(
        this.portExposureProvider, "internetGatewayDevice")
    this.troubleshootingInternetGatewayDeviceProperty.bind(deviceBinding)

    this.troubleshootingDeviceNameTextField.textProperty().bind(
        Bindings.selectString(this.troubleshootingInternetGatewayDeviceProperty, "friendlyName"))
    this.troubleshootingDeviceModelNameTextField.textProperty().bind(
        Bindings.selectString(this.troubleshootingInternetGatewayDeviceProperty, "modelName"))
    this.troubleshootingDeviceManufacturerTextField.textProperty().bind(
        Bindings.selectString(this.troubleshootingInternetGatewayDeviceProperty, "manufacturer"))

    this.troubleshootingDeviceVendorUrlButton.disableProperty().bind(
        Bindings.select<String>(this.troubleshootingInternetGatewayDeviceProperty,
                                "manufacturerUrl").isNull)

    this.troubleshootingDebugLogging.isSelected = debugCookie
    this.troubleshootingDebugLogging.selectedProperty().addListener { _, _, newValue ->
      if (newValue) {
        logger.info("Setting debug cookie")
      } else {
        logger.info("Removing debug cookie")
      }

      debugCookie = newValue
    }

    this.aboutVersionLabel.text = BeaconUiMetadata.version
  }

  @FXML
  private fun onGeneralAddRepository() {
    val stage = Stage()
    val controller = stage.window<RepositoryEditorController>(
        "repository-editor.fxml",
        minimizable = false,
        maximizable = false)
    stage.initModality(Modality.APPLICATION_MODAL)

    stage.showAndWait()

    val repositoryUri = controller.repositoryUri
        ?: return

    if (repositoryUri !in this.configuration.userRepositoryIndex) {
      this.configuration.userRepositoryIndex.add(repositoryUri)
    }
  }

  @FXML
  private fun onGeneralRemoveRepository() {
    val uri = this.generalSelectedUserRepository
        ?: return

    this.configuration.userRepositoryIndex -= uri
  }

  @FXML
  private fun onTroubleshootingVisitVendorUrl() {
    val websiteUri = try {
      this.troubleshootingInternetGatewayDevice
          ?.manufacturerUrl
          ?.let(URI::create)
          ?: return
    } catch (ex: IllegalArgumentException) {
      logger.debug("Device reported invalid device URI", ex)
      return
    }

    Desktop.getDesktop().browse(websiteUri)
  }

  @FXML
  private fun onTroubleshootingShowLogs() {
    Desktop.getDesktop().open(BeaconCli.logDirectory.toFile())
  }

  @FXML
  private fun onTroubleshootingPurgeCache() {
    this.cache.purgeAll()
  }

  @FXML
  private fun onAboutShowLicenses() {
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
}
