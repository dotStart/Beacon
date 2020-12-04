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
package tv.dotstart.beacon.core.gateway

import net.mm2d.upnp.Action
import java.util.concurrent.CompletableFuture

/**
 * Provides functions which simplify the interaction with devices.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 02/12/2020
 */

/**
 * Invokes a given UPnP operation with a given set of parameters and synchronously returns a
 * converted response.
 */
operator fun <T> Action.invoke(parameters: Map<String, String?> = emptyMap(),
                               converter: (Map<String, String>) -> T): T {
  val future = CompletableFuture<T>()

  this.invoke(
      parameters,
      onResult = {
        val result = converter(it)
        future.complete(result)
      },
      onError = future::completeExceptionally
  )

  return future.join()
}
