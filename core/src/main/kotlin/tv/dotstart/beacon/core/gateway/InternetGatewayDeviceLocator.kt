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

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.time.withTimeoutOrNull
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.ControlPointFactory
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.core.upnp.DeviceDiscoveryEvent
import tv.dotstart.beacon.core.upnp.FlowDiscoveryListener
import java.net.NetworkInterface
import java.time.Duration

/**
 * Provides an interface for the purposes of locating standard compliant internet gateway devices
 * within the local network.
 *
 * When no network interface is explicitly specified, devices are queried via all available network
 * interfaces on the system. This behavior is typically desirable as systems may be connected to
 * multiple networks (such as VPNs) while only a single gateway will typically answer queries.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 01/12/2020
 */
class InternetGatewayDeviceLocator(

    /**
     * Provides a listing of network interfaces on which device queries are published.
     */
    val networkInterfaces: List<NetworkInterface> = emptyList(),

    /**
     * Identifies the maximum amount the locator will wait before assuming no compatible device to
     * be present within the network.
     */
    private val timeout: Duration = Duration.ofSeconds(5)) {

  companion object {

    private val logger by logManager()

    internal const val deviceType = "urn:schemas-upnp-org:device:WANConnectionDevice:1"
  }

  /**
   * Attempts to locate a compatible internet gateway device within the local network.
   *
   * This method will return the first gateway device which manages to respond to the query as the
   * network is expected to only ever contain a single gateway device (or is capable of configuring
   * the actual gateway if acting as a repeater of sorts).
   *
   * TODO: These assumptions may not necessarily hold true - We should select the most compatible
   *       device based on its responses
   */
  suspend fun locate(): InternetGatewayDevice? {
    val cp = ControlPointFactory.builder()
        .also {
          if (this.networkInterfaces.isNotEmpty()) {
            it.setInterfaces(this.networkInterfaces)
          }
        }
        .build()

    cp.start()

    val device = this.locate(cp)
    if (device == null) {
      cp.terminate()
      return null
    }

    return device
  }

  /**
   * Attempts to locate an internet gateway device using a given control point.
   *
   * This method is cancellation compliant and will exit early when its surrounding coroutine scope
   * has been cancelled thus permitting timeout behaviors if desired.
   *
   * By default, this method will never timeout. Only a single query will be dispatched, however.
   */
  internal suspend fun locate(cp: ControlPoint): InternetGatewayDevice? {
    FlowDiscoveryListener().use { listener ->
      cp.addDiscoveryListener(listener)
      cp.search(deviceType)

      @Suppress("ConvertLambdaToReference") // ugly
      val device = withTimeoutOrNull(this.timeout) {
        listener.events
            // TODO: Handle loss of devices within the network gracefully
            .filter { it.type == DeviceDiscoveryEvent.Type.DISCOVERED }
            .mapNotNull { (_, device) ->
              logger.info(
                  "Received announcement for device \"${device.friendlyName}\" (${device.modelName}) of type ${device.deviceType} by ${device.manufacture} <${device.manufactureUrl}>")

              if (device.deviceType == deviceType) {
                device
              } else {
                device.findDeviceByTypeRecursively(deviceType)
              }
            }
            .map { device -> InternetGatewayDevice(cp, device) }
            .filter {
              if (!it.portMappingAvailable) {
                logger.warn(
                    "Device \"${it.friendlyName}\" does not support port mapping and will not be considered")
              }

              it.portMappingAvailable
            }
            .firstOrNull()
      }

      cp.removeDiscoveryListener(listener)
      return device
    }
  }
}
