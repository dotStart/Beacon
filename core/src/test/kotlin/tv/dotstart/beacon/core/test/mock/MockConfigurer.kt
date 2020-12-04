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
import org.mockito.internal.util.MockUtil
import kotlin.reflect.KClass

/**
 * Permits the configuration of implementation aspects on a given mock.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
class MockConfigurer<M : Any> internal constructor(private val mock: M) {

  /**
   * Configures the implementation of a given target function on this mock object.
   */
  fun <R> on(target: M.() -> R) = MockFunctionConfigurer<M, R>(this.mock, target)
}

/**
 * Allocates a mock for a given object type.
 *
 * Callers may optionally configure the implementations of mocked functions through the [config]
 * parameter. When omitted, all functions simply return null.
 */
fun <M : Any> mock(type: KClass<M>, config: MockConfigurer<M>.() -> Unit = {}): M {
  val obj = Mockito.mock(type.java)
  return mock(obj, config)
}

/**
 * Allocates a mock for a given object type.
 *
 * Callers may optionally configure the implementations of mocked functions through the [config]
 * parameter. When omitted, all functions simply return null.
 */
inline fun <reified M : Any> mock(noinline config: MockConfigurer<M>.() -> Unit = {}) =
    mock(M::class, config)

/**
 * Adjusts the configuration of a given mock or allocates a spy for a given non-mock object.
 *
 * Callers may optionally configure the implementations of mocked functions through the [config]
 * parameter. When omitted, all functions simply return null.
 */
fun <M : Any> mock(instance: M, config: MockConfigurer<M>.() -> Unit = {}): M {
  val mockInstance = if (MockUtil.isMock(instance)) {
    instance
  } else {
    Mockito.spy(instance)
  }

  MockConfigurer(mockInstance).config()
  return mockInstance
}
