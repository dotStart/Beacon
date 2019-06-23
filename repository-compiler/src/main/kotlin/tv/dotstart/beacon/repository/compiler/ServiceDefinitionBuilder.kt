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
package tv.dotstart.beacon.repository.compiler

import com.google.protobuf.ByteString
import tv.dotstart.beacon.repository.Model
import java.nio.file.Files
import java.nio.file.Path

/**
 * Provides a factory for service definitions.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class ServiceDefinitionBuilder(val id: String, val title: String) {

  private val builder = Model.ServiceDefinition.newBuilder()

  /**
   * Identifies the location of the service definition icon.
   */
  var icon: Path? = null
    set(value) {
      if (value == null) {
        this.builder.clearIcon()
        return
      }

      this.builder.icon = ByteString.copyFrom(Files.readAllBytes(value))
    }

  /**
   * Identifies the category in which this service is to be placed.
   */
  var category
    get() = this.builder.category
    set(value) {
      this.builder.category = value
    }

  init {
    this.builder.id = this.id
    this.builder.title = this.title
    this.builder.category = Model.Category.GAME
  }

  internal operator fun invoke(): Model.ServiceDefinition = this.builder.build()

  /**
   * Configures a port which is to be forwarded via UPnP when this service is selected.
   */
  infix fun Int.via(protocol: Model.Protocol) {
    builder.addPort(Model.PortDefinition.newBuilder()
        .setPort(this)
        .setProtocol(protocol)
        .build())
  }
}
