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
package tv.dotstart.beacon.core.model

/**
 * Represents a single port with its respective number and protocol.
 *
 * Note: Port numbers are only considered equivalent if their protocol _AND_ numbers match as
 * applications are typically permitted to bind the same port number for each respective protocol if
 * desired.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
interface Port {

  /**
   * Identifies the port number.
   *
   * This value is constrained to positive numbers within the range of 1 to 65535. Values outside
   * this range typically cause exceptions to be raised within the client code.
   */
  val number: Int

  /**
   * Identifies the protocol via which communication takes place on this port.
   */
  val protocol: Protocol
}
