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
package tv.dotstart.beacon.github.interceptor

import okhttp3.Interceptor
import okhttp3.Response


/**
 * Provides an interceptor which replaces the `User-Agent` header in each respective processed
 * request.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 13/12/2020
 */
class UserAgentInterceptor(

    /**
     * Defines a user agent string which is to be applied to all requests passing through this
     * interceptor.
     */
    val userAgent: String) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
        .header("User-Agent", this.userAgent)
        .build()

    return chain.proceed(request)
  }
}
