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
package tv.dotstart.beacon.ui.component

import javafx.beans.DefaultProperty
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tv.dotstart.beacon.ui.delegate.property
import java.net.URL
import java.util.*

/**
 * Provides an alternative version of the tab view which displays all available options within a
 * standard list view.
 *
 * This component is primarily aimed at preference related window implementations.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 09/12/2020
 */
@DefaultProperty("pages")
class PreferencePageView : HBox(), Initializable {

  private val preferenceListView = ListView<PreferencePage>()
  private val contentPane = VBox()

  private val _pageProperty: ObjectProperty<PreferencePage> = SimpleObjectProperty()

  /**
   * Defines a listing of pages present within this component.
   */
  val pages: ObservableList<PreferencePage> = FXCollections.observableArrayList()

  /**
   * Identifies the page which is currently displayed within the preference view.
   */
  val pageProperty: ReadOnlyObjectProperty<PreferencePage>
    get() = this._pageProperty

  /**
   * @see pageProperty
   */
  val page by property(_pageProperty)

  init {
    this.styleClass.add("preference-page-view")

    this.children.setAll(this.preferenceListView, this.contentPane)

    this.preferenceListView.styleClass.add("preference-page-list")
    this.preferenceListView.setCellFactory { PageCell() }
    Bindings.bindContent(this.preferenceListView.items, this.pages)

    this.contentPane.styleClass.add("preference-page-content")
    HBox.setHgrow(this.contentPane, Priority.ALWAYS)

    this._pageProperty.addListener { _, _, page ->
      this.contentPane.children.clear()

      page?.let { this.contentPane.children.setAll(it) }
    }

    val selectionBinding = Bindings.select<PreferencePage>(
        this.preferenceListView,
        "selectionModel", "selectedItem")
    this._pageProperty.bind(selectionBinding)

    this.pages.addListener(ListChangeListener { change ->
      while (change.next()) {
        val currentSelection = this.page
        if (currentSelection == null || currentSelection in change.removed) {
          this.children
              .takeIf(ObservableList<Node>::isNotEmpty)
              ?.let { this.preferenceListView.selectionModel.select(0) }
        }
      }
    })
  }

  override fun initialize(location: URL?, resources: ResourceBundle?) {
  }

  private class PageCell : ListCell<PreferencePage>() {

    override fun updateItem(item: PreferencePage?, empty: Boolean) {
      super.updateItem(item, empty)

      this.textProperty().unbind()
      if (!empty && item != null) {
        this.textProperty().bind(item.titleProperty)
      } else {
        this.text = null
      }
    }
  }
}
