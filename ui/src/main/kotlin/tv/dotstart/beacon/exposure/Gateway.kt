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

import io.reactivex.Observable
import javafx.application.Platform
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleObjectProperty
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.RemoteService
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.igd.callback.GetExternalIP
import tv.dotstart.beacon.preload.Loader
import tv.dotstart.beacon.upnp.UPnP
import tv.dotstart.beacon.util.logger
import java.net.InetAddress
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Exposes basic information on the current gateway device (if any).
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
object Gateway {

  private val igdDeviceType = UDADeviceType("InternetGatewayDevice", 1)
  private val connectionDeviceType = UDADeviceType("WANConnectionDevice", 1)

  private val ipServiceType = UDAServiceType("WANIPConnection", 1)
  private val pppServiceType = UDAServiceType("WANPPPConnection", 1)

  private val logger = Gateway::class.logger

  private val lock = ReentrantLock()
  private var service: Service<*, *>? = null

  private val _gatewayAddressProperty = SimpleObjectProperty<InetAddress?>()
  val gatewayAddressProperty: ReadOnlyProperty<InetAddress?>
    get() = this._gatewayAddressProperty
  val gatewayAddress: InetAddress?
    get() = this._gatewayAddressProperty.value

  private val _externalAddressProperty = SimpleObjectProperty<InetAddress?>()
  val externalAddressProperty: ReadOnlyProperty<InetAddress?>
    get() = this._externalAddressProperty
  val externalAddress: InetAddress?
    get() = this._externalAddressProperty.value

  /**
   * Attempts to retrieve an updated version of the external gateway address from a previously
   * discovered gateway.
   *
   * When no gateway has been discovered at the time of invocation, the external address will simply
   * be reset.
   */
  fun refresh() {
    this.lock.withLock(this::doRefresh)
  }

  private fun doRefresh() {
    logger.info("Refreshing gateway information")
    val service = this.service
    if (service == null) {
      logger.warn("No known gateway - Skipped")

      Platform.runLater {
        this._externalAddressProperty.set(null)
      }
      return
    }

    UPnP(object : GetExternalIP(service) {
      override fun failure(invocation: ActionInvocation<out Service<*, *>>,
          operation: UpnpResponse, defaultMsg: String) {
        logger.error("Failed to retrieve external gateway address")

        Platform.runLater {
          _externalAddressProperty.set(null)
        }
      }

      override fun success(externalIPAddress: String) {
        val addr = InetAddress.getByName(externalIPAddress)
        logger.info("Discovered external gateway address: $addr")

        Platform.runLater {
          _externalAddressProperty.set(addr)
        }
      }
    })
  }

  /**
   * Registers the implementation with the UPnP abstraction layer
   */
  // TODO: This registration method is ugly
  object RegistrationLoader : Loader {

    override val description = "preload.registration.gateway"

    override fun load() {
      UPnP.device
          .flatMap {
            logger.trace("""Searching for WAN service in "${it.displayString}"""")

            if (it.type != igdDeviceType) {
              return@flatMap Observable.empty<Device<*, *, *>>()
            }

            Observable.fromArray(
                *it.findDevices(connectionDeviceType)
            )
          }
          .flatMap {
            logger.trace("""Searching for IP or PPP capabilities in "${it.displayString}"""")

            val service = it.findService(ipServiceType) ?: it.findService(pppServiceType)

            if (service != null) {
              Observable.just(service)
            } else {
              Observable.empty()
            }
          }
          .filter { it is RemoteService }
          .subscribe {
            val device = it.device as RemoteDevice
            val gatewayHost = InetAddress.getByName(device.identity.descriptorURL.host)

            logger.info("""Discovered gateway device "${it.device.displayString} ($gatewayHost)"""")

            lock.withLock {
              service = it

              Platform.runLater {
                _gatewayAddressProperty.set(gatewayHost)
              }

              doRefresh()
            }
          }
    }
  }
}
