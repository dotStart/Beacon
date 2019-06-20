/*
 * Copyright 2019 Johannes Donath <johannesd@torchmind.com>
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
package tv.dotstart.beacon.exposure

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
object InterfaceChooser {

  /**
   * Retrieves the address of the local machine.
   *
   * This value will typically match the assigned interface address on the gateway interface (e.g.
   * the interface with which this device connects to the internet).
   */
  val localAddress: InetAddress = InetAddress.getLocalHost()

  /**
   * Provides a complete listing of all compatible interfaces on the executing system.
   *
   * This list will only contain interfaces which are currently up (e.g. connected)  and
   * support multicast (which is required for UPnP functionality).
   */
  val interfaces = NetworkInterface.getNetworkInterfaces().asSequence()
      .filter { !it.isLoopback && it.isUp && it.supportsMulticast() }
      .toList()

  /**
   * Retrieves the detected gateway interface (if any).
   */
  val gatewayInterface = interfaces
      .find {
        it.inetAddresses.asSequence()
            .any { it == localAddress }
      }

  /**
   * Identifies a recommended interface which is most likely capable of broadcasting a successful
   * UPnP request to the upstream router.
   *
   * This field will be set to null when no interfaces are available.
   */
  val recommended = this.gatewayInterface ?: this.interfaces.firstOrNull()

  val selectedProperty: ObjectProperty<NetworkInterface> = SimpleObjectProperty(this.recommended)

  /**
   * Identifies the interface which will be used to broadcast UPnP forwarding messages.
   *
   * At initialization time, this will match the recommended interface but may be altered via the
   * user configuration if necessary.
   *
   * Note that there is absolutely no guarantee that this interface is capable of reaching a UPnP
   * compatible gateway.
   */
  var selected: NetworkInterface
    get() = this.selectedProperty.value
    set(value) {
      this.selectedProperty.value = value
    }
}
