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
package tv.dotstart.beacon.ui.controller

import com.jfoenix.controls.JFXTextField
import com.jfoenix.controls.JFXTreeView
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TreeItem
import javafx.scene.image.ImageView
import javafx.stage.Modality
import javafx.stage.Stage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tv.dotstart.beacon.repository.Model
import tv.dotstart.beacon.ui.BeaconUiMetadata
import tv.dotstart.beacon.ui.cell.ServiceListTreeCell
import tv.dotstart.beacon.ui.cell.model.CategoryNode
import tv.dotstart.beacon.ui.cell.model.ServiceListNode
import tv.dotstart.beacon.ui.cell.model.ServiceNode
import tv.dotstart.beacon.ui.exposure.PortExposureProvider
import tv.dotstart.beacon.ui.repository.ServiceRegistry
import tv.dotstart.beacon.ui.repository.model.Port
import tv.dotstart.beacon.ui.repository.model.Service
import tv.dotstart.beacon.ui.util.*
import java.net.URL
import java.util.*

/**
 * Manages the components and interactions of the main application window.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class MainController : Initializable, KoinComponent {

  private val exposureProvider by inject<PortExposureProvider>()
  private val serviceRegistry by inject<ServiceRegistry>()

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

    this.externalAddress.textProperty().bind(this.exposureProvider.externalAddressProperty)

    this.rebuildServiceList()

    if (BeaconUiMetadata.unstable) {
      dialog(Localization("warning.unstable"), Localization("warning.unstable.body"))
    }
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

    this.serviceRegistry.forEach {
      val parent = this.categoryMap[it.category]!!
      parent.children.add(TreeItem(ServiceNode(it, it.icon?.toImage(), it.title)))
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
    this.serviceIcon.image = node.icon

    this.serviceOpenButton.isVisible = service !in this.exposureProvider
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
      this.serviceRegistry.persist()
    } catch (ex: Throwable) {
      logger.error("Failed to persist custom services", ex)

      dialog(Localization("error.custom.title"), Localization("error.custom.body"))
    }
  }

  @FXML
  private fun onServiceOpen() {
    val node = this.serviceList.selectionModel
      .selectedItem
      ?.value
      as? ServiceNode ?: return
    val service = node.service

    this.exposureProvider.expose(service)

    this.serviceOpenButton.isVisible = false
  }

  @FXML
  private fun onServiceClose() {
    val node = this.serviceList.selectionModel
      .selectedItem
      ?.value
      as? ServiceNode ?: return
    val service = node.service

    this.exposureProvider.close(service)

    this.serviceOpenButton.isVisible = true
  }

  @FXML
  private fun onAddService() {
    val stage = Stage()
    val controller =
      stage.window<ServiceEditorController>(
        "service-editor.fxml",
        maximizable = false,
        minimizable = false
      )
    stage.initModality(Modality.APPLICATION_MODAL)

    stage.title = Localization("editor.title")
    stage.isResizable = false

    stage.showAndWait()

    val definition = controller.service
      ?: return

    this.serviceRegistry += definition
    this.persistCustomServices()

    this.rebuildServiceList()
    this.selectService(definition)
  }

  @FXML
  private fun onServiceEdit() {
    val previous = this.currentService
      ?.takeIf { it.category == Model.Category.CUSTOM }
      ?: return

    val stage = Stage()
    val controller =
      stage.window<ServiceEditorController>(
        "service-editor.fxml",
        maximizable = false,
        minimizable = false
      )
    stage.initModality(Modality.APPLICATION_MODAL)

    controller.service = previous

    stage.title = Localization("editor.title")
    stage.isResizable = false

    stage.showAndWait()

    val new = controller.service
    if (new != null) {
      this.serviceRegistry += new
      this.persistCustomServices()

      this.rebuildServiceList()
      this.selectService(new)
    }
  }

  @FXML
  private fun onServiceRemove() {
    val selected = this.currentService
      ?.takeIf { it.category == Model.Category.CUSTOM }
      ?: return

    this.serviceRegistry -= selected

    this.serviceRegistry.persist()
    this.rebuildServiceList()
  }

  @FXML
  private fun onSettingsOpen() {
    val stage = Stage()
    stage.window<SettingsController>("settings.fxml", minimizable = false, maximizable = false)
    stage.title = Localization("settings.title")
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.showAndWait()
  }
}
