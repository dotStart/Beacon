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
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.ui.delegate.property
import java.net.URI
import java.net.URL
import java.util.*

/**
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 10/12/2020
 */
class RepositoryEditorController : Initializable {

  @FXML
  private lateinit var repositoryUriTextField: TextField

  @FXML
  private lateinit var saveButton: JFXButton

  /**
   * @see repositoryUriProperty
   */
  private val _repositoryUriProperty: ObjectProperty<URI> = SimpleObjectProperty()

  /**
   * Identifies the repository URI given by the user.
   */
  val repositoryUriProperty: ReadOnlyObjectProperty<URI>
    get() = this._repositoryUriProperty

  /**
   * @see repositoryUriProperty
   */
  val repositoryUri by property(_repositoryUriProperty)

  companion object {

    private val logger by logManager()
  }

  override fun initialize(location: URL?, resources: ResourceBundle?) {
    val uriBinding = Bindings.createObjectBinding(
        {
          try {
            this.repositoryUriTextField.text
                .takeIf(String::isNotBlank)
                ?.trim()
                ?.let(URI::create)
          } catch (ex: IllegalArgumentException) {
            logger.debug("Illegal repository URI passed", ex)
            null
          }
        }, this.repositoryUriTextField.textProperty())

    this._repositoryUriProperty.bind(uriBinding)
    this.saveButton.disableProperty().bind(uriBinding.isNull)
  }

  fun onSave() {
    this.saveButton.scene.window.hide()
  }
}
