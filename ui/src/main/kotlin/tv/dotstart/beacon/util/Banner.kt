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

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Writes an application banner to the command line.
 *
 * Note that this implementation circumvents the logger in order to prevent unnecessary spam. The
 * banner is expected to be present as a "banner.txt" resource within the jar root.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object Banner {

  private val logger = Banner::class.logger

  operator fun invoke() {
    val path = this.javaClass.getResource("/banner.txt")
        ?.let { Paths.get(it.toURI()) }
    if (path == null) {
      logger.error("Failed to load application banner: No such file or directory")
      return
    }

    Files.readAllLines(path, StandardCharsets.UTF_8)
        .forEach { println(it) }
  }
}
