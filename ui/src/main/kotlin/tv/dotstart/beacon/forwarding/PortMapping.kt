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
package tv.dotstart.beacon.forwarding

import net.mm2d.upnp.Device
import tv.dotstart.beacon.forwarding.error.IncompatibleDeviceException
import tv.dotstart.beacon.repository.Model
import java.net.InetAddress

/**
 * Represents an allocated port forwarding instance on a given gateway device.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 02/12/2020
 */
class PortMapping internal constructor(
    private val device: Device,
    private val protocol: Model.Protocol,
    private val port: Int,
    private val description: String?,
    private val duration: Long) {

  private val registrationAction = this.device.findAction("AddPortMapping")
      ?: throw IncompatibleDeviceException(
          this.device, "Device does not support AddPortMapping")
  private val removalAction = this.device.findAction("DeletePortMapping")
      ?: throw IncompatibleDeviceException(
          this.device, "Device does not support DeletePortMapping")


  companion object {

    private val defaultDescription = "Beacon Mapping"
  }

  /**
   * Refreshes the port mapping lease for the desired duration.
   */
  fun refresh() {
    val localAddress = getLocalAddressFor(InetAddress.getByName(this.device.ipAddress))

    val params = mutableMapOf(
        "NewEnabled" to "1",
        "NewProtocol" to this.protocol.toString(),
        "NewRemoteHost" to "",
        "NewExternalPort" to this.port.toString(),
        "NewInternalClient" to localAddress.hostAddress,
        "NewInternalPort" to this.port.toString(),
        "NewPortMappingDescription" to (this.description ?: defaultDescription),
        "NewLeaseDuration" to this.duration.toString()
    )

    this.registrationAction(params) { Unit }
  }

  /**
   * Permanently removes a port mapping.
   */
  fun remove() {
    val params = mutableMapOf(
        "NewProtocol" to this.protocol.toString(),
        "NewExternalPort" to this.port.toString()
    )

    this.removalAction(params) { Unit }
  }
}
