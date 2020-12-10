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
package tv.dotstart.beacon.core

import kotlinx.coroutines.*
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.core.gateway.InternetGatewayDevice
import tv.dotstart.beacon.core.gateway.PortMapping
import tv.dotstart.beacon.core.model.Service
import java.io.Closeable
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * Provides a UPnP port forwarding "beacon".
 *
 * This implementation will regularly transmit lease renewals for a given set of services until
 * explicitly stopped. As such, leases will time out if the application is terminated unexpectedly.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 06/12/2020
 */
class Beacon(

    /**
     * Defines a gateway device to which all forwarding requests are transmitted.
     */
    private val gateway: InternetGatewayDevice,

    /**
     * Defines the duration for which leases remain valid.
     */
    private val leaseDuration: Long = 60,

    /**
     * Defines the duration for which leases will remain valid when not renewed following the
     * expiration of [leaseDuration].
     */
    private val graceDuration: Long = 30) : Closeable, Iterable<Service> {

  private val runtimeLock = ReentrantLock()

  private val executor: ExecutorService = Executors.newSingleThreadExecutor(
      { thread(name = "beacon", start = false, block = it::run) })
  private val dispatcher: CoroutineDispatcher = executor
      .asCoroutineDispatcher()

  private var renewalJob: Job? = null

  private val services = mutableListOf<Service>()
  private val mappings = mutableMapOf<URI, ServiceMapping>()

  companion object {

    private val logger by logManager()
  }

  /**
   * Initializes the beacon renewal process.
   */
  fun start() {
    this.runtimeLock.withLock {
      this.dispatcher.ensureActive()

      if (renewalJob != null) {
        return
      }

      dispatcher.ensureActive()
      renewalJob = GlobalScope.launch(dispatcher + CoroutineName("beacon-renewal")) {
        renewalLoop()
      }
    }
  }

  /**
   * Exposes all ports associated with a given service for the lifetime of this beacon.
   */
  suspend fun expose(service: Service) {
    withContext(this.dispatcher) {
      check(renewalJob != null) { "Beacon is inactive" }

      // return immediately if the service is already announced by this beacon as it would otherwise
      // cause an error to be thrown
      if (service.id in mappings) {
        return@withContext
      }

      // register a new port mapping with the underlying gateway device in order to establish a
      // lease for the desired service - renewal will take place when the loop continues the next
      // time (at most in the amount of seconds specified by leaseDuration)
      val createdMappings = service.ports
          .map {
            logger.info("Requesting mapping for $it")

            gateway.forward(
                it, "Beacon for ${service.title}", leaseDuration + graceDuration)
          }

      val serviceMapping = ServiceMapping(createdMappings)
      services += service
      mappings[service.id] = serviceMapping
    }
  }

  private suspend fun renewalLoop() {
    logger.info("Starting renewal loop")

    coroutineScope {
      while (isActive) {
        try {
          delay(leaseDuration * 1000)
        } catch (ex: CancellationException) {
          logger.debug("Interrupted while awaiting next renewal", ex)
          break
        }

        logger.debug("Performing renewal of all active leases")

        mappings.values
            .forEach { mapping ->
              mapping.refresh()
            }
      }
    }

    logger.info("Shut down renewal loop")
  }

  /**
   * Closes a previously opened service.
   */
  suspend fun close(service: Service) {
    withContext(this.dispatcher) {
      val mappings = mappings.remove(service.id)
          ?: return@withContext

      mappings.remove()
      services -= service
    }
  }

  override fun close() {
    this.runtimeLock.withLock {
      logger.info("Shutting down renewal loop")
      runBlocking {
        renewalJob?.cancelAndJoin()
      }

      logger.info("Shutting down remaining coroutines")
      dispatcher.cancel()

      this.executor.shutdownNow()
      this.executor.awaitTermination(2, TimeUnit.MINUTES)

      logger.info("Removing remaining mappings")
      runBlocking {
        mappings.values
            .forEach { mapping -> mapping.remove() }
      }
    }
  }

  override fun iterator(): Iterator<Service> = runBlocking(this.dispatcher) {
    services.toList().iterator()
  }

  /**
   * Stores the mappings for a given service.
   */
  private data class ServiceMapping(private val mappings: List<PortMapping>) {

    /**
     * Shorthand for [PortMapping.refresh]
     */
    suspend fun refresh() {
      this.mappings.forEach { mapping ->
        logger.info("Refreshing mapping for ${mapping.port}")
        mapping.refresh()
      }
    }

    /**
     * Shorthand for [PortMapping.remove]
     */
    suspend fun remove() {
      this.mappings.forEach { mapping ->
        try {
          logger.info("Removing mapping for ${mapping.port}")
          mapping.remove()
        } catch (ex: Throwable) {
          // [#13] removal errors are ignored as this typically implies that the gateway no longer
          // recognizes a given port
          logger.error("Failed to remove mapping for port ${mapping.port}", ex)
        }
      }
    }
  }
}
