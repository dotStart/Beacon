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
import org.mockito.invocation.InvocationOnMock

/**
 * Configures the mocked aspects of a given function.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
class MockFunctionConfigurer<M : Any, R> internal constructor(
    internal val mock: M,
    internal val target: M.() -> R) {

  /**
   * Instructs a given target method to invoke the given block of code when invoked.
   */
  infix fun answer(block: (InvocationOnMock) -> R) {
    Mockito.`when`(this.mock.target())
        .thenAnswer { block(it) }
  }

  /**
   * Instructs a given target method to return the desired static value when invoked.
   */
  infix fun returnValue(value: R) {
    Mockito.`when`(this.mock.target())
        .thenReturn(value)
  }

  /**
   * Instructs a given target method to throw the desired static exception when invoked.
   */
  infix fun throwException(ex: Throwable) {
    Mockito.`when`(this.mock.target())
        .thenThrow(ex)
  }
}
