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
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.RollingFileAppender
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.layout.PatternLayout
import tv.dotstart.beacon.util.Banner
import tv.dotstart.beacon.util.OperatingSystem
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.Duration


/**
 * Provides a CLI entry point which permits the customization of application parameters.
 */
object BeaconCli : CliktCommand(name = "Beacon") {

  /**
   * Defines a listing of default system repositories which are queried when no custom overrides are
   * given.
   */
  private val defaultSystemRepositories = listOf(
      URI.create("github://dotStart/Beacon#indie.dat"),
      URI.create("github://dotStart/Beacon#steam.dat"),
      URI.create("github://dotStart/Beacon#tools.dat")
  )

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
      .multiple(defaultSystemRepositories)

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
    registerFileAppender()

    val logger = LogManager.getLogger(Beacon::class.java)

    logger.info("Operating System: ${OperatingSystem.current}")
    logger.info("Persistence Directory: ${OperatingSystem.current.storage}")

    if (this.defaultSystemRepositories != this.systemRepositories) {
      logger.warn(
          "System repositories have been overridden - Some standard services may be missing")
    }

    logger.info(
        "System Repositories (${systemRepositories.size}): ${systemRepositories.joinToString()}")

    if (this.verbose || this.debug) {
      Configurator.setRootLevel(
          if (this.verbose) {
            Level.ALL
          } else {
            Level.DEBUG
          }
      )

      if (this.verbose) {
        logger.warn("Enabled VERBOSE logging - This may cause significant log output")
      } else {
        logger.warn("Enabled DEBUG logging")
      }
    }

    if (this.disableCache) {
      logger.warn("Caching has been disabled - Performance may be degraded")
    } else {
      logger.info("Cache duration has been set to $cacheDuration")
    }

    // we do not pass any of our arguments to JavaFX since there's nothing special to handle here
    Application.launch(Beacon::class.java)
  }

  private fun registerFileAppender() {
    val ctx = LoggerContext.getContext(false)
    val root = ctx.rootLogger
    val cfg = ctx.configuration
    val layout = PatternLayout.createLayout("[%d{HH:mm:ss}] [%25.25t] [%level]: %msg%n", null, cfg,
        null, StandardCharsets.UTF_8, true, false, null, null)

    val logDirectory = OperatingSystem.current.storage.resolve("log")
    Files.createDirectories(logDirectory)

    val policy = OnStartupTriggeringPolicy.createPolicy()
    val strategy = DefaultRolloverStrategy.createStrategy("10", "1", "min", null, null, true, cfg)

    val fileAppender = RollingFileAppender.createAppender(
        logDirectory.resolve("latest.log").toAbsolutePath().toString(),
        logDirectory.resolve("beacon.log").toAbsolutePath().toString() + ".%i",
        "true", "File", null, null, "true",
        policy, strategy, layout, null, null, null, null, cfg
    )

    fileAppender.start()

    cfg.addAppender(fileAppender)
    root.addAppender(fileAppender)
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
  // register Log4j as the JDK logging manager as early as possible for third party library
  // logging support
  System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")

  BeaconCli.main(args)
}
