package tv.dotstart.beacon.ui

import tv.dotstart.beacon.core.util.OperatingSystem
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Provides functions which simplify the interaction with debug cookies.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 26/12/2020
 */

/**
 * Identifies the location at which the debug cookie file is placed by the application.
 *
 * When this file exists, debug logging is enabled regardless of the specified command line
 * arguments thus permitting easy access to debug logging for inexperienced users.
 */
val debugCookiePath: Path by lazy {
  OperatingSystem.current
      .resolveApplicationDirectory(applicationName)
      .resolve("debug.txt")
}

/**
 * Identifies or sets whether the debug cookie is currently set.
 *
 * @see debugCookiePath
 */
var debugCookie: Boolean
  get() = Files.exists(debugCookiePath)
  set(value) {
    try {
      if (value) {
        Files.createFile(debugCookiePath)
      } else {
        Files.deleteIfExists(debugCookiePath)
      }
    } catch (ignore: IOException) {
    }
  }
