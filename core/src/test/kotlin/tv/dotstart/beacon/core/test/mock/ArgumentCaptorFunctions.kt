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

import org.mockito.ArgumentCaptor
import kotlin.reflect.KClass

/**
 * Provides functions which simplify the creation and interaction with argument captors.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */

/**
 * Creates an argument captor for a given Kotlin type.
 */
fun <V : Any> captor(type: KClass<V>): ArgumentCaptor<V> = ArgumentCaptor.forClass(type.java)

/**
 * Creates an argument captor for a given Kotlin type.
 */
inline fun <reified V : Any> captor() = captor(V::class)

/**
 * Captures a given value using an argument captor.
 *
 * This function is provided as a workaround to circumvent restrictions on Kotlin's nullability
 * checks.
 */
fun <V> capture(captor: ArgumentCaptor<V>): V = captor.capture()
