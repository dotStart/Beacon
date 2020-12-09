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
package tv.dotstart.beacon.ui

import tv.dotstart.beacon.core.util.VersionMetadata

/**
 * Exposes application versioning information to other application components.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object BeaconUiMetadata : VersionMetadata() {

  private val unstableFlags = listOf(
      "SNAPSHOT",
      "alpha",
      "beta",
      "rc"
  )

  /**
   * Identifies whether this version of the UI is considered unstable thus potentially including
   * application breaking bugs.
   */
  val unstable by lazy {
    unstableFlags.any { this.version.contains(it, ignoreCase = true) }
  }
}
