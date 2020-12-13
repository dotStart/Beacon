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
package tv.dotstart.beacon.ui.preload

import com.jfoenix.controls.JFXButton
import javafx.application.Platform
import javafx.event.EventHandler
import tv.dotstart.beacon.core.version.update.UpdateProvider
import tv.dotstart.beacon.ui.util.Localization
import tv.dotstart.beacon.ui.util.dialog
import java.awt.Desktop
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CyclicBarrier

/**
 * Provides a preloader implementation which evaluates whether an update is available for download
 * and redirects the user if asked to.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 13/12/2020
 */
class UpdateCheckLoader(private val provider: UpdateProvider) : Loader {

  override val description = "update"

  override val priority = Int.MAX_VALUE

  override fun load() {
    val update = this.provider.check()
        ?: return

    // this is a bit ugly but this check is performed on the preload thread thus preventing us from
    // displaying a dialog here
    val barrier = CyclicBarrier(2)

    Platform.runLater {
      dialog(Localization("warning.update"), Localization("warning.update.body"), listOf(
          JFXButton(Localization("warning.update.download"))
              .also {
                it.onAction = EventHandler {
                  Desktop.getDesktop().browse(update.url.toURI())
                  System.exit(0)
                }
              }
      ))

      barrier.await()
    }

    barrier.await()
  }
}
