/*
 * Copyright 2019 Johannes Donath <johannesd@torchmind.com>
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
package tv.dotstart.beacon.exposure

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.model.PortMapping
import tv.dotstart.beacon.preload.Loader
import tv.dotstart.beacon.repository.Model
import tv.dotstart.beacon.repository.model.Port
import tv.dotstart.beacon.repository.model.Service
import tv.dotstart.beacon.util.logger
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Provides a service which manages the currently forwarded set of services.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
object PortMapper {

  const val LEASE_DURATION = 30L
  const val LEASE_GRACE_DURATION = 5L
  const val REFRESH_DURATION = LEASE_DURATION - LEASE_GRACE_DURATION

  private val logger = PortMapper::class.logger

  private val executor = Executors.newScheduledThreadPool(1)
  private val lock = ReentrantReadWriteLock()
  private val mappings = mutableListOf<Service>()

  /**
   * Appends a set of port mappings to the regular announcement.
   */
  operator fun plusAssign(service: Service) {
    val registered = this.lock.write {
      if (this.mappings.any { it.id == service.id }) {
        return@write false
      }

      logger.info("Registering mappings for service ${service.id}")
      this.mappings += service
      return@write true
    }

    if (registered) {
      this.announce()
    }
  }

  /**
   * Removes a set of port mappings from the regular announcement.
   */
  operator fun minusAssign(service: Service) {
    val removed = this.lock.write {
      if (!this.mappings.removeIf { it.id == service.id }) {
        return@write false
      }

      logger.info("Removed mappings for service ${service.id}")
      return@write true
    }

    if (removed) {
      this.createPortMappings(service).forEach { Gateway -= it }

      // re-announce in case of overlaps
      this.announce()
    }
  }

  /**
   * Announces all currently registered services to the chosen router.
   */
  private fun announce() {
    logger.debug("Announcing port mappings")

    this.lock.read(this::mappings)
        .flatMap(this::createPortMappings)
        .forEach {
          logger.debug(
              "Announcing port mapping for ${it.internalClient}:${it.internalPort} (${it.protocol})")
          Gateway += it
        }
  }

  private fun createPortMappings(service: Service) = service.ports
      .map { this.createPortMapping(service, it) }

  private fun createPortMapping(service: Service, port: Port): PortMapping {
    val mapping = PortMapping(
        port.number,
        InetAddress.getLocalHost().hostAddress,
        when (port.protocol) {
          Model.Protocol.TCP -> PortMapping.Protocol.TCP
          Model.Protocol.UDP -> PortMapping.Protocol.UDP
          else -> throw IllegalArgumentException("Unsupported protocol: ${port.protocol}")
        },
        "Beacon Service ({$service.id})"
    )
    mapping.leaseDurationSeconds = UnsignedIntegerFourBytes(LEASE_DURATION)
    return mapping
  }

  /**
   * Evaluates whether a given service has already been registered with this mapper and is currently
   * announced to the chosen gateway device.
   */
  operator fun contains(service: Service) = this.lock.read {
    this.mappings.any { it.id == service.id }
  }

  object ScheduleLoader : Loader {

    override val description = "mapper"

    override fun load() {
      executor.scheduleAtFixedRate({ announce() }, REFRESH_DURATION, REFRESH_DURATION,
          TimeUnit.SECONDS)
    }

    override fun shutdown() {
      logger.info("Shutting down announcement scheduler")
      executor.shutdownNow()

      logger.info("Clearing any remaining registrations")
      lock.read { mappings }
          .flatMap { createPortMappings(it) }
          .forEach { Gateway -= it }
    }
  }
}
