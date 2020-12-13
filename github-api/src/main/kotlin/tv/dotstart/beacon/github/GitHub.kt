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
package tv.dotstart.beacon.github

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import tv.dotstart.beacon.github.GitHub.Factory
import tv.dotstart.beacon.github.interceptor.AcceptInterceptor
import tv.dotstart.beacon.github.interceptor.UserAgentInterceptor
import tv.dotstart.beacon.github.operations.RepositoryOperations

/**
 * Provides a central access point to various GitHub operations.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 13/12/2020
 */
class GitHub private constructor(

    /**
     * Defines an HTTP client which is used to perform all queries made through this type or any of
     * its respective operations subclasses.
     */
    private val client: OkHttpClient,

    /**
     * @see Factory.objectMapper
     */
    private val objectMapper: ObjectMapper,

    /**
     * @see Factory.baseUrl
     */
    val baseUrl: HttpUrl) {

  /**
   * Creates an HTTP URl for a given set of parameters.
   */
  private fun urlFactory(block: HttpUrl.Builder.() -> Unit): HttpUrl {
    return this.baseUrl.newBuilder()
        .also(block)
        .build()
  }

  fun forRepository(owner: String, name: String) = RepositoryOperations(
      this.client,
      this.objectMapper,
      this::urlFactory,
      owner,
      name)

  companion object {

    /**
     * Defines the media type which is to be passed via the `Accept` header.
     *
     * This value is used by GitHub in order to route the request to a compatible backend
     * implementation (e.g. to select the correct API revision).
     */
    private val acceptMediaType = "application/vnd.github.v3+json"
  }

  class Factory {

    /**
     * Identifies the location at which GitHub is reachable.
     *
     * By default, this will be set to `api.github.com` but may be overridden to support integration
     * with enterprise instances.
     */
    var baseUrl: HttpUrl = HttpUrl.Builder()
        .scheme("https")
        .host("api.github.com")
        .build()

    /**
     * Defines a user agent which is transmitted along with each respective request to the GitHub
     * endpoints.
     */
    var userAgent: String = "Beacon GitHub Library (+https://github.com/dotStart/Beacon)"

    /**
     * Defines an object mapper which is used to decode the contents of API responses.
     */
    var objectMapper: ObjectMapper = jacksonObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Creates a new GitHub client based on the given set of parameters within this factory.
     *
     */
    fun build(): GitHub {
      val client = OkHttpClient.Builder()
          .addInterceptor(UserAgentInterceptor(this.userAgent))
          .addInterceptor(AcceptInterceptor(acceptMediaType))
          .build()

      return GitHub(client, this.objectMapper, this.baseUrl)
    }
  }
}
