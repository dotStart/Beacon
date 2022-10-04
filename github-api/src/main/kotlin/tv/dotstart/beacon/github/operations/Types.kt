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

import okhttp3.HttpUrl

/**
 * Provides various types for use throughout the operations API.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 13/12/2020
 */

/**
 * Defines a factory which permits the creation of HttpUrls in accordance with a common template.
 */
typealias HttpUrlFactory = (HttpUrl.Builder.() -> Unit) -> HttpUrl