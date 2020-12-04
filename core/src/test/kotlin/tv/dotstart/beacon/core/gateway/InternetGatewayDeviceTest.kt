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

import net.mm2d.upnp.Action
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.Device
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tv.dotstart.beacon.core.test.mock.any
import tv.dotstart.beacon.core.test.mock.mock
import tv.dotstart.beacon.core.test.mock.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Johannes Donath
 * @date 04/12/2020
 */
internal class InternetGatewayDeviceTest {

  private lateinit var cp: ControlPoint
  private lateinit var device: Device

  @BeforeEach
  fun prepareDevice() {
    this.cp = mock()
    this.device = mock()
  }

  /**
   * Evaluates whether the implementation exposes device metadata.
   */
  @Test
  fun `It exposes device metadata`() {
    mock(this.device) {
      on { friendlyName } returnValue "Some Router"
      on { modelName } returnValue "Some Model"
      on { manufacture } returnValue "Some Vendor"
      on { manufactureUrl } returnValue "https://example.org"
    }

    val device = InternetGatewayDevice(this.cp, this.device)

    assertEquals("Some Router", device.friendlyName)
    assertEquals("Some Model", device.modelName)
    assertEquals("Some Vendor", device.manufacturer)
    assertEquals("https://example.org", device.manufacturerUrl)

    verify(this.device) {
      invocation { friendlyName }.occurredOnce()
      invocation { modelName }.occurredOnce()
      invocation { manufacture }.occurredOnce()
      invocation { manufactureUrl }.occurredOnce()

      invocation { findAction(any()) } occurredTimes 3

      noMoreInteractions()
    }
  }

  /**
   * Evaluates whether the implementation properly detects device capabilities.
   */
  @Test
  fun `It detects feature availability`() {
    val device1 = InternetGatewayDevice(this.cp, this.device)
    assertFalse(device1.externalAddressAvailable)
    assertFalse(device1.portMappingAvailable)

    val mockAction = mock<Action>()
    mock(this.device) {
      on { findAction(InternetGatewayDevice.externalAddressActionName) } returnValue mockAction
    }
    val device2 = InternetGatewayDevice(this.cp, this.device)

    assertTrue(device2.externalAddressAvailable)
    assertFalse(device2.portMappingAvailable)

    mock(this.device) {
      on { findAction(InternetGatewayDevice.portRegistrationActionName) } returnValue mockAction
    }
    val device3 = InternetGatewayDevice(this.cp, this.device)

    assertTrue(device3.externalAddressAvailable)
    assertFalse(device3.portMappingAvailable)

    mock(this.device) {
      on { findAction(InternetGatewayDevice.portRemovalActionName) } returnValue mockAction
    }
    val device4 = InternetGatewayDevice(this.cp, this.device)

    assertTrue(device4.externalAddressAvailable)
    assertTrue(device4.portMappingAvailable)

    verify(this.device) {
      invocation { findAction(any()) } occurredTimes (4 * 3)
    }
  }
}
