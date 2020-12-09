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
package tv.dotstart.beacon.ui.delegate

import javafx.beans.property.BooleanProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Provides a delegate which forwards all of its read and write operations to a designated JavaFX
 * observable property.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 09/12/2020
 */
class BooleanPropertyReadWriteProperty(private val property: BooleanProperty)
  : ReadWriteProperty<Any, Boolean> {

  override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = this.property.value

  override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
    this.property.set(value)
  }
}

/**
 * Shorthand constructor for [BooleanPropertyReadWriteProperty].
 */
@JvmName("booleanProperty")
fun property(property: BooleanProperty) = BooleanPropertyReadWriteProperty(property)
