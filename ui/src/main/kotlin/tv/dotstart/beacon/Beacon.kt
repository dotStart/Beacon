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

import javafx.application.Application
import javafx.stage.Stage
import tv.dotstart.beacon.config.Configuration
import tv.dotstart.beacon.util.*
import java.nio.file.Files

/**
 * JavaFX Entry Point
 *
 * This type handles the bootstrapping of the JavaFX components (e.g. the splash screen stage) as
 * well as the backing services (such as game definition updaters, UPnP client instances, etc).
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class Beacon : Application() {

  companion object {
    private val logger = Beacon::class.logger
  }

  override fun start(stage: Stage) {
    Banner()

    if (Files.notExists(OperatingSystem.current.storage)) {
      logger.info("Creating persistence directory")
      Files.createDirectories(OperatingSystem.current.storage)
    }

    logger.info("Operating System: ${OperatingSystem.current}")
    logger.info("Persistence Directory: ${OperatingSystem.current.storage}")

    Configuration.load()

    logger.info("Displaying splash screen")
    stage.splashWindow("splash.fxml")
    stage.show()
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
  Application.launch(Beacon::class.java, *args)
}
