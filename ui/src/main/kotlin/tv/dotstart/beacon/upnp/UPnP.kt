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
package tv.dotstart.beacon.upnp

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.fourthline.cling.UpnpServiceImpl
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import tv.dotstart.beacon.preload.Loader
import tv.dotstart.beacon.util.logger
import java.util.concurrent.Future

/**
 * Exposes the UPnP client in an accessible and simple fashion.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
object UPnP {

  private val logger = UPnP::class.logger

  private lateinit var serviceImpl: UpnpServiceImpl

  private val deviceSubject = PublishSubject.create<Device<*, *, *>>()

  /**
   * Notifies subscribers about all devices which are discovered within the network.
   *
   * This observable will be closed when the service itself is shut down (e.g. when the application
   * shuts down).
   */
  val device: Observable<Device<*, *, *>>
    get() = this.deviceSubject

  /**
   * Performs a full device list refresh.
   */
  fun refresh() {
    logger.info("Performing UPnP device search")
    this.serviceImpl.controlPoint.search()
  }

  /**
   * Invokes an arbitrary UPnP command on the network.
   */
  operator fun invoke(
      callback: ActionCallback): Future<Any> = this.serviceImpl.controlPoint.execute(callback)

  /**
   * Handles the early initialization of the UPnP services.
   */
  object ServiceLoader : Loader {

    override val description = "preload.service"

    override fun load() {
      serviceImpl = UpnpServiceImpl(DiscoveryListener)
      refresh()
    }

    override fun shutdown() {
      serviceImpl.shutdown()
    }
  }

  /**
   * Forwards UPnP registry events to the public facing reactive components.
   */
  object DiscoveryListener : DefaultRegistryListener() {

    override fun deviceRemoved(registry: Registry,
        device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
      logger.debug(
          """Removed device "${device.displayString}" (${device.type} v${device.version.major}.${device.version.minor})""")
    }

    override fun deviceAdded(registry: Registry,
        device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
      logger.debug(
          """Discovered device "${device.displayString}" (${device.type} v${device.version.major}.${device.version.minor})""")
      deviceSubject.onNext(device)
    }

    override fun beforeShutdown(registry: Registry) {
      logger.info("Performing clean UPnP shutdown")
      deviceSubject.onComplete()
    }
  }
}
