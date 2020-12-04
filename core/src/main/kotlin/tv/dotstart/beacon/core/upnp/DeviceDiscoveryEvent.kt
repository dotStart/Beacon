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

import net.mm2d.upnp.Device

/**
 * Represents a device discovery event.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
data class DeviceDiscoveryEvent(

    /**
     * Identifies the type of event represented by this object.
     */
    val type: Type,

    /**
     * Identifies the device which is affected by this event.
     */
    val device: Device) {

  /**
   * Provides a listing of recognized event types.
   */
  enum class Type {

    /**
     * Identifies that a given device has been located within the local network as a result of a
     * prior query.
     */
    DISCOVERED,

    /**
     * Identifies that a given device has been lost (e.g. has been removed from the network).
     */
    LOST
  }
}
