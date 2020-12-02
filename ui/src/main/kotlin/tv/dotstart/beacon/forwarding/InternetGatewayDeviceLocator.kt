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

import io.reactivex.Observable
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.ControlPointFactory
import net.mm2d.upnp.Device
import tv.dotstart.beacon.util.logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Locates compatible internet gateway devices within the local network.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 01/12/2020
 */
// TODO: Permit explicit interface selection
class InternetGatewayDeviceLocator {

  companion object {

    private const val deviceType = "urn:schemas-upnp-org:device:WANConnectionDevice:1"

    private const val deviceTimeout = 5L

    private val logger = InternetGatewayDeviceLocator::class.logger
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
  fun locate(): Observable<InternetGatewayDevice?> = Observable.create {
    val cp = ControlPointFactory.create()

    cp.start()

    val device = this.locateDevice(cp)
    if (device == null) {
      cp.terminate()

      it.onComplete()
      return@create
    }

    it.onNext(InternetGatewayDevice(cp, device))
    it.onComplete()

    return@create
  }

  private fun locateDevice(cp: ControlPoint): Device? {
    val future = CompletableFuture<Device>()
    val listener = FutureDiscoveryListener(future, deviceType)

    cp.addDiscoveryListener(listener)
    cp.search(deviceType)

    val device = future.get(deviceTimeout, TimeUnit.MINUTES)

    cp.removeDiscoveryListener(listener)
    return device
  }

  private class FutureDiscoveryListener(
      private val future: CompletableFuture<Device>,
      private val type: String) : ControlPoint.DiscoveryListener {

    override fun onDiscover(device: Device) {
      logger.info(
          "Received announcement for device \"${device.friendlyName}\" (${device.modelName}) of type ${device.deviceType} by ${device.manufacture} <${device.manufactureUrl}>")

      val targetDevice = device
          .takeIf { it.deviceType == this.type }
          ?: device.findDeviceByTypeRecursively(this.type)
          ?: return

      if (!this.future.isDone) {
        this.future.complete(targetDevice)
      }
    }

    override fun onLost(device: Device) = Unit
  }
}
