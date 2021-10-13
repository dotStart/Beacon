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
package tv.dotstart.beacon.ui.exposure

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import kotlinx.coroutines.runBlocking
import tv.dotstart.beacon.core.Beacon
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.core.gateway.InternetGatewayDevice
import tv.dotstart.beacon.core.gateway.InternetGatewayDeviceLocator
import tv.dotstart.beacon.core.upnp.error.ActionException
import tv.dotstart.beacon.ui.delegate.property
import tv.dotstart.beacon.ui.preload.Loader
import tv.dotstart.beacon.ui.preload.error.PreloadError
import tv.dotstart.beacon.ui.repository.model.Service
import tv.dotstart.beacon.ui.util.ErrorReporter
import tv.dotstart.beacon.ui.util.actionErrorDialog
import java.io.Closeable

/**
 * Manages the port mapping state along with any discovery logic.
 *
 * @author Johannes Donath
 * @date 01/12/2020
 */
class PortExposureProvider : Closeable {

  companion object {

    private const val leaseDuration = 120L
    private const val refreshPeriod = 30L

    private val logger by logManager()
  }

  private val locator = InternetGatewayDeviceLocator()

  val internetGatewayDeviceProperty: ObjectProperty<InternetGatewayDevice> =
      SimpleObjectProperty()
  var internetGatewayDevice: InternetGatewayDevice? by property(internetGatewayDeviceProperty)

  private val _externalAddressProperty: StringProperty = SimpleStringProperty()
  val externalAddressProperty: ReadOnlyStringProperty
    get() = this._externalAddressProperty
  val externalAddress: String? by property(_externalAddressProperty)

  private val beaconProperty: ObjectProperty<Beacon> = SimpleObjectProperty()
  private val beacon: Beacon? by property(beaconProperty)

  init {
    // TODO: Terminate previous Beacon on change
    val beaconBinding = Bindings.createObjectBinding(
        { this.internetGatewayDevice?.let(::Beacon) },
        this.internetGatewayDeviceProperty)
    this.beaconProperty.bind(beaconBinding)
  }

  fun refresh(): Boolean {
    logger.info("Querying network for compatible internet gateway")

    try {
      logger.debug("Locating new gateway device within local network")
      this.internetGatewayDevice = runBlocking {
        locator.locate()
      }

      logger.debug("Requesting external address from gateway device (if present)")
      runBlocking {
        _externalAddressProperty.set(internetGatewayDevice?.getExternalAddress())
      }

      logger.debug("Attempting to initialize beacon instance for new gateway")
      val beacon = this.beacon
      beacon?.start()
    } catch (ex: ActionException) {
      logger.error("Gateway refresh failed", ex)
      actionErrorDialog(ex)
    }

    ErrorReporter.trace("exposure", "Internet gateway refresh performed")
    return beacon != null
  }

  fun expose(service: Service) {
    val beacon = this.beacon
        ?: throw IllegalStateException("No active beacon")

    try {
      runBlocking {
        beacon.expose(service)
      }
    } catch (ex: ActionException) {
      logger.error("UPnP exposure for service $service failed", ex)
      actionErrorDialog(ex)
    }

    ErrorReporter.trace("exposure", "Service exposed", data = mapOf(
        "service_id" to service.id,
        "service_title" to service.title
    ))
  }

  operator fun contains(service: Service): Boolean {
    val beacon = this.beacon
        ?: throw IllegalStateException("No active beacon")

    return service in beacon
  }

  fun close(service: Service) {
    val beacon = this.beacon
        ?: throw IllegalStateException("No active beacon")

    runBlocking {
      beacon.close(service)
    }

    ErrorReporter.trace("exposure", "Service closed", data = mapOf(
        "service_id" to service.id,
        "service_title" to service.title
    ))
  }

  override fun close() {
    this.beacon?.close()
    this.internetGatewayDevice?.close()
  }

  class Preloader(private val provider: PortExposureProvider) : Loader {

    override val description = "gateway"

    override val priority = Int.MAX_VALUE

    override fun load() {
      if (!this.provider.refresh()) {
        throw PreloadError(
            "gateway",
            "Failed to locate UPnP compatible internet gateway")
      }
    }

    override fun shutdown() {
      this.provider.close()
    }
  }
}
