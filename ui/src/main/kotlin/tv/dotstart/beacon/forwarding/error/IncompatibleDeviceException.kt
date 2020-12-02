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
package tv.dotstart.beacon.forwarding.error

import net.mm2d.upnp.Device

/**
 * Represents an error caused by the lack of a required service within a given device profile.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 02/12/2020
 */
class IncompatibleDeviceException(device: Device, message: String, cause: Throwable? = null)
  : InternetGatewayDeviceException(
    buildString {
      append("Unsupported gateway device: ")
      append(message)

      append(System.lineSeparator())
      append(System.lineSeparator())

      append("Device Address: ")
      append(device.ipAddress)
      append(System.lineSeparator())

      append("Device Presentation URL: ")
      append(device.presentationUrl)
      append(System.lineSeparator())

      append("Device Type: ")
      append(device.deviceType)
      append(System.lineSeparator())

      append("Device Friendly Name: ")
      append(device.friendlyName)
      append(System.lineSeparator())

      append("Device Model Number: ")
      append(device.modelName)
      append(System.lineSeparator())

      append("Device Model: ")
      append(device.modelName)
      append(" <")
      append(device.modelUrl)
      append(">")
      append(System.lineSeparator())

      append("Device Model Description:")
      append(device.modelDescription)
      append(System.lineSeparator())

      append("Manufacturer: ")
      append(device.manufacture)
      append(" <")
      append(device.manufactureUrl)
      append(">")
      append(System.lineSeparator())
      append(System.lineSeparator())

      append("Service Report")
      append(System.lineSeparator())
      append("--------------")
      append(System.lineSeparator())

      device.serviceList.forEach {
        append(" - ")
        append(it.serviceType)
        append(" (")
        append(it.serviceId)
        append(")")
        append(System.lineSeparator())
      }

      append(System.lineSeparator())

      append("Device Report")
      System.lineSeparator()
      append("-------------")
      append(System.lineSeparator())

      device.deviceList.forEach {
        append(" - ")
        append(it.deviceType)
        append(System.lineSeparator())
      }
    },
    cause)
