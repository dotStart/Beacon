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

/**
 * Provides a standardized loader interface which abstracts the initialization of application
 * state while the splash screen is up.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
interface Loader {

  /**
   * Provides a human readable description for this particular loader step.
   *
   * This value is displayed as part of the splash screen in order to inform the user of the current
   * ongoing process.
   */
  val description: String

  /**
   * Handles the preloading for this particular manager implementation.
   */
  fun load()
}
