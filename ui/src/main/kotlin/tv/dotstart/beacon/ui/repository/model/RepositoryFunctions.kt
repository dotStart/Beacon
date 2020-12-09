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
package tv.dotstart.beacon.ui.repository.model

import tv.dotstart.beacon.core.model.Protocol
import tv.dotstart.beacon.repository.Model

/**
 * Provides functions for the purposes of simplifying interactions with repository related objects.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 03/12/2020
 */

/**
 * Converts a repository protocol value to its respective model representation.
 */
fun Model.Protocol.toCoreProtocol(): Protocol = when (this) {
  Model.Protocol.TCP -> Protocol.TCP
  Model.Protocol.UDP -> Protocol.UDP
  else -> throw IllegalArgumentException("Illegal protocol type: $this")
}

/**
 * Converts a protocol value to its respective repository representation.
 */
fun Protocol.toRepositoryDefinition(): Model.Protocol = when (this) {
  Protocol.TCP -> Model.Protocol.TCP
  Protocol.UDP -> Model.Protocol.UDP
}
