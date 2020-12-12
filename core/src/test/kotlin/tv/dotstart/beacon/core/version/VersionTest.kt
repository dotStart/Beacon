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
package tv.dotstart.beacon.core.version

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

/**
 * @author Johannes Donath
 * @date 12/12/2020
 */
internal class VersionTest {

  companion object {

    private val parserMap = mapOf(
        "0.1.0-alpha" to Version(0, 1, 0, "alpha", null, InstabilityType.ALPHA, null),
        "0.1.0-alpha.1" to Version(0, 1, 0, "alpha", 1, InstabilityType.ALPHA, null),
        "0.1.0" to Version(0, 1, 0, null, null, InstabilityType.INCUBATION, null),
        "0.1.0+git-abcdef" to
            Version(0, 1, 0, null, null, InstabilityType.INCUBATION, "git-abcdef"),
        "0.1.0-alpha+git-abcdef" to
            Version(0, 1, 0, "alpha", null, InstabilityType.ALPHA, "git-abcdef"),
        "2.1.3" to Version(2, 1, 3, null, null, InstabilityType.NONE, null),
        "2.1.3-alpha.ci.2" to Version(2, 1, 3, "alpha.ci", 2, InstabilityType.ALPHA, null),
    )

    private val compareList = listOf(
        Version(0, 1, 0, "alpha", null, InstabilityType.ALPHA, null),
        Version(0, 1, 0, "alpha", 1, InstabilityType.ALPHA, null),
        Version(0, 1, 0, "beta", null, InstabilityType.BETA, null),
        Version(0, 1, 0, "beta", 1, InstabilityType.BETA, null),
        Version(0, 1, 0, null, null, InstabilityType.INCUBATION, null),
        Version(0, 2, 0, "alpha", null, InstabilityType.ALPHA, null),
        Version(0, 2, 0, null, null, InstabilityType.INCUBATION, null),
        Version(1, 0, 0, "rc", null, InstabilityType.RELEASE_CANDIDATE, null),
        Version(1, 0, 0, null, null, InstabilityType.NONE, null),
        Version(1, 0, 1, null, null, InstabilityType.NONE, null),
        Version(2, 0, 1, null, null, InstabilityType.NONE, null)
    )
  }

  /**
   * Evaluates whether the implementation is capable of parsing human readable representations for
   * each respective component within the version number.
   */
  @TestFactory
  fun `It parses versions`() = parserMap
      .map { (input, expected) ->
        DynamicTest.dynamicTest(input) {
          val actual = Version.parse(input)
          assertEquals(expected, actual)
        }
      }

  /**
   * Evaluates whether the implementation is capable of comparing version numbers.
   */
  @TestFactory
  fun `It compares versions`() = compareList
      .flatMap { current ->
        val position = compareList.indexOf(current)

        compareList
            .mapIndexed { index, other ->
              val expected = position.compareTo(index)
              val operationName = when {
                expected < 0 -> "<"
                expected > 0 -> ">"
                else -> "=="
              }

              DynamicTest.dynamicTest("$current $operationName $other") {
                val actual = current.compareTo(other)

                assertEquals(expected, actual)
              }
            }
      }
}
