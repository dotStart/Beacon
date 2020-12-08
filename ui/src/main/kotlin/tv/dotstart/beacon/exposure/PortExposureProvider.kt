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
package tv.dotstart.beacon.exposure

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import kotlinx.coroutines.runBlocking
import tv.dotstart.beacon.core.Beacon
import tv.dotstart.beacon.core.delegate.logManager
import tv.dotstart.beacon.core.gateway.InternetGatewayDevice
import tv.dotstart.beacon.core.gateway.InternetGatewayDeviceLocator
import tv.dotstart.beacon.delegate.property
import tv.dotstart.beacon.preload.Loader
import tv.dotstart.beacon.preload.error.PreloadError
import tv.dotstart.beacon.repository.model.Service
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

    this.internetGatewayDevice = runBlocking {
      locator.locate()
    }

    val beacon = this.beacon
    beacon?.start()

    return beacon != null
  }

  fun expose(service: Service) {
    val beacon = this.beacon
        ?: throw IllegalStateException("No active beacon")

    runBlocking {
      beacon.expose(service)
    }
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
