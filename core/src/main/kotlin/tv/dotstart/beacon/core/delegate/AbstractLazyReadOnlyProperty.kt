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

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Provides a lazily initialized read-only property.
 *
 * This implementation performs its initialization in a synchronous fashion (e.g. only a single
 * thread may invoke the initialization logic at a given time). Reads occur concurrently once
 * initialized, however.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
abstract class AbstractLazyReadOnlyProperty<in T, out V> : ReadOnlyProperty<T, V> {

  /**
   * Retrieves the object which shall act as a lock when performing the property initialization.
   *
   * This value is expected to be constant throughout the instance lifecycle as its primary purpose
   * is to protect the property state from concurrency related corruption.
   */
  protected val lock = ReentrantLock()

  /**
   * Stores the current property value.
   *
   * Upon construction, this value is initialized with a reference to the [Uninitialized] marker
   * object (the same is true following an invocation of [reset]).
   */
  @Volatile
  private var value: Any? = Uninitialized

  /**
   * Performs the initialization of this property.
   *
   * This method is invoked within the protection provided by [lock] and is guaranteed to be invoked
   * at most once throughout the lifetime of the object (unless [reset] is invoked).
   *
   * Note: If an exception is thrown by this method, it will simply propagate to the accessing
   * thread. Initialization will occur again for subsequent accesses.
   */
  protected abstract fun initialize(thisRef: T, property: KProperty<*>): V

  /**
   * Resets the delegate to its construction-time state.
   *
   * This method actively invalidates the at-most-once guarantee outlined for the implementation of
   * [initialize]. As such, this method is not exposed to the public by default. Implementations
   * may, however, choose to invoke this method in order to adjust the initialization behavior.
   */
  protected open fun reset() {
    this.lock.withLock {
      this.value = Uninitialized
    }
  }

  override fun getValue(thisRef: T, property: KProperty<*>): V {
    // attempt to access a previously initialized value without protection first as this is the
    // most likely case to occur throughout the object's lifetime
    // the value field has been marked volatile to immediately propagate changes to other threads
    // when they occur
    val unprotected = this.value
    if (unprotected !== Uninitialized) {
      @Suppress("UNCHECKED_CAST")
      return unprotected as V
    }

    // reaching this point implies that the property has yet to be initialized according to the
    // calling thread's view thus requiring protection for the purposes of populating the object
    // state
    this.lock.withLock {
      // lock acquisition may be delayed by the initialization on another thread thus requiring a
      // secondary check against the current value
      val protected = this.value
      if (protected !== Uninitialized) {
        @Suppress("UNCHECKED_CAST")
        return protected as V
      }

      // object is guaranteed to be uninitialized at this point as this thread holds the sole
      // permission to populate the state
      // exceptions simply propagate and leave the object uninitialized
      val initialized = this.initialize(thisRef, property)
      this.value = initialized
      return initialized
    }
  }

  /**
   * Marker Object
   *
   * This object is used as a marker for property instances which have yet to be initialized. This
   * is a replacement to `null` as null may be a perfectly valid within the context of an
   * implementation thus conflicting with the uninitialized state.
   */
  private object Uninitialized
}
