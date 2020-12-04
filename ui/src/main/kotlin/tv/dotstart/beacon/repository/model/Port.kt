/*
 * Copyright (C) 2019 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.beacon.repository.model

import tv.dotstart.beacon.core.model.Protocol
import tv.dotstart.beacon.repository.Model
import tv.dotstart.beacon.core.model.Port as CorePort

/**
 * Represents a port numbe definition along with its respective protocol.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
data class Port(

    /**
     * Identifies the protocol with which this port typically communicates.
     *
     * Note that the service will express all possible protocols when multiple are possible (or
     * permanently in use).
     */
    override val protocol: Protocol,

    /**
     * Identifies the actual port number on which this particular service listens.
     */
    override val number: Int) : CorePort {

  constructor(model: Model.PortDefinition) : this(model.protocol.toCoreProtocol(), model.port)

  /**
   * Converts this model into its respective repository model representation.
   */
  fun toRepositoryDefinition(): Model.PortDefinition = Model.PortDefinition.newBuilder()
      .setProtocol(this.protocol.toRepositoryDefinition())
      .setPort(this.number)
      .build()
}
