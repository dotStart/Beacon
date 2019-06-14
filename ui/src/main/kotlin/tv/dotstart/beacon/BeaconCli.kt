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
package tv.dotstart.beacon

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import javafx.application.Application
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import tv.dotstart.beacon.util.Banner
import java.net.URI
import java.time.Duration

/**
 * Provides a CLI entry point which permits the customization of application parameters.
 */
object BeaconCli : CliktCommand(name = "Beacon") {

  /**
   * Specifies a listing of system repositories which are to be queried.
   *
   * This value is typically hardcoded but may be overridden for development purposes (e.g. to test
   * an unpublished system repository).
   */
  val systemRepositories: List<URI> by option(
      names = *arrayOf("--repository", "-r"),
      help = "Specifies a custom system repository (Overrides any default system repositories)")
      .convert { URI.create(it) }
      .multiple(listOf(
          URI.create("github://dotStart/Beacon")
      ))

  /**
   * When enabled, this flag causes all cache checks to fail (e.g. files such as repositories and
   * icons will be reloaded immediately).
   */
  val disableCache: Boolean by option(
      "--disable-cache",
      help = "Disables all caching measures")
      .flag()

  /**
   * Defines the minimum amount of time that has to pass between repository cache refreshes.
   */
  val cacheDuration: Duration by option(
      "--cache-duration",
      help = "Specifies the duration for which repositories will be cached locally")
      .convert { Duration.parse(it) }
      .default(Duration.ofMinutes(10))

  /**
   * Enables global debug logging.
   */
  val debug: Boolean by option("--debug",
      help = "Enables debug logging")
      .flag()

  /**
   * Enables global trace logging.
   */
  val verbose: Boolean by option("--verbose",
      help = "Enables verbose logging")
      .flag()

  init {
    versionOption(BeaconMetadata.version)
  }

  override fun run() {
    Banner()

    if (this.verbose || this.debug) {
      Configurator.setRootLevel(
          if (this.verbose) {
            Level.ALL
          } else {
            Level.DEBUG
          }
      )

      val logger = LogManager.getLogger(Beacon::class.java)
      if (this.verbose) {
        logger.warn("Enabled VERBOSE logging - This may cause significant log output")
      } else {
        logger.warn("Enabled DEBUG logging")
      }
    }

    // we do not pass any of our arguments to JavaFX since there's nothing special to handle here
    Application.launch(Beacon::class.java)
  }
}

/**
 * JVM Entry Point
 *
 * Note that this method will immediately initialize JavaFX before any other services are referenced
 * in order to give the framework a chance to initialize its threads and take control of the JVM
 * main thread.
 *
 * All following logic will be invoked from JFX managed threads.
 */
fun main(args: Array<String>) {
  BeaconCli.main(args)
}
