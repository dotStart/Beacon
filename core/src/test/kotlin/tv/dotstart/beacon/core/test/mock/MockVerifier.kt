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

/**
 * Configures the verification of a previously accessed mock.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 04/12/2020
 */
class MockVerifier<M : Any> internal constructor(private val mock: M) {

  /**
   * Configures the verification of a given method invocation on the mock object.
   */
  fun <R> invocation(block: M.() -> R) = MockFunctionVerifier(this.mock, block)

  /**
   * Verifies that there were no more interactions with this mock.
   */
  fun noMoreInteractions() {
    Mockito.verifyNoMoreInteractions(this.mock)
  }
}

/**
 * Performs the verification of a given mock.
 */
fun <M : Any> verify(mock: M, block: MockVerifier<M>.() -> Unit) {
  require(MockUtil.isMock(mock)) { "Expected mock instance" }

  MockVerifier(mock).block()
}
