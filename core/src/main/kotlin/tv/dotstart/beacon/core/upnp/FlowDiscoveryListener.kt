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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.Device
import java.io.Closeable

/**
 * Provides a discovery listener implementation which wraps all received events in a flow for
 * further processing.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
class FlowDiscoveryListener : ControlPoint.DiscoveryListener, Closeable {

  private val channel = Channel<DeviceDiscoveryEvent>()

  val events: Flow<DeviceDiscoveryEvent>
    get() = this.channel.receiveAsFlow()

  override fun onDiscover(device: Device) {
    runBlocking {
      channel.send(DeviceDiscoveryEvent(
          DeviceDiscoveryEvent.Type.DISCOVERED,
          device))
    }
  }

  override fun onLost(device: Device) {
    runBlocking {
      channel.send(DeviceDiscoveryEvent(
          DeviceDiscoveryEvent.Type.LOST,
          device))
    }
  }

  override fun close() {
    this.channel.close()
  }
}
