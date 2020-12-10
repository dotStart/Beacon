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
package tv.dotstart.beacon.core.cache

import org.junit.jupiter.api.Assertions.assertNull
import tv.dotstart.beacon.core.cache.serialize.StringSerializer
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertSame

/**
 * @author Johannes Donath
 * @date 07/12/2020
 */
internal class NoopCacheProviderTest {

  companion object {

    private const val cacheKey = "test"
    private const val stringPayload = "Test"
  }

  /**
   * Evaluates whether the implementation discards all store operations returning only null values
   * as a result.
   */
  @Test
  fun `It does not persist data`() {
    val testPayload = ByteArray(1)

    assertNull(NoopCacheProvider.get(cacheKey))
    assertNull(NoopCacheProvider.get(cacheKey, Duration.ofSeconds(20)))
    assertNull(NoopCacheProvider.get(cacheKey, StringSerializer, Duration.ofDays(30)))

    NoopCacheProvider.store(cacheKey, testPayload)
    assertNull(NoopCacheProvider.get(cacheKey))

    NoopCacheProvider.store(cacheKey, StringSerializer, stringPayload)
    assertNull(NoopCacheProvider.get(cacheKey, StringSerializer))

    NoopCacheProvider.purge(cacheKey)
    assertNull(NoopCacheProvider.get(cacheKey))

    val populated = NoopCacheProvider.getOrPopulate(cacheKey) { testPayload }
    assertSame(testPayload, populated)
    assertNull(NoopCacheProvider.get(cacheKey))

    val populatedString = NoopCacheProvider.getOrPopulate(
        cacheKey, StringSerializer) { stringPayload }
    assertSame(stringPayload, populatedString)
    assertNull(NoopCacheProvider.get(cacheKey))
  }
}
