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
package tv.dotstart.beacon.core.util

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Provides a listing of recognized operating systems.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
enum class OperatingSystem(vararg patterns: String) {

  LINUX("linux"),
  MAC_OS("mac"),
  UNIX("nix"),
  WINDOWS("win") {

    override val storageDirectory: Path by lazy {
      Paths.get(System.getenv("APPDATA"))
    }

    override val storageDirectoryPrefix = ""

    override fun resolveApplicationDirectory(name: String): Path =
        this.storageDirectory.resolve(storageDirectoryPrefix + name)
  },

  /**
   * Fallback Value
   *
   * This property indicates that the executing operating system is not within this list of
   * recognized implementations. As such, safe fallback values will be returned for any OS specific
   * values.
   */
  UNKNOWN;

  /**
   * Identifies whether this operating system is currently active (e.g. is executing the
   * application).
   */
  open val active: Boolean by lazy {
    val value = System.getProperty("os.name", "unknown")
    patterns.any { value.contains(it, ignoreCase = true) }
  }

  /**
   * Identifies the location at which user specific files (such as configuration files) are to be
   * stored.
   */
  open val storageDirectory: Path by lazy {
    Paths.get(System.getProperty("user.home", "."))
  }

  /**
   * Identifies a prefix which is to be prepended to the application directory when storing data
   * inside the [storageDirectory].
   */
  open val storageDirectoryPrefix = "."

  /**
   * Resolves an application specific storage directory for the executing user.
   */
  open fun resolveApplicationDirectory(name: String): Path =
      this.storageDirectory.resolve(storageDirectoryPrefix + name.lowercase())

  companion object {

    /**
     * Retrieves the current operating system.
     *
     * Note that the [active] constant may not necessarily be set to true on the returned
     * constant. This is typically the case when [UNKNOWN] is returned.
     */
    val current: OperatingSystem by lazy {
      values()
          .find(OperatingSystem::active)
          ?: UNKNOWN
    }
  }

  /**
   * Executes the given code block when this operating system is currently executing.
   *
   * When the detected operating system differs, the call will be omitted and a default value is
   * returned instead.
   */
  fun <R> runIf(block: () -> R, defaultValue: R): R {
    if (current == this) {
      return block()
    }

    return defaultValue
  }

  fun runIf(block: () -> Unit) = this.runIf(block, Unit)

  /**
   * Executes the given code block when this operating system is not currently executing.
   *
   * When the detected operating system matches, the call will be omitted and a default value is
   * returned instead.
   */
  fun <R> runUnless(block: () -> R, defaultValue: R): R {
    if (current != this) {
      return block()
    }

    return defaultValue
  }

  fun runUnless(block: () -> Unit) = this.runUnless(block, Unit)
}
