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
package tv.dotstart.beacon.core.artifact.error

/**
 * Notifies a caller about an issue related to the protocol specific part of the artifact URI.
 *
 * This typically occurs when the payload is entirely malformed (e.g. does not fit the expected
 * format or is otherwise invalid).
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
class ArtifactSpecificationException(message: String? = null, cause: Throwable? = null) :
    ArtifactException(message, cause)
