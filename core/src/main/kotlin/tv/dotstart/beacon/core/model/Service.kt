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

import java.net.URI

/**
 * Represents an arbitrary service along with its respective associated port numbers and protocols.
 *
 * This implementation effectively mirrors the protocol buffer models provided by the
 * `repository-model` module. Iconography such as logos or banners have been omitted from this
 * specification as they are not required to provide the forwarding functionality provided by the
 * core module.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */
interface Service {

  /**
   * Defines a human readable identifier with which this service may be referenced.
   *
   * This value is used as a primary identification attribute within configuration files and
   * command line arguments. As such, it is expected to be unique amongst repositories and should
   * not be changed unless absolutely required.
   *
   * A set of examples is available from the system repository definitions shipped along with
   * the application source code.
   */
  val id: URI

  /**
   * Defines a human readable title for this service.
   *
   * This value is primarily provided for the purposes of quick identification without requiring any
   * prior knowledge of the identifier format. As such, it is typically used within user interfaces.
   */
  val title: String

  /**
   * Exposes a listing of ports which are typically utilized for communication with this service
   * and will thus require whitelisting within network firewalls.
   */
  val ports: List<Port>
}
