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
import net.mm2d.upnp.Device
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tv.dotstart.beacon.core.model.Port
import tv.dotstart.beacon.core.model.Protocol
import tv.dotstart.beacon.core.test.mock.*
import kotlin.test.assertEquals

/**
 * @author Johannes Donath
 * @date 04/12/2020
 */
internal class PortMappingTest {

  private lateinit var mapping: PortMapping

  private lateinit var port: Port

  private lateinit var device: Device
  private lateinit var registrationAction: Action
  private lateinit var removalAction: Action

  companion object {

    private val protocol = Protocol.TCP
    private const val portNumber = 42
    private const val description = "Test"
    private const val duration = 10L
  }

  @BeforeEach
  fun preparePortMapping() {
    this.registrationAction = mock {
      on { invoke(any(), anyBoolean(), any(), any()) } answer {
        it.getArgument<(Map<String, String>) -> Unit>(2)(emptyMap())
      }
    }
    this.removalAction = mock {
      on { invoke(any(), anyBoolean(), any(), any()) } answer {
        it.getArgument<(Map<String, String>) -> Unit>(2)(emptyMap())
      }
    }

    this.device = mock {
      on { ipAddress } returnValue "127.0.0.1"
    }

    this.port = mock {
      on { protocol } returnValue protocol
      on { number } returnValue portNumber
    }

    this.mapping = PortMapping(this.device, this.registrationAction, this.removalAction,
                               this.port, description, duration)
  }

  /**
   * Evaluates whether the implementation performs registration requests upon refresh.
   */
  @Test
  fun `It sends registration requests`() {
    this.mapping.refresh()

    val parameterCaptor = captor<Map<String, String?>>()
    verify(this.registrationAction) {
      invocation { invoke(capture(parameterCaptor), anyBoolean(), any(), any()) }.occurredOnce()
      noMoreInteractions()
    }
    val parameters = parameterCaptor.value

    assertEquals("1", parameters[PortMapping.enabledParameterName])
    assertEquals("TCP", parameters[PortMapping.protocolParameterName])
    assertEquals("", parameters[PortMapping.remoteHostParameterName])
    assertEquals("42", parameters[PortMapping.externalPortParameterName])
    assertEquals("127.0.0.1", parameters[PortMapping.internalClientParameterName])
    assertEquals("42", parameters[PortMapping.internalPortParameterName])
    assertEquals("Test", parameters[PortMapping.descriptionParameterName])
    assertEquals("10", parameters[PortMapping.leaseDurationParameterName])
  }

  /**
   * Evaluates whether the implementation deletes registrations.
   */
  @Test
  fun `It sends removal requests`() {
    this.mapping.remove()

    val parameterCaptor = captor<Map<String, String?>>()
    verify(this.removalAction) {
      invocation { invoke(capture(parameterCaptor), anyBoolean(), any(), any()) }.occurredOnce()
      noMoreInteractions()
    }
    val parameters = parameterCaptor.value

    assertEquals("TCP", parameters[PortMapping.protocolParameterName])
    assertEquals("42", parameters[PortMapping.externalPortParameterName])
  }
}
