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

import com.jfoenix.controls.JFXDecorator
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.nio.file.NoSuchFileException

/**
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */

/**
 * Loads an FXML resource from the given location.
 */
fun <T : Any> fxml(path: String): T {
  val classLoader = Thread.currentThread().contextClassLoader
  val resource = classLoader.getResource("fxml/$path")
      ?: throw FileNotFoundException("No such FXML resource: $path")

  val loader = FXMLLoader(resource)
  loader.charset = StandardCharsets.UTF_8
  return loader.load()
}

/**
 * Creates an undecorated window from the given FXML resource.
 */
fun Stage.splashWindow(path: String): Scene {
  this.initStyle(StageStyle.UNDECORATED)

  val node = fxml<Parent>(path)

  val scene = Scene(node)
  this.scene = scene
  return scene
}

/**
 * Creates a decorated window from the given FXML resource.
 */
fun Stage.window(path: String, fullScreen: Boolean = false, maximizable: Boolean = true,
    minimizable: Boolean = true): Scene {
  val node = fxml<Node>(path)

  val decorator = JFXDecorator(this, node, fullScreen, maximizable, minimizable)

  val scene = Scene(decorator)
  this.scene = scene
  return scene
}
