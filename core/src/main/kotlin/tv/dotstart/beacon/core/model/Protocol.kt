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
package tv.dotstart.beacon.core.model

/**
 * Provides a listing of recognized network protocols.
 *
 * The values within this list are dictated by the supported set of protocols within the UPnP
 * specification. Newer protocols may thus be unavailable for selection. In these cases, users
 * should refer to the respective application documentation for more information (typically either
 * UDP or TCP are used as a base in order to provide backwards compatibility with older networking
 * hardware thus permitting their use in place of the actual protocol identification).
 *
 * @author Johannes Donath
 * @date 03/12/2020
 */
enum class Protocol {

  TCP,
  UDP
}
