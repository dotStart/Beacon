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

import com.jfoenix.controls.JFXTextField
import com.jfoenix.controls.JFXTreeView
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import tv.dotstart.beacon.cell.ServiceListTreeCell
import tv.dotstart.beacon.cell.model.CategoryNode
import tv.dotstart.beacon.cell.model.ServiceListNode
import tv.dotstart.beacon.cell.model.ServiceNode
import tv.dotstart.beacon.forwarding.PortExposureProvider
import tv.dotstart.beacon.repository.Model
import tv.dotstart.beacon.repository.ServiceRegistry
import tv.dotstart.beacon.repository.model.Port
import tv.dotstart.beacon.repository.model.Service
import tv.dotstart.beacon.util.*
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
  private lateinit var externalAddress: JFXTextField

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

  @FXML
  private lateinit var serviceEditButton: Button

  @FXML
  private lateinit var serviceRemoveButton: Button

  private val root = TreeItem<ServiceListNode>()
  private val categoryMap = Model.Category.values()
      .map { it to TreeItem<ServiceListNode>(CategoryNode(it)) }
      .toMap()

  private var currentServiceProperty: ObjectProperty<Service> = SimpleObjectProperty()
  private var currentService: Service?
    get() = this.currentServiceProperty.value
    set(value) {
      this.currentServiceProperty.value = value
    }

  override fun initialize(p0: URL, p1: ResourceBundle?) {
    this.serviceList.cellFactory = ServiceListTreeCell.Factory
    this.serviceList.root = this.root

    this.serviceList.selectionModel.selectedItemProperty()
        .addListener({ _, _, new -> this.onServiceSelect(new?.value) })

    this.serviceOpenButton.managedProperty().bind(this.serviceOpenButton.visibleProperty())
    this.serviceCloseButton.managedProperty().bind(this.serviceCloseButton.visibleProperty())
    this.serviceCloseButton.visibleProperty().bind(this.serviceOpenButton.visibleProperty().not())

    val selectedCategory = Bindings.select<Model.Category>(this.currentServiceProperty, "category")
    val customSelected = selectedCategory.isEqualTo(Model.Category.CUSTOM)

    this.serviceEditButton.managedProperty().bind(this.serviceEditButton.visibleProperty())
    this.serviceRemoveButton.managedProperty().bind(this.serviceRemoveButton.visibleProperty())
    this.serviceEditButton.visibleProperty().bind(customSelected)
    this.serviceRemoveButton.visibleProperty().bind(customSelected)

    this.externalAddress.text = PortExposureProvider.externalAddress
        ?: Localization("address.unknown")

    this.rebuildServiceList()
  }

  companion object {

    private val logger = MainController::class.logger
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
          it.children.sortBy { it.value.title }
          this.root.children.add(it)
        }

    this.serviceList.selectionModel.select(1)
  }

  private fun onServiceSelect(node: ServiceListNode?) {
    if (node == null || node !is ServiceNode) {
      return
    }

    val service = node.service
    this.currentService = service

    this.serviceTitle.text = service.title
    this.serviceIcon.image = service.icon?.let {
      Files.newInputStream(it).use(::Image)
    }

    this.serviceOpenButton.isVisible = service !in PortExposureProvider
    this.servicePorts.items.setAll(service.ports)
  }

  private fun selectService(definition: Service) {
    val (node, _) = this.categoryMap.values
        .flatMap { it.children }
        .mapNotNull { node ->
          (node.value as? ServiceNode)
              ?.let { node to it.service }
        }
        .find { (_, service) -> service == definition }
        ?: return

    this.serviceList.selectionModel.select(node)
  }

  private fun persistCustomServices() {
    try {
      ServiceRegistry.persist()
    } catch (ex: Throwable) {
      logger.error("Failed to persist custom services", ex)

      errorDialog(Localization("error.custom.title"), Localization("error.custom.body"))
    }
  }

  @FXML
  private fun onServiceOpen(actionEvent: ActionEvent) {
    val node = this.serviceList.selectionModel
        .selectedItem
        ?.value
        as? ServiceNode ?: return
    val service = node.service

    PortExposureProvider.expose(service)

    this.serviceOpenButton.isVisible = false
  }

  @FXML
  private fun onServiceClose(actionEvent: ActionEvent) {
    val node = this.serviceList.selectionModel
        .selectedItem
        ?.value
        as? ServiceNode ?: return
    val service = node.service

    PortExposureProvider.close(service)

    this.serviceOpenButton.isVisible = true
  }

  @FXML
  private fun onAddService(actionEvent: ActionEvent) {
    val stage = Stage()
    val controller =
        stage.window<ServiceEditorController>("service-editor.fxml",
                                              maximizable = false,
                                              minimizable = false)

    stage.title = Localization("editor.title")
    stage.isResizable = false

    stage.showAndWait()

    val definition = controller.service
        ?: return

    ServiceRegistry += definition
    this.persistCustomServices()

    this.rebuildServiceList()
    this.selectService(definition)
  }

  @FXML
  private fun onServiceEdit(actionEvent: ActionEvent) {
    val previous = this.currentService
        ?.takeIf { it.category == Model.Category.CUSTOM }
        ?: return

    val stage = Stage()
    val controller =
        stage.window<ServiceEditorController>("service-editor.fxml",
                                              maximizable = false,
                                              minimizable = false)
    controller.service = previous

    stage.title = Localization("editor.title")
    stage.isResizable = false

    stage.showAndWait()

    val new = controller.service
    if (new != null) {
      ServiceRegistry += new
      this.persistCustomServices()

      this.rebuildServiceList()
      this.selectService(new)
    }
  }

  @FXML
  private fun onServiceRemove(actionEvent: ActionEvent) {
    val selected = this.currentService
        ?.takeIf { it.category == Model.Category.CUSTOM }
        ?: return

    ServiceRegistry -= selected

    ServiceRegistry.persist()
    this.rebuildServiceList()
  }

  @FXML
  private fun onAboutOpen(actionEvent: ActionEvent) {
    val stage = Stage()
    stage.focusedProperty()
        .addListener({ _, _, n ->
                       if (!n) {
                         stage.close()
                       }
                     })

    stage.splashWindow<AboutController>("about.fxml")
    stage.show()
  }
}
