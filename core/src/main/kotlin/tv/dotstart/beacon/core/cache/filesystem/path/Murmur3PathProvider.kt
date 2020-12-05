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
package tv.dotstart.beacon.core.cache.filesystem.path

import com.sangupta.murmur.Murmur3
import java.nio.file.Path

/**
 * Provides a path provider which hashes its cache keys using the Murmur 3 hashing algorithm.
 *
 * This implementation is typically preferable over classic MessageDigest based implementations
 * due to its improved performance.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 05/12/2020
 */
class Murmur3PathProvider(

    /**
     * Defines the seed with which the hash algorithm is initialized.
     */
    private val seed: Long) : PathProvider {

  override fun resolve(root: Path, key: String): Path {
    val keyBytes = key.toByteArray(Charsets.UTF_8)
    val hashData = Murmur3.hash_x64_128(keyBytes, keyBytes.size, this.seed)

    val fileName = buildString {
      hashData.forEach {
        append("%016X".format(it))
      }
    }
    val filePrefix = fileName.substring(0..1)

    return root.resolve(filePrefix)
        .resolve(fileName)
  }
}
