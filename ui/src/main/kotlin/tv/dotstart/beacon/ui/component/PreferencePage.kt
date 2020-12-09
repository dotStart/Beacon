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
package tv.dotstart.beacon.ui.component

import javafx.beans.DefaultProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.layout.VBox
import tv.dotstart.beacon.ui.delegate.property

/**
 * Represents a single preference page with its content and title respectively.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 09/12/2020
 */
@DefaultProperty("children")
class PreferencePage : VBox() {

  /**
   * Defines a human readable name with which this preference page is addressed within the view.
   */
  val titleProperty: StringProperty = SimpleStringProperty()

  /**
   * @see titleProperty
   */
  var title by property(titleProperty)
}
