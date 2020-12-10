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
package tv.dotstart.beacon.ui.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

/**
 * Provides extension functions and properties which enhance logging within the application.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */

/**
 * Permits retrieving and altering of the root logger level.
 */
var rootLevel: Level
  get() = LogManager.getRootLogger().level
  set(value) {
    Configurator.setRootLevel(value)
  }

/**
 * Retrieves the logger for a given type.
 *
 * Note that this field will be inlined when accessed (e.g. is equal to a direct call to
 * LogManager#getLogger()) and should thus be cached in a property or local variable if accessed
 * more frequently.
 */
inline val <T : Any> KClass<T>.logger: Logger
  get() = LogManager.getLogger(this.java)

/**
 * Configures the root logger to write all of its logs to a given storage location.
 */
fun configureLogStorage(target: Path) {
  Files.createDirectories(target)

  System.setProperty("logPath", target.toAbsolutePath().toString())
}
