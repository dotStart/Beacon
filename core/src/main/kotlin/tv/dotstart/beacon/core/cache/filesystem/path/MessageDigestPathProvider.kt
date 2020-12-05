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

import tv.dotstart.beacon.core.util.toUppercaseHex
import java.nio.file.Path
import java.security.MessageDigest

/**
 * Provides a [MessageDigest] based path provider implementation.
 *
 * @author Johannes Donath
 * @date 05/12/2020
 */
open class MessageDigestPathProvider(

    /**
     * Defines a message digest implementation which is used to compute key hashes.
     */
    private val digest: MessageDigest) : PathProvider {

  companion object {

    /**
     * Provides a SHA-256 based path provider.
     */
    val sha256 by lazy { MessageDigestPathProvider(MessageDigest.getInstance("SHA-256")) }
  }

  override fun resolve(root: Path, key: String): Path {
    val keyBytes = key.toByteArray(Charsets.UTF_8)
    val hashBytes = this.digest.digest(keyBytes)

    return this.resolve(root, hashBytes)
  }

  /**
   * Resolves a cache file based on its computed hash.
   */
  protected open fun resolve(root: Path, hash: ByteArray): Path {
    val hashString = hash.toUppercaseHex()
    val hashPrefix = hashString.substring(0..1)

    return root.resolve(hashPrefix).resolve(hashString)
  }
}
