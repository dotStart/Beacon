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
import net.mm2d.upnp.Device
import tv.dotstart.beacon.core.model.Port
import tv.dotstart.beacon.core.upnp.invoke
import tv.dotstart.beacon.core.util.getLocalAddressFor
import java.net.InetAddress

/**
 * Represents an allocated port forwarding instance on a given gateway device.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 02/12/2020
 */
class PortMapping internal constructor(
    private val device: Device,
    private val registrationAction: Action,
    private val removalAction: Action,

    /**
     * Identifies the port for which this mapping was established.
     */
    val port: Port,

    /**
     * Defines a human readable identification with which this mapping is registered.
     *
     * This value is primarily provided for administrative reasons and may be displayed as part of
     * the gateway device user interface in some cases.
     */
    val description: String?,

    /**
     * Identifies the total duration for which this lease is valid (in seconds).
     *
     * The lease duration is extended by this duration every time [refresh] is invoked thus
     * preventing potential issues related to application crashes. The first refresh is expected to
     * occur once this period has passed.
     *
     * When zero is passed, the lease remains valid for an indefinite amount of time.
     */
    val duration: Long) {


  companion object {

    private const val defaultDescription = "Beacon Mapping"

    internal const val enabledParameterName = "NewEnabled"
    internal const val protocolParameterName = "NewProtocol"
    internal const val remoteHostParameterName = "NewRemoteHost"
    internal const val externalPortParameterName = "NewExternalPort"
    internal const val internalClientParameterName = "NewInternalClient"
    internal const val internalPortParameterName = "NewInternalPort"
    internal const val descriptionParameterName = "NewPortMappingDescription"
    internal const val leaseDurationParameterName = "NewLeaseDuration"
  }

  /**
   * Refreshes the port mapping lease for the desired duration.
   */
  suspend fun refresh() {
    val localAddress = getLocalAddressFor(InetAddress.getByName(this.device.ipAddress))

    val params = mutableMapOf(
        enabledParameterName to "1",
        protocolParameterName to this.port.protocol.toString(),
        remoteHostParameterName to "",
        externalPortParameterName to this.port.number.toString(10),
        internalClientParameterName to localAddress.hostAddress,
        internalPortParameterName to this.port.number.toString(),
        descriptionParameterName to (this.description ?: defaultDescription),
        leaseDurationParameterName to this.duration.toString()
    )

    this.registrationAction(params) { Unit }
  }

  /**
   * Permanently removes a port mapping.
   */
  suspend fun remove() {
    val params = mutableMapOf(
        protocolParameterName to this.port.protocol.toString(),
        externalPortParameterName to this.port.number.toString()
    )

    this.removalAction(params) { Unit }
  }
}
