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
import javafx.application.Platform
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.dotstart.beacon.config.configModule
import tv.dotstart.beacon.core.cache.CacheProvider
import tv.dotstart.beacon.core.cache.NoopCacheProvider
import tv.dotstart.beacon.core.cache.filesystem.FileSystemCache
import tv.dotstart.beacon.core.cache.filesystem.path.Murmur3PathProvider
import tv.dotstart.beacon.core.util.Banner
import tv.dotstart.beacon.core.util.OperatingSystem
import tv.dotstart.beacon.exposure.exposureModule
import tv.dotstart.beacon.preload.Preloader
import tv.dotstart.beacon.repository.repositoryModule
import tv.dotstart.beacon.util.Localization
import tv.dotstart.beacon.util.configureLogStorage
import tv.dotstart.beacon.util.detailedErrorDialog
import tv.dotstart.beacon.util.rootLevel
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import javax.swing.JOptionPane


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
      names = arrayOf("--repository", "-r"),
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
   * Defines the location in which log files shall be placed by the application.
   *
   * Defaults to the operating system dependent storage directory (e.g. APPDATA on Windows, Home on
   * NIX based systems, etc).
   */
  val logDirectory: Path by option(
      "--log-dir",
      help = "Specifies the log storage directory")
      .convert { Paths.get(it) }
      .defaultLazy { OperatingSystem.current.storageDirectory.resolve("log") }

  /**
   * Enables global debug logging.
   */
  val debug: Boolean by option(
      "--debug",
      help = "Enables debug logging")
      .flag()

  /**
   * Enables global trace logging.
   */
  val verbose: Boolean by option(
      "--verbose",
      help = "Enables verbose logging")
      .flag()

  init {
    versionOption(BeaconUiMetadata.version)
  }

  @KoinApiExtension
  override fun run() {
    // stash the desired logging path as early as possible to make sure Logger construction does not
    // fail due to missing system properties
    configureLogStorage(this.logDirectory)

    val logger = LogManager.getLogger(BeaconApplication::class.java)

    val bytecodeVersion = System.getProperty("java.class.version", "").toFloatOrNull() ?: 53f
    if (bytecodeVersion < 53) {
      logger.error("Detected native Bytecode version $bytecodeVersion which is incompatible")
      logger.error("Launch has been aborted - Cannot recover")

      JOptionPane.showMessageDialog(null,
                                    "You are running an outdated version of Java\nYou will need Java 9 or newer to run this application",
                                    "Outdated Java Version",
                                    JOptionPane.ERROR_MESSAGE)
      return
    }

    Banner()

    Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
      logger.error("Uncaught exception on thread \"${thread.name}\" (#${thread.id})", ex)

      Platform.runLater {
        detailedErrorDialog(Localization("error.unknown.title"),
                            Localization("error.unknown.body"), ex)
        System.exit(128)
      }
    }

    logger.info("Application Version: ${BeaconUiMetadata.version}")
    logger.info("Native Bytecode Version: $bytecodeVersion")
    logger.info("Operating System: ${OperatingSystem.current}")
    logger.info("Persistence Directory: ${OperatingSystem.current.storageDirectory}")

    if (this.defaultSystemRepositories != this.systemRepositories) {
      logger.warn(
          "System repositories have been overridden - Some standard services may be missing")
    }

    logger.info(
        "System Repositories (${systemRepositories.size}): ${systemRepositories.joinToString()}")

    if (this.verbose || this.debug) {
      val level = if (this.verbose) {
        Level.ALL
      } else {
        Level.DEBUG
      }

      rootLevel = level
      logger.info("Adjusted the application log level to $level")
    }

    if (this.disableCache) {
      logger.warn("Caching has been disabled - Performance may be degraded")
    } else {
      logger.info("Cache duration has been set to $cacheDuration")
    }

    val uiModule = module {
      single(named("storagePath")) {
        OperatingSystem.current.resolveApplicationDirectory("Beacon")
      }
      single(named("cachePath")) {
        get<Path>(named("storagePath")).resolve("cache")
      }

      single(named("systemRepositories")) { systemRepositories }

      if (disableCache) {
        single<CacheProvider> { NoopCacheProvider }
      } else {
        single<CacheProvider> {
          val cachePath = get<Path>(named("cachePath"))

          FileSystemCache(cachePath, cacheDuration,
                          pathProvider = Murmur3PathProvider(424242L))
        }
      }

      single { Preloader(getAll()) }
    }

    startKoin {
      modules(configModule)
      modules(exposureModule)
      modules(repositoryModule)

      modules(uiModule)
    }

    // we do not pass any of our arguments to JavaFX since there's nothing special to handle here
    Application.launch(BeaconApplication::class.java)
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
