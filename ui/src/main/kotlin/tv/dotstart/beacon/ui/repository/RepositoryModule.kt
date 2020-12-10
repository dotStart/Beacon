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
package tv.dotstart.beacon.ui.repository

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.dotstart.beacon.ui.preload.Loader
import tv.dotstart.beacon.ui.storagePathQualifier
import java.nio.file.Path

/**
 * Exposes all components within this module to the injection framework.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 * @date 08/12/2020
 */
val repositoryModule = module {
  single {
    val storagePath = get<Path>(storagePathQualifier)
    ServiceRegistry(storagePath, get())
  }

  single<Loader>(named("systemRepositoryLoader")) { ServiceRegistry.SystemRepositoryLoader(get()) }
  single<Loader>(named("userRepositoryLoader")) {
    ServiceRegistry.UserRepositoryLoader(get(), get())
  }
  single<Loader>(named("customRepositoryLoader")) { ServiceRegistry.CustomRepositoryLoader(get()) }
}
