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
package tv.dotstart.beacon.ui.util

import java.util.*

/**
 * Provides shorthand access to the application localization.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
object Localization {

  const val bundleLocation = "localization/messages"

  /**
   * Stores the resource bundle from which all messages are retrieved.
   *
   * This bundle defaults to the system locale but will simply fall back to English if the desired
   * locale is not available.
   */
  private val bundle: ResourceBundle = try {
    ResourceBundle.getBundle(bundleLocation)
  } catch (ex: MissingResourceException) {
    Localization::class.logger.warn(
        "No localization for ${Locale.getDefault()} available - Falling back to English")
    ResourceBundle.getBundle(bundleLocation, Locale.ENGLISH)
  }

  /**
   * Retrieves a given translation in its raw unformatted state.
   */
  operator fun invoke(key: String): String = try {
    bundle.getString(key)
  } catch (ex: MissingResourceException) {
    "??_${key}_??"
  }

  operator fun invoke(key: String, vararg parameters: Any?) = String.format(this(key), parameters)

  /**
   * Provides a customized bundle implementation which relies on our localization adapter to
   * resolve its messages.
   */
  object Bundle : ResourceBundle() {

    override fun getKeys(): Enumeration<String> = bundle.keys

    // FXML workaround for incomplete translations
    override fun containsKey(key: String) = true

    override fun handleGetObject(key: String) = Localization(key)
  }
}
