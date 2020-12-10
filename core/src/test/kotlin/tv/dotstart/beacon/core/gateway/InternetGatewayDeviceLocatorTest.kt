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
package tv.dotstart.beacon.core.gateway

import kotlinx.coroutines.runBlocking
import net.mm2d.upnp.Action
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.Device
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tv.dotstart.beacon.core.test.mock.any
import tv.dotstart.beacon.core.test.mock.mock
import tv.dotstart.beacon.core.test.mock.verify
import java.net.NetworkInterface
import java.time.Duration
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.streams.asSequence
import kotlin.test.*

/**
 * @author Johannes Donath
 * @date 03/12/2020
 */
internal class InternetGatewayDeviceLocatorTest {

  private lateinit var deviceLocator: InternetGatewayDeviceLocator

  @BeforeEach
  fun prepareDeviceLocator() {
    val loopbackInterface = NetworkInterface.networkInterfaces().asSequence()
        .find { it.isLoopback }
        ?: throw IllegalStateException("No loopback interface present")

    this.deviceLocator = InternetGatewayDeviceLocator(
        listOf(loopbackInterface), Duration.ofSeconds(2))
  }

  /**
   * Evaluates whether the implementation selects compatible WAN devices when presented with
   * responses from non-compliant devices.
   */
  @Test
  fun `It locates WAN devices`() {
    val listeners = mutableListOf<ControlPoint.DiscoveryListener>()
    val barrier = CyclicBarrier(2)

    val registrationAction = mock<Action>()
    val removalAction = mock<Action>()

    val cp = mock<ControlPoint> {
      on { addDiscoveryListener(any()) } answer {
        listeners.add(it.getArgument(0))
        barrier.await()
      }
    }
    val incompatibleDevice = mock<Device> {
      on { deviceType } returnValue InternetGatewayDeviceLocator.deviceType
      on { friendlyName } returnValue "Incompatible Device"
      on { modelName } returnValue "Incompatible Device"
    }
    val compatibleDevice = mock<Device> {
      on { deviceType } returnValue InternetGatewayDeviceLocator.deviceType
      on { friendlyName } returnValue "Compatible Device"
      on { modelName } returnValue "Compatible Device"

      on {
        findAction(InternetGatewayDevice.portRegistrationActionName)
      } returnValue registrationAction
      on {
        findAction(InternetGatewayDevice.portRemovalActionName)
      } returnValue removalAction
    }

    thread {
      barrier.await()

      listeners.forEach { it.onDiscover(incompatibleDevice) }
      Thread.sleep(500)
      listeners.forEach { it.onDiscover(compatibleDevice) }
    }

    Assertions.assertTimeout(Duration.ofSeconds(2)) {
      val gateway = runBlocking {
        deviceLocator.locate(cp)
      }

      assertNotNull(gateway)
      assertEquals("Compatible Device", gateway.friendlyName)
      assertFalse(gateway.externalAddressAvailable)
      assertTrue(gateway.portMappingAvailable)
    }

    verify(cp) {
      invocation { addDiscoveryListener(any()) }.occurredOnce()
      invocation { search(InternetGatewayDeviceLocator.deviceType) }.occurredOnce()
      invocation { removeDiscoveryListener(any()) }.occurredOnce()

      noMoreInteractions()
    }
  }

  /**
   * Evaluates whether the implementation runs into a timeout when nothing responds to its query.
   */
  @Test
  fun `It returns null when the timeout is exceeded`() {
    val cp = mock<ControlPoint>()

    Assertions.assertTimeout(Duration.ofSeconds(5)) {
      val result = runBlocking {
        deviceLocator.locate(cp)
      }

      assertNull(result)
    }

    verify(cp) {
      invocation { addDiscoveryListener(any()) }.occurredOnce()
      invocation { search(InternetGatewayDeviceLocator.deviceType) }.occurredOnce()
      invocation { removeDiscoveryListener(any()) }.occurredOnce()

      noMoreInteractions()
    }
  }

  /**
   * Evaluates whether the implementation runs into a timeout when only incompatible devices respond
   * to its query.
   */
  @Test
  fun `It returns null when only incompatible devices respond`() {
    val barrier = CyclicBarrier(2)
    val cp = mock<ControlPoint>()

    val listeners = mutableListOf<ControlPoint.DiscoveryListener>()

    mock(cp) {
      on { addDiscoveryListener(any()) } answer {
        listeners.add(it.getArgument(0))
        barrier.await()
      }
    }
    val incompatibleDevice = mock<Device> {
      on { deviceType } returnValue InternetGatewayDeviceLocator.deviceType
      on { friendlyName } returnValue "Incompatible Device"
      on { modelName } returnValue "Incompatible Device"
    }

    thread {
      barrier.await()

      listeners.forEach { it.onDiscover(incompatibleDevice) }
    }

    Assertions.assertTimeout(Duration.ofSeconds(5)) {
      val result = runBlocking {
        deviceLocator.locate(cp)
      }

      assertNull(result)
    }

    verify(cp) {
      invocation { addDiscoveryListener(any()) }.occurredOnce()
      invocation {
        search(tv.dotstart.beacon.core.gateway.InternetGatewayDeviceLocator.deviceType)
      }.occurredOnce()
      invocation { removeDiscoveryListener(any()) }.occurredOnce()

      noMoreInteractions()
    }
  }
}
