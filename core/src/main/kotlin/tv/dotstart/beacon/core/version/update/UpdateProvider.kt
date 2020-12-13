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
package tv.dotstart.beacon.core.version.update

import tv.dotstart.beacon.core.version.InstabilityType
import tv.dotstart.beacon.core.version.Version

/**
 * Retrieves information on available application updates from a given remote source.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 12/12/2020
 */
interface UpdateProvider {

  /**
   * Identifies the local application version against which the provider compares remote versions.
   */
  val current: Version

  /**
   * Identifies the target stability level which is to be returned by the provider.
   *
   * When set to a value other than [InstabilityType.NONE], versions with a stability of at least
   * the given value will be returned thus returning the most recent version with the desired level
   * of stability.
   */
  val channel: InstabilityType

  /**
   * Polls the remote source for an update which matches the parameters set out by [current] and
   * [channel] respectively.
   *
   * When no newer release matches the given parameters, null is returned by this method instead.
   */
  fun check(): Update?
}
