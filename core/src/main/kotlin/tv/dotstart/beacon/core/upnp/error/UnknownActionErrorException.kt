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
package tv.dotstart.beacon.core.upnp.error

/**
 * Notifies a caller about an issue within a performed action which is unknown to the application.
 *
 * This exception is raised when the device returns an error code that is not recognized by the
 * application thus preventing proper reporting.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 06/12/2020
 */
class UnknownActionErrorException(message: String? = null, cause: Throwable? = null) :
    ActionException(message, cause)
