/*
 * Copyright (C) 2019 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.beacon.repository.error

/**
 * Notifies the caller about a repository availability problem.
 *
 * This issue is typically thrown when the server returns an otherwise unknown error code when the
 * repository or some of its metadata is queried.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class RepositoryAvailabilityException(message: String? = null, cause: Throwable? = null) :
    IllegalRepositoryException(message, cause)
