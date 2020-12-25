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
package tv.dotstart.beacon.core.upnp

import kotlinx.coroutines.future.await
import net.mm2d.upnp.Action
import tv.dotstart.beacon.core.upnp.error.*
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

/**
 * Provides functions which simplify the interaction with devices.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 02/12/2020
 */

private const val errorCodeFieldName = "UPnPError/errorCode"
private const val errorDescriptionFieldName = "UPnPError/errorDescription"

/**
 * Invokes a given UPnP operation with a given set of parameters and synchronously returns a
 * converted response.
 *
 * @throws ActionFailedException when the action failed to execute as desired.
 * @throws DeviceOutOfMemoryException when the device ran out of memory while processing the action.
 * @throws HumanInterventionRequiredException when the action requires human intervention.
 * @throws InvalidActionArgumentException when the given set of arguments has been rejected.
 * @throws UnknownActionErrorException when an unknown error occurs.
 * @throws CancellationException when the thread is interrupted while awaiting the action result.
 */
suspend operator fun <T> Action.invoke(parameters: Map<String, String?> = emptyMap(),
                                       converter: (Map<String, String>) -> T): T {
  val future = CompletableFuture<T>()

  this.invoke(
      parameters,
      onResult = actionInvocation@{
        if (errorCodeFieldName in it) {
          val errorDescription = it[errorDescriptionFieldName]
              ?.takeIf(String::isNotBlank)
              ?: "No additional information given"

          val errorCodeStr = it[errorCodeFieldName]
          val errorCode = errorCodeStr
              ?.toIntOrNull()
              ?: throw UnknownActionErrorException(
                  "Device responded with malformed error code \"$errorCodeStr\": $errorDescription")

          val ex = when (errorCode) {
            401, 602 -> InvalidActionException("Device rejected action: $errorDescription")
            402, 600, 601, 605 -> InvalidActionArgumentException(
                "Device rejected arguments: $errorDescription")
            501 -> ActionFailedException("Device failed to perform action: $errorDescription")
            603 -> DeviceOutOfMemoryException("Device ran out of memory: $errorDescription")
            604 -> HumanInterventionRequiredException(
                "Device requested human intervention: $errorDescription")
            else -> UnknownActionErrorException(
                "Device responded with unknown error code $errorCode: $errorDescription")
          }

          future.completeExceptionally(ex)
          return@actionInvocation
        }

        val result = converter(it)
        future.complete(result)
      },
      onError = future::completeExceptionally,
      returnErrorResponse = true,
  )

  return try {
    future.await()
  } catch (ex: CompletionException) {
    val cause = ex.cause
    if (cause is ActionException) {
      throw cause
    }

    throw UnknownActionErrorException(
        "Unknown error occurred while invoking device action", cause ?: ex)
  }
}
