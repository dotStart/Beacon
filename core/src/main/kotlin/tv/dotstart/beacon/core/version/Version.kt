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
package tv.dotstart.beacon.core.version

import java.util.*

/**
 * Represents a version number which has been formatted in accordance with the
 * [Semantic Versioning specification](https://semver.org/).
 *
 * Due to their formatting, version numbers may be compared against each other thus permitting
 * update checking.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 12/12/2020
 */
data class Version(

    /**
     * Specifies the major version bit.
     *
     * When incremented, this bit implies the presence of backwards incompatible changes within this
     * version.
     */
    val major: Int,

    /**
     * Specifies the minor version bit.
     *
     * When incremented, this bit implies the presence of new features which have been added in a
     * backwards compatible fashion.
     */
    val minor: Int,

    /**
     * Specifies the patch version bit.
     *
     * When incremented, this bit implies the presence of bugfixes which have been added in a
     * backwards compatible fashion.
     */
    val patch: Int,

    /**
     * Specifies an instability identifier (such as "alpha", "beta" or "snapshot").
     *
     * When set, this version is considered older than versions which lack an instability identifier
     * all together.
     *
     * Note: This implementation additionally recognizes the order of common instability
     * identifiers.
     */
    val instabilityIdentifier: String? = null,

    /**
     * Specifies an instability revision.
     *
     * When incremented, this version is considered newer than prior versions with the same
     * instability identifier.
     */
    val instabilityRevision: Int? = null,

    /**
     * Identifies the respective specific instability type exerted by a given
     * [instabilityIdentifier].
     */
    val instabilityType: InstabilityType = InstabilityType.NONE,

    /**
     * Specifies a set of build metadata (such as an SCM revision or CI build number).
     *
     * This field is entirely ignore when comparing two versions and is merely provided for
     * informational purposes. Monotonically incrementing version bits (such as CI build numbers)
     * are expected to be placed within the instability revision instead.
     */
    val metadata: String? = null) : Comparable<Version> {

  /**
   * Identifies whether this version is considered unstable (thus carrying an instability type).
   */
  val unstable: Boolean = instabilityType != InstabilityType.NONE

  companion object {

    private const val bitSeparator = '.'
    private const val instabilitySeparator = '-'
    private const val metadataSeparator = '+'

    /**
     * Parses a given version number in accordance with the SemVer specification.
     *
     * @throws IllegalArgumentException when the given version number is malformed.
     */
    fun parse(value: String): Version {
      val metadataOffset = value.indexOf(metadataSeparator)
          .takeIf { it != -1 }
      val instabilityOffset = value.indexOf(instabilitySeparator)
          .takeIf { it != -1 }
          ?.takeIf { metadataOffset == null || it < metadataOffset }

      val metadata = metadataOffset
          ?.let { value.substring(it + 1) }
          ?.also {
            require(it.isNotBlank()) { "Expected metadata but got empty string: $value" }
          }
      val instabilityStr = instabilityOffset
          ?.let {
            if (metadataOffset != null) {
              value.substring(it + 1, metadataOffset)
            } else {
              value.substring(it + 1)
            }
          }
          ?.also {
            require(it.isNotBlank()) { "Expected instability flag but got empty string: $value" }
          }
      val mainStr = when {
        instabilityOffset != null -> value.substring(0, instabilityOffset)
        metadataOffset != null -> value.substring(0, metadataOffset)
        else -> value
      }

      val splitStr = mainStr
          .split(bitSeparator)
      require(splitStr.size in 2..3) { "Expected 2 or 3 version bits: $value" }

      val major = try {
        splitStr[0]
            .toInt(10)
            .also {
              require(it >= 0) { "Negative major version bit: $value" }
            }
      } catch (ex: NumberFormatException) {
        throw IllegalArgumentException("Malformed major version bit: $value", ex)
      }
      val minor = try {
        splitStr[1]
            .toInt(10)
            .also {
              require(it >= 0) { "Negative minor version bit: $value" }
            }
      } catch (ex: NumberFormatException) {
        throw IllegalArgumentException("Malformed minor version bit: $value", ex)
      }
      val patch = try {
        splitStr
            .takeIf { it.size == 3 }
            ?.let { it[2].toInt() }
            ?.also {
              require(it >= 0) { "Negative patch version bit: $value" }
            }
            ?: 0
      } catch (ex: NumberFormatException) {
        throw IllegalArgumentException("Malformed patch version bit: $value", ex)
      }

      val instabilityRevision = instabilityStr
          ?.let { instabilityFlag ->
            instabilityFlag.lastIndexOf(bitSeparator)
                .takeIf { it != -1 }
                ?.let { instabilityFlag.substring(it + 1) }
                ?.toIntOrNull()
          }
      val instabilityIdentifier = if (instabilityRevision != null) {
        instabilityStr
            .lastIndexOf(bitSeparator)
            .let { instabilityStr.substring(0, it) }
      } else {
        instabilityStr
      }

      if (instabilityIdentifier != null) {
        require(instabilityIdentifier.isNotBlank()) {
          "Expected instability identifier but got empty string: $value"
        }
      }

      val instabilityType = instabilityIdentifier
          ?.let { instabilityFlag ->
            instabilityFlag.indexOf(bitSeparator)
                .takeIf { it != -1 }
                ?.let { instabilityFlag.substring(0, it) }
                ?: instabilityFlag
          }
          ?.let(InstabilityType.Companion::ofFlag)
          ?: if (major == 0) {
            InstabilityType.INCUBATION
          } else {
            InstabilityType.NONE
          }

      return Version(major, minor, patch,
                     instabilityIdentifier, instabilityRevision, instabilityType,
                     metadata)
    }
  }

  override fun compareTo(other: Version): Int {
    this.major.compareTo(other.major)
        .takeIf { it != 0 }
        ?.coerceIn(-1, 1)
        ?.also { return it }

    this.minor.compareTo(other.minor)
        .takeIf { it != 0 }
        ?.coerceIn(-1, 1)
        ?.also { return it }

    this.patch.compareTo(other.patch)
        .takeIf { it != 0 }
        ?.coerceIn(-1, 1)
        ?.also { return it }

    this.instabilityType.compareTo(other.instabilityType)
        .takeIf { it != 0 }
        ?.coerceIn(-1, 1)
        ?.also { return it }

    if (this.instabilityType != InstabilityType.NONE) {
      (this.instabilityRevision ?: 0).compareTo(other.instabilityRevision ?: 0)
          .takeIf { it != 0 }
          ?.coerceIn(-1, 1)
          ?.also { return it }
    }

    return 0
  }


  override fun toString() = buildString {
    append(major)
    append(bitSeparator)
    append(minor)
    append(bitSeparator)
    append(patch)

    if (instabilityIdentifier != null) {
      append(instabilitySeparator)
      append(instabilityIdentifier)

      if (instabilityRevision != null) {
        append(bitSeparator)
        append(instabilityRevision)
      }
    }

    if (metadata != null) {
      append(metadataSeparator)
      append(metadata)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Version) return false

    if (this.major != other.major) return false
    if (this.minor != other.minor) return false
    if (this.patch != other.patch) return false
    if (this.instabilityIdentifier != other.instabilityIdentifier) return false
    if (this.instabilityRevision != other.instabilityRevision) return false

    return true
  }

  override fun hashCode(): Int {
    return Objects.hash(this.major, this.minor, this.patch,
                        this.instabilityIdentifier, this.instabilityRevision)
  }
}
