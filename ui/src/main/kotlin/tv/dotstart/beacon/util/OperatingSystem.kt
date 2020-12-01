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
package tv.dotstart.beacon.util

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Provides a listing of recognized operating systems and their respective associated
 * characteristics such as persistent storage directories.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
enum class OperatingSystem {

  LINUX {

    override fun match(value: String) = value.containsAny("linux", ignoreCase = true)
  },
  MAC_OS {

    override fun match(value: String) = value.containsAny("mac", ignoreCase = true)
  },
  UNIX {

    override fun match(value: String) = value.containsAny("nix", ignoreCase = true)
  },
  WINDOWS {

    override val storage: Path by lazy {
      val userDirectory = Paths.get(System.getenv("APPDATA"))
      userDirectory.resolve("Beacon")
    }

    override fun match(value: String) = value.containsAny("win", ignoreCase = true)
  },

  /**
   * Fallback Value
   *
   * This constant is used when none of the known constants match the JVM's detected operating
   * system.
   */
  UNKNOWN {

    override fun match(value: String) = false
  };

  /**
   * Identifies whether this particular operating system is currently being executed (e.g. acts as
   * a host to this application).
   *
   * Unless overridden by the respective OS constant, this value will by lazily initialized and
   * persisted throughout the entire lifetime of the application.
   */
  open val executing: Boolean by lazy {
    val value = System.getProperty("os.name", "unknown")
    this.match(value)
  }

  /**
   * Identifies the location of the directory which will contain the persisted configuration
   * information on this particular operating system (if selected).
   *
   * This value is computed lazily and should not be accessed unless the given operating system is
   * detected by this implementation's companion object.
   */
  open val storage: Path by lazy {
    val userDirectory = Paths.get(System.getProperty("user.home", "."))
    userDirectory.resolve(".beacon")
  }

  companion object {

    /**
     * Retrieves the current operating system.
     *
     * Note that the [executing] constant may not necessarily be set to true on the returned
     * constant. This is typically the case when [UNKNOWN] is returned instead of a real
     * constant (e.g. the fallback is chosen).
     *
     * This value is lazily initialized by this implementation and persisted throughout the
     * entirety of the application lifetime.
     */
    val current: OperatingSystem by lazy {
      values()
          .find(OperatingSystem::executing)
          ?: UNKNOWN
    }
  }

  /**
   * Evaluates whether this particular definition matches the given os.name definition returned by
   * the JVM.
   */
  protected abstract fun match(value: String): Boolean

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
