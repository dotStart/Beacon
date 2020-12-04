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
package tv.dotstart.beacon.forwarding

import javafx.application.Platform
import kotlinx.coroutines.runBlocking
import tv.dotstart.beacon.core.gateway.InternetGatewayDevice
import tv.dotstart.beacon.core.gateway.InternetGatewayDeviceLocator
import tv.dotstart.beacon.core.gateway.PortMapping
import tv.dotstart.beacon.core.gateway.error.IncompatibleDeviceException
import tv.dotstart.beacon.preload.Loader
import tv.dotstart.beacon.preload.error.PreloadError
import tv.dotstart.beacon.repository.model.Service
import tv.dotstart.beacon.util.detailedErrorDialog
import tv.dotstart.beacon.util.logger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Manages the port mapping state along with any discovery logic.
 *
 * @author Johannes Donath
 * @date 01/12/2020
 */
object PortExposureProvider : Loader {

  private const val leaseDuration = 120L
  private const val refreshPeriod = 30L

  override val description = "gateway"

  private val logger = PortExposureProvider::class.logger

  private val locator = InternetGatewayDeviceLocator()
  private lateinit var device: InternetGatewayDevice

  private lateinit var scheduledExecutor: ScheduledExecutorService
  private val mappingLock = ReentrantLock()
  private val mappings = mutableMapOf<Service, List<PortMapping>>()

  var externalAddress: String? = null
    private set

  override fun load() {
    device = runBlocking {
      locator.locate()
          ?: throw PreloadError(
              "gateway",
              "Failed to locate UPnP compatible internet gateway")
    }

    logger.info(
        "Discovered gateway device \"${device.friendlyName}\" (${device.modelName}) provided by ${device.manufacturer} <${device.manufacturerUrl}>")

    externalAddress = device.getExternalAddress()
    logger.info("Discovered external address: $externalAddress")

    logger.info("Initializing lease refresh task")
    scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
    scheduledExecutor.scheduleAtFixedRate(
        { refreshPeriod }, refreshPeriod, refreshPeriod, TimeUnit.SECONDS)
  }

  fun expose(spec: Service) {
    mappingLock.withLock {
      val existing = mappings[spec]
      if (existing != null) {
        return
      }

      try {
        logger.info("Performing mapping registration for specification: $spec")
        val mappings = spec.ports
            .map {
              device.forward(it, "Beacon Mapping for ${spec.title}", leaseDuration)
            }

        PortExposureProvider.mappings[spec] = mappings
      } catch (ex: IncompatibleDeviceException) {
        logger.error("Failed to register port mapping: Incompatible device", ex)

        detailedErrorDialog(
            "Incompatible Gateway Device",
            "Your gateway device seems to be incompatible - Please report this issue to the Beacon developers",
            ex)
      } catch (ex: Throwable) {
        logger.error("Failed to register port mapping: Unknown error", ex)

        detailedErrorDialog(
            "Port Mapping Error",
            "Your port mapping request failed due to an unknown error",
            ex)
      }
    }
  }

  private fun refreshLeases() {
    mappingLock.withLock {
      mappings
          .forEach { spec, mappings ->
            logger.error("Refreshing mapping registrations for specification: $spec")

            try {
              mappings.forEach(PortMapping::refresh)
            } catch (ex: Throwable) {
              logger.error("Failed to renew one or more port mappings for specification: $spec", ex)

              Platform.runLater {
                detailedErrorDialog(
                    "Port Mapping Error",
                    "Your port mapping request failed due to an unknown error",
                    ex)
              }
            }
          }
    }
  }

  operator fun contains(spec: Service): Boolean = mappingLock.withLock {
    spec in mappings
  }

  fun close(spec: Service) {
    mappingLock.withLock {
      val mappings = mappings.remove(spec)
          ?: return

      try {
        logger.debug("Removing mapping registration for specification: $spec")
        mappings.forEach(PortMapping::remove)
      } catch (ex: IncompatibleDeviceException) {
        logger.error("Failed to remove port mapping: Incompatible device", ex)

        detailedErrorDialog(
            "Incompatible Gateway Device",
            "Your gateway device seems to be incompatible - Please report this issue to the Beacon developers",
            ex)
      } catch (ex: Throwable) {
        logger.error("Failed to register port mapping: Unknown error", ex)

        detailedErrorDialog(
            "Port Mapping Error",
            "Your port mapping request failed due to an unknown error",
            ex)
      }
    }
  }

  override fun shutdown() {
    logger.info("Cancelling lease refresh task")
    scheduledExecutor.shutdownNow()
    scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)

    logger.info("Removing remaining port mappings")
    mappingLock.withLock {
      mappings.forEach { spec, mappings ->
        logger.info("Removing mapping for port specification: $spec")

        mappings.forEach(PortMapping::remove)
      }
    }

    device.close()
  }
}
