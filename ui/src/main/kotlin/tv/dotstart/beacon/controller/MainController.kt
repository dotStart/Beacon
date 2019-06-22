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

import com.jfoenix.controls.JFXTreeView
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import tv.dotstart.beacon.cell.ServiceListTreeCell
import tv.dotstart.beacon.cell.model.CategoryNode
import tv.dotstart.beacon.cell.model.ServiceListNode
import tv.dotstart.beacon.cell.model.ServiceNode
import tv.dotstart.beacon.exposure.PortMapper
import tv.dotstart.beacon.repository.Model
import tv.dotstart.beacon.repository.ServiceRegistry
import tv.dotstart.beacon.repository.model.Port
import java.net.URL
import java.nio.file.Files
import java.util.*

/**
 * Manages the components and interactions of the main application window.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class MainController : Initializable {

  @FXML
  private lateinit var serviceList: JFXTreeView<ServiceListNode>

  @FXML
  private lateinit var serviceIcon: ImageView
  @FXML
  private lateinit var serviceTitle: Label
  @FXML
  private lateinit var servicePorts: TableView<Port>
  @FXML
  private lateinit var serviceCopyright: Label

  @FXML
  private lateinit var serviceOpenButton: Button
  @FXML
  private lateinit var serviceCloseButton: Button

  private val root = TreeItem<ServiceListNode>()
  private val categoryMap = Model.Category.values()
      .map { it to TreeItem<ServiceListNode>(CategoryNode(it)) }
      .toMap()

  override fun initialize(p0: URL, p1: ResourceBundle?) {
    this.serviceList.cellFactory = ServiceListTreeCell.Factory
    this.serviceList.root = this.root

    this.serviceList.selectionModel.selectedItemProperty()
        .addListener({ _, _, new -> this.onServiceSelect(new.value) })

    this.serviceOpenButton.managedProperty().bind(this.serviceOpenButton.visibleProperty())
    this.serviceCloseButton.managedProperty().bind(this.serviceCloseButton.visibleProperty())
    this.serviceCloseButton.visibleProperty().bind(this.serviceOpenButton.visibleProperty().not())

    this.rebuildServiceList()
  }

  /**
   * Rebuilds the service listing in its entirety.
   */
  fun rebuildServiceList() {
    this.root.children.clear()
    this.categoryMap.forEach { _, node -> node.children.clear() }

    ServiceRegistry.forEach {
      val parent = this.categoryMap[it.category]!!
      parent.children.add(TreeItem(ServiceNode(it)))
    }

    this.categoryMap.values
        .filter { it.children.isNotEmpty() }
        .forEach {
          it.isExpanded = true
          this.root.children.add(it)
        }

    this.serviceList.selectionModel.select(1)
  }

  private fun onServiceSelect(node: ServiceListNode) {
    if (node !is ServiceNode) {
      return
    }

    val service = node.service

    this.serviceTitle.text = service.title
    this.serviceIcon.image = service.icon?.let {
      Files.newInputStream(it).use(::Image)
    }
    this.serviceOpenButton.isVisible = service !in PortMapper

    this.servicePorts.items.setAll(service.ports)
  }

  @FXML
  private fun onServiceOpen(actionEvent: ActionEvent) {
    val node = this.serviceList.selectionModel
        .selectedItem
        ?.value
        as? ServiceNode ?: return
    val service = node.service

    PortMapper += service

    this.serviceOpenButton.isVisible = false
  }

  @FXML
  private fun onServiceClose(actionEvent: ActionEvent) {
    val node = this.serviceList.selectionModel
        .selectedItem
        ?.value
        as? ServiceNode ?: return
    val service = node.service

    PortMapper -= service

    this.serviceOpenButton.isVisible = true
  }
}
