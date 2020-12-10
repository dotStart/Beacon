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
package tv.dotstart.beacon.core.upnp

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import net.mm2d.upnp.Device
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tv.dotstart.beacon.core.test.mock.mock
import java.time.Duration
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * @author Johannes Donath
 * @date 04/12/2020
 */
internal class FlowDiscoveryListenerTest {

  private lateinit var listener: FlowDiscoveryListener

  private lateinit var device: Device

  @BeforeEach
  fun prepareListener() {
    this.device = mock()

    this.listener = FlowDiscoveryListener()
  }

  /**
   * Evaluates whether the implementation forwards discovery events to the resulting flow.
   */
  @Test
  fun `It forwards discovery events`() {
    thread {
      Thread.sleep(250)
      this.listener.onDiscover(this.device)
    }

    Assertions.assertTimeout(Duration.ofSeconds(2)) {
      val event = runBlocking {
        listener.events.firstOrNull()
      }

      assertNotNull(event)
      assertEquals(DeviceDiscoveryEvent.Type.DISCOVERED, event.type)
      assertSame(this.device, event.device)
    }
  }

  /**
   * Evaluates whether the implementation forwards loss events to the resulting flow.
   */
  @Test
  fun `It forwards loss events`() {
    thread {
      Thread.sleep(250)
      this.listener.onLost(this.device)
    }

    Assertions.assertTimeout(Duration.ofSeconds(2)) {
      val event = runBlocking {
        listener.events.firstOrNull()
      }

      assertNotNull(event)
      assertEquals(DeviceDiscoveryEvent.Type.LOST, event.type)
      assertSame(this.device, event.device)
    }
  }
}
