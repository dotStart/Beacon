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
package tv.dotstart.beacon.github.operations

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.dotstart.beacon.github.error.ServiceException
import tv.dotstart.beacon.github.error.repository.NoSuchRepositoryException
import tv.dotstart.beacon.github.model.repository.Release

/**
 * Provides access to various repository related operations.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 13/12/2020
 */
class RepositoryOperations(
    client: OkHttpClient,
    objectMapper: ObjectMapper,
    urlFactory: HttpUrlFactory,

    /**
     * Identifies the owner of the target repository (such as a user or organization).
     */
    val repositoryOwner: String,

    /**
     * Identifies the target repository.
     */
    val repositoryName: String) : AbstractOperations(client, objectMapper, urlFactory) {

  private fun createUrl(block: HttpUrl.Builder.() -> Unit) = this.urlFactory {
    addEncodedPathSegment("repos")
    addPathSegment(repositoryOwner)
    addPathSegment(repositoryName)

    this.block()
  }

  /**
   * Retrieves a listing of releases for this repository.
   *
   * @throws NullPointerException when [page] is given but [resultsPerPage] remains unset.
   */
  fun listReleases(resultsPerPage: Int? = null, page: Int? = null): List<Release> {
    if (page != null) {
      requireNotNull(resultsPerPage)
    }

    val url = this.createUrl {
      addEncodedPathSegment("releases")

      if (resultsPerPage != null) {
        addQueryParameter("per_page", resultsPerPage.toString())
      }
      if (page != null) {
        addQueryParameter("page", page.toString())
      }
    }

    val request = Request.Builder()
        .url(url)
        .build()

    return this.client.newCall(request).execute().use { response ->
      if (!response.isSuccessful && response.code != 404) {
        throw ServiceException(
            "Received illegal response code ${response.code} for request to repository $repositoryOwner/$repositoryName")
      }

      response.body
          ?.let {
            this.objectMapper.readValue<List<Release>>(it.string())
          }
          ?: throw NoSuchRepositoryException(this.repositoryOwner, this.repositoryName)
    }
  }
}
