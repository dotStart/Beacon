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
package tv.dotstart.beacon.core.update

/**
 * Provides a listing of recognizes instability types along with their respective associated
 * instability flag strings.
 *
 * The values within this definition are ordered in accordance with their respective maturity thus
 * permitting comparisons against their respective ordinal numbers. Increasing ordinals refer to
 * increased maturity.
 *
 * @author Johannes Donath
 * @date 12/12/2020
 */
enum class InstabilityType(vararg flags: String) {

  /**
   * Placeholder value which is chosen when a given instability flag has not is unknown to the
   * implementation.
   */
  UNKNOWN,

  /**
   * Identifies a snapshot release.
   *
   * This instability type is typically set for builds which were created by a CI tool.
   */
  SNAPSHOT("snapshot"),

  /**
   * Identifies an alpha release.
   *
   * This instability type is typically applied to builds which are in early testing and subject to
   * rapid changes.
   */
  ALPHA("alpha", "a"),

  /**
   * Identifies a beta release.
   *
   * This instability type is typically applied to builds which remain in early testing but are
   * approaching the desired implementation state.
   */
  BETA("beta", "b"),

  /**
   * Identifies a release candidate.
   *
   * This instability type is typically applied to builds when a feature freeze has been put in
   * place thus only permitting bugfixes on a given release.
   */
  RELEASE_CANDIDATE("rc"),

  /**
   * Placeholder value which is chosen when the major bit within a version number is set to zero.
   */
  INCUBATION,

  /**
   * Placeholder value which is chosen when no instability flag has been set on a given version thus
   * giving it the highest maturity rating.
   */
  NONE;

  /**
   * Stores a listing of recognized flags for this instability type.
   */
  private val flags = flags.toList()

  companion object {

    /**
     * Locates a given type of instability based on its respective release flag.
     */
    fun ofFlag(flag: String) = values()
        .find { flag.toLowerCase() in it.flags }
        ?: UNKNOWN
  }
}
