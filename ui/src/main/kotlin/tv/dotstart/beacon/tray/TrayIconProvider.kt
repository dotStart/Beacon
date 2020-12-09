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
package tv.dotstart.beacon.tray

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.delegate.property
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage

/**
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 09/12/2020
 */
class TrayIconProvider(icon: BufferedImage?) {

  private val callbacks = mutableListOf<() -> Unit>()

  /**
   * Evaluates whether tray icons are supported within the current execution environment.
   */
  val supported: Boolean
    get() = SystemTray.isSupported()

  val visibleProperty: BooleanProperty = SimpleBooleanProperty()
  var visible by property(visibleProperty)

  private val icon: TrayIcon?

  companion object {

    private val logger by logManager()
  }

  init {
    this.visibleProperty.addListener { _, _, newValue ->
      if (newValue) {
        this.show()
      } else {
        this.hide()
      }
    }

    this.icon = TrayIcon(icon, "Beacon")
        .also {
          it.addActionListener {
            Platform.runLater {
              this.callbacks.forEach { it() }
            }
          }
        }
  }

  fun registerCallback(block: () -> Unit) {
    this.callbacks += block
  }

  /**
   * Registers the tray icon with the system tray.
   */
  fun show() {
    val icon = this.icon
        ?: return

    val tray = SystemTray.getSystemTray()
        ?: return

    logger.info("Displaying tray icon")
    tray.add(icon)
  }

  /**
   * Un-registers the tray icon with the system tray.
   */
  fun hide() {
    val icon = this.icon
        ?: return
    val tray = SystemTray.getSystemTray()
        ?: return

    logger.info("Hiding tray icon")
    tray.remove(icon)
  }
}
