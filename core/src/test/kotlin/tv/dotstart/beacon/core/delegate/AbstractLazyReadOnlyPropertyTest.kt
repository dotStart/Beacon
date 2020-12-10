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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.reflect.KProperty

/**
 * @author Johannes Donath
 * @date 03/12/2020
 */
internal class AbstractLazyReadOnlyPropertyTest {

  private val dummyProperty: TestMarker
    get() = throw NotImplementedError("Dummy property for testing purposes")

  companion object {

    private const val initializationTimeout = 1000L
    private const val initializationLeeway = 250L
  }

  /**
   * Evaluates whether generic implementations retain their generated value until explicitly reset.
   */
  @Test
  fun `It should retain the current value until reset`() {
    val property = TrackingLazyReadOnlyProperty()

    val value1 = property.getValue(this, ::dummyProperty)
    val value2 = property.getValue(this, ::dummyProperty)

    assertSame(value1, value2, "Delegate returned different values")

    property.reset()

    val value3 = property.getValue(this, ::dummyProperty)

    assertNotSame(value1, value3, "Delegate did not reset")
  }

  /**
   * Evaluates whether generic implementations initialize exactly once in the face of concurrent
   * access during prolonged initializations.
   *
   * This test is necessary due to the thread safety strategy which has been optimized for quick
   * access (thus attempting to access their value without protection).
   */
  @Test
  fun `It should prevent race conditions`() {
    val property = SlowLazyReadOnlyProperty()

    val barrier = CyclicBarrier(2)
    val volatileValue = VolatileHolder<TestMarker?>(null)

    thread {
      // thread creation is typically a relatively slow operation thus introducing a potential race
      // condition within this test - wait for both parties to be ready before actually performing
      // this test
      barrier.await()

      // force initialization here first and store the result once available (this invocation occurs
      // ahead of the second access further below due to its Thread.sleep invocation)
      volatileValue(property.getValue(this, ::dummyProperty))

      // additional synchronization to ensure the generated value is accessible via its atomic
      // wrapper object
      barrier.await()
    }

    barrier.await()

    // assert that access to the property does not significantly exceed initialization duration as
    // this would indicate duplicate initialization due to a race condition
    assertTimeout(Duration.ofMillis(initializationTimeout).plusMillis(initializationLeeway)) {
      // wait out the quarter of the initialization duration to ensure that we do not cause
      // initialization but rather run into a condition where we wait for the lock to be released
      Thread.sleep(initializationLeeway)

      val actual = property.getValue(this, ::dummyProperty)

      // additional synchronization to safely retrieve generated value (this should complete almost
      // immediately as the initialization thread is done by now)
      barrier.await()

      val expected = volatileValue()

      assertNotNull(expected) { "Initialization not performed" }
      assertNotNull(actual) { "Early initialization not performed" }
      assertSame(expected, actual, "Re-Initialization performed")
    }
  }

  /**
   * Acts as a marker value for delegate instances throughout this test.
   */
  class TestMarker

  /**
   * Provides a holder which stores its value within a volatile variable for improved propagation
   * between threads.
   */
  private class VolatileHolder<T>(private val initial: T) {

    @Volatile
    private var value: T = initial

    /**
     * Retrieves the current value stashed within this holder.
     */
    operator fun invoke() = this.value

    /**
     * Updates the value of this holder.
     */
    operator fun invoke(newValue: T) {
      this.value = newValue
    }
  }

  /**
   * Provides a delegate implementation which prevents secondary initializations from succeeding.
   */
  class TrackingLazyReadOnlyProperty : AbstractLazyReadOnlyProperty<Any, TestMarker>() {

    private var initialized = false

    public override fun reset() {
      this.initialized = false
      super.reset()
    }

    override fun initialize(thisRef: Any, property: KProperty<*>): TestMarker {
      assertFalse(this.initialized, "Already initialized")
      this.initialized = true

      return TestMarker()
    }
  }

  /**
   * Provides a delegate implementation which waits for [initializationTimeout] milliseconds to pass
   * before finalizing the initialization.
   */
  class SlowLazyReadOnlyProperty : AbstractLazyReadOnlyProperty<Any, TestMarker>() {

    override fun initialize(thisRef: Any, property: KProperty<*>): TestMarker {
      Thread.sleep(initializationTimeout)

      return TestMarker()
    }
  }
}
