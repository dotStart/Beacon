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

import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.Device
import tv.dotstart.beacon.core.gateway.error.IncompatibleDeviceException
import tv.dotstart.beacon.core.model.Port
import tv.dotstart.beacon.core.upnp.invoke
import java.io.Closeable

/**
 * Represents a previously discovered internet gateway device along with its profile defined
 * capabilities.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 01/12/2020
 */
class InternetGatewayDevice internal constructor(
    private val cp: ControlPoint,
    private val device: Device) : Closeable {

  val friendlyName: String
    get() = this.device.friendlyName
  val modelName: String
    get() = this.device.modelName
  val manufacturer: String?
    get() = this.device.manufacture
  val manufacturerUrl: String?
    get() = this.device.manufactureUrl

  private val externalAddressAction = this.device.findAction(externalAddressActionName)
  private val portRegistrationAction = this.device.findAction(portRegistrationActionName)
  private val portRemovalAction = this.device.findAction(portRemovalActionName)

  /**
   * Evaluates whether the external address may be acquired from this device.
   */
  val externalAddressAvailable: Boolean
    get() = this.externalAddressAction != null

  /**
   * Evaluates whether port mapping is available on this device.
   */
  val portMappingAvailable: Boolean
    get() = this.portRegistrationAction != null && this.portRemovalAction != null

  companion object {

    internal const val externalAddressActionName = "GetExternalIPAddress"
    internal const val portRegistrationActionName = "AddPortMapping"
    internal const val portRemovalActionName = "DeletePortMapping"

    internal const val externalAddressField = "NewExternalIPAddress"
  }

  /**
   * Retrieves the address at which this gateway exposes forwarded addresses to the outside world.
   *
   * When there is no active connection, null is returned instead of a valid address.
   *
   * @throws IncompatibleDeviceException when the device does not expose the necessary action.
   */
  suspend fun getExternalAddress(): String? {
    val action = this.externalAddressAction
        ?: throw IncompatibleDeviceException(
            this.device, "Device does not support $externalAddressAction")

    return action {
      it[externalAddressField]
          ?.takeIf(String::isNotBlank)
    }
  }

  /**
   * Creates a new forwarded port for a given protocol and port.
   *
   * Optionally, a lease duration and description may be passed in order to establish automatic
   * un-registration in case the application dies as well as a human readable description with which
   * the forwarding is identified within potential router user interfaces.
   *
   * When a lease duration is given, [PortMapping.refresh] must be invoked on a regular basis in
   * order to retain ownership of the forwarded port.
   */
  suspend fun forward(port: Port, description: String? = null, duration: Long = 0): PortMapping {
    val registerAction = this.portRegistrationAction
        ?: throw IncompatibleDeviceException(
            this.device, "Device does not support $portRegistrationActionName")
    val removeAction = this.portRemovalAction
        ?: throw IncompatibleDeviceException(
            this.device, "Device does not support $portRemovalActionName")

    return PortMapping(this.device, registerAction, removeAction, port, description, duration)
        .also { it.refresh() }
  }

  override fun close() {
    this.cp.stop()
    this.cp.terminate()
  }
}
