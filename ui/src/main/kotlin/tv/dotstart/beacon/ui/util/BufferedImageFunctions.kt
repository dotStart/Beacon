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
package tv.dotstart.beacon.ui.util

import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import java.awt.image.BufferedImage

/**
 * Provides functions which simplify the interaction with buffered images.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 05/12/2020
 */

/**
 * Converts a given image to its respective JavaFX representation.
 */
fun BufferedImage.toImage(): Image {
  val target = WritableImage(this.width, this.height)
  val writer = target.pixelWriter

  (0 until this.height).forEach { y ->
    (0 until this.width).forEach { x ->
      val color = this.getRGB(x, y)
      writer.setArgb(x, y, color)
    }
  }

  return target
}
