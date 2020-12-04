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
package tv.dotstart.beacon.controller

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXRadioButton
import com.jfoenix.controls.JFXTextField
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextFormatter
import javafx.scene.control.ToggleGroup
import javafx.stage.Stage
import tv.dotstart.beacon.core.model.Protocol
import tv.dotstart.beacon.repository.model.Port
import java.net.URL
import java.util.*
import java.util.function.UnaryOperator

/**
 * Provides a simple editor capable of creating or changing port specifications.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
class PortEditorController : Initializable {

  private val protocolToggleGroup = ToggleGroup()

  @FXML
  private lateinit var tcpProtocolButton: JFXRadioButton

  @FXML
  private lateinit var udpProtocolButton: JFXRadioButton

  @FXML
  private lateinit var portNumberTextField: JFXTextField

  @FXML
  private lateinit var saveButton: JFXButton

  var port: Port? = null
    private set

  override fun initialize(location: URL?, resources: ResourceBundle?) {
    this.tcpProtocolButton.toggleGroup = this.protocolToggleGroup
    this.udpProtocolButton.toggleGroup = this.protocolToggleGroup

    this.portNumberTextField.textFormatter = TextFormatter<String>(UnaryOperator {
      val newValue = it.controlNewText

      if (newValue.isEmpty) {
        return@UnaryOperator it
      }

      val newNumericValue = newValue.toIntOrNull(10)
          ?.takeIf { it > 0 }

      if (newNumericValue != null) {
        it
      } else {
        null
      }
    })

    val validProperty = this.protocolToggleGroup.selectedToggleProperty()
        .isNotNull
        .and(this.portNumberTextField.textProperty().isNotEmpty)
    this.saveButton.disableProperty().bind(validProperty.not())

    val port = this.port
    if (port != null) {
      val selectedButton = when (port.protocol) {
        Protocol.TCP -> this.tcpProtocolButton
        Protocol.UDP -> this.udpProtocolButton
        else -> null
      }
      selectedButton?.let(this.protocolToggleGroup::selectToggle)

      this.portNumberTextField.text = port.number.toString()
    }
  }

  @FXML
  private fun onSave(event: ActionEvent) {
    this.port = Port(
        if (this.protocolToggleGroup.selectedToggle == this.tcpProtocolButton) {
          Protocol.TCP
        } else {
          Protocol.UDP
        },
        this.portNumberTextField.text.toInt(10)
    )

    (this.saveButton.scene.window as Stage).close()
  }
}
