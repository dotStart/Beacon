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
package tv.dotstart.beacon.core.delegate

import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Johannes Donath
 * @date 03/12/2020
 */
internal class LoggerReadOnlyPropertyTest {

  val dummyProperty: Logger
    get() = throw NotImplementedError("Dummy property for testing purposes")

  companion object {

    val dummyProperty: Logger
      get() = throw NotImplementedError("Dummy property for testing purposes")
  }

  object Alternative {

    val dummyProperty: Logger
      get() = throw NotImplementedError("Dummy property for testing purposes")
  }

  /**
   * Evaluates whether the implementation lazily constructs and stores logger instances.
   */
  @Test
  fun `It lazily creates logger instances`() {
    val property = logManager()

    val logger1 = property.getValue(Companion, Companion::dummyProperty)
    val logger2 = property.getValue(Companion, Companion::dummyProperty)

    assertSame(logger1, logger2, "Delegate returned different instances")
  }

  /**
   * Evaluates whether the implementation registers loggers with their expected names based on
   * whether its properties are defined within companion objects or not.
   */
  @Test
  fun `It respects companion objects`() {
    val propertyA = logManager()
    val propertyB = logManager()
    val propertyC = logManager()

    val loggerA = propertyA.getValue(Companion, Companion::dummyProperty)
    assertEquals(this::class.qualifiedName, loggerA.name)

    val loggerB = propertyB.getValue(this, ::dummyProperty)
    assertEquals(this::class.qualifiedName, loggerB.name)

    val loggerC = propertyC.getValue(Alternative, Alternative::dummyProperty)
    assertEquals(Alternative::class.qualifiedName, loggerC.name)
  }
}
