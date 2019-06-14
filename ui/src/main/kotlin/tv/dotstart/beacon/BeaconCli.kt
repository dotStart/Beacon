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
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import javafx.application.Application
import java.net.URI

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

  init {
    versionOption(BeaconMetadata.version)
  }

  override fun run() {
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
