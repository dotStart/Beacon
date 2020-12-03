/*
 * Copyright (C) 2019 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.beacon.util

import com.jfoenix.assets.JFoenixResources
import com.jfoenix.controls.JFXDecorator
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

/**
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */

/**
 * Loads an FXML resource from the given location.
 */
internal fun <C : Any, N : Any> fxml(path: String): Pair<C, N> {
  val classLoader = Thread.currentThread().contextClassLoader
  val resource = classLoader.getResource("fxml/$path")
      ?: throw FileNotFoundException("No such FXML resource: $path")

  val loader = FXMLLoader(resource)
  loader.charset = StandardCharsets.UTF_8
  loader.resources = Localization.Bundle

  val node = loader.load<N>()
  return loader.getController<C>() to node
}

/**
 * Creates an undecorated window from the given FXML resource.
 */
fun <C : Any> Stage.splashWindow(path: String): C {
  this.initStyle(StageStyle.UNDECORATED)

  val (controller, node) = fxml<C, Parent>(path)

  val scene = Scene(node)
  this.scene = scene
  return controller
}

/**
 * Creates a decorated window from the given FXML resource.
 */
fun <C : Any> Stage.window(path: String,
                           fullScreen: Boolean = false,
                           maximizable: Boolean = true,
                           minimizable: Boolean = true): C {
  val classLoader = Thread.currentThread().contextClassLoader
  val (controller, node) = fxml<C, Node>(path)

  val decorator = JFXDecorator(this, node, fullScreen, maximizable, minimizable)

  val scene = Scene(decorator)
  scene.stylesheets.addAll(
      JFoenixResources.load("css/jfoenix-fonts.css").toExternalForm(),
      JFoenixResources.load("css/jfoenix-design.css").toExternalForm(),
      classLoader.getResource("style/application.css")!!.toExternalForm()
  )
  this.scene = scene
  return controller
}
