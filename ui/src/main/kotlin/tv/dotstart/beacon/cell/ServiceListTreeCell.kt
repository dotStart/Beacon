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
package tv.dotstart.beacon.cell

import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.util.Callback
import tv.dotstart.beacon.cell.model.ServiceListNode
import java.nio.file.Files

/**
 * Provides a list cell which displays service nodes (e.g. service definitions or categories).
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class ServiceListTreeCell : TreeCell<ServiceListNode>() {

  override fun updateItem(node: ServiceListNode?, empty: Boolean) {
    super.updateItem(node, empty)

    if (empty || node == null) {
      this.text = null
      this.graphic = null
      return
    }

    this.styleClass.add(node.styleClass)

    this.text = node.title
    this.graphic = node.icon?.let {
      Files.newInputStream(it).use {
        ImageView(Image(it))
      }
    }
  }

  /**
   * Provides a factory which is capable of constructing service list cells.
   */
  object Factory : Callback<TreeView<ServiceListNode>, TreeCell<ServiceListNode>> {

    override fun call(list: TreeView<ServiceListNode>) = ServiceListTreeCell()
  }
}
