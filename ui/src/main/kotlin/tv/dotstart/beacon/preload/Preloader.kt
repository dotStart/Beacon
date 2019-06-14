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
package tv.dotstart.beacon.preload

import javafx.application.Platform
import javafx.beans.property.*
import kotlin.concurrent.thread

/**
 * Manages the execution of pre loading components during application startup.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object Preloader {

  private val loaders = listOf<Loader>()

  private val _description = SimpleStringProperty()
  private val _percentage = SimpleDoubleProperty()
  private val _completed = SimpleBooleanProperty()

  val description: String
    get() = this._description.value
  val descriptionProperty: ReadOnlyStringProperty
    get() = this._description
  val percentage: Double
    get() = this._percentage.value
  val percentageProperty: ReadOnlyDoubleProperty
    get() = this._percentage
  val completed: Boolean
    get() = this._completed.value
  val completedProperty: ReadOnlyBooleanProperty
    get() = this._completed

  operator fun invoke(onComplete: () -> Unit) {
    thread(name = "preload") {
      Platform.runLater {
        this._percentage.set(0.0)
        this._description.set("")
        this._completed.set(false)
      }

      for (i in 0 until this.loaders.size) {
        val loader = this.loaders[i]

        Platform.runLater {
          this._percentage.set((i.toDouble() / this.loaders.size.toDouble()))
          this._description.set(loader.description)
        }

        loader.load()
      }

      Platform.runLater {
        this._percentage.set(1.0)
        this._description.set("")
        this._completed.set(true)

        onComplete()
      }
    }
  }
}