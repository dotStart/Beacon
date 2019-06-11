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

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.reflect.KClass

/**
 * Provides extension functions and properties which enhance logging within the application.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */

/**
 * Retrieves the logger for a given type.
 *
 * Note that this field will be inlined when accessed (e.g. is equal to a direct call to
 * LogManager#getLogger()) and should thus be cached in a property or local variable if accessed
 * more frequently.
 */
inline val KClass<Any>.logger: Logger
  get() = LogManager.getLogger(this.java)
