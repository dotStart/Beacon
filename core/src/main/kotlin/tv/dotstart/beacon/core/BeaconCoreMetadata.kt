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
package tv.dotstart.beacon.core

import tv.dotstart.beacon.core.util.VersionMetadata

/**
 * Provides version metadata for the core library.
 *
 * @author Johannes Donath
 * @date 05/12/2020
 */
object BeaconCoreMetadata : VersionMetadata() {

  /**
   * Defines a human readable user agent which is transmitted along with any HTTP requests performed
   * by this library.
   */
  val userAgent: String by lazy {
    buildString {
      append("Beacon Core (")
      append(version)

      url?.let {
        append("; +")
        append(it)
      }

      append(")")
    }
  }
}
