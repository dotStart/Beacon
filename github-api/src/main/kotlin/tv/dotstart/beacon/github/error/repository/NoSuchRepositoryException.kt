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
package tv.dotstart.beacon.github.error.repository

/**
 * Notifies the caller about an issue related to accessing a given repository.
 *
 * This exception is typically thrown when a given repository does not exist or cannot be accessed
 * with the current authentication.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 13/12/2020
 */
class NoSuchRepositoryException(
    repositoryOwner: String,
    repositoryName: String,
    cause: Throwable? = null) : RepositoryException(
    repositoryOwner,
    repositoryName,
    buildString {
      append("No such repository: ")
      append(repositoryOwner)
      append("/")
      append(repositoryName)
    },
    cause)
