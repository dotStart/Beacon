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

import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.*

/**
 * Provides a base for version metadata objects which retrieve their information via a properties
 * file within the application Class-Path.
 *
 * This implementation is primarily used within the core and UI modules in order to gain access to
 * information such as the version number, official repository, branding information, etc.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 05/12/2020
 */
abstract class VersionMetadata {

  /**
   * Defines an identifier with which the location of version files is distinguished.
   *
   * This property is primarily used in order to differentiate the versions of multiple libraries
   * within the same Class-Path (as is the case within the UI and CLI modules respectively).
   *
   * By default, this property will simply return the name of the package in which it is located as
   * the metadata object is typically placed within the root package of a library or application.
   */
  protected open val id: String
    get() = this::class.java.packageName

  /**
   * Provides a set of properties set for this library or application version.
   *
   * When no version metadata is present within the application Class-Path upon construction, an
   * empty properties object is constructed instead. As such, the information within the property
   * object should be treated as incomplete and potentially invalid.
   */
  protected val properties: Properties = Properties()
      .also { properties ->
        try {
          this::class.java.getResourceAsStream("$resourcePrefix$id")
              ?.let(properties::load)
        } catch (ex: IOException) {
          val logger = LogManager.getLogger(this::class.java)
          logger.warn("Failed to parse version metadata for \"$id\"", ex)
        }
      }

  /**
   * Defines a human readable version identifier for this particular application or library.
   *
   * This property should follow the rules outlined within the Semantic Versioning specification
   * and may thus also be parsed in order to compare against other application versions or
   * verify compatibility constraints.
   */
  val version: String by lazy { this.properties.getProperty("version", "0.0.0+dev") }

  /**
   * Defines the URL at which more information about the application or library may be
   * acquired (typically a homepage or repository address).
   *
   * This value may be incorporated into certain application aspects such as user agents and may
   * thus be visible to remote servers.
   */
  val url: String? by lazy { this.properties.getProperty("url") }

  companion object {

    /**
     * Defines the location at which version metadata resources are placed.
     */
    private const val resourcePrefix = "/META-INF/metadata/"
  }
}
