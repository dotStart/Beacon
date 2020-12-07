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
package tv.dotstart.beacon.core.cache.filesystem

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tv.dotstart.beacon.core.cache.filesystem.path.PathProvider
import tv.dotstart.beacon.core.test.mock.any
import tv.dotstart.beacon.core.test.mock.mock
import tv.dotstart.beacon.core.test.mock.verify
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.streams.asSequence
import kotlin.test.*

/**
 * @author Johannes Donath
 * @date 07/12/2020
 */
internal class FileSystemCacheTest {

  private lateinit var cache: FileSystemCache
  private lateinit var root: Path
  private lateinit var pathProvider: PathProvider

  companion object {

    private val expirationDuration = Duration.ofMillis(500)
  }

  @BeforeEach
  fun prepareCacheProvider() {
    this.root = Files.createTempDirectory("beacon_test")
    this.pathProvider = mock()

    this.cache = FileSystemCache(this.root, expirationDuration, pathProvider = this.pathProvider)
  }

  @AfterEach
  fun cleanupCacheProvider() {
    Files.walk(this.root).asSequence()
        .sortedWith(Comparator.reverseOrder())
        .forEach { Files.delete(it) }
  }

  /**
   * Evaluates whether the implementation persists keys to disk.
   */
  @Test
  fun `It should persist keys`() {
    mock(this.pathProvider) {
      on { resolve(any(), any()) } answer { it.getArgument<Path>(0).resolve("test") }
    }

    this.cache.store("test", ByteArray(1))

    verify(this.pathProvider) {
      invocation { resolve(root, "test") }.occurredOnce()

      noMoreInteractions()
    }

    assertTrue(Files.exists(this.root.resolve("test")))

    val payload = Files.readAllBytes(this.root.resolve("test"))
    assertEquals(1, payload.size)
    assertEquals(0, payload[0])

    val stored = this.cache.get("test")
    Assertions.assertArrayEquals(payload, stored)

    this.cache.purge("test")
    assertFalse(Files.exists(this.root.resolve("test")))
  }

  /**
   * Evaluates whether the implementation honors expiration durations.
   */
  @Test
  fun `It should expire keys`() {
    mock(this.pathProvider) {
      on { resolve(any(), any()) } answer { it.getArgument<Path>(0).resolve("test") }
    }

    this.cache.store("test", ByteArray(1))

    Thread.sleep(1000)

    val stored = this.cache.get("test")
    assertNull(stored)

    val storedExpiredCustom = this.cache.get("test", Duration.ofMillis(750))
    assertNull(storedExpiredCustom)

    val storedNonExpiredCustom = this.cache.get("test", Duration.ofDays(1))
    assertNotNull(storedNonExpiredCustom)
  }
}
