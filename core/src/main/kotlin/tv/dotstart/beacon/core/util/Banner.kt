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

import java.io.BufferedInputStream
import java.nio.charset.StandardCharsets

/**
 * Writes an application banner to the command line.
 *
 * Note that this implementation circumvents the logger in order to prevent unnecessary spam. The
 * banner is expected to be present as a "banner.txt" resource within the jar root.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object Banner {

  operator fun invoke() {
    val banner = this.javaClass.getResourceAsStream("/banner.txt").use {
      BufferedInputStream(it).use {
        String(it.readAllBytes(), StandardCharsets.UTF_8)
      }
    }

    banner.lineSequence()
        .forEach(::println)
  }
}
