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
package tv.dotstart.beacon.ui.util

import com.mindscapehq.raygun4java.core.RaygunClient
import com.mindscapehq.raygun4java.core.messages.RaygunBreadcrumbLevel
import tv.dotstart.beacon.ui.BeaconUiMetadata

/**
 * Exports error reports to an external service provider if available.
 *
 * Authentication information is provided as part of the `raygun.token` resource file in the root
 * of the application Class-Path.
 *
 * @author Johannes Donath
 * @date 02/12/2020
 */
object ErrorReporter {

  private val logger = ErrorReporter::class.logger

  /**
   * Permanently stores the authentication token which is to be transmitted for all requests to
   * the error reporting backend.
   *
   * When no authentication token has been configured, null is stored within this property instead.
   */
  private val token = System.getProperty("raygun.token")
      ?.takeIf(String::isNotBlank)
      ?: this::class.java.getResourceAsStream("/raygun.token")
          ?.readAllBytes()
          ?.toString(Charsets.UTF_8)
          ?.takeIf(String::isNotBlank)
          ?.trim()

  /**
   * Evaluates whether error reporting is available.
   */
  val available: Boolean by lazy { token != null }

  /**
   * Caches the error reporting client once initialized.
   */
  private val client by lazy {
    token?.let {
      RaygunClient(it)
          .apply {
            setVersion(BeaconUiMetadata.version)

            withData("javaVersion", System.getProperty("java.version", "unknown"))
            withData("javaVendor", System.getProperty("java.vendor", "unknown"))
            withData("javaBytecodeVersion", System.getProperty("java.class.version", "unknown"))
          }
    }
  }

  /**
   * Submits an application error if authentication is present within the application Class-Path.
   *
   * When no authentication is given, this method simply acts as a no-op.
   */
  operator fun invoke(ex: Throwable) {
    val client = client
    if (client == null) {
      logger.error("Cannot submit error report without valid authentication", ex)
      return
    }

    val responseCode = client.send(ex)
    logger.info("Submitted error report (server responded with code $responseCode)")
  }

  /**
   * Records a trace of an action performed within the application.
   *
   * This information is transmitted along with error records if submitted thus providing additional
   * information on how to reproduce a given error within the application.
   */
  fun trace(category: String,
            message: String,
            level: RaygunBreadcrumbLevel = RaygunBreadcrumbLevel.INFO,
            data: Map<String, Any?> = emptyMap()) {
    val client = client
    if (client == null) {
      logger.debug("Ignoring breadcrumb of level $level for category \"$category\": $message")
      return
    }

    client.recordBreadcrumb(message)
        .withLevel(level)
        .withCategory(category)
        .withCustomData(data)
  }
}
