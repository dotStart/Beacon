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
package tv.dotstart.beacon.core.test.mock

import org.mockito.Mockito

/**
 * Provides functions which simplify the interaction with argument matchers.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 04/12/2020
 */

/**
 * Matches an arbitrary argument value.
 *
 * This function is provided as a workaround to circumvent Kotlin's null checking.
 */
fun <T> any(): T = Mockito.any()

/**
 * Matches an arbitrary boolean value.
 *
 * This function is provided as a workaround to circumvent Kotlin's null checking.
 */
fun anyBoolean(): Boolean {
  Mockito.anyBoolean()
  return false
}
