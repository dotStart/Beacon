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
package tv.dotstart.beacon.ui.preload

import javafx.application.Platform
import javafx.beans.property.*
import org.koin.core.component.KoinComponent
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.ui.preload.error.PreloadError
import tv.dotstart.beacon.ui.util.ErrorReporter
import tv.dotstart.beacon.ui.util.Localization
import tv.dotstart.beacon.ui.util.detailedErrorDialog
import tv.dotstart.beacon.ui.util.dialog
import kotlin.concurrent.thread

/**
 * Manages the execution of pre loading components during application startup.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class Preloader(loaders: List<Loader>) : KoinComponent {

  companion object {

    private val logger by logManager()
  }

  private val loaders = loaders
      .sortedByDescending(Loader::priority)

  private val _description = SimpleStringProperty()
  private val _percentage = SimpleDoubleProperty()
  private val _completed = SimpleBooleanProperty()

  val description: String
    get() = this._description.value ?: ""
  val descriptionProperty: ReadOnlyStringProperty
    get() = this._description
  val percentage: Double
    get() = this._percentage.value ?: 0.0
  val percentageProperty: ReadOnlyDoubleProperty
    get() = this._percentage
  val completed: Boolean
    get() = this._completed.value ?: false
  val completedProperty: ReadOnlyBooleanProperty
    get() = this._completed

  /**
   * Invokes all preloader components in an asynchronous fashion.
   */
  fun preload(onComplete: () -> Unit) {
    thread(name = "preload") {
      Platform.runLater {
        this._percentage.set(0.0)
        this._description.set("")
        this._completed.set(false)
      }

      // slight delay to prevent the screen from flickering
      Thread.sleep(1000)

      for (i in 0 until this.loaders.size) {
        val loader = this.loaders[i]
        logger.info("--- ${loader.description} ---")

        Platform.runLater {
          this._percentage.set((i.toDouble() / this.loaders.size.toDouble()))
          this._description.set(loader.description)
        }

        try {
          loader.load()
        } catch (ex: PreloadError) {
          logger.error("Preloading failed - Aborting application startup", ex)
          Platform.runLater {
            dialog(Localization("error.${ex.key}.title"), Localization("error.${ex.key}.body"))
            System.exit(1)
          }
          return@thread
        } catch (ex: Throwable) {
          logger.error("Preloading failed - Aborting application startup", ex)
          Platform.runLater {
            detailedErrorDialog(Localization("error.unknown.title"),
                                Localization("error.unknown.body"), ex)
            System.exit(128)
          }
          return@thread
        }
      }

      Thread.sleep(500)

      ErrorReporter.trace("startup", "Preloading completed")
      logger.info("--- preload end ---")

      Platform.runLater {
        this._percentage.set(1.0)
        this._description.set("")
        this._completed.set(true)

        try {
          onComplete()
        } catch (ex: Throwable) {
          logger.error("Preload completion listener failed - Aborting application startup", ex)
          detailedErrorDialog(Localization("error.unknown.title"),
                              Localization("error.unknown.body"), ex)
          System.exit(128)
        }
      }
    }
  }

  /**
   * Performs a clean shutdown of all loaders.
   */
  fun shutdown() {
    this.loaders
        .reversed()
        .forEach {
          logger.info("--- ${it.description} shutdown ---")

          try {
            it.shutdown()
          } catch (ex: Throwable) {
            logger.error("Shutdown of ${it.description} failed", ex)
          }
        }

    logger.info("--- shutdown complete ---")
  }
}
