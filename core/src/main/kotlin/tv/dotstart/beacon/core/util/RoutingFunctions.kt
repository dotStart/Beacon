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
package tv.dotstart.beacon.core.util

import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Provides functions which simplify the interaction with routing concepts.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 02/12/2020
 */

/**
 * Retrieves the local address for a given target route.
 *
 * This method will effectively establish a temporary datagram socket in order to identify the local
 * address used in order to establish communication with a given target.
 *
 * Note: This implementation is pretty ugly, however, Java does not provide any methods for
 * interacting with the routing table directly thus preventing the application from discovering its
 * local address without prior knowledge of the target network interface.
 */
fun getLocalAddressFor(target: InetAddress): InetAddress = DatagramSocket(0)
    .also { it.connect(target, 1) }
    .localAddress
