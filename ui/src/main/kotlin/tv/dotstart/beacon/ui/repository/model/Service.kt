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
package tv.dotstart.beacon.ui.repository.model

import com.google.protobuf.ByteString
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.repository.Model
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import javax.imageio.ImageIO
import tv.dotstart.beacon.core.model.Service as CoreService

/**
 * Represents a service along with its human readable identification and port definitions.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
data class Service(

    /**
     * Stores a URI with which this particular service is uniquely identified.
     *
     * This is most likely a store identifier (such as "game+steam://<id>") but may also be a
     * reference to the website of a game's developer (such as "game://example.org/game")
     */
    override val id: URI,

    /**
     * Identifies the category in which this service is to be organized.
     */
    val category: Model.Category,

    /**
     * Identifies the location at which an extracted version of this service's icon may be found.
     *
     * When no icon has been assigned for this particular icon, null is returned instead. In this
     * case, the icon is to be substituted with a standard fallback icon.
     */
    val icon: BufferedImage?,

    /**
     * Provides a human readable name with which this service is identified.
     */
    override val title: String,

    /**
     * Identifies the ports on which this service will typically listen.
     */
    override val ports: List<Port>) : CoreService {

  companion object {

    private val logger by logManager()
  }

  constructor(model: Model.ServiceDefinition) : this(
      URI.create(model.id),
      model.category,
      model.icon
          ?.let { iconData ->
            try {
              ByteArrayInputStream(iconData.toByteArray())
                  .let { ImageIO.read(it) }
            } catch (ex: IOException) {
              logger.error("Failed to decode icon for service \"${model.id}\"", ex)
              null
            }
          },
      model.title,
      model.portList.map(::Port)
  )

  fun toRepositoryDefinition(): Model.ServiceDefinition {
    val icon = this.icon
        ?.let {
          ByteArrayOutputStream().use { stream ->
            try {
              ImageIO.write(it, "PNG", stream)
              stream.toByteArray()
            } catch (ex: IOException) {
              logger.error("Failed to encode icon for service \"$id\"", ex)
              null
            }
          }
        }

    return Model.ServiceDefinition.newBuilder()
        .setId(this.id.toASCIIString())
        .setTitle(this.title)
        .setCategory(this.category)
        .addAllPort(this.ports.map(Port::toRepositoryDefinition))
        .also {
          if (icon != null) {
            it.icon = ByteString.copyFrom(icon)
          }
        }
        .build()
  }
}
