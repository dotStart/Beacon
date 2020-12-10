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
import com.jfoenix.controls.JFXTextField
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableView
import javafx.stage.Modality
import javafx.stage.Stage
import tv.dotstart.beacon.repository.Model
import tv.dotstart.beacon.ui.repository.model.Port
import tv.dotstart.beacon.ui.repository.model.Service
import tv.dotstart.beacon.ui.util.Localization
import tv.dotstart.beacon.ui.util.window
import java.net.URI
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
class ServiceEditorController : Initializable {

  @FXML
  private lateinit var nameTextField: JFXTextField

  @FXML
  private lateinit var portTableView: TableView<Port>

  @FXML
  private lateinit var removePortButton: JFXButton

  @FXML
  private lateinit var saveButton: JFXButton

  private val ports: ObservableList<Port> = FXCollections.observableArrayList()

  val serviceProperty: ObjectProperty<Service> = SimpleObjectProperty()
  var service: Service?
    get() = this.serviceProperty.value
    set(value) {
      this.serviceProperty.value = value
    }

  override fun initialize(location: URL?, resources: ResourceBundle?) {
    Bindings.bindContent(this.portTableView.items, this.ports)

    val selectionProperty = Bindings.select<Port?>(
        this.portTableView.selectionModelProperty(), "selectedItem")
    this.removePortButton.disableProperty().bind(selectionProperty.isNull)

    val valid = Bindings.size(this.ports)
        .isNotEqualTo(0)
        .and(this.nameTextField.textProperty().isNotEmpty)
    this.saveButton.disableProperty().bind(valid.not())

    this.serviceProperty.addListener({ _, _, new -> this.refreshContent(new) })
  }

  private fun refreshContent(service: Service?) {
    if (service == null) {
      return
    }

    this.nameTextField.text = service.title
    this.ports.setAll(service.ports)
  }

  @FXML
  private fun onAddPort(event: ActionEvent) {
    val stage = Stage()
    val controller = stage.window<PortEditorController>(
        "port-editor.fxml", maximizable = false, minimizable = false)
    stage.initModality(Modality.APPLICATION_MODAL)

    stage.title = Localization("editor.port.title")
    stage.isResizable = false

    stage.showAndWait()

    val port = controller.port
    if (port != null && port !in this.ports) {
      this.ports.add(port)
    }
  }

  @FXML
  private fun onRemovePort(event: ActionEvent) {
    val selectedPort = this.portTableView.selectionModel.selectedItem
        ?: return

    this.ports.remove(selectedPort)
  }

  @FXML
  private fun onSave(event: ActionEvent) {
    val serviceId = this.service
        ?.id
        ?: URI("custom", UUID.randomUUID().toString(), null)

    this.service = Service(
        serviceId,
        Model.Category.CUSTOM,
        null,
        this.nameTextField.text,
        ArrayList(this.ports)
    )

    (this.saveButton.scene.window as Stage).close()
  }
}
